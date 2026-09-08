// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;
public class NecroRevenantSprite extends GhoulSprite {
    @Override public void resetColor(){super.resetColor();tint(0x7BB33B, .45f);}
    public NecroRevenantSprite(){resetColor();}
}
