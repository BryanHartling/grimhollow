"""Pack committed bounty paintings; no generation service needed by CI."""
from pathlib import Path
from PIL import Image, ImageOps
from inventory import icon
HERE = Path(__file__).resolve().parent


def items(cell_size=64):
    result = {}
    for name, file in [('BLOODMARKED_BRAND', 'brand'), ('WARDENS_COAT', 'coat'), ('WARRANT', 'warrant')]:
        art = Image.open(HERE / 'sources/bounty' / (file + '.png')).convert('RGBA')
        assert art.getchannel('A').getextrema()[0] == 0, (name, 'genuine transparency required')
        result[name] = icon(art, cell_size)
    return result

def source(name):
    im=Image.open(HERE/'sources/bounty'/f'{name}.png').convert('RGBA')
    assert im.getchannel('A').getextrema()[0]==0,name
    return im

def outputs():
    from readability import cutouts,centered
    result={}
    for name in ('cole','board'):
        result[f'sprites/bounty_{name}.png']=icon(source(name),256)
    sheet=Image.new('RGBA',(1024,512))
    paper=source('board').crop((428,225,828,810)).resize((240,240),Image.Resampling.LANCZOS)
    from monsters import CONTRACT,parts
    for i,name in enumerate(('skeleton','thief','guard','dm100','necromancer','tengu','chainwarden')):
        spec=next(s for s in CONTRACT['monsters'] if s['name']==name)
        portrait=icon(parts(spec['sheet'])[spec['row']*4],174)
        tile=Image.new('RGBA',(256,256));tile.alpha_composite(paper,(8,8));tile.alpha_composite(portrait,(41,26))
        sheet.alpha_composite(tile,(i%4*256,i//4*256))
    result['interfaces/bounty_posters.png']=sheet
    seals=Image.new('RGBA',(192,64));frames=cutouts('details.png',4,2)
    wax=source('board').crop((540,710,670,849))
    for i in range(3):
        tile=Image.new('RGBA',(64,64));centered(tile,frames[i],(62,62));centered(tile,wax,(38,38))
        seals.alpha_composite(tile,(i*64,0))
    result['interfaces/bounty_seals.png']=seals
    return result
