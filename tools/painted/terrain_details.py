"""Coherent door families and three vegetation states in existing atlas slots."""
from pathlib import Path
from functools import lru_cache
import numpy as np
from PIL import Image

HERE=Path(__file__).resolve().parent
REGIONS=['sewers','prison','caves','city','halls']


@lru_cache(None)
def sheet(name, rows):
    image=Image.open(HERE/'sources'/name).convert('RGBA')
    if image.getchannel('A').getextrema()[0]!=0:
        raise ValueError(name+': transparent source required')
    return [image.crop((round(x*image.width/4),round(y*image.height/rows),
                        round((x+1)*image.width/4),round((y+1)*image.height/rows)))
            for y in range(rows) for x in range(4)]


def sized(image, size):
    box=image.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
    if box is None:raise ValueError('Empty terrain detail')
    pixels=np.array(image.crop(box).resize(size,Image.Resampling.LANCZOS))
    pixels[pixels[:,:,3]<8]=0
    return Image.fromarray(pixels)


def split(source, floor, size, foot=124):
    whole=Image.new('RGBA',(64,128))
    whole.paste(floor,(0,64))
    sprite=sized(source,size)
    whole.alpha_composite(sprite,((64-size[0])//2,foot-size[1]))
    return whole.crop((0,64,64,128)),whole.crop((0,0,64,64))


def patch(region, atlas, old, floor, cap, features, raised):
    from pack import put,cell,surface_mask
    stage=REGIONS.index(region)
    clear=Image.new('RGBA',(64,64))
    plants=sheet('vegetation-states.png',4)[stage*3:stage*3+3]
    # GRASS is already the game's trampled state. Keep paving exposed beneath
    # low clippings; HIGH_GRASS and FURROWED_GRASS retain their gameplay flags.
    for alt in range(2):
        source=plants[2] if alt==0 else plants[2].transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        low,_=split(source,clear,(60,20))
        put(atlas,2+6*alt,floor)
        put(features,132+16*stage+alt,low)
        for state in range(2):
            source=plants[state] if alt==0 else plants[state].transpose(Image.Transpose.FLIP_LEFT_RIGHT)
            size=(56,92) if state==0 else (60,78)
            low,high=split(source,clear,size)
            put(atlas,122+state+3*alt,floor)
            put(atlas,234+state+3*alt,high)
            put(features,128+16*stage+2*state+alt,low)
            front=Image.new('RGBA',(64,64))
            front.paste(low.crop((0,32,64,64)),(0,32))
            put(raised,stage*4+state+2*alt,front)
            flat,_=split(source,floor,(52,52))
            put(atlas,66+state+3*alt,flat)

    doors=sheet('door-states.png',2)
    for state in range(4):
        low,high=split(doors[state],floor,(56,88),120)
        put(atlas,112+state,low)
        if state!=2:put(atlas,224+(state if state<2 else 2),high)
        flat,_=split(doors[state],floor,(36,56),124)
        put(atlas,56+state,flat)
    # Side-facing doors have their leaf in the wall overlay. This cell is only
    # the walkable threshold, including while open; never a second closed leaf.
    put(atlas,116,floor)
    for state,index in [(0,227),(2,228),(3,229)]:
        box=cell(old,index).getchannel('A').getbbox()
        tile=clear.copy()
        if box:
            x,y,right,bottom=box
            tile.alpha_composite(sized(doors[4+state],(right-x,bottom-y)),(x,y))
        put(atlas,index,tile)
    # Side doorway wall caps keep all four upstream neighbour masks. Repaint
    # their masonry and the changed central leaf separately, without altering
    # the cutaway alpha contract or filling an open doorway with opaque art.
    for index in range(208,224):
        template=cell(old,index)
        tile=surface_mask(template,cap)
        if index>=212:
            opened=np.asarray(cell(old,208+(index-208)%4))
            pixels=np.asarray(template)
            mask=np.any(pixels!=opened,axis=2)&(pixels[:,:,3]>0)
            mask[:,:20]=False;mask[:,44:]=False
            state=0 if index<216 else 2 if index<220 else 3
            paint=clear.copy();paint.alpha_composite(sized(doors[4+state],(24,64)),(20,0))
            mask &= np.asarray(paint)[:,:,3]>0
            tile.paste(paint,(0,0),Image.fromarray((mask*255).astype('uint8')))
            tile.putalpha(template.getchannel('A'))
        put(atlas,index,tile)
