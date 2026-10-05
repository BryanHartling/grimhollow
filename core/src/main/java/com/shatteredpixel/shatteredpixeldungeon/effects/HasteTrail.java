// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

/** Two short brush wisps at the boots, only during accelerated movement. */
public final class HasteTrail {
    private final Image[] wisps={new Image(),new Image()};
    private final HeroSprite hero;
    private float lastX,lastY,dx,dy,fade;
    public HasteTrail(HeroSprite hero){
        this.hero=hero;lastX=hero.x;lastY=hero.y;
        for(Image w:wisps){ParticleArt.ray(w,2);w.logicalSize(5,.7f);w.origin.set(5,.35f);}
    }
    public boolean active(){
        if(hero.ch==null || !hero.ch.isAlive())return false;
        Momentum momentum=hero.ch.buff(Momentum.class);
        return hero.ch.buff(Haste.class)!=null || hero.ch.buff(GreaterHaste.class)!=null
                || momentum!=null && momentum.freerunning();
    }
    public void update(){
        float x=hero.x-lastX,y=hero.y-lastY;lastX=hero.x;lastY=hero.y;
        float distance=(float)Math.sqrt(x*x+y*y);
        if(active() && hero.isMoving && distance>.002f && distance<12){dx=x/distance;dy=y/distance;fade=.12f;}
        else fade=Math.max(0,fade-Game.elapsed);
    }
    public void draw(){
        if(fade<=0 || !active() || !hero.visible || !EnhancedEffects.enabled())return;
        float footX=hero.x+hero.width/2,footY=hero.y+hero.height-1;
        for(int i=0;i<2;i++){
            Image w=wisps[i];float side=i==0?-1.6f:1.6f;
            w.camera=hero.camera();w.angle=(float)Math.toDegrees(Math.atan2(dy,dx));
            w.x=footX-dx*(1+i)-dy*side-w.origin.x;
            w.y=footY-dy*(1+i)+dx*side-w.origin.y;
            w.alpha(hero.am*.22f*fade/.12f);w.rm=hero.rm;w.gm=hero.gm;w.bm=hero.bm;
            w.draw();
        }
    }
    public void destroy(){for(Image w:wisps)w.destroy();}
}
