// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.BufferUtils;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.tiles.*;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.*;
import java.nio.FloatBuffer;
import java.util.*;

/** Test 47: actual GPU pixels, independent FOV expectations, reused/panned camera. */
final class FogAlignmentChecks {
    static void run(FogOfWar fog, List<Gizmo> world, int region, String phase) {
        if (phase.equals("start")) torchFaces(world, region);
        Camera c=Camera.main;
        float[] matrix=c.matrix.clone();float z=c.zoom,sx=c.scroll.x,sy=c.scroll.y;
        int cw=c.width,ch=c.height;boolean full=c.fullScreen;
        int w=Dungeon.level.width(),h=Dungeon.level.height();
        int bw=(w+8)*16*3,bh=(h+8)*16*3;
        FrameBuffer buffer=new FrameBuffer(Pixmap.Format.RGBA8888,bw,bh,false);
        ArrayList<String> errors=new ArrayList<>();long hidden=0,visible=0;int cameras=0;
        int knownWalls=0,southernEdges=0;
        for(Gizmo g:world)if(g instanceof WallBlockingTilemap){
            WallBlockingTilemap blockers=(WallBlockingTilemap)g;
            for(int cell=0;cell<w*h;cell++)if(DungeonTileSheet.wallStitcheable(Dungeon.level.map[cell])
                    &&(Dungeon.level.heroFOV[cell]||Dungeon.level.visited[cell]||Dungeon.level.mapped[cell])){
                knownWalls++;
                if(blockers.image(cell%w,cell/w)!=null)errors.add("known wall blacked out at "+cell);
                int below=cell+w;
                if(below<w*h&&!Dungeon.level.heroFOV[below]&&!Dungeon.level.visited[below]&&!Dungeon.level.mapped[below])southernEdges++;
            }
        }
        System.out.println("TEST 47 WALL EDGES: known="+knownWalls+" unexplored-below="+southernEdges+" black blockers="+errors.size());
        try {
            c.fullScreen=true;
            for(int zoom:new int[]{1,2,3})for(int[] pan:new int[][]{{32,32},{16,48},{48,16},{61,53}}) {
                c.zoom=zoom;c.scroll.set(-pan[0],-pan[1]);c.width=bw/zoom;c.height=bh/zoom;
                Arrays.fill(c.matrix,0);c.matrix[0]=2f*zoom/bw;c.matrix[5]=-2f*zoom/bh;
                c.matrix[10]=c.matrix[15]=1;c.matrix[12]=-1+pan[0]*c.matrix[0];c.matrix[13]=1+pan[1]*c.matrix[5];
                buffer.begin();reset();Gdx.gl.glClearColor(1,1,1,1);Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
                fog.draw();Pixmap mask=Pixmap.createFromFrameBuffer(0,0,bw,bh);
                // All pixels of all generated cells, including edges and visible walls.
                for(int cell=0;cell<w*h;cell++) {
                    boolean seen=Dungeon.level.heroFOV[cell];
                    boolean never=!seen&&!Dungeon.level.visited[cell]&&!Dungeon.level.mapped[cell];
                    if(!seen&&!never)continue;
                    int left=(cell%w*16+pan[0])*zoom,top=(cell/w*16+pan[1])*zoom;
                    for(int y=0;y<16*zoom;y++)for(int x=0;x<16*zoom;x++) {
                        int rgb=mask.getPixel(left+x,bh-1-top-y)>>>8;
                        if(rgb!=(seen?0xFFFFFF:0)&&errors.size()<8)
                            errors.add("fog "+(seen?"visible":"never-seen")+" cell="+cell+" zoom="+zoom+" pan="+Arrays.toString(pan)+" pixel="+x+","+y+" rgb="+Integer.toHexString(rgb));
                        if(seen)visible++;else hidden++;
                    }
                }
                mask.dispose();
                reset();Gdx.gl.glClearColor(0,0,0,1);Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
                for(Gizmo g:world) {if(g==fog)break;if(g!=null&&g.exists&&g.isVisible())g.draw();}
                // The wall lighting program must follow the SAME mutated Camera object.
                LightingOverlay.WallLightScript shader=Script.use(LightingOverlay.WallLightScript.class);
                FloatBuffer actual=BufferUtils.newFloatBuffer(16);
                Gdx.gl.glGetUniformfv(shader.handle(),shader.uCamera.location(),actual);
                for(int i=0;i<16;i++)if(Math.abs(actual.get(i)-c.matrix[i])>0.00001f)
                    errors.add("stale wall-light camera zoom="+zoom+" matrix["+i+"]="+actual.get(i)+" expected="+c.matrix[i]);
                Pixmap raw=Pixmap.createFromFrameBuffer(0,0,bw,bh);
                fog.draw();Pixmap covered=Pixmap.createFromFrameBuffer(0,0,bw,bh);
                for(int cell=0;cell<w*h;cell++) {
                    boolean seen=Dungeon.level.heroFOV[cell];
                    if(!seen&&(Dungeon.level.visited[cell]||Dungeon.level.mapped[cell]))continue;
                    int left=(cell%w*16+pan[0])*zoom,top=(cell/w*16+pan[1])*zoom;
                    for(int y=0;y<16*zoom;y++)for(int x=0;x<16*zoom;x++) {
                        int px=left+x,py=bh-1-top-y;
                        int expected=seen?raw.getPixel(px,py)>>>8:0;
                        if((covered.getPixel(px,py)>>>8)!=expected&&errors.size()<12)
                            errors.add("world/fog mismatch cell="+cell+" zoom="+zoom+" pixel="+x+","+y);
                    }
                }
                raw.dispose();covered.dispose();
                buffer.end();cameras++;
            }
            float coverage=GameGeometry.FOG_SAMPLES_PER_TILE*fog.scale.x/16f;
            if(coverage!=1||fog.scale.y!=fog.scale.x)errors.add("fog cell/world ratio="+coverage);
            if(GameGeometry.FOG_SAMPLES_PER_TILE!=1||fog.scale.x!=16)errors.add("Fog must use one texel per 16-unit cell");
            for(Gizmo g:world)if(g instanceof LightingOverlay) {
                LightingOverlay light=(LightingOverlay)g;
                if(light.width()!=w*16||light.height()!=h*16||light.x!=0||light.y!=0)
                    errors.add("Light-map quad is not aligned to the level's world bounds");
            }
            if(!errors.isEmpty())throw new AssertionError("TEST 47 region="+region+" phase="+phase+" "+errors);
            System.out.println("TEST 47: region="+region+" phase="+phase+" zooms=3 pans=4 cameras="+cameras
                    +" everyCell="+(w*h)+" hiddenPixels="+hidden+" visiblePixels="+visible+" fogTexel/cell=1:1 worldUnits=16 lightQuad=aligned failures=0");
        } finally {
            buffer.dispose();c.matrix=matrix;c.zoom=z;c.scroll.set(sx,sy);c.width=cw;c.height=ch;c.fullScreen=full;
            reset();Gdx.gl.glClearColor(0,0,0,1);
        }
    }

