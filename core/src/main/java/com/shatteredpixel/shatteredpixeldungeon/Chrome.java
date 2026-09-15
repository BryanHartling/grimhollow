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

package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.noosa.NinePatch;

public class Chrome {

	public enum  Type {
		TOAST,
		TOAST_TR,
		TOAST_TR_HEAVY,
		TOAST_WHITE,
		WINDOW,
		WINDOW_SILVER,
		RED_BUTTON,
		GREY_BUTTON,
		GREY_BUTTON_TR,
		TAG,
		GEM,
		SCROLL,
		TAB_SET,
		TAB_SELECTED,
		TAB_UNSELECTED,
		BLANK
	}
	
	public static NinePatch get( Type type ) {
		String Asset = Assets.Interfaces.CHROME;
		switch (type) {
		case WINDOW:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 0, 0, 20, 20, 6 );
		case WINDOW_SILVER:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 86, 0, 22, 22, 7 );
		case TOAST:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 20, 0, 9, 9, 4 );
		case TOAST_TR:
		case GREY_BUTTON_TR:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 20, 9, 9, 9, 4 );
		case TOAST_TR_HEAVY:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 29, 9, 9, 9, 4 );
		case TOAST_WHITE:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 29, 0, 9, 9, 4 );
		case RED_BUTTON:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 38, 0, 6, 6, 2 );
		case GREY_BUTTON:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 38, 6, 6, 6, 2 );
		case TAG:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 22, 18, 16, 14, 3 );
		case GEM:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 0, 32, 32, 32, 13 );
		case SCROLL:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 32, 32, 32, 32, 5, 11, 5, 11 );
		case TAB_SET:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 64, 0, 20, 20, 6 );
		case TAB_SELECTED:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 65, 22, 8, 13, 3, 7, 3, 5 );
		case TAB_UNSELECTED:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 75, 22, 8, 13, 3, 7, 3, 5 );
		case BLANK:
			return com.shatteredpixel.shatteredpixeldungeon.ui.PaintedInterface.patch( Asset, 45, 0, 1, 1, 0, 0, 0, 0 );
		default:
			return null;
		}
	}
}
