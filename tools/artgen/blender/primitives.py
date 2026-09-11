"""GPL-3.0-or-later. Shared environment and prop mesh primitives."""
import bpy

def joint(name,parent,location):
    obj=bpy.data.objects.new(name,None);bpy.context.collection.objects.link(obj);obj.parent=parent;obj.location=location;return obj
def mesh(kind,name,parent,location,scale,material,vertices=8):
    if kind=='cone':bpy.ops.mesh.primitive_cone_add(vertices=vertices,radius1=1,radius2=.55,depth=2)
    elif kind=='sphere':bpy.ops.mesh.primitive_uv_sphere_add(segments=vertices,ring_count=4,radius=1)
    else:bpy.ops.mesh.primitive_cube_add(size=2)
    obj=bpy.context.object;obj.name=name;obj.parent=parent;obj.location=location;obj.scale=scale;obj.data.materials.append(material);return obj

