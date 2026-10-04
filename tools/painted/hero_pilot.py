"""Animate complete, individually painted figures in the existing hero contract.

Authored armor variants are uniformly fitted, never stretched into a torso box.
A small offline deformation mesh preserves the continuous painting at joints.
The game still consumes its original 21 poses and eight armor rows; no runtime
mesh, additional textures, movement rules or animation timing changes are needed.
"""
from functools import lru_cache
import math
from PIL import Image, ImageDraw
from actors import HERE, SCALE, FRAME_WIDTH, FRAME_HEIGHT

PILOT=('warrior','enchanter')
from actors import HEROES as ALL_HEROES
HEROES=tuple(hero for hero in ALL_HEROES if (HERE/'sources/hero-pilot'/f'{hero}.png').exists())
PROFILES={
    'warrior':(1.9,.7,.25,(-12,10,3),1.2),
    'enchanter':(1.35,.5,.45,(10,-8,-2),1.2),
    'mage':(1.1,.35,.65,(-6,8,2),.65),
    'rogue':(1.7,.8,.2,(-12,9,2),1.4),
    'huntress':(2.1,.65,.4,(8,-7,2),1.4),
    'duelist':(1.8,.6,.2,(-14,10,4),1.2),
    'cleric':(1.,.35,.7,(-10,10,3),.6),
    'necromancer':(.95,.3,.75,(7,-7,-1),.7),
    'psychic':(.8,.3,.6,(-4,7,1),.5),
}
SIZE = (48*SCALE, 60*SCALE)
GRID = 4096


