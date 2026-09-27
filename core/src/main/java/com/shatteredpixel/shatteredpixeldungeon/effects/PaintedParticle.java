// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.noosa.Image;
import com.watabou.noosa.particles.PixelParticle;

/** Smooth authored substitute for every core solid-pixel emitter; legacy mode is unchanged. */
public class PaintedParticle extends PixelParticle {
    private Image painted;
    private final int motif=motif(getClass().getSimpleName());
    public static int motif(String family){
        if(family.equals("LeafParticle"))return ParticleArt.GRASS;
        if(family.contains("Shadow")||family.contains("Purple")||family.contains("Wind"))return ParticleArt.CURSE;
        if(family.contains("Spark"))return ParticleArt.SPARK;
        if(family.contains("Earth")||family.contains("Pitfall"))return ParticleArt.STONE;
        if(family.contains("Blood")||family.contains("Flow")||family.contains("Poison")||family.contains("Corrosion"))return ParticleArt.DROP;
        if(family.contains("Wool")||family.contains("Snow"))return ParticleArt.FROST;
        if(family.contains("Web"))return ParticleArt.WEB;
        if(family.contains("Smoke"))return ParticleArt.MIST;
        if(family.contains("Flame")||family.contains("Elmo"))return ParticleArt.FLAME;
        if(family.contains("Shaft"))return ParticleArt.EMBER;
        if(family.contains("White")||family.contains("Rainbow"))return ParticleArt.GLINT;
        return ParticleArt.MOTE;
    }
    @Override public void draw(){
        // Existing painted fire, Tengu, necrotic and ember particles keep their
        // animation/batching path. Only the legacy solid texture is substituted.
        if(!EnhancedEffects.enabled()||texture.width!=1||texture.height!=1){super.draw();return;}
        if(painted==null){painted=new Image();ParticleArt.frame(painted,motif,1,1);}
        ParticleArt.appearance(this,painted);
        float expansion=motif==ParticleArt.GRASS?1.65f:motif==ParticleArt.SPARK?2.5f:1.35f;
        painted.scale.scale(expansion);
        painted.x-=(expansion-1)*scale.x*.5f;painted.y-=(expansion-1)*scale.y*.5f;
        if(motif==ParticleArt.GRASS)painted.angle+=Math.sin((lifespan-left)*6)*35;
        if(motif==ParticleArt.CURSE){painted.rm=Math.max(.4f,rm);painted.gm=Math.max(.12f,gm);painted.bm=Math.max(.48f,bm);}
        painted.draw();
    }
    @Override public void destroy(){if(painted!=null)painted.destroy();super.destroy();}
    public static class Shrinking extends PaintedParticle {
        @Override public void update(){super.update();size(size*left/lifespan);}
    }
}
