"""Painted room details assembled into the v4 quest and mining layout contracts."""
from functools import lru_cache
from PIL import Image, ImageEnhance
import numpy as np
from pack import HERE, LAYOUT, historical, panels, fit, prop, cell, put, surface_mask
from terrain_details import sized


@lru_cache(None)
def details():
    source=Image.open(HERE/'sources/tablet-room-props.png').convert('RGBA')
    # Authored object bounds: the rail is taller than the nominal first row.
    return [source.crop(box) for box in [(200,0,475,540),(680,130,1505,380),
                                       (65,575,705,980),(855,470,1315,990)]]


@lru_cache(None)
def forge():
    source=Image.open(HERE/'sources/mine-workshop.png').convert('RGBA')
    return [source.crop(box) for box in [(20,0,640,749),(670,0,1470,749),(1490,0,2098,749)]]


def centered(source,size=(56,56)):
    source=fit(source,*size)
    out=Image.new('RGBA',(64,64))
    out.alpha_composite(source,((64-source.width)//2,(64-source.height)//2))
    return out


def connected(region,atlas,features):
    shelf=sized(details()[1],(192,32))
    strip=Image.new('RGBA',(64,64))
    strip.paste(shelf.crop((64,0,128,32)),(0,0))
    strip.paste(shelf.crop((0,0,64,32)),(0,32))
    for index in [50,54,92,93,94,95,108,109,110,111]:put(atlas,index,strip)
    # The wooden top and overhang retain upstream visibility/cutaway masks.
    old=historical(LAYOUT,'environment/tiles_'+region+'.png').resize(atlas.size,Image.Resampling.NEAREST)
    timber=details()[1].crop((50,10,700,23)).resize((64,64),Image.Resampling.LANCZOS)
    for index in list(range(176,192))+list(range(200,204)):
        put(atlas,index,surface_mask(cell(old,index),timber))
    if region!='caves':return
    # Repeated straight rails share the same edge positions, with no per-cell pedestal.
    source=details()[0].crop((0,100,275,420))
    vertical=sized(source,(44,64))
    rail=Image.new('RGBA',(64,64));rail.alpha_composite(vertical,(10,0))
    horizontal=rail.transpose(Image.Transpose.ROTATE_90)
    for mask in range(16):
        tile=Image.new('RGBA',(64,64))
        if mask==0:tile=rail.copy()
        for bit,art,box in [(1,rail,(0,0,64,32)),(2,horizontal,(32,0,64,64)),
                            (4,rail,(0,32,64,64)),(8,horizontal,(0,0,32,64))]:
            if mask&bit:tile.alpha_composite(art.crop(box),(box[0],box[1]))
        put(features,224+mask,tile)
    for alt in range(2):
        put(features,169+alt,rail)
        ground=cell(atlas,4 if alt else 0)
        put(atlas,130+alt,ground)
        put(atlas,242+alt,Image.new('RGBA',(64,64)))
        flat=ground.copy();flat.alpha_composite(rail);put(atlas,74+alt,flat)


def mining(base,features,crystal):
    kind='crystal' if crystal else 'gnoll'
    path='environment/tiles_caves_'+kind+'.png'
    old=historical(LAYOUT,path).resize((1024,1024),Image.Resampling.NEAREST)
    atlas=base.copy()
    # Both mining branches share the painted Caves geology, without wall torches.
    ore=centered(forge()[2],(49,42))
    for index in [49,53]+list(range(84,88))+list(range(100,104)):
        face=cell(base,48);face.alpha_composite(ore,(0,4));put(atlas,index,face)
    # Internal walls expose only a 20px strip on each open side. A centered
    # deposit is almost entirely clipped by those strips, hiding mineable ore.
    side_ore=fit(forge()[2],16,28)
    for index in range(160,176):
        ore_cap=cell(base,48)
        ore_cap.alpha_composite(side_ore,(2,14))
        ore_cap.alpha_composite(side_ore.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(46,14))
        tile=surface_mask(cell(old,index),ore_cap)
        if index>160:
            bare=surface_mask(cell(old,index),cell(base,48))
            changed=np.any(np.array(tile)[:,:,:3]!=np.array(bare)[:,:,:3],axis=2)
            assert np.count_nonzero(changed & (np.array(tile)[:,:,3]>0))>=160,('clipped ore',kind,index)
        put(atlas,index,tile)
    # Overhangs expose a horizontal band at y=32..47, not the tile center.
    rim_ore=fit(forge()[2],36,14)
    ore_cap=cell(base,48);ore_cap.alpha_composite(rim_ore,((64-rim_ore.width)//2,33))
    for index in range(196,200):
        put(atlas,index,surface_mask(cell(old,index),ore_cap))
    floor=cell(base,0)
    rocks=panels('region-props.png')[6]
    colors=[panels('details.png')[7]]
    # Existing authored crystals, tinted to the game's three established mineral colors.
    for order in [(1,0,2,3),(0,2,1,3)]:
        pixels=np.array(colors[0]);colors.append(Image.fromarray(pixels[:,:,order]))
    for variant in range(6 if crystal else 3):
        art=colors[variant//2] if crystal else rocks
        if variant%2:art=art.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        low,high=prop(Image.new('RGBA',(64,64)),art,58,76 if crystal else 70)
        put(features,(210 if crystal else 216)+variant,low)
        put(atlas,132+variant,floor)
        # Crystal overhang alternatives are keyed by color rather than lower variant.
        put(atlas,244+(variant//2 if crystal else variant),high)
        if not crystal or variant%2==0:
            put(atlas,76+(variant//2 if crystal else variant),prop(floor,art,58,56,True)[0])
    for index in [5,11]:
        art=rocks if not crystal else colors[0]
        put(atlas,index,prop(floor,art,50,30,True)[0])
    return path,atlas


def outputs(world,features):
    output={}
    for region in ['sewers','prison','caves','city','halls']:
        connected(region,world['environment/tiles_'+region+'.png'],features)
    # Sewers uses the same source sheet for the wall layer.
    world['environment/walls_sewers.png']=world['environment/tiles_sewers.png'].copy()
    for crystal in [False,True]:
        path,atlas=mining(world['environment/tiles_caves.png'],features,crystal);output[path]=atlas

    path='environment/custom_tiles/prison_quest.png'
    prison=historical(LAYOUT,path).resize((1024,1024),Image.Resampling.NEAREST)
    table=Image.new('RGBA',(64,128));art=sized(details()[2],(60,94));table.alpha_composite(art,(2,28))
    prison.paste(table,(0,0))
    marker=Image.new('RGBA',(320,320))
    # Authored sockets sit near the outside of the sigil. Scaling to 160px places
    # them at the cardinal centers one tile from the ritual cell, not the corners.
    ring=sized(Image.open(HERE/'sources/ritual-mark.png').convert('RGBA'),(160,160))
    marker.alpha_composite(ring,(80,80))
    prison.paste(marker,(0,128))
    from inventory import panels as item_panels
    bones=item_panels('treasures')[0]
    put(prison,3,centered(bones,(44,32)));put(prison,4,centered(bones,(38,38)));put(prison,19,centered(bones,(50,40)))
    # The mass-grave footprint remains 9x9; transparent bones use the actual floor.
    for y in range(9):
        for x in range(9):
            art=bones.transpose(Image.Transpose.FLIP_LEFT_RIGHT) if (x+y)%2 else bones
            put(prison,(5+x)+16*y,centered(art,(54,45)))
    output[path]=prison

    path='environment/custom_tiles/caves_quest.png'
    smithy=Image.new('RGBA',(256,512))
    floor=cell(world['environment/tiles_caves.png'],4)
    hatch=centered(forge()[0],(60,60))
    put(smithy,0,hatch)
    # All nine entrance/platform cells use the same floor as the smithy.
    # Exactly one center cell carries a ladder/hatch.
    for y in range(1,4):
        for x in range(3):put(smithy,x+4*y,floor)
    middle=floor.copy();middle.alpha_composite(hatch);put(smithy,9,middle)
    for index in [20,21]:put(smithy,index,cell(world['environment/tiles_caves.png'],20))
    low,high=prop(floor,details()[3],62,103)
    put(smithy,7,low);put(smithy,3,high)
    work=Image.new('RGBA',(128,64));art=sized(forge()[1],(122,58));work.alpha_composite(art,(3,3))
    for index,box in [(16,(0,0,64,64)),(17,(64,0,128,64))]:
        tile=floor.copy();tile.alpha_composite(work.crop(box));put(smithy,index,tile)
    # The lower-right cell is a separate water-filled quenching basin, not a
    # third fragment of the anvil. Its gameplay water flag remains unchanged.
    from regional_scenery import props
    put(smithy,18,centered(props()[0],(60,54)))
    output[path]=smithy
    return output
