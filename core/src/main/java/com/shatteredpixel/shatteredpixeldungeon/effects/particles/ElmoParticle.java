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

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;

import com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects;

public class ElmoParticle extends PixelParticle.Shrinking {
	
	public static final Emitter.Factory FACTORY = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((ElmoParticle)emitter.recycle( ElmoParticle.class )).reset( x, y );
            EnhancedEffects.Ember.emit(emitter,index,x,y);
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};
	
	private boolean enhanced;
	public ElmoParticle() {
		super();
		
		color( 0x22EE66 );
		lifespan = 0.6f;
		
		acc.set( 0, -80 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.x = x;
		this.y = y;
		
		left = lifespan;
        enhanced=EnhancedEffects.enabled();
        texture(enhanced?EnhancedEffects.ATLAS:com.watabou.gltextures.TextureCache.createSolid(0xFFFFFFFF));
        logicalSize(1,1);if(enhanced)resetColor();else color(0x22EE66);
        acc.set(0,enhanced?-120:-80);
		
		size = 4;
		speed.set( 0 );
	}
	
	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
        if(enhanced){EnhancedEffects.frame(this,EnhancedEffects.Style.GREEN_FLAME,(int)((1-p)*6),1,1);
            speed.x=(float)Math.sin((lifespan-left)*19+x*.17)*3;}
		am = p > 0.8f ? (1 - p) * 5 : 1;
	}
}