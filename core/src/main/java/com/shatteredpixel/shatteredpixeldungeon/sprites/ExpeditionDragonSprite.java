// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.watabou.noosa.TextureFilm;

/** Independent animation controller; visual component replaces the interim painted atlas. */
public class ExpeditionDragonSprite extends MobSprite {
    public ExpeditionDragonSprite() {
        texture(Assets.Sprites.DM300);
        TextureFilm frames = GameGeometry.characterFilm(texture, 25, 22);
        idle = new Animation(1, true); idle.frames(frames, 0);
        run = new Animation(8, true); run.frames(frames, 0, 2);
        attack = new Animation(12, false); attack.frames(frames, 3, 4, 5);
        zap = attack.clone();
        die = new Animation(8, false); die.frames(frames, 0);
        play(idle);
    }
}
