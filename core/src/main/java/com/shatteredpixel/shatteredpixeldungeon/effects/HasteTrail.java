// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

/** Two short brush wisps at the boots, only during accelerated movement. */
public final class HasteTrail {
    private final Image[] wisps={new Image(),new Image()};
    private final HeroSprite hero;
    private float lastX,lastY,dx,dy,fade;
    public HasteTrail(HeroSprite hero){
        this.hero=hero;lastX=hero.x;lastY=hero.y;
        for(Image w:wisps){ParticleArt.ray(w,2);w.logicalSize(7,1.2f);w.origin.set(7,.6f);w.hardlight(0xB8D4DC);}
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
        if(active() && hero.isMoving && distance>.002f && distance<12){dx=x/distance;dy=y/distance;fade=.16f;}
        else fade=Math.max(0,fade-Game.elapsed);
    }
    public void draw(){
        if(fade<=0 || !active() || !hero.visible || !EnhancedEffects.enabled())return;
        RectF body=hero.visibleBounds();
        float footX=(body.left+body.right)/2,footY=body.bottom-1;
        for(int i=0;i<2;i++){
            Image w=wisps[i];float side=i==0?-1.6f:1.6f;
            w.camera=hero.camera();w.angle=(float)Math.toDegrees(Math.atan2(dy,dx));
            w.x=footX-dx*(1+i)-dy*side-w.origin.x;
            w.y=footY-dy*(1+i)+dx*side-w.origin.y;
            w.alpha(hero.am*.46f*fade/.16f);
            w.draw();
        }
    }
    public void destroy(){for(Image w:wisps)w.destroy();}
}
