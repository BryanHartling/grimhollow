"""GPL-3.0-or-later. Original, named low-poly inventory and quest prop models.

Objects are shallow reliefs with real bevels, shafts and cavities. The fixed
orthographic rig sees their broad silhouette; no upstream sprite is sampled.
"""
import bpy,math,json
from mathutils import Vector
import primitives as P,materials

def palette(entry):
    result=[]
    for key in ('primary','secondary','accent'):
        mat=bpy.data.materials.new(entry['name']+' '+key);mat.use_nodes=True
        nodes=mat.node_tree.nodes;nodes.clear();links=mat.node_tree.links
        out=nodes.new('ShaderNodeOutputMaterial');diffuse=nodes.new('ShaderNodeBsdfDiffuse')
        light=nodes.new('ShaderNodeShaderToRGB');links.new(diffuse.outputs[0],light.inputs[0])
        ramp=nodes.new('ShaderNodeValToRGB');ramp.color_ramp.interpolation='CONSTANT'
        for e in list(ramp.color_ramp.elements)[1:]:ramp.color_ramp.elements.remove(e)
        color=materials.rgb(entry[key])
        for i,(at,shade) in enumerate(((0,.12),(.10,.32),(.25,.62),(.47,1),(.9,1.12))):
            e=ramp.color_ramp.elements[0] if i==0 else ramp.color_ramp.elements.new(at)
            e.position=at;e.color=tuple(c*shade if shade<=1 else c+(1-c)*(shade-1) for c in color)+(1,)
        links.new(light.outputs[0],ramp.inputs[0]);links.new(ramp.outputs[0],out.inputs[0]);result.append(mat)
    return result

class Model:
    def __init__(self,entry):
        self.e=entry;self.m=palette(entry);self.root=P.joint(entry['name'],None,(0,0,.48));self.v=entry['variant']
    def box(self,x,y,w,h,m=0,z=.04,d=.035):
        obj=P.mesh('cube','forged block',self.root,(x,y,z),(w/2,h/2,d),self.m[m])
        # Bevel in world dimensions so small facets catch the same fixed lights.
        obj.data.transform(obj.matrix_basis);obj.location=(0,0,0);obj.scale=(1,1,1)
        bevel=obj.modifiers.new('chipped edge','BEVEL');bevel.width=.008;bevel.segments=1
        return obj
    def poly(self,points,m=0,z=.065,d=.035):
        if sum(a[0]*b[1]-b[0]*a[1] for a,b in zip(points,points[1:]+points[:1]))<0:points=list(reversed(points))
        n=len(points);verts=[(x,y,z-d) for x,y in points]+[(x,y,z+d) for x,y in points]
        faces=[tuple(reversed(range(n))),tuple(range(n,2*n))]+[(i,(i+1)%n,(i+1)%n+n,i+n) for i in range(n)]
        mesh=bpy.data.meshes.new('cut profile');mesh.from_pydata(verts,[],faces);mesh.update()
        obj=bpy.data.objects.new('cut profile',mesh);bpy.context.collection.objects.link(obj);obj.parent=self.root;obj.data.materials.append(self.m[m])
        bevel=obj.modifiers.new('worn edge','BEVEL');bevel.width=.01;bevel.segments=1
        return obj
    def ball(self,x,y,rx,ry=None,m=0,z=.08):
        return P.mesh('sphere','faceted body',self.root,(x,y,z),(rx,ry or rx,rx*.7),self.m[m],8)
    def ring(self,x,y,r=.16,m=0,z=.09,thickness=.035):
        bpy.ops.mesh.primitive_torus_add(major_segments=16,minor_segments=4,location=(0,0,0),major_radius=r,minor_radius=thickness)
        obj=bpy.context.object;obj.parent=self.root;obj.location=(x,y,z);obj.data.materials.append(self.m[m]);return obj
    def rod(self,a,b,r=.025,m=0,z=.08):
        a=Vector((*a,z));b=Vector((*b,z));delta=b-a
        bpy.ops.mesh.primitive_cylinder_add(vertices=8,radius=r,depth=delta.length)
        obj=bpy.context.object;obj.parent=self.root;obj.location=(a+b)/2;obj.rotation_euler=delta.to_track_quat('Z','Y').to_euler();obj.data.materials.append(self.m[m]);return obj
    def gem(self,x=0,y=0,r=.12,m=2):
        return self.poly([(x,y+r*1.3),(x+r,y+r*.35),(x+r*.65,y-r),(x-r*.65,y-r),(x-r,y+r*.35)],m,.17,.09)
    def rune(self,x=0,y=0,r=.11,m=2):
        shapes=[[(0,1),(-.7,-1),(.7,-1),(0,1)], [(-.8,1),(.8,0),(-.8,-1),(-.8,1)],[(0,-1),(0,1),(-.8,.2),(.8,.2)], [(-.7,-1),(-.7,1),(.7,-1),(.7,1)]]
        points=[(x+a*r,y+b*r) for a,b in shapes[self.v]]
        for a,b in zip(points,points[1:]):self.rod(a,b,.014,m,.18)
    def skull(self,x=0,y=0,r=.17):
        self.ball(x,y,r,r*.9,0);self.box(x,y-r*.7,r,r*.5,0,.09)
        for dx in (-.36,.36):self.ball(x+dx*r,y+.05*r,r*.20,r*.24,1,.20)
        self.poly([(x,y-r*.1),(x-r*.12,y-r*.4),(x+r*.12,y-r*.4)],1,.20,.005)
    def leaf(self,x=0,y=0,r=.2,m=0):
        self.poly([(x,y-r),(x-r*.65,y-r*.1),(x-r*.45,y+r*.5),(x,y+r),(x+r*.7,y+r*.2),(x+r*.4,y-r*.4)],m)
        self.rod((x,y-r),(x,y+r*.75),.012,1,.13)

