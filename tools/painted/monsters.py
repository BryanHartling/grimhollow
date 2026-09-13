"""Pack painted key poses into the existing monster animation rectangles.

No animation definitions, callbacks, combat rules or random numbers are changed.
Components are extracted by alpha connectivity because authored limbs can cross
the nominal source grid. All unassigned variant/quest frames remain pinned.
"""
from collections import deque
from functools import lru_cache
import json
import math
import numpy as np
from PIL import Image, ImageFilter
from pack import HERE, historical

CONTRACT = json.loads((HERE/'monsters.json').read_text(encoding='utf-8'))


@lru_cache(None)
def parts(name):
    source = Image.open(HERE/'sources/monsters'/f'{name}.png').convert('RGBA')
    assert source.getchannel('A').getextrema()[0] == 0, name
    # The reduced connectivity mask keeps this portable to Pillow/numpy-only CI.
    size = (source.width//2, source.height//2)
    alpha = np.asarray(source.getchannel('A').resize(size, Image.Resampling.BILINEAR))
    active = alpha >= 24
    labels = np.full(active.shape, -1, dtype=np.int16)
    groups = [[] for _ in range(16)]
    for sy, sx in zip(*np.nonzero(active)):
        if labels[sy, sx] != -1:
            continue
        queue = deque([(int(sy), int(sx))]); labels[sy, sx] = 16; points = []
        while queue:
            y, x = queue.popleft(); points.append((y, x))
            for dy, dx in ((-1,0),(1,0),(0,-1),(0,1),(-1,-1),(-1,1),(1,-1),(1,1)):
                yy, xx = y+dy, x+dx
                if 0 <= yy < size[1] and 0 <= xx < size[0] and active[yy,xx] and labels[yy,xx] == -1:
                    labels[yy,xx] = 16; queue.append((yy,xx))
        if len(points) < 5:
            continue
        ys, xs = np.array(points).T
        row = min(3, int(ys.mean()*4/size[1]))
        col = min(3, int(xs.mean()*4/size[0]))
        index = row*4+col
        labels[ys, xs] = index
        groups[index].append(len(points))
    result = []
    for index in range(16):
        assert groups[index] and max(groups[index]) > 400, (name, index, groups[index])
        mask = Image.fromarray(np.uint8(labels == index)*255).resize(source.size, Image.Resampling.NEAREST)
        mask = mask.filter(ImageFilter.MaxFilter(7))
        pixels = np.array(source)
        pixels[np.asarray(mask) == 0] = 0
        pixels[pixels[:,:,3] < 8] = 0
        part = Image.fromarray(pixels)
        box = part.getchannel('A').point(lambda a: 255 if a >= 16 else 0).getbbox()
        result.append(part.crop(box))
    return result


def rectangle(atlas, frame, index):
    w, h = (n*4 for n in frame)
    cols = atlas.width//w
    x, y = index%cols*w, index//cols*h
    assert y+h <= atlas.height, (frame, index, atlas.size)
    return x, y, x+w, y+h


def pose(art, size, scale, mode, step, count, name):
    phase = step/max(1,count-1)
    choice, angle, bob = 0, 0, 0
    if mode == 'idle':
        bob = -math.sin(phase*math.tau)*.7
        if name in ('bat','piranha'): choice = step%2
        if name == 'spawner': choice = 1 if 4 <= step <= 11 else 0
    elif mode == 'move':
        choice = step%2
        angle = (-1 if step%2 else 1)*1.4
        bob = -1 if step%2 else 0
    elif mode == 'attack':
        choice = 2
        angle = 2*(1-phase)
    else:
        choice = 3
        bob = 0
    original = art[choice]
    im = original.resize((max(1,round(original.width*scale)),max(1,round(original.height*scale))),Image.Resampling.LANCZOS)
    if angle: im = im.rotate(angle,Image.Resampling.BICUBIC,expand=True)
    canvas = Image.new('RGBA',size)
    canvas.alpha_composite(im, (round((size[0]-im.width)/2), round(size[1]-3-im.height+bob)))
    pixels = np.array(canvas); pixels[pixels[:,:,3] < 8] = 0
    return Image.fromarray(pixels)


def outputs():
    result = {}
    for spec in CONTRACT['monsters']:
        name = spec['name']; path = f"sprites/{spec.get('atlas',name)}.png"
        atlas = result.get(path)
        if atlas is None: atlas = historical(CONTRACT['base'],path).copy()
        art = parts(spec['sheet'])[spec['row']*4:spec['row']*4+4]
        size = tuple(v*4 for v in spec['frame'])
        scale = min((size[0]-8)/max(p.width for p in art), (size[1]-8)/max(p.height for p in art))
        occupied = set()
        for mode in ('idle','move','attack','defeated'):
            indices = spec[mode]
            for step, index in enumerate(indices):
                assert index not in occupied, (name,index)
                occupied.add(index)
                atlas.paste(pose(art,size,scale,mode,step,len(indices),name),rectangle(atlas,spec['frame'],index))
        result[path] = atlas
    return result


def sizes():
    paths = {f"sprites/{m.get('atlas',m['name'])}.png" for m in CONTRACT['monsters']}
    return {p: historical(CONTRACT['base'],p).size for p in paths}
