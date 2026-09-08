"""Shared four-step palette materials; all variation comes from seeded nodes."""
import bpy

PALETTE={'wet_stone':'6B645C','dry_stone':'3B3733','mortar':'1A1816','mud':'3C2E1F',
         'rotting_wood':'5A4630','iron':'565B62','bone':'C9BFA8','cloth':'C9BFA8',
         'leather':'4A3B2A','flesh':'6B645C','mildew':'3F6A1F','sewage':'2E6F7A','blood':'5E0D12'}
def rgb(value):
    def linear(v):return v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4
    return tuple(linear(int(value[i:i+2],16)/255) for i in (0,2,4))

def library(seed):
    result={}
    for name,hexcolor in PALETTE.items():
        mat=bpy.data.materials.new(name);mat.use_nodes=True;nodes=mat.node_tree.nodes;nodes.clear();links=mat.node_tree.links
        output=nodes.new('ShaderNodeOutputMaterial');diffuse=nodes.new('ShaderNodeBsdfDiffuse');diffuse.inputs['Color'].default_value=(1,1,1,1)
        light=nodes.new('ShaderNodeShaderToRGB');links.new(diffuse.outputs[0],light.inputs[0])
        noise=nodes.new('ShaderNodeTexNoise');noise.noise_dimensions='4D';noise.inputs['W'].default_value=seed%997;noise.inputs['Scale'].default_value=24;noise.inputs['Detail'].default_value=2
        position=nodes.new('ShaderNodeNewGeometry');links.new(position.outputs['Position'],noise.inputs['Vector'])
        grain=nodes.new('ShaderNodeMapRange');grain.inputs['To Min'].default_value=.25;grain.inputs['To Max'].default_value=1.75;links.new(noise.outputs['Fac'],grain.inputs['Value'])
        bump=nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.35;bump.inputs['Distance'].default_value=.025
        links.new(noise.outputs['Fac'],bump.inputs['Height']);links.new(bump.outputs['Normal'],diffuse.inputs['Normal'])
        multiply=nodes.new('ShaderNodeMath');multiply.operation='MULTIPLY';links.new(light.outputs['Color'],multiply.inputs[0]);links.new(grain.outputs[0],multiply.inputs[1])
        ramp=nodes.new('ShaderNodeValToRGB');ramp.color_ramp.interpolation='CONSTANT'
        colors=rgb(hexcolor)
        stops=[(.0,.18),(.13,.40),(.28,.70),(.52,1.0)]
        for e in list(ramp.color_ramp.elements)[1:]:ramp.color_ramp.elements.remove(e)
        for i,(at,shade) in enumerate(stops):
            element=ramp.color_ramp.elements[0] if i==0 else ramp.color_ramp.elements.new(at)
            element.position=at;element.color=tuple(c*shade for c in colors)+(1,)
        if name in ('wet_stone','iron','sewage','blood'):
            e=ramp.color_ramp.elements.new(.85);e.color=tuple(c+(1-c)*.16 for c in colors)+(1,)
        links.new(multiply.outputs[0],ramp.inputs['Fac']);links.new(ramp.outputs[0],output.inputs['Surface']);result[name]=mat
    return result
