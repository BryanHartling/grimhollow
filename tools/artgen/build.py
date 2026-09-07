"""SPDX-License-Identifier: GPL-3.0-or-later. Deterministic, source-only asset painter."""
import hashlib
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
SPEC_DIR = Path(__file__).parent / 'specs'
PALETTE = ['1A1816','3B3733','6B645C','2E241A','4A3B2A','3C2E1F','5A4630',
           '2C2F33','565B62','C9BFA8','EFE7D2','5E0D12','9E1B24','3F6A1F',
           '7BB33B','2E6F7A','6FD3E0','8A4B12','E0982F','8A7331','E4C76A','4A2C6E','9D6BD1']
COLORS = np.array([tuple(bytes.fromhex(x)) for x in PALETTE], dtype=np.uint8)

def locked(path):
    return path.with_suffix(path.suffix+'.lock').exists() or path.with_suffix('.lock').exists()

def paint(spec):
    w,h = spec['dimensions']
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
        # Coordinate-hashed fine grain plus broad material modulation. No RNG state or timestamps.
        yy,xx = np.indices((h,w),dtype=np.int64)
        grain = (xx*374761393 + yy*668265263 + spec['seed']*1274126177) & 0xffffffff
        grain = ((grain ^ (grain >> 13))*1274126177) & 0xffffffff
        shades = 70 + (grain%21) + ((xx//11+yy//17)%5)
        pixels[:,:,:3] = (pixels[:,:,:3].astype(np.int32)*shades[:,:,None]//100).astype(np.uint8)
        # Per-frame exposure controls bright stone/bone while preserving palette hue.
        for ty in range(0,h,64):
            for tx in range(0,w,64):
                tile=pixels[ty:ty+64,tx:tx+64]
                active=tile[:,:,3]>0
                if active.any():
                    mean=(tile[:,:,:3][active] @ np.array([.2126,.7152,.0722])/255).mean()
                    if mean>.42: tile[:,:,:3]=(tile[:,:,:3]*(.42/mean)).astype(np.uint8)
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

if __name__ == '__main__': build()
