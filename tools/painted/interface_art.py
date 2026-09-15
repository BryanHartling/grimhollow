"""Compile authored UI materials into the existing logical nine-patch layout."""
from functools import lru_cache
import json
import numpy as np
from PIL import Image
from pack import HERE
from inventory import gutters

SCALE=4

@lru_cache(None)
def panels():
    image=Image.open(HERE/'sources/interface/frames.png').convert('RGBA')
    # Authored panel edges; the final 39 source rows are an unused presentation strip.
    xs=[1,419,836,1253];ys=[1,405,802,1215]
    return [image.crop((xs[x],ys[y],xs[x+1],ys[y+1])) for y in range(3) for x in range(3)]

def patch(source,size,margins):
    w,h=size;l,t,r,b=margins
    border=60
    sx=[0,border,source.width-border,source.width]
    sy=[0,border,source.height-border,source.height]
    dx=[0,l,w-r,w];dy=[0,t,h-b,h]
    output=Image.new('RGBA',size)
    for y in range(3):
        for x in range(3):
            width,height=dx[x+1]-dx[x],dy[y+1]-dy[y]
            if width>0 and height>0:
                part=source.crop((sx[x],sy[y],sx[x+1],sy[y+1]))
                output.paste(part.resize((width,height),Image.Resampling.LANCZOS),(dx[x],dy[y]))
    return output

@lru_cache(None)
def glyphs():
    config=json.loads((HERE/'interface-prompts.json').read_text())
    result={}
    for sheet,entry in config['icons'].items():
        source=Image.open(HERE/f'sources/interface/{sheet}.png').convert('RGBA')
        alpha=np.asarray(source.getchannel('A'));assert alpha.min()==0
        xs,ys=gutters(alpha,0),gutters(alpha,1)
        for i,name in enumerate(entry['names']):
            part=source.crop((xs[i%4],ys[i//4],xs[i%4+1],ys[i//4+1]))
            box=part.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox();assert box,name
            part=part.crop(box);scale=56/max(part.size)
            part=part.resize((max(1,round(part.width*scale)),max(1,round(part.height*scale))),Image.Resampling.LANCZOS)
            pixels=np.array(part);pixels[pixels[:,:,3]<8]=0;part=Image.fromarray(pixels)
            cell=Image.new('RGBA',(64,64));cell.alpha_composite(part,((64-part.width)//2,(64-part.height)//2))
            result[name]=cell
    return result

def outputs():
    frames=panels();chrome=Image.new('RGBA',(512,384))
    # x, y, logical width/height, borders, authored material, optional alpha.
    layouts=[
        (0,0,20,20,(6,6,6,6),0,255),(86,0,22,22,(7,7,7,7),1,255),
        (20,0,9,9,(4,4,4,4),1,255),(20,9,9,9,(4,4,4,4),3,225),
        (29,9,9,9,(4,4,4,4),0,250),(29,0,9,9,(4,4,4,4),6,255),
        (38,0,6,6,(2,2,2,2),2,255),(38,6,6,6,(2,2,2,2),3,255),
        (22,18,16,14,(3,3,3,3),2,255),(0,32,32,32,(13,13,13,13),1,255),
        (32,32,32,32,(5,11,5,11),6,255),(64,0,20,20,(6,6,6,6),0,255),
        (65,22,8,13,(3,7,3,5),0,255),(75,22,8,13,(3,7,3,5),3,255),
        (0,64,28,28,(2,2,2,2),4,255),(28,64,28,28,(2,2,2,2),5,255)
    ]
    for x,y,w,h,margins,material,alpha in layouts:
        art=patch(frames[material],(w*SCALE,h*SCALE),tuple(n*SCALE for n in margins))
        if alpha<255:art.putalpha(alpha)
        chrome.paste(art,(x*SCALE,y*SCALE))
    chrome.paste((255,255,255,255),(45*SCALE,0,46*SCALE,SCALE))
    status=Image.new('RGBA',(1024,512))
    for x,y,w,h in [(0,0,82,38),(0,64,160,39),(90,0,12,9)]:
        status.paste(patch(frames[0],(w*SCALE,h*SCALE),(8,8,8,8)),(x*SCALE,y*SCALE))
    for x,y,w,h,material in [(0,40,50,4,7),(0,44,50,4,8),(0,103,128,9,7),(0,112,128,9,8)]:
        source=frames[material];glass=source.crop((65,95,source.width-65,source.height-75))
        status.paste(glass.resize((w*SCALE,h*SCALE),Image.Resampling.LANCZOS),(x*SCALE,y*SCALE))
    gold=frames[0].crop((90,9,300,34))
    for x,y,w,h in [(0,48,17,4),(0,121,128,7)]:
        status.paste(gold.resize((w*SCALE,h*SCALE),Image.Resampling.LANCZOS),(x*SCALE,y*SCALE))
    atlas=Image.new('RGBA',(512,256))
    for i,art in enumerate(glyphs().values()):atlas.paste(art,(i%8*64,i//8*64))
    return {'interfaces/chrome.png':chrome,'interfaces/status_pane.png':status,'interfaces/painted_glyphs.png':atlas}
