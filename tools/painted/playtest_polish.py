"""Offline v1.29 paintings: final sanctum, office furnishings and retained hero talents.

This is atlas assembly only. Source paintings and exact generator prompts are committed.
All world cells stay sixteen logical units and use sixty-four texture pixels.
"""
from functools import lru_cache
import re
from PIL import Image, ImageOps
from pack import HERE, ROOT, historical, BASE, put
from inventory import icon

@lru_cache(None)
def panels(name,cols=4,rows=2):
    im=Image.open(HERE/'sources/playtest-v129'/f'{name}.png').convert('RGBA')
    assert im.getchannel('A').getextrema()[0]==0,(name,'genuine alpha required')
    if name=='office':
        # The authored sheet has broad objects which straddle nominal grid lines.
        # Isolate their actual bounds; equal cells would leak a neighbor into the rug.
        return [im.crop(box) for box in [(29,150,490,510),(541,14,813,510),
                (889,10,1138,538),(1185,127,1506,489),(28,553,480,960),
                (520,605,826,943),(885,593,1138,960),(1194,599,1521,944)]]
    return [im.crop((round(x*im.width/cols),round(y*im.height/rows),
                     round((x+1)*im.width/cols),round((y+1)*im.height/rows)))
            for y in range(rows) for x in range(cols)]

def opaque(im):
    im=im.copy();im.putalpha(255);return im

def tile_part(image,box,size):
    return image.crop(box).resize(size,Image.Resampling.LANCZOS)

