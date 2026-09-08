"""Install the pinned portable Blender LTS in the ignored project toolchain."""
from pathlib import Path
import hashlib
import urllib.request
import zipfile

ROOT=Path(__file__).resolve().parents[2]
VERSION='4.5.13'
BASE='https://download.blender.org/release/Blender4.5/'
NAME=f'blender-{VERSION}-windows-x64.zip'
destination=ROOT/'.toolchain/blender'

def fetch(name,path):
    request=urllib.request.Request(BASE+name,headers={'User-Agent':'Grimhollow-art-pipeline'})
    with urllib.request.urlopen(request,timeout=45) as source,path.open('wb') as out:
        while data:=source.read(1024*1024):out.write(data)

if __name__=='__main__':
    if (destination/'blender.exe').exists():
        print('Blender already installed:',destination)
    else:
        archive=ROOT/'.toolchain'/NAME
        sums=ROOT/'.toolchain'/f'blender-{VERSION}.sha256'
        fetch(f'blender-{VERSION}.sha256',sums)
        expected=next(line.split()[0] for line in sums.read_text().splitlines() if line.endswith(NAME))
        if not archive.exists() or hashlib.sha256(archive.read_bytes()).hexdigest()!=expected:fetch(NAME,archive)
        assert hashlib.sha256(archive.read_bytes()).hexdigest()==expected,'Blender download checksum mismatch'
        destination.mkdir(parents=True,exist_ok=True)
        with zipfile.ZipFile(archive) as package:
            prefix=f'blender-{VERSION}-windows-x64/'
            for member in package.infolist():
                assert member.filename.startswith(prefix)
                relative=member.filename[len(prefix):]
                if not relative:continue
                target=(destination/relative).resolve()
                assert target.is_relative_to(destination.resolve())
                if member.is_dir():target.mkdir(parents=True,exist_ok=True)
                else:
                    target.parent.mkdir(parents=True,exist_ok=True)
                    target.write_bytes(package.read(member))
        print('Installed Blender',VERSION,'SHA256',expected,'at',destination)
