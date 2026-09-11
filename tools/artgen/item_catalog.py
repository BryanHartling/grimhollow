"""GPL-3.0-or-later. Item identities and material/model choices for Blender.

Java remains authoritative for atlas indices. This compiler reads constants, never
upstream item pixels. Unknown/unallocated slots stay transparent.
"""
import ast,json,re,hashlib
from pathlib import Path
from build import ROOT

SOURCE=ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java'
HERE=Path(__file__).resolve().parent
SWATCHES=['9E1B24','E0982F','E4C76A','7BB33B','2E6F7A','6FD3E0','4A2C6E','9D6BD1','5A4630','2C2F33','565B62','C9BFA8']

def constants(text):
    values={'SIZE':32,'TX_WIDTH':512,'TX_HEIGHT':1056,'WIDTH':16};public={}
    def number(expression):
        expression=re.sub(r'xy\(\s*(\d+)\s*,\s*(\d+)\s*\)',lambda m:str(int(m[1])-1+16*(int(m[2])-1)),expression)
        def visit(node):
            if isinstance(node,ast.Constant) and isinstance(node.value,int):return node.value
            if isinstance(node,ast.Name):return values[node.id]
            if isinstance(node,ast.UnaryOp) and isinstance(node.op,ast.USub):return -visit(node.operand)
            if isinstance(node,ast.BinOp):
                a,b=visit(node.left),visit(node.right)
                if isinstance(node.op,ast.Add):return a+b
                if isinstance(node.op,ast.Sub):return a-b
                if isinstance(node.op,ast.Mult):return a*b
                if isinstance(node.op,ast.Div):return a//b
            raise ValueError('Unsupported constant expression '+expression)
        return visit(ast.parse(expression.strip(),mode='eval').body)
    for access,block in re.findall(r'(public|private)\s+static\s+final\s+int\s+([^;]+);',text):
        for name,expression in re.findall(r'(\w+)\s*=\s*(xy\([^)]*\)|[^,]+)(?:,|$)',block):
            if name in ('SIZE','TX_WIDTH','TX_HEIGHT','WIDTH'):continue
            value=number(expression);values[name]=value
            if access=='public' and value>=0:public[name]=value
    return public


