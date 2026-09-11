// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.necromancer;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Phylactery;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.*;
public class CorpseExplosion extends ArmorAbility {
    {baseChargeUse=35;}
    @Override public int icon(){return HeroIcon.ELEMENTAL_BLAST;}
    @Override public String targetingPrompt(){return Messages.get(this,"prompt");}
    @Override public int targetedPos(Char user,int dst){return dst;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){
        if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell]||Dungeon.level.corpses.get(cell)==null||armor.charge<chargeUse(hero))return;
        int damage=Math.max(10,Math.round(Dungeon.level.corpses.get(cell)*.2f)+2*hero.lvl),radius=hero.hasTalent(Talent.WIDER_BLAST)?2:1;
        Dungeon.level.corpses.remove(cell);armor.charge-=chargeUse(hero);
        for(Mob m:Dungeon.level.mobs.toArray(new Mob[0]))if(m.alignment==Char.Alignment.ENEMY && Dungeon.level.distance(cell,m.pos)<=radius){
            int p=hero.pointsInTalent(Talent.ROT);if(p>0)Buff.affect(m,Corrosion.class).set(1+2*p,2);
            Buff.prolong(m,Necromancy.HeroDamage.class,8);
            m.damage(m.buff(AmplifySuffering.class)!=null?Math.round(damage*1.5f):damage,hero);
            if(!m.isAlive()&&Random.Float()<hero.pointsInTalent(Talent.SOUL_REFUND)/3f){Phylactery item=hero.belongings.getItem(Phylactery.class);if(item!=null)item.gainCharge(1);}
            if(!com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.enabled()&&m.sprite!=null&&m.sprite.parent!=null)m.sprite.centerEmitter().burst(com.shatteredpixel.shatteredpixeldungeon.effects.particles.NecroticParticle.FACTORY,10);
        }
        com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.burst(cell,com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.Style.CORPSE,(radius*2+1)*16,.6f);
        com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(Actor.TICK);
    }
    @Override public Talent[] talents(){return new Talent[]{Talent.WIDER_BLAST,Talent.ROT,Talent.SOUL_REFUND,Talent.HEROIC_ENERGY};}
}
