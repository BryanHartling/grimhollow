"""GPL-3.0-or-later. Native character design catalog and atlas compiler.

Only animation indices are read from Java; all geometry is original characters.py
geometry, with materials below. Run after changing this catalog or sprite layouts.
"""
import json, re, math
from pathlib import Path
from build import ROOT, SPEC_DIR

JAVA=ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon'
# path | legacy columns | native size | silhouette | materials | features | animation source
DESIGNS='''
hexcaster|21|48|humanoid|4A2C6E|7BB33B|hood robe crook book|Hexcaster
chainwarden|18|96|humanoid|2E6F7A|E0982F|helm guard tabard flail chain|Chainwarden
rat|16|48|rat|565B62|9E1B24||Rat
brute|21|48|humanoid|8A4B12|9E1B24|gnoll brute axe|Brute
spinner|16|48|spider|3F6A1F|C9BFA8||Spinner
dm300|10|96|machine|8A4B12|E4C76A|crawler|DM300
wraith|9|48|ghost|4A2C6E|9D6BD1||Wraith
undead|21|48|humanoid|6B645C|C9BFA8|skeleton sword|Undead
king|16|96|humanoid|5E0D12|E4C76A|undead crown robe mace|King
piranha|21|48|fish|2E6F7A|9E1B24||Piranha
eye|16|48|eye|4A2C6E|E4C76A||Eye
gnoll|21|48|humanoid|8A4B12|2E6F7A|gnoll axe|Gnoll
crab|16|48|crab|9E1B24|C9BFA8||Crab
goo|12|96|slime|7BB33B|8A7331|toxic|Goo
swarm|16|48|swarm|565B62|8A7331||Swarm
skeleton|21|48|humanoid|6B645C|EFE7D2|skeleton sword|Skeleton
shaman|21|48|humanoid|9E1B24|C9BFA8|gnoll hood robe staff|Shaman
thief|21|48|humanoid|2E6F7A|6FD3E0|hood dagger|Thief
tengu|18|96|humanoid|9E1B24|C9BFA8|hood dagger guard|Tengu
sheep|4|48|sheep|C9BFA8|EFE7D2||Sheep
shopkeeper|2|48|humanoid|8A7331|E4C76A|hat robe|Shopkeeper
bat|8|48|bat|5E0D12|9E1B24||Bat
elemental|42|48|elemental|8A4B12|9E1B24||Elemental
monk|17|48|humanoid|C9BFA8|8A7331|robe|Monk
warlock|21|48|humanoid|4A2C6E|C9BFA8|hood robe staff|Warlock
golem|15|48|golem|565B62|6FD3E0||Golem
statue|21|48|humanoid|565B62|C9BFA8|helm guard sword|Statue
succubus|21|48|humanoid|9E1B24|C9BFA8|wings dagger|Succubus
scorpio|15|48|scorpio|2E6F7A|6FD3E0||Scorpio
yog_fists|10|96|fist|E0982F|9E1B24||Fist
yog|12|96|eye|9E1B24|7BB33B||Yog
larva|10|48|snake|C9BFA8|9E1B24||Larva
ghost|9|48|ghost|565B62|EFE7D2||Ghost
wandmaker|5|48|humanoid|4A2C6E|C9BFA8|hood robe staff|Wandmaker
blacksmith|4|48|humanoid|565B62|E0982F|brute hammer|Blacksmith
imp|5|48|humanoid|9E1B24|E4C76A|wings book|Imp
ratking|8|48|rat|8A7331|E4C76A|crown|RatKing
bee|16|48|bee|8A7331|E4C76A||Bee
mimic|16|48|mimic|5A4630|565B62||Mimic
rot_lasher|10|48|plant|3F6A1F|9E1B24||RotLasher
rot_heart|8|48|plant|5E0D12|7BB33B||RotHeart
guard|21|48|humanoid|565B62|E0982F|helm guard axe|Guard
wards|6|48|tower|4A2C6E|9D6BD1||-
guardian|21|48|humanoid|3B3733|7BB33B|helm guard mace|EarthGuardian
slime|9|48|slime|3F6A1F|7BB33B||Slime
snake|21|48|snake|2E6F7A|C9BFA8||Snake
necromancer|16|48|humanoid|4A2C6E|C9BFA8|hood robe crook|Necromancer
ghoul|21|48|humanoid|3F6A1F|C9BFA8|undead brute|Ghoul
ripper|17|48|humanoid|6B645C|9E1B24|undead wings dagger|Ripper
spawner|16|48|egg|5E0D12|9E1B24||Spawner
dm100|16|48|machine|565B62|6FD3E0||DM100
pylon|3|48|tower|565B62|6FD3E0||Pylon
dm200|12|48|machine|565B62|E4C76A||DM200
lotus|1|48|plant|3F6A1F|9D6BD1||Lotus
ninja_log|5|48|log|5A4630|C9BFA8||SmokeBomb
spirit_hawk|8|48|bird|2E6F7A|6FD3E0||SpiritHawk
sentry|4|48|tower|5E0D12|E0982F||-
crystal_wisp|42|48|elemental|2E6F7A|6FD3E0||CrystalWisp
crystal_guardian|21|48|humanoid|565B62|6FD3E0|helm guard mace|CrystalGuardian
crystal_spire|8|96|tower|565B62|6FD3E0||CrystalSpire
gnoll_guard|21|48|humanoid|565B62|9D6BD1|gnoll guard axe|GnollGuard
gnoll_sapper|21|48|humanoid|8A4B12|7BB33B|gnoll book|GnollSapper
gnoll_geomancer|21|96|humanoid|8A4B12|9D6BD1|gnoll robe staff|GnollGeomancer
fungal_spinner|16|48|spider|3F6A1F|9D6BD1||FungalSpinner
fungal_sentry|7|48|fungus|3F6A1F|E4C76A||FungalSentry
fungal_core|4|96|fungus|4A2C6E|7BB33B||FungalCore
vault_tokens_door|1|48|door|565B62|E4C76A||VaultTokenDoor
vault_mirror|16|48|mirror|8A7331|6FD3E0||VaultMirror
vault_boss_elemental|16|96|elemental|9E1B24|E0982F||VaultBossElemental
minion_skeleton|21|48|humanoid|6B645C|6FD3E0|skeleton sword|Skeleton
minion_ghoul|21|48|humanoid|3F6A1F|6FD3E0|undead brute|Ghoul
'''
# Forms retain the game's established variant offsets, never relabel mechanics.
# start, source, primary, accent, extra feature (blank fields inherit the base).
VARIANTS={
 'rat':[(16,'Albino','C9BFA8','9E1B24',''),(32,'FetidRat','3F6A1F','C9BFA8','plague')],
 'crab':[(16,'HermitCrab','C9BFA8','2E6F7A','shell'),(32,'GreatCrab','565B62','E4C76A','great')],
 'gnoll':[(21,'GnollExile','565B62','E0982F','hood'),(42,'GnollTrickster','8A4B12','9D6BD1','hat')],
 'slime':[(9,'CausticSlime','3F6A1F','E4C76A','toxic')],
 'thief':[(21,'Bandit','5E0D12','E0982F','')],
 'necromancer':[(16,'SpectralNecromancer','2E6F7A','9D6BD1','')],
 'brute':[(21,'Shielded','565B62','E0982F','guard')],
 'scorpio':[(15,'Acidic','3F6A1F','7BB33B','')],
 'piranha':[(21,'PhantomPiranha','4A2C6E','6FD3E0','')],
 'monk':[(17,'Senior','8A7331','9E1B24','')],
 'wraith':[(9,'TormentedSpirit','565B62','9E1B24','gash')],
 'skeleton':[(21,'Skeleton','565B62','EFE7D2','guard')],
 'shaman':[(21,'Shaman','2E6F7A','E4C76A',''),(42,'Shaman','4A2C6E','C9BFA8',''),(63,'Shaman','3F6A1F','C9BFA8','')],
 'elemental':[(14,'Elemental','3F6A1F','7BB33B',''),(28,'Elemental','6FD3E0','565B62',''),(42,'Elemental','8A7331','EFE7D2',''),(56,'Elemental','4A2C6E','9E1B24','')],
 'yog_fists':[(10,'Fist','3F6A1F','7BB33B',''),(20,'Fist','6B645C','7BB33B',''),(30,'Fist','565B62','E0982F',''),(40,'Fist','E4C76A','6FD3E0',''),(50,'Fist','4A2C6E','9D6BD1','')],
 'mimic':[(16,'Mimic','E4C76A','565B62',''),(32,'Mimic','2E6F7A','C9BFA8',''),(48,'Mimic','3B3733','9D6BD1','')],
 'dm300':[(10,'DM300','565B62','9E1B24','gash')],
 'dm100':[(16,'DM100','565B62','9D6BD1','')],
 'dm200':[(12,'DM201','565B62','9E1B24',''),(24,'DM200','565B62','6FD3E0','')],
 'crystal_wisp':[(13,'CrystalWisp','3F6A1F','7BB33B',''),(26,'CrystalWisp','9E1B24','E0982F','')],
 'crystal_guardian':[(21,'CrystalGuardian','565B62','7BB33B',''),(42,'CrystalGuardian','565B62','9E1B24','')],
 'crystal_spire':[(8,'CrystalSpire','3B3733','7BB33B',''),(16,'CrystalSpire','3B3733','9E1B24','')],
 'gnoll_geomancer':[(21,'GnollGeomancer','6B645C','9D6BD1','')],
 'vault_boss_elemental':[(16,'VaultBossElemental','565B62','6FD3E0',''),(32,'VaultBossElemental','565B62','E4C76A',''),(48,'VaultBossElemental','4A2C6E','7BB33B','')],
 'ratking':[(8,'RatKing','6B645C','C9BFA8',''),(16,'RatKing','5E0D12','E4C76A',''),(24,'RatKing','3F6A1F','E4C76A','')],
 'statue':[(i,'Statue','565B62','C9BFA8','') for i in (21,32,43,54,65)],
 'wards':[(i,'-','4A2C6E','9D6BD1','') for i in range(1,6)],
 'sentry':[(1,'-','2E6F7A','6FD3E0',''),(2,'-','8A7331','E4C76A','')],
}
HEROES={
 'warrior':('565B62','9E1B24','helm guard sword tabard'),
 'mage':('4A2C6E','9D6BD1','hood robe staff'),
 'rogue':('2C2F33','C9BFA8','hood cloak dagger'),
 'huntress':('3F6A1F','C9BFA8','cloak bow'),
 'duelist':('2E6F7A','E4C76A','hat rapier cape'),
 'cleric':('C9BFA8','E4C76A','cleric robe mace'),
 'necromancer':('5E0D12','C9BFA8','hood robe scythe book'),
 'enchanter':('4A2C6E','E4C76A','hat robe brush book'),
 'psychic':('2E6F7A','9D6BD1','hood robe psychic'),
}
# Region populations include rare replacements, quest enemies, bosses and shared
# encounter-room enemies. Armour/state changes of one species are one identity.
POPULATIONS={
 'sewers':'rat gnoll crab slime snake swarm goo',
 'prison':'skeleton thief dm100 guard necromancer bat tengu chainwarden rot_lasher rot_heart elemental:14',
 'caves':'hexcaster bat brute shaman spinner dm200 ghoul dm300 pylon crystal_wisp crystal_guardian crystal_spire gnoll_guard gnoll_sapper gnoll_geomancer fungal_spinner fungal_sentry fungal_core',
 'city':'hexcaster ghoul elemental warlock monk golem king succubus vault_boss_elemental vault_mirror skeleton:21 shaman:63 dm100:16 dm200:24',
 'halls':'succubus eye scorpio ripper spawner larva yog yog_fists',
}
SHARED='piranha mimic statue wraith bee'

