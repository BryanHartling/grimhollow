"""Blender -b -P entry point. Fixed camera, lighting and deterministic POC assets."""
from pathlib import Path
import sys,math,random
import bpy
from mathutils import Vector
HERE=Path(__file__).resolve().parent;sys.path.insert(0,str(HERE))
import materials,primitives,sewers
import liquids,json
ROOT=HERE.parents[2];CACHE=ROOT/'tools/artgen/render_cache';CACHE.mkdir(parents=True,exist_ok=True);rendered_count=0
ANIMATIONS={'idle':2,'run':6,'attack':5,'die':5,'special':4}

def reset():
    bpy.ops.object.select_all(action='SELECT');bpy.ops.object.delete(use_global=False)
    for mat in list(bpy.data.materials):bpy.data.materials.remove(mat)
    scene=bpy.context.scene;scene.render.engine='BLENDER_EEVEE_NEXT';scene.eevee.taa_render_samples=16
    scene.render.resolution_percentage=100;scene.render.resolution_x=96;scene.render.resolution_y=96
    scene.render.image_settings.file_format='PNG';scene.render.image_settings.color_mode='RGBA';scene.render.image_settings.color_depth='8';scene.render.film_transparent=True
    scene.view_settings.view_transform='Standard';scene.view_settings.look='None';scene.view_settings.exposure=0;scene.view_settings.gamma=1
    scene.world.color=(.015,.015,.015)
    for name,location,energy,color in [('key',(-3,-4,5),850,'E0982F'),('fill',(4,-1,3),212.5,'2E6F7A'),('rim',(0,3,4),85,'C9BFA8')]:
        light=bpy.data.lights.new(name,'AREA');light.energy=energy;light.color=materials.rgb(color);light.shape='DISK';light.size=3
        obj=bpy.data.objects.new(name,light);scene.collection.objects.link(obj);obj.location=location;obj.rotation_euler=(-obj.location).to_track_quat('-Z','Y').to_euler()
    camera=bpy.data.cameras.new('fixed orthographic');camera.type='ORTHO';obj=bpy.data.objects.new('camera',camera);scene.collection.objects.link(obj);scene.camera=obj
    return scene

def camera(scene,tile=False):
    camera=scene.camera
    if tile:
        camera.location=(0,0,6);target=Vector((0,0,0));camera.data.ortho_scale=3
        scene.render.resolution_x=scene.render.resolution_y=192
    else:
        # One metre of vertical figure projects to 60 pixels at this fixed rig.
        target=Vector((0,0,.48));camera.location=target+Vector((0,-4,4*math.sqrt(3)));camera.data.ortho_scale=128/120
        scene.render.resolution_x=scene.render.resolution_y=128
    camera.rotation_euler=(target-camera.location).to_track_quat('-Z','Y').to_euler()

def render(path):
    global rendered_count
    path.parent.mkdir(parents=True,exist_ok=True);bpy.context.scene.render.filepath=str(path);bpy.ops.render.render(write_still=True)
    rendered_count+=1

