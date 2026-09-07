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
public class DeathPact extends ArmorAbility {
    {baseChargeUse=50;}
    @Override public int icon(){return HeroIcon.DEATH_MARK;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){
        if(armor.charge<chargeUse(hero))return;
        int health=0;for(NecroSkeleton m:NecroSkeleton.minions()){health+=m.HP;m.sacrificed=true;m.die(hero);}
        armor.charge-=chargeUse(hero);Necromancy.heal(Math.min(hero.HT/2,health));Buff.prolong(hero,Adrenaline.class,10);
        int p=hero.pointsInTalent(Talent.BLESSED_PACT);if(p>0)Buff.prolong(hero,Bless.class,5*p);
        p=hero.pointsInTalent(Talent.BONE_SHELL);if(p>0)Buff.affect(hero,Barkskin.class).setForDuration(hero.lvl/2,5+5*p);
        Phylactery item=hero.belongings.getItem(Phylactery.class);if(item!=null)item.gainCharge(hero.pointsInTalent(Talent.RECLAIMED));
        com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(Actor.TICK);
    }
    @Override public Talent[] talents(){return new Talent[]{Talent.BLESSED_PACT,Talent.BONE_SHELL,Talent.RECLAIMED,Talent.HEROIC_ENERGY};}
}
