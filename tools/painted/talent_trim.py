"""Vector-drawn bronze studs and shuffle medallion for the painted interface.

Original GPL-3.0-or-later geometry; deterministic Pillow packing, no generator.
"""
from PIL import Image, ImageDraw
import math

CELL = 64
OVERSAMPLE = 4


def stud(state):
    im = Image.new('RGBA', (CELL*OVERSAMPLE, CELL*OVERSAMPLE))
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            dx, dy = x/OVERSAMPLE-31.5, y/OVERSAMPLE-31.5
            r = math.hypot(dx, dy)
            if r > 25:
                continue
            edge = max(0, min(1, (25-r)*2))
            if r > 21:
                bright = .5 - .3*dy/25
                base = (145, 111, 74) if state != 'empty' else (101, 91, 78)
                rgb = tuple(round(c*(.6+bright)) for c in base)
            elif state == 'empty' or state == 'future':
                rgb = (29, 26, 23) if state == 'empty' else (48, 43, 37)
            else:
                shade = .85 - .28*dy/25 - .14*r/25
                base = (227, 171, 106) if state == 'spent' else (255, 237, 185)
                rgb = tuple(min(255, round(c*shade)) for c in base)
            px[x, y] = (*rgb, round(255*edge))
    d = ImageDraw.Draw(im)
    if state == 'available':
        d.polygon([(32*4,14*4),(39*4,32*4),(32*4,50*4),(25*4,32*4)], fill=(255,249,220,255))
    elif state == 'spent':
        d.arc((13*4,13*4,50*4,50*4),200,310,fill=(247,206,148,235),width=2*4)
    return im.resize((CELL,CELL), Image.Resampling.LANCZOS)


def outputs():
    from interface_art import panels, patch
    atlas = Image.new('RGBA',(512,64))
    for index,state in enumerate(('empty','spent','available','spent','future')):
        atlas.paste(stud(state),(index*CELL,0))
    shuffle = stud('empty').resize((256,256),Image.Resampling.LANCZOS)
    d = ImageDraw.Draw(shuffle)
    d.line([(62,72),(92,72),(164,184),(192,184)],fill=(126,91,58,255),width=16,joint='curve')
    d.polygon([(184,160),(212,184),(184,208)],fill=(199,156,98,255))
    d.line([(62,184),(92,184),(164,72),(192,72)],fill=(30,25,21,255),width=29,joint='curve')
    d.line([(62,184),(92,184),(164,72),(192,72)],fill=(239,206,149,255),width=13,joint='curve')
    d.polygon([(184,48),(212,72),(184,96)],fill=(239,206,149,255))
    atlas.paste(shuffle.resize((64,64),Image.Resampling.LANCZOS),(5*CELL,0))
    atlas.paste(patch(panels()[0],(64,64),(6,6,6,6)),(6*CELL,0))
    return {'interfaces/painted_talents.png':atlas}
