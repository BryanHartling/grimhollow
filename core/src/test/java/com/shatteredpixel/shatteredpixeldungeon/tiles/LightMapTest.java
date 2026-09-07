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
}
