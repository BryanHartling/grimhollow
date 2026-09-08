// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Necromancy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WraithSprite;
import com.watabou.utils.Random;
public class NecroWraith extends NecroSkeleton {
    {spriteClass=WraithSprite.class;flying=true;baseSpeed=1.5f;properties.add(Property.INORGANIC);}
    @Override protected int baseHealth(){return 10+3*summonerLevel;}
    @Override public int damageRoll(){return Math.round(Random.NormalIntRange(3+summonerLevel/2,7+summonerLevel)*growth())+Necromancy.points(Talent.DEATHSPEAKERS_COMMAND);}
    @Override public int defenseSkill(Char enemy){return 5*super.attackSkill(enemy);}
}
