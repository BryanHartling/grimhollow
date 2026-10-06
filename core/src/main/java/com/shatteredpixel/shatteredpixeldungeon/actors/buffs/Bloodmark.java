// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
public class Bloodmark extends FlavourBuff {
    { type = buffType.NEGATIVE; }
    public static final float DURATION = 8;
    public static float accuracy(com.shatteredpixel.shatteredpixeldungeon.actors.Char user, com.shatteredpixel.shatteredpixeldungeon.actors.Char enemy) {
        return user == com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero
                && com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.belongings.attackingWeapon() != null
                && enemy.buff(Bloodmark.class) != null ? 1.25f : 1f;
    }
    public static int armor(com.shatteredpixel.shatteredpixeldungeon.actors.Char user, com.shatteredpixel.shatteredpixeldungeon.actors.Char enemy, int dr) {
        return accuracy(user, enemy) > 1 ? dr-dr/2 : dr;
    }
    @Override public int icon() { return BuffIndicator.TARGETED; }
    @Override public void tintIcon(com.watabou.noosa.Image icon) { icon.hardlight(0xD47758); }
    @Override public float iconFadePercent() { return Math.max(0, (DURATION-visualcooldown())/DURATION); }
}
