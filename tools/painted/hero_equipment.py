"""Reproducible hand anchors for separately rendered, existing painted items."""
import json
from hero_pilot import HEROES, displacement

# Composition-space hands, in the 48x60 rig; these are reviewed with each body.
# Palm centres reviewed against all eight shipping armor rows, not the older
# part rig. In particular the Necromancer's hand is inward of the robe edge.
HANDS={'warrior':(35,31.5),'enchanter':(15,31),
       'mage':(33.5,32.5),'rogue':(35.5,32),'huntress':(16,32),'duelist':(34,31.5),
       'cleric':(34,31.5),'necromancer':(19,32.5),'psychic':(33,32.5)}

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
    # Endpoint replaces the old rotation guess; both describe the inventory painting.
    points={name:(x,y,.18,.85) if angle==180 else (x,y,.82,.15)
            for name,(x,y,angle) in points.items()}
    points.update(BONE_ROD=(.38,.69,.76,.18),SPEAR=(.4,.62,.86,.13),
                  SHORTSWORD=(.34,.7,.89,.16),WORN_SHORTSWORD=(.34,.7,.89,.16),
                  MAGES_STAFF=(.34,.69,.75,.13),FLAIL=(.77,.27,.28,.75),
                  GREATAXE=(.78,.75,.3,.25),SAI=(.7,.72,.35,.15),
                  WHIP=(.75,.65,.25,.25),RUNED_BATON=(.35,.67,.83,.15))
    # Curved blades use the handle's centreline at the blade collar, rather
    # than a generic diagonal pointing into empty space or the curved tip.
    points.update(SICKLE=(.33,.72,.49,.53),WAR_SCYTHE=(.53,.63,.75,.18),
                  BONE_SCYTHE=(.453125,.6875,.74,.34),REAPER_SCYTHE=(.53125,.59375,.72,.3),
                  GRAVE_SCYTHE=(.5,.5625,.85,.14),FLAIL=(.8125,.609375,.9,.35),
                  WAR_HAMMER=(.3125,.65625,.72,.35),THROWING_CLUB=(.296875,.671875,.7,.32),
                  THROWING_KNIFE=(.64,.36,.28,.8),KUNAI=(.6,.39,.24,.85),
                  GREATSWORD=(.75,.25,.18,.85),TOMAHAWK=(.390625,.5625,.67,.21),
                  MAGES_STAFF=(.34375,.703125,.75,.13),BONE_ROD=(.375,.671875,.76,.18),
                  SPIRIT_BOW=(.421875,.46875,.72,.18),SHURIKEN=(.546875,.484375,.55,.15),
                  BOLAS=(.484375,.375,.5,.15),SAI=(.8,.64,.23,.1),BOOMERANG=(.43,.45,.82,.14))
    return (json.dumps({str(catalog[name]['id']):list(point) for name,point in points.items()},indent=2)+'\n').encode('utf-8')
