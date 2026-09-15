// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** Floor-bound control; the original enemy object keeps its attacks, art, stats and save identity. */
public class PsychicDomination extends AllyBuff {
    private int ownerID;
    private boolean canEnthrall, permanent;
    private final SharedWill shared = new SharedWill();

    { type = buffType.NEGATIVE; announced = true; }

    public PsychicDomination configure(Hero hero, int crystalLevel) {
        ownerID = hero.id();
        canEnthrall = crystalLevel >= 8;
        spend(15);
        return this;
    }

    public boolean isPermanent() { return permanent; }
    public boolean ownedBy(Hero hero) { return hero != null && ownerID == hero.id(); }

    @Override public boolean attachTo(Char target) {
        if (!(target instanceof Mob) || Char.hasProp(target, Char.Property.BOSS)) return false;
        if (!super.attachTo(target)) return false;
        ((Mob) target).enableDirectableAI();
        return true;
    }

    public void share(Hero hero) { if (ownedBy(hero)) shared.update(target, hero); }

    public static void releasePermanent(Hero hero, Char except) {
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            PsychicDomination control = mob.buff(PsychicDomination.class);
            if (mob != except && control != null && control.permanent && control.ownedBy(hero)) control.detach();
        }
    }

    public static Mob enthralled(Hero hero) {
        for (Mob mob : Dungeon.level.mobs) {
            PsychicDomination control = mob.buff(PsychicDomination.class);
            if (mob.isAlive() && control != null && control.permanent && control.ownedBy(hero)) return mob;
        }
        return null;
    }

    @Override public boolean act() {
        if (!target.isAlive() || (!permanent && !canEnthrall)) {
            detach();
        } else {
            if (!permanent) {
                releasePermanent(Dungeon.hero, target);
                permanent = true;
            }
            diactivate();
        }
        return true;
    }

    @Override public void detach() {
        shared.clear(target);
        super.detach();
        if (target.isAlive() && target.buffs(AllyBuff.class).isEmpty()) {
            target.alignment = Char.Alignment.ENEMY;
            ((Mob) target).disableDirectableAI();
            ((Mob) target).aggro(null);
        }
    }

    @Override public int icon() { return permanent ? BuffIndicator.HEART : BuffIndicator.AMOK; }
    @Override public String name() { return Messages.get(this, permanent ? "enthralled" : "name"); }
    @Override public String desc() { return Messages.get(this, permanent ? "enthralled_desc" : "desc", dispTurns(visualcooldown())); }
    @Override public String iconTextDisplay() { return permanent ? "" : Integer.toString((int) visualcooldown()); }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("owner", ownerID); b.put("can_enthrall", canEnthrall); b.put("permanent", permanent); shared.store(b);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        ownerID = b.getInt("owner"); canEnthrall = b.getBoolean("can_enthrall"); permanent = b.getBoolean("permanent"); shared.restore(b);
    }
}
