// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.watabou.noosa.TextureFilm;
public class BountyBoardSprite extends MobSprite {
    @Override public float visualFootprint(){return 20;}
    public BountyBoardSprite(){
        texture("sprites/bounty_board.png");TextureFilm film=GameGeometry.characterFilm(texture,32,32);
        idle=new Animation(1,true);idle.frames(film,0);run=idle.clone();attack=idle.clone();zap=idle.clone();die=idle.clone();play(idle);
    }
}
