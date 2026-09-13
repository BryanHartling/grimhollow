"""Offline cutout animation from original painted parts; no runtime rig or AI.

The 21 pose indices and eight armor rows are the existing HeroSprite contract.
Every frame uses the same authored parts, preserving identity across animation.
"""
from pathlib import Path
from functools import lru_cache
import math
from PIL import Image

HERE = Path(__file__).resolve().parent
SCALE = 4
HEROES = ('warrior', 'mage', 'rogue', 'huntress', 'duelist', 'cleric',
          'necromancer', 'enchanter', 'psychic')


@lru_cache(None)
def parts(name, rows=4):
    sheet = Image.open(HERE/'sources/actors'/f'{name}.png').convert('RGBA')
    if sheet.getchannel('A').getextrema()[0] != 0:
        raise ValueError(f'{name}: source has no transparent alpha')
    # Reviewed crop gutters for the Psychic's unevenly spaced source rows.
    xs = [0,.25,.5,935/1280,1] if name=='psychic' else [x/4 for x in range(5)]
    ys = [0,355/1280,627/1280,917/1280,1] if name=='psychic' else [y/rows for y in range(rows+1)]
    result = []
    for y in range(rows):
        for x in range(4):
            im = sheet.crop((round(xs[x]*sheet.width), round(ys[y]*sheet.height),
                             round(xs[x+1]*sheet.width), round(ys[y+1]*sheet.height)))
            box = im.getchannel('A').point(lambda a: 255 if a >= 16 else 0).getbbox()
            if not box:
                raise ValueError(f'Empty component {name} {x},{y}')
            result.append(im.crop(box))
    return result


def place(canvas, part, center, size, angle=0):
    im = part.resize(tuple(max(1,round(v*SCALE)) for v in size), Image.Resampling.LANCZOS)
    if angle:
        im = im.rotate(angle, Image.Resampling.BICUBIC, expand=True)
    canvas.alpha_composite(im, (round(center[0]*SCALE-im.width/2),
                               round(center[1]*SCALE-im.height/2)))


def limb(canvas, part, start, end, width):
    dx,dy = end[0]-start[0],end[1]-start[1]
    place(canvas,part,((start[0]+end[0])/2,(start[1]+end[1])/2),
          (width,math.hypot(dx,dy)+2),math.degrees(math.atan2(dx,dy)))


def frame(hero, tier, index):
    p = parts(hero); gear = parts('armor',2)
    canvas = Image.new('RGBA',(48*SCALE,60*SCALE))
    bob = -.35 if index==1 else 0
    stride = 0
    if 2 <= index <= 7:
        phase = (index-2)*math.tau/6
        stride = math.sin(phase)*5
        bob = -abs(math.cos(phase))*.9
    bend = 4 if index in (16,17) else 0
    cx,cy = 23+bend*.5,25+bob+bend
    shoulder1=(cx-6,cy-7);shoulder2=(cx+6,cy-6)
    elbow1=(cx-9-stride*.4,cy+2);wrist1=(cx-7-stride*.75,cy+9)
    elbow2=(cx+9+stride*.4,cy+3);wrist2=(cx+7+stride*.75,cy+10)
    hip1=(22,36+bob);hip2=(26,36+bob)
    knee1=(20+stride,44+bob);ankle1=(19+stride*1.1,52-abs(stride)*.25)
    knee2=(27-stride,44+bob);ankle2=(28-stride*1.1,52-abs(stride)*.25)
    if index==13:elbow2=(32,16);wrist2=(28,9)
    if index==14:elbow2=(36,21);wrist2=(42,19)
    if index==15:elbow2=(35,30);wrist2=(38,35)
    if index in (16,17):
        elbow1=(20,35);wrist1=(25,40+(index-16)*3)
        elbow2=(33,34);wrist2=(36,41+(index-16)*3)
    if index==18:
        knee1=(16,42);ankle1=(20,46);knee2=(32,42);ankle2=(28,46)
        elbow1=(12,24);wrist1=(9,20);elbow2=(36,24);wrist2=(40,20)
    if index in (19,20):
        elbow1=(18,30);wrist1=(25,28-(index-19)*2)
        elbow2=(34,30);wrist2=(31,28-(index-19)*2)
    # The back cape and far limbs precede the torso; hands stay visible in front.
    place(canvas,p[3],(cx-1,32+bob),(21,35),stride*.7)
    limb(canvas,p[8],hip1,knee1,6);limb(canvas,p[9],knee1,ankle1,5)
    place(canvas,p[12],(ankle1[0]+1,ankle1[1]+2),(8,6))
    limb(canvas,p[10],hip2,knee2,6);limb(canvas,p[11],knee2,ankle2,5)
    place(canvas,p[13],(ankle2[0]+1,ankle2[1]+2),(8,6))
    limb(canvas,p[4],shoulder1,elbow1,6);limb(canvas,p[5],elbow1,wrist1,5)
    place(canvas,p[14],(wrist1[0],wrist1[1]+1.5),(4,5))
    robed = hero in ('mage','cleric','necromancer','enchanter','psychic')
    place(canvas,p[2],(24,38+bob if robed else 35+bob),(15,16) if robed else (14,9))
    place(canvas,p[1],(cx,cy),(17,21))
    if tier:
        # Distinct cloth/leather/mail/scale/plate/class silhouettes are authored,
        # not palette substitutions of one cuirass. Cape/head preserve class ID.
        place(canvas,gear[tier-1],(cx,cy),(16,20))
    limb(canvas,p[6],shoulder2,elbow2,6);limb(canvas,p[7],elbow2,wrist2,5)
    place(canvas,p[15],(wrist2[0],wrist2[1]+1.5),(4,5))
    place(canvas,p[0],(cx+1,10+bob+bend),(13,15),-bend)
    if index in (19,20):
        place(canvas,gear[7],(28,29-(index-19)*2),(15,10))
    if 8 <= index <= 12:
        angle=[-12,-35,-62,-82,-90][index-8]
        whole=canvas.crop(canvas.getbbox()).rotate(angle,Image.Resampling.BICUBIC,expand=True)
        scale=min(44*SCALE/whole.width,52*SCALE/whole.height,1)
        whole=whole.resize((round(whole.width*scale),round(whole.height*scale)),Image.Resampling.LANCZOS)
        canvas=Image.new('RGBA',canvas.size)
        canvas.alpha_composite(whole,((canvas.width-whole.width)//2,56*SCALE-whole.height))
    return canvas.resize((48,60),Image.Resampling.LANCZOS)


def outputs():
    result={}
    for hero in HEROES:
        if not (HERE/'sources/actors'/f'{hero}.png').exists():
            continue
        atlas=Image.new('RGBA',(1024,512))
        for tier in range(8):
            for pose in range(21):
                atlas.alpha_composite(frame(hero,tier,pose),(pose*48,tier*60))
        result[f'sprites/hero_{hero}.png']=atlas
    return result
