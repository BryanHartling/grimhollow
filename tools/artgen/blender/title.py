"""GPL-3.0-or-later. Original extruded blackletter and a ruined nave composition."""
import math,bpy
import props,primitives as P,materials

# Heavy stems, fine joining strokes, angular bowls. Only the initial is ornamented.
LETTERS={
'G':[((.60,.84),(.45,1),(.14,1),(0,.83),(0,.13),(.14,0),(.53,0),(.65,.15),(.65,.44),(.35,.44))],
'R':[((0,0),(0,1)),((0,1),(.40,1),(.60,.84),(.60,.64),(.39,.5),(0,.5)),((.26,.50),(.63,0))],
'I':[((.3,0),(.3,1)),((.10,1),(.5,1)),((.10,0),(.5,0))],
'M':[((0,0),(0,1)),((0,1),(.32,.60),(.64,1)),((.64,1),(.64,0))],
'H':[((0,0),(0,1)),((.62,0),(.62,1)),((0,.5),(.62,.5))],
'O':[((.15,0),(0,.14),(0,.85),(.15,1),(.48,1),(.64,.85),(.64,.14),(.48,0),(.15,0))],
'L':[((0,1),(0,0),(.58,0))],
'W':[((0,1),(.12,0),(.34,.4),(.56,0),(.7,1))]}

def strip(root,a,b,width,material,z=.04):
    dx,dy=b[0]-a[0],b[1]-a[1];l=math.hypot(dx,dy);nx=-dy/l*width/2;ny=dx/l*width/2
    points=[(a[0]+nx,a[1]+ny),(b[0]+nx,b[1]+ny),(b[0]-nx,b[1]-ny),(a[0]-nx,a[1]-ny)]
    root.poly(points,material,z,.035)

def word(reset,camera,save,cache):
    scene=reset();camera(scene,True);scene.render.resolution_x=1000;scene.render.resolution_y=200;scene.camera.data.ortho_scale=9.8
    scene.camera.location=(0,0,6);scene.camera.rotation_euler=(0,0,0)
    e=dict(name='carved bone wordmark',primary='C9BFA8',secondary='5E0D12',accent='8A7331',variant=0)
    model=props.Model(e);model.root.location=(0,0,0)
    for i,ch in enumerate('GRIMHOLLOW'):
        x=(i-4.5)*.92-.34
        for stroke in LETTERS[ch]:
            for a,b in zip(stroke,stroke[1:]):
                aa=(x+a[0],a[1]-.5);bb=(x+b[0],b[1]-.5)
                vertical=abs(a[1]-b[1])>abs(a[0]-b[0])*2
                strip(model,aa,bb,.13 if vertical else .027,0)
        # Diamond-ended stems supply a consistent angular baseline and cap line.
        for dx in [stroke[0][0] for stroke in LETTERS[ch] if len(stroke)==2 and stroke[0][0]==stroke[1][0]]:
            for y in (-.48,.48):model.poly([(x+dx-.08,y),(x+dx,y+.06),(x+dx+.08,y),(x+dx,y-.06)],0)
        if i==0:
            for yy,sign in [(.67,1),(-.67,-1)]:
                strip(model,(x-.14,yy),(x+.66,yy),.022,1)
                model.poly([(x-.14,yy),(x-.24,yy-sign*.12),(x-.32,yy+sign*.03)],1)
            model.ring(x+.31,0,.57,1,z=-.06,thickness=.018)
    save(cache/'title/wordmark.png')

def background(reset,camera,save,cache):
    scene=reset();camera(scene,True);scene.render.resolution_x=800;scene.render.resolution_y=600;scene.camera.data.ortho_scale=12
    scene.camera.location=(0,0,9);scene.camera.rotation_euler=(0,0,0)
    entry=dict(name='abandoned gothic nave',primary='3B3733',secondary='1A1816',accent='5A4630',variant=0)
    m=props.Model(entry);m.root.location.z=0;m.box(0,0,13,10,1,z=-.6,d=.1)
    for layer in range(6):
        scale=1-layer*.12;width=5.1*scale;base=-4.;spring=.4*scale;top=4.*scale
        for side in (-1,1):
            x=side*width
            m.box(x,-1.8,.35*scale,5.8,0,z=.4-layer*.12,d=.10)
            m.box(x,-3.7,.6*scale,.32,2,z=.5-layer*.12)
            m.box(x,spring,.56*scale,.21,2,z=.5-layer*.12)
            points=[(x,spring),(x*.83,1.6*scale),(x*.50,2.8*scale),(0,top)]
            for a,b in zip(points,points[1:]):strip(m,a,b,.25*scale,0,z=.4-layer*.12)
            for k in range(5):m.box(x,-3.0+k*.58,.43*scale,.055,1,z=.53-layer*.12)
    # Damaged raised paving leads toward a sealed sanctuary; no seamless floor
    # assets are reused or regenerated for this standalone title composition.
    for row in range(6):
        y=-4.3+row*.40;ww=.75-row*.06
        for col in range(-8,9):
            xx=col*ww+(row%2)*ww/2
            m.poly([(xx-ww*.46,y),(xx+ww*.46,y+.02),(xx+ww*.41,y+.32),(xx-ww*.38,y+.30)],0,z=.08+row*.005,d=.02)
    m.poly([(-.68,-1.9),(.68,-1.9),(.68,.45),(0,1.15),(-.68,.45)],1,z=-.35)
    for x in (-.4,-.2,0,.2,.4):m.box(x,-.60,.045,2.5,2,z=-.27)
    # Sparse geometric hanging chains and sconces; sources stay in the same rig.
    for x in (-4.8,4.8):
        for y in (2.7,2.3,1.9,1.5):m.ring(x,y,.12,2,z=.5,thickness=.025)
        m.box(x,1.2,.55,.15,2,z=.52)
    save(cache/'title/background.png')

def banner(text,name,reset,camera,save,cache):
    scene=reset();camera(scene,True);scene.render.resolution_x=512;scene.render.resolution_y=128;scene.camera.data.ortho_scale=9
    scene.camera.location=(0,0,6);scene.camera.rotation_euler=(0,0,0)
    curve=bpy.data.curves.new('carved banner lettering','FONT');curve.body=text;curve.align_x='CENTER';curve.align_y='CENTER';curve.size=1;curve.extrude=.04;curve.bevel_depth=.012
    obj=bpy.data.objects.new('carved banner lettering',curve);bpy.context.collection.objects.link(obj)
    entry=dict(name=name,primary='C9BFA8',secondary='5E0D12',accent='8A7331',variant=0);obj.data.materials.append(props.palette(entry)[0])
    save(cache/'title'/f'{name}.png')

def render(reset,camera,save,cache):
    word(reset,camera,save,cache);background(reset,camera,save,cache)
    banner('BOSS SLAIN','boss_slain',reset,camera,save,cache);banner('GAME OVER','game_over',reset,camera,save,cache)
