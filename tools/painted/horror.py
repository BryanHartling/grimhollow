"""Deterministically pack the original four-pose Horror painting; no generation in CI."""
from PIL import Image, ImageFilter
from pack import HERE

def outputs():
    im=Image.open(HERE/'sources/lurking-horror.png').convert('RGBA')
    poses=[]
    for y in range(2):
        for x in range(2):
            part=im.crop((x*im.width//2,y*im.height//2,(x+1)*im.width//2,(y+1)*im.height//2))
            poses.append(part.crop(part.getbbox()))
    scale=min(238/max(p.width for p in poses),238/max(p.height for p in poses))
    atlas=Image.new('RGBA',(512,512))
    for i,part in enumerate(poses):
        part=part.resize((round(part.width*scale),round(part.height*scale)),Image.Resampling.LANCZOS)
        part=part.filter(ImageFilter.UnsharpMask(radius=.7,percent=110,threshold=3))
        atlas.alpha_composite(part,(i%2*256+(256-part.width)//2,i//2*256+246-part.height))
    return {'sprites/lurking_horror.png':atlas}
