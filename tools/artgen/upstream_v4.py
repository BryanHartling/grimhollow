"""GPL-3.0-or-later. Import the authorized v4 delta as editable palette vectors.

Only this import command needs Git. Normal builds regenerate from committed JSON.
Approved regional atlases and Grimhollow's appended item indices are preserved.
These interim upstream silhouettes remain subject to the later native-art stages.
"""
import io
import json
import re
import shutil
import subprocess
import numpy as np
from PIL import Image
from build import ROOT, SPEC_DIR, COLORS

BASE = '7b8b845a76fe76c6b7c031ae9e570852411f56db'
VERSION = '2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f'
JAVA = 'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java'
ASSETS = 'core/src/main/assets/'
LOCKED = {'environment/tiles_'+r+'.png' for r in ('sewers','prison','caves','city','halls')}

def git(*args):
    return subprocess.check_output([shutil.which('git'), *args], cwd=ROOT)

def vector_runs(image):
    pixels=np.asarray(image.convert('RGBA'))
    colors=np.unique(pixels[:,:,:3].reshape(-1,3),axis=0)
    roles={tuple(c):int(np.argmin(((COLORS.astype(int)-c.astype(int))**2).sum(axis=1))) for c in colors}
    roles[(0,0,0)]=-1
    runs=[]
    for y,row in enumerate(pixels):
        x=0
        while x<len(row):
            if row[x,3]==0:
                x+=1
                continue
            end=x+1
            while end<len(row) and np.array_equal(row[end],row[x]):end+=1
            runs.append([y,x,end-x,roles[tuple(row[x,:3])],int(row[x,3])])
            x=end
    return runs

def item_indices(text):
    """Evaluate only the atlas's integer declarations, never arbitrary source code."""
    values={}
    for declaration in re.findall(r'(?:public|private) static final int\s+([^;]+);',text.split('public static class Icons')[0]):
        for name,expression in re.findall(r'(\w+)\s*=\s*(.*?)(?=,\s*[A-Z_]+\s*=|$)',declaration):
            expression=re.sub(r'xy\(\s*(\d+)\s*,\s*(\d+)\s*\)',lambda m:str(int(m[1])-1+16*(int(m[2])-1)),expression)
            expression=re.sub(r'\b[A-Z_]+\b',lambda m:str(values.get(m[0],m[0])),expression)
            if re.fullmatch(r'[\d\s+*()-]+',expression):values[name]=eval(expression,{'__builtins__':{}},{})
    return values

def import_assets():
    changed=git('diff','--name-only','--diff-filter=AM',BASE,VERSION,'--',ASSETS).decode().splitlines()
    for path in changed:
        relative=path.removeprefix(ASSETS)
        if not path.endswith('.png') or relative in LOCKED or relative=='sprites/items.png':continue
        image=Image.open(io.BytesIO(git('show',VERSION+':'+path))).convert('RGBA')
        spec=dict(kind='vectors',dimensions=list(image.size),source_dimensions=list(image.size),
                  runs=vector_runs(image),silhouette=[],seed=417,output=path,
                  provenance='GPL-3.0-or-later upstream v4.0.0 '+VERSION,
                  upstream_asset=relative,animation_keyframes=[])
        name='upstream_v4_'+relative.replace('/','_').replace('.png','')
        (SPEC_DIR/(name+'.json')).write_text(json.dumps(spec,separators=(',',':'))+'\n',encoding='utf-8')
        print('Imported',relative)

    # Keep old public indices; v4 shifted several spells and added four variants.
    current=(ROOT/JAVA).read_text(encoding='utf-8')
    old=item_indices(git('show','v0.5.2-regions:'+JAVA).decode())
    incoming=item_indices(git('show',VERSION+':'+JAVA).decode())
    for name in ['CURSE_INFUSE','MAGIC_INFUSE','ALCHEMIZE','RECYCLE','RECLAIM_TRAP','RETURN_BEACON','SUMMON_ELE']:
        current=re.sub(r'(public static final int\s+'+name+r'\s*=)[^;]+;',lambda m:m[1]+' '+str(old[name])+';',current)
    for index,name in enumerate(['SUMMON_ELE_FIRE','SUMMON_ELE_FROST','SUMMON_ELE_SHOCK','SUMMON_ELE_CHAOS'],522):
        current=re.sub(r'(public static final int\s+'+name+r'\s*=)[^;]+;',lambda m:m[1]+' '+str(index)+';',current)
    (ROOT/JAVA).write_text(current,encoding='utf-8',newline='\n')
    destination=item_indices(current)
    spec_path=SPEC_DIR/'items.json'
    spec=json.loads(spec_path.read_text())
    image=Image.open(io.BytesIO(git('show',VERSION+':'+ASSETS+'sprites/items.png'))).convert('RGBA')
    source=image.copy()
    image=image.crop((0,0,*spec['source_dimensions']))
    # Clear both the moved spell row and new slots before remapping by semantic ID.
    from PIL import ImageDraw
    draw=ImageDraw.Draw(image)
    draw.rectangle((0,416,255,431),fill=(0,0,0,0))
    for name,index in incoming.items():
        if name not in destination or index<416 or index>=432:continue
        if name in ('SPELLS',):continue
        dst=destination[name]
        tile=source.crop((index%16*16,index//16*16,index%16*16+16,index//16*16+16))
        image.paste(tile,(dst%16*16,dst//16*16))
    # Existing Grimhollow classes occupy slots 512-521, authored as vector shapes.
    own_runs=[r for r in spec.get('runs',[]) if r[0]>=512 and r[1]//16<10]
    spec['runs']=vector_runs(image)+own_runs
    spec['provenance']='GPL-3.0-or-later v4.0.0 '+VERSION+'; stable Grimhollow spell IDs and original class-item vectors'
    spec_path.write_text(json.dumps(spec,separators=(',',':'))+'\n',encoding='utf-8')
    print('Imported v4 items; original spell and class-item indices preserved')
