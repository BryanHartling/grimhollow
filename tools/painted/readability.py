"""Offline composition of authored medals, lock and particle sprites."""
import re
from PIL import Image, ImageOps
from pack import HERE, ROOT, put

MOTIFS={
    'UNLOCK_MAGE':0,'UNLOCK_ROGUE':1,'UNLOCK_HUNTRESS':2,'UNLOCK_DUELIST':3,'UNLOCK_CLERIC':4,
    'MONSTERS_SLAIN':5,'GOLD_COLLECTED':6,'ITEM_LEVEL':7,'LEVEL_REACHED':8,
    'STRENGTH_ATTAINED':9,'FOOD_EATEN':10,'ITEMS_CRAFTED':11,'BOSS_SLAIN_1':12,
    'CATALOG_ONE_EQUIPMENT':13,'DEATH_FROM_FIRE':14,'DEATH_FROM_POISON':15,
    'DEATH_FROM_GAS':16,'DEATH_FROM_HUNGER':17,'DEATH_FROM_FALLING':18,
    'RESEARCHER':19,'GAMES_PLAYED':20,'HIGH_SCORE':21,'NO_MONSTERS_SLAIN':22,
    'BOSS_SLAIN_REMAINS':23,'BOSS_SLAIN_2':24,'BOSS_SLAIN_3':25,'CATALOG_POTIONS_SCROLLS':26,
    'DEATH_FROM_ENEMY_MAGIC':27,'DEATH_FROM_FRIENDLY_MAGIC':28,'DEATH_FROM_SACRIFICE':29,
    'BOSS_SLAIN_1_ALL_CLASSES':30,'ENEMY_HAZARDS':31,'PIRANHAS':32,'GRIM_WEAPON':33,
    'ALL_BAGS_BOUGHT':34,'MASTERY_COMBO':35,'BOSS_SLAIN_4':36,'ALL_RARE_ENEMIES':37,
    'DEATH_FROM_GRIM_TRAP':31,'VICTORY':38,'BOSS_CHALLENGE':41,'MANY_BUFFS':40,
    'HAPPY_END':39,'VICTORY_RANDOM':38,'HAPPY_END_REMAINS':23,'RODNEY':37,
    'VICTORY_ALL_CLASSES':30,'DEATH_FROM_ALL':5,'BOSS_SLAIN_3_ALL_SUBCLASSES':36,
    'CHAMPION':41,'PACIFIST_ASCENT':22,'TAKING_THE_MICK':21,
}

def cutouts(name,cols,rows):
    sheet=Image.open(HERE/'sources/readability'/name).convert('RGBA')
    assert sheet.getchannel('A').getextrema()[0]==0
    result=[]
    for i in range(cols*rows):
        x,y=i%cols,i//cols
        part=sheet.crop((round(x*sheet.width/cols),round(y*sheet.height/rows),
                         round((x+1)*sheet.width/cols),round((y+1)*sheet.height/rows)))
        box=part.getchannel('A').point(lambda a:255 if a>=12 else 0).getbbox()
        assert box,(name,i)
        result.append(part.crop(box))
    return result

def centered(canvas,part,size,center=(32,32)):
    part=ImageOps.contain(part,size,Image.Resampling.LANCZOS)
    canvas.alpha_composite(part,(round(center[0]-part.width/2),round(center[1]-part.height/2)))

def outputs():
    motifs=cutouts('badges.png',6,7);details=cutouts('details.png',4,2)
    badges=Image.new('RGBA',(1024,512));fx=Image.new('RGBA',(512,64))
    java=(ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Badges.java').read_text()
    entries=re.findall(r'^\s*(\w+)\s*\(\s*(\d+)\s*[,)]',java,re.M)
    hashes={}
    for name,index in entries:
        index=int(index)
        key=name if name in MOTIFS else re.sub(r'_\d+$','',name)
        assert key in MOTIFS,(name,'unmapped badge')
        tier=0 if index<32 else 1 if index<64 else 2 if index<96 else 3 if index<120 else 4
        tile=Image.new('RGBA',(64,64));centered(tile,details[tier],(62,62))
        centered(tile,motifs[MOTIFS[key]],(37,35),(32,30))
        if name=='DEATH_FROM_GRIM_TRAP':centered(tile,motifs[5],(15,15),(42,42))
        match=re.search(r'_(\d+)$',name)
        # Tiers within the same metal also retain a readable count of authored gems.
        if match:
            count=int(match[1])
            for n in range(count):centered(tile,motifs[40],(5,5),(32+(n-(count-1)/2)*6,49))
        put(badges,index,tile)
        hashes.setdefault(tile.tobytes(),[]).append(name)
    assert len(entries)==98
    assert len(hashes)==len(entries),[names for names in hashes.values() if len(names)>1]
    for i,part in enumerate(details):
        tile=Image.new('RGBA',(64,64));centered(tile,part,(60,60))
        fx.alpha_composite(tile,(i*64,0))
    return {'interfaces/painted_badges.png':badges,'effects/readability.png':fx}
