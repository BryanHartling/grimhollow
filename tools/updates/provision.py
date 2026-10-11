# SPDX-License-Identifier: GPL-3.0-or-later
"""Upload signing secrets using a process-only token. Never print secret contents."""
import base64
import json
import os
from pathlib import Path
import urllib.request
from nacl.public import PublicKey, SealedBox

API = 'https://api.github.com/repos/bryanhartling/grimhollow/actions/secrets'

def request(url, method='GET', data=None):
    headers = {'Authorization': 'Bearer ' + os.environ['GRIMHOLLOW_TOKEN'],
               'Accept': 'application/vnd.github+json', 'X-GitHub-Api-Version': '2022-11-28'}
    req = urllib.request.Request(url, data=json.dumps(data).encode() if data else None, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=30) as response:
        content = response.read()
        return json.loads(content) if content else None

def provision(directory):
    info = request(API + '/public-key')
    box = SealedBox(PublicKey(base64.b64decode(info['key'])))
    secrets = {
        'GRIMHOLLOW_UPDATE_PRIVATE_KEY_B64': base64.b64encode((directory / 'update-private.pem').read_bytes()).decode(),
        'GRIMHOLLOW_ANDROID_KEYSTORE_B64': base64.b64encode((directory / 'android-release.p12').read_bytes()).decode(),
        'GRIMHOLLOW_ANDROID_STORE_PASSWORD': os.environ['GRIMHOLLOW_ANDROID_STORE_PASSWORD'],
        'GRIMHOLLOW_ANDROID_KEY_PASSWORD': os.environ['GRIMHOLLOW_ANDROID_STORE_PASSWORD'],
    }
    for name, value in secrets.items():
        encrypted = base64.b64encode(box.encrypt(value.encode())).decode()
        request(API + '/' + name, 'PUT', dict(encrypted_value=encrypted, key_id=info['key_id']))
        print('Configured ' + name)

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(); parser.add_argument('directory', type=Path)
    provision(parser.parse_args().directory)
