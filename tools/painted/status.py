"""Painted status symbols at four texture pixels per logical HUD pixel."""
import re
import numpy as np
from PIL import Image
from pack import HERE, ROOT
from inventory import gutters


def symbols():
    result=[]
    for sheet in range(6):
        source=Image.open(HERE/f'sources/status/status-{sheet}.png').convert('RGBA')
        alpha=np.asarray(source.getchannel('A'))
        assert alpha.min()==0, ('Missing transparent status background',sheet)
        xs,ys=gutters(alpha,0),gutters(alpha,1)
        for row in range(4):
            for col in range(4):
                part=source.crop((xs[col],ys[row],xs[col+1],ys[row+1]))
                box=part.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
                assert box, ('Empty status',sheet,row,col)
                result.append(part.crop(box))
    return result


def outputs():
    result={}
    art=symbols()
    for name,cell in [('buffs',28),('large_buffs',64)]:
        atlas=Image.new('RGBA',(cell*16,cell*8))
        # 0..85 are existing buff IDs; 86..88 are the overhead alert/question/sleep.
        for index,source in enumerate(art[:89]):
            scale=min((cell-4)/source.width,(cell-4)/source.height)
            im=source.resize((max(1,round(source.width*scale)),max(1,round(source.height*scale))),Image.Resampling.LANCZOS)
            pixels=np.array(im);pixels[pixels[:,:,3]<8]=0;im=Image.fromarray(pixels)
            atlas.alpha_composite(im,(index%16*cell+(cell-im.width)//2,index//16*cell+(cell-im.height)//2))
        assert atlas.crop((15*cell,7*cell,16*cell,8*cell)).getbbox() is None, 'NONE stays transparent'
        result[f'interfaces/{name}.png']=atlas
    # The named constants are the contract; new statuses cannot silently keep old pixels.
    text=(ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/BuffIndicator.java').read_text()
    for name,index in re.findall(r'public static final int (\w+)\s*=\s*(\d+);',text.split('public static final int SIZE_SMALL')[0]):
        assert int(index)<86 or name=='NONE',('Unmapped status icon',name,index)
    return result
