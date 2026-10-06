"""Painted Chart states and distinct memory veil; deterministic offline packing."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
from inventory import icon
HERE=Path(__file__).resolve().parent

def items(cell_size=64):
    make_icon=lambda image:icon(image,cell_size)
    chart=Image.open(HERE/'sources/wayward/chart.png').convert('RGBA')
    mound=Image.open(HERE/'sources/wayward/cache.png').convert('RGBA')
    for source in (chart,mound):
        if source.getchannel('A').getextrema()[0]!=0:raise ValueError('Wayward source lacks genuine transparency')
    images={'WAYWARD_CHART':make_icon(chart)}
    for panel,name in enumerate(('WAYWARD_FULL','WAYWARD_PARTIAL','WAYWARD_EMPTY')):
        images[name]=make_icon(mound.crop((round(panel*mound.width/3),0,round((panel+1)*mound.width/3),mound.height)))
    # A painted cross taken from the actual chart, isolated with its red pigment.
    cross=chart.crop((round(chart.width*.64),round(chart.height*.42),round(chart.width*.76),round(chart.height*.55)))
    pixels=cross.load();alpha=Image.new('L',cross.size);a=alpha.load()
    for y in range(cross.height):
        for x in range(cross.width):
            r,g,b,v=pixels[x,y]
            if r>55 and r>g*2.4 and r>b*2.4:a[x,y]=v
    # Retain the connected pigment stroke, excluding isolated red-brown paper flecks.
    from collections import deque
    remaining={(x,y) for y in range(cross.height) for x in range(cross.width) if a[x,y]>=16}
    largest=set()
    while remaining:
        seed=min(remaining);remaining.remove(seed);part={seed};queue=deque([seed])
        while queue:
            x,y=queue.popleft()
            for dx in (-1,0,1):
                for dy in (-1,0,1):
                    point=x+dx,y+dy
                    if point in remaining:remaining.remove(point);part.add(point);queue.append(point)
        if len(part)>len(largest):largest=part
    for y in range(cross.height):
        for x in range(cross.width):
            if (x,y) not in largest:a[x,y]=0
    cross.putalpha(alpha)
    from PIL import ImageEnhance
    images['WAYWARD_MARKER']=make_icon(ImageEnhance.Brightness(cross).enhance(2))
    return images

def outputs():
    images=items();atlas=Image.new('RGBA',(192,64))
    for n,name in enumerate(('WAYWARD_FULL','WAYWARD_PARTIAL','WAYWARD_EMPTY')):atlas.paste(images[name],(n*64,0))
    # This is memory ink, not unexplored black. It is applied on a separate layer.
    paper=Image.open(HERE/'sources/wayward/chart.png').convert('RGBA')
    paper=paper.crop((paper.width*4//10,paper.height*4//10,paper.width*6//10,paper.height*6//10)).resize((64,64),Image.Resampling.LANCZOS).filter(ImageFilter.GaussianBlur(4))
    paper.putalpha(255)
    from PIL import ImageEnhance
    paper=ImageEnhance.Brightness(ImageEnhance.Color(paper).enhance(.6)).enhance(.27)
    # A subdued, continuous faded-paper field has no repeated glyphs or tile grid.
    paper=Image.blend(Image.new('RGBA',(64,64),(46,39,29,255)),paper,.2)
    return {'environment/custom_tiles/wayward_cache.png':atlas,'effects/wayward_veil.png':paper}