def regions_for(name,start):
    if (name,start) in (('skeleton',21),('dm100',16),('dm200',24),('shaman',63)):return ['city']
    if (name,start)==('elemental',14):return ['prison']
    if (name,start)==('vault_boss_elemental',48):return [] # Upstream's unused UNSTABLE enum slot.
    result=[]
    for region,population in POPULATIONS.items():
        names=(population+' '+SHARED).split()
        if name in names or name+':'+str(start) in names:result.append(region)
    # Rare Skeleton appears on depth four; its Vault armour does not.
    if name=='skeleton' and start==0:result.append('sewers')
    return result

def animations(source,start,end):
    if source=='-':return {'idle':[start]}
    files=list(JAVA.rglob(source+'Sprite.java')) or list(JAVA.rglob(source+'.java'))
    text=files[0].read_text(encoding='utf-8')
    result={}
    for pose,args in re.findall(r'(\w+)\.frames\(\s*(?:frames|film)\s*,(.*?)\);',text,re.S):
        if pose not in ('idle','run','attack','die','operate','zap','fly','read','charge','summoning'):continue
        ids=[]
        for term in args.split(','):
            term=term.strip();relative=bool(re.search(r'\b(?:c|ofs)\b|texOffset\(\)',term))
            term=re.sub(r'\b(?:c|ofs)\b|texOffset\(\)',str(start),term)
            if not re.fullmatch(r'[\d+\s]+',term):continue
            value=sum(int(v) for v in term.split('+'))
            # Alternate classes with literal absolute indices already name their row.
            if not relative and start and value<start:value+=start
            if start<=value<end:ids.append(value)
        if ids:result[pose]=ids
    return result or {'idle':[start]}

