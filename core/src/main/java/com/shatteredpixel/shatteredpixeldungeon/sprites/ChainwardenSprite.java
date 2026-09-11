// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
import com.watabou.noosa.TextureFilm;
public class ChainwardenSprite extends TenguSprite {
    public ChainwardenSprite(){
        super();texture("sprites/chainwarden.png");
		TextureFilm frames = com.shatteredpixel.shatteredpixeldungeon.GameGeometry.characterFilm(texture, 14, 16 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1 );
		
		run = new Animation( 15, false );
		run.frames( frames, 2, 3, 4, 5, 0 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 6, 7, 7, 0 );
		
		zap = attack.clone();
		
		die = new Animation( 8, false );
		die.frames( frames, 8, 9, 10, 10, 10, 10, 10, 10 );
		
		play( run );
		isMoving = true;
    }
}
