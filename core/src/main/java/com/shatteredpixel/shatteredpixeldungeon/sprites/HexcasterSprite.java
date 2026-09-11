// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
import com.watabou.noosa.TextureFilm;
public class HexcasterSprite extends WarlockSprite {
    public HexcasterSprite(){
        super();texture("sprites/hexcaster.png");
		TextureFilm frames = com.shatteredpixel.shatteredpixeldungeon.GameGeometry.characterFilm(texture, 12, 15 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( 15, true );
		run.frames( frames, 0, 2, 3, 4 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 0, 5, 6 );
		
		zap = attack.clone();
		
		die = new Animation( 15, false );
		die.frames( frames, 0, 7, 8, 8, 9, 10 );
		
		play( idle );
    }
}
