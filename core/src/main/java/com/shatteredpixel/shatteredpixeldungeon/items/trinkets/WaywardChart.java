// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** Passive, carried trinket; advancement uses the native catalyst recipe. */
public class WaywardChart extends Trinket {
    { image = ItemSpriteSheet.WAYWARD_CHART; }
    @Override protected int upgradeEnergyCost() { return 10 + 5 * level(); }
    @Override public String statsDesc() { return Messages.get(this, "stats_desc"); }
    public static int rank() { return trinketLevel(WaywardChart.class); }
}
