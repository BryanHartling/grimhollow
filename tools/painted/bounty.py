"""Pack committed bounty paintings; no generation service needed by CI."""
from pathlib import Path
from functools import lru_cache
from PIL import Image, ImageOps
from inventory import icon
HERE = Path(__file__).resolve().parent


def items(cell_size=64):
    result = {}
    for name, file in [('BLOODMARKED_BRAND', 'brand'), ('WARDENS_COAT', 'coat'), ('WARRANT', 'warrant')]:
        art = source(file)
        assert art.getchannel('A').getextrema()[0] == 0, (name, 'genuine transparency required')
        result[name] = icon(art, cell_size)
    return result

def source(name):
    folder='playtest-v129' if name=='brand' else 'bounty'
    im=Image.open(HERE/'sources'/folder/f'{name}.png').convert('RGBA')
    assert im.getchannel('A').getextrema()[0]==0,name
    return im

@lru_cache(None)
def poster_assets():
    # Full-resolution poses, not the small game sprites or old 256px notices.
    from monsters import CONTRACT,parts
    from wanted import WANTED,source_poses
    sheet=Image.new('RGBA',(2048,1024))
    for i,name in enumerate(('skeleton','thief','guard','dm100','necromancer','tengu','chainwarden')):
        spec=next(s for s in CONTRACT['monsters'] if s['name']==name)
        art=source_poses(WANTED[i][0])[0] if i<5 else parts(spec['sheet'])[spec['row']*4]
        tile=icon(art,512)
        sheet.alpha_composite(tile,(i%4*512,i//4*512))
    sheet.alpha_composite(icon(source_poses('morcant')[0],512),(1536,512))
    source_dir=HERE/'sources/bounty-poster'
    paper=Image.open(source_dir/'paper.png').convert('RGBA')
    raw_seals=Image.open(source_dir/'seals.png').convert('RGBA')
    for name,im in [('paper',paper),('seals',raw_seals)]:
        assert im.getchannel('A').getextrema()[0]==0,(name,'genuine transparency required')
    seals=Image.new('RGBA',(768,256))
    for i in range(3):
        art=raw_seals.crop((round(i*raw_seals.width/3),0,round((i+1)*raw_seals.width/3),raw_seals.height))
        seals.alpha_composite(icon(art,256),(i*256,0))
    paper=paper.crop(paper.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox())
    return {'interfaces/bounty_posters.png':sheet,
            'interfaces/bounty_poster_seals.png':seals,
            'interfaces/bounty_parchment.png':paper.resize((512,768),Image.Resampling.LANCZOS)}

def outputs():
    from readability import cutouts,centered
    result={}
    from wanted import outputs as wanted_outputs
    result.update(wanted_outputs())
    for name in ('cole','board'):
        result[f'sprites/bounty_{name}.png']=icon(source(name),256)
    result.update(poster_assets())
    seals=Image.new('RGBA',(192,64));frames=cutouts('details.png',4,2)
    wax=source('board').crop((540,710,670,849))
    for i in range(3):
        tile=Image.new('RGBA',(64,64));centered(tile,frames[i],(62,62));centered(tile,wax,(38,38))
        seals.alpha_composite(tile,(i*64,0))
    result['interfaces/bounty_seals.png']=seals
    return result
