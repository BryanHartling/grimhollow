"""Reproducible hand anchors for separately rendered, existing painted items."""
import json
from hero_pilot import HEROES, displacement

# Composition-space hands, in the 48x60 rig; these are reviewed with each body.
HANDS={'warrior':(35,33),'enchanter':(15.8,35),
       'mage':(34,34),'rogue':(34,33),'huntress':(16,34),'duelist':(34,33),
       'cleric':(34,34),'necromancer':(16,34),'psychic':(34,34)}

def metadata():
    from actors import SCALE
    data={hero:[[round(v/SCALE,4) for v in displacement(hero,pose,*HANDS[hero])]
                for pose in range(21)] for hero in HEROES}
    return (json.dumps(data,indent=2)+'\n').encode('utf-8')

def item_metadata():
    from actors import HERE
    catalog=json.loads((HERE.parents[1]/'desktop/src/test/resources/item-semantics.json').read_text())['items']
    # Reviewed handle locations in the shipping inventory paintings. Some blades
    # point down-left in inventory; rotate those instead of gripping their tips.
    points={name:(.28,.76,0) for name in ('WORN_SHORTSWORD','CUDGEL','DAGGER','SHORTSWORD','HAND_AXE','DIRK','SICKLE','SWORD','MACE','SCIMITAR','BATTLE_AXE','WAR_HAMMER','THROWING_SPIKE','FISHING_SPEAR','THROWING_CLUB','TRIDENT','THROWING_HAMMER')}
    points.update({name:(.78,.22,180) for name in ('RAPIER','LONGSWORD','RUNIC_BLADE','ASSASSINS_BLADE','KATANA','GREATSWORD','THROWING_KNIFE','KUNAI')})
    points.update({name:(.5,.5,0) for name in ('ROUND_SHIELD','GREATSHIELD','SPIRIT_BOW','CROSSBOW','THROWING_STONE','SHURIKEN','FORCE_CUBE','FOCUS_CRYSTAL','FOCUS_RING')})
    points.update(MAGES_STAFF=(.38,.62,0),SPEAR=(.4,.6,0),QUARTERSTAFF=(.45,.55,0),SAI=(.7,.72,0),
                  WHIP=(.75,.65,0),FLAIL=(.77,.27,0),GLAIVE=(.38,.63,0),GREATAXE=(.78,.75,0),
                  WAR_SCYTHE=(.53,.63,0),THROWING_SPEAR=(.4,.6,0),BOLAS=(.5,.4,0),JAVELIN=(.45,.55,0),
                  TOMAHAWK=(.4,.65,0),BOOMERANG=(.35,.7,0),BONE_ROD=(.4,.65,0),RUNED_BATON=(.4,.6,0),
                  BONE_SCYTHE=(.45,.68,0),REAPER_SCYTHE=(.55,.6,0),GRAVE_SCYTHE=(.55,.6,0))
    points.update({name:(.7,.3,180) for name in catalog if name=='DART' or name.endswith('_DART')})
    return (json.dumps({str(catalog[name]['id']):list(point) for name,point in points.items()},indent=2)+'\n').encode('utf-8')