def compile_catalog():
    existing={json.loads(p.read_text())['output']:p for p in SPEC_DIR.glob('*.json')}
    designs=[row.split('|') for row in DESIGNS.strip().splitlines()]
    for hero,(primary,accent,features) in HEROES.items():
        designs.append(['hero_'+hero,'21','48','humanoid',primary,accent,features,'Hero'])
    for name,columns,size,kind,primary,accent,features,source in designs:
        columns=int(columns);fw=int(size);fh=60 if fw==48 else 96
        variants=[(0,source,primary,accent,'')]+VARIANTS.get(name,[])
        hero=name.startswith('hero_')
        # Sufficient complete atlas rows for every referenced upstream frame.
        ends=[v[0] for v in variants[1:]]+[variants[-1][0]+max(columns,21 if hero else 18)]
        if name in ('wards','sentry'):ends[-1]=len(variants)
        if hero:variants=[(21*t,source,primary,accent,'') for t in range(8)];ends=[21*(t+1) for t in range(8)]
        forms=[];poses=[]
        for n,((start,src,p,a,extra),end) in enumerate(zip(variants,ends)):
            form={'name':src if len(variants)==1 else src+'-'+str(start),'kind':kind,'primary':p,'accent':a,'features':(features+' '+extra).split(),'idle':start}
            form['regions']=regions_for(name,start)
            form['species']=name if name in ('statue','dm300','pylon','gnoll_geomancer') else name+':'+str(start)
            if name.startswith('minion_'):form['name']=name
            forms.append(form)
            tier=n if hero or name in ('statue','wards') else (3 if 'guard' in form['features'] else 0)
            anims=animations(src,start,end);form['animations']=anims;form['idle']=anims.get('idle',[start])[0]
            while len(poses)<end:poses.append({'form':n,'tier':tier,'pose':'idle','phase':round((len(poses)%2)*.5,4)})
            # Idle overrides shared recovery frames, preserving normal standing geometry.
            for pose in ('die','attack','zap','operate','read','fly','run','idle'):
                ids=list(dict.fromkeys(anims.get(pose,[])))
                for j,index in enumerate(ids):
                    poses[index]={'form':n,'tier':tier,'pose':pose,'phase':round(j/max(1,len(ids)-(0 if pose in ('idle','run') else 1)),4)}
        dimensions=[1024,512] if hero else [columns*fw,math.ceil(len(poses)/columns)*fh]
        output='core/src/main/assets/sprites/'+name+'.png'
        spec={'kind':'character','native_character':True,'output':output,'dimensions':dimensions,'frame':[fw,fh],
              'seed':603,'references':['ref-25','ref-26','ref-28','ref-29','ref-30'],'forms':forms,'poses':poses}
        (existing.get(output) or SPEC_DIR/('character_'+name+'.json')).write_text(json.dumps(spec,indent=2)+'\n')
    print('Native character specifications:',len(designs))

if __name__=='__main__':compile_catalog()
