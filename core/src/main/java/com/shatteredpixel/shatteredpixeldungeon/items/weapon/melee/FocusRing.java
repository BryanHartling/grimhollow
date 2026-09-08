// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.util.ArrayList;
public class FocusRing extends MeleeWeapon {
    {tier=1;image=ItemSpriteSheet.FOCUS_RING;}
    @Override public int min(int lvl){return 1+lvl;}
    @Override public int max(int lvl){return 5+2*lvl;}
}
