// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
public class ReapersScythe extends BoneScythe {
    {tier=4;image=com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.REAPER_SCYTHE;}
    @Override public int min(int level){return 8+level;}
    @Override public int max(int level){return 28+(tier+1)*level;}
    @Override protected float sweep(){return 0.75f;}
}
