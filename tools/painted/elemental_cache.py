"""Reproducible six-state elemental mechanisms from the committed painted source."""
from PIL import Image
from pack import HERE
from inventory import icon

def outputs():
    source=Image.open(HERE/'sources/sprint/elemental-cache.png').convert('RGBA')
    assert source.getchannel('A').getextrema()[0]==0
    atlas=Image.new('RGBA',(256,128))
    for row in range(2):
        for col in range(3):
            part=source.crop((col*source.width//3,row*source.height//2,(col+1)*source.width//3,(row+1)*source.height//2))
            atlas.alpha_composite(icon(part),(col*64,row*64))
    return {'environment/custom_tiles/elemental_cache.png':atlas}