    static void cameraDuringDoor() {
        // Redraw the current transition frame after Camera.update, without resetting
        // custom scripts. This covers intermediate movement, not just settled doors.
        reset();Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);Game.scene().draw();
        LightingOverlay.WallLightScript shader=Script.use(LightingOverlay.WallLightScript.class);
        FloatBuffer actual=BufferUtils.newFloatBuffer(16);
        Gdx.gl.glGetUniformfv(shader.handle(),shader.uCamera.location(),actual);
        for(int i=0;i<16;i++)if(Math.abs(actual.get(i)-Camera.main.matrix[i])>0.00001f)
            throw new AssertionError("TEST 47 stale camera during door transition at matrix component "+i);
    }

    /** Reproduce a visible wall back whose torch faces an unseen room. */
    private static void torchFaces(List<Gizmo> world, int region) {
        com.shatteredpixel.shatteredpixeldungeon.levels.Level level=Dungeon.level;
        int cell=level.length()/2,front=cell+level.width();
        int wall=level.map[cell],floor=level.map[front];
        boolean sourceFov=level.heroFOV[cell],frontFov=level.heroFOV[front];
        boolean visited=level.visited[front],mapped=level.mapped[front];
        boolean enhanced=SPDSettings.enhancedEffects(),lighting=SPDSettings.dynamicLighting();
        float elapsed=Game.elapsed;Camera c=Camera.main;
        float[] matrix=c.matrix.clone();float z=c.zoom,sx=c.scroll.x,sy=c.scroll.y;
        int cw=c.width,ch=c.height;boolean full=c.fullScreen;
        FrameBuffer buffer=new FrameBuffer(Pixmap.Format.RGBA8888,96,96,false);
        Group torches=new Group();torches.camera=c;
        level.map[cell]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL_DECO;
        level.map[front]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY;
        torches.add(new com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.Torch(cell));
        torches.add(new com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel.Torch(cell));
        try {
            c.fullScreen=true;c.zoom=2;c.width=c.height=48;
            c.scroll.set(cell%level.width()*16-16,cell/level.width()*16-16);
            Arrays.fill(c.matrix,0);c.matrix[0]=2f/48;c.matrix[5]=-2f/48;c.matrix[10]=c.matrix[15]=1;
            c.matrix[12]=-1-c.scroll.x*c.matrix[0];c.matrix[13]=1-c.scroll.y*c.matrix[5];
            Game.elapsed=.2f;
            for(boolean enhancedMode:new boolean[]{false,true})for(boolean lightMode:new boolean[]{false,true}) {
                SPDSettings.enhancedEffects(enhancedMode);SPDSettings.dynamicLighting(lightMode);
                for(int state=0;state<8;state++) {
                    level.heroFOV[cell]=state!=0&&state!=5;
                    level.heroFOV[front]=state>=4;
                    level.visited[front]=state==2;level.mapped[front]=state==3;
                    level.map[front]=state==6?com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL:
                            state==7?com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR:
                            com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY;
                    torches.update();
                    torchPixels(buffer,torches,state==4,"state="+state+" enhanced="+enhancedMode+" lighting="+lightMode);
                    if(state==4) {
                        level.heroFOV[front]=false; // Actor FOV changes after the animation update.
                        torchPixels(buffer,torches,false,"FOV changed before draw");
                    }
                }
            }
            // The same hidden fixture must not contribute a separate pool of light.
            SPDSettings.dynamicLighting(true);level.heroFOV[cell]=true;level.heroFOV[front]=false;
            level.map[front]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY;
            for(Gizmo g:world)if(g instanceof LightingOverlay) {
                buffer.begin();
                try {
                    level.map[cell]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;g.draw();
                    int[] absent=((int[])RecoveryChecks.field(g,"pixels")).clone();
                    level.map[cell]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL_DECO;g.draw();
                    if(!Arrays.equals(absent,(int[])RecoveryChecks.field(g,"pixels")))
                        throw new AssertionError("Unseen torch face leaks a light-map glow");
                } finally {buffer.end();}
            }
            System.out.println("TEST 47 TORCH FACES: region="+region+" painted/legacy x dynamic/halo; unseen, back, remembered, mapped, exposed, blocked, doorway and draw-time FOV; hidden light unchanged; failures=0");
        } finally {
            torches.destroy();buffer.dispose();Game.elapsed=elapsed;
            level.map[cell]=wall;level.map[front]=floor;level.heroFOV[cell]=sourceFov;level.heroFOV[front]=frontFov;
            level.visited[front]=visited;level.mapped[front]=mapped;
            SPDSettings.enhancedEffects(enhanced);SPDSettings.dynamicLighting(lighting);
            c.matrix=matrix;c.zoom=z;c.scroll.set(sx,sy);c.width=cw;c.height=ch;c.fullScreen=full;
            reset();Gdx.gl.glClearColor(0,0,0,1);
        }
    }

    private static void torchPixels(FrameBuffer buffer, Group torches, boolean expected, String state) {
        buffer.begin();Pixmap pixels=null;
        try {
            reset();Gdx.gl.glClearColor(0,0,0,1);Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
            torches.draw();pixels=Pixmap.createFromFrameBuffer(0,0,96,96);int lit=0;
            for(int y=0;y<96;y++)for(int x=0;x<96;x++)if((pixels.getPixel(x,y)>>>8)!=0)lit++;
            if((lit>0)!=expected)throw new AssertionError("Torch visibility "+state+": litPixels="+lit+" expected="+expected);
        } finally {if(pixels!=null)pixels.dispose();buffer.end();}
    }

    private static void reset() {
        Texture.clear();NoosaScript.get().resetCamera();NoosaScriptNoLighting.get().resetCamera();
        Gdx.gl.glDisable(Gdx.gl.GL_SCISSOR_TEST);Blending.useDefault();
    }
}
