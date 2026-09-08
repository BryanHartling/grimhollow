// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Necromancy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhoulSprite;
import com.watabou.utils.Random;
public class NecroGhoul extends NecroSkeleton {
    {spriteClass=com.shatteredpixel.shatteredpixeldungeon.sprites.NecroGhoulSprite.class;remaining=40;}
    private boolean risen;
    @Override protected int baseHealth(){return 30+6*summonerLevel;}
    @Override public int damageRoll(){return Math.round(Random.NormalIntRange(4+summonerLevel/2,8+summonerLevel)*growth())+Necromancy.points(Talent.DEATHSPEAKERS_COMMAND);}
    @Override public void die(Object cause){
        if(!sacrificed&&!risen&&minions().stream().anyMatch(m->m!=this)){risen=true;HP=Math.max(1,HT/2);return;}
        super.die(cause);
    }
    @Override public void storeInBundle(com.watabou.utils.Bundle b){super.storeInBundle(b);b.put("risen",risen);}
    @Override public void restoreFromBundle(com.watabou.utils.Bundle b){super.restoreFromBundle(b);risen=b.getBoolean("risen");}
    @Override public int attackProc(Char enemy,int damage){Necromancy.heal(Math.round(Math.min(enemy.HP,Math.max(0,damage))*.3f));return super.attackProc(enemy,damage);}
}
