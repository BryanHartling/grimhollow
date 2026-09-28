// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror;
import com.watabou.noosa.TextureFilm;

public class LurkingHorrorSprite extends MobSprite {
    public LurkingHorrorSprite() {
        texture("sprites/lurking_horror.png");
        TextureFilm frames=GameGeometry.characterFilm(texture,32,32);
        idle=new Animation(1,true); idle.frames(frames,0);
        run=new Animation(8,true); run.frames(frames,1);
        attack=new Animation(10,false); attack.frames(frames,0,2,0);
        zap=attack.clone(); die=new Animation(8,false); die.frames(frames,3);
        play(idle);
    }
    @Override public float visualFootprint() { return 18; }
    @Override public void update() {
        if(ch instanceof LurkingHorror && ch.sprite==this && ch.isAlive())
            visible=((LurkingHorror)ch).visibleToHero() && Dungeon.level.heroFOV[ch.pos];
        super.update();
    }
}
