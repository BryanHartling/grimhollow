"""Shared articulated limb rig for rat and crab silhouettes."""
import math
from rig_biped import joint,mesh
def build(kind,m):
    root=joint('root',None,(0,0,0));legs=[]
    if kind=='rat':
        mesh('sphere','haunches',root,(0,.02,.23),(.19,.25,.18),m['leather'])
        mesh('sphere','head',root,(0,-.22,.25),(.14,.14,.12),m['leather'])
        mesh('cone','snout',root,(0,-.36,.23),(.08,.11,.06),m['flesh'])
        for side in [-1,1]:
            mesh('sphere','ear',root,(side*.10,-.18,.36),(.06,.035,.08),m['flesh'])
            mesh('sphere','eye',root,(side*.095,-.31,.29),(.021,.016,.021),m['bone'])
        for side in [-1,1]:
            for y in [-.13,.16]:
                leg=joint('leg',root,(side*.15,y,.18));legs.append(leg);mesh('cone','leg',leg,(side*.06,0,-.07),(.055,.055,.10),m['leather'])
        for i in range(5):mesh('sphere','tail',root,(.06*math.sin(i*.55),.25+i*.065,.07),(.024,.055,.024),m['flesh'])
    else:
        root.scale.x=.86
        mesh('sphere','carapace',root,(0,0,.23),(.27,.20,.16),m['blood'])
        for side in [-1,1]:
            for y in [-.10,.02,.13]:
                leg=joint('leg',root,(side*.22,y,.18));legs.append(leg)
                mesh('cone','leg',leg,(side*.12,.035,-.065),(.15,.032,.032),m['blood'])
            mesh('cone','eye stalk',root,(side*.10,-.17,.35),(.025,.025,.09),m['blood'])
            mesh('sphere','eye',root,(side*.10,-.18,.44),(.027,.027,.027),m['mortar'])
            claw=joint('claw',root,(side*.21,-.17,.24));legs.append(claw)
            mesh('sphere','pincer',claw,(side*.10,-.13,.025),(.11,.13,.07),m['blood'])
            mesh('cube','pincer cleft',claw,(side*.10,-.22,.05),(.018,.08,.012),m['mortar'])
    return root,legs
def pose(root,legs,animation,phase):
    root.rotation_euler=(0,0,0);root.location=(0,0,0)
    for i,leg in enumerate(legs):leg.rotation_euler.z=.16*math.sin(phase*math.tau+i*math.pi) if animation=='run' else 0
    if animation=='attack':root.location.y=-.12*math.sin(phase*math.pi)
    elif animation=='die':root.rotation_euler.y=1.1*phase;root.location.z=-.05*phase
    elif animation=='special':root.location.z=.04*math.sin(phase*math.pi)
