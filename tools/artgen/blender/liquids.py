"""Directly authored animated liquid surfaces; no parameter-search loop."""
import math,random
import bpy
from sewers import mesh
from materials import rgb

COLORS={'sewage':'506344','water':'486E78','lava':'783D24'}

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
    # The specular band comes from the slope of the same displaced surface.
    # Nothing is stamped over it: interference between two travelling waves
    # breaks reflected crests into irregular, open segments as time advances.
    levels.extend([emission('wave shoulder',COLORS[kind],.28),
                   emission('reflected wave normal','EFE7D2',.90)])
    n=64;vertices=[];values=[]
    ky=rng.uniform(3.2,4.0);kx=rng.uniform(1.5,1.9)
    offset=rng.uniform(0,math.tau);cross=rng.uniform(0,math.tau)
    bend=rng.uniform(.5,1.1);slope=rng.uniform(-.15,.15)
    def surface(x,y):
        a=math.tau*(ky*y+slope*x)+bend*math.sin(math.tau*x+cross)+.45*math.sin(5*y+offset)+phase
        b=math.tau*(kx*x+.15*y)+cross-phase
        return .014*math.sin(a)+.006*math.sin(b),a,b
    for y in range(n+1):
        for x in range(n+1):
            u=x/n-.5;v=y/n-.5;z,_,_=surface(u,v)
            vertices.append((u,v,z))
    faces=[]
    for y in range(n):
        for x in range(n):
            u=(x+.5)/n-.5;v=(y+.5)/n-.5;z,a,b=surface(u,v)
            angle=variant*.73+.2
            across=u*math.cos(angle)+v*math.sin(angle)
            along=-u*math.sin(angle)+v*math.cos(angle)
            depth=math.exp(-((across/.27)**4+(along/.47)**4))
            reflection=((math.cos(a)+1)/2)**35*((math.sin(b)+1)/2)**1.1
            value=.137-.037*depth+.15*z
            material=max(0,min(48,round((value-.075)/.0025)))
            if reflection>.30:material=49
            if reflection>.62:material=50
            faces.append((y*(n+1)+x,y*(n+1)+x+1,(y+1)*(n+1)+x+1,(y+1)*(n+1)+x))
            values.append(material)
    # Sub-pixel glints become flickering dots at gameplay zoom. Keep them in the
    # lower reflection band; only coherent crest segments catch the bright key.
    visited=set()
    for cell,material in enumerate(values):
        if material!=50 or cell in visited:continue
        pending=[cell];visited.add(cell);component=[]
        while pending:
            c=pending.pop();component.append(c);x=c%n;y=c//n
            for dy in (-1,0,1):
                for dx in (-1,0,1):
                    xx=x+dx;yy=y+dy;j=yy*n+xx
                    if 0<=xx<n and 0<=yy<n and j not in visited and values[j]==50:
                        visited.add(j);pending.append(j)
        if max(c%n for c in component)-min(c%n for c in component)<7:
            for c in component:values[c]=49
    obj=mesh('continuous animated reflective surface',vertices,faces,levels[0])
    for mat in levels[1:]:obj.data.materials.append(mat)
    for material,poly in zip(values,obj.data.polygons):poly.material_index=material
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
