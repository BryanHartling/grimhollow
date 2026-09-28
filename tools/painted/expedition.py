"""Offline packing of the original expedition paintings; no generation at build time."""
from collections import deque
from functools import lru_cache
import numpy as np
from PIL import Image, ImageFilter
from pack import HERE, fit, put, cell, surface_mask, historical, LAYOUT


def source(name):
    return Image.open(HERE/'sources/expedition'/f'{name}.png').convert('RGBA')


@lru_cache(None)
def poses(name):
    """Extract disconnected silhouettes, not nominal quadrants that can clip wings."""
    im=source(name)
    alpha=np.array(im.getchannel('A').resize((im.width//2,im.height//2)))
    active=alpha>=24; labels=np.full(active.shape,-1,dtype=np.int16)
    groups=[]
    for sy,sx in zip(*np.nonzero(active)):
        if labels[sy,sx]!=-1:continue
        index=len(groups);queue=deque([(int(sy),int(sx))]);labels[sy,sx]=index;points=[]
        while queue:
            y,x=queue.popleft();points.append((y,x))
            for dy,dx in ((-1,0),(1,0),(0,-1),(0,1),(-1,-1),(-1,1),(1,-1),(1,1)):
                yy,xx=y+dy,x+dx
                if 0<=yy<active.shape[0] and 0<=xx<active.shape[1] and active[yy,xx] and labels[yy,xx]==-1:
                    labels[yy,xx]=index;queue.append((yy,xx))
        groups.append(points)
    biggest=sorted(range(len(groups)),key=lambda i:len(groups[i]),reverse=True)[:4]
    biggest.sort(key=lambda i:(int(np.mean(groups[i],axis=0)[0]>=alpha.shape[0]/2),np.mean(groups[i],axis=0)[1]))
    assert len(biggest)==4 and min(len(groups[i]) for i in biggest)>1000,name
    result=[]
    for index in biggest:
        mask=Image.fromarray(np.uint8(labels==index)*255).resize(im.size,Image.Resampling.NEAREST).filter(ImageFilter.MaxFilter(5))
        pixels=np.array(im);pixels[np.asarray(mask)==0]=0;pixels[pixels[:,:,3]<8]=0
        part=Image.fromarray(pixels);result.append(part.crop(part.getbbox()))
    return result


def creature(name):
    art=poses(name)
    # One scale and ground anchor for all poses; neither attack nor death changes body size.
    scale=min(480/max(p.width for p in art),480/max(p.height for p in art))
    atlas=Image.new('RGBA',(1024,1024))
    for i,part in enumerate(art):
        part=part.resize((round(part.width*scale),round(part.height*scale)),Image.Resampling.LANCZOS)
        atlas.alpha_composite(part,(i%2*512+(512-part.width)//2,i//2*512+496-part.height))
    return atlas


def outputs(base):
    result={f'sprites/expedition_{name}.png':creature(name) for name in ('dragon','spider')}
    hunter=Image.new('RGBA',(256,256));art=fit(source('hunter'),230,240)
    hunter.alpha_composite(art,((256-art.width)//2,248-art.height))
    result['sprites/expedition_hunter.png']=hunter
    atlas=base['environment/tiles_caves.png'].copy()
    wood=source('timber').resize((128,128),Image.Resampling.LANCZOS)
    # Shared edge-spanning planks; no tile-sized raised pedestals or unsafe holes.
    for index in (0,4,6,10,12):put(atlas,index,wood.crop((0,0,64,64)))
    old=historical(LAYOUT,'environment/tiles_caves.png').resize(atlas.size,Image.Resampling.NEAREST)
    for index in (25,26):put(atlas,index,surface_mask(cell(old,index),wood.crop((0,0,64,64))))
    result['environment/tiles_expedition.png']=atlas
    floor=base['environment/tiles_caves.png'].copy()
    put(floor,20,cell(floor,0))
    result['environment/tiles_expedition_hoard.png']=floor
    hoard=Image.new('RGBA',(512,256));art=fit(source('hoard'),444,246)
    hoard.alpha_composite(art,((448-art.width)//2,252-art.height))
    result['environment/custom_tiles/expedition_hoard.png']=hoard
    return result


SIZES={'sprites/expedition_dragon.png':(1024,1024),'sprites/expedition_spider.png':(1024,1024),
       'sprites/expedition_hunter.png':(256,256),'environment/tiles_expedition.png':(1024,1024),'environment/tiles_expedition_hoard.png':(1024,1024),
       'environment/custom_tiles/expedition_hoard.png':(512,256)}
