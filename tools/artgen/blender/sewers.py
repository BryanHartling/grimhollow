"""Seeded environment geometry. Fixed orthographic camera; no reference pixels are sampled.

Every slab is an irregular bevelled mesh, repeated with its neighbours before the
centre tile is cropped. Object-local periodic material coordinates avoid atlas seams.
"""
import json
import math
import random
from pathlib import Path
import bpy
from mathutils import Vector
import materials

PARAMS = Path(__file__).parent / 'params'
CLASSES = ('floor', 'wall', 'water', 'door', 'decor', 'wall_torch')


def parameters(kind):
    return json.loads((PARAMS / (('door' if kind == 'door_open' else kind)+'.json')).read_text())


def mesh(name, vertices, faces, material, bevel=0):
    data = bpy.data.meshes.new(name)
    data.from_pydata(vertices, [], faces)
    data.update()
    obj = bpy.data.objects.new(name, data)
    bpy.context.collection.objects.link(obj)
    obj.data.materials.append(material)
    if bevel:
        modifier = obj.modifiers.new('worn edges', 'BEVEL')
        modifier.width = bevel
        modifier.segments = 2
        obj.modifiers.new('weighted slab normals', 'WEIGHTED_NORMAL')
    return obj


def slab(name, x, y, width, height, bottom, top, mat, rng, chip=.02, bevel=.025):
    # Eight clipped corners, with broad planar facets rather than a noisy cube grid.
    outline = [(-.5,-.36),(-.37,-.5),(.35,-.5),(.5,-.35),(.5,.35),(.37,.5),(-.36,.5),(-.5,.36)]
    xy = [(x+px*width+rng.uniform(-chip,chip), y+py*height+rng.uniform(-chip,chip)) for px,py in outline]
    vertices = [(px,py,bottom) for px,py in xy] + [(px,py,top+rng.uniform(-chip/2,chip/2)) for px,py in xy]
    faces = [tuple(reversed(range(8))), tuple(range(8,16))]
    faces += [(i,(i+1)%8,(i+1)%8+8,i+8) for i in range(8)]
    return mesh(name,vertices,faces,mat,bevel)


def box(name, xyz, size, mat, bevel=.015):
    x,y,z=xyz;w,h,d=size
    v=[(x+sx*w/2,y+sy*h/2,z+sz*d/2) for sz in (-1,1) for sy in (-1,1) for sx in (-1,1)]
    return mesh(name,v,[(0,2,3,1),(4,5,7,6),(0,1,5,4),(2,6,7,3),(0,4,6,2),(1,3,7,5)],mat,bevel)


def ring(name, x, y, z, rx, ry, thickness, material):
    vertices=[]
    for radius in (1,1-thickness):
        for i in range(32):
            a=2*math.pi*i/32
            vertices.append((x+rx*radius*math.cos(a),y+ry*radius*math.sin(a),z))
    return mesh(name,vertices,[(i,(i+1)%32,(i+1)%32+32,i+32) for i in range(32)],material)


def flame(x,y,z,p,m):
    # A static sculpted lick for the wall prop. Animated fire remains stage 7.
    silhouette=[(-.035,0),(-.09,.07),(-.10,.14),(-.07,.12),(-.09,.22),(-.015,.30),(.025,.43),(.02,.25),(.06,.30),(.045,.15),(.10,.23),(.095,.085),(.055,0)]
    for size,depth,material in [(1,0,m['flame_edge']),(.67,.012,m['flame']),(.28,.022,m['flame_inner'])]:
        pts=[(x+a*size,y+b*size,z+depth) for a,b in silhouette]
        mesh('sculpted flame',pts,[tuple(range(len(pts)))],material)
    ring('pinpoint hot core',x,y+.025,z+.035,.009,.013,1,m['hot_core'])


