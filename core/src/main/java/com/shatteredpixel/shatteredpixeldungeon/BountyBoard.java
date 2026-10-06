// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BloodmarkedBrand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** One authoritative quest record, independent of floor files and physical claims. */
public final class BountyBoard {
    private BountyBoard() {}
    // Components remain isolated from ordinary runs until the full quest is integrated.
    public static boolean AVAILABLE = false;
    public static boolean present, shopClosed;
    public static final int OFFICE_DEPTH = 7;
    public static int officeCell = -1;
    public static Item[] stock = new Item[5];
    public static int[] prices = new int[5];

    public static void reset() {
        present = AVAILABLE;
        shopClosed = false; officeCell = -1;
        stock = new Item[5]; prices = new int[5];
    }

    /** Seeding this plan cannot consume generation rolls from unrelated rooms. */
    public static void planShop() {
        if (!present || prices[0] != 0) return;
        Random.pushGenerator(Dungeon.seed ^ 0x434F4C4553484F50L);
        try {
            stock[0] = new Food();
            stock[1] = new PotionOfHealing();
            stock[2] = Random.Int(2) == 0 ? new PotionOfHaste() : new PotionOfInvisibility();
            Weapon weapon = (Weapon) Generator.randomUsingDefaults(Random.Int(3) == 0
                    ? Generator.Category.MIS_T3 : Generator.Category.WEP_T3);
            weapon.level(Random.IntRange(1, 2)); weapon.cursed = false;
            weapon.enchant(Weapon.Enchantment.random());
            stock[3] = weapon.identify();
            stock[4] = new BloodmarkedBrand().quantity(2);
            for (int i = 0; i < 5; i++) prices[i] = standardPrice(stock[i], OFFICE_DEPTH) * 2;
        } finally { Random.popGenerator(); }
    }

    public static int standardPrice(Item item, int depth) {
        return item.value() * 5 * (depth / 5 + 1);
    }
    public static boolean owns(Heap heap) {
        return present && Dungeon.branch == 0 && Dungeon.depth == OFFICE_DEPTH
                && heap.coleSlot >= 0 && heap.coleSlot < stock.length;
    }
    public static boolean canTrade(Heap heap) {
        return !owns(heap) || !shopClosed && stock[heap.coleSlot] != null;
    }
    public static void takeStock(Heap heap) {
        if (owns(heap)) stock[heap.coleSlot] = null;
    }
    public static void failedTheft() {
        shopClosed = true;
        for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs)
            if (mob instanceof Cole) mob.yell(Messages.get(Cole.class, "thief"));
        for (Heap heap : Dungeon.level.heaps.valueList().toArray(new Heap[0]))
            if (owns(heap)) heap.destroy();
    }
    public static void store(Bundle quests) {
        Bundle b = new Bundle();
        b.put("present", present); b.put("closed", shopClosed); b.put("office", officeCell);
        for (int i = 0; i < 5; i++) { b.put("stock_" + i, stock[i]); b.put("price_" + i, prices[i]); }
        quests.put("bounty_board", b);
    }
    public static void restore(Bundle quests) {
        reset(); present = false; // Old saves never acquire retroactive room plans.
        if (!quests.contains("bounty_board")) return;
        Bundle b = quests.getBundle("bounty_board");
        present = b.getBoolean("present"); shopClosed = b.getBoolean("closed");
        officeCell = b.contains("office") ? b.getInt("office") : -1;
        for (int i = 0; i < 5; i++) { stock[i] = (Item)b.get("stock_" + i); prices[i] = b.getInt("price_" + i); }
    }
}
