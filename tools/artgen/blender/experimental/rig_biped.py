"""A rigid low-poly joint hierarchy shared by the hero and humanoid minions."""
import bpy,math

from primitives import joint,mesh

def build(kind,tier,m):
    root=joint('root',None,(0,0,0));j={}
    torso='bone' if kind=='skeleton' else 'flesh' if kind=='ghoul' else 'cloth'
    mesh('cone','torso',root,(0,0,.59),(.17,.105,.23),m[torso])
    if kind=='necromancer':
        mesh('cone','draped robe',root,(0,0,.32),(.22,.115,.27),m['cloth'])
        mesh('cube','green stole',root,(0,-.12,.60),(.035,.012,.22),m['mildew'])
        armor=['cloth','cloth','leather','iron','iron','iron','bone','bone'][tier]
        if tier>=2:
            mesh('sphere','cuirass',root,(0,-.045,.64),(.17,.115,.16),m[armor])
            for side in [-1,1]:mesh('sphere','pauldron',root,(side*.19,0,.76),(.09+.008*tier,.12,.07),m[armor])
        if tier>=6:
            mesh('cone','bone crown',root,(0,0,.96),(.15,.09,.07),m['bone'])
    for side,name in [(-1,'L'),(1,'R')]:
        arm=joint('arm'+name,root,(side*.20,0,.73));j['arm'+name]=arm
        mesh('cone','upper arm',arm,(side*.025,0,-.115),(.045,.045,.14),m[torso])
        mesh('sphere','hand',arm,(side*.035,-.015,-.27),(.045,.045,.055),m['bone' if kind!='ghoul' else 'flesh'])
        leg=joint('leg'+name,root,(side*.085,0,.36));j['leg'+name]=leg
        mesh('cone','leg',leg,(0,0,-.145),(.045,.05,.16),m[torso])
        mesh('cube','foot',leg,(0,-.035,-.31),(.055,.10,.035),m['bone' if kind=='skeleton' else 'leather'])
    head=joint('head',root,(0,0,.81));j['head']=head
    mesh('sphere','head',head,(0,0,.07),(.12,.095,.125),m['bone' if kind!='ghoul' else 'flesh'])
    for side in [-1,1]:mesh('cube','eye socket',head,(side*.043,-.087,.09),(.023,.014,.025),m['mortar'])
    if kind=='necromancer':
        mesh('cone','hood',head,(0,.038,.115),(.155,.13,.135),m['cloth'])
        mesh('sphere','hood opening',head,(0,-.085,.05),(.098,.019,.084),m['mortar'])
        mesh('sphere','bone mask',head,(0,-.107,.043),(.067,.021,.063),m['bone'])
        for side in [-1,1]:mesh('cube','green eye',head,(side*.026,-.131,.062),(.013,.008,.009),m['mildew'])
        mesh('cone','bone rod',j['armR'],(.08,-.05,-.18),(.025,.025,.39),m['bone'])
        mesh('sphere','rod skull',j['armR'],(.08,-.05,.22),(.055,.04,.065),m['bone'])
    elif kind=='skeleton':
        for z in [.49,.55,.61,.67]:mesh('cube','rib',root,(0,-.11,z),(.12,.027,.014),m['bone'])
        mesh('cone','rusted blade',j['armR'],(.05,-.06,-.35),(.024,.02,.18),m['iron'])
    else:
        mesh('sphere','hunched back',root,(0,.06,.69),(.20,.15,.17),m['flesh'])
        mesh('cone','ragged wrap',root,(0,0,.35),(.16,.13,.09),m['leather'])
    return root,j

def pose(root,j,animation,phase):
    for obj in j.values():obj.rotation_euler=(0,0,0)
    root.rotation_euler=(0,0,0);root.location=(0,0,0)
    if animation=='idle':root.location.z=.012*math.sin(phase*math.tau)
    elif animation=='run':
        wave=math.sin(phase*math.tau)*.62
        j['armL'].rotation_euler.x=wave;j['armR'].rotation_euler.x=-wave
        j['legL'].rotation_euler.x=-wave;j['legR'].rotation_euler.x=wave;root.location.z=abs(wave)*.028
    elif animation=='attack':
        j['armR'].rotation_euler.x=-1.4*math.sin(phase*math.pi);j['armL'].rotation_euler.x=-.35
        root.rotation_euler.z=.12*math.sin(phase*math.tau)
    elif animation=='die':
        root.rotation_euler.x=-1.4*phase;root.location.z=.03;root.location.y=.23*phase
    else:
        j['armL'].rotation_euler.x=-1.2;j['armR'].rotation_euler.x=-1.2;j['head'].rotation_euler.x=.12*math.sin(phase*math.tau)
