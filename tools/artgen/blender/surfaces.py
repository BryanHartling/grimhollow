"""GPL-3.0-or-later. Native relief materials for retained v4 special-room layouts.

Room atlases encode door apertures and multipart quest architecture. Their
semantic contours stay aligned to the original cell IDs; these rendered reliefs
replace the surface pixels, including the new Vault's metal and masonry.
"""
import math,bpy
import props,primitives,materials

COLORS=['1A1816','3B3733','6B645C','2E241A','4A3B2A','3C2E1F','5A4630','2C2F33','565B62','C9BFA8','EFE7D2','5E0D12','9E1B24','3F6A1F','7BB33B','2E6F7A','6FD3E0','8A4B12','E0982F','8A7331','E4C76A','4A2C6E','9D6BD1']

def render(index,reset,camera,save,cache):
    scene=reset();camera(scene,True);color=COLORS[index]
    entry=dict(name='material relief '+str(index),primary=color,secondary=color,accent=color,seed=3400+index,variant=index%4)
    model=props.Model(entry);model.root.location.z=0
    # Periodic continuous mesh: no isolated pixel-sized cubes or traced pixels.
    verts=[];faces=[];steps=96
    for y in range(steps+1):
        for x in range(steps+1):
            xx=x/steps*3-1.5;yy=y/steps*3-1.5
            grain=math.sin(xx*math.tau*3+math.sin(yy*math.tau))*math.cos(yy*math.tau*5)+.35*math.sin((xx+yy)*math.tau*13)
            if index in (3,4,5,6):grain+=1.3*math.sin(xx*math.tau*8+.2*math.sin(yy*math.tau))
            if index in (7,8,19,20):grain=.3*grain+math.sin((xx*.6+yy)*math.tau*2)
            verts.append((xx,yy,.05+grain*.025))
    for y in range(steps):
        for x in range(steps):
            a=x+y*(steps+1);faces.append((a,a+1,a+steps+2,a+steps+1))
    mesh=bpy.data.meshes.new('continuous material relief');mesh.from_pydata(verts,[],faces);mesh.update()
    obj=bpy.data.objects.new('continuous material relief',mesh);bpy.context.collection.objects.link(obj);obj.data.materials.append(model.m[0])
    save(cache/'surfaces'/f'{index:02d}.png')
