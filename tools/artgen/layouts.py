"""GPL-3.0-or-later. Retained quest-layout contours with native rendered materials."""
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
from functools import lru_cache
from build import COLORS
from rendered import CACHE

@lru_cache(maxsize=23)
def surface(role):
    source=np.array(Image.open(CACHE/f'surfaces/{role:02d}.png').convert('RGB').crop((64,64,128,128)),float)
    luma=source@np.array([.2126,.7152,.0722]);lo,hi=np.percentile(luma,[5,95])
    value=np.array([.055,.16,.28,.41])[np.minimum(3,(np.clip((luma-lo)/max(1,hi-lo),0,.999)*4).astype(int))]
    color=COLORS[role].astype(float);cl=color@np.array([.2126,.7152,.0722])/255
    # Shades or white tints, never independent channel clipping that changes hue.
    rgb=np.where((value<=cl)[:,:,None],color*value[:,:,None]/max(.001,cl),color+(255-color)*((value-cl)/max(.001,1-cl))[:,:,None])
    return np.rint(rgb).clip(0,255).astype(np.uint8)

def paint(spec):
    sw,sh=spec['source_dimensions'];w,h=spec['dimensions'];factor=w//sw
    roles=Image.new('I',(sw,sh),-1);alpha=Image.new('L',(sw,sh));dr=ImageDraw.Draw(roles);da=ImageDraw.Draw(alpha)
    for run in spec.get('runs',[]):
        y,x,length,role=run[:4];a=run[4] if len(run)>4 else 255
        dr.line((x,y,x+length-1,y),fill=max(0,role));da.line((x,y,x+length-1,y),fill=a)
    r=np.array(roles.resize((w,h),Image.Resampling.NEAREST));a=np.array(alpha.resize((w,h),Image.Resampling.NEAREST))
    yy,xx=np.indices((h,w));out=np.zeros((h,w,4),np.uint8);out[:,:,3]=a
    for role in np.unique(r[r>=0]):
        mask=r==role;out[:,:,:3][mask]=surface(int(role))[yy[mask]%64,xx[mask]%64]
    return Image.fromarray(out)
