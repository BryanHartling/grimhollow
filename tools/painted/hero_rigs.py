"""Class-specific painted cutout rigs, baked into HeroSprite's existing frames.

Animation indices, runtime timing, world geometry and collision are unchanged.
Keep an unfinished class on its previous rig until its own visual review.
"""
import math
from functools import lru_cache
from PIL import Image
from actors import SCALE, FRAME_WIDTH, FRAME_HEIGHT, parts, place, limb


@lru_cache(None)
def phylactery():
    from inventory import panels
    image=panels('class-kit')[1]
    box=image.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
    return image.crop(box)


def finish(canvas,index):
    if 8<=index<=12:
        whole=canvas.crop(canvas.getbbox()).rotate(
            [-12,-35,-62,-82,-90][index-8],Image.Resampling.BICUBIC,expand=True)
        scale=min(44*SCALE/whole.width,52*SCALE/whole.height,1)
        whole=whole.resize((round(whole.width*scale),round(whole.height*scale)),Image.Resampling.LANCZOS)
        canvas=Image.new('RGBA',canvas.size)
        canvas.alpha_composite(whole,((canvas.width-whole.width)//2,56*SCALE-whole.height))
    image=canvas.resize((FRAME_WIDTH,FRAME_HEIGHT),Image.Resampling.LANCZOS)
    # Invisible Lanczos ringing must not enlarge GameGeometry's occupancy box.
    image.putalpha(image.getchannel('A').point(lambda a:0 if a<8 else a))
    return image


def necromancer(tier,index):
    p=parts('necromancer-v2');gear=parts('armor',2)
    canvas=Image.new('RGBA',(48*SCALE,60*SCALE))
    stride=bob=sway=0
    if 2<=index<=7:
        phase=(index-2)*math.tau/6
        stride=math.sin(phase)*2.5
        bob=-abs(math.sin(phase))*.35
        sway=math.sin(phase-math.pi/3)*.55
    # Very quiet breath; a shuffle and cloth follow-through during movement.
    if index==1:bob=-.15
    bend=2.5 if index in (16,17) else 0
    cx,cy=23+bend*.45,25+bob+bend
    shoulders=((cx-7,cy-7),(cx+7,cy-6))
    elbows=[(cx-9-stride*.2,cy+3),(cx+8+stride*.2,cy+2)]
    wrists=[(cx-7-stride*.3,cy+10),(cx+10+stride*.3,cy+8)]
    hips=((21,36+bob),(26,36+bob))
    knees=[(20+stride,45+bob),(27-stride,45+bob)]
    ankles=[(20+stride,53-abs(stride)*.2),(27-stride,53-abs(stride)*.2)]
    # Raise the bound Phylactery, extend the free hand, then let both settle.
    # This is a visual gesture for the existing attack/zap duration only.
    if index==13:
        elbows=[(14,22),(33,22)];wrists=[(12,17),(31,16)]
    elif index==14:
        elbows=[(15,25),(35,22)];wrists=[(16,22),(41,21)]
    elif index==15:
        elbows=[(14,28),(33,26)];wrists=[(16,31),(37,31)]
    elif index in (16,17):
        elbows=[(19,34),(32,34)];wrists=[(23,39+(index-16)*2),(33,40+(index-16)*2)]
    elif index==18:
        knees=[(17,43),(30,43)];ankles=[(20,48),(28,48)]
        elbows=[(12,25),(36,25)];wrists=[(10,20),(40,20)]
    elif index in (19,20):
        elbows=[(18,31),(33,31)];wrists=[(24,29-(index-19)),(32,29-(index-19))]
    # The mantle defines the stooped shoulder silhouette. The robe conceals
    # knees but leaves boots visible, avoiding the old short-skirt/trouser body.
    place(canvas,p[3],(cx-2+sway,31.5+bob),(28,43),-2+sway)
    for side in (0,1):
        limb(canvas,p[8+side*2],hips[side],knees[side],5)
        limb(canvas,p[9+side*2],knees[side],ankles[side],4.5)
        place(canvas,p[12+side],(ankles[side][0]+1,ankles[side][1]+1.5),(6.5,5))
    limb(canvas,p[4],shoulders[0],elbows[0],6)
    limb(canvas,p[5],elbows[0],wrists[0],4.5)
    place(canvas,p[2],(24+sway*.5,41+bob),(23,27),sway)
    place(canvas,p[1],(cx,cy),(18,21),-4)
    if tier:
        # Armor remains identifiable, but no longer replaces the entire class
        # costume. Restore its painted bone collar over the fitted chest plate.
        place(canvas,gear[tier-1],(cx+.5,cy+1),(14,17),-4)
        collar=p[1].crop((0,0,p[1].width,round(p[1].height*.34)))
        place(canvas,collar,(cx-.4,cy-6.9),(18,7.1),-4)
    limb(canvas,p[6],shoulders[1],elbows[1],6)
    limb(canvas,p[7],elbows[1],wrists[1],4.5)
    # Hood is broad, the face inside it remains small: a full-height human
    # body instead of the previous oversized head and glove proportions.
    place(canvas,p[0],(cx+1.4,12+bob+bend),(13,14),-4-bend)
    for side in (0,1):
        place(canvas,p[14+side],(wrists[side][0],wrists[side][1]+1),(3.2,4))
    if index not in (19,20):
        place(canvas,phylactery(),(wrists[0][0],wrists[0][1]+5),(4.8,8))
    else:
        place(canvas,gear[7],(28,30-(index-19)),(14,9))
    return finish(canvas,index)