def halls():
    p=panels('halls')
    out=Image.new('RGBA',(512,512))
    # The floor painting ends before the nominal panel edge. Cropping away its
    # transparent gutter prevents a black horizontal seam in each world cell.
    floor=opaque(p[0].crop((0,0,p[0].width,470)).resize((64,64),Image.Resampling.LANCZOS))
    stone=opaque(p[1].crop((0,0,p[1].width,175)).resize((64,64),Image.Resampling.LANCZOS))
    # Copy only alpha footprints from the old contract, never its colors.
    old=historical(BASE,'environment/custom_tiles/halls_special.png')
    old=old.resize(out.size,Image.Resampling.NEAREST)
    for i in range(64):
        if old.crop((i%8*64,i//8*64,i%8*64+64,i//8*64+64)).getbbox():
            put(out,i,stone)
    for i in (19,23,36,37):put(out,i,floor)
    # Continuous side masonry and south-facing platform lip.
    parapet=ImageOps.fit(p[2],(192,64),Image.Resampling.LANCZOS)
    for row in ((24,25,26),(28,29,30),(32,33,34),(40,41,48)):
        for n,i in enumerate(row):put(out,i,parapet.crop((n*64,0,(n+1)*64,64)))
    put(out,49,out.crop((64,320,128,384)))
    rim=opaque(p[1].crop((0,175,p[1].width,380)).resize((192,64),Image.Resampling.LANCZOS))
    put(out,35,rim.crop((64,0,128,64)))
    # Shrine is assembled across three columns and two rows. Open state preserves layout.
    for kind,indices in ((6,((10,11,12),(18,19,20))),(7,((1,0,2),(9,23,13)))):
        door=icon(p[kind],192)
        door=ImageOps.fit(door,(192,128),Image.Resampling.LANCZOS)
        for y,row in enumerate(indices):
            for x,i in enumerate(row):
                piece=stone.copy();piece.alpha_composite(door.crop((x*64,y*64,(x+1)*64,(y+1)*64)));put(out,i,piece)
    # Thin stained-glass side bays use the same connected masonry baseline.
    window=icon(p[3],64)
    for i in (8,14,16,22):
        t=stone.copy();t.alpha_composite(window);put(out,i,t)
    # Original upper cutaways retain their visibility silhouettes.
    for i in (3,4,5,7,15):
        t=stone.copy();t.putalpha(old.crop((i%8*64,i//8*64,i%8*64+64,i//8*64+64)).getchannel('A'));put(out,i,t)
    for spent in (False,True):
        candle=icon(p[5 if spent else 4],42)
        for n in range(6):
            t=floor.copy()
            art=candle if n!=5 else candle.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
            t.alpha_composite(art,(11+(n%2)*2,14));put(out,42+n+(8 if spent else 0),t)
        t=floor.copy();t.alpha_composite(icon(p[5 if spent else 4],50),(7,10));put(out,31 if spent else 27,t)
    # Index 19 is the common walkable paving, even beneath the gate. It must
    # never carry a door fragment throughout either sanctum.
    put(out,19,floor)
    return out

def office():
    p=panels('office');out=Image.new('RGBA',(512,512))
    # A continuous rug, cut into a 3x3 footprint with no per-cell borders.
    carpet=icon(p[2],192)
    for y in range(3):
        for x in range(3):put(out,y*3+x,carpet.crop((x*64,y*64,(x+1)*64,(y+1)*64)))
    desk=icon(p[0],128)
    desk=ImageOps.contain(desk,(128,64),Image.Resampling.LANCZOS)
    tabletop=Image.new('RGBA',(128,64));tabletop.alpha_composite(desk,((128-desk.width)//2,(64-desk.height)//2))
    for x in range(2):put(out,16+x,tabletop.crop((x*64,0,(x+1)*64,64)))
    for i,n in ((18,1),(19,3),(20,4),(21,5)):put(out,i,icon(p[n]))
    return out

# Effect-specific primary / secondary paintings in enum order. No unnamed fallback.
# 0 meal, 1 eye, 2 fury, 3 shield, 4 runes, 5 speed, 6 strength, 7 heart,
# 8 sword, 9 sword+shield, 10 leap, 11 impact, 12 staff, 13 magic, 14 soul,
# 15 portal, 16 cloak, 17 search, 18 ring, 19 hood, 20 arrow, 21 nature,
# 22 sun, 23 light, 24 chain, 25 dual swords, 26 fist, 27 lance, 28 healing,
# 29 forms, 30 rat, 31 time.
TALENTS=[
[(0,7),(1,9),(2,8),(3,7),(0,3),(3,28),(4,9),(5,8),(20,11),(3,9),(6,8),(2,31),(2,7),(2,4),(8,25),(9,3),(25,31),(10,6),(11,10),(10,5),(11,23),(11,8),(11,6),(3,2),(3,6),(25,3)],
[(0,13),(1,12),(13,31),(3,12),(0,12),(4,13),(12,3),(1,13),(3,13),(13,7),(15,14),(12,8),(12,18),(13,18),(14,0),(14,28),(14,19),(11,13),(13,29),(3,11),(13,6),(12,25),(4,31),(15,8),(15,12),(15,5)],
[(0,19),(1,16),(26,8),(16,3),(0,14),(4,16),(17,1),(5,16),(1,19),(18,13),(16,5),(8,19),(8,5),(19,18),(3,5),(20,5),(16,10),(10,16),(19,15),(15,16),(19,30),(3,19),(19,25),(16,8),(16,9),(16,29)],
[(21,0),(1,21),(20,8),(21,28),(0,5),(21,7),(21,5),(1,17),(20,3),(20,26),(20,1),(1,20),(20,4),(20,18),(20,21),(21,3),(28,3),(25,20),(20,15),(20,14),(21,6),(21,2),(21,10),(1,22),(20,22),(14,5)],
[(0,6),(1,8),(8,31),(3,8),(0,26),(5,7),(8,13),(8,5),(25,5),(8,1),(25,8),(25,13),(25,18),(25,2),(26,19),(26,7),(26,13),(10,8),(7,8),(8,25),(27,13),(8,6),(27,1),(19,10),(1,26),(9,26)],
[(0,22),(1,22),(23,13),(3,22),(0,23),(4,22),(23,22),(1,23),(28,22),(28,13),(4,23),(27,22),(21,22),(4,28),(28,7),(22,3),(23,3),(28,15),(22,11),(23,5),(29,6),(29,1),(29,14),(23,27),(7,24),(31,23)]
]

@lru_cache(None)
def talents(size=64):
    parts=panels('talents',8,4);out=Image.new('RGBA',(size*16,size*16));portraits={}
    def composed(primary,secondary,cls):
        tile=icon(parts[primary],size)
        # Secondary painted motif identifies the particular action within a shared family.
        detail=icon(parts[secondary],round(size*26/64))
        tile.alpha_composite(detail,(round(size*38/64),round(size*38/64)))
        # Tiny class crest distinguishes cross-class combinations without plain number badges.
        crest=icon(parts[(9,12,16,21,25,22,30)[cls]],round(size*15/64))
        tile.alpha_composite(crest,(0,0))
        return tile
    for cls,row in enumerate(TALENTS):
        assert len(row)==26
        for offset,pair in enumerate(row):
            i=cls*32+offset;portraits[i]=composed(*pair,cls);out.paste(portraits[i],(i%16*size,i//16*size))
    for cls in range(7):
        i=26+32*cls;portraits[i]=composed(29,31,cls);out.paste(portraits[i],(i%16*size,i//16*size))
    for i,pair in ((215,(30,3)),(216,(30,7)),(217,(30,25))):
        portraits[i]=composed(*pair,6);out.paste(portraits[i],(i%16*size,i//16*size))
    source=(ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java').read_text()
    actual={int(i) for i in re.findall(r'\b[A-Z][A-Z_]+\((\d+)(?:,\s*\d+)?\)',source) if int(i)<224}
    assert actual<=portraits.keys(),('missing talents',actual-portraits.keys())
    from pack import digest
    assert len({digest(p) for p in portraits.values()})==len(portraits),'duplicate painted talent'
    return out,portraits

def journal_consumables(items):
    import json
    contract=json.loads((ROOT/'desktop/src/test/resources/item-semantics.json').read_text())
    result=Image.new('RGBA',(64,64))
    for key,size,pos in (('POTION_CRIMSON',43,(0,4)),('SCROLL_KAUNAN',38,(26,23))):
        # Names in the public semantic contract remain the source of atlas indices.
        key=next((name for name in contract['items'] if key in name),key)
        index=contract['items'][key]['artIndex']
        art=items.crop((index%16*64,index//16*64,index%16*64+64,index//16*64+64))
        result.alpha_composite(icon(art,size),pos)
    return result

def bloodmark():
    # The SAME physical engraving, not a separately redrawn debuff symbol.
    im=Image.open(HERE/'sources/playtest-v129/brand.png').convert('RGBA')
    face=im.crop((round(im.width*.585),round(im.height*.407),round(im.width*.90),round(im.height*.755)))
    return icon(face,64)

def hero_icons(size=64):
    # Subclass/armor selectors and Cleric actions share the corresponding
    # talent's motif, as upstream does, rather than unrelated placeholder glyphs.
    indices={0:11,1:16,2:44,3:47,4:75,5:80,6:107,7:111,8:140,9:143,10:171,11:175,
             16:17,17:22,18:24,19:49,20:52,21:56,22:81,23:84,24:87,
             25:113,26:116,27:119,28:145,29:148,30:151,31:177,32:180,33:183,34:215,
             40:162,41:171,42:163,43:161,44:163,45:165,46:166,47:167,48:168,
             49:169,50:162,51:171,52:172,53:173,54:178,55:174,56:175,57:176,
             58:177,59:178,60:179,61:180,62:181,63:182,64:183,65:184,66:185,
             104:11,105:16,106:75,107:80,108:107,109:140,110:143}
    for i in range(40,67):indices[i+32]=indices[i]
    paint=talents(size)[1];out=Image.new('RGBA',(size*8,size*16))
    for i,talent in indices.items():out.paste(paint[talent],(i%8*size,i//8*size))
    # NONE (127) deliberately remains completely transparent.
    return out,indices

def outputs():
    from bounty import poster_assets
    paper=poster_assets()['interfaces/bounty_parchment.png']
    return {'environment/custom_tiles/halls_special.png':halls(),
            'environment/custom_tiles/wardens_office.png':office(),
            'interfaces/bounty_parchment.png':paper,
            'effects/bloodmark.png':bloodmark(),
            'interfaces/hero_icons.png':hero_icons()[0],
            'interfaces/talent_icons.png':talents()[0]}
