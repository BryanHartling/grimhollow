"""SPDX-License-Identifier: GPL-3.0-or-later.
One-time, repeatable conversion of GPL upstream tile silhouettes into editable JSON.
The normal build never needs Git or upstream PNGs. New icons are original primitives.
"""
import io
import json
import shutil
import subprocess
import numpy as np
from PIL import Image
from build import ROOT, SPEC_DIR, COLORS

BASE = '7b8b845a76fe76c6b7c031ae9e570852411f56db'
git = shutil.which('git')
if '--region' in __import__('sys').argv:
    region=__import__('sys').argv[__import__('sys').argv.index('--region')+1]
    if region not in ('prison','caves','city','halls'):raise ValueError('Unknown region')
    name='tiles_'+region;source=subprocess.check_output([git,'show',BASE+':core/src/main/assets/environment/'+name+'.png'],cwd=ROOT)
    image=Image.open(io.BytesIO(source)).convert('RGBA');pixels=np.array(image);runs=[]
    for y,row in enumerate(pixels):
        x=0
        while x<len(row):
            if row[x,3]<128:x+=1;continue
            role=int(np.argmin(np.sum((COLORS.astype(np.int32)-row[x,:3].astype(np.int32))**2,axis=1)))
            end=x+1
            while end<len(row) and np.array_equal(row[end],row[x]):end+=1
            runs.append([y,x,end-x,role]);x=end
    mapping=json.loads((SPEC_DIR/'tiles_sewers.json').read_text())['rendered_tiles']
    spec=dict(kind='tile',dimensions=[image.width*4,image.height*4],source_dimensions=list(image.size),frame=[64,64],
        silhouette=runs,seed=417,region=region,rendered_tiles={i:k for i,k in mapping.items() if k!='grass'},
        provenance='GPL-3.0-or-later upstream '+BASE,output='core/src/main/assets/environment/'+name+'.png')
    (SPEC_DIR/(name+'.json')).write_text(json.dumps(spec,separators=(',',':'))+'\n')
    print('Imported '+region+' layout; approved Sewers inputs unchanged.');raise SystemExit(0)
source = subprocess.check_output([git,'show',BASE+':core/src/main/assets/environment/tiles_sewers.png'],cwd=ROOT)
image = Image.open(io.BytesIO(source)).convert('RGBA')
pixels = np.array(image)
SPEC_DIR.mkdir(parents=True,exist_ok=True)
runs = []
for y,row in enumerate(pixels):
    x = 0
    while x < len(row):
        if row[x,3] < 128: x += 1; continue
        # Preserve semantic regions while recoloring to the specified material palette.
        role = int(np.argmin(np.sum((COLORS.astype(np.int32)-row[x,:3].astype(np.int32))**2,axis=1)))
        end = x+1
        while end<len(row) and np.array_equal(row[end],row[x]): end+=1
        runs.append([y,x,end-x,role]); x=end
for name in ['tiles_sewers','walls_sewers']:
    spec = dict(kind='tile',dimensions=[image.width*4,image.height*4],source_dimensions=list(image.size),
        frame=[64,64],palette_roles=list(range(len(COLORS))),silhouette=runs,animation_keyframes=[],seed=417,
        provenance='GPL-3.0-or-later upstream '+BASE,output='core/src/main/assets/environment/'+name+'.png')
    (SPEC_DIR/(name+'.json')).write_text(json.dumps(spec,separators=(',',':'))+'\n')

shapes = [dict(type='rect',points=[0,0,255,255],role=0),
          dict(type='ellipse',points=[30,20,226,230],role=7),
          dict(type='ellipse',points=[45,30,211,213],role=1),
          dict(type='ellipse',points=[65,45,191,161],role=9),
          dict(type='polygon',points=[[77,110],[179,110],[169,191],[145,205],[111,205],[87,191]],role=9),
          dict(type='ellipse',points=[83,91,117,126],role=0),
          dict(type='ellipse',points=[139,91,173,126],role=0),
          dict(type='ellipse',points=[94,105,111,117],role=13),
          dict(type='ellipse',points=[145,105,162,117],role=13),
          dict(type='polygon',points=[[127,120],[116,143],[138,143]],role=0),
          dict(type='rect',points=[101,165,107,192],role=1),
          dict(type='rect',points=[123,165,129,195],role=1),
          dict(type='rect',points=[145,165,151,192],role=1)]
targets = []
for size in [16,32,48,64,128,256]: targets.append(('desktop/src/main/assets/icons/icon_'+str(size)+'.png',size,'icon'))
targets += [('desktop/src/main/assets/icons/windows.ico',256,'icon'),('desktop/src/main/assets/icons/mac.icns',1024,'icon')]
for density,size in [('ldpi',36),('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    targets.append(('android/src/main/res/mipmap-'+density+'/ic_launcher.png',size,'icon'))
    adaptive = size*108//48
    for name in ['foreground','background','monochrome']:
        targets.append(('android/src/main/res/mipmap-'+density+'/ic_launcher_'+name+'.png',adaptive,name))
for output,size,kind in targets:
    current_shapes = shapes if kind in ['icon','foreground'] else ([shapes[0]] if kind=='background' else shapes[3:])
    spec = dict(kind='icon',dimensions=[size,size],palette_roles=list(range(len(COLORS))),silhouette=current_shapes,
                animation_keyframes=[],seed=417,output=output,provenance='Original Grimhollow primitives, GPL-3.0-or-later')
    (SPEC_DIR/(output.replace('/','_').replace('.','_')+'.json')).write_text(json.dumps(spec,indent=2)+'\n')
print('Imported fixed upstream Sewer layout and original launcher icon specifications.')

# Character and item inputs retain GPL upstream silhouettes as editable horizontal vectors.
for name in ['warrior','mage','rogue','huntress','duelist','cleric','items']:
    source = subprocess.check_output([git,'show',BASE+':core/src/main/assets/sprites/'+name+'.png'],cwd=ROOT)
    image=Image.open(io.BytesIO(source)).convert('RGBA'); pixels=np.array(image); runs=[]
    for y,row in enumerate(pixels):
        x=0
        while x<len(row):
            if row[x,3]<128: x+=1; continue
            role=int(np.argmin(np.sum((COLORS.astype(np.int32)-row[x,:3].astype(np.int32))**2,axis=1)))
            end=x+1
            while end<len(row) and np.array_equal(row[end],row[x]): end+=1
            runs.append([y,x,end-x,role]); x=end
    factor=2 if name=='items' else 4
    output=name if name=='items' else 'hero_'+name
    spec=dict(kind='items' if name=='items' else 'hero',dimensions=[image.width*factor,image.height*factor],
        source_dimensions=list(image.size),frame=[32,32] if name=='items' else [48,60],runs=runs,silhouette=[],
        palette_roles=list(range(23)),animation_keyframes=[],seed=417,provenance='GPL-3.0-or-later upstream '+BASE,
        output='core/src/main/assets/sprites/'+output+'.png')
    (SPEC_DIR/(output+'.json')).write_text(json.dumps(spec,separators=(',',':'))+'\n')
