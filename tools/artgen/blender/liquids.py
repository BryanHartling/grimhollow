"""Directly authored animated liquid surfaces; no parameter-search loop."""
import math,random
import bpy
from sewers import mesh
from materials import rgb

COLORS={'sewage':'506344','water':'486678','lava':'783D24'}

def emission(name,color,value):
    mat=bpy.data.materials.new(name);mat.use_nodes=True
    nodes=mat.node_tree.nodes;nodes.clear();links=mat.node_tree.links
    out=nodes.new('ShaderNodeOutputMaterial');emit=nodes.new('ShaderNodeEmission')
    channels=[int(color[i:i+2],16)/255 for i in (0,2,4)]
    lum=sum(c*w for c,w in zip(channels,(.2126,.7152,.0722)))
    def linear(v):return v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4
    emit.inputs['Color'].default_value=(*[linear(min(1,c*value/lum)) for c in channels],1)
    links.new(emit.outputs[0],out.inputs[0]);return mat

def render(kind,variant,frame,reset,camera,save,cache):
    scene=reset();camera(scene,True)
    scene.render.resolution_x=scene.render.resolution_y=64;scene.camera.data.ortho_scale=1
    phase=2*math.pi*frame/8;rng=random.Random(8631+variant*901)
    # A connected, animated mesh. Colour bands follow the displaced wave surface.
    levels=[emission('liquid depth '+str(i),COLORS[kind],.075+i*.0025) for i in range(49)]
    n=96;vertices=[];values=[]
    waves=[(rng.uniform(3,7),rng.uniform(-3,3),rng.uniform(0,6.28),rng.uniform(.2,.5)) for _ in range(4)]
    def wave(x,y):return sum(a*math.sin(kx*x+ky*y+offset+phase) for kx,ky,offset,a in waves)
    for y in range(n+1):
        for x in range(n+1):
            u=x/n-.5;v=y/n-.5;w=wave(u,v)
            vertices.append((u,v,.012*w))
            angle=variant*.73+.2
            across=u*math.cos(angle)+v*math.sin(angle)
            along=-u*math.sin(angle)+v*math.cos(angle)
            depth=math.exp(-((across/.27)**4+(along/.47)**4))
            values.append(.137-.037*depth+.006*w)
    faces=[(y*(n+1)+x,y*(n+1)+x+1,(y+1)*(n+1)+x+1,(y+1)*(n+1)+x) for y in range(n) for x in range(n)]
    surface=mesh('animated continuous liquid',vertices,faces,levels[0])
    for mat in levels[1:]:surface.data.materials.append(mat)
    for face,poly in zip(faces,surface.data.polygons):
        value=sum(values[v] for v in face)/4
        poly.material_index=max(0,min(48,round((value-.075)/.0025)));poly.use_smooth=True
    # Unevenly spaced open crest fragments, with independently moving lengths.
    bright=emission('reflected crest','EFE7D2',.90)
    soft=emission('soft crest edge',COLORS[kind],.27)
    centers=[]
    for streak in range(4):
        for attempt in range(100):
            cx=rng.uniform(-.34,.34);cy=rng.uniform(-.39,.39)
            if all(abs(cy-y)>.13 or abs(cx-x)>.36 for x,y in centers):break
        centers.append((cx,cy))
        offset=rng.uniform(0,6.28)
        length=rng.uniform(.21,.28)*(1+.1*math.sin(phase+offset));angle=rng.uniform(-.22,.22)
        drift=.022*math.sin(phase+offset);cy+=drift
        for width,z,mat in ((.047,.047,soft),(.032,.049,bright)):
            points=[]
            for side in (-1,1):
                for i in range(13):
                    t=i/12;along=(t-.5)*length
                    bend=.003*math.sin(t*math.pi*1.6+phase)
                    taper=math.sin(math.pi*t)**.35
                    points.append((cx+along*math.cos(angle),cy+along*math.sin(angle)+bend+side*width*taper/2,z))
            mesh('open reflected wave streak',points,[(i,i+1,14+i,13+i) for i in range(12)],mat)
    save(cache/f'liquids/{kind}/{variant}_{frame}.png')

def ripple(frame,reset,camera,save,cache):
    scene=reset();camera(scene,True);scene.render.resolution_x=scene.render.resolution_y=64;scene.camera.data.ortho_scale=1
    mat=emission('entry ripple','C9BFA8',.62);radius=.06+.052*frame
    points=[]
    for r in (radius,max(.01,radius-.014)):
        for i in range(96):
            a=2*math.pi*i/96;points.append((r*math.cos(a),r*math.sin(a),.03))
    mesh('one expanding entry ripple',points,[(i,(i+1)%96,(i+1)%96+96,i+96) for i in range(96)],mat)
    save(cache/f'liquids/ripple/{frame}.png')
