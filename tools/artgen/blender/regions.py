"""SPDX-License-Identifier: GPL-3.0-or-later. Region-specific environment models."""
import json,math,random
from pathlib import Path
import bpy
import materials,sewers

PARAMS=Path(__file__).parent/'params'

def parameters(region,kind):
    return json.loads((PARAMS/region/(('door' if kind=='door_open' else kind)+'.json')).read_text())

def prison(kind,variant,p,m):
    if kind in ('floor','wall'):
        sewers.geometry(kind,variant,p,m);return
    rng=random.Random(p['seed']+variant)
    # Props retain this first authored geometry throughout their material rounds.
    for ty in (-1,0,1):
        for tx in (-1,0,1):
            if kind.startswith('door'):
                sewers.box('recessed iron doorway',(tx,ty,.025),(.86,.94,.045),m['mortar'],0)
                for side in (-1,1):
                    for row in range(3):sewers.slab('dressed jamb',tx+side*.40,ty+(row-1)*.30,.16,.285,0,.23,m['stone'],rng,.004,.014)
                sewers.box('massive lintel',(tx,ty+.40,.23),(.92,.12,.17),m['stone'],.018)
                for i in range(5):
                    x=tx+(i-2)*.12;y=ty
                    size=(.035,.73,.035)
                    if kind=='door_open':x=tx-.31;y=ty+(i-2)*.10;size=(.035,.085,.035)
                    sewers.box('iron grille',(x,y,.18),size,m['iron_glint'],.006)
                for y in (-.25,.22):sewers.box('grille crossbar',(tx,ty+y,.16),(.64,.045,.05),m['iron'],.005)
                sewers.box('lock housing',(tx+.21,ty-.07,.22),(.115,.15,.05),m['iron'],.008)
                sewers.box('keyhole',(tx+.21,ty-.065,.252),(.024,.065,.008),m['mortar'],0)
            elif kind=='decor':
                for i in range(7):
                    x=tx+(i-3)*.078;y=ty+.045*math.sin(i*.9+variant)
                    sewers.ring('fallen chain link',x,y,.07,.049,.035,.30,m['iron_glint'] if i%2 else m['iron'])
                for side in (-1,1):
                    sewers.ring('open manacle',tx+side*.29,ty-.08,.06,.09,.07,.28,m['iron'])
                    sewers.box('worn shackle hinge',(tx+side*.28,ty-.15,.07),(.06,.05,.025),m['iron_glint'],.005)
            elif kind=='wall_torch':
                sewers.box('wall lantern back plate',(tx,ty-.07,.13),(.16,.43,.03),m['iron'],.008)
                sewers.box('candle lantern',(tx,ty+.06,.21),(.19,.28,.09),m['wood'],.008)
                for x in (-.10,.10):sewers.box('lantern iron upright',(tx+x,ty+.07,.27),(.03,.34,.04),m['iron_glint'],.005)
                for y in (-.10,.23):sewers.box('lantern roof and base',(tx,ty+y,.25),(.28,.045,.10),m['iron'],.008)
                sewers.box('short wax candle',(tx,ty+.015,.27),(.048,.12,.025),m['flame_inner'],.006)
                vertices=[(tx-.025,ty+.065,.30),(tx-.038,ty+.13,.30),(tx+.012,ty+.22,.30),(tx+.03,ty+.10,.30)]
                sewers.mesh('small candle flame',vertices,[(0,1,2,3)],m['flame'])
                sewers.ring('lantern handle',tx,ty+.29,.22,.064,.048,.30,m['iron_glint'])

def crag(name,x,y,rx,ry,height,m,rng):
    n=9;points=[]
    for i in range(n):
        a=2*math.pi*i/n;radius=rng.uniform(.78,1.12)
        points.append((x+rx*math.cos(a)*radius,y+ry*math.sin(a)*radius))
    vertices=[(a,b,0) for a,b in points]+[(a,b,height*rng.uniform(.72,1)) for a,b in points]
    vertices.append((x-rx*.12,y+ry*.13,height*1.16))
    faces=[(i,(i+1)%n,(i+1)%n+n,i+n) for i in range(n)]+[(i+n,(i+1)%n+n,2*n) for i in range(n)]
    return sewers.mesh(name,vertices,faces,m,.006)


def turn(obj,x,y,angle):
    # Mesh helpers author world coordinates: rotate around the prop, not world zero.
    c,s=math.cos(angle),math.sin(angle)
    for vertex in obj.data.vertices:
        dx,dy=vertex.co.x-x,vertex.co.y-y
        vertex.co.x=x+c*dx-s*dy;vertex.co.y=y+s*dx+c*dy