@lru_cache(None)
def standing(hero, tier):
    source = Image.open(HERE/'sources/hero-pilot'/f'{hero}.png').convert('RGBA')
    x, y = tier % 4, tier // 4
    part = source.crop((x*source.width//4, y*source.height//2,
                        (x+1)*source.width//4, (y+1)*source.height//2))
    alpha = part.getchannel('A').point(lambda a: 0 if a < 8 else a)
    box = alpha.point(lambda a: 255 if a >= 16 else 0).getbbox()
    if not box:
        raise ValueError(f'Missing {hero} armor {tier}')
    part.putalpha(alpha)
    part = part.crop(box)
    ratio = min(52*SCALE/part.height, 36*SCALE/part.width)
    part = part.resize((round(part.width*ratio), round(part.height*ratio)), Image.Resampling.LANCZOS)
    result = Image.new('RGBA', SIZE)
    # Identical ground line for every armor variant and pose.
    result.alpha_composite(part, ((SIZE[0]-part.width)//2, 56*SCALE-part.height))
    return result


def smooth(a, b, value):
    t = min(1., max(0., (value-a)/(b-a)))
    return t*t*(3-2*t)


def displacement(hero, index, x, y):
    """Skin weights in composition coordinates, shared by fitted armor variants."""
    enchanter = hero == 'enchanter'
    left=hero in ('enchanter','huntress','necromancer')
    step,arm_swing,cloth,angles,thrust=PROFILES[hero]
    dx = dy = 0.
    phase = (index-2)*math.tau/6 if 2 <= index <= 7 else 0.
    stride = math.sin(phase) if 2 <= index <= 7 else 0.
    side = -1 if x < 24 else 1
    lower = smooth(32, 53, y)
    if 2 <= index <= 7:
        # Opposed legs, small planted-foot lift; the head does not bob or swell.
        dx += side*stride*step*lower
        dy -= max(0., side*stride)*.85*lower
        # A low-amplitude counter-swing and delayed coat motion.
        arm = smooth(5.5, 10., abs(x-24))*smooth(14, 29, y)*(1-smooth(33, 37, y))
        dx -= side*stride*arm_swing*arm
        dy += side*stride*.4*arm
        coat = smooth(28, 36, y)*(1-smooth(43, 51, y))
        dx += math.sin(phase-.6)*cloth*coat
    # Pose 1 deliberately equals pose 0: no continuous twitching at rest.
    if index in (13, 14, 15):
        # Wind-up, extension, recovery. Rotation preserves the forearm's shape;
        # the shoulder weight eases to zero inside the ribcage.
        angle = angles[index-13]
        arm_side = 24-x if left else x-24
        end = 44 if left else 33
        amount = smooth(5.5, 10., arm_side)*smooth(13, 23, y)*(1-smooth(end, end+4, y))
        px, py = x-(16 if left else 32), y-16
        theta = math.radians(angle)
        dx += (px*math.cos(theta)-py*math.sin(theta)-px)*amount
        dy += (px*math.sin(theta)+py*math.cos(theta)-py)*amount
        torso = (1-smooth(28, 39, y))
        dx += (-.45, thrust, .25)[index-13]*torso
        dy += (.15, .35, .1)[index-13]*torso
    if index in (16, 17):
        bend = (1-smooth(27, 45, y))
        dx += 1.1*bend
        dy += (1.8 if index == 16 else 2.1)*bend
    if index == 18:
        dy -= 1.2*lower
        dx += side*.65*lower
    if index in (19, 20):
        arm = smooth(5., 10., abs(x-24))*smooth(15, 29, y)*(1-smooth(33, 37, y))
        dx -= side*1.8*arm
        dy -= 2.2*arm
    return (x+dx)*SCALE, (y+dy)*SCALE


@lru_cache(None)
def mesh(hero, index):
    # Dense near elbows, waist, knees and ankles; faces retain a rigid surface.
    xs = (0, 6, 10, 14, 17, 20, 24, 28, 31, 34, 38, 42, 48)
    ys = (0, 4, 9, 13, 16, 20, 24, 28, 32, 35, 39, 43, 47, 51, 56, 60)
    result = []
    for y0, y1 in zip(ys, ys[1:]):
        for x0, x1 in zip(xs, xs[1:]):
            corners = ((x0,y0), (x1,y0), (x1,y1), (x0,y1))
            for ids in ((0,1,2), (0,2,3)):
                src = [(corners[i][0]*SCALE, corners[i][1]*SCALE) for i in ids]
                # Commit to a 1/4096-pixel grid before deriving the inverse map.
                # BLAS-backed floating solves produced different edge pixels on
                # two Linux CI machines. Integer determinants make the exported
                # atlas independent of CPU-specific linear-algebra kernels.
                dest = [tuple(round(v*GRID) for v in displacement(hero,index,*corners[i])) for i in ids]
                (ax,ay),(bx,by),(cx,cy) = dest
                determinant = ax*(by-cy)+bx*(cy-ay)+cx*(ay-by)
                # An inverted triangle folds the painted body over itself.
                if determinant <= 0:
                    raise ValueError(f'Folded {hero} pose {index} at {x0},{y0}')
                left = max(0, min(p[0] for p in dest)//GRID)
                top = max(0, min(p[1] for p in dest)//GRID)
                right = min(SIZE[0], -(-max(p[0] for p in dest)//GRID)+1)
                bottom = min(SIZE[1], -(-max(p[1] for p in dest)//GRID)+1)
                if right <= left or bottom <= top:
                    continue
                transform = []
                for axis in (0,1):
                    a,b,c = (p[axis] for p in src)
                    m = (a*(by-cy)+b*(cy-ay)+c*(ay-by))*GRID
                    n = (a*(cx-bx)+b*(ax-cx)+c*(bx-ax))*GRID
                    k = a*(bx*cy-cx*by)+b*(cx*ay-ax*cy)+c*(ax*by-bx*ay)
                    transform.extend((m/determinant, n/determinant,
                                      (k+m*left+n*top)/determinant))
                points = [(round(p[0]/GRID-left), round(p[1]/GRID-top)) for p in dest]
                mask = Image.new('L',(right-left,bottom-top))
                ImageDraw.Draw(mask).polygon(points, fill=255)
                result.append(((left,top), mask, tuple(transform)))
    return result


def frame(hero, tier, index):
    from hero_rigs import finish
    base = standing(hero, tier)
    if index in (0, 1) or 8 <= index <= 12:
        return finish(base.copy(), index)
    canvas = Image.new('RGBA', SIZE)
    for origin, mask, coefficients in mesh(hero,index):
        tile = base.transform(mask.size, Image.Transform.AFFINE, coefficients,
                              Image.Resampling.BICUBIC)
        canvas.paste(tile, origin, mask)
    if index in (19, 20):
        from actors import parts, place
        place(canvas,parts('armor-front',2)[7],(23.5,31),(17,7))
    return finish(canvas,index)


def review(hero):
    """Before/after, all armor rows, action frames and loops from shipping pixels."""
    import subprocess
    from io import BytesIO
    from PIL import ImageFont
    from actors import atlas
    target = HERE.parents[1]/'verification/heroes'/('pilot' if hero in PILOT else hero)
    target.mkdir(parents=True,exist_ok=True)
    previous = Image.open(BytesIO(subprocess.check_output(['git','show',
        '5d1ecc2a7:core/src/main/assets/sprites/hero_'+hero+'.png'],cwd=HERE.parents[1]))).convert('RGBA')
    current = atlas(hero)
    font = ImageFont.load_default(size=19)
    small = ImageFont.load_default(size=14)
    sheet = Image.new('RGB',(1120,710),(25,29,31)); draw = ImageDraw.Draw(sheet)
    draw.text((22,15),hero.title()+' | previous / pilot | equal visible height',font=font,fill='#eedcc0')
    for col,tier in enumerate((0,1,2,3,4,5,6,7)):
        x=col*140
        draw.text((x+10,52),('Base','Cloth','Leather','Mail','Scale','Plate','Class','Ancient')[col],font=small,fill='#b7bab9')
        for row,source in enumerate((previous,current)):
            figure=source.crop((0,tier*FRAME_HEIGHT,FRAME_WIDTH,(tier+1)*FRAME_HEIGHT))
            box=figure.getchannel('A').point(lambda a:255 if a>=8 else 0).getbbox()
            figure=figure.crop(box); factor=174/figure.height
            figure=figure.resize((round(figure.width*factor),174),Image.Resampling.LANCZOS)
            sheet.paste(figure,(x+(140-figure.width)//2,80+row*195),figure)
        # Native-sized presentation, without enlarging anatomy to hide defects.
        figure=frame(hero,tier,0)
        sheet.paste(figure,(x+22,490),figure)
        draw.text((x+12,621),'96 x 120 frame',font=small,fill='#b7bab9')
    draw.text((22,672),'Original portrait identity | quiet idle | unchanged gameplay and world height',font=small,fill='#eedcc0')
    sheet.save(target/f'{hero}-comparison.png')
    poses=(0,2,3,4,5,6,7,13,14,15,16,17,18,19,20,12)
    board=Image.new('RGB',(960,600),(25,29,31)); d=ImageDraw.Draw(board)
    for i,pose in enumerate(poses):
        x=(i%8)*120;y=(i//8)*280
        # Preserve the full canvas at one texture pixel per display pixel.
        im=frame(hero,1,pose)
        board.paste(im,(x+12,y+65),im)
        d.text((x+12,y+30),f'pose {pose}',font=small,fill='#eedcc0')
        im=frame(hero,5,pose);board.paste(im,(x+12,y+190),im)
    board.save(target/f'{hero}-poses.png')
    sequence=[(0,1500)]+[(i,80) for _ in range(3) for i in range(2,8)]+[(0,800),(13,70),(14,70),(15,60),(0,1000),(16,130),(17,120),(16,130),(17,120),(0,1000),(19,50),(20,400),(19,50),(0,1500)]
    frames=[]; durations=[]
    for pose,duration in sequence:
        panel=Image.new('RGB',(800,320),(25,29,31)); d=ImageDraw.Draw(panel)
        d.text((18,15),hero.title()+' | tablet cadence; cloth / plate',font=font,fill='#eedcc0')
        for x,tier in ((160,1),(480,5)):
            im=frame(hero,tier,pose).resize((192,240),Image.Resampling.LANCZOS)
            panel.paste(im,(x,65),im)
        frames.append(panel); durations.append(duration)
    palette=sheet.quantize(255)
    frames=[im.quantize(palette=palette,dither=Image.Dither.NONE) for im in frames]
    frames[0].save(target/f'{hero}-animation.gif',save_all=True,append_images=frames[1:],duration=durations,loop=0,optimize=False,disposal=2)


def summary():
    """Compact, clearly labelled comparison at the same visible body height."""
    import subprocess
    from io import BytesIO
    from PIL import ImageFont
    target = HERE.parents[1]/'verification/heroes/pilot'
    canvas = Image.new('RGB',(1200,760),(25,29,31)); d = ImageDraw.Draw(canvas)
    title = ImageFont.load_default(size=27)
    label = ImageFont.load_default(size=17)
    small = ImageFont.load_default(size=14)
    d.text((28,19),'GRIMHOLLOW  /  TWO-CHARACTER ART PILOT',font=title,fill='#eedcc0')
    d.text((28,58),'Equal visible height. Existing portrait identity. Gameplay unchanged.',font=label,fill='#b7bab9')
    for row,hero in enumerate(PILOT):
        previous = Image.open(BytesIO(subprocess.check_output(['git','show',
            '5d1ecc2a7:core/src/main/assets/sprites/hero_'+hero+'.png'],cwd=HERE.parents[1]))).convert('RGBA')
        y=100+row*320
        d.text((28,y),hero.title(),font=title,fill='#eedcc0')
        d.text((28,y+40),'Cloth / plate',font=label,fill='#b7bab9')
        for group,(name,tiers) in enumerate((('Previous',(1,5)),('Pilot',(1,5)))):
            gx=210+group*440
            d.text((gx+64,y),name,font=label,fill='#eedcc0')
            for col,tier in enumerate(tiers):
                im=previous.crop((0,tier*FRAME_HEIGHT,FRAME_WIDTH,(tier+1)*FRAME_HEIGHT)) if group==0 else frame(hero,tier,0)
                box=im.getchannel('A').point(lambda a:255 if a>=8 else 0).getbbox()
                im=im.crop(box); factor=210/im.height
                im=im.resize((round(im.width*factor),210),Image.Resampling.LANCZOS)
                canvas.paste(im,(gx+col*190+(180-im.width)//2,y+39),im)
                # 54px body sample corresponds to a 3x world camera.
                native=im.resize((round(im.width*54/210),54),Image.Resampling.LANCZOS)
                canvas.paste(native,(gx+col*190+78,y+253),native)
        if row==1:d.text((28,y+270),'Small samples: 3x camera scale',font=small,fill='#b7bab9')
    canvas.save(target/'summary.png')
    if all((target/'native'/f'{hero}-{tier}'/'sewers-lighting-on.png').exists()
           for hero in PILOT for tier in (1,5)):
        board=Image.new('RGB',(920,980),(25,29,31)); draw=ImageDraw.Draw(board)
        draw.text((20,15),'Actual game captures | lighting on | crops at 100%',font=label,fill='#eedcc0')
        for row,hero in enumerate(PILOT):
            for col,tier in enumerate((1,5)):
                screenshot=Image.open(target/'native'/f'{hero}-{tier}'/'sewers-lighting-on.png')
                crop=screenshot.crop((680,320,1120,760))
                x=20+col*450;y=55+row*460
                draw.text((x,y),hero.title()+(' / cloth' if tier==1 else ' / plate'),font=label,fill='#eedcc0')
                board.paste(crop,(x,y+25))
        board.save(target/'ingame-comparison.png')

def equipment_review():
    from PIL import ImageFont
    target=HERE.parents[1]/'verification/heroes/pilot'
    names=('empty','Dagger','Greatsword','SpiritBow','MagesStaff','FocusCrystal','ThrowingKnife','Spear')
    board=Image.new('RGB',(1200,730),(25,29,31));draw=ImageDraw.Draw(board)
    font=ImageFont.load_default(size=16)
    draw.text((20,12),'Held equipment | native OpenGL crops at 100% | cloth / plate',font=font,fill='#eedcc0')
    for row,(hero,tier) in enumerate((('warrior',1),('warrior',5),('enchanter',1),('enchanter',5))):
        y=45+row*170
        draw.text((20,y),f'{hero.title()} / tier {tier}',font=font,fill='#eedcc0')
        for col,name in enumerate(names):
            path=target/'weapons'/f'{hero}-{tier}-{name}.png'
            if not path.exists():continue
            image=Image.open(path).convert('RGBA').crop((12,8,140,136))
            board.paste(image,(col*150+12,y+25),image)
            draw.text((col*150+12,y+151),name,font=font,fill='#b7bab9')
    board.save(target/'weapon-aware.png')


def portfolio():
    """Review every delivered figure and actual dungeon crop at labelled scales."""
    from PIL import ImageFont
    target=HERE.parents[1]/'verification/heroes'
    board=Image.new('RGB',(1200,1230),(25,29,31));draw=ImageDraw.Draw(board)
    title=ImageFont.load_default(size=26);label=ImageFont.load_default(size=17)
    small=ImageFont.load_default(size=13)
    draw.text((25,18),'GRIMHOLLOW / INDIVIDUAL PAINTED HEROES',font=title,fill='#eedcc0')
    draw.text((25,55),'Cloth / plate. Portrait identity preserved. Quiet idles. Held equipment follows your loadout.',font=label,fill='#b7bab9')
    live=Image.new('RGB',(1200,1080),(25,29,31));ld=ImageDraw.Draw(live)
    ld.text((25,18),'Actual dungeon captures / lighting on / crops at 100%',font=title,fill='#eedcc0')
    for i,hero in enumerate(ALL_HEROES):
        x=i%3*400;y=100+i//3*370
        draw.text((x+22,y),hero.title(),font=title,fill='#eedcc0')
        if hero not in HEROES:
            draw.text((x+22,y+55),'Awaiting its individual batch',font=label,fill='#b7bab9');continue
        for col,tier in enumerate((1,5)):
            im=frame(hero,tier,0)
            enlarged=im.resize((192,240),Image.Resampling.LANCZOS)
            board.paste(enlarged,(x+col*190+7,y+35),enlarged)
            native=im.resize((48,60),Image.Resampling.LANCZOS)
            board.paste(native,(x+col*190+77,y+280),native)
            draw.text((x+col*190+45,y+344),'Cloth' if tier==1 else 'Plate',font=small,fill='#b7bab9')
        path=target/'pilot/native'/f'{hero}-1'/'sewers-lighting-on.png' if hero in PILOT else target/hero/'native/1/sewers-lighting-on.png'
        lx=i%3*400;ly=65+i//3*335
        ld.text((lx+15,ly),hero.title()+' / cloth',font=label,fill='#eedcc0')
        if path.exists():
            capture=Image.open(path).convert('RGB')
            cx,cy=capture.width//2,capture.height//2
            crop=capture.crop((cx-190,cy-140,cx+190,cy+150))
            live.paste(crop,(lx+10,ly+30))
    board.save(target/'sprint-summary.png')
    live.save(target/'sprint-ingame.png')


if __name__ == '__main__':
    import argparse
    parser=argparse.ArgumentParser()
    parser.add_argument('--review', choices=HEROES, required=True)
    review(parser.parse_args().review)
    summary()
    equipment_review()
    portfolio()
