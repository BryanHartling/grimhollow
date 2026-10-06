"""Offline packing of the approved exit, cushion and Rebuff paintings."""
from PIL import Image, ImageOps
from pack import HERE

def source(name, bounds):
    image=Image.open(HERE/'sources/mystery'/f'{name}.png').convert('RGBA')
    assert image.getchannel('A').getextrema()[0]==0, name+' requires real alpha'
    alpha=image.getchannel('A').point(lambda v:0 if v<8 else v)
    image.putalpha(alpha)
    box=alpha.point(lambda v:255 if v>=16 else 0).getbbox()
    assert box, name+' is empty'
    return ImageOps.contain(image.crop(box),bounds,Image.Resampling.LANCZOS)

def cushion():
    tile=Image.new('RGBA',(64,64));part=source('cushion',(60,54))
    tile.alpha_composite(part,((64-part.width)//2,(64-part.height)//2))
    return tile

def outputs():
    # Preserve the existing three-by-five world footprint. Split a single
    # painting rather than repeating masonry cells across the gate.
    atlas=Image.new('RGBA',(192,384));whole=source('sewer-exit',(192,320))
    atlas.alpha_composite(whole,((192-whole.width)//2,320-whole.height))
    locked=atlas.crop((64,128,128,192))
    from readability import cutouts
    lock=ImageOps.contain(cutouts('details.png',4,2)[5],(28,34),Image.Resampling.LANCZOS)
    locked.alpha_composite(lock,((64-lock.width)//2,20))
    atlas.paste(locked,(0,320))
    return {'environment/custom_tiles/painted_sewer_exit.png':atlas}
