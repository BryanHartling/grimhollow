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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.noosa.audio.Sample;

public class InventorySlot extends ItemSlot {


	private PaintedInterface.Patch bg;

	public InventorySlot( Item item ) {

		super( item );
	}

	@Override
	protected void createChildren() {
		bg = PaintedInterface.slot();
		add( bg );

		super.createChildren();
	}

	@Override
	protected void layout() {
		bg.size(width, height);
		bg.x = x;
		bg.y = y;

		super.layout();
	}

	@Override
	public void alpha(float value) {
		super.alpha(value);
		bg.alpha(value);
	}

	@Override
	public void item( Item item ) {

		super.item( item );

		bg.visible = !(item instanceof Gold || item instanceof Bag);

		if (item != null) {

			boolean equipped = item.isEquipped(Dungeon.hero) ||
					item == Dungeon.hero.belongings.weapon ||
					item == Dungeon.hero.belongings.armor ||
					item == Dungeon.hero.belongings.artifact ||
					item == Dungeon.hero.belongings.misc ||
					item == Dungeon.hero.belongings.ring ||
					item == Dungeon.hero.belongings.secondWep;

			bg.source(equipped ? 28 : 0, 64);
			bg.resetColor();
			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.20f;
				bg.ga = -0.06f;
				bg.ba = -0.06f;
			} else if (!item.isIdentified()) {
				if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown){
					bg.ba = +0.16f;
					bg.ra = -0.04f;
				} else {
					bg.ra = +0.14f;
					bg.ba = +0.14f;
				}
			}

			if (item.name() == null) {
				enable( false );
			} else if (Dungeon.hero.belongings.lostInventory()
					&& !item.keptThroughLostInventory()){
				enable(false);
			}
		} else {
			bg.source(0,64);
			bg.resetColor();
		}
	}

	public Item item(){
		return item;
	}

	@Override
	protected void onPointerDown() {
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
	}

	protected void onPointerUp() {
		bg.brightness( 1.0f );
	}

}