def classify(name):
    n=name.upper()
    exact={
        'BONE_ROD':'skull_staff','PHYLACTERY':'phylactery','SIGIL_BRUSH':'brush','RUNED_BATON':'wand',
        'FOCUS_CRYSTAL':'crystal','FOCUS_RING':'ring','RUNE_ETCHING':'tablet',
        'BONES':'bones','REMAINS':'bones','TOMB':'tomb','GRAVE':'tomb','GEO_BOULDER':'stone',
        'GOLD':'coins','ENERGY':'crystal','DEWDROP':'drop','PETAL':'leaf','SANDBAG':'bag',
        'ANKH':'ankh','STYLUS':'wand','SEAL':'seal','TORCH':'torch','BEACON':'lantern',
        'HONEYPOT':'pot','SHATTPOT':'pot','MASK':'mask','CROWN':'crown','AMULET':'amulet',
        'MASTERY':'book','KIT':'toolkit','SEAL_SHARD':'shard','BROKEN_STAFF':'wand',
        'CLOAK_SCRAP':'cloak','BOW_FRAGMENT':'bow','BROKEN_HILT':'sword','TORN_PAGE':'scroll',
        'TRINKET_CATA':'crystal','ROUND_SHIELD':'shield','GREATSHIELD':'shield','GLOVES':'glove','GAUNTLETS':'glove',
        'CUDGEL':'club','QUARTERSTAFF':'staff','SAI':'trident','WHIP':'whip','FLAIL':'flail',
        'CROSSBOW':'crossbow','FORCE_CUBE':'cube','THROWING_STONE':'stone','SHURIKEN':'shuriken',
        'BOLAS':'bolas','BOOMERANG':'boomerang','MAGES_STAFF':'staff','SPIRIT_ARROW':'arrow',
        'RAT_SKULL':'skull','PARCHMENT_SCRAP':'scroll','PETRIFIED_SEED':'seed','EXOTIC_CRYSTALS':'crystal',
        'MOSSY_CLUMP':'moss','SUNDIAL':'sundial','CLOVER':'leaf','TRAP_MECHANISM':'trap',
        'MIMIC_TOOTH':'fang','WONDROUS_RESIN':'resin','EYE_OF_NEWT':'eye','SALT_CUBE':'cube',
        'BLOOD_VIAL':'bottle','OBLIVION_SHARD':'shard','CHAOTIC_CENSER':'censer','FERRET_TUFT':'tuft','SPYGLASS':'spyglass',
        'ARCANE_RESIN':'resin','LIQUID_METAL':'pot','MEAT':'meat','STEAK':'meat','STEWED':'pot',
        'OVERPRICED':'ration','CARPACCIO':'meat','RATION':'ration','PASTY':'pie','MEAT_PIE':'pie',
        'BLANDFRUIT':'fruit','BLAND_CHUNKS':'fruit','BERRY':'fruit','PHANTOM_MEAT':'meat',
        'SUPPLY_RATION':'ration','STEAMED_FISH':'fish','FISH_LEFTOVER':'fish',
        'CHOC_AMULET':'amulet','EASTER_EGG':'egg','RAINBOW_POTION':'bottle','SHATTERED_CAKE':'cake',
        'PUMPKIN_PIE':'pie','VANILLA_CAKE':'cake','CANDY_CANE':'cane','SPARKLING_POTION':'bottle',
        'DUST':'dust','CANDLE':'candle','EMBER':'ember','PICKAXE':'pickaxe','ORE':'ore','TOKEN':'coins',
        'BLOB':'drop','SHARD':'shard','ESCAPE':'amulet','STATUE':'statue','WATERSKIN':'bag',
        'BACKPACK':'bag','POUCH':'bag','HOLDER':'quiver','BANDOLIER':'bandolier','HOLSTER':'quiver','VIAL':'bottle',
        'ARTIFACT_CLOAK':'cloak','ARTIFACT_ARMBAND':'bracelet','ARTIFACT_CAPE':'cloak',
        'ARTIFACT_TALISMAN':'amulet','ARTIFACT_HOURGLASS':'hourglass','ARTIFACT_TOOLKIT':'toolkit',
        'ARTIFACT_SPELLBOOK':'book','ARTIFACT_BEACON':'lantern','ARTIFACT_CHAINS':'chain',
        'ARTIFACT_TOME':'book','ARTIFACT_KEY':'key',
        'WEAPON_HOLDER':'sword','ARMOR_HOLDER':'armor','MISSILE_HOLDER':'arrow','WAND_HOLDER':'wand',
        'RING_HOLDER':'ring','ARTIFACT_HOLDER':'amulet','TRINKET_HOLDER':'crystal','FOOD_HOLDER':'ration',
        'BOMB_HOLDER':'bomb','POTION_HOLDER':'bottle','SEED_HOLDER':'seed','SCROLL_HOLDER':'scroll',
        'STONE_HOLDER':'stone','ELIXIR_HOLDER':'bottle','SPELL_HOLDER':'tablet','MOB_HOLDER':'skull','DOCUMENT_HOLDER':'scroll',
        'SOMETHING':'chest',
    }
    if n in exact:return exact[n]
    if n.startswith('ARTIFACT_HORN'):return 'horn'
    if n.startswith('ARTIFACT_CHALICE'):return 'chalice'
    if n.startswith('ARTIFACT_ROSE'):return 'rose'
    if n in ('ARTIFACT_SANDALS','ARTIFACT_SHOES','ARTIFACT_BOOTS','ARTIFACT_GREAVES'):return 'boot'
    if n.startswith('ARMOR_'):return 'armor'
    if n.startswith('WAND_'):return 'wand'
    if n.startswith('RING_'):return 'ring'
    if n.startswith('SEED_'):return 'seed'
    if n.startswith('STONE_'):return 'runestone'
    if n.endswith('_PAGE'):return 'scroll'
    if n.startswith('SCROLL_') or n.startswith('EXOTIC_') and any(word in n for word in ('KAUNAN','SOWILO','LAGUZ','YNGVI','GYFU','RAIDO','ISAZ','MANNAZ','NAUDIZ','BERKANAN','ODAL','TIWAZ')):return 'scroll'
    if n.startswith(('POTION_','EXOTIC_','BREW_','ELIXIR_')):return 'bottle'
    if 'CHEST' in n:return 'chest'
    if 'KEY' in n:return 'key'
    if 'BOMB' in n or n in ('FLASHBANG','NOISEMAKER','TENGU_SHOCKER'):return 'bomb'
    if 'SCYTHE' in n or 'SICKLE' in n:return 'scythe'
    if 'DART' in n or 'SPIKE' in n:return 'arrow'
    if 'KNIFE' in n or n in ('DAGGER','DIRK','KUNAI'):return 'dagger'
    if 'AXE' in n or n=='TOMAHAWK':return 'axe'
    if 'HAMMER' in n:return 'hammer'
    if 'CLUB' in n:return 'club'
    if 'SPEAR' in n or n in ('GLAIVE','JAVELIN'):return 'spear'
    if 'BOW' in n:return 'bow'
    if n=='TRIDENT':return 'trident'
    if n=='MACE':return 'mace'
    if any(word in n for word in ('SWORD','BLADE','RAPIER','KATANA','SCIMITAR')):return 'sword'
    if n in ('WILD_ENERGY','PHASE_SHIFT','TELE_GRAB','UNSTABLE_SPELL','CURSE_INFUSE','MAGIC_INFUSE','ALCHEMIZE','RECYCLE','RECLAIM_TRAP','RETURN_BEACON') or n.startswith('SUMMON_ELE'):return 'tablet'
    raise ValueError('No item silhouette assigned: '+name)


