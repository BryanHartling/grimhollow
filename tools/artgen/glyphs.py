"""GPL-3.0-or-later. Native 32px semantic UI pictograms, without text or source pixels."""
import re,hashlib
from PIL import Image,ImageDraw,ImageFilter
from build import ROOT

RULES=[('heart','HEAL HEART VITAL VIGOR RESTORE DIVINE SOUL BLOOD'),('meal','MEAL FOOD RATION EATER STOMACH'),
 ('eye','VISION SIGHT INTUITION SEARCH FORESIGHT ACCURACY FOCUS AWARENESS PRECOGNITION'),
 ('shield','SHIELD BARRIER DEFENSE ARMOR DEFENCE GUARD ENDURE DURABILITY WILL HOLD PROTECT RESIST'),
 ('book','SCHOLAR INSCRIBE SCROLL RUNE RUNIC LORE TRANSFER SCRIVENER WRITING ETCH'),
 ('skull','DEATH NECRO BONE MINION CORPSE REAPER CURSE HEX WRAITH REVENANT'),
 ('boot','SPEED HASTE STEP LEAP JUMP MOMENTUM SWIFT STAMINA SPRINT RETREAT EVASIVE'),
 ('leaf','NATURE SEED GRASS ROOT GROWTH REJUVENAT HARVEST DEW BOUNTY'),
 ('flame','FIRE BURN RAGE FURY ANGER FLAME EXPLOS BLAST'),
 ('star','MAGIC ENERGY POWER CHARGE ENERGY ARCANE ARCANA CATALYST ELEMENT ENCHANT ENERGIZ'),
 ('arrow','SHOT ARROW PROJECTILE RANGE REACH MISSILE MARK STRIKE SLAM FORCE IMPACT'),
 ('veil','SHADOW STEALTH INVIS CLOAK SILENT SMOKE FOG'),('ring','RING TELE WARP DOMINAT MIND PSYCHIC BEACON SUMMON'),
 ('sword','LETHAL CLEAVE PUNCH DUEL BLADE COMBO ATTACK WEAPON STRONG STRENGTH STRIKING'),
 ('snow','FROST FREEZ ICE'),('drop','LIQUID POTION GAS PURITY CLEANSE TOXIC CORRO PARA'),
 ('chain','ALLY FRIEND CHAIN LINK BOND SUPPORT TEAM COOP'),('sun','LIGHT HOLY BLESS SANCT ANGEL PRIEST'),
 ('mirror','COPY CLONE REPLACE REFLECT'),('key','KEY LOCK OPEN'),('gem','CRYSTAL WAND WILD EXCESS LUCK FORTUNE')]

def semantic(name):
    matches=[shape for shape,words in RULES if any(word in name for word in words.split())]
    return matches[0] if matches else 'star',matches[1] if len(matches)>1 else None

