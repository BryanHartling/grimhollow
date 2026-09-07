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
public class BonePrison extends ArmorAbility {
    {baseChargeUse=60;}
    @Override public int icon(){return HeroIcon.WALL_OF_LIGHT;}
    @Override public String targetingPrompt(){return Messages.get(this,"prompt");}
    @Override public int targetedPos(Char user,int dst){return dst;}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){
        if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell]||armor.charge<chargeUse(hero))return;
        if(!com.shatteredpixel.shatteredpixeldungeon.levels.features.BoneWalls.prison(cell,10+3*hero.pointsInTalent(Talent.LASTING_CAGE),hero.pointsInTalent(Talent.NECROMANCERS_KEY)))return;
        armor.charge-=chargeUse(hero);com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(Actor.TICK);
    }
    @Override public Talent[] talents(){return new Talent[]{Talent.LASTING_CAGE,Talent.JAGGED,Talent.NECROMANCERS_KEY,Talent.HEROIC_ENERGY};}
}