def caves(kind,variant,p,m):
    for ty in (-1,0,1):
        for tx in (-1,0,1):
            rng=random.Random(p['seed']+variant)
            if kind=='floor':
                # Continuous earth, with scattered broken rock rather than joints.
                ground=sewers.box('packed earth',(tx,ty,-.02),(1,1,.04),m['stone'],0);ground.color=(.86,.86,.86,1)
                for x,y,r in [(-.27,-.22,.31),(.27,-.02,.29),(-.07,.33,.23)]:
                    rock=crag('loose angular rubble',tx+x,ty+y,r,r*.73,p['relief'],m['stone'],rng);rock.color=(1.13,1.13,1.13,1)
            elif kind=='wall':
                sewers.box('uncut rock mass',(tx,ty,-.02),(1,1,.08),m['mortar'],0)
                for x,y,rx,ry in [(-.3,-.3,.31,.29),(.24,-.20,.36,.37),(-.17,.30,.36,.30),(.39,.39,.25,.25)]:
                    crag('fractured cliff face',tx+x,ty+y,rx,ry,p['wall_height'],m['stone'],rng)
            elif kind.startswith('door'):
                sewers.box('mine door recess',(tx,ty,.025),(.84,.94,.04),m['mortar'],0)
                for x in (-.38,.38):sewers.box('rough timber jamb',(tx+x,ty,.15),(.13,.92,.17),m['wood'],.018)
                sewers.box('mine lintel',(tx,ty+.40,.23),(.88,.12,.16),m['wood'],.016)
                for i in range(4):
                    x=tx+(i-1.5)*.15;y=ty
                    if kind=='door_open':x=tx-.27;y=ty+(i-1.5)*.13
                    sewers.box('heavy split plank',(x,y,.15),(.13,.71,.10) if kind=='door' else (.075,.115,.10),m['wood'],.012)
                for y in (-.22,.20):sewers.box('mine door strap',(tx,ty+y,.23),(.60,.035,.025),m['iron'],.004)
            elif kind=='decor':
                for x in (-.24,.24):sewers.box('timber support',(tx+x,ty,.13),(.12,.68,.15),m['wood'],.015)
                beam=sewers.box('diagonal support brace',(tx,ty,.23),(.68,.085,.075),m['wood'],.01);turn(beam,tx,ty,.67)
                for x in (-.24,.24):sewers.box('iron timber collar',(tx+x,ty-.18,.22),(.145,.06,.03),m['iron_glint'],.005)
            elif kind=='wall_torch':
                sewers.box('lantern timber mount',(tx,ty-.04,.10),(.11,.77,.10),m['wood'],.014)
                sewers.box('lantern cage',(tx,ty+.11,.18),(.23,.31,.10),m['iron'],.018)
                sewers.box('lantern window',(tx,ty+.11,.25),(.15,.23,.025),m['flame_edge'],.006)
                sewers.mesh('lantern flame',[(tx-.03,ty+.07,.28),(tx-.02,ty+.24,.28),(tx+.035,ty+.10,.28)],[(0,1,2)],m['flame_inner'])
                for x in (-.10,.10):sewers.box('lantern rim',(tx+x,ty+.11,.30),(.025,.32,.02),m['iron_glint'],.004)


def city(kind,variant,p,m):
    if kind=='wall':sewers.geometry(kind,variant,p,m);return
    for ty in (-1,0,1):
        for tx in (-1,0,1):
            rng=random.Random(p['seed']+variant)
            if kind=='floor':
                sewers.box('patterned floor bed',(tx,ty,-.02),(1,1,.04),m['mortar'],0)
                for y in (-.25,.25):
                    for x in (-.25,.25):
                        stone=sewers.slab('worn patterned slab',tx+x,ty+y,.49,.49,0,p['relief'],m['stone'],rng,p['chip'],p['bevel']);stone.color=(1,1,1,1)
                        # Two repeats per edge; clipped corners and mineral wear break the pattern.
                        border=sewers.ring('worn inset border',tx+x,ty+y,p['relief']+.005,.16,.16,.12,m['mortar'])
                        border.data.materials.append(m['stone']);border.color=(1,1,1,1)
                        start=rng.randrange(32)
                        for worn in range(6):border.data.polygons[(start+worn)%32].material_index=1
            elif kind.startswith('door'):
                sewers.box('arched recess',(tx,ty,.02),(.84,.91,.04),m['mortar'],0)
                for side in (-1,1):
                    sewers.box('slender portal column',(tx+side*.35,ty-.08,.15),(.11,.68,.16),m['stone'],.014)
                    for y in (-.39,.22):sewers.box('column capital',(tx+side*.35,ty+y,.18),(.19,.09,.20),m['stone'],.012)
                for i in range(7):
                    a=math.pi*(i+.5)/7
                    x,y=tx+.30*math.cos(a),ty+.16+.23*math.sin(a)
                    wedge=sewers.box('pointed arch stone',(x,y,.20),(.17,.14,.14),m['stone'],.01);turn(wedge,x,y,a-math.pi/2)
                for x in (-.16,0,.16):
                    xx=tx+x if kind=='door' else tx-.26
                    sewers.box('carved door panel',(xx,ty-.08,.14),(.145,.56,.08),m['wood'],.01)
                sewers.ring('bronze door ring',tx+.15,ty-.09,.20,.036,.045,.32,m['iron_glint'])
            elif kind=='decor':
                for side in (-1,1):
                    sewers.box('broken column plinth',(tx+side*.25,ty-.18,.08),(.24,.19,.12),m['stone'],.014)
                    sewers.box('broken column shaft',(tx+side*.25,ty+.02,.14),(.12,.38,.15),m['stone'],.016)
                for i in range(5):
                    a=math.pi*(i+.5)/5
                    x,y=tx+.24*math.cos(a),ty+.17+.15*math.sin(a)
                    obj=sewers.box('fallen arch voussoir',(x,y,.13),(.16,.10,.09),m['stone'],.012);turn(obj,x,y,a-math.pi/2)
            elif kind=='wall_torch':
                sewers.box('stone candle bracket',(tx,ty-.12,.16),(.27,.18,.14),m['stone'],.02)
                for x in (-.065,.065):
                    sewers.box('paired wax candle',(tx+x,ty+.04,.25),(.065,.30,.05),m['bone'],.008)
                    sewers.mesh('small candle flame',[(tx+x-.026,ty+.18,.29),(tx+x-.015,ty+.29,.29),(tx+x+.028,ty+.19,.29)],[(0,1,2)],m['flame_inner'])


