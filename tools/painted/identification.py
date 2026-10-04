"""Dedicated identity emblems, compiled from committed painted source sheets."""
from pathlib import Path
import hashlib, json
from PIL import Image, ImageOps
from functools import lru_cache
HERE=Path(__file__).resolve().parent

def cutout(sheet, column, row, columns=4, rows=4, bounds=(29,29)):
    part=sheet.crop((round(column*sheet.width/columns),round(row*sheet.height/rows),
                     round((column+1)*sheet.width/columns),round((row+1)*sheet.height/rows)))
    alpha=part.getchannel('A').point(lambda a:0 if a<8 else a)
    box=alpha.point(lambda a:255 if a>=16 else 0).getbbox()
    assert box is not None,'Empty identity painting'
    part.putalpha(alpha)
    return ImageOps.contain(part.crop(box),bounds,Image.Resampling.LANCZOS)

@lru_cache(None)
def build():
    from inventory import base_file, SEMANTICS
    contract=json.loads(base_file(SEMANTICS))['icons']
    # Retire the recovery-era alias now that Mind Vision has its own painting.
    contract['POTION_MINDVIS']['artIndex']=contract['POTION_MINDVIS']['id']
    contract['POTION_MINDVIS']['reason']='Dedicated third-eye profile, distinct from the Magical Sight prism.'
    prompts=json.loads((HERE/'sprint-icons-prompts.json').read_text())
    names=[name for spec in prompts['sheets'].values() for name in spec['names']]
    assert set(names)==set(contract) and len(names)==len(set(names)), 'Every identity needs one distinct painting'
    assert len({v['artIndex'] for v in contract.values()})==len(names),'Identity atlas indices must be unique'
    atlas=Image.new('RGBA',(512,256));hashes=set()
    for filename,spec in prompts['sheets'].items():
        sheet=Image.open(HERE/'sources/sprint-icons'/f'{filename}.png').convert('RGBA')
        assert sheet.getchannel('A').getextrema()[0]==0,'Identity sheet needs true transparency'
        for i,name in enumerate(spec['names']):
            im=cutout(sheet,i%4,i//4)
            # A restrained shared value range reads at HUD size without color noise.
            from PIL import ImageEnhance
            alpha=im.getchannel('A')
            values=ImageEnhance.Contrast(ImageOps.grayscale(im)).enhance(1.35)
            im=ImageOps.colorize(values,'#241b11','#fff3ca').convert('RGBA');im.putalpha(alpha)
            cell=Image.new('RGBA',(32,32));cell.alpha_composite(im,((32-im.width)//2,(32-im.height)//2))
            digest=hashlib.sha256(cell.tobytes()).hexdigest()
            assert digest not in hashes,(name,'Duplicate identity painting')
            hashes.add(digest)
            index=contract[name]['artIndex'];atlas.paste(cell,(index%16*32,index//16*32))
            contract[name].pop('sourceStatus',None)
            contract[name].update(rgbaSha256=digest,sourcePainting=f'{filename}:{i}',concept=spec['concepts'][i])
    return atlas,contract

def special(index,bounds=(58,58)):
    sheet=Image.open(HERE/'sources/sprint-icons/special.png').convert('RGBA')
    return cutout(sheet,index,0,2,1,bounds)

def outputs():
    snake=Image.new('RGBA',(64,64));im=special(0)
    snake.alpha_composite(im,((64-im.width)//2,(64-im.height)//2))
    return {'sprites/item_icons.png':build()[0],'interfaces/painted_snake.png':snake}

def review():
    from PIL import ImageDraw,ImageFont
    atlas,contract=build()
    board=Image.new('RGB',(1000,620),(25,29,31));draw=ImageDraw.Draw(board)
    font=ImageFont.load_default(size=11)
    draw.text((12,6),'Distinct identities | each cell: enlarged painting, actual 8-unit HUD emblem at 2x camera',font=font,fill='#eedcc0')
    for i,(name,spec) in enumerate(contract.items()):
        x,y=i%10*100,i//10*98+28;index=spec['artIndex']
        tile=atlas.crop((index%16*32,index//16*32,index%16*32+32,index//16*32+32))
        big=tile.resize((48,48),Image.Resampling.LANCZOS)
        board.paste(big,(x+15,y),big)
        small=tile.resize((16,16),Image.Resampling.LANCZOS)
        board.paste(small,(x+73,y+16),small)
        draw.text((x+3,y+55),name.replace('SCROLL_','S ').replace('POTION_','P ').replace('RING_','R '),font=font,fill='#eedcc0')
    board.save(HERE.parents[1]/'verification/interface/identity-emblems.png')

if __name__=='__main__':review()
