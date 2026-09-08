// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
public class ForceWall extends ArmorAbility {
    {baseChargeUse=60;}
    @Override public String targetingPrompt(){return Messages.get(this,"prompt");}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell]||armor.charge<chargeUse(hero)||!ForceWalls.line(cell,8+3*hero.pointsInTalent(Talent.HELD_FIRM)))return;armor.charge-=chargeUse(hero);com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(1);}
    @Override public Talent[] talents(){return new Talent[]{Talent.HELD_FIRM,Talent.REPULSE,Talent.PERMEABLE,Talent.HEROIC_ENERGY};}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.ENDURE;}
}
