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
# Two texture pixels per rig coordinate preserve painted faces and cloth at
# zoomed-in sizes. World height is still set by the existing runtime footprint.
OUTPUT_SCALE = 2
FRAME_WIDTH, FRAME_HEIGHT = 48*OUTPUT_SCALE, 60*OUTPUT_SCALE
ATLAS_SIZE = (1024*OUTPUT_SCALE,512*OUTPUT_SCALE)
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
    if name=='necromancer-v2':
        # Reviewed transparent gutters of the committed 1254px source, whose
        # long first-row robe made the rows taller than a uniform quarter.
        xs=[0,330/1254,627/1254,940/1254,1]
        ys=[0,359/1254,660/1254,957/1254,1]
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
    from hero_rigs import PROFILES, humanoid
    if hero=='necromancer':
        from hero_rigs import necromancer
        return necromancer(tier,index)
    if hero in PROFILES:return humanoid(hero,tier,index)
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
    return canvas.resize((FRAME_WIDTH,FRAME_HEIGHT),Image.Resampling.LANCZOS)


def atlas(hero):
    image=Image.new('RGBA',ATLAS_SIZE)
    for tier in range(8):
        for pose in range(21):
            image.alpha_composite(frame(hero,tier,pose),(pose*FRAME_WIDTH,tier*FRAME_HEIGHT))
    return image


def outputs():
    return {f'sprites/hero_{hero}.png':atlas(hero) for hero in HEROES
            if (HERE/'sources/actors'/f'{hero}.png').exists()}


def review(hero):
    """Reproducible static/animated art review from the shipping pose frames."""
    import subprocess
    from io import BytesIO
    from PIL import ImageDraw, ImageFont
    root=HERE.parents[1]
    target=root/'verification/heroes'/hero
    target.mkdir(parents=True,exist_ok=True)
    previous=Image.open(BytesIO(subprocess.check_output([
        'git','show','2c0cb62dca6a50c6a78490e235eeb5ee60aac2ff:core/src/main/assets/sprites/hero_'+hero+'.png'],cwd=root))).convert('RGBA')
    font=ImageFont.load_default(size=18)
    small=ImageFont.load_default(size=13)
    poses=[0,2,4,6,13,14,15,16,19,12]
    sheet=Image.new('RGB',(1200,740),(30,34,34));draw=ImageDraw.Draw(sheet)
    draw.text((24,15),hero.title()+' | painted body, class posture and action poses',font=font,fill='#e2d4b9')
    for row,tier in enumerate((0,1,5,6)):
        for col,pose in enumerate(poses):
            im=frame(hero,tier,pose)
            sheet.paste(im,(col*120+12,52+row*168),im)
            draw.text((col*120+8,176+row*168),f'tier {tier} / pose {pose}',font=small,fill='#b1b9bb')
    sheet.save(target/'poses.png')
    comparison=Image.new('RGB',(660,380),(30,34,34));draw=ImageDraw.Draw(comparison)
    draw.text((26,14),'Previous',font=font,fill='#b1b9bb')
    draw.text((350,14),'Revised',font=font,fill='#e2d4b9')
    for x,im in ((54,previous.crop((0,60,48,120))),(384,frame(hero,1,0))):
        im=im.resize((216,270),Image.Resampling.LANCZOS)
        comparison.paste(im,(x,56),im)
    draw.text((26,348),'Same world height and gameplay. New pose + 2x texture detail.',font=small,fill='#b1b9bb')
    comparison.save(target/'comparison.png')
    images=[];durations=[]
    # GIF quantizes to 10ms: distribute rounding rather than speed actions up.
    sequence=[(0,1400)]+[(i,50) for _ in range(4) for i in range(2,8)]+[(0,600),(13,70),(14,70),(15,60),(0,900),(16,130),(17,120),(16,130),(17,120),(0,900),(19,50),(20,400),(19,50),(0,1200)]
    for pose,duration in sequence:
        panel=Image.new('RGB',(400,320),(30,34,34));d=ImageDraw.Draw(panel)
        d.text((18,12),hero.title()+' | existing runtime timings',font=small,fill='#e2d4b9')
        im=frame(hero,1,pose).resize((192,240),Image.Resampling.LANCZOS)
        panel.paste(im,(104,55),im);images.append(panel);durations.append(duration)
    # One shared palette avoids shimmering colors between otherwise still pixels.
    palette=sheet.quantize(255)
    images=[im.quantize(palette=palette,dither=Image.Dither.NONE) for im in images]
    images[0].save(target/'animation.gif',save_all=True,append_images=images[1:],duration=durations,loop=0,optimize=False,disposal=2)


if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser();parser.add_argument('--review',choices=HEROES,required=True)
    review(parser.parse_args().review)
