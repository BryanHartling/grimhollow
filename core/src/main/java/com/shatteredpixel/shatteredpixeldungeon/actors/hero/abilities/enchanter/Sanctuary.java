// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SanctuaryZone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
public class Sanctuary extends ArmorAbility {
    {baseChargeUse=50;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){if(armor.charge<chargeUse(hero))return;armor.charge-=chargeUse(hero);SanctuaryZone.place(hero.pos,2+hero.pointsInTalent(Talent.WIDE_SANCTUARY),15);com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(1);}
    @Override public Talent[] talents(){return new Talent[]{Talent.WIDE_SANCTUARY,Talent.CONSECRATED,Talent.MOBILE,Talent.HEROIC_ENERGY};}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.ENDURE;}
}
