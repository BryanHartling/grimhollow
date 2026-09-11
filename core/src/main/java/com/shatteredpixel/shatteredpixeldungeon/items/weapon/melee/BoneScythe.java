// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
/** Uses the glaive's reach, attack delay and spike ability; adds only the specified sweep. */
public class BoneScythe extends Glaive {
    {tier=2;image=ItemSpriteSheet.BONE_SCYTHE;}
    @Override public int min(int level){return 4+level;}
    @Override public int max(int level){return 14+(tier+1)*level;}
    protected float sweep(){return .5f;}
    @Override public int proc(Char attacker,Char defender,int damage){
        damage=super.proc(attacker,defender,damage);int width=Dungeon.level.width();
        int dx=Integer.signum(defender.pos%width-attacker.pos%width),dy=Integer.signum(defender.pos/width-attacker.pos/width),offset=-dy+dx*width;
        for(int sign:new int[]{-1,1}){int cell=defender.pos+sign*offset;if(!Dungeon.level.insideMap(cell)||!Dungeon.level.adjacent(defender.pos,cell))continue;
            Char victim=Actor.findChar(cell);if(victim!=null&&victim!=attacker&&victim.alignment!=attacker.alignment&&victim.alignment!=Char.Alignment.NEUTRAL)victim.damage(Math.round(Math.max(0,damage)*sweep()),attacker);}
        return damage;
    }
    private int spikeBoost(int level){return Math.round(2.4f*tier)+Math.round(.5f*tier*level);}
    @Override protected void duelistAbility(Hero hero,Integer target){Spear.spikeAbility(hero,target,1,augment.damageFactor(spikeBoost(buffedLvl())),this);}
    @Override public String upgradeAbilityStat(int level){int boost=spikeBoost(level);return augment.damageFactor(min(level)+boost)+"-"+augment.damageFactor(max(level)+boost);}
    @Override public String abilityInfo(){int level=levelKnown?buffedLvl():0,boost=spikeBoost(level);return Messages.get(Glaive.class,levelKnown?"ability_desc":"typical_ability_desc",augment.damageFactor(min(level)+boost),augment.damageFactor(max(level)+boost));}
}
