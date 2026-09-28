// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
import com.watabou.noosa.TextureFilm;

public class BroodmotherSprite extends MobSprite {
    @Override public float visualFootprint() { return 26; }
    public BroodmotherSprite() {
        texture("sprites/expedition_spider.png");
        TextureFilm frames = new TextureFilm(texture, 512, 512);
        idle = new Animation(1, true); idle.frames(frames, 0);
        run = idle.clone();
        attack = new Animation(10, false); attack.frames(frames, 1, 2, 0);
        zap = attack.clone();
        die = new Animation(8, false); die.frames(frames, 3);
        play(idle);
    }
}
