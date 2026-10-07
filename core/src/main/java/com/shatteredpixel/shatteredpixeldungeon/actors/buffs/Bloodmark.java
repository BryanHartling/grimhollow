// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
public class Bloodmark extends FlavourBuff {
    { type = buffType.NEGATIVE; }
    public static final float DURATION = 8;
    public static float accuracy(com.shatteredpixel.shatteredpixeldungeon.actors.Char user, com.shatteredpixel.shatteredpixeldungeon.actors.Char enemy) {
        return user == com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero
                && com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.belongings.attackingWeapon() != null
                && enemy.buff(Bloodmark.class) != null ? com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.multiplier(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.BRAND_ACCURACY) : 1f;
    }
    public static int armor(com.shatteredpixel.shatteredpixeldungeon.actors.Char user, com.shatteredpixel.shatteredpixeldungeon.actors.Char enemy, int dr) {
        return user==com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero
                &&com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.belongings.attackingWeapon()!=null&&enemy.buff(Bloodmark.class)!=null
                ?dr-(int)Math.floor(dr*com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.multiplier(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.BRAND_PENETRATION)):dr;
    }
    @Override public int icon() { return BuffIndicator.TARGETED; }
    @Override public void tintIcon(com.watabou.noosa.Image icon) {
        float w=icon.width(), h=icon.height();
        icon.texture("effects/bloodmark.png"); icon.frame(0,0,64,64); icon.logicalSize(w,h);
        icon.texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR,com.badlogic.gdx.graphics.GL20.GL_LINEAR);
    }
    @Override public float iconFadePercent() { return Math.max(0, (DURATION-visualcooldown())/DURATION); }
}
