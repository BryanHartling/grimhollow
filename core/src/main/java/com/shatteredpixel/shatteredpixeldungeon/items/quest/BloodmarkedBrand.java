// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.quest;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
/** Identity and shop valuation; combat integration follows in the item component. */
public class BloodmarkedBrand extends Item {
    { stackable = true; image = ItemSpriteSheet.STYLUS; }
    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }
    @Override public int value() { return 15 * quantity(); }
}
