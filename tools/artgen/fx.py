"""GPL-3.0-or-later. One native atlas for all enhanced effects."""
from PIL import Image,ImageFilter,ImageDraw
import numpy as np
from rendered import CACHE,quantize
STYLES={'smoke':16,'flame':6,'ember':3,'grass':6,'necrotic':8,'curse':8,'inscription':8,
 'psychic':8,'bone_wall':4,'force_wall':8,'corpse':8,'sanctuary':8,'scorch':3,'ripple':8,'green_flame':6}

def frame(kind,index):
    path=CACHE/('liquids/ripple/'+str(index)+'.png' if kind=='ripple' else f'effects/{kind}/{index:02d}.png')
    source=Image.open(path).convert('RGBA')
    if source.size==(192,192):source=source.crop((64,64,128,128))
    if source.size!=(64,64):source=source.resize((64,64),Image.Resampling.LANCZOS)
    image=quantize(source)
    if kind=='smoke':
        image=Image.new('RGBA',(64,64),'#C9BFA8');image.putalpha(source.getchannel('A').filter(ImageFilter.GaussianBlur(2.2)))
    elif kind=='scorch':
        image.putalpha(source.getchannel('A').filter(ImageFilter.GaussianBlur(.6)))
        box=image.getbbox()
        if box:
            patch=image.crop(box);patch.thumbnail((32,32),Image.Resampling.LANCZOS);patch=quantize(patch)
            canvas=Image.new('RGBA',(64,64));canvas.alpha_composite(patch,((64-patch.width)//2,(64-patch.height)//2));image=canvas
    elif kind in ('grass','bone_wall'):
        out=Image.new('RGBA',(64,64),'#0E0D0C');out.putalpha(image.getchannel('A').filter(ImageFilter.MaxFilter(5)));out.alpha_composite(image);image=out
    return image

def paint(spec):
    out=Image.new('RGBA',spec['dimensions'])
    for row,(kind,count) in enumerate(STYLES.items()):
        for i in range(count):out.alpha_composite(frame(kind,i),(i*64,row*64))
    return out

def gallery():
    out=Image.new('RGB',(1024,15*92),'#1A1816');d=ImageDraw.Draw(out)
    for row,(kind,count) in enumerate(STYLES.items()):
        d.text((4,row*92+3),kind,fill='#C9BFA8')
        for i in range(count):im=frame(kind,i);out.paste(im,(i*64,row*92+24),im)
    return out
