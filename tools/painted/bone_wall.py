"""Pack the committed, authored bone-wall sections. No runtime generation."""
from PIL import Image, ImageOps
from pack import HERE


def outputs():
    source = Image.open(HERE/'sources/terrain/bone-wall.png').convert('RGBA')
    assert source.getchannel('A').getextrema()[0] == 0
    atlas = Image.new('RGBA', (256, 256))
    for index in range(4):
        x, y = index % 2, index // 2
        section = source.crop((x*source.width//2, y*source.height//2,
                               (x+1)*source.width//2, (y+1)*source.height//2))
        box = section.getchannel('A').point(lambda a: 255 if a >= 8 else 0).getbbox()
        assert box, index
        section = ImageOps.contain(section.crop(box), (122, 122), Image.Resampling.LANCZOS)
        atlas.alpha_composite(section, (x*128+(128-section.width)//2, y*128+126-section.height))
    return {'environment/painted_bone_wall.png': atlas}
