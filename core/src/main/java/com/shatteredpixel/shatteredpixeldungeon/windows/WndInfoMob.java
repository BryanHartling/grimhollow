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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ui.Component;

public class WndInfoMob extends WndTitledMessage {
	
	public WndInfoMob( Mob mob ) {

		super( new MobTitle( mob ), mob.info() );
		((MobTitle)titlebar).artworkButton.givePointerPriority();

		if (mob.isDirectableAlly()) {
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton direct = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton(Messages.get(this, "direct")) {
				@Override protected void onClick() {
					hide();
					com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell(new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener() {
						public void onSelect(Integer cell) { if (cell != null) mob.directTocell(cell); }
						public String prompt() { return Messages.get(WndInfoMob.class, "direct_prompt"); }
					});
				}
			};
			direct.setRect(0, height + 3, width, 20);
			add(direct);
			resize(width, height + 23);
		}
		
	}
	
    @Override protected float targetHeight(){
        // Reserve the command row before sizing a long ally description.
        return super.targetHeight()-23;
    }
    private static class MobTitle extends Component {

		private static final int GAP	= 2;
		
		private CharSprite image;
		private RenderedTextBlock name;
		private HealthBar health;
		private BuffIndicator buffs;
		private com.shatteredpixel.shatteredpixeldungeon.ui.ArtworkButton artworkButton;
		
		public MobTitle( Mob mob ) {
			
			name = PixelScene.renderTextBlock( Messages.titleCase( mob.name() ), 9 );
			name.hardlight( TITLE_COLOR );
			add( name );
			
			image = mob.sprite();
			add( image );

			health = new HealthBar();
			health.level(mob);
			if (!Char.hasProp(mob, Char.Property.OBJECT)) add( health );

			buffs = new BuffIndicator( mob, false );
			if (!Char.hasProp(mob, Char.Property.OBJECT)) add( buffs );
			artworkButton = new com.shatteredpixel.shatteredpixeldungeon.ui.ArtworkButton(() -> image, () -> name.text());
			add(artworkButton);
		}
		
		@Override
		protected void layout() {
			
			image.x = 0;
			image.y = Math.max( 0, name.height() + health.height() - image.height() );

			float imageColumn = Math.max(24, image.width() + 8);
			artworkButton.setRect(0, image.y, imageColumn, Math.max(24, image.height()));
			float w = width - imageColumn - GAP;

			name.maxWidth((int)w);
			name.setPos(x + imageColumn + GAP,
					image.height() > name.height() ? y +(image.height() - name.height()) / 2 : y);

			health.setRect(imageColumn + GAP, name.bottom() + GAP, w, health.height());

			buffs.maxBuffs = 50; //infinite, effectively
			buffs.setRect(name.right(), name.bottom() - BuffIndicator.SIZE_SMALL-2, w - name.width(), 8);

			//If buff bar doesn't have enough room, move it below
			if (!buffs.allBuffsVisible()){
				buffs.setRect(0, health.bottom(), width, 8);
				height = Math.max(artworkButton.bottom(), buffs.bottom());
			} else {
				height = Math.max(artworkButton.bottom(), health.bottom());
			}
		}
	}
}
