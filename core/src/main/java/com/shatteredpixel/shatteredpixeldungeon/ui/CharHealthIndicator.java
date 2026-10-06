/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

public class CharHealthIndicator extends HealthBar {
	
	private static final int HEIGHT = 1;
	
	private Char target;
	private com.watabou.noosa.Image wantedSeal;
	
	public CharHealthIndicator( Char c ){
		target = c;
		GameScene.add(this);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		height = HEIGHT;
		wantedSeal=new com.watabou.noosa.Image("interfaces/bounty_seals.png");wantedSeal.logicalSize(4,4);add(wantedSeal);wantedSeal.visible=false;
	}
	
	@Override
	public void update() {
		super.update();
		
		if (target != null && target.isAlive() && target.isActive() && target.sprite.visible) {
			CharSprite sprite = target.sprite;
			com.watabou.utils.RectF body = sprite.visibleBounds();
			width = body.width()*(4/6f);
			x = body.left + body.width()/6f;
			y = body.top - 2;
			level( target );
			visible = target.HP < target.HT || target.shielding() > 0 || target.incomingDOT() > 0;
			wantedSeal.visible=target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
				&&((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)target).bountyContract>=0
				&&((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)target).bountyContract<3
				&&target.invisible==0&&com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.heroFOV[target.pos];
			if(wantedSeal.visible){
				wantedSeal.frame(new com.watabou.noosa.TextureFilm(wantedSeal.texture,64,64).get(((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)target).bountyContract));
				wantedSeal.logicalSize(4,4);wantedSeal.x=x+width+1;wantedSeal.y=y-1.5f;visible=true;
			}
		} else {
			visible = false;
			wantedSeal.visible=false;
		}
	}
	
	public void target( Char ch ) {
		if (ch != null && ch.isAlive() && ch.isActive()) {
			target = ch;
		} else {
			target = null;
		}
	}
	
	public Char target() {
		return target;
	}
}
