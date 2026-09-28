// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
import com.watabou.noosa.TextureFilm;

public class TreasureHunterSprite extends MobSprite {
    @Override public float visualFootprint() { return 20; }
    public TreasureHunterSprite() {
        texture("sprites/expedition_hunter.png");
        TextureFilm frames = new TextureFilm(texture, 256, 256);
        idle = new Animation(1, true); idle.frames(frames, 0);
        run = idle.clone(); attack = idle.clone(); zap = idle.clone(); die = idle.clone();
        play(idle);
    }
}
