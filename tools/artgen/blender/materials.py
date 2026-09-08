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


def environment(p):
    """Coarse periodic pores, wet/dry specular and explicit contact occlusion.

    This library is used only by the Sewers iteration. The experimental POC
    character materials stay unchanged until the separate silhouette stage.
    """
    result={}
    swatches={'stone':p['stone_color'],'mortar':'1A1816','wood':'5A4630',
              'iron':'2C2F33','iron_glint':'6B645C','moss':p['moss_color'],
              'water':p['water_color'],'flame_edge':p.get('flame_edge','782D17'),'flame':'E0982F',
              'flame_inner':'E4C76A','hot_core':'EFE7D2'}
    for name,color in swatches.items():
        mat=bpy.data.materials.new(name);mat.use_nodes=True
        nodes=mat.node_tree.nodes;nodes.clear();links=mat.node_tree.links
        out=nodes.new('ShaderNodeOutputMaterial')
        if p.get('calibrated') and p['asset_class'] in ('floor','wall','water'):
            calibrated_surface(nodes,links,out,name,color,p)
            result[name]=mat;continue
        if name.startswith('flame') or name=='hot_core':
            emission=nodes.new('ShaderNodeEmission')
            emission.inputs['Color'].default_value=(*rgb(color),1)
            emission.inputs['Strength'].default_value=p.get('core_emission',1.1) if name=='hot_core' else p.get('inner_emission',1) if name=='flame_inner' else 1
            links.new(emission.outputs[0],out.inputs[0]);result[name]=mat;continue
        pos=nodes.new('ShaderNodeNewGeometry')
        # Torus embedding is smooth across integer tile coordinates, unlike FRACT noise.
        sep=nodes.new('ShaderNodeSeparateXYZ');links.new(pos.outputs['Position'],sep.inputs[0])
        periodic=[]
        for axis in ('X','Y'):
            angle=nodes.new('ShaderNodeMath');angle.operation='MULTIPLY';angle.inputs[1].default_value=2*3.141592653589793
            links.new(sep.outputs[axis],angle.inputs[0])
            for op in ('SINE','COSINE'):
                trig=nodes.new('ShaderNodeMath');trig.operation=op;links.new(angle.outputs[0],trig.inputs[0]);periodic.append(trig.outputs[0])
        combine=nodes.new('ShaderNodeCombineXYZ')
        for i in range(3):links.new(periodic[i],combine.inputs[i])
        noise=nodes.new('ShaderNodeTexNoise');noise.noise_dimensions='4D'
        links.new(combine.outputs[0],noise.inputs['Vector']);links.new(periodic[3],noise.inputs['W'])
        noise.inputs['Scale'].default_value=p['grain_frequency'];noise.inputs['Detail'].default_value=2
        bump=nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.45
        bump.inputs['Distance'].default_value=p['bump_depth']*(.25 if name=='water' else 1)
        links.new(noise.outputs['Fac'],bump.inputs['Height'])
        diffuse=nodes.new('ShaderNodeBsdfDiffuse');diffuse.inputs['Color'].default_value=(1,1,1,1)
        links.new(bump.outputs['Normal'],diffuse.inputs['Normal'])
        light=nodes.new('ShaderNodeShaderToRGB');links.new(diffuse.outputs[0],light.inputs[0])
        ao=nodes.new('ShaderNodeAmbientOcclusion');ao.inputs['Distance'].default_value=p['ao_distance'];ao.samples=16
        mul=nodes.new('ShaderNodeMath');mul.operation='MULTIPLY';links.new(light.outputs[0],mul.inputs[0]);links.new(ao.outputs['AO'],mul.inputs[1])
        grain=nodes.new('ShaderNodeMapRange');grain.inputs['To Min'].default_value=p.get('grain_min',.6);grain.inputs['To Max'].default_value=p.get('grain_max',1.25)
        links.new(noise.outputs['Fac'],grain.inputs['Value'])
        shaded=nodes.new('ShaderNodeMath');shaded.operation='MULTIPLY';links.new(mul.outputs[0],shaded.inputs[0]);links.new(grain.outputs[0],shaded.inputs[1])
        ramp=nodes.new('ShaderNodeValToRGB');ramp.color_ramp.interpolation='CONSTANT'
        for e in list(ramp.color_ramp.elements)[1:]:ramp.color_ramp.elements.remove(e)
        stops=p.get('light_steps',[0,.10,.23,.45])
        if name=='moss':stops=p.get('moss_light_steps',stops)
        if name=='water':stops=p.get('water_steps',stops)
        for i,(at,value) in enumerate(zip(stops,(.15,.38,.65,1))):
            e=ramp.color_ramp.elements[0] if i==0 else ramp.color_ramp.elements.new(at)
            e.position=at;e.color=tuple(v*value for v in rgb(color))+(1,)
        links.new(noise.outputs['Fac'] if name=='water' and 'water_steps' in p else shaded.outputs[0],ramp.inputs['Fac'])
        emission=nodes.new('ShaderNodeEmission');links.new(ramp.outputs[0],emission.inputs['Color'])
        if name in ('stone','water','iron_glint'):
            gloss=nodes.new('ShaderNodeBsdfGlossy')
            gloss.inputs['Color'].default_value=(.7,.65,.50,1)
            gloss.inputs['Roughness'].default_value=p['water_roughness'] if name=='water' else p['stone_roughness']
            links.new(bump.outputs['Normal'],gloss.inputs['Normal'])
            add=nodes.new('ShaderNodeMixShader');add.inputs[0].default_value=p['specular'] if name=='water' else p['specular']*.40
            links.new(emission.outputs[0],add.inputs[1]);links.new(gloss.outputs[0],add.inputs[2]);links.new(add.outputs[0],out.inputs[0])
        else:links.new(emission.outputs[0],out.inputs[0])
        result[name]=mat
    return result


