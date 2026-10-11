# SPDX-License-Identifier: GPL-3.0-or-later
"""Exercise the shipped helper in isolated fake installations, never the user's game."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
import zipfile

ROOT = Path(__file__).resolve().parents[2]
HELPER = ROOT / 'core/src/main/assets/updates/windows-update.ps1'

@unittest.skipUnless(os.name == 'nt', 'Windows installer integration')
class WindowsInstallerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        directory = ROOT / '.local/update-tests'
        directory.mkdir(parents=True, exist_ok=True)
        cls.workspace = tempfile.TemporaryDirectory(prefix='helper-', dir=directory)
        cls.root = Path(cls.workspace.name)
        source = cls.root / 'Test.cs'
        source.write_text('class Test { static int Main() { return 1; } }', encoding='utf-8')
        cls.exe = cls.root / 'FakeGame.exe'
        compiler = Path(os.environ['SystemRoot']) / 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
        subprocess.run([str(compiler), '/nologo', '/target:winexe', '/out:' + str(cls.exe), str(source)], check=True, capture_output=True)

    @classmethod
    def tearDownClass(cls):
        cls.workspace.cleanup()

    def setUp(self):
        self.case = Path(tempfile.mkdtemp(dir=self.root))
        self.target = self.case / 'Grimhollow'
        self.cache = self.case / 'cache'; self.cache.mkdir()
        self.target.mkdir()
        (self.target / 'Grimhollow.exe').write_bytes(self.exe.read_bytes())
        (self.target / 'update-package.json').write_text(json.dumps(dict(schema=1, versionCode=1017, versionName='1.29.18', platform='windows-x64')))
        (self.target / 'original.txt').write_text('old game')
        self.archive = self.cache / 'package-1018.zip'

    def package(self, extra=None, version=1018):
        with zipfile.ZipFile(self.archive, 'w') as archive:
            archive.writestr('Grimhollow/update-package.json', json.dumps(dict(schema=1,versionCode=version,versionName='1.30.0',platform='windows-x64')))
            archive.writestr('Grimhollow/Grimhollow.exe', self.exe.read_bytes())
            archive.writestr('Grimhollow/runtime/bin/java.exe', b'fake runtime')
            archive.writestr('Grimhollow/new.txt', b'new game')
            if extra:
                for name in extra: archive.writestr(name,b'unsafe')

    def run_helper(self, validate=True, checksum=None, code=1018):
        command = ['powershell.exe','-NoProfile','-NonInteractive','-WindowStyle','Hidden','-ExecutionPolicy','Bypass','-File',str(HELPER),
                   '-Target',str(self.target),'-Archive',str(self.archive),'-ExpectedSHA256',checksum or hashlib.sha256(self.archive.read_bytes()).hexdigest(),
                   '-VersionCode',str(code),'-ParentPid','999999','-StatusDirectory',str(self.cache)]
        if validate: command.append('-ValidateOnly')
        return subprocess.run(command,capture_output=True,text=True,timeout=90)

    def unchanged(self):
        self.assertEqual((self.target / 'original.txt').read_text(),'old game')
        self.assertFalse((self.target / 'new.txt').exists())

    def test_valid_archive_stages_without_changing_current_game(self):
        self.package();result=self.run_helper();self.assertEqual(result.returncode,0,result.stdout+result.stderr);self.unchanged()

    def test_path_traversal_is_rejected(self):
        self.package(['../outside.txt']);result=self.run_helper();self.assertNotEqual(result.returncode,0);self.unchanged();self.assertFalse((self.case/'outside.txt').exists())

    def test_checksum_mismatch_is_rejected(self):
        self.package();self.assertNotEqual(self.run_helper(checksum='0'*64).returncode,0);self.unchanged()

    def test_case_insensitive_duplicate_is_rejected(self):
        self.package(['Grimhollow/new.TXT']);self.assertNotEqual(self.run_helper().returncode,0);self.unchanged()

    def test_wrong_package_version_is_rejected(self):
        self.package(version=1020);self.assertNotEqual(self.run_helper().returncode,0);self.unchanged()

    def test_failed_new_launch_restores_the_previous_application(self):
        self.package();result=self.run_helper(validate=False);self.assertNotEqual(result.returncode,0);self.unchanged()
        status=json.loads((self.cache/'install-status.json').read_text(encoding='utf-8-sig'))
        self.assertEqual(status['phase'],'rolled-back',result.stdout+result.stderr)

if __name__ == '__main__': unittest.main(verbosity=2)
