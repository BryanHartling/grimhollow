// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Image;

/** Screen-edge feedback independent of world texture resolution. */
public final class HealthVignette extends Image {
    public HealthVignette() { super("effects/health_vignette.png"); camera=PixelScene.uiCamera; }
    public static float opacity(float fraction) { return Math.min(.6f,Math.max(0,(.3f-fraction)*3)); }
    @Override public void update() {
        super.update();
        alpha(Dungeon.hero==null ? 0 : opacity((float)Dungeon.hero.HP/Math.max(1,Dungeon.hero.HT)));
        width=camera.width; height=camera.height;
    }
}