def flagstones(p):
    """Periodic Voronoi cells: broad unequal polygons, not a perturbed square grid."""
    sites=p.get('stone_sites',[[.12,.14],[.63,.10],[.21,.65],[.68,.70],[.90,.37]])
    result=[]
    weights=p.get('stone_weights',[0]*len(sites))
    for index,(sx,sy) in enumerate(sites):
        poly=[(-2.,-2.),(3.,-2.),(3.,3.),(-2.,3.)]
        for ox in (-1,0,1):
            for oy in (-1,0,1):
                for other,(bx,by) in enumerate(sites):
                    bx+=ox;by+=oy
                    if abs(bx-sx)+abs(by-sy)<1e-8:continue
                    nx,ny=bx-sx,by-sy;limit=(bx*bx+by*by-sx*sx-sy*sy+weights[index]-weights[other])/2
                    clipped=[]
                    for a,b in zip(poly,poly[1:]+poly[:1]):
                        da=a[0]*nx+a[1]*ny-limit;db=b[0]*nx+b[1]*ny-limit
                        if da<=0:clipped.append(a)
                        if (da<=0)!=(db<=0):
                            t=da/(da-db);clipped.append((a[0]+t*(b[0]-a[0]),a[1]+t*(b[1]-a[1])))
                    poly=clipped
        # Offset edges by half the mortar joint using a centroid contraction.
        inset=p['mortar_width']/2
        points=[]
        for x,y in poly:
            distance=math.hypot(x-sx,y-sy);ratio=max(0,1-inset/distance)
            points.append((sx+(x-sx)*ratio-.5,sy+(y-sy)*ratio-.5))
        result.append(points)
    return result


