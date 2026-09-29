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
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.effects.ParticleArt;
import com.watabou.utils.Random;

/** Soft garden motes, with bounded positive scale and no stretched solid-pixel shaft. */
public class ShaftParticle extends Image {
    public static final Emitter.Factory FACTORY=new Emitter.Factory(){
        @Override public void emit(Emitter emitter,int index,float x,float y){
            ((ShaftParticle)emitter.recycle(ShaftParticle.class)).reset(x,y);
        }
        @Override public boolean lightMode(){return true;}
    };
    private float lifespan,left,startX,startY,phase;
    public ShaftParticle(){ParticleArt.frame(this,ParticleArt.MOTE,4,4);originToCenter();hardlight(0xD9E9B5);}
    public void reset(float x,float y){
        revive();startX=x;startY=y;lifespan=left=Random.Float(2.4f,3.6f);phase=Random.Float(6.283f);alpha(0);
    }
    @Override public void update(){
        super.update();left-=Game.elapsed;if(left<=0){kill();return;}
        float progress=1-left/lifespan;
        x=startX+(float)Math.sin(phase+progress*4)*2-width/2;
        y=startY-progress*8-height/2;
        scale.set(.55f+.25f*(float)Math.sin(Math.PI*progress));
        alpha(.6f*(float)Math.sin(Math.PI*progress));
    }
}
