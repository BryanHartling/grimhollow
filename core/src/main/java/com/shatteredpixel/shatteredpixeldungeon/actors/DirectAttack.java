// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors;

/** Keep the resistance/damage source class intact while attributing an immediate spell hit. */
public final class DirectAttack {
    private DirectAttack() {}
    private static final ThreadLocal<Char> caster = new ThreadLocal<>();
    public static Char owner(Object source) { return source instanceof Char ? (Char)source : caster.get(); }
    public static void apply(Char target, int damage, Object source, Char owner) {
        Char previous = caster.get(); caster.set(owner);
        try {
            if (owner instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob && !(source instanceof Char))
                damage = Math.round(damage * ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)owner).wantedDamage);
            target.damage(damage, source);
        }
        finally { if (previous == null) caster.remove(); else caster.set(previous); }
    }
}
