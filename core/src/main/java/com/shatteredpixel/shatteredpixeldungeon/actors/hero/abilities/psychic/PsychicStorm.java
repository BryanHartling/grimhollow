// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
public class PsychicStorm extends ArmorAbility {
    {baseChargeUse=35;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){if(armor.charge<chargeUse(hero))return;int affected=0;
        for(Mob mob:Dungeon.level.mobs)if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.distance(hero.pos,mob.pos)<=4+hero.pointsInTalent(Talent.WIDER_STORM)){Buff.prolong(mob,Vertigo.class,5);Buff.prolong(mob,Amok.class,5);int p=hero.pointsInTalent(Talent.DREAD);if(p>0){Terror terror=Buff.prolong(mob,Terror.class,3*p);if(terror!=null)terror.object=hero.id();}affected++;}
        int p=hero.pointsInTalent(Talent.BACKLASH);FocusCrystal crystal=hero.belongings.getItem(FocusCrystal.class);if(p>0&&crystal!=null)crystal.gainCharge(affected/(4-p));armor.charge-=chargeUse(hero);com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(1);
    }
    @Override public Talent[] talents(){return new Talent[]{Talent.WIDER_STORM,Talent.DREAD,Talent.BACKLASH,Talent.HEROIC_ENERGY};}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.ELEMENTAL_BLAST;}
}