def build(e):
    s=Model(e);k=e['kind'];v=s.v;n=e['name']
    if k in ('sword','dagger','spear','trident','arrow','wand','staff','skull_staff','brush','cane','pickaxe','axe','hammer','club','mace','scythe'):
        length=.35 if k=='dagger' else .48
        s.rod((0,-length),(0,.27),.025 if k in ('arrow','wand') else .04,1)
        if k in ('sword','dagger','spear'):
            width=.08 if k=='spear' else .075+.018*v
            s.poly([(-width,-.06),(width,-.06),(width*.8,.28),(0,length),(-width*.8,.28)])
            s.rod((0,-.06),(0,length-.04),.009,2,.12)
            if k!='spear':s.rod((-.16,-.09),(.16,-.09),.026,0);s.ball(0,-length,.055,m=2)
        elif k=='trident':
            s.rod((-.15,.12),(.15,.12),.025)
            for x in (-.15,0,.15):s.poly([(x-.035,.12),(x+.035,.12),(x,.47)])
        elif k=='arrow':
            s.poly([(-.075,.25),(.075,.25),(0,.47)])
            for x in (-1,1):s.poly([(0,-.25),(x*.11,-.38),(x*.11,-.47),(0,-.40)],2)
        elif k=='skull_staff':s.skull(0,.29,.15);s.ring(0,.28,.2,2)
        elif k in ('wand','staff'):
            for y in (-.30,-.21):s.box(0,y,.105,.045,0,.08)
            if k=='staff' or v%2==0:s.gem(0,.31,.11)
            else:s.ring(0,.31,.10,0);s.ball(0,.31,.05,m=2,z=.18)
            if v==3:s.rod((-.09,.12),(.10,.24),.02,2)
        elif k=='brush':s.poly([(-.09,.2),(.09,.2),(.04,.45),(-.04,.40)],0);s.box(0,.18,.13,.065,2)
        elif k=='cane':s.ring(.08,.33,.08);s.box(0,-.1,.075,.6,2)
        elif k in ('axe','pickaxe','scythe'):
            if k=='pickaxe':points=[(-.32,.22),(-.12,.37),(.15,.37),(.36,.22),(.10,.28),(-.10,.28)]
            elif k=='scythe':points=[(-.05,.30),(.15,.43),(.38,.35),(.45,.1),(.26,.27),(.05,.28)]
            else:points=[(-.05,.33),(.19,.44),(.31,.22),(.20,.07),(-.05,.16)]
            s.poly(points);s.box(0,.25,.10,.23,2)
        elif k=='hammer':s.box(0,.27,.44,.24);s.box(-.23,.27,.10,.29,2)
        else:
            s.ball(0,.27,.13,.2,0 if k=='mace' else 1)
            for x in (-1,1):
                for y in (.16,.30,.4):s.poly([(x*.08,y-.03),(x*.2,y),(x*.08,y+.04)],0)
        # Diagonal silhouettes use inventory space efficiently while retaining identity.
        s.root.rotation_euler.z=math.radians(-24 if k not in ('wand','staff','skull_staff') else 15)
    elif k in ('bow','crossbow'):
        for a,b in zip([(-.18,-.42),(.05,-.27),(.15,0),(.05,.27)],[ (.05,-.27),(.15,0),(.05,.27),(-.18,.42)]):s.rod(a,b,.037,0)
        s.rod((-.18,-.42),(-.18,.42),.008,1)
        if k=='crossbow':s.rod((-.34,0),(.4,0),.045,1);s.box(.19,0,.20,.10,0)
    elif k in ('shield','armor','cloak','glove','boot','mask','statue'):
        if k=='shield':
            s.poly([(-.31,.36),(.31,.36),(.29,-.12),(0,-.44),(-.29,-.12)])
            s.poly([(-.24,.28),(.24,.28),(.20,-.10),(0,-.33),(-.20,-.10)],1,.11);s.gem(0,.05,.10)
        elif k in ('armor','cloak'):
            s.poly([(-.12,.33),(-.28,.30),(-.36,.08),(-.25,-.35),(.25,-.35),(.36,.08),(.28,.30),(.12,.33),(.10,.18),(-.10,.18)])
            if k=='armor':
                for x in (-.24,.24):s.ball(x,.20,.16,.13,0)
                s.poly([(-.16,.13),(.16,.13),(.12,-.27),(-.12,-.27)],1,.12)
                s.box(0,-.25,.43,.055,1,.15);s.gem(0,.07,.08)
                if n=='ARMOR_BONE':
                    for y in (-.15,-.03,.09):s.rod((-.17,y+.04),(.17,y-.02),.024,0,.22)
                    s.rod((0,-.23),(0,.16),.026,0,.24)
            else:
                s.ring(0,.25,.13,1);s.rod((-.17,-.28),(-.08,.14),.02,1);s.rod((.17,-.28),(.08,.14),.02,1);s.gem(0,.15,.07)
        elif k=='boot':s.poly([(-.17,.36),(.13,.36),(.12,-.14),(.34,-.24),(.34,-.38),(-.20,-.38)],0);s.box(-.02,.12,.32,.065,2)
        elif k=='glove':
            s.box(0,-.12,.31,.35);s.box(0,-.34,.36,.09,2)
            for x in (-.13,-.04,.05,.14):s.rod((x,0),(x,.3-abs(x)*.5),.04,0)
            s.rod((-.15,-.06),(-.28,.1),.045,0)
        elif k=='mask':s.skull(0,0,.30);s.poly([(-.3,.1),(-.4,.4),(-.12,.22)],2);s.poly([(.3,.1),(.4,.4),(.12,.22)],2)
        else:s.box(0,-.35,.46,.13,1);s.ball(0,.29,.12);s.poly([(-.14,.14),(.14,.14),(.25,-.3),(-.25,-.3)])
    elif k in ('ring','bracelet','ankh','amulet','crown','coins','seal','sundial'):
        if k in ('ring','bracelet'):s.ring(0,-.05,.24 if k=='bracelet' else .20);s.gem(0,.17,.11 if k=='ring' else .08)
        elif k=='ankh':s.ring(0,.22,.13);s.box(0,-.15,.095,.47);s.box(0,.02,.43,.085)
        elif k=='crown':
            s.poly([(-.33,-.22),(.33,-.22),(.39,.25),(.15,.07),(0,.38),(-.15,.07),(-.39,.25)])
            for x in (-.21,0,.21):s.gem(x,-.10,.055)
        elif k=='coins':
            for x,y,r in ((-.18,-.16,.14),(.12,-.19,.14),(0,.08,.17),(.24,.07,.11)):
                s.ball(x,y,r,r*.7,0,z=.08);s.ring(x,y,r*.68,1,.14,.012)
        elif k=='sundial':s.ball(0,0,.30,.27);s.poly([(0,-.1),(0,.27),(.18,-.1)],1,.18);s.ring(0,0,.26,1,.16,.012)
        else:
            if k=='amulet':s.ring(0,.22,.17,1,.07,.022)
            s.poly([(-.25,.14),(0,.27),(.25,.14),(.23,-.19),(0,-.36),(-.23,-.19)])
            if k=='seal':s.rune(0,-.04,.14)
            else:s.gem(0,-.04,.15)
    elif k in ('bottle','drop','pot','phylactery','lantern','censer','chalice','hourglass','horn','torch','candle','bomb'):
        if k=='bottle':
            silhouettes=[ [(-.20,-.30),(.20,-.30),(.24,.03),(.08,.22),(.08,.39),(-.08,.39),(-.08,.22),(-.24,.03)],
                [(-.24,-.32),(.24,-.32),(.24,.06),(.10,.19),(.10,.38),(-.10,.38),(-.10,.19),(-.24,.06)],
                [(0,-.38),(.27,-.12),(.20,.12),(.08,.24),(.08,.38),(-.08,.38),(-.08,.24),(-.20,.12),(-.27,-.12)],
                [(-.15,-.34),(.15,-.34),(.16,.20),(.09,.24),(.09,.39),(-.09,.39),(-.09,.24),(-.16,.2)]]
            s.poly(silhouettes[v]);s.box(0,.38,.21,.09,1,.06);s.box(-.09,-.01,.027,.2,2,.12)
        elif k=='drop':s.poly([(0,.4),(.24,0),(.16,-.28),(0,-.34),(-.18,-.23),(-.21,0)]);s.rod((-.06,.06),(-.11,-.12),.018,2,.13)
        elif k in ('phylactery','lantern','censer'):
            s.box(0,0,.34,.42,2);s.box(0,-.24,.44,.09,0);s.box(0,.24,.44,.09,0)
            for x in (-.18,.18):s.rod((x,-.23),(x,.24),.035,0,.13)
            s.ring(0,.35,.09,0)
            if k=='phylactery':s.skull(0,0,.115)
            elif k=='censer':s.rune(0,0,.12,1)
        elif k=='hourglass':
            s.poly([(-.23,.27),(.23,.27),(0,0),(.23,-.27),(-.23,-.27),(0,0)],1)
            for y in (-.3,.3):s.box(0,y,.54,.075)
            for x in (-.24,.24):s.rod((x,-.3),(x,.3),.025,0)
            s.poly([(-.14,-.22),(.14,-.22),(0,-.02)],2,.13)
        elif k=='chalice':
            s.poly([(-.28,.33),(.28,.33),(.18,.03),(.05,-.06),(-.05,-.06),(-.18,.03)])
            s.rod((0,-.04),(0,-.3),.04);s.box(0,-.33,.4,.07);s.gem(0,.15,.09)
        elif k=='horn':s.poly([(-.31,.3),(-.05,.31),(.07,.02),(.24,-.17),(.38,-.24),(.12,-.3),(-.12,-.10)]);s.rod((-.31,.30),(-.05,.31),.04,2)
        elif k in ('torch','candle'):
            s.box(0,-.13,.12 if k=='torch' else .24,.52,1 if k=='torch' else 0)
            s.poly([(-.12,.17),(-.14,.31),(.02,.49),(.09,.31),(.14,.19),(0,.13)],2)
        elif k=='bomb':s.ball(0,-.06,.25,.28);s.box(0,.22,.13,.11,1);s.rod((0,.26),(.12,.39),.019,1);s.ball(.13,.39,.055,m=2)
        else:s.ball(0,-.04,.29,.29);s.ring(0,.16,.2,1,.16);s.box(0,.21,.43,.07,2)
    elif k in ('scroll','book','tablet','runestone','stone','ore','crystal','shard','resin','cube','tomb','toolkit','chest','trap'):
        if k=='scroll':
            s.box(0,0,.42,.56,0,.035,.012)
            for y in (-.28,.28):s.rod((-.25,y),(.25,y),.054,0)
            s.rune(0,0,.13,2)
        elif k=='book':s.box(0,0,.49,.61,1);s.box(.015,.015,.41,.54,0,.10);s.box(-.2,0,.065,.63,2,.14);s.rune(.04,0,.13,2)
        elif k in ('tablet','runestone','stone','ore','resin','shard','crystal'):
            r=.3 if k!='shard' else .19
            s.poly([(-r,-.22),(.15,-.33),(r,-.07),(.22,.20),(-.05,.36),(-r,.18)])
            if k in ('tablet','runestone'):s.rune(0,0,.15)
            elif k in ('crystal','ore','shard','resin'):
                s.gem(-.08,0,.16,0);s.gem(.15,-.08,.11,2);s.rod((-.07,-.03),(-.07,.19),.014,1,.29)
        elif k=='cube':s.box(0,0,.45,.45,0,.09,.10);s.rune(0,0,.12,2)
        elif k=='tomb':s.poly([(-.22,-.35),(.22,-.35),(.22,.22),(.11,.35),(-.11,.35),(-.22,.22)]);s.box(0,-.35,.6,.12,1);s.rune(0,.06,.13,1)
        elif k in ('toolkit','chest'):
            s.box(0,-.05,.66,.39);s.box(0,.19,.65,.17);s.box(0,-.02,.10,.48,1,.13)
            for x in (-.25,.25):s.box(x,-.02,.07,.48,1,.13)
            s.box(0,.03,.13,.15,2,.18)
            if k=='toolkit':s.ring(0,.33,.11,1)
        else:
            s.ring(0,0,.28,1)
            for a in range(8):
                angle=a*math.tau/8;s.poly([(math.cos(angle)*.19,math.sin(angle)*.19),(math.cos(angle+.16)*.33,math.sin(angle+.16)*.33),(math.cos(angle-.16)*.33,math.sin(angle-.16)*.33)])
            s.box(0,0,.16,.18,2)
    elif k in ('bag','quiver','bandolier'):
        if k=='bag':s.poly([(-.15,.28),(.15,.28),(.30,-.04),(.23,-.30),(-.22,-.30),(-.3,-.02)]);s.box(0,.24,.4,.07,1);s.box(0,-.08,.22,.19,2,.14)
        elif k=='quiver':
            s.box(0,-.10,.26,.48);s.box(0,.1,.31,.07,2)
            for x in (-.09,0,.09):s.rod((x,.12),(x,.40),.014,1);s.poly([(x,.3),(x-.04,.40),(x+.04,.40)],2)
        else:
            s.rod((-.27,-.32),(.27,.32),.07,0)
            for x in (-.18,0,.18):s.box(x,x,.13,.20,2,.16)
    elif k in ('skull','bones','fang','eye','seed','leaf','moss','rose','tuft','dust','ember'):
        if k=='skull':s.skull(0,0,.29)
        elif k=='bones':
            for a,b in [((-.26,-.26),(.24,.27)),((-.26,.23),(.27,-.24))]:
                s.rod(a,b,.055);s.ball(*a,.095);s.ball(*b,.095)
        elif k=='fang':s.poly([(-.16,.34),(.18,.25),(.06,-.14),(-.19,-.39),(-.08,-.06)])
        elif k=='eye':s.ball(0,0,.30,.19,1);s.ball(0,0,.14,.16,0,.22);s.box(0,0,.03,.19,2,.32)
        elif k in ('seed','leaf'):s.leaf(r=.25);s.ball(.08,-.06,.1,.14,2,.12)
        elif k=='rose':
            s.rod((0,-.4),(0,.12),.025,1);s.leaf(-.10,-.16,.10,1)
            for a in range(5):s.ball(math.cos(a*math.tau/5)*.11,.17+math.sin(a*math.tau/5)*.10,.12,.10,2)
            s.ball(0,.17,.07,m=0,z=.21)
        else:
            for i in range(5):
                x=(i-2)*.09;s.poly([(x-.09,-.27),(x+.10,-.26),(x+.06,.05+(.22 if i%2==0 else .09)),(x-.02,.26)],0 if i%2 else 2)
    elif k in ('ration','pie','cake','meat','fish','fruit','egg'):
        if k=='ration':s.box(0,0,.54,.40,0);s.box(0,0,.085,.43,1,.15);s.box(0,0,.56,.065,1,.16);s.ball(0,0,.06,m=2,z=.2)
        elif k in ('pie','cake'):s.ball(0,0,.32,.23,1);s.box(0,.02,.52,.23,0,.14);s.ball(.03,.09,.07,m=2,z=.27)
        elif k=='meat':s.ball(0,0,.28,.23);s.ball(.08,-.02,.10,m=1,z=.24);s.rod((-.13,.12),(-.05,.07),.015,1,.25)
        elif k=='fish':s.ball(.03,0,.3,.14,0);s.poly([(-.22,0),(-.42,.17),(-.4,-.16)],1);s.ball(.20,.025,.025,m=2,z=.18)
        elif k=='egg':s.ball(0,0,.23,.32);s.rune(0,0,.13)
        else:s.ball(0,-.03,.28,.26);s.rod((0,.18),(.05,.34),.025,1);s.leaf(.14,.24,.10,2)
    elif k in ('chain','bolas','whip','flail','boomerang','shuriken','spyglass','key'):
        if k in ('chain','whip','flail'):
            points=[(-.25,-.32),(-.3,-.07),(-.1,.05),(.18,0),(.3,.17),(.23,.35)]
            for a,b in zip(points,points[1:]):
                if k=='chain':s.ring((a[0]+b[0])/2,(a[1]+b[1])/2,.10,0)
                else:s.rod(a,b,.025,1)
            if k=='flail':s.ball(.24,.32,.13);s.rod((-.25,-.32),(-.32,-.45),.045,0)
        elif k=='bolas':
            for x,y in ((-.25,.21),(.26,.12),(0,-.3)):s.rod((0,0),(x,y),.017,1);s.ball(x,y,.13)
        elif k=='boomerang':s.poly([(-.36,-.3),(-.13,.32),(.03,.42),(.32,-.3),(.17,-.23),(0,.11),(-.22,-.30)])
        elif k=='shuriken':
            points=[]
            for i in range(8):r=.37 if i%2==0 else .12;a=i*math.pi/4;points.append((r*math.cos(a),r*math.sin(a)))
            s.poly(points);s.ring(0,0,.05,1,.14,.02)
        elif k=='spyglass':s.box(0,0,.55,.18,0);s.box(-.27,0,.13,.29,1);s.box(.27,0,.10,.23,2)
        else:
            s.ring(0,.24,.14);s.rod((0,.10),(0,-.36),.035,0)
            s.box(.10,-.30,.20,.075);s.box(.16,-.23,.07,.15)
    else:raise ValueError('Missing mesh construction for '+k)
    return s

def render(entry,reset,camera,save,cache):
    scene=reset();camera(scene);build(entry);save(cache/'items'/f"{entry['index']:03d}.png")
