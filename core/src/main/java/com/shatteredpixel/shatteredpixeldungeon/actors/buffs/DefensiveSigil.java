// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.SkillIcon;
import com.watabou.noosa.Image;

/** A paid, short-lived ward. Recasting restores its strength; it never adds another ward. */
public class DefensiveSigil extends ShieldBuff {
    public static final float DURATION = 6;
    private float duration = DURATION;
    { type = buffType.POSITIVE; shieldUsePriority = 2; }

    public void refresh(int rank) {
        setShield(rank >= 2 ? 10 : 6);
        duration = rank >= 2 ? 10 : 6;
        // Use Actor's scheduled expiry, which already survives save/load and floor changes.
        spendConstant(duration - cooldown());
        BuffIndicator.refreshHero();
    }

    @Override public void storeInBundle(com.watabou.utils.Bundle b){super.storeInBundle(b);b.put("sigil_duration",duration);}
    @Override public void restoreFromBundle(com.watabou.utils.Bundle b){super.restoreFromBundle(b);duration=b.contains("sigil_duration")?b.getFloat("sigil_duration"):DURATION;}
    @Override public boolean act() { detach(); return true; }
    @Override public int icon() { return BuffIndicator.ARMOR; }
    @Override public void tintIcon(Image icon) {
        float w = icon.width, h = icon.height;
        SkillIcon.apply(icon, 45);
        icon.logicalSize(w, h);
    }
    @Override public float iconFadePercent() { return Math.max(0, 1 - cooldown()/duration); }
    @Override public String iconTextDisplay() { return Integer.toString((int)Math.ceil(cooldown())); }
    @Override public String desc() { return Messages.get(this, "desc", shielding(), dispTurns(Math.max(0, cooldown()))); }
}
