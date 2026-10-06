// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.watabou.noosa.TextureFilm;
public class ColeSprite extends MobSprite {
    @Override public float visualFootprint(){return 22;}
    public ColeSprite(){
        texture("sprites/bounty_cole.png");TextureFilm film=GameGeometry.characterFilm(texture,32,32);
        idle=new Animation(1,true);idle.frames(film,0);run=idle.clone();
        attack=new Animation(8,false);attack.frames(film,0,0);zap=attack.clone();die=attack.clone();
        play(idle);
    }
    @Override public void zap(int cell){
        super.zap(cell);
        ((MissileSprite)parent.recycle(MissileSprite.class)).reset(this,cell,
            ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole)ch).projectile(),
            ()->((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole)ch).onZapComplete());
    }
    @Override public void onComplete(Animation anim){if(anim==zap)idle();super.onComplete(anim);}
}
