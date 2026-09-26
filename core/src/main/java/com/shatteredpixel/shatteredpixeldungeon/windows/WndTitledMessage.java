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

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class WndTitledMessage extends Window {

	protected static final int WIDTH_MIN    = 120;
	protected static final int WIDTH_MAX    = 220;
	protected static final int GAP	= 2;
	private com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane description;
	private float descriptionTop,descriptionHeight;

	public WndTitledMessage( Image icon, String title, String message ) {
		
		this( new IconTitle( icon, title ), message );

	}
	
	public WndTitledMessage( Component titlebar, String message ) {

		super();

		int maxWidth = Math.max(80, Math.min(WIDTH_MAX, (int)PixelScene.uiCamera.width - 32));
		int width = Math.min(WIDTH_MIN, maxWidth);

		titlebar.setRect( 0, 0, width, 0 );
		add(titlebar);

		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		if (!useHighlighting()) text.setHightlighting(false);
		text.text( message, width );
		text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
		add( text );

		while (text.bottom() > targetHeight() && width < maxWidth){
			width = Math.min(width + 20, maxWidth);
			titlebar.setRect(0, 0, width, 0);
			text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
			text.maxWidth(width);
		}

		bringToFront(titlebar);

		remove(text);
		descriptionTop=titlebar.bottom()+2*GAP;
		text.setPos(0,0);
		Component content=new Component();content.add(text);
		// Include the last baseline and rounding margin in the scrollable bounds.
		content.setSize(width,(float)Math.ceil(text.bottom())+4);
		description=new com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane(content);
		add(description);
		descriptionHeight=Math.max(18,Math.min(content.height(),Math.min(targetHeight(),PixelScene.uiCamera.height-36)-descriptionTop));
		description.setRect(0,descriptionTop,width,descriptionHeight);
		resize( width, (int)(descriptionTop+descriptionHeight)+2 );
	}
	@Override public void resize(int width,int height){
		super.resize(width,height);
		alignDescription();
	}
	@Override public void offset(int x,int y){
		super.offset(x,y);
		alignDescription();
	}
	private void alignDescription(){
		if(description!=null)description.setRect(0,descriptionTop,width,descriptionHeight);
	}

	protected boolean useHighlighting(){
		return true;
	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}
}