def colors(name,kind,seed):
    primary,secondary,accent='565B62','5A4630',SWATCHES[seed%len(SWATCHES)]
    if kind in ('scroll','skull','bones','phylactery','skull_staff','fang'):primary='C9BFA8';accent='7BB33B'
    if kind in ('bottle','crystal','drop','seed','leaf','moss','resin','shard','eye'):primary=accent;secondary='565B62'
    if kind in ('ring','bracelet','crown','amulet','ankh','coins','seal','chalice','hourglass','key','censer','sundial'):primary='8A7331';secondary='C9BFA8'
    if kind in ('bag','boot','quiver','bandolier','club','staff','bow','toolkit','chest'):primary='5A4630';secondary='565B62'
    if kind in ('meat','fish'):primary='9E1B24';secondary='C9BFA8';accent='5A4630'
    if kind in ('pie','cake','ration','egg','fruit'):primary='C9BFA8';secondary='8A4B12';accent='9E1B24'
    if 'BONE' in name or 'NECRO' in name or name=='PHYLACTERY':primary='C9BFA8';accent='7BB33B'
    if 'FOCUS' in name or 'PSYCHIC' in name:accent='9D6BD1'
    if 'ENCHANTER' in name or name in ('SIGIL_BRUSH','RUNED_BATON','RUNE_ETCHING'):accent='E4C76A'
    for key,color in [('CRIMSON','9E1B24'),('AMBER','E0982F'),('GOLDEN','E4C76A'),('JADE','7BB33B'),('TURQUOISE','2E6F7A'),('AZURE','6FD3E0'),('INDIGO','4A2C6E'),('MAGENTA','9D6BD1'),('BISTRE','5A4630'),('CHARCOAL','2C2F33'),('SILVER','565B62'),('IVORY','C9BFA8')]:
        if key in name:primary=color
    if kind=='armor':
        primary='4A3B2A' if 'LEATHER' in name or 'CLOTH' in name else 'C9BFA8' if 'NECRO' in name else '565B62'
    if accent in (primary,secondary):accent='E4C76A' if primary!='E4C76A' else '9D6BD1'
    gems={'GARNET':'5E0D12','RUBY':'9E1B24','TOPAZ':'E0982F','EMERALD':'7BB33B','ONYX':'2C2F33','OPAL':'C9BFA8','TOURMALINE':'9D6BD1','SAPPHIRE':'2E6F7A','AMETHYST':'4A2C6E','QUARTZ':'EFE7D2','AGATE':'8A4B12','DIAMOND':'6FD3E0'}
    if name.startswith('RING_') and name[5:] in gems:accent=gems[name[5:]]
    if name=='IRON_KEY':primary='565B62'
    if name in ('CRYSTAL_CHEST','CRYSTAL_KEY'):primary='6FD3E0';accent='EFE7D2'
    if name=='EBONY_CHEST':primary='1A1816';secondary='8A7331'
    return primary,secondary,accent


def compile_catalog():
    text=SOURCE.read_text();main,icons=text.split('public static class Icons',1)
    public=constants(main);identifications=constants(icons);slots={}
    for name,index in public.items():slots.setdefault(index,[]).append(name)
    entries=[]
    for index,names in sorted(slots.items()):
        name=names[0];kind=classify(name);seed=int(hashlib.sha256(name.encode()).hexdigest()[:8],16)
        primary,secondary,accent=colors(name,kind,seed)
        entries.append(dict(index=index,name=name,aliases=names,kind=kind,seed=seed,primary=primary,secondary=secondary,accent=accent,variant=seed%4))
    result=dict(dimensions=[512,1056],frame=[32,32],items=entries,identifications=identifications)
    (HERE/'blender/items.json').write_text(json.dumps(result,indent=2)+'\n')
    print('Item models:',len(entries),'public indices:',len(public),'identification glyphs:',len(identifications))

if __name__=='__main__':compile_catalog()
