"""Offline packing of authored v1.23 playtesting paintings; no generation in CI."""
from PIL import Image,ImageOps
from pack import HERE,BASE,historical

def source(name,bounds):
    im=Image.open(HERE/'sources/playtest-v123'/f'{name}.png').convert('RGBA')
    assert im.getchannel('A').getextrema()[0]==0, name+' requires transparent background'
    a=im.getchannel('A').point(lambda v:0 if v<8 else v);im.putalpha(a)
    box=a.point(lambda v:255 if v>=24 else 0).getbbox();assert box,name+' is empty'
    return ImageOps.contain(im.crop(box),bounds,Image.Resampling.LANCZOS)

def center(im,size):
    out=Image.new('RGBA',size);out.alpha_composite(im,((size[0]-im.width)//2,(size[1]-im.height)//2));return out

def well_icon():return center(source('well',(60,60)),(64,64))

def outputs():
    cursor=Image.new('RGBA',(64,64));cursor.alpha_composite(source('cursor',(60,60)),(2,2))
    compass=center(source('compass',(30,22)),(32,24))
    well=Image.new('RGBA',(320,64))
    for region in range(5):well.paste(well_icon(),(region*64,0))
    rat=historical(BASE,'environment/custom_tiles/rat_king_room.png').copy()
    assert rat.size==(512,64),rat.size
    flat=center(source('statue',(60,60)),(64,64))
    whole=Image.new('RGBA',(64,128));statue=source('statue',(60,112))
    whole.alpha_composite(statue,((64-statue.width)//2,124-statue.height))
    for index,im in ((0,flat),(1,whole.crop((0,64,64,128))),(2,whole.crop((0,0,64,64))),(4,flat)):
        rat.paste(im,(index*64,0))
    return {'gdx/grimhollow_cursor.png':cursor,'interfaces/painted_compass.png':compass,
            'environment/custom_tiles/weak_floor.png':well,'environment/custom_tiles/rat_king_room.png':rat}
