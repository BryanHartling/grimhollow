// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
public class Unmaking extends ArmorAbility {
    {baseChargeUse=60;}
    @Override public String targetingPrompt(){return Messages.get(this,"prompt");}
    public void unmake(Char enemy,Hero hero){int removed=EnchanterMagic.strip(enemy),turns=Char.hasProp(enemy,Char.Property.BOSS)?4:8;Buff.prolong(enemy,Unmade.class,turns);Buff.prolong(enemy,Vulnerable.class,turns);Buff.prolong(enemy,Cripple.class,turns);Buff.prolong(enemy,EnchanterMagic.EnchanterDamage.class,turns);hero.heal(removed*hero.pointsInTalent(Talent.RECLAMATION));EnchanterMagic.counterweight();}
    @Override public void activate(ClassArmor armor,Hero hero,Integer cell){if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell]||armor.charge<chargeUse(hero))return;Char target=Actor.findChar(cell);if(target==null||target.alignment!=Char.Alignment.ENEMY)return;armor.charge-=chargeUse(hero);unmake(target,hero);for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0]))if(mob!=target&&mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.adjacent(cell,mob.pos)&&Random.Float()<hero.pointsInTalent(Talent.CASCADE)/3f)unmake(mob,hero);com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();hero.spendAndNext(1);}
    @Override public Talent[] talents(){return new Talent[]{Talent.CASCADE,Talent.SALVAGE,Talent.RECLAMATION,Talent.HEROIC_ENERGY};}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.DEATH_MARK;}
}
