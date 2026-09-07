// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.util.ArrayList;
public class BoneRod extends MeleeWeapon {
    { tier=1; unique=true; bones=false; image=ItemSpriteSheet.BONE_ROD; }
    @Override public int min(int level) { return 1+level; }
    @Override public int max(int level) { return 6+2*level; }
    @Override public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions=super.actions(hero); actions.remove(AC_DROP); actions.remove(AC_THROW); return actions;
    }
    @Override public void doDrop(Hero hero) {}
}
