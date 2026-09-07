// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects.particles;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;

/** Shared palette-constrained emissive effect for necrotic status effects. */
public final class NecroticParticle extends PixelParticle {
    public static final Emitter.Factory FACTORY=new Emitter.Factory() {
        @Override public void emit(Emitter emitter,int index,float x,float y) {
            NecroticParticle p=(NecroticParticle)emitter.recycle(NecroticParticle.class);
            p.revive(); p.x=x; p.y=y; p.left=p.lifespan=.6f;
            p.speed.set((index%3-1)*2,-4); p.color(0x7BB33B); p.size(2);
        }
        @Override public boolean lightMode() { return true; }
    };
    @Override public void update() { super.update(); alpha(left/lifespan); }
}
