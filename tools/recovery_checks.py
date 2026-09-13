#!/usr/bin/env python3
"""Recovery acceptance over real renderer evidence and compiled call sites.

This tool never generates or changes shipped art. It replaces the retired
per-tile art-loop checks with the requested within-room comparisons.
"""
from pathlib import Path
import argparse
import colorsys
import itertools
import json
import os
import re
import subprocess
import zipfile
import numpy as np
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
REGIONS=('sewers','prison','caves','city','halls')


def room(region):
    folder=ROOT/'verification/recovery'/region
    data=json.loads((folder/'room.json').read_text(encoding='utf-8'))
    image=np.asarray(Image.open(folder/'lit.png').convert('RGB'),dtype=float)/255
    grouped={}
    if not data['lighting']:raise AssertionError('Unlit recovery screenshot')
    for cell in data['cells']:
        # Hero pixels are not terrain; all other visible cells, including decor,
        # participate with their complete on-screen tile rectangle.
        if cell['hero']:continue
        left,top,right,bottom=cell['box']
        if left<0 or top<0 or right>image.shape[1] or bottom>image.shape[0]:
            raise AssertionError('Room is cropped')
        pixels=image[top:bottom,left:right,:].reshape(-1,3)
        if len(pixels):grouped.setdefault(cell['kind'],[]).append(pixels)
    measurements={}
    for kind,parts in sorted(grouped.items()):
        pixels=np.concatenate(parts)
        # Match the existing screenshot luminance definition. Hue needs a
        # circular mean (359 and 1 degrees average to 0, not 180). Achromatic
        # pixels have no hue; they still contribute to mean luminance.
        hues=[colorsys.rgb_to_hsv(*pixel)[0]*2*np.pi for pixel in pixels
              if pixel.max()>pixel.min()]
        hue=float(np.degrees(np.angle(np.mean(np.exp(1j*np.asarray(hues))))))%360 if hues else 0
        measurements[kind]=dict(luminance=float((pixels@np.array([.2126,.7152,.0722])).mean()),
                               hue=hue,pixels=len(pixels))
    pairs=[]
    for a,b in itertools.combinations(measurements,2):
        x,y=measurements[a],measurements[b]
        dl=abs(x['luminance']-y['luminance']);dh=abs(x['hue']-y['hue']);dh=min(dh,360-dh)
        pairs.append(dict(types=[a,b],luminance_difference=dl,hue_difference=dh,passing=dl>=.12 or dh>=40))
    failures=[p for p in pairs if not p['passing']]
    if len(measurements)<4:failures.append(dict(reason='Fewer than four visible terrain types'))
    report=dict(region=region,seed=data['seed'],measurements=measurements,pairs=pairs,failures=failures)
    (folder/'distinctness.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(f'TEST 45: region={region} types={len(measurements)} pairs={len(pairs)} failures={len(failures)}')
    for pair in pairs:
        print('/'.join(pair['types'])+f": delta L={pair['luminance_difference']:.4f}, delta hue={pair['hue_difference']:.2f} "+('PASS' if pair['passing'] else 'FAIL'))
    return bool(failures)


def handlers(jar):
    java=Path(os.environ.get('JAVA_HOME',''))/'bin'/('javap.exe' if os.name=='nt' else 'javap')
    if not java.exists():java=Path('javap')
    with zipfile.ZipFile(jar) as z:
        classes=[p[:-6].replace('/','.') for p in z.namelist() if p.endswith('.class') and
                 (p.startswith('com/shatteredpixel/') or p.startswith('com/watabou/utils/PlatformSupport') or p.startswith('com/watabou/utils/RepositoryUris'))]
    failures=[];opens=[];fetches=[]
    for offset in range(0,len(classes),30):
        text=subprocess.check_output([str(java),'-J-Dfile.encoding=UTF-8','-c','-p','-classpath',str(jar)]+classes[offset:offset+30],text=True,encoding='utf-8')
        current=''
        for line in text.splitlines():
            header=re.search(r'\b(?:class|interface) ([\w.$]+)',line)
            if header and not line.startswith(' '):current=header[1]
            call=re.search(r'^\s*\d+:\s+(invoke\w+)\s+.*// (?:InterfaceMethod|Method) ([\w/$]+)\.([^:]+):',line)
            if not call:continue
            opcode,owner,name=call.groups()
            if owner=='com/badlogic/gdx/Net' and name=='openURI':
                opens.append(current)
                if current!='com.watabou.utils.PlatformSupport':failures.append('Unguarded URL sink in '+current)
            if (name in ('sendHttpRequest','newClientSocket','newServerSocket','openConnection','openStream','getInputStream','connect','send','sendAsync') and
                    (owner=='com/badlogic/gdx/Net' or owner.startswith(('java/net/','javax/net/')))):
                fetches.append(current+' -> '+owner+'.'+name)
            if name=='"<init>"' and owner in ('java/net/Socket','java/net/DatagramSocket','java/net/ServerSocket'):
                fetches.append(current+' -> '+owner+'.'+name)
    failures.extend(fetches)
    if opens!=['com.watabou.utils.PlatformSupport']:failures.append('Unexpected browser sink inventory '+str(opens))
    # These modules contain old upstream feed clients; they must not be packaged.
    if any(c.endswith(('.GitHubUpdates','.ShatteredNews','.DebugUpdates','.DebugNews')) for c in classes):
        failures.append('Legacy remote service packaged')
    print(f'TEST 46 compiled handlers: classes={len(classes)} guarded browser sinks={len(opens)} HTTP/socket calls={len(fetches)} failures={len(failures)}')
    for f in failures:print('FAIL:',f)
    return bool(failures)


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--region',choices=REGIONS);p.add_argument('--all-regions',action='store_true');p.add_argument('--jar',type=Path)
    args=p.parse_args();failed=False
    if args.jar:failed|=handlers(args.jar)
    for region in REGIONS if args.all_regions else ([args.region] if args.region else []):failed|=room(region)
    raise SystemExit(failed)