def halls(kind,variant,p,m):
    if kind in ('floor','wall'):
        sewers.geometry(kind,variant,p,m);return
    for ty in (-1,0,1):
        for tx in (-1,0,1):
            if kind.startswith('door'):
                sewers.box('obsidian gate shadow',(tx,ty,.02),(.88,.93,.04),m['mortar'],0)
                for side in (-1,1):
                    sewers.box('obsidian jamb',(tx+side*.35,ty,.17),(.15,.88,.20),m['stone'],.012)
                    for y in (-.29,.03,.33):sewers.box('bone gate binding',(tx+side*.35,ty+y,.29),(.19,.03,.025),m['bone'],.008)
                sewers.box('dark lintel',(tx,ty+.38,.23),(.84,.15,.18),m['stone'],.014)
                for x in (-.18,0,.18):
                    sewers.box('obsidian door blade',(tx+x if kind=='door' else tx-.25,ty-.05,.15),(.16,.65,.06),m['stone'],.012)
            elif kind=='decor':
                sewers.ring('inscribed ritual circle',tx,ty,.025,.37,.37,.045,m['ritual'])
                for i in range(6):
                    a=math.pi*i/3
                    x,y=tx+.27*math.cos(a),ty+.27*math.sin(a)
                    mark=sewers.box('radial ritual cut',(x,y,.03),(.12,.025,.006),m['ritual'],0);turn(mark,x,y,a)
                sewers.box('bone throne seat',(tx,ty-.06,.09),(.21,.18,.08),m['stone'],.014)
                for side in (-1,1):
                    sewers.box('ribbed throne post',(tx+side*.10,ty+.05,.16),(.025,.36,.025),m['bone'],.006)
                for y in (.01,.09,.17):sewers.box('throne rib',(tx,ty+y,.18),(.20,.022,.022),m['bone'],.006)
            elif kind=='wall_torch':
                sewers.box('obsidian brazier back',(tx,ty-.06,.14),(.23,.39,.10),m['stone'],.014)
                sewers.ring('bone brazier rim',tx,ty+.07,.22,.12,.055,.24,m['bone'])
                for side in (-1,1):sewers.box('brazier bone brace',(tx+side*.08,ty-.06,.24),(.025,.24,.025),m['bone'],.006)
                sewers.flame(tx,ty-.04,.25,p,m)


def render(region,kind,variant,reset,camera,save,cache):
    p=parameters(region,kind);scene=reset();camera(scene,True);scene.eevee.taa_render_samples=32
    for name in ('key','fill','rim'):
        light=bpy.data.objects[name];light.data.energy*=p['light_strength']
        angle=math.radians(p['light_angle']);x,y,z=light.location
        light.location=(x*math.cos(angle)-y*math.sin(angle),x*math.sin(angle)+y*math.cos(angle),z)
        light.rotation_euler=(-light.location).to_track_quat('-Z','Y').to_euler()
    mats=materials.environment(dict(p,asset_class=kind))
    {'prison':prison,'caves':caves,'city':city,'halls':halls}[region](kind,variant,p,mats)
    save(cache/region/f'{kind}_{variant}.png')

    if kind.startswith('door'):
        # A material-ID pass survives the warm key and rust, unlike hue guessing.
        masks={}
        for name in ('iron','iron_glint'):
            masks[mats[name]]=True
        flat={}
        for metal in (False,True):
            material=bpy.data.materials.new('metal mask '+str(metal));material.use_nodes=True
            nodes=material.node_tree.nodes;nodes.clear()
            emission=nodes.new('ShaderNodeEmission');emission.inputs['Color'].default_value=((1,1,1,1) if metal else (0,0,0,1))
            out=nodes.new('ShaderNodeOutputMaterial');material.node_tree.links.new(emission.outputs[0],out.inputs[0]);flat[metal]=material
        for obj in bpy.data.objects:
            if obj.type=='MESH':
                for slot in obj.material_slots:slot.material=flat[slot.material in masks]
        save(cache/region/f'{kind}_{variant}_metal.png')
