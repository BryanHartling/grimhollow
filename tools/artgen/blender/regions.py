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

def render(region,kind,variant,reset,camera,save,cache):
    p=parameters(region,kind);scene=reset();camera(scene,True);scene.eevee.taa_render_samples=32
    for name in ('key','fill','rim'):
        light=bpy.data.objects[name];light.data.energy*=p['light_strength']
        angle=math.radians(p['light_angle']);x,y,z=light.location
        light.location=(x*math.cos(angle)-y*math.sin(angle),x*math.sin(angle)+y*math.cos(angle),z)
        light.rotation_euler=(-light.location).to_track_quat('-Z','Y').to_euler()
    mats=materials.environment(dict(p,asset_class=kind))
    if region!='prison':raise ValueError('Region has not reached its stage: '+region)
    prison(kind,variant,p,mats)
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
