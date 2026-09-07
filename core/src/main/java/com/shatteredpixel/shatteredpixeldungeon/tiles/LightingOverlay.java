// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import java.util.Arrays;

/** One cached low-resolution light texture, one multiplicative draw, no per-light quads. */
public class LightingOverlay extends Image {
    private final LightMap map;
    private final String cacheKey;
    private int previousHash;
    private boolean initialized;
    public int rebuilds;

    public LightingOverlay() {
        map = new LightMap(Dungeon.level.width(), Dungeon.level.height(), GameGeometry.LIGHT_SAMPLES_PER_TILE);
        cacheKey = "Grimhollow-lightmap-" + map.width + "x" + map.height;
        texture(TextureCache.create(cacheKey, map.width, map.height));
        scale.set((float)GameGeometry.WORLD_TILE_SIZE / map.samples);
    }
    @Override protected NoosaScript script() { return NoosaScriptNoLighting.get(); }

    private int stateHash() {
        int hash = 31*Dungeon.hero.pos + Dungeon.hero.viewDistance;
        hash = 31*hash + Dungeon.depth;
        hash = 31*hash + Arrays.hashCode(Dungeon.level.map);
        hash = 31*hash + Arrays.hashCode(Dungeon.level.heroFOV);
        for (Blob blob : Dungeon.level.blobs.values()) {
            hash = 31*hash + blob.getClass().getName().hashCode();
            hash = 31*hash + Arrays.hashCode(blob.cur);
        }
        return hash;
    }
    private void source(int cell, float radius, float r, float g, float b) {
        map.add(cell % Dungeon.level.width() + .5f, cell / Dungeon.level.width() + .5f, radius, r, g, b);
    }
    private void rebuild() {
        map.clear((Dungeon.depth-1)/5);
        source(Dungeon.hero.pos, Dungeon.hero.viewDistance, .45f, .31f, .12f);
        for (int cell=0; cell<Dungeon.level.length(); cell++) {
            if (Dungeon.level.heroFOV[cell] && Dungeon.level.map[cell] == Terrain.WALL_DECO)
                source(cell, 3, .38f, .24f, .08f);
        }
        for (Blob blob : Dungeon.level.blobs.values()) {
            if (blob.cur == null || blob.volume <= 0) continue;
            String type = blob.getClass().getSimpleName();
            for (int cell=0; cell<blob.cur.length; cell++) {
                if (blob.cur[cell] <= 0 || !Dungeon.level.heroFOV[cell]) continue;
                if (type.contains("Fire")) source(cell, 2, .48f, .20f, .04f);
                else if (type.contains("Electric")) source(cell, 2, .06f, .35f, .4f);
                else if (type.contains("Freez") || type.contains("Frost")) source(cell, 1, .14f, .3f, .35f);
                else if (type.contains("Corro") || type.contains("Toxic")) source(cell, 2, .12f, .32f, .04f);
            }
        }
        Pixmap bitmap = texture.bitmap;
        bitmap.setBlending(Pixmap.Blending.None);
        for (int y=0; y<map.height; y++) for (int x=0; x<map.width; x++) {
            int cell = x/map.samples + (y/map.samples)*Dungeon.level.width();
            bitmap.drawPixel(x, y, map.rgba(x,y));
        }
        texture.bitmap(bitmap);
        rebuilds++;
    }
    @Override public void draw() {
        if (!SPDSettings.dynamicLighting()) return;
        int hash = stateHash();
        if (!initialized || hash != previousHash) {
            rebuild(); previousHash = hash; initialized = true;
        }
        Gdx.gl.glBlendFunc(Gdx.gl.GL_DST_COLOR, Gdx.gl.GL_ZERO);
        try { super.draw(); } finally { Blending.setNormalMode(); }
    }
    @Override public void destroy() {
        super.destroy();
        TextureCache.remove(cacheKey);
    }
}
