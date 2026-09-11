"""GPL-3.0-or-later. Deterministic rendered effect strips, with no gameplay state.

Smoke is an analytically advected periodic density field. Geometry and material
phase close over each loop; all sources use the project's fixed lights/camera.
"""
import bpy,math,random
import props,primitives as P,materials

STYLES={'smoke':16,'flame':6,'ember':3,'grass':6,'necrotic':8,'curse':8,
 'inscription':8,'psychic':8,'bone_wall':4,'force_wall':8,'corpse':8,
 'sanctuary':8,'scorch':3,'ripple':8,'green_flame':6}

def density(phase):
    mat=bpy.data.materials.new('advected smoke density');mat.use_nodes=True
    nodes=mat.node_tree.nodes;nodes.clear();links=mat.node_tree.links
    output=nodes.new('ShaderNodeOutputMaterial');pos=nodes.new('ShaderNodeNewGeometry')
    separate=nodes.new('ShaderNodeSeparateXYZ');links.new(pos.outputs['Position'],separate.inputs[0])
    def op(kind,a,b=0):
        n=nodes.new('ShaderNodeMath');n.operation=kind
        for i,v in enumerate((a,b)):
            if isinstance(v,(int,float)):n.inputs[i].default_value=v
            else:links.new(v,n.inputs[i])
        return n.outputs[0]
    x,y=separate.outputs['X'],separate.outputs['Y']
    coords=nodes.new('ShaderNodeCombineXYZ')
    for i,v in enumerate((op('SINE',op('MULTIPLY',x,math.tau)),op('COSINE',op('MULTIPLY',x,math.tau)),op('SINE',op('ADD',op('MULTIPLY',y,math.tau),phase)))):links.new(v,coords.inputs[i])
    noise=nodes.new('ShaderNodeTexNoise');noise.noise_dimensions='4D';noise.inputs['Scale'].default_value=.8;noise.inputs['Detail'].default_value=1.5;noise.inputs['Roughness'].default_value=.3
    links.new(coords.outputs[0],noise.inputs['Vector']);noise.inputs['W'].default_value=2*math.cos(phase)
    # Broad wisps and low-density channels, not a hard tile-shaped tint.
    ramp=nodes.new('ShaderNodeValToRGB');ramp.color_ramp.elements[0].position=.29;ramp.color_ramp.elements[0].color=(0,0,0,1)
    ramp.color_ramp.elements[1].position=.72;ramp.color_ramp.elements[1].color=(.92,.92,.92,1);links.new(noise.outputs['Fac'],ramp.inputs[0])
    glow=nodes.new('ShaderNodeEmission');glow.inputs['Color'].default_value=(*materials.rgb('C9BFA8'),1)
    transparent=nodes.new('ShaderNodeBsdfTransparent');mix=nodes.new('ShaderNodeMixShader')
    links.new(ramp.outputs[0],mix.inputs[0]);links.new(transparent.outputs[0],mix.inputs[1]);links.new(glow.outputs[0],mix.inputs[2]);links.new(mix.outputs[0],output.inputs[0])
    return mat

