// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.*;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

/** Presentation only. No Actor, level flag, random gameplay stream, or turn is changed. */
public final class EnhancedEffects {
    public static final String ATLAS="effects/enhanced.png";
    public enum Style {
        SMOKE(16), FLAME(6), EMBER(3), GRASS(6), NECROTIC(8), CURSE(8), INSCRIPTION(8),
        PSYCHIC(8), BONE_WALL(4), FORCE_WALL(8), CORPSE(8), SANCTUARY(8), SCORCH(3), RIPPLE(8), GREEN_FLAME(6);
        public final int count;
        Style(int count){this.count=count;}
    }
    private static TextureFilm film;
    private static FloorLayer floor;
    public static boolean enabled(){return SPDSettings.enhancedEffects();}
    public static RectF uv(Style style,int frame){
        if(film==null)film=new TextureFilm(ATLAS,64,64);
        return film.get(style.ordinal()*16+Math.floorMod(frame,style.count));
    }
    public static void frame(Image image,Style style,int frame,float width,float height){
        image.frame(uv(style,frame));image.logicalSize(width,height);
    }
    public static int phase(int cell){int n=cell*1103515245+12345;n^=n>>>16;return n&0x7fffffff;}
    public static void step(int cell){if(floor!=null&&cell>=0&&cell<floor.steps.length
            &&(Dungeon.level.map[cell]==Terrain.HIGH_GRASS||Dungeon.level.map[cell]==Terrain.FURROWED_GRASS))floor.steps[cell]=.6f;}
    public static boolean replaces(Blob blob){return blob instanceof ToxicGas||blob instanceof ParalyticGas
            ||blob instanceof CorrosiveGas||blob instanceof ConfusionGas||blob instanceof SmokeScreen
            ||blob instanceof StenchGas||blob instanceof SanctuaryZone;}
    public static void burst(int cell,Style style,float diameter,float duration){
        if(enabled()&&Dungeon.level!=null&&Game.scene() instanceof GameScene&&Dungeon.level.heroFOV[cell])
            GameScene.effect(new Burst(cell,style,diameter,duration));
    }
    private static class Burst extends Image {
        final Style style;final float duration,diameter;float time;
        Burst(int cell,Style style,float diameter,float duration){super(ATLAS);this.style=style;
            this.duration=duration;this.diameter=diameter;point(DungeonTilemap.tileToWorld(cell));
            x+=(16-diameter)/2;y+=(16-diameter)/2;EnhancedEffects.frame(this,style,0,diameter,diameter);}
        @Override public void update(){super.update();time+=Game.elapsed;visible=enabled();
            if(time>=duration){killAndErase();return;}EnhancedEffects.frame(this,style,(int)(time/duration*style.count),diameter,diameter);alpha(1-time/duration);}
    }
    /** Cached floor sprites; stepping can finish its visual loop after mechanical trampling. */
    public static class FloorLayer extends Group {
        final Image[] images=new Image[Dungeon.level.length()];
        final float[] steps=new float[images.length];float time;
        public FloorLayer(){floor=this;}
        @Override public void update(){time+=Game.elapsed;visible=enabled();if(!visible)return;
            for(int cell=0;cell<images.length;cell++){
                int tile=Dungeon.level.map[cell];Style style=null;
                if(tile==Terrain.BONE_WALL)style=Style.BONE_WALL;
                else if(tile==Terrain.FORCE_WALL)style=Style.FORCE_WALL;
                boolean shown=style!=null&&(Dungeon.level.heroFOV[cell]||Dungeon.level.visited[cell]||Dungeon.level.mapped[cell]);
                Image image=images[cell];
                if(shown){if(image==null){image=images[cell]=new Image(ATLAS);image.point(DungeonTilemap.tileToWorld(cell));add(image);}
                    float phase=phase(cell)%997/997f;
                    float t=steps[cell]>0?(.6f-steps[cell])*10:time*5+phase*style.count;
                    frame(image,style,(int)t,16,16);image.alpha(style==Style.FORCE_WALL?.75f:1);}
                if(image!=null)image.visible=shown;
                steps[cell]=Math.max(0,steps[cell]-Game.elapsed);
            }
        }
        @Override public void destroy(){if(floor==this)floor=null;super.destroy();}
    }
    /** Four clipped sub-quads wrap within one atlas frame, never into a neighboring frame. */
    public static class GasLayer extends Group {
        final Blob blob;final GasCell[] cells=new GasCell[Dungeon.level.length()];float time;
        private final java.util.ArrayList<GasCell>[] buckets=new java.util.ArrayList[101];
        private final java.nio.FloatBuffer vertices=com.watabou.glwrap.Quad.createSet(cells.length*4);
        private final float[] quad=new float[16],identity={1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
        private final int[] counts=new int[101];private com.watabou.glwrap.Vertexbuffer buffer;
        public GasLayer(Blob blob){this.blob=blob;for(int i=0;i<buckets.length;i++)buckets[i]=new java.util.ArrayList<>();}
        @Override public void update(){time+=Game.elapsed;visible=enabled();if(!visible)return;
            for(java.util.ArrayList<GasCell> bucket:buckets)bucket.clear();
            for(int cell=0;cell<cells.length;cell++){
                boolean shown=blob.cur!=null&&blob.cur[cell]>0&&(Dungeon.level.heroFOV[cell]||(blob.alwaysVisible&&(Dungeon.level.mapped[cell]||Dungeon.level.visited[cell])));
                if(shown){if(cells[cell]==null)add(cells[cell]=new GasCell(cell,blob));cells[cell].refresh(time);buckets[Math.min(100,blob.cur[cell])].add(cells[cell]);}
                if(cells[cell]!=null)cells[cell].visible=shown;
            }
        }
        @Override public void draw(){
            // Same-density cells share tint and alpha, so all their wrapped quads draw together.
            ((java.nio.Buffer)vertices).clear();java.util.Arrays.fill(counts,0);Image tint=null;
            for(int density=1;density<=100;density++)for(GasCell cell:buckets[density])for(Image image:cell.pieces)if(image.isVisible()){
                RectF uv=image.frame();com.watabou.glwrap.Quad.fill(quad,image.x,image.x+image.width,image.y,image.y+image.height,uv.left,uv.right,uv.top,uv.bottom);
                vertices.put(quad);counts[density]++;tint=image;
            }
            if(tint==null)return;
            ((java.nio.Buffer)vertices).flip();if(buffer==null)buffer=new com.watabou.glwrap.Vertexbuffer(vertices);else buffer.updateVertices(vertices);
            NoosaScript script=NoosaScript.get();tint.texture.bind();script.camera(camera());script.uModel.valueM4(identity);
            int offset=0;for(int density=1;density<=100;density++)if(counts[density]>0){
                float alpha=blob instanceof SanctuaryZone?.75f:.15f+.7f*density/100;
                script.lighting(tint.rm,tint.gm,tint.bm,alpha,0,0,0,0);script.drawQuadSet(buffer,counts[density],offset);offset+=counts[density];
            }
        }
        @Override public void destroy(){if(buffer!=null)buffer.delete();super.destroy();}
    }
    private static class GasCell extends Group {
        final int cell;final Blob blob;final Image[] pieces=new Image[4];final RectF[] rects=new RectF[4];
        GasCell(int cell,Blob blob){this.cell=cell;this.blob=blob;
            for(int i=0;i<4;i++){pieces[i]=new Image(ATLAS);rects[i]=new RectF();add(pieces[i]);}
        }
        void refresh(float time){
            if(blob instanceof SanctuaryZone){edge(time);return;}
            float p=phase(cell)%997/997f;
            RectF base=uv(Style.SMOKE,(int)(time*8+p*16));
            float sx=(time*.021f+p)%1,sy=(time*.013f+p*.67f)%1;
            float size=blob.cur[cell]<20?8:16,alpha=.15f+.7f*Math.min(100,blob.cur[cell])/100f;
            float x=(cell%Dungeon.level.width())*16+(16-size)/2,y=(cell/Dungeon.level.width())*16+(16-size)/2;
            int tint=blob instanceof ConfusionGas?0x68409C:blob instanceof SmokeScreen?0xC9BFA8
                    :blob instanceof ParalyticGas?0x8A8B88:0x7BB33B;
            for(int i=0;i<4;i++){
                boolean right=(i&1)!=0,bottom=(i&2)!=0;
                float u=right?0:sx,v=bottom?0:sy,w=right?sx:1-sx,h=bottom?sy:1-sy;
                RectF r=rects[i];r.set(base.left+u*base.width(),base.top+v*base.height(),base.left+(u+w)*base.width(),base.top+(v+h)*base.height());
                Image image=pieces[i];image.visible=w>0&&h>0;image.frame(r);image.logicalSize(w*size,h*size);
                image.x=x+(right?(1-sx)*size:0);image.y=y+(bottom?(1-sy)*size:0);image.hardlight(tint);image.alpha(alpha);
            }
        }
        void edge(float time){
            RectF base=uv(Style.SANCTUARY,(int)(time*8));int width=Dungeon.level.width();

            for(int i=0;i<4;i++){
                int n=cell+(i==0?-width:i==1?1:i==2?width:-1);Image image=pieces[i];image.visible=n<0||n>=blob.cur.length||blob.cur[n]==0;
                if(!image.visible)continue;
                float u=i==1?.875f:0,v=i==2?.875f:0,w=i%2==0?1:.125f,h=i%2==0?.125f:1;
                rects[i].set(base.left+u*base.width(),base.top+v*base.height(),base.left+(u+w)*base.width(),base.top+(v+h)*base.height());
                image.frame(rects[i]);image.logicalSize(16*w,16*h);image.x=cell%width*16+16*u;image.y=cell/width*16+16*v;image.alpha(.75f);
            }
        }
    }
    public static class Torch extends Image {
        final int cell;float time;
        public Torch(int cell){super(ATLAS);this.cell=cell;point(DungeonTilemap.tileToWorld(cell));x+=4;y+=1;}
        @Override public void update(){time+=Game.elapsed;visible=enabled()&&Dungeon.level.heroFOV[cell];
            EnhancedEffects.frame(this,Style.FLAME,((int)(time*8+phase(cell)%4)%4)*5/3,8,8);}
    }
    public static class Ember extends PixelParticle {
        int variant;
        public Ember(){texture(ATLAS);}
        public static void emit(Emitter emitter,int index,float x,float y){
            if(!enabled()||index%4!=0)return;
            Ember p=(Ember)emitter.recycle(Ember.class);p.revive();p.variant=index%3;
            p.x=x;p.y=y;p.left=p.lifespan=.6f;p.speed.set((index%7-3)*2,-18);EnhancedEffects.frame(p,Style.EMBER,p.variant,3,3);
        }
        @Override public void update(){super.update();visible=enabled();alpha(left/lifespan);}
    }
}
