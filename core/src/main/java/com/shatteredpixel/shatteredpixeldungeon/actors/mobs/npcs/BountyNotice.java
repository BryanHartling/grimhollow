// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
public class BountyNotice extends NPC {
    {spriteClass=ShopkeeperSprite.class;properties.add(Property.IMMOVABLE);}
    @Override public int defenseSkill(Char enemy){return INFINITE_EVASION;}
    @Override public void damage(int damage,Object source){}
    @Override public boolean add(Buff b){return false;}
    @Override public boolean reset(){return true;}
    @Override public boolean interact(Char ch){if(ch==Dungeon.hero)BountyBoard.showBoard();return true;}
}
