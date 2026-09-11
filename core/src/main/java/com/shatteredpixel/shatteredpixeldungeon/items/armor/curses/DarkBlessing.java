// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.armor.curses;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
public class DarkBlessing extends Armor.Glyph {
    @Override public int proc(Armor armor,Char a,Char d,int damage){return damage;}
    @Override public boolean curse(){return true;}
    @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x000000);}
}
