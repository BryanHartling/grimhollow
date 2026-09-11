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
    private static LightingOverlay active;
    private final LightMap map;
    private final String cacheKey;
    private int previousHash;
    private boolean initialized;
    public int rebuilds;

    public LightingOverlay() {
        active = this;
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
        boolean lavaSurface = Dungeon.level.waterTex().equals(com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.WATER_HALLS);
        if (lavaSurface) {
            for (int cell=0;cell<Dungeon.level.length();cell++)
                if (Dungeon.level.water[cell] && Dungeon.level.heroFOV[cell]) source(cell,1.25f,.38f,.25f,.05f);
        }
        for (int cell=0; cell<Dungeon.level.length(); cell++) {
            if (Dungeon.level.heroFOV[cell] && Dungeon.level.map[cell] == Terrain.WALL_DECO)
                source(cell, Dungeon.depth >= 16 && Dungeon.depth <= 20 ? 4 : 3, .38f, .24f, .08f);
        }
        for (Blob blob : Dungeon.level.blobs.values()) {
            if (blob.cur == null || blob.volume <= 0) continue;
            String type = blob.getClass().getSimpleName();
            for (int cell=0; cell<blob.cur.length; cell++) {
                if (blob.cur[cell] <= 0 || !Dungeon.level.heroFOV[cell]) continue;
                if (blob instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom.EternalFire)
                    source(cell, 2, .05f, .26f, .10f);
                else if (type.contains("Fire")) source(cell, 2, .48f, .20f, .04f);
                else if (type.contains("Electric")) source(cell, 2, .06f, .35f, .4f);
                else if (type.contains("Freez") || type.contains("Frost")) source(cell, 1, .14f, .3f, .35f);
                else if (type.contains("Corro") || type.contains("Toxic")) source(cell, 2, .12f, .32f, .04f);
            }
        }
        Pixmap bitmap = texture.bitmap;
        bitmap.setBlending(Pixmap.Blending.None);
        for (int y=0; y<map.height; y++) for (int x=0; x<map.width; x++) {
            int cell = x/map.samples + (y/map.samples)*Dungeon.level.width();
            // The rendered lava already carries its emission. Its light spills
            // onto neighbours, while the emitting surface keeps that radiance.
            bitmap.drawPixel(x, y, lavaSurface && Dungeon.level.water[cell] ? 0xFFFFFFFF : map.rgba(x,y));
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
        if (active == this) active = null;
        super.destroy();
        TextureCache.remove(cacheKey);
    }

    /** Walls draw after actors for occlusion, so sample the same map in their own pass. */
    public static NoosaScript walls() {
        if (active == null || !SPDSettings.dynamicLighting())
            return NoosaScriptNoLighting.get();
        WallLightScript script = com.watabou.glscripts.Script.use(WallLightScript.class);
        Gdx.gl.glActiveTexture(Gdx.gl.GL_TEXTURE1);
        Gdx.gl.glBindTexture(Gdx.gl.GL_TEXTURE_2D, active.texture.id);
        Gdx.gl.glActiveTexture(Gdx.gl.GL_TEXTURE0);
        Gdx.gl.glUniform1i(script.uniform("uLight").location(), 1);
        script.uniform("uLevelSize").value2f(Dungeon.level.width()*16, Dungeon.level.height()*16);
        return script;
    }

    public static class WallLightScript extends NoosaScriptNoLighting {
        @Override protected String shader() {
            return "uniform mat4 uCamera; uniform mat4 uModel; uniform vec2 uLevelSize;\n"
                + "attribute vec4 aXYZW; attribute vec2 aUV; varying vec2 vUV; varying vec2 vLight;\n"
                + "void main(){ vec4 world=uModel*aXYZW; gl_Position=uCamera*world; vUV=aUV; vLight=world.xy/uLevelSize; }\n//\n"
                + "#ifdef GL_ES\nprecision mediump float;\n#endif\n"
                + "varying vec2 vUV; varying vec2 vLight; uniform sampler2D uTex; uniform sampler2D uLight;\n"
                + "void main(){ vec4 color=texture2D(uTex,vUV); color.rgb*=texture2D(uLight,vLight).rgb; gl_FragColor=color; }\n";
        }
    }
}