def render(kind,frame,reset,camera,save,cache):
    if kind=='ripple':return # The approved stage-6a cache is packed without rerendering.
    scene=reset();camera(scene,True);phase=math.tau*frame/STYLES[kind];rng=random.Random(7301)
    if kind=='smoke':
        material=density(phase);root=P.joint('periodic density surface',None,(0,0,0))
        P.mesh('cube','smoke field',root,(0,0,0),(1.5,1.5,.001),material)
    else:
        primary,secondary,accent=('C9BFA8','565B62','7BB33B')
        if kind in ('flame','ember'):primary,secondary,accent='E0982F','5E0D12','E4C76A'
        if kind=='green_flame':primary,secondary,accent='7BB33B','3F6A1F','C9BFA8'
        if kind=='grass':primary,secondary,accent='3F6A1F','5A4630','7BB33B'
        if kind in ('psychic','force_wall'):primary,secondary,accent='9D6BD1','4A2C6E','6FD3E0'
        if kind in ('inscription','sanctuary'):primary,secondary,accent='E4C76A','8A7331','C9BFA8'
        if kind=='scorch':primary,secondary,accent='1A1816','2C2F33','3B3733'
        model=props.Model(dict(name=kind,primary=primary,secondary=secondary,accent=accent,variant=frame%4))
        model.root.location.z=0
        if kind in ('flame','green_flame'):
            bend=.06*math.sin(phase)
            model.poly([(-.13,-.44),(-.23,-.22),(-.21,.03),(-.10,-.02),(-.07,.25),(.07+bend,.47),(.04,.1),(.14,.23),(.20,-.10),(.14,-.39),(0,-.47)],1)
            model.poly([(-.08,-.39),(-.14,-.13),(-.04,.10),(.04+bend,.29),(.04,-.04),(.13,-.11),(.09,-.37),(0,-.44)],0,.14)
            model.poly([(-.04,-.35),(-.05,-.20),(.02,-.03),(.07,-.22),(.04,-.36)],2,.22)
            # A very small hot core; broad white wedges would wash out the flame.
            model.ball(.012,-.31,.018,.027,2,.30)
        elif kind=='ember':
            a=frame*1.4;model.poly([(-.025,-.055),(.025,-.05),(.012,.09),(-.02,.04)],0);model.ball(0,-.015,.018,m=2,z=.13);model.root.rotation_euler.z=a
        elif kind=='grass':
            for i in range(17):
                x=rng.uniform(-.35,.35);y=rng.uniform(-.35,-.15);length=rng.uniform(.35,.73);bend=.09*math.sin(phase+x*5)+rng.uniform(-.08,.08)
                model.poly([(x-.023,y),(x+.024,y),(x+bend+.012,y+length*.65),(x+bend*1.4,y+length),(x+bend-.024,y+length*.45)],i%3,.015+i*.004,.007)
        elif kind=='bone_wall':
            for x in (-.31,0,.31):
                model.rod((x,-.40),(x,.28),.045,0)
                model.skull(x,.32,.12)
            for y in (-.28,.03):model.rod((-.39,y),(.39,y),.035,0,.09)
            for x in (-.31,0,.31):model.ball(x,.35,.025,m=2,z=.2)
        elif kind=='force_wall':
            for x in (-.4,-.13,.13,.4):model.rod((x,-.46),(x,.46),.017,0)
            for y in (-.38,-.12,.14,.40):model.rod((-.43,y),(.43,y),.012,2)
            for x in (-.26,0,.26):model.gem(x,.13*math.sin(phase+x),.06,0)
        elif kind=='scorch':
            rng=random.Random(817+frame)
            for i in range(20):
                a=rng.random()*math.tau;r=rng.random()*.23
                model.ball(math.cos(a)*r,math.sin(a)*r,rng.uniform(.035,.085),m=i%3,z=.015)
        elif kind=='inscription':
            model.ring(0,0,.35,1,thickness=.012);model.rune(0,0,.22,0)
            model.root.rotation_euler.z=phase*.1
        elif kind=='sanctuary':
            for a,b in [((-.47,-.47),(.47,-.47)),((.47,-.47),(.47,.47)),((.47,.47),(-.47,.47)),((-.47,.47),(-.47,-.47))]:model.rod(a,b,.018,0)
            for x,y in [(-.46,0),(.46,0),(0,-.46),(0,.46)]:model.gem(x,y,.04,2)
        elif kind in ('curse','psychic'):
            model.ring(0,0,.36+.025*math.sin(phase),0,thickness=.018)
            for i in range(5):
                a=i*math.tau/5+phase*.12;x,y=.35*math.cos(a),.35*math.sin(a);model.gem(x,y,.045,2)
            if kind=='curse':model.rune(0,0,.18,2)
            else:model.ring(0,0,.16,2,thickness=.012)
        elif kind in ('necrotic','corpse'):
            radius=.08+.37*frame/7 if kind=='corpse' else .25
            for i in range(9 if kind=='corpse' else 4):
                a=i*2.399+phase*.10;x,y=radius*math.cos(a),radius*math.sin(a)
                model.poly([(x-.025,y-.06),(x+.025,y-.05),(x+.008,y+.09)],i%3)
                if kind=='corpse':model.rod((x*.7,y*.7),(x,y),.012,2)
        else:raise ValueError(kind)
    save(cache/'effects'/kind/f'{frame:02d}.png')