def geometry(kind, variant, p, m):
    for ty in (-1,0,1):
        for tx in (-1,0,1):
            if kind=='water' and (tx!=0 or ty!=0):continue
            rng=random.Random(p['seed']+variant)
            if kind not in ('decor','wall_torch'):
                box('deep mortar bed',(tx,ty,-.15 if kind=='water' else -.035),(3,3,.06) if kind=='water' else (1,1,.06),m['mortar'],0)
            if kind=='floor' and p.get('calibrated'):
                for index,poly in enumerate(flagstones(p)):
                    top=p['relief']+rng.uniform(0,p['displacement']);n=len(poly)
                    vertices=[(tx+x,ty+y,z) for z in (0,top) for x,y in poly]
                    faces=[tuple(reversed(range(n))),tuple(range(n,2*n))]+[(i,(i+1)%n,(i+1)%n+n,i+n) for i in range(n)]
                    obj=mesh('broad polygonal flagstone',vertices,faces,m['stone'],p['bevel'])
                    shade=p.get('stone_values',[.85,1.1,1.24,.92,1.06])[index]
                    obj.color=(shade,shade,shade,1)
            elif kind=='wall' and p.get('calibrated'):
                for row in range(3):
                    for col in range(2):
                        x=tx+(col+.25+(row%2)*.45)*.5-.5
                        y=ty+(row+.5)/3-.5
                        slab('horizontal masonry course',x,y,.5-p['mortar_width'],1/3-p['mortar_width'],0,p['wall_height'],m['stone'],rng,p['chip'],p['bevel'])
            elif kind in ('floor','wall'):
                count=p['count'];pitch=1/count
                # Offset from tile borders; periodic boundary stones are completed by neighbours.
                for row in range(count):
                    for col in range(count):
                        jitter=rng.uniform(-.027,.027) if kind=='floor' else 0
                        x=tx+(col+.21+(row%2)*.43)*pitch-.5+jitter
                        y=ty+(row+.32)*pitch-.5
                        top=(p['wall_height'] if kind=='wall' else p['relief'])+rng.uniform(0,p['displacement'])
                        slab('wet brick' if kind=='wall' else 'flagstone',x,y,pitch-p['mortar_width'],pitch-p['mortar_width'],0,top,m['stone'],rng,p['chip'],p['bevel'])
                        if rng.random()<p['moss_density']:
                            slab('moss in the wet joint',x-pitch*.46,y,.045,pitch*.55,.002,.022,m['moss'],rng,.008,.004)
            elif kind=='water':
                n=192;vertices=[]
                for y in range(n+1):
                    for x in range(n+1):
                        u=3*x/n;v=3*y/n
                        z=p['water_depth']+p['displacement']*(math.sin(2*math.pi*(u+v)+variant)+.35*math.sin(2*math.pi*(2*u-v)))
                        vertices.append((tx+u-1.5,ty+v-1.5,z))
                faces=[(y*(n+1)+x,y*(n+1)+x+1,(y+1)*(n+1)+x+1,(y+1)*(n+1)+x) for y in range(n) for x in range(n)]
                surface=mesh('continuous shallow water',vertices,faces,m['water'])
                for poly in surface.data.polygons:poly.use_smooth=True
            elif kind.startswith('door'):
                # A recessed gate with tapered voussoirs, iron straps and a dark aperture.
                box('doorway shadow',(tx,ty,.03),(.82,.98,.055),m['mortar'])
                for side in (-1,1):
                    for row in range(3):
                        slab('jamb',tx+side*.41,ty+(row-1)*.30,.16,.285,0,.24,m['stone'],rng,.008,.012)
                for i in range(7):
                    a=math.pi*(i+.5)/7
                    obj=box('arch voussoir',(tx+.35*math.cos(a),ty+.15+.30*math.sin(a),.28),(.19,.16,.12),m['stone'])
                    obj.rotation_euler.z=a-math.pi/2
                for i in range(5):
                    x=tx+(i-2)*.127;y=ty-.085
                    if kind=='door_open': x=tx-.27;y=ty+(i-2)*.09
                    obj=box('weathered oak plank',(x,y,.12),(.118,.63,.12),m['wood'])
                    if kind=='door_open':obj.rotation_euler.z=1.30
                if kind=='door':
                    for y in (-.29,.06):
                        box('wrought iron strap',(tx,ty+y,.197),(.62,.045,.025),m['iron'])
                        for x in (-.23,0,.23):box('riveted strap',(tx+x,ty+y,.215),(.023,.026,.02),m['iron_glint'],.006)
                    ring('ring handle',tx+.18,ty-.08,.222,.041,.052,.38,m['iron_glint'])
            elif kind=='decor':
                for i in range(p['decor_density']):
                    x=tx+rng.uniform(-.27,.27);y=ty+rng.uniform(-.26,.26)
                    slab('fallen masonry',x,y,rng.uniform(.10,.23),rng.uniform(.09,.19),.01,rng.uniform(.07,.15),m['stone'],rng,.013,.012)
                    if rng.random()<p['moss_density']:slab('moss cushion',x-.04,y+.03,.12,.08,.02,.04,m['moss'],rng,.014,.008)
            elif kind=='wall_torch':
                box('iron wall plate',(tx,ty-.13,.16),(.105,.33,.03),m['iron'])
                box('charred torch shaft',(tx,ty-.09,.21),(.050,.45,.045),m['wood'])
                for y in (-.21,-.02):
                    box('iron collar',(tx,ty+y,.25),(.10,.035,.04),m['iron_glint'])
                ring('sconce bowl',tx,ty+.065,.255,.103,.045,.3,m['iron_glint'])
                flame(tx,ty+.035,.27,p,m)


def render(kind,variant,reset,camera,save,cache):
    p=parameters(kind)
    scene=reset();camera(scene,True)
    scene.eevee.taa_render_samples=32
    for name in ('key','fill','rim'):
        light=bpy.data.objects[name]
        light.data.energy*=p['light_strength']
        angle=math.radians(p['light_angle'])
        x,y,z=light.location
        light.location=(x*math.cos(angle)-y*math.sin(angle),x*math.sin(angle)+y*math.cos(angle),z)
        light.rotation_euler=(-light.location).to_track_quat('-Z','Y').to_euler()
    mats=materials.environment(dict(p,asset_class=kind))
    geometry(kind,variant,p,mats)
    save(cache/f'tiles/{kind}_{variant}.png')
