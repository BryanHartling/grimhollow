// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.quest;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
public class BloodmarkedBrand extends Item {
    { stackable = true; image = ItemSpriteSheet.BLOODMARKED_BRAND; defaultAction = AC_THROW; usesTargeting = true; }
    @Override protected void onThrow(int cell) {
        com.shatteredpixel.shatteredpixeldungeon.actors.Char target = com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(cell);
        if (target != null && target.alignment == com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY)
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(target,
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.class,
                    com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.get(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.BRAND_TURNS));
    }
    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }
    @Override public int value() { return 15 * quantity(); }
}