def tiles(kind,variant):
    scene=reset();camera(scene,True);m=materials.library(501+variant);rng=random.Random(501+variant)
    root=primitives.joint('tiles with seamless neighbors',None,(0,0,0))
    for ty in [-1,0,1]:
        for tx in [-1,0,1]:
            base='mud' if kind=='grass' else 'mortar';primitives.mesh('cube','mortar',root,(tx,ty,-.04),(.5,.5,.04),m[base])
            if kind=='water':
                # A seeded, displaced surface with broad depth changes and wet highlights.
                for y in range(8):
                    for x in range(8):primitives.mesh('cube','sewage surface',root,(tx+(x+.5)/8-.5,ty+(y+.5)/8-.5,.035+.025*math.sin((x+variant)*.8+y*.7)),(.063,.063,.035),m['sewage'])
            else:
                for y in range(4):
                    for x in range(4):
                        z=(.6 if kind=='wall' else .05)+rng.random()*.075
                        slab=primitives.mesh('cube','relief slab',root,(tx+(x+.5)/4-.5,ty+(y+.5)/4-.5,z/2),(.119,.118,z/2),m['wet_stone'])
                        bpy.context.view_layer.objects.active=slab;slab.select_set(True);bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
                        bevel=slab.modifiers.new('Chipped wet bevel','BEVEL');bevel.width=.018;bevel.segments=1
            if kind=='grass':
                for i in range(23):
                    obj=primitives.mesh('cone','mildew reed',root,(tx+rng.uniform(-.44,.44),ty+rng.uniform(-.44,.44),.14),(.022,.022,rng.uniform(.09,.22)),m['mildew'],5)
                    obj.rotation_euler=(rng.uniform(-.4,.4),rng.uniform(-.4,.4),0)
            if kind.startswith('door'):
                for side in [-1,1]:primitives.mesh('cube','door post',root,(tx+side*.39,ty,.32),(.075,.12,.32),m['dry_stone'])
                for i in range(5):
                    x=tx+(i-2)*.13;y=ty
                    if kind=='door_open':x=tx-.32;y=ty+(i-2)*.13
                    primitives.mesh('cube','rotting plank',root,(x,y,.30),(.060,.065,.30) if kind=='door' else (.065,.060,.30),m['rotting_wood'])
                primitives.mesh('cube','iron band',root,(tx,ty-.072,.39),(.32,.025,.025),m['iron'])
    render(CACHE/f'tiles/{kind}_{variant}.png')

def character(kind,tier=0):
    from experimental import rig_biped,rig_quad
    scene=reset();camera(scene);m=materials.library(570+tier)
    biped=kind not in ('rat','crab');module=rig_biped if biped else rig_quad
    root,joints=module.build(kind,tier,m) if biped else module.build(kind,m)
    # Quadrupeds retain the same rig/camera calibration; their squat anatomy is not stretched here.
    for animation,count in ANIMATIONS.items():
        for frame in range(count):
            module.pose(root,joints,animation,frame/max(1,count-1))
            render(CACHE/f'{kind}/{tier}/{animation}_{frame}.png')

import argparse,regions
parser=argparse.ArgumentParser();parser.add_argument('--asset');args=parser.parse_args(sys.argv[sys.argv.index('--')+1:] if '--' in sys.argv else [])
assets=set(args.asset.split(',')) if args.asset else None
locked=set(json.loads((HERE/'locks.json').read_text())['classes'])
for kind in ['floor','wall','water','grass','door','door_open','decor','wall_torch']:
    if kind in locked or kind=='water':continue
    if assets is None or kind in assets or ('sewers' in assets and kind!='grass') or ('door' in assets and kind=='door_open'):
        for variant in range(3):
            if kind=='grass':tiles(kind,variant)
            else:sewers.render(kind,variant,reset,camera,render,CACHE)
for kind in ['rat','crab','skeleton','ghoul']:
    if args.asset=='experimental/'+kind:character(kind)
if args.asset=='experimental/necromancer':
    for tier in range(8):character('necromancer',tier)
if assets is None or any(a=='liquids' or a.startswith('liquids/') for a in assets):
    for kind in liquids.COLORS:
        if assets is None or 'liquids' in assets or 'liquids/'+kind in assets:
            for variant in range(4):
                for frame in range(8):liquids.render(kind,variant,frame,reset,camera,render,CACHE)
    if assets is None or 'liquids' in assets or 'liquids/ripple' in assets:
        for frame in range(8):liquids.ripple(frame,reset,camera,render,CACHE)

for region in ('prison','caves','city','halls'):
    if not (HERE/'params'/region).exists():continue
    if assets is None or any(a.startswith(region) for a in assets):
        for kind in ('floor','wall','door','door_open','decor','wall_torch'):
            if assets is None or region in assets or region+':'+kind in assets or (kind=='door_open' and region+':door' in assets):
                for variant in range(3):regions.render(region,kind,variant,reset,camera,render,CACHE)
print(f'Rendering complete: {rendered_count} rendered files')
