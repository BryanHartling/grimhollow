# SPDX-License-Identifier: GPL-3.0-or-later
"""Build and sign public update packages. Private keys never enter the repository."""
import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import zipfile

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa

ROOT = Path(__file__).resolve().parents[2]
PUBLIC = ROOT / 'core/src/main/assets/updates/public-key.der'


def metadata():
    text = (ROOT / 'build.gradle').read_text(encoding='utf-8')
    return int(re.search(r'appVersionCode = (\d+)', text)[1]), re.search(r"appVersionName = '([^']+)'", text)[1]


def digest(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def validate_windows(directory, code, version):
    if not (directory / 'Grimhollow.exe').is_file() or not (directory / 'runtime/bin/java.exe').is_file():
        raise ValueError('Incomplete Windows package')
    details = json.loads((directory / 'update-package.json').read_text(encoding='utf-8'))
    if details != dict(schema=1, versionCode=code, versionName=version, platform='windows-x64'):
        raise ValueError('Windows package metadata mismatch')
    jars = list((directory / 'app').glob('desktop-*.jar'))
    if len(jars) != 1:
        raise ValueError('Windows package must contain exactly one game jar')
    with zipfile.ZipFile(jars[0]) as archive:
        manifest = archive.read('META-INF/MANIFEST.MF').decode('utf-8')
        if f'Implementation-Version: {code}\r\n' not in manifest or f'Specification-Version: {version}\r\n' not in manifest:
            raise ValueError('Windows executable and version metadata differ')


def build(directory, windows, apk, tag, private_file=None):
    code, version = metadata()
    if tag != f'v{version}-autoupdate' and tag != f'v{version}' and not re.fullmatch(r'v' + re.escape(version) + r'-[a-z0-9-]+', tag):
        raise ValueError('Release tag must match the built version')
    validate_windows(windows, code, version)
    directory.mkdir(parents=True, exist_ok=True)
    win = directory / 'grimhollow-windows-x64.zip'
    with zipfile.ZipFile(win, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for file in sorted(windows.rglob('*')):
            if file.is_symlink():
                raise ValueError('Symlinks are not allowed in update packages')
            if file.is_file():
                archive.write(file, 'Grimhollow/' + file.relative_to(windows).as_posix())
    android = directory / 'grimhollow-android.apk'
    import shutil
    shutil.copyfile(apk, android)
    # This metadata is generated only in a job dependent on every acceptance job.
    channel = os.environ.get('GRIMHOLLOW_RELEASE_CHANNEL', 'stable')
    if channel not in ('stable', 'playtest'):
        raise ValueError('Invalid release channel')
    assets = {}
    for platform, file in [('windows-x64', win), ('android', android)]:
        assets[platform] = dict(name=file.name, size=file.stat().st_size, sha256=digest(file),
                              url=f'https://github.com/bryanhartling/grimhollow/releases/download/{tag}/{file.name}')
    data = dict(schema=1, repository='bryanhartling/grimhollow', tag=tag, versionCode=code,
                versionName=version, channel=channel, minAndroid=21, assets=assets)
    raw = (json.dumps(data, sort_keys=True, separators=(',', ':')) + '\n').encode('utf-8')
    pem = private_file.read_bytes() if private_file else base64.b64decode(os.environ['GRIMHOLLOW_UPDATE_PRIVATE_KEY_B64'], validate=True)
    key = serialization.load_pem_private_key(pem, password=None)
    public = key.public_key().public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)
    if public != PUBLIC.read_bytes():
        raise ValueError('Signing key does not match the public key shipped in the game')
    signature = key.sign(raw, padding.PKCS1v15(), hashes.SHA256())
    key.public_key().verify(signature, raw, padding.PKCS1v15(), hashes.SHA256())
    (directory / 'release-manifest.json').write_bytes(raw)
    (directory / 'release-manifest.sig').write_text(base64.b64encode(signature).decode() + '\n', encoding='ascii')
    (directory / 'SHA256SUMS').write_text(''.join(f'{digest(p)}  {p.name}\n' for p in [win, android, directory / 'release-manifest.json', directory / 'release-manifest.sig']), encoding='ascii')
    (directory / 'release-notes.md').write_text(f'Grimhollow {version}\n\nWindows: download and extract the complete ZIP, then run Grimhollow.exe.\nAndroid: install the APK over the existing app; keep your existing data.\n\nFuture releases can be downloaded from the home-screen update dialog.\n\n' + os.environ.get('GRIMHOLLOW_RELEASE_NOTES', 'See the in-game Update Log for changes.') + '\n', encoding='utf-8')
    print(f'Signed {channel} release {tag}, build {code}; packages and manifest ready')


def init_key(directory):
    directory.mkdir(parents=True, exist_ok=True)
    private = directory / 'update-private.pem'
    if private.exists() or PUBLIC.exists():
        raise ValueError('Refusing to overwrite an existing signing identity')
    key = rsa.generate_private_key(public_exponent=65537, key_size=3072)
    private.write_bytes(key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8, serialization.NoEncryption()))
    PUBLIC.parent.mkdir(parents=True, exist_ok=True)
    PUBLIC.write_bytes(key.public_key().public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo))
    print('Created local signing key; only public-key.der may be committed')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest='command', required=True)
    init = sub.add_parser('init-key'); init.add_argument('--directory', type=Path, required=True)
    make = sub.add_parser('build')
    for arg in ('directory', 'windows', 'apk'):
        make.add_argument('--' + arg, type=Path, required=True)
    make.add_argument('--tag', required=True); make.add_argument('--private-file', type=Path)
    args = parser.parse_args()
    if args.command == 'init-key': init_key(args.directory)
    else: build(args.directory, args.windows, args.apk, args.tag, args.private_file)