def calibrated_surface(nodes,links,out,name,color,p):
    """Broad depth and masonry value fields in the render, before palette packing."""
    def mathnode(op,a,b=0):
        node=nodes.new('ShaderNodeMath');node.operation=op
        for i,value in enumerate((a,b)):
            if isinstance(value,(int,float)):node.inputs[i].default_value=value
            else:links.new(value,node.inputs[i])
        return node.outputs[0]
    pos=nodes.new('ShaderNodeNewGeometry');sep=nodes.new('ShaderNodeSeparateXYZ');links.new(pos.outputs['Position'],sep.inputs[0])
    x,y=sep.outputs['X'],sep.outputs['Y'];kind=p['asset_class']
    noise=nodes.new('ShaderNodeTexNoise')
    if kind=='floor':
        # Periodic coordinates keep mineral and moss variation continuous at joins.
        periodic=nodes.new('ShaderNodeCombineXYZ')
        for i,v in enumerate((mathnode('SINE',mathnode('MULTIPLY',x,6.2831853)),mathnode('COSINE',mathnode('MULTIPLY',x,6.2831853)),mathnode('SINE',mathnode('MULTIPLY',y,6.2831853)))):links.new(v,periodic.inputs[i])
        links.new(periodic.outputs[0],noise.inputs['Vector']);noise.noise_dimensions='4D'
        links.new(mathnode('COSINE',mathnode('MULTIPLY',y,6.2831853)),noise.inputs['W'])
    else:links.new(pos.outputs['Position'],noise.inputs['Vector'])
    noise.inputs['Scale'].default_value=p['grain_frequency'];noise.inputs['Detail'].default_value=1
    value=p.get('mortar_value',.18) if name=='mortar' else p.get('stone_value',.42)
    if name=='stone':
        if kind=='floor':
            info=nodes.new('ShaderNodeObjectInfo');value=mathnode('MULTIPLY',value,info.outputs['Color'])
        value=mathnode('ADD',value,mathnode('MULTIPLY',mathnode('SUBTRACT',noise.outputs['Fac'],.5),p.get('grain_amplitude',.04)))
        if kind=='wall':
            fy=mathnode('FRACT',mathnode('ADD',y,.5))
            cap=mathnode('GREATER_THAN',fy,.80);damp=mathnode('LESS_THAN',fy,.30)
            value=mathnode('ADD',value,mathnode('MULTIPLY',cap,p.get('cap_light',.25)))
            value=mathnode('SUBTRACT',value,mathnode('MULTIPLY',damp,p.get('damp_dark',.12)))
    if name=='water':
        cx=mathnode('MULTIPLY',mathnode('ADD',mathnode('COSINE',mathnode('MULTIPLY',x,6.2831853)),1),.5)
        cy=mathnode('MULTIPLY',mathnode('ADD',mathnode('COSINE',mathnode('MULTIPLY',y,6.2831853)),1),.5)
        depth=mathnode('POWER',mathnode('MULTIPLY',cx,cy),p.get('depth_power',1))
        value=mathnode('SUBTRACT',p.get('water_edge',.12),mathnode('MULTIPLY',depth,p.get('depth_gradient',.12)))
        # Long smooth wave crests catch one narrow reflected light, never pore noise.
        wave=mathnode('ADD',mathnode('MULTIPLY',mathnode('ADD',y,p.get('wave_phase',0)),6.2831853),mathnode('MULTIPLY',mathnode('SINE',mathnode('MULTIPLY',x,6.2831853)),p.get('wave_bend',.6)))
        crest=mathnode('POWER',mathnode('MULTIPLY',mathnode('ADD',mathnode('COSINE',wave),1),.5),p.get('crest_power',250))
        patch=mathnode('POWER',mathnode('MULTIPLY',mathnode('ADD',mathnode('COSINE',mathnode('MULTIPLY',mathnode('ADD',x,p.get('reflection_x',0)),6.2831853)),1),.5),p.get('reflection_power',2))
        highlight=mathnode('MULTIPLY',crest,patch)
        reflected=mathnode('MULTIPLY',highlight,p.get('specular_value',1.5))
    # sRGB values are converted to linear before emission so they survive Standard view.
    value=mathnode('MAXIMUM',value,.002)
    rgbvalue=tuple(int(color[i:i+2],16)/255 for i in (0,2,4));luma=sum(a*b for a,b in zip(rgbvalue,(.2126,.7152,.0722)))
    channels=[]
    moss=tuple(int(p['moss_color'][i:i+2],16)/255 for i in (0,2,4));moss_luma=sum(a*b for a,b in zip(moss,(.2126,.7152,.0722)))
    for i,c in enumerate(rgbvalue):
        ratio=c/luma
        if kind=='floor' and name=='stone' and 'moss_threshold' in p:
            patch=mathnode('GREATER_THAN',noise.outputs['Fac'],p['moss_threshold'])
            ratio=mathnode('ADD',ratio,mathnode('MULTIPLY',patch,moss[i]/moss_luma-ratio))
        encoded=mathnode('MULTIPLY',value,ratio)
        if name=='water':encoded=mathnode('ADD',encoded,reflected)
        linear=mathnode('POWER',mathnode('DIVIDE',mathnode('ADD',encoded,.055),1.055),2.4)
        channels.append(linear)
    combine=nodes.new('ShaderNodeCombineColor');combine.mode='RGB'
    for i,c in enumerate(channels):links.new(c,combine.inputs[i])
    emission=nodes.new('ShaderNodeEmission');links.new(combine.outputs[0],emission.inputs['Color']);links.new(emission.outputs[0],out.inputs[0])
