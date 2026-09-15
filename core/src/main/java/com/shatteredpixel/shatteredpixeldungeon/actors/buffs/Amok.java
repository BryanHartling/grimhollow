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
    private final SharedWill shared = new SharedWill();

    public void share(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
        if (dominated) shared.update(target, hero);
    }

    @Override public void storeInBundle(com.watabou.utils.Bundle b) {
        super.storeInBundle(b); b.put("dominated", dominated); shared.store(b);
    }
    @Override public void restoreFromBundle(com.watabou.utils.Bundle b) {
        super.restoreFromBundle(b); dominated = b.getBoolean("dominated"); shared.restore(b);
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
        shared.clear(target);
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
