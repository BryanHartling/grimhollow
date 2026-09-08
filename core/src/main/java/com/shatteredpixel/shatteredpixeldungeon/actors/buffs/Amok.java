/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Amok extends FlavourBuff {

    public boolean dominated;
    private boolean sharedBless, sharedHaste, sharedBarkskin;

    /** Only remove buffs supplied by this domination; preserve the target's own buffs. */
    public void share(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
        if (!dominated || !hero.hasTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SHARED_WILL)) return;
        if (hero.buff(Bless.class)!=null) {
            sharedBless |= target.buff(Bless.class)==null;
            if(sharedBless) Buff.prolong(target,Bless.class,2);
        } else if(sharedBless) { Buff.detach(target,Bless.class);sharedBless=false; }
        if (hero.buff(Haste.class)!=null) {
            sharedHaste |= target.buff(Haste.class)==null;
            if(sharedHaste) Buff.prolong(target,Haste.class,2);
        } else if(sharedHaste) { Buff.detach(target,Haste.class);sharedHaste=false; }
        if (Barkskin.currentLevel(hero)>0) {
            sharedBarkskin |= target.buff(Barkskin.class)==null;
            if(sharedBarkskin) Buff.affect(target,Barkskin.class).setForDuration(Barkskin.currentLevel(hero),2);
        } else if(sharedBarkskin) { Buff.detach(target,Barkskin.class);sharedBarkskin=false; }
    }

    @Override public void storeInBundle(com.watabou.utils.Bundle b) {
        super.storeInBundle(b);b.put("dominated",dominated);b.put("shared_bless",sharedBless);b.put("shared_haste",sharedHaste);b.put("shared_barkskin",sharedBarkskin);
    }
    @Override public void restoreFromBundle(com.watabou.utils.Bundle b) {
        super.restoreFromBundle(b);dominated=b.getBoolean("dominated");sharedBless=b.getBoolean("shared_bless");sharedHaste=b.getBoolean("shared_haste");sharedBarkskin=b.getBoolean("shared_barkskin");
    }

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.AMOK;
	}

	@Override
	public void detach() {
        if(sharedBless) Buff.detach(target,Bless.class);
        if(sharedHaste) Buff.detach(target,Haste.class);
        if(sharedBarkskin) Buff.detach(target,Barkskin.class);
		//if our target is an enemy, reset any enemy-to-enemy aggro involving it
		if (target.isAlive()) {
			if (target.alignment == Char.Alignment.ENEMY) {
				for (Mob m : Dungeon.level.mobs) {
					if (m.alignment == Char.Alignment.ENEMY && m.isTargeting(target)) {
						m.aggro(null);
					}
					if (target instanceof Mob && ((Mob) target).isTargeting(m)){
						((Mob) target).aggro(null);
					}
				}
			}
		}

		super.detach();
	}
}
