// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
public class MindMeld extends ArmorAbility {
    {baseChargeUse=50;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){if(armor.charge<chargeUse(hero))return;int turns=20+10*hero.pointsInTalent(Talent.LONG_MELD);Buff.prolong(hero,MindVision.class,turns);Buff.prolong(hero,MeldedMind.class,turns);int p=hero.pointsInTalent(Talent.TOTAL_SIGHT);PsychicMind.reveal(true,true,p>0,p==3);armor.charge-=chargeUse(hero);com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(1);}
    @Override public Talent[] talents(){return new Talent[]{Talent.LONG_MELD,Talent.TOTAL_SIGHT,Talent.KINETIC_SURGE,Talent.HEROIC_ENERGY};}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.DIVINE_SENSE;}
}
