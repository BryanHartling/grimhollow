"""Read-only 256px inspection exports from the SAME committed paintings.

No generation and no world-atlas edits. Lookup uses displayed atlas rectangles,
never item identity: an unidentified item cannot reveal its secret here.
"""
import json
from functools import lru_cache
from PIL import Image, ImageOps
from pack import HERE, ASSETS, panels, digest
from inventory import icon, build, gutters
import numpy as np

SIZE=256


@lru_cache(None)
def build_previews():
    images={};lookup={}

    def add(texture,rect,art,name):
        box=art.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
        assert box,(name,'empty inspection artwork')
        path='artwork/'+name+'.png'
        if path not in images:images[path]=icon(art,SIZE)
        lookup.setdefault(texture,[]).append({'rect':list(rect),'file':path})

    atlas,semantics,_=build(SIZE)
    for index in sorted({e['artIndex'] for e in semantics['items'].values()}):
        x,y=index%16,index//16
        path=f'artwork/item-{index}.png'
        images[path]=atlas.crop((x*SIZE,y*SIZE,(x+1)*SIZE,(y+1)*SIZE))
        assert images[path].getbbox(),('empty named item',index)
        lookup.setdefault('sprites/items.png',[]).append({'rect':[x*64,y*64,(x+1)*64,(y+1)*64],'file':path})
    del atlas
    from bounty import source,outputs as bounty_outputs
    for name in ('cole','board'):
        add(f'sprites/bounty_{name}.png',(0,0,256,256),source(name),'bounty-'+name)
    for i in range(7):
        x,y=i%4*256,i//4*256
        add('interfaces/bounty_posters.png',(x,y,x+256,y+256),bounty_outputs()['interfaces/bounty_posters.png'].crop((x,y,x+256,y+256)),f'bounty-poster-{i}')

    from monsters import CONTRACT,parts,rectangle,blank_atlas,pose_choice,density
    for spec in CONTRACT['monsters']:
        name=spec['name'];texture=f"sprites/{spec.get('atlas',name)}.png"
        den=density(spec.get('atlas',name));atlas=blank_atlas(spec.get('atlas',name))
        art=parts(spec['sheet'])[spec['row']*4:spec['row']*4+4]
        for mode in ('closed','idle','move','attack','defeated'):
            for step,index in enumerate(spec.get(mode,[])):
                choice=pose_choice(mode,step,name)
                rect=tuple(n*den//4 for n in spec['rects'][str(index)]) if 'rects' in spec else rectangle(atlas,spec['frame'],index,den)
                add(texture,rect,art[choice],f'creature-{name}-{choice}')

    from status import symbols
    for i,art in enumerate(symbols()[:89]):
        for texture,cell in [('interfaces/buffs.png',28),('interfaces/large_buffs.png',64)]:
            add(texture,(i%16*cell,i//16*cell,(i%16+1)*cell,(i//16+1)*cell),art,f'status-{i}')

    from botany_skills import DATA,cutouts
    for group,(name,skills) in enumerate(DATA['skills'].items()):
        for i,art in enumerate(cutouts(name,6,7,len(skills),(SIZE,SIZE))):
            special={'FIELD_REPAIR':'particles/defensive-sigil.png','SPELLGUARD':'sprint/spellguard.png','WRENCH':'mystery/rebuff.png'}
            if skills[i]['key']=='KINETIC_RESERVE':continue  # composed identity emblem; exact runtime fallback
            if skills[i]['key'] in special:art=Image.open(HERE/'sources'/special[skills[i]['key']]).convert('RGBA')
            index=group*42+i
            add('interfaces/painted_skills.png',(index%16*64,index//16*64,(index%16+1)*64,(index//16+1)*64),art,f'skill-{index}')
    for i,art in enumerate(cutouts('plants',4,4,len(DATA['plants']),(SIZE,SIZE))):
        add('environment/terrain_features.png',(i*64,448,(i+1)*64,512),art,f'plant-{i}')
    for i,name in enumerate(('dewcatcher','seedpod'),13):
        add('environment/terrain_features.png',(i*64,448,(i+1)*64,512),Image.open(HERE/f'sources/botany-skills/{name}.png').convert('RGBA'),f'plant-{i}')

    # Independent journal / interface pictures, including the small magnifier.
    for texture,source in [('interfaces/painted_journal.png','sprint/journal.png'),('interfaces/painted_landmarks.png','sprint/journal-landmarks.png')]:
        sheet=Image.open(HERE/'sources'/source).convert('RGBA')
        xs,ys=gutters(np.asarray(sheet.getchannel('A')),0),gutters(np.asarray(sheet.getchannel('A')),1)
        for i in range(16):
            x,y=i%4,i//4
            art=sheet.crop((xs[x],ys[y],xs[x+1],ys[y+1]))
            if texture.endswith('landmarks.png') and i==9:art=Image.open(HERE/'sources/playtest-v123/well.png').convert('RGBA')
            add(texture,(x*64,y*64,(x+1)*64,(y+1)*64),art,f'{texture.split("/")[-1][:-4]}-{i}')
    from actors import HEROES
    for i,hero in enumerate(HEROES):
        # The full class painting is already shipped at 1600x900.
        lookup.setdefault('interfaces/painted_portraits.png',[]).append({'rect':[i%3*128,i//3*128,(i%3+1)*128,(i//3+1)*128],'file':f'splashes/painted_{hero}.png'})

    from presentation import traps
    trap_atlas=Image.new('RGBA',(SIZE*16,SIZE*7));traps(trap_atlas,SIZE)
    for shape in range(7):
        for color in range(9):
            art=trap_atlas.crop((color*SIZE,shape*SIZE,(color+1)*SIZE,(shape+1)*SIZE))
            path=f'artwork/trap-{shape}-{color}.png';images[path]=art
            lookup.setdefault('environment/terrain_features.png',[]).append({'rect':[color*64,shape*64,(color+1)*64,(shape+1)*64],'file':path})
    del trap_atlas

    # Use source detail only when it matches that exact current material cell.
    # Props, stencils and approved composite doors retain their exact frame.
    for region in ('sewers','prison','caves','city','halls'):
        texture=f'environment/tiles_{region}.png';atlas=Image.open(ASSETS/texture).convert('RGBA')
        for material,art in enumerate(panels(region+'-materials.png')):
            low=art.resize((64,64),Image.Resampling.LANCZOS)
            for index in range(256):
                x,y=index%16*64,index//16*64
                if digest(atlas.crop((x,y,x+64,y+64)))==digest(low):
                    add(texture,(x,y,x+64,y+64),art,f'{region}-material-{material}')
    # Never write or regenerate gameplay images while building these previews.
    return images,lookup


def outputs():return build_previews()[0]


def index_bytes():return (json.dumps(build_previews()[1],sort_keys=True,indent=2)+'\n').encode('utf-8')
