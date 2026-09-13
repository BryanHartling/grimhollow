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
        Camera c=Camera.main;
        float[] matrix=c.matrix.clone();float z=c.zoom,sx=c.scroll.x,sy=c.scroll.y;
        int cw=c.width,ch=c.height;boolean full=c.fullScreen;
        int w=Dungeon.level.width(),h=Dungeon.level.height();
        int bw=(w+8)*16*3,bh=(h+8)*16*3;
        FrameBuffer buffer=new FrameBuffer(Pixmap.Format.RGBA8888,bw,bh,false);
        ArrayList<String> errors=new ArrayList<>();long hidden=0,visible=0;int cameras=0;
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

    private static void reset() {
        Texture.clear();NoosaScript.get().resetCamera();NoosaScriptNoLighting.get().resetCamera();
        Gdx.gl.glDisable(Gdx.gl.GL_SCISSOR_TEST);Blending.useDefault();
    }
}
