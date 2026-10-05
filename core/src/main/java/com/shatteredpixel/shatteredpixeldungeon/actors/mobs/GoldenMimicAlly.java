// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

/** Directable protector. Never inherits a hostile mimic's guaranteed ambush or loot. */
public class GoldenMimicAlly extends DirectableAlly {
    private float rootReady;
    {spriteClass=MimicSprite.Golden.class;state=WANDERING;EXP=0;}
    public void refreshStrength(boolean full){
        GoldenMimicCompanion item=GoldenMimicCompanion.carried();if(item==null)return;
        float fraction=HP/(float)Math.max(1,HT);
        HT=Math.round((40+4*Dungeon.hero.lvl+2*item.coinLevel+8*item.hatchlingLevel)*BalanceTuning.multiplier(COMPANION_HEALTH));
        HP=full?HT:Math.max(1,Math.round(fraction*HT));defenseSkill=9+Dungeon.hero.lvl;
    }
    @Override public int attackSkill(Char c){return 9+Dungeon.hero.lvl;}
    @Override public int damageRoll(){return Math.round(Random.NormalIntRange(3+Dungeon.hero.lvl/2,6+3*Dungeon.hero.lvl/4)*BalanceTuning.multiplier(COMPANION_DAMAGE));}
    @Override public int drRoll(){return Random.NormalIntRange(0,1+Dungeon.hero.lvl/5);}
    public boolean pilfer(Mob enemy){
        if(enemy.alignment!=Alignment.ENEMY||enemy.EXP<=0||enemy.generatedRespawn||enemy.goldPilfered
                ||Char.hasProp(enemy,Property.BOSS)||Char.hasProp(enemy,Property.MINIBOSS)
                ||Random.Int(100)>=BalanceTuning.get(COMPANION_STEAL))return false;
        enemy.goldPilfered=true;int gold=Math.round((5+Dungeon.depth)*BalanceTuning.multiplier(COMPANION_GOLD));
        if(gold>0)new Gold(gold).award(Dungeon.hero);
        com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(this,"pilfer",gold));return true;
    }
    @Override public int attackProc(Char enemy,int damage){
        if(enemy instanceof Mob && pilfer((Mob)enemy))return 0;
        if(!Char.hasProp(enemy,Property.BOSS)&&!Char.hasProp(enemy,Property.MINIBOSS)&&Actor.now()>=rootReady&&BalanceTuning.get(COMPANION_ROOT)>0){
            Buff.prolong(enemy,Roots.class,BalanceTuning.get(COMPANION_ROOT));rootReady=Actor.now()+BalanceTuning.get(COMPANION_ROOT_COOLDOWN);
        }
        return super.attackProc(enemy,damage);
    }
    @Override public boolean act(){
        if(GoldenMimicCompanion.carried()==null){destroy();if(sprite!=null)sprite.killAndErase();return true;}
        if(fieldOfView==null||fieldOfView.length!=Dungeon.level.length())fieldOfView=new boolean[Dungeon.level.length()];Dungeon.level.updateFieldOfView(this,fieldOfView);
        refreshStrength(false);
        if(paralysed>0||rooted)return super.act();
        boolean hostile=false;for(Mob m:Dungeon.level.mobs)if(m.alignment==Alignment.ENEMY&&fieldOfView[m.pos]&&m.invisible==0){hostile=true;break;}
        if(!hostile && BalanceTuning.get(COMPANION_COLLECT)>0 && defendingPos<0 && (enemy==null||!enemy.isAlive()||!fieldOfView[enemy.pos])){
            Heap best=null;int distance=Integer.MAX_VALUE;
            for(Heap heap:Dungeon.level.heaps.valueList())if(heap.type==Heap.Type.HEAP && !heap.hidden && Dungeon.level.heroFOV[heap.pos]
                    && Dungeon.level.distance(Dungeon.hero.pos,heap.pos)<=BalanceTuning.get(COMPANION_COLLECT)
                    && !Dungeon.level.avoid[heap.pos] && heap.items.stream().anyMatch(i->i instanceof Gold)){
                int d=Dungeon.level.distance(pos,heap.pos);if(d<distance){best=heap;distance=d;}
            }
            if(best!=null){
                if(best.pos==pos){for(Item i:new java.util.ArrayList<>(best.items))if(i instanceof Gold){((Gold)i).award(Dungeon.hero);best.remove(i);}spend(TICK);return true;}
                // Follow only a known, safe route. Do not scout outside direct hero sight.
                boolean[] safe=new boolean[Dungeon.level.length()];
                for(int i=0;i<safe.length;i++)safe[i]=Dungeon.level.passable[i]&&Dungeon.level.heroFOV[i]&&!Dungeon.level.avoid[i]&&!Dungeon.level.pit[i]&&Dungeon.level.traps.get(i)==null;
                int step=Dungeon.findStep(this,best.pos,safe,fieldOfView,true);
                if(step>=0){int old=pos;move(step);spend(1/speed());return sprite==null||moveSprite(old,pos);}
            }
        }
        return super.act();
    }
    @Override public void die(Object cause){GoldenMimicCompanion item=GoldenMimicCompanion.carried();if(item!=null)item.knockOut();destroy();if(sprite!=null)sprite.die();}
    @Override public void fixTime(float decrement){super.fixTime(decrement);rootReady-=decrement;}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("root_ready",rootReady);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);rootReady=b.getFloat("root_ready");}
}
