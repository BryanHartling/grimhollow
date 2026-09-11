// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HexcasterSprite;
import com.watabou.utils.Bundle;
/** Uses Warlock's projectile callback and pathfinding, with curses in place of its bolt. */
public class Hexcaster extends Warlock {
    private boolean decrepifyNext;
    {HP=HT=60;spriteClass=HexcasterSprite.class;loot=Generator.Category.WAND;lootChance=.25f;HUNTING=new HexHunting();}
    @Override public Item createLoot(){return Generator.random(Generator.Category.WAND);}
    public void curse(Char victim){
        NecroCurse.apply(victim,decrepifyNext?NecroCurse.Kind.DECREPIFY:NecroCurse.Kind.AMPLIFY,5);
        decrepifyNext=!decrepifyNext;
    }
    @Override protected void zap(){spend(TICK);Invisibility.dispel(this);if(enemy!=null)curse(enemy);}
    private class HexHunting extends Hunting {
        @Override public boolean act(boolean inFOV,boolean alerted){
            if(inFOV&&enemy!=null&&Dungeon.level.adjacent(pos,enemy.pos)&&!rooted&&getFurther(enemy.pos)){
                spend(1/speed());return true;
            }
            return super.act(inFOV,alerted);
        }
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("decrepify_next",decrepifyNext);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);decrepifyNext=b.getBoolean("decrepify_next");}
}
