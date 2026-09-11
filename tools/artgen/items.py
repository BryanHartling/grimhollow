"""GPL-3.0-or-later. Three-material item packing from reproducible Blender frames."""
import json
from functools import lru_cache
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
HERE=Path(__file__).resolve().parent

def catalog():return json.loads((HERE/'blender/items.json').read_text())

def tones(entry):
    colors=np.array([tuple(bytes.fromhex(entry[k])) for k in ('primary','secondary','accent')],float)
    return np.unique(np.rint(np.concatenate([colors*s for s in (.18,.4,.7,1)]+[colors+(255-colors)*.16])).astype(np.uint8),axis=0)

@lru_cache(maxsize=600)
def frame(index):
    entry=next(e for e in catalog()['items'] if e['index']==index)
    image=Image.open(HERE/f'render_cache/items/{index:03d}.png').convert('RGBA');box=image.getbbox()
    if not box:raise ValueError('Empty rendered item '+entry['name'])
    image=image.crop(box);ratio=min(25/image.width,25/image.height)
    image=image.resize((max(1,round(image.width*ratio)),max(1,round(image.height*ratio))),Image.Resampling.LANCZOS)
    pixels=np.array(image);active=pixels[:,:,3]>=128;rgb=pixels[:,:,:3].astype(float);palette=tones(entry)
    nearest=((rgb[:,:,None,:]-palette[None,None,:,:])**2*np.array([.2126,.7152,.0722])).sum(axis=3).argmin(axis=2)
    pixels[:,:,:3]=palette[nearest];pixels[:,:,3]=active*255
    image=Image.fromarray(pixels);canvas=Image.new('RGBA',(32,32));canvas.alpha_composite(image,((32-image.width)//2,(32-image.height)//2))
    outline=Image.new('RGBA',(32,32),'#0E0D0C');outline.putalpha(canvas.getchannel('A').filter(ImageFilter.MaxFilter(5)));outline.alpha_composite(canvas)
    return outline

def atlas(spec):
    result=Image.new('RGBA',spec['dimensions'])
    for entry in catalog()['items']:
        index=entry['index'];result.alpha_composite(frame(index),(index%16*32,index//16*32))
    return result

def gallery():
    entries=catalog()['items'];columns=10;w=128;h=84
    out=Image.new('RGB',(columns*w,((len(entries)+columns-1)//columns)*h),'#1A1816');draw=ImageDraw.Draw(out)
    for i,e in enumerate(entries):
        x=i%columns*w;y=i//columns*h;im=frame(e['index']);out.paste(im.resize((48,48),Image.Resampling.NEAREST),(x+10,y+3),im.resize((48,48),Image.Resampling.NEAREST));small=im.resize((16,16),Image.Resampling.LANCZOS);out.paste(small,(x+83,y+20),small)
        name=e['name'];draw.text((x+3,y+54),name[:19],fill='#C9BFA8');draw.text((x+3,y+66),name[19:],fill='#C9BFA8')
    return out
