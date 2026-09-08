// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.*;
import java.util.ArrayList;
public class RunedBaton extends MeleeWeapon {
    {tier=1;image=ItemSpriteSheet.RUNED_BATON;}
    @Override public int min(int lvl){return 2+lvl;}
    @Override public int max(int lvl){return 6+2*lvl;}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);if(b.contains("floor_enchant")&&runeEtching==null){runeEtching=new com.shatteredpixel.shatteredpixeldungeon.items.RuneEtching();runeEtching.floorEnchant=(Weapon.Enchantment)b.get("floor_enchant");}}
}
