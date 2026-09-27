"""Pack the committed authored particle motifs; no image generation at build time."""
from PIL import Image, ImageOps
import numpy as np
from pack import HERE

def outputs():
    source=Image.open(HERE/'sources/particles/motifs.png').convert('RGBA')
    assert source.getchannel('A').getextrema()[0]==0
    atlas=Image.new('RGBA',(256,256))
    for i in range(16):
        x,y=i%4,i//4
        cell=source.crop((x*source.width//4,y*source.height//4,(x+1)*source.width//4,(y+1)*source.height//4))
        box=cell.getchannel('A').point(lambda a:255 if a>=8 else 0).getbbox()
        assert box, ('empty particle motif',i)
        part=ImageOps.contain(cell.crop(box),(58,58),Image.Resampling.LANCZOS)
        atlas.alpha_composite(part,(x*64+(64-part.width)//2,y*64+(64-part.height)//2))
    from status import symbols
    from interface_art import glyphs
    status=symbols();icons=glyphs()
    def motif(index):
        return atlas.crop((index%4*64,index//4*64,index%4*64+64,index//4*64+64))
    # Preserve the meanings of the legacy sixteen Speck indices.
    art=[status[44],motif(10),motif(5),status[87],icons['RIGHTARROW'].rotate(90),
         status[18],motif(12),motif(13),motif(2),icons['AUDIO'],icons['REPEAT'],
         status[21],motif(14),motif(8),icons['COIN_SML'],icons['RIGHTARROW'].rotate(270)]
    specks=Image.new('RGBA',(256,256))
    for i,part in enumerate(art):
        part=ImageOps.contain(part,(58,58),Image.Resampling.LANCZOS)
        specks.alpha_composite(part,(i%4*64+(64-part.width)//2,i//4*64+(64-part.height)//2))
    source=Image.open(HERE/'sources/particles/rays.png').convert('RGBA')
    from inventory import gutters
    rows=gutters(np.asarray(source.getchannel('A')),1)
    rays=Image.new('RGBA',(256,256))
    for i in range(4):
        part=source.crop((0,rows[i],source.width,rows[i+1]))
        box=part.getchannel('A').point(lambda a:255 if a>=8 else 0).getbbox()
        assert box, ('empty ray',i)
        part=ImageOps.contain(part.crop(box),(252,58),Image.Resampling.LANCZOS)
        rays.alpha_composite(part,((256-part.width)//2,i*64+(64-part.height)//2))
    return {'effects/painted_particles.png':atlas,'effects/painted_specks.png':specks,'effects/painted_rays.png':rays}
