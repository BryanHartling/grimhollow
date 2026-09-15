#!/usr/bin/env python3
"""Offline atlas packaging from committed painted images; no image synthesis.

Tile indices and cutaway/shore masks come from the upstream layout contract.
Untouched special assets and approved doors/torches come from the fogfix tag.
"""
from pathlib import Path
from io import BytesIO
from functools import lru_cache
import argparse
import hashlib
import json
import subprocess
import numpy as np
from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / 'core/src/main/assets'
PREFIX = 'core/src/main/assets/'
BASE = 'v1.0.2-fogfix'
LAYOUT = 'v4.0.0'
TILE = 64
MANIFEST = ASSETS / 'painted-assets.json'
APPROVED = [56,57,58,59,60,61,
            112,113,114,115,116,224,225,226,227,228,229]


@lru_cache(None)
def historical(ref, path):
    data = subprocess.check_output(['git','show',ref+':'+PREFIX+path],cwd=ROOT)
    return Image.open(BytesIO(data)).convert('RGBA')


def rect(index, size=64):
    x,y = index%16*size,index//16*size
    return x,y,x+size,y+size


def cell(atlas, index, size=64):
    return atlas.crop(rect(index,size))


def put(atlas, index, tile):
    assert tile.size == (64,64)
    columns=atlas.width//64
    atlas.paste(tile,(index%columns*64,index//columns*64))


@lru_cache(None)
def panels(name):
    im=Image.open(HERE/'sources'/name).convert('RGBA')
    # Built-in outputs may have odd dimensions. Rational grid edges avoid drift.
    return [im.crop((round(x*im.width/4),round(y*im.height/2),
                     round((x+1)*im.width/4),round((y+1)*im.height/2)))
            for y in range(2) for x in range(4)]


def fit(image,width,height):
    box=image.getbbox()
    if box is None:raise ValueError('Empty painted source')
    image=image.crop(box)
    scale=min(width/image.width,height/image.height)
    return image.resize((round(image.width*scale),round(image.height*scale)),Image.Resampling.LANCZOS)


def prop(floor, source, width, height, flat=False):
    """One sprite spanning the existing upper/lower tile pair, foot at y=124."""
    sprite=fit(source,width,min(height,56) if flat else height)
    whole=Image.new('RGBA',(64,128))
    whole.paste(floor,(0,64))
    whole.alpha_composite(sprite,((64-sprite.width)//2,124-sprite.height))
    return whole.crop((0,64,64,128)),whole.crop((0,0,64,64))


def surface_mask(source, paint):
    """Retain exact upstream cutaway alpha and black wall interiors."""
    rgb=np.asarray(source)[:,:,:3]
    mask=Image.fromarray(np.where(rgb.max(axis=2)>0,255,0).astype('uint8'))
    out=Image.new('RGBA',(64,64),(0,0,0,255))
    out.paste(paint,(0,0),mask)
    out.putalpha(source.getchannel('A'))
    return out


def region_atlas(region, features, raised):
    path='environment/tiles_'+region+'.png'
    atlas=historical(BASE,path).copy()
    old=historical(LAYOUT,path).resize(atlas.size,Image.Resampling.NEAREST)
    materials=panels(region+'-materials.png')
    floor,alt1,alt2,special,face,cap,grass,water=[p.resize((64,64),Image.Resampling.LANCZOS) for p in materials]
    props=panels('props.png')
    details=panels('details.png')
    regional=panels('region-props.png')
    for index,tile in [(0,floor),(6,alt1),(12,alt2),(4,special),(10,special),(2,grass),(8,grass.transpose(Image.Transpose.ROTATE_180))]:
        put(atlas,index,tile)
    for index in [1,7,5,11]:
        low,_=prop([floor,alt1][index%2],props[7],44,26,True)
        put(atlas,index,low)
    if region=='sewers':
        for index in [1,7]:put(atlas,index,grass)
    for index,source in [(16,0),(17,1),(18,3),(19,2),(20,4),(22,0),(3,5),(9,5)]:
        put(atlas,index,prop(special if index==22 else floor,details[source],60,56,True)[0])
    # Chasm art cannot resemble walkable paving. Preserve the original ledge alpha.
    for index in range(24,32):
        template=cell(old,index)
        put(atlas,index,surface_mask(template,face))
    # The shoreline is a transparent stencil over the existing scrolling water.
    for index in range(32,48):
        edge=floor.copy();edge.putalpha(cell(old,index).getchannel('A'))
        put(atlas,index,edge)
    # Wall fronts and the top strip use separate painted material planes.
    front=face.copy();front.paste(cap.crop((0,0,64,16)),(0,0))
    for index in [48,52]+list(range(80,112)):
        put(atlas,index,front)
    # Exact upstream cap/overhang coverage; never paint a floor-sized wall slab.
    for index in list(range(144,208)):
        put(atlas,index,surface_mask(cell(old,index),cap))
    # Flat, lower and upper indices are fixed by DungeonTileSheet.
    placements=[(64,120,232,3,52,60),(65,121,233,5,60,58),
                (66,122,234,1,60,60),(67,123,235,2,56,60),
                (69,125,237,1,56,56),(70,126,238,2,54,56),
                (72,128,240,4,48,90),(73,129,241,4,48,90)]
    stage=['sewers','prison','caves','city','halls'].index(region)
    feature_slots={64:135,65:134,66:128,67:130,69:129,70:131,72:136,73:136}
    for flat,lower,upper,source,w,h in placements:
        ground=special if flat in [73,75] else grass if source==1 else floor
        art=props[source]
        if source==4 and region in ['city','halls']:art=regional[4 if region=='city' else 5]
        if source in [1,2] and region=='halls':art=regional[7]
        low,high=prop(Image.new('RGBA',(64,64)),art,w,h)
        put(atlas,lower,ground);put(atlas,upper,high)
        put(features,feature_slots[flat]+16*stage,low)
        if source in [1,2]:
            # Foreground leaves retain the existing after-actor layer, keeping
            # heads clear while feet sit inside the same painted vegetation.
            front=Image.new('RGBA',(64,64));front.paste(low.crop((0,32,64,64)),(0,32))
            put(raised,stage*4+{66:0,67:1,69:2,70:3}[flat],front)
        put(atlas,flat,prop(ground,art,w,h,True)[0])
    for i in [132,133]:put(features,i+16*stage,grass)
    for i in [208,209]:put(features,i,prop(Image.new('RGBA',(64,64)),details[5],60,56,True)[0])
    for alternate in [False,True]:
        art=props[0] if region=='sewers' else regional[{'prison':1 if alternate else 0,'caves':2,'city':3,'halls':6}.get(region,0)]
        ground=special if alternate else floor
        if region=='prison' and alternate:ground=cell(atlas,24)
        height=90 if region in ['prison','city'] else 72
        low,high=prop(Image.new('RGBA',(64,64)),art,60,height)
        put(features,137+alternate+16*stage,low)
        for index,img in [(74+alternate,prop(ground,art,60,height,True)[0]),(130+alternate,ground),(242+alternate,high)]:put(atlas,index,img)
    # Bookshelf keeps the same collision/occlusion and uses the same cap masks.
    shelf=prop(floor,props[6],60,62,True)[0]
    for i in [50,54,92,93,94,95,108,109,110,111]:put(atlas,i,shelf)
    # Preserve approved doors; torch foregrounds must not carry their old masonry.
    if region=='sewers':
        approved=historical(BASE,path)
        for i in APPROVED:put(atlas,i,cell(approved,i))
    from terrain_details import patch
    patch(region,atlas,old,floor,cap,features,raised)
    approved=historical(BASE,'environment/tiles_sewers.png')
    for i in [49,53,84,85,86,87,100,101,102,103]:
        # Use the committed render's alpha only to separate the approved fixture
        # (including its two-pixel outline) from the obsolete wall behind it.
        source=Image.open(ROOT/f'tools/artgen/render_cache/tiles/wall_torch_{i%3}.png')
        mask=source.crop((64,64,128,128)).getchannel('A').point(lambda a:255 if a>100 else 0)
        mask=mask.filter(ImageFilter.MaxFilter(5))
        fixture=cell(approved,i);fixture.putalpha(mask)
        material=face.copy();material.paste(cap.crop((0,0,64,16)),(0,0))
        material.alpha_composite(fixture);put(atlas,i,material)
    outputs={path:atlas}
    # 512 source pixels cover eight world cells. Scrolling still uses SkinnedBlock.
    water_path='environment/water'+str(['sewers','prison','caves','city','halls'].index(region))+'.png'
    outputs[water_path]=materials[7].resize((512,512),Image.Resampling.LANCZOS)
    if region=='sewers':
        outputs['environment/walls_sewers.png']=atlas.copy()
    return outputs


def outputs():
    result={}
    features=historical(BASE,'environment/terrain_features.png').copy()
    raised=historical(BASE,'environment/raised_terrain.png').copy()
    for region in ['sewers','prison','caves','city','halls']:
        result.update(region_atlas(region,features,raised))
    result['environment/terrain_features.png']=features
    from presentation import traps, outputs as presentation
    traps(features)
    result.update(presentation())
    from botany_skills import plants, outputs as botany_skills
    plants(features)
    result.update(botany_skills())
    result['environment/raised_terrain.png']=raised
    from actors import outputs as actors
    result.update(actors())
    from inventory import outputs as inventory
    result.update(inventory())
    from title import outputs as title
    result.update(title())
    from monsters import outputs as monsters
    result.update(monsters())
    from status import outputs as status
    result.update(status())
    from interface_art import outputs as interface_art
    result.update(interface_art())
    return result


def digest(im):
    return hashlib.sha256(im.convert('RGBA').tobytes()).hexdigest()


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true')
    args=parser.parse_args();built=outputs();failures=[]
    sources={p.relative_to(HERE/'sources').as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted((HERE/'sources').rglob('*.png'))}
    manifest={'base':BASE,'layout':LAYOUT,'source_sha256':sources,'assets':{}}
    from monsters import sizes as monster_sizes
    fixed_monster_sizes=monster_sizes()
    from monsters import coverage
    painted_rects=coverage()
    for path,im in built.items():
        expected=(1024,2176) if path=='sprites/items.png' else (1024,512) if path.startswith('sprites/hero_') else (512,512) if '/water' in path else (256,512) if '/raised_terrain' in path else (1024,1024)
        expected={'interfaces/title_grimhollow.png':(1920,1080),'interfaces/title_wordmark.png':(1024,144),'interfaces/title_mist.png':(1024,342)}.get(path,expected)
        expected=fixed_monster_sizes.get(path,expected)
        expected={'interfaces/buffs.png':(448,224),'interfaces/large_buffs.png':(1024,512),'interfaces/chrome.png':(512,384),'interfaces/status_pane.png':(1024,512),'interfaces/painted_glyphs.png':(512,256)}.get(path,expected)
        if path.startswith('splashes/painted_'):expected=(1600,900)
        if path=='interfaces/painted_portraits.png':expected=(384,384)
        if path=='interfaces/painted_skills.png':expected=(1024,512)
        assert im.size==expected,path
        manifest['assets'][path]={'size':list(im.size),'rgba_sha256':digest(im)}
        if path in painted_rects:manifest['assets'][path]['painted_rects']=painted_rects[path]
        target=ASSETS/path
        if args.check:
            if not target.exists() or digest(Image.open(target))!=digest(im):failures.append(path)
        else:
            target.parent.mkdir(parents=True,exist_ok=True)
            im.save(target,optimize=False)
    from presentation import pack_launchers
    pack_launchers(args.check,failures)
    from inventory import semantic_bytes, SEMANTICS
    expected_semantics=semantic_bytes()
    if args.check:
        if (ROOT/SEMANTICS).read_bytes().replace(b'\r\n',b'\n')!=expected_semantics:failures.append(SEMANTICS)
        if not MANIFEST.exists() or json.loads(MANIFEST.read_text(encoding='utf-8'))!=manifest:failures.append('painted-assets.json')
    else:
        (ROOT/SEMANTICS).write_bytes(expected_semantics)
        MANIFEST.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    print(f'PAINTED assets={len(built)} source sheets={len(sources)} failures={len(failures)}')
    for failure in failures:print('FAIL:',failure)
    return bool(failures)


if __name__=='__main__':raise SystemExit(main())