def symbol(kind,color='#C9BFA8'):
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    def line(p,w=3):d.line(p,fill=color,width=w,joint='curve')
    def poly(p):d.polygon(p,fill=color)
    def oval(box,w=3):d.ellipse(box,outline=color,width=w)
    if kind=='heart':
        d.ellipse((5,7,17,19),fill=color);d.ellipse((15,7,27,19),fill=color);poly([(6,14),(26,14),(16,27)])
    elif kind=='meal':oval((4,8,27,27));line([(7,4),(7,13)]);line([(24,4),(24,13)]);poly([(11,14),(22,12),(21,23),(12,24)])
    elif kind=='eye':line([(3,16),(9,9),(16,7),(24,11),(29,16),(24,21),(16,25),(8,22),(3,16)]);oval((12,11,21,22),4)
    elif kind=='shield':line([(5,6),(16,3),(27,6),(25,20),(16,29),(7,20),(5,6)]);line([(16,7),(16,23)]);line([(10,13),(22,13)])
    elif kind=='book':line([(16,7),(8,4),(3,5),(3,24),(9,24),(16,28),(23,24),(29,24),(29,5),(24,4),(16,7),(16,28)],2);line([(7,11),(12,13)],2);line([(20,13),(25,11)],2)
    elif kind=='skull':
        d.ellipse((5,3,27,24),fill=color);d.rectangle((10,18,22,27),fill=color)
        for x in (9,19):d.ellipse((x,11,x+5,17),fill='#0E0D0C')
        d.polygon([(16,17),(13,22),(19,22)],fill='#0E0D0C')
    elif kind=='boot':poly([(11,4),(23,4),(22,20),(28,24),(26,28),(6,28),(5,21),(12,18)]);line([(3,9),(8,9)],2);line([(2,15),(7,15)],2)
    elif kind=='leaf':poly([(5,24),(6,12),(15,5),(28,4),(25,18),(15,25)]);d.line([(4,28),(23,9)],fill='#0E0D0C',width=2)
    elif kind=='flame':poly([(6,25),(3,17),(9,20),(12,8),(18,3),(17,15),(23,10),(27,19),(25,27),(16,29)]);d.polygon([(13,25),(16,17),(21,24),(18,27)],fill='#0E0D0C')
    elif kind in ('star','gem'):poly([(16,2),(20,11),(29,16),(20,20),(16,30),(11,20),(2,16),(11,11)]);oval((12,12,20,20),2)
    elif kind=='arrow':line([(4,27),(25,6)],4);poly([(15,4),(28,3),(27,16)]);line([(4,20),(10,27)],2)
    elif kind=='veil':poly([(5,28),(8,10),(16,3),(24,10),(28,28),(19,23),(12,25)]);d.polygon([(12,13),(21,13),(22,19),(10,19)],fill='#0E0D0C')
    elif kind=='ring':oval((5,7,26,27));oval((11,13,20,22),2);line([(16,2),(16,6)],2);line([(28,7),(25,10)],2)
    elif kind=='sword':poly([(12,20),(18,20),(22,4),(19,2),(12,13)]);line([(7,18),(22,23)]);line([(12,20),(8,28)],4)
    elif kind=='snow':
        for p in [((16,3),(16,29)),((4,9),(28,23)),((4,23),(28,9))]:line(p,2)
        for x,y in [(16,7),(8,12),(8,21),(16,25),(24,21),(24,12)]:oval((x-2,y-2,x+2,y+2),2)
    elif kind=='drop':poly([(16,3),(25,17),(27,24),(20,29),(11,29),(5,23),(8,16)]);d.line([(12,17),(10,23),(13,25)],fill='#0E0D0C',width=2)
    elif kind=='chain':oval((3,5,17,22));oval((14,11,28,28));line([(12,17),(21,16)],2)
    elif kind=='sun':
        oval((9,9,23,23),4)
        for p in [((16,2),(16,6)),((16,26),(16,30)),((2,16),(6,16)),((26,16),(30,16)),((5,5),(8,8)),((24,24),(27,27))]:line(p,2)
    elif kind=='mirror':line([(8,4),(24,4),(27,26),(5,26),(8,4)]);line([(11,21),(22,9)],2);line([(11,15),(17,9)],2)
    elif kind=='key':oval((5,3,19,16));line([(12,16),(12,28)],4);line([(12,24),(24,24)]);line([(23,20),(23,27)])
    return im

def icon(name,index,identification=False):
    primary,modifier=semantic(name);accent=['#C9BFA8','#6FD3E0','#9D6BD1','#7BB33B','#E4C76A','#E0982F','#7BB33B','#E4C76A','#9D6BD1'][min(index//32,8)]
    image=symbol(primary,accent)
    if modifier and not identification:
        small=symbol(modifier,'#EFE7D2').resize((12,12),Image.Resampling.NEAREST);image.alpha_composite(small,(19,19))
    edge=Image.new('RGBA',(32,32),'#0E0D0C');edge.putalpha(image.getchannel('A').filter(ImageFilter.MaxFilter(5)));edge.alpha_composite(image)
    return edge

def entries(kind):
    if kind=='identification':
        import items
        return items.catalog()['identifications']
    text=(ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java').read_text()
    result={name:int(index) for name,index in re.findall(r'\b([A-Z][A-Z_0-9]+)\(\s*(\d+)(?:\s*[,)]).*?',text.split('public static final int')[0])}
    dynamic=text.split('public int icon(){',1)[1].split('public int maxPoints',1)[0]
    for index in re.findall(r'return\s+(\d+)\s*;',dynamic):result['HEROIC_ENERGY_'+index]=int(index)
    return result

def atlas(spec):
    image=Image.new('RGBA',spec['dimensions']);kind=spec['native_glyphs']
    for name,index in entries(kind).items():image.alpha_composite(icon(name,index,kind=='identification'),(index%16*32,index//16*32))
    return image
