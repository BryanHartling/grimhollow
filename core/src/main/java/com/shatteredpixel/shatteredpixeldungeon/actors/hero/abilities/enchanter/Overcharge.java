// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
public class Overcharge extends ArmorAbility {
    {baseChargeUse=35;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){if(armor.charge<chargeUse(hero))return;armor.charge-=chargeUse(hero);Buff.prolong(hero,Overcharged.class,10+3*hero.pointsInTalent(Talent.SUSTAINED));com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(1);}
    @Override public Talent[] talents(){return new Talent[]{Talent.SUSTAINED,Talent.AMPLIFIED,Talent.FEEDBACK,Talent.HEROIC_ENERGY};}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.ELEMENTAL_STRIKE;}
}
