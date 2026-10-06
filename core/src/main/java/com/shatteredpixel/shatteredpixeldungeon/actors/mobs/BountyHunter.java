// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GuardSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
/** Finite regional roles, all using ordinary Mob navigation, visibility and controls. */
public class BountyHunter extends Mob {
    public int crew=-1, member=-1, region, role, ammunition=6, spells=3;
    public boolean healed, coleReinforcement;
    {spriteClass=GuardSprite.class;loot=Gold.class;lootChance=.5f;}
    public void configure(int depth,int role){
        this.role=role;region=Math.max(0,Math.min(2,(depth-11)/5));
        HT=HP=Math.round(new int[]{40,70,80}[region]*BalanceTuning.multiplier(BalanceTuning.Key.CREW_HEALTH));
        defenseSkill=new int[]{15,25,25}[region];EXP=5+region*3;maxLvl=30;
        wantedDamage=BalanceTuning.multiplier(BalanceTuning.Key.CREW_DAMAGE);state=WANDERING;
    }
    @Override public int attackSkill(Char enemy){return 20+region*6;}
    public void endPursuit(){alignment=Alignment.NEUTRAL;state=WANDERING;enemy=null;target=-1;}
    @Override public int damageRoll(){return Random.NormalIntRange(new int[]{5,12,25}[region],new int[]{25,25,30}[region]);}
    @Override public int drRoll(){return super.drRoll()+Random.NormalIntRange(0,role==0?4+region*2:region*2);}
    @Override protected boolean canAttack(Char enemy){
        return super.canAttack(enemy)||role>0&&ammunition>0&&Dungeon.level.distance(pos,enemy.pos)<=6
                &&new Ballistica(pos,enemy.pos,Ballistica.PROJECTILE).collisionPos==enemy.pos;
    }
    @Override protected boolean act(){
        if(coleReinforcement&&BountyBoard.resolved&&alignment==Alignment.ENEMY)endPursuit();
        if(role==2&&!healed&&HP>0&&HP<HT/3&&paralysed==0&&buff(Sleep.class)==null){
            healed=true;HP=Math.min(HT,HP+HT/3);spend(TICK);return true;
        }
        return super.act();
    }
    @Override protected boolean doAttack(Char enemy){
        if(!Dungeon.level.adjacent(pos,enemy.pos))ammunition=Math.max(0,ammunition-1);
        return super.doAttack(enemy);
    }
    @Override public int attackProc(Char enemy,int damage){
        damage=super.attackProc(enemy,damage);
        if(role==2&&spells>0){spells--;Buff.prolong(enemy,Weakness.class,3);}
        return damage;
    }
    @Override public void die(Object source){BountyBoard.hunterDied(this);super.die(source);}
    @Override public void storeInBundle(Bundle b){
        super.storeInBundle(b);b.put("crew",crew);b.put("member",member);b.put("region",region);b.put("role",role);
        b.put("ammunition",ammunition);b.put("spells",spells);b.put("healed",healed);b.put("cole_reinforcement",coleReinforcement);
    }
    @Override public void restoreFromBundle(Bundle b){
        super.restoreFromBundle(b);crew=b.getInt("crew");member=b.getInt("member");region=b.getInt("region");role=b.getInt("role");
        ammunition=b.getInt("ammunition");spells=b.getInt("spells");healed=b.getBoolean("healed");coleReinforcement=b.getBoolean("cole_reinforcement");
        defenseSkill=new int[]{15,25,25}[region];EXP=5+region*3;
    }
}
