// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects.particles;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;

/** Shared palette-constrained emissive effect for necrotic status effects. */
import com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects;
public final class NecroticParticle extends PixelParticle {
    public static final Emitter.Factory FACTORY=new Emitter.Factory() {
        @Override public void emit(Emitter emitter,int index,float x,float y) {
            NecroticParticle p=(NecroticParticle)emitter.recycle(NecroticParticle.class);
            p.enhanced=EnhancedEffects.enabled();p.texture(p.enhanced?EnhancedEffects.ATLAS:com.watabou.gltextures.TextureCache.createSolid(0xFFFFFFFF));p.logicalSize(1,1);p.revive(); p.x=x; p.y=y; p.left=p.lifespan=.6f;
            p.speed.set((index%3-1)*2,-4); p.color(0x7BB33B); p.size(2);if(p.enhanced)p.resetColor();
        }
        @Override public boolean lightMode() { return true; }
    };
    private boolean enhanced;
    @Override public void update() { super.update();if(enhanced)EnhancedEffects.frame(this,EnhancedEffects.Style.NECROTIC,(int)((1-left/lifespan)*8),1,1); alpha(left/lifespan); }
}
