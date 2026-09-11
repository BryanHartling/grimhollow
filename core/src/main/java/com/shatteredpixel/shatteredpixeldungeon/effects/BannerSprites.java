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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Image;

public class BannerSprites {

	public enum  Type {
		TITLE_PORT,
		TITLE_GLOW_PORT,
		TITLE_LAND,
		TITLE_GLOW_LAND,
		BOSS_SLAIN,
		GAME_OVER,
	}

	public static Image get( Type type ) {
		Image icon = new Image( Assets.Interfaces.BANNERS );
		switch (type) {
			case TITLE_PORT:
				frame(icon, 0, 0, 139, 100);
				break;
			case TITLE_GLOW_PORT:
				frame(icon, 139, 0, 278, 100);
				break;
			case TITLE_LAND:
				frame(icon, 0, 100, 240, 157);
				break;
			case TITLE_GLOW_LAND:
				frame(icon, 240, 100, 480, 157);
				break;
			case BOSS_SLAIN:
				frame(icon, 0, 157, 127, 225);
				break;
			case GAME_OVER:
				frame(icon, 128, 157, 256, 192);
				break;
		}
		return icon;
	}
    private static void frame(Image icon, int x, int y, int right, int bottom) {
        icon.frame(icon.texture.uvRect(x*2, y*2, right*2, bottom*2));
        icon.logicalSize(right-x, bottom-y);
    }
}
