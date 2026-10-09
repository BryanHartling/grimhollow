"""Offline packing of boss architecture and Imp/forge props, in existing tile contracts."""
from functools import lru_cache
from PIL import Image, ImageOps
from pack import HERE, ASSETS, historical, LAYOUT, put, cell, fit, prop, panels

DIR=HERE/'sources/regional-v1297'

@lru_cache(None)
def source(name):
    im=Image.open(DIR/(name+'.png')).convert('RGBA')
    assert im.getchannel('A').getextrema()[0]==0, name+' needs genuine alpha'
    im.putalpha(im.getchannel('A').point(lambda a: 0 if a<8 else a))
    return im

@lru_cache(None)
def props():
    # Actual authored bounds, including broad objects crossing nominal grid lines.
    boxes=[(15,168,414,459),(484,12,714,478),(750,119,1154,473),(1163,147,1534,444),
           (36,473,361,1004),(376,537,798,957),(803,539,1187,964),(1241,460,1460,1001)]
    return [source('props').crop(box) for box in boxes]

def centered(art,w=60,h=58):
    art=fit(art,w,h);out=Image.new('RGBA',(64,64))
    out.alpha_composite(art,((64-art.width)//2,64-art.height-2));return out

def split(atlas,art,start,columns,rows):
    # One coherent painting, never eight separately resampled tiles.
    art=ImageOps.contain(art,(columns*64,rows*64),Image.Resampling.LANCZOS)
    whole=Image.new('RGBA',(columns*64,rows*64))
    whole.alpha_composite(art,((whole.width-art.width)//2,whole.height-art.height))
    stride=atlas.width//64
    for y in range(rows):
        for x in range(columns):put(atlas,start+x+y*stride,whole.crop((64*x,64*y,64*(x+1),64*(y+1))))

def ground(region):
    return cell(Image.open(ASSETS/f'environment/tiles_{region}.png').convert('RGBA'),0)

def material_contract(path,floor):
    # Retain footprint masks only; all old colors are replaced by painted stone.
    original=historical(LAYOUT,path)
    atlas=Image.new('RGBA',(original.width*4,original.height*4))
    for i in range(original.width//16*original.height//16):
        x,y=i%(original.width//16)*16,i//(original.width//16)*16
        mask=original.crop((x,y,x+16,y+16)).getchannel('A').resize((64,64),Image.Resampling.LANCZOS)
        tile=floor.copy();tile.putalpha(mask);put(atlas,i,tile)
    return atlas

def outputs():
    p=props();result={}
    floor=ground('caves')
    caves=material_contract('environment/custom_tiles/caves_boss.png',floor)
    # The barrier must occupy all five blocked cells, including its end posts.
    art=source('gate');gate=art.crop(art.getbbox()).resize((320,64),Image.Resampling.LANCZOS)
    for x in range(5):
        put(caves,40+x,gate.crop((64*x,0,64*(x+1),64)))
        opened=Image.new('RGBA',(64,64))
        if x in (0,4):opened=gate.crop((64*x,0,64*(x+1),64))
        # Broken gate leaves only the sideposts, with an unobstructed middle.
        if x==0:opened.paste((0,0,0,0),(22,0,64,64))
        if x==4:opened.paste((0,0,0,0),(0,0,42,64))
        put(caves,32+x,opened)
    plate=centered(p[3]);put(caves,37,plate)
    put(caves,38,centered(p[2]));
    # Pylon's nine-cell paving uses the same readable carved stone floor.
    for i in (45,46,47,53,54,55,61,62,63):put(caves,i,floor)
    # Three wide, two tall entrance; original logical index layout is unchanged.
    stairs=ImageOps.fit(source('stairs'),(192,128),Image.Resampling.LANCZOS)
    for y,row in enumerate(((1,2,3),(9,10,11))):
        for x,i in enumerate(row):put(caves,i,stairs.crop((64*x,64*y,64*(x+1),64*(y+1))))
    low,high=prop(Image.new('RGBA',(64,64)),p[4],58,118)
    for i in (17,19,30,31):put(caves,i,low)
    for i in (6,14):put(caves,i,high)
    result['environment/custom_tiles/caves_boss.png']=caves

    city=material_contract('environment/custom_tiles/city_boss.png',ground('city'))
    # Old stair-shadow geometry must not paint over the new complete staircase.
    for i in range(32):put(city,i,Image.new('RGBA',(64,64)))
    split(city,source('stairs'),32,7,7)
    low,high=prop(Image.new('RGBA',(64,64)),p[1],58,116)
    put(city,125,low);put(city,117,high)
    put(city,101,centered(p[2]));put(city,108,centered(p[2]))
    low,high=prop(Image.new('RGBA',(64,64)),panels('region-props.png')[4],48,90)
    put(city,124,low);put(city,116,high)
    # Throne guards span two side-by-side cells. No inherited pixel background.
    for start,flip in ((71,False),(87,True)):
        figure=fit(p[4],100,64)
        if flip:figure=figure.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        whole=Image.new('RGBA',(128,64));whole.alpha_composite(figure,((128-figure.width)//2,64-figure.height))
        put(city,start,whole.crop((0,0,64,64)));put(city,start+8,whole.crop((64,0,128,64)))
    split(city,p[7],102,2,4)
    result['environment/custom_tiles/city_boss.png']=city

    # v4 callers require sixteen columns: pit at (0,0), barrier at (5,1),
    # separate arrival stairs at (8,1). The old eight-column sheet aliased the pit.
    quest=Image.new('RGBA',(1024,512))
    split(quest,p[5],0,5,5)
    split(quest,p[6],24,3,3)
    from playtest_polish import panels as polish_panels
    banner=polish_panels('office')[2]
    # Wall hangings must fit within one wall face. The former two-cell rug
    # continued down onto walkable paving and covered wall-torch flames.
    hanging=Image.new('RGBA',(64,64));cloth_art=fit(banner,40,44)
    hanging.alpha_composite(cloth_art,((64-cloth_art.width)//2,18))
    put(quest,80,hanging);put(quest,81,hanging.transpose(Image.Transpose.FLIP_LEFT_RIGHT))
    put(quest,82,Image.new('RGBA',(64,64)))
    # Barrier uses authored metalwork rather than the obsolete neon pixel checker.
    barrier=fit(source('gate'),192,192)
    whole=Image.new('RGBA',(192,192));whole.alpha_composite(barrier,(0,(192-barrier.height)//2))
    for y in range(3):
        for x in range(3):put(quest,21+x+y*16,whole.crop((64*x,64*y,64*(x+1),64*(y+1))))
    result['environment/custom_tiles/city_quest.png']=quest
    carpet=historical(LAYOUT,'environment/custom_tiles/carpet.png')
    carpet=carpet.resize((carpet.width*4,carpet.height*4),Image.Resampling.NEAREST)
    rug=fit(banner,192,192).resize((192,192),Image.Resampling.LANCZOS)
    cloth=rug.crop((64,64,128,128));cloth.putalpha(255)
    # Independent border bits also cover one-cell-wide/long carpet strips.
    for mask in range(16):
        tile=cloth.copy()
        for bit,box,dest in ((1,(64,0,128,12),(0,0)),(2,(180,64,192,128),(52,0)),
                             (4,(64,180,128,192),(0,52)),(8,(0,64,12,128),(0,0))):
            if mask&bit:tile.paste(rug.crop(box),dest)
        for vertical,horizontal,box,dest in ((1,8,(0,0,12,12),(0,0)),(1,2,(180,0,192,12),(52,0)),
                                           (4,8,(0,180,12,192),(0,52)),(4,2,(180,180,192,192),(52,52))):
            if mask&vertical and mask&horizontal:tile.paste(rug.crop(box),dest)
        put(carpet,48+mask,tile)
    # These are floor overrides beneath normal statues/daises, not second sprites.
    # The old overrides contained low-resolution duplicate statue fragments.
    for i in range(80,89):put(carpet,i,cloth)
    stair=cloth.copy()
    stair.alpha_composite(centered(Image.open(HERE/'sources/playtest-polish/stairs-up.png').convert('RGBA')))
    put(carpet,82,stair)
    dais=cloth.copy();dais.alpha_composite(centered(p[2]));put(carpet,81,dais)
    result['environment/custom_tiles/carpet.png']=carpet
    return result
