// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;
import org.junit.Test;
import static org.junit.Assert.*;

public class LightMapTest {
    @Test public void healthVignetteUsesSpecifiedThresholds() {
        assertEquals(0,com.shatteredpixel.shatteredpixeldungeon.effects.HealthVignette.opacity(.3f),.0001f);
        assertEquals(.3f,com.shatteredpixel.shatteredpixeldungeon.effects.HealthVignette.opacity(.2f),.0001f);
        assertEquals(.6f,com.shatteredpixel.shatteredpixeldungeon.effects.HealthVignette.opacity(.1f),.0001f);
        assertEquals(.6f,com.shatteredpixel.shatteredpixeldungeon.effects.HealthVignette.opacity(0),.0001f);
    }
    @Test public void falloffIsQuadraticAndBounded() {
        assertEquals(1, LightMap.falloff(0, 4), 0);
        assertEquals(.25f, LightMap.falloff(2, 4), .00001f);
        assertEquals(0, LightMap.falloff(4, 4), 0);
        assertEquals(0, LightMap.falloff(5, 4), 0);
        assertEquals(0, LightMap.falloff(0, 0), 0);
    }
    @Test public void accumulationClampsAndDoesNotReachOutsideRadius() {
        LightMap map = new LightMap(8, 8, 16);
        map.clear(0);
        int untouched = map.rgba(0,0);
        map.add(4, 4, 2, 10, 10, 10);
        assertEquals(untouched, map.rgba(0,0));
        assertEquals(0xffffffff, map.rgba(64,64));
        map.clear(0);
        assertEquals(untouched, map.rgba(64,64));
    }
    @Test public void sourcesOutsideEdgesAreClippedSafely() {
        LightMap map = new LightMap(1,1,16);
        map.clear(4);
        map.add(-2,-2,4,.5f,.5f,.5f);
        map.add(2,2,4,.5f,.5f,.5f);
        assertEquals(255, map.rgba(0,0) & 255);
    }
    @Test public void tabletGridPreservesSmoothLightField(){
        LightMap fine=new LightMap(12,12,16),coarse=new LightMap(12,12,com.shatteredpixel.shatteredpixeldungeon.GameGeometry.LIGHT_SAMPLES_PER_TILE);
        fine.clear(0);coarse.clear(0);
        for(LightMap m:new LightMap[]{fine,coarse}){m.add(6.5f,6.5f,8,.45f,.31f,.12f);m.add(3.5f,4.5f,3,.38f,.24f,.08f);m.add(9.5f,7.5f,2,.48f,.20f,.04f);}
        float largest=0;
        for(int y=1;y<fine.height-1;y++)for(int x=1;x<fine.width-1;x++){
            float u=(x+.5f)*coarse.samples/fine.samples-.5f,v=(y+.5f)*coarse.samples/fine.samples-.5f;
            int ax=Math.max(0,(int)Math.floor(u)),ay=Math.max(0,(int)Math.floor(v));
            int bx=Math.min(coarse.width-1,ax+1),by=Math.min(coarse.height-1,ay+1);
            float tx=Math.max(0,u-ax),ty=Math.max(0,v-ay);
            for(int shift:new int[]{24,16,8}){
                float a=(coarse.rgba(ax,ay)>>>shift)&255,b=(coarse.rgba(bx,ay)>>>shift)&255;
                float c=(coarse.rgba(ax,by)>>>shift)&255,d=(coarse.rgba(bx,by)>>>shift)&255;
                float interpolated=(a+(b-a)*tx)*(1-ty)+(c+(d-c)*tx)*ty;
                largest=Math.max(largest,Math.abs(interpolated-((fine.rgba(x,y)>>>shift)&255)));
            }
        }
        assertTrue("Light-grid interpolation error "+largest,largest<=10);
        assertEquals(16,fine.width*fine.height/(coarse.width*coarse.height));
    }
}
