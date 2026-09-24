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
    from hero_rigs import necromancer, humanoid
    return necromancer(tier,index) if hero=='necromancer' else humanoid(hero,tier,index)


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


def lineup():
    """Compare every class at equal visible height, as the renderer fits them."""
    from PIL import ImageDraw, ImageFont
    target=HERE.parents[1]/'verification/heroes'
    target.mkdir(parents=True,exist_ok=True)
    sheet=Image.new('RGB',(1440,610),(30,34,34));d=ImageDraw.Draw(sheet)
    font=ImageFont.load_default(size=18);small=ImageFont.load_default(size=13)
    for col,hero in enumerate(HEROES):
        d.text((100+col*148,16),hero.title(),font=font,fill='#e2d4b9')
        for row,tier in enumerate((0,1,5)):
            im=frame(hero,tier,0);box=im.getchannel('A').point(lambda a:255 if a>=8 else 0).getbbox();im=im.crop(box)
            scale=150/im.height;im=im.resize((round(im.width*scale),150),Image.Resampling.LANCZOS)
            sheet.paste(im,(100+col*148+(110-im.width)//2,50+row*185),im)
    for row,label in enumerate(('Base','Cloth','Plate')):d.text((18,112+row*185),label,font=font,fill='#b1b9bb')
    d.text((100,590),'Painted class silhouettes | same world height, armor progression and gameplay',font=small,fill='#b1b9bb')
    sheet.save(target/'lineup.png')
    if all((target/hero/'sewers-lighting-on.png').exists() for hero in HEROES):
        # A labelled crop of each actual renderer capture, never a composed
        # character pasted into a room. Full screenshots sit beside this image.
        native=Image.new('RGB',(1152,966),(30,34,34));nd=ImageDraw.Draw(native)
        for index,hero in enumerate(HEROES):
            x=index%3*384;y=index//3*322
            im=Image.open(target/hero/'sewers-lighting-on.png').convert('RGB')
            im=im.crop((864,464,1056,608)).resize((384,288),Image.Resampling.NEAREST)
            native.paste(im,(x,y+32));nd.text((x+12,y+6),hero.title()+' | native capture, 2x crop',font=small,fill='#e2d4b9')
        native.save(target/'ingame-lineup.png')


if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser();parser.add_argument('--review',choices=HEROES+('all',),required=True)
    selected=parser.parse_args().review
    if selected=='all':
        for hero in HEROES:review(hero)
        lineup()
    else:review(selected)
