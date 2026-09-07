// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Necromancy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhoulSprite;
import com.watabou.utils.Random;
public class NecroGhoul extends NecroSkeleton {
    {spriteClass=GhoulSprite.class;}
    @Override protected int baseHealth(){return 25+6*summonerLevel;}
    @Override public int damageRoll(){return Random.NormalIntRange(4+summonerLevel/2,8+summonerLevel)+Necromancy.points(Talent.DEATHSPEAKERS_COMMAND);}
    @Override public int attackProc(Char enemy,int damage){Necromancy.heal(Math.round(Math.min(enemy.HP,Math.max(0,damage))*.3f));return super.attackProc(enemy,damage);}
}
