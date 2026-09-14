"""Named inventory replacements packed from reviewed painted source cells.

IDs, art-index remaps and the remaining icons come from the pinned fogfix-era
semantic contract. Only explicitly named replacement pixels can change.
"""
from pathlib import Path
from functools import lru_cache
from io import BytesIO
import hashlib
import json
import subprocess
from PIL import Image, ImageEnhance
import numpy as np
from actors import parts, HEROES

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
BASE='v1.1.0-painted-world'
SEMANTICS='desktop/src/test/resources/item-semantics.json'
# Chest concepts from the item-only sheet are replaced below with the matching
# mimic source. Other multi-state families retain their complete existing art.
RETAINED={'CHEST','LOCKED_CHEST','CRYSTAL_CHEST','EBONY_CHEST',
          'ARTIFACT_HORN1','ARTIFACT_CHALICE1','ARTIFACT_ROSE1','DART'}


def gutters(alpha, axis):
    projection=(alpha>=16).sum(axis=axis)
    length=len(projection);edges=[0]
    for i in (1,2,3):
        center=round(length*i/4);radius=round(length*.035)
        candidates=range(center-radius,center+radius+1)
        edges.append(min(candidates,key=lambda x:(int(projection[max(0,x-1):x+2].sum()),abs(x-center))))
    return edges+[length]


@lru_cache(None)
def base_file(path):
    return subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT)


@lru_cache(None)
def panels(name):
    im=Image.open(HERE/'sources/items'/f'{name}.png').convert('RGBA')
    if im.getchannel('A').getextrema()[0]!=0:
        raise ValueError(f'{name}: missing genuine alpha')
    alpha=np.asarray(im.getchannel('A'))
    xs,ys=gutters(alpha,0),gutters(alpha,1)
    return [im.crop((xs[x],ys[y],xs[x+1],ys[y+1]))
            for y in range(4) for x in range(4)]


def icon(image):
    box=image.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
    if not box:raise ValueError('Empty named inventory source')
    image=image.crop(box)
    scale=min(28/image.width,28/image.height)
    image=image.resize((max(1,round(image.width*scale)),max(1,round(image.height*scale))),Image.Resampling.LANCZOS)
    # Lanczos can leave detached, nearly transparent ringing pixels. They are
    # invisible after GPU minification but would enlarge the runtime occupancy
    # bounds and shrink the actual icon. Preserve useful antialiasing while
    # removing that sub-3% coverage from the compiled atlas.
    pixels=np.array(image)
    pixels[pixels[:,:,3]<8]=0
    image=Image.fromarray(pixels)
    out=Image.new('RGBA',(32,32))
    out.alpha_composite(image,((32-image.width)//2,(32-image.height)//2))
    return out


def build():
    config=json.loads((HERE/'items.json').read_text(encoding='utf-8'))
    semantics=json.loads(base_file(SEMANTICS))
    atlas=Image.open(BytesIO(base_file('core/src/main/assets/sprites/items.png'))).convert('RGBA')
    replacements={}
    for sheet,names in config['sheets'].items():
        if len(names)!=16:raise ValueError(f'{sheet}: expected sixteen named cells')
        for cell,name in enumerate(names):
            if name in RETAINED:continue
            if name in replacements:raise ValueError('Duplicate named replacement '+name)
            replacements[name]=icon(panels(sheet)[cell])
    gear=parts('armor',2)
    for i,name in enumerate(('CLOTH','LEATHER','MAIL','SCALE','PLATE')):
        replacements['ARMOR_'+name]=icon(gear[i])
    for hero in HEROES:
        replacements['ARMOR_'+hero.upper()]=icon(parts(hero)[1])
    replacements['ARMOR_LEATHER_OCHRE']=icon(ImageEnhance.Color(gear[1]).enhance(1.3))
    replacements['ARMOR_LEATHER_ASH']=icon(ImageEnhance.Color(gear[1]).enhance(.08))
    for name,source in {'ARTIFACT_TOOLKIT':'KIT','ARTIFACT_BEACON':'BEACON'}.items():
        replacements[name]=replacements[source]
    # A disguised mimic and its ordinary chest share the exact same authored
    # closed pose, avoiding a visual tell introduced by independent art passes.
    from monsters import parts as monster_parts
    for row,name in enumerate(('CHEST','LOCKED_CHEST','CRYSTAL_CHEST','EBONY_CHEST')):
        replacements[name]=icon(monster_parts('mimics')[row*4])
    written={}
    for name,image in replacements.items():
        if name not in semantics['items']:raise ValueError('Unknown inventory ID '+name)
        index=semantics['items'][name]['artIndex']
        digest=hashlib.sha256(image.tobytes()).hexdigest()
        if index in written and written[index]!=digest:raise ValueError('Conflicting art index '+str(index))
        atlas.paste(image,(index%16*32,index//16*32));written[index]=digest
    # Aliases such as DARTS and DART keep the same ID and therefore pixels.
    for entry in semantics['items'].values():
        if entry['artIndex'] in written:entry['rgbaSha256']=written[entry['artIndex']]
    semantics['source']='v1.1.0-painted-world identities; declared painted source cells in tools/painted/items.json; unchanged names, IDs, icon remaps and non-replaced pixels'
    return atlas,semantics,len(written)


def outputs():
    atlas,_,_=build()
    return {'sprites/items.png':atlas}


def semantic_bytes():
    _,semantics,_=build()
    return (json.dumps(semantics,indent=2)+'\n').encode('utf-8')
