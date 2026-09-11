"""GPL-3.0-or-later. Fixed UI rectangles packed from the Blender title cache."""
from PIL import Image,ImageFilter
from rendered import CACHE,quantize

def word(name,width,height):
    im=Image.open(CACHE/'title'/f'{name}.png').convert('RGBA');box=im.getbbox()
    if box is None:raise ValueError('Empty title render '+name)
    im=im.crop(box);im.thumbnail((width-4,height-4),Image.Resampling.LANCZOS);im=quantize(im)
    result=Image.new('RGBA',(width,height));result.alpha_composite(im,((width-im.width)//2,(height-im.height)//2))
    edge=Image.new('RGBA',result.size,'#0E0D0C');edge.putalpha(result.getchannel('A').filter(ImageFilter.MaxFilter(5)));edge.alpha_composite(result);return edge

def paint(spec):
    if spec['rendered_title']=='background':return quantize(Image.open(CACHE/'title/background.png'))
    result=Image.new('RGBA',(1024,512))
    for box in [(0,0,139,100),(0,100,240,157)]:
        x,y,r,b=[v*2 for v in box];im=word('wordmark',r-x,b-y);result.alpha_composite(im,(x,y))
        # The glow frame contains only the decorated initial's muted red edges.
        import numpy as np
        a=np.array(im);red=(a[:,:,0]>a[:,:,1]*1.8)&(a[:,:,0]>a[:,:,2]*1.5);a[:,:,3]=a[:,:,3]*red
        result.alpha_composite(Image.fromarray(a),(278 if y==0 else 480,y))
    for name,box in [('boss_slain',(0,157,127,225)),('game_over',(128,157,256,192))]:
        x,y,r,b=[v*2 for v in box];result.alpha_composite(word(name,r-x,b-y),(x,y))
    return result
