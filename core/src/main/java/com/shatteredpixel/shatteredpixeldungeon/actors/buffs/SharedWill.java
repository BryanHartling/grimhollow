// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Bundle;

/** Tracks only the buffs lent by Shared Will, preserving the creature's own effects. */
public class SharedWill {
    private boolean bless, haste, barkskin;

    public void update(Char target, Hero hero) {
        if (!hero.hasTalent(Talent.SHARED_WILL)) return;
        if (hero.buff(Bless.class) != null) {
            bless |= target.buff(Bless.class) == null;
            if (bless) Buff.prolong(target, Bless.class, 2);
        } else if (bless) { Buff.detach(target, Bless.class); bless = false; }
        if (hero.pointsInTalent(Talent.SHARED_WILL)>=2 && hero.buff(Haste.class) != null) {
            haste |= target.buff(Haste.class) == null;
            if (haste) Buff.prolong(target, Haste.class, 2);
        } else if (haste) { Buff.detach(target, Haste.class); haste = false; }
        if (hero.pointsInTalent(Talent.SHARED_WILL)>=3 && Barkskin.currentLevel(hero) > 0) {
            barkskin |= target.buff(Barkskin.class) == null;
            if (barkskin) Buff.affect(target, Barkskin.class).setForDuration(Barkskin.currentLevel(hero), 2);
        } else if (barkskin) { Buff.detach(target, Barkskin.class); barkskin = false; }
    }

    public void clear(Char target) {
        if (bless) Buff.detach(target, Bless.class);
        if (haste) Buff.detach(target, Haste.class);
        if (barkskin) Buff.detach(target, Barkskin.class);
        bless = haste = barkskin = false;
    }

    public void store(Bundle b) {
        b.put("shared_bless", bless); b.put("shared_haste", haste); b.put("shared_barkskin", barkskin);
    }

    public void restore(Bundle b) {
        bless = b.getBoolean("shared_bless"); haste = b.getBoolean("shared_haste"); barkskin = b.getBoolean("shared_barkskin");
    }
}
