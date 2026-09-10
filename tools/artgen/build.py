"""SPDX-License-Identifier: GPL-3.0-or-later. Deterministic, source-only asset painter."""
import hashlib
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
import pngencode
pngencode.register()

ROOT = Path(__file__).resolve().parents[2]
SPEC_DIR = Path(__file__).parent / 'specs'
PALETTE = ['1A1816','3B3733','6B645C','2E241A','4A3B2A','3C2E1F','5A4630',
           '2C2F33','565B62','C9BFA8','EFE7D2','5E0D12','9E1B24','3F6A1F',
           '7BB33B','2E6F7A','6FD3E0','8A4B12','E0982F','8A7331','E4C76A','4A2C6E','9D6BD1']
COLORS = np.array([tuple(bytes.fromhex(x)) for x in PALETTE], dtype=np.uint8)

def locked(path):
    return path.with_suffix(path.suffix+'.lock').exists() or path.with_suffix('.lock').exists()

def procedural(spec):
    w,h = spec['dimensions']
    if spec['kind']=='talents':
        image=Image.new('RGBA',(w,h));d=ImageDraw.Draw(image)
        for index in range(320):
            x=index%16*32;y=index//16*32;accent=PALETTE[13+(index//32)%10]
            d.polygon([(x+16,y+2),(x+29,y+16),(x+16,y+29),(x+2,y+16)],fill='#0E0D0C')
            d.polygon([(x+16,y+5),(x+26,y+16),(x+16,y+26),(x+5,y+16)],fill='#'+PALETTE[1])
            d.line([(x+10,y+22),(x+16,y+9),(x+22,y+22)],fill='#'+accent,width=3)
            for bit in range(9):
                if (index+1)&(1<<bit):
                    bx=x+9+bit%3*5;by=y+10+bit//3*5
                    d.rectangle((bx,by,bx+2,by+2),fill='#'+PALETTE[10])
        return image
    if spec['kind']=='splash':
        # Original illustrated composition, independent of upstream splash images.
        image=Image.new('RGBA',(w,h),'#1A1816');d=ImageDraw.Draw(image)
        for y in range(0,h,36):
            for x in range(-40,w,80):
                x+=(y//36%2)*40
                d.rectangle((x+2,y+2,x+77,y+33),fill='#'+PALETTE[1+(x//80+y//36+spec['seed'])%2])
        d.ellipse((235,-130,755,470),fill='#0E0D0C');d.rectangle((235,170,755,450),fill='#0E0D0C')
        a='#'+PALETTE[spec['accent']]
        d.polygon([(440,195),(405,260),(372,449),(640,449),(593,248),(545,195)],fill=a)
        d.polygon([(487,193),(470,283),(504,426),(536,280),(522,193)],fill='#1A1816')
        d.ellipse((454,89,547,213),fill='#0E0D0C');d.ellipse((465,100,536,202),fill='#6B645C')
        d.polygon([(458,142),(468,101),(500,76),(538,109),(552,157),(521,127),(486,130)],fill=a)
        d.line((479,156,490,155),fill='#EFE7D2',width=3);d.line((516,155,527,156),fill='#EFE7D2',width=3)
        d.polygon([(430,224),(451,268),(404,334),(379,315)],fill='#2E241A')
        d.polygon([(554,224),(579,230),(624,312),(600,334)],fill='#2E241A')
        d.line((614,185,582,450),fill='#0E0D0C',width=20);d.line((614,185,582,450),fill='#8A7331',width=9)
        d.ellipse((592,156,636,208),fill='#0E0D0C');d.ellipse((600,164,628,200),fill=a)
        for x in [215,750]:
            d.rectangle((x,310,x+8,430),fill='#565B62');d.ellipse((x-6,293,x+14,319),fill='#E0982F')
        return image
    if spec['kind'] in ['hero','items','vectors']:
        sw,sh = spec.get('source_dimensions', [w,h])
        original = Image.new('RGBA',(sw,sh)); draw = ImageDraw.Draw(original)
        for y,x,length,role in spec.get('runs', []):
            draw.line((x,y,x+length-1,y), fill=tuple(COLORS[role])+(255,))
        for shape in spec.get('silhouette', []):
            points=shape['points']; color='#'+PALETTE[shape['role']]
            if shape['type']=='rect': draw.rectangle(points,fill=color)
            elif shape['type']=='ellipse': draw.ellipse(points,fill=color)
            elif shape['type']=='polygon': draw.polygon([tuple(p) for p in points],fill=color)
            elif shape['type']=='line': draw.line([tuple(p) for p in points], fill=color, width=shape.get('width',1))
        if spec['kind']=='hero':
            image=Image.new('RGBA',(w,h))
            sfw,sfh=spec.get("source_frame",[12,15]);fw,fh=spec.get("frame",[48,60])
            for y in range(sh//sfh):
                for x in range(sw//sfw):
                    frame=original.crop((x*sfw,y*sfh,x*sfw+sfw,y*sfh+sfh))
                    box=frame.getbbox()
                    if not box: continue
                    frame=frame.crop(box); ratio=min((fw-6)/frame.width,(fh-14)/frame.height)
                    frame=frame.resize((int(frame.width*ratio),int(frame.height*ratio)),Image.Resampling.NEAREST)
                    tile=Image.new('RGBA',(fw,fh)); tile.alpha_composite(frame,((fw-frame.width)//2,fh-6-frame.height))
                    outline=Image.new('RGBA',(fw,fh),'#0E0D0C'); outline.putalpha(tile.getchannel('A').filter(ImageFilter.MaxFilter(5)))
                    outline.alpha_composite(tile); image.alpha_composite(outline,(x*fw,y*fh))
        else:
            image=original.resize((w,h),Image.Resampling.NEAREST)
        pixels=np.array(image); yy,xx=np.indices((h,w),dtype=np.int64)
        shade=85+((xx*374761393+yy*668265263+spec['seed'])%16)
        pixels[:,:,:3]=(pixels[:,:,:3].astype(np.int32)*shade[:,:,None]//100).astype(np.uint8)
        return Image.fromarray(pixels)
    if spec['kind']=='vignette':
        yy,xx=np.indices((h,w))
        edge=np.minimum.reduce([xx,yy,w-1-xx,h-1-yy])
        pixels=np.zeros((h,w,4),dtype=np.uint8)
        pixels[:,:,:3]=COLORS[11]
        pixels[:,:,3]=(np.clip(1-edge/spec['edge_width'],0,1)**2*255).astype(np.uint8)
        return Image.fromarray(pixels)
    if spec['kind']=='blood':
        image=Image.new('RGBA',(w,h)); draw=ImageDraw.Draw(image)
        for shape in spec['silhouette']:
            draw.ellipse(shape['points'],fill='#'+PALETTE[shape['role']])
        return image
    if spec['kind'] == 'tile':
        sw,sh = spec['source_dimensions']
        original = Image.new('RGBA',(sw,sh))
        draw = ImageDraw.Draw(original)
        for y,x,length,role in spec['silhouette']:
            draw.line((x,y,x+length-1,y), fill=tuple(COLORS[role])+(255,))
        image = original.resize((w,h), Image.Resampling.NEAREST)
        pixels = np.array(image)
        # Dark mortar and irregular wet highlights widen each tile's value range.
        yy,xx=np.indices((h,w),dtype=np.int64)
        noise=((xx*374761393+yy*668265263+spec['seed']*1274126177)&0xffffffff)
        noise=((noise^(noise>>13))*1274126177)&0xffffffff
        material=((xx//5+yy//7)%7)/6
        target=.13+.26*material+.12*(noise%101)/100
        target=np.where((xx%16<2)|(yy%13<2),.055,target)
        rgb=pixels[:,:,:3].astype(float);lum=rgb@np.array([.2126,.7152,.0722])/255
        ratio=np.minimum(1,target/np.maximum(lum,.001))
        tint=np.clip((target-lum)/np.maximum(1-lum,.001),0,1)
        rgb=np.where((target<lum)[:,:,None],rgb*ratio[:,:,None],rgb+(255-rgb)*tint[:,:,None])
        pixels[:,:,:3]=np.rint(rgb).clip(0,255).astype(np.uint8)
        return Image.fromarray(pixels)
    canvas = Image.new('RGBA',(256,256))
    draw = ImageDraw.Draw(canvas)
    for shape in spec['silhouette']:
        color = '#'+PALETTE[shape['role']]
        points = shape['points']
        if shape['type']=='ellipse': draw.ellipse(points,fill=color)
        elif shape['type']=='rect': draw.rectangle(points,fill=color)
        elif shape['type']=='polygon': draw.polygon([tuple(p) for p in points],fill=color)
        else: raise ValueError('Unknown primitive: '+shape['type'])
    # Nearest sampling keeps the finite palette exact even on tiny launcher icons.
    return canvas.resize((w,h),Image.Resampling.NEAREST)

def paint(spec):
    import rendered
    if spec.get('rendered_liquid'):return rendered.liquid_atlas(spec['rendered_liquid'])
    if spec.get('rendered_ripple'):return rendered.ripple_atlas()
    if spec.get('rendered_character'):return rendered.character(spec,procedural(spec))
    if spec.get('rendered_water'):return rendered.tile('water')
    base=procedural(spec)
    return rendered.apply_tiles(spec,base) if spec.get('rendered_tiles') else base

def render(asset=None):
    import os,subprocess
    blender=Path(os.environ.get('BLENDER',ROOT/'.toolchain/blender/blender.exe'))
    env=os.environ.copy();env['BLENDER_USER_CONFIG']=str(ROOT/'.toolchain/blender/config')
    command=[str(blender),'-b','-t','4','--python-exit-code','1','-P',str(Path(__file__).parent/'blender/render.py')]
    if asset:command+=['--','--asset',asset]
    subprocess.run(command,check=True,env=env)
    import rendered
    rendered.tile.cache_clear()
    rendered.region_tile.cache_clear()
    rendered.liquid_frame.cache_clear()

def build():
    for source in sorted(SPEC_DIR.glob('*.json')):
        spec = json.loads(source.read_text())
        path = ROOT / spec['output']
        if locked(path):
            print('LOCKED',spec['output']); continue
        image = paint(spec)
        path.parent.mkdir(parents=True,exist_ok=True)
        if path.suffix == '.ico': image.save(path,format='ICO',sizes=[(16,16),(32,32),(48,48),(64,64),(128,128),(256,256)])
        elif path.suffix == '.icns': image.save(path,format='ICNS')
        else: image.save(path,format='PNG',compress_level=9,optimize=False)
        print(hashlib.sha256(path.read_bytes()).hexdigest(),spec['output'])
    # Historical POC evidence is immutable; stage 5.6 has its own comparison.
    if not (ROOT/'verification/render-poc.png').exists() and (Path(__file__).parent/'render_cache/necromancer/0/idle_0.png').exists():
        import rendered
        rendered.comparison([json.loads(p.read_text()) for p in sorted(SPEC_DIR.glob('*.json'))],procedural)

if __name__ == '__main__':
    import argparse
    parser=argparse.ArgumentParser();parser.add_argument('--render',action='store_true');parser.add_argument('--asset');args=parser.parse_args()
    if args.render:render(args.asset)
    build()
