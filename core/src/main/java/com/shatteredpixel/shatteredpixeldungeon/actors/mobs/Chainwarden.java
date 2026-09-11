// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silenced;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfGravity;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChainTrap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ChainwardenSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
public class Chainwarden extends Tengu {
    private float chainClock;
    private int trapClock;
    {spriteClass=ChainwardenSprite.class;}
    public void advanceChains(float turns){
        chainClock+=turns;
        if(chainClock>=4){
            chainClock%=4;
            if(buff(Silenced.class)==null&&Dungeon.hero.isAlive()&&Dungeon.level.distance(pos,Dungeon.hero.pos)>1){
                if(sprite!=null&&sprite.parent!=null&&sprite.visible)sprite.parent.add(new Chains(pos,Dungeon.hero.pos,Effects.Type.CHAIN,null));
                WandOfGravity.pull(Dungeon.hero,pos,2,this);
            }
        }
    }
    @Override protected void spendConstant(float time){super.spendConstant(time);if(time>0)advanceChains(time);}
    @Override public boolean canUseAbility(){return HP<=HT/2&&++trapClock>=4;}
    @Override public boolean useAbility(){
        trapClock=0;
        for(int offset:PathFinder.NEIGHBOURS8){
            int cell=Dungeon.hero.pos+offset;
            if(Dungeon.level.insideMap(cell)&&Dungeon.level.passable[cell]&&Actor.findChar(cell)==null&&Dungeon.level.traps.get(cell)==null&&Dungeon.level.heaps.get(cell)==null){
                Dungeon.level.setTrap(new ChainTrap().reveal(),cell);Level.set(cell,Terrain.TRAP);GameScene.updateMap(cell);
            }
        }
        spend(TICK);return true;
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("chain_clock",chainClock);b.put("trap_clock",trapClock);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);chainClock=b.getFloat("chain_clock");trapClock=b.getInt("trap_clock");}
}
