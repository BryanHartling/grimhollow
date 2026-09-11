// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.traps;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
public class ChainTrap extends Trap {
    {color=GREY;shape=DOTS;canBeHidden=true;canBeSearched=false;}
    @Override public void activate(){
        Char victim=Actor.findChar(pos);
        if(victim==null||victim instanceof Tengu||victim.flying)return;
        Buff.prolong(victim,Roots.class,2);
        if(victim.sprite!=null&&victim.sprite.parent!=null&&victim.sprite.visible){
            for(int offset:new int[]{-1,1})if(Dungeon.level.insideMap(pos+offset))victim.sprite.parent.add(new Chains(pos+offset,pos,Effects.Type.CHAIN,null));
        }
    }
}
