// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NecroRevenantSprite;
import com.watabou.utils.Random;
public class NecroRevenant extends NecroSkeleton {
    {spriteClass=NecroRevenantSprite.class;remaining=50;immunities.add(Terror.class);immunities.add(Amok.class);}
    @Override public int slots(){return 2;}
    @Override protected int baseHealth(){return 50+8*summonerLevel;}
    @Override public int damageRoll(){return Math.round(Random.NormalIntRange(6+summonerLevel,12+summonerLevel)*growth())+Necromancy.points(Talent.DEATHSPEAKERS_COMMAND);}
}
