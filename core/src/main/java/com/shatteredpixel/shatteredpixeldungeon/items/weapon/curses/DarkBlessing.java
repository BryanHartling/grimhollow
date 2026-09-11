// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
public class DarkBlessing extends Weapon.Enchantment implements com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.Doom {
    @Override public void onDeath(){com.shatteredpixel.shatteredpixeldungeon.Dungeon.fail(this);}
    @Override public int proc(Weapon w,Char a,Char d,int damage){Echo.arm(a,d,.1f,getClass());return Math.round(damage*1.3f);}
    @Override public boolean curse(){return true;}
    @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x000000);}
}
