// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.watabou.noosa.TextureFilm;

/** Independent, steady flight pose with painted action silhouettes. */
public class ExpeditionDragonSprite extends MobSprite {
    @Override public float visualFootprint() { return 40; }
    private TextureFilm poses;
    @Override public void update() {
        super.update();
        if (ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon && !isMoving && curAnim == idle) {
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon dragon = (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon) ch;
            frame(poses.get(dragon.pending == com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon.Attack.BREATH ? 2
                    : dragon.pending == com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon.Attack.WINGBEAT ? 1 : 0));
        }
    }
    public ExpeditionDragonSprite() {
        texture("sprites/expedition_dragon.png");
        TextureFilm frames = poses = com.shatteredpixel.shatteredpixeldungeon.GameGeometry.characterFilm(texture, 64, 64);
        idle = new Animation(1, true); idle.frames(frames, 0);
        run = new Animation(8, true); run.frames(frames, 0);
        attack = new Animation(12, false); attack.frames(frames, 0, 2, 0);
        zap = attack.clone();
        die = new Animation(8, false); die.frames(frames, 3);
        play(idle);
    }
}
