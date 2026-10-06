// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Quantity is the number of distinct receipts, never an editable cash balance. */
public class Warrant extends Item {
    public boolean hunter;
    public int payment;
    public String targetName = "";
    public LinkedHashSet<Integer> claims = new LinkedHashSet<>();
    public Warrant() { unique = true; stackable = true; image = ItemSpriteSheet.EXPEDITION_MAP; }
    public Warrant(int contract) {
        this(); claims.add(contract); payment = BountyBoard.contracts[contract].payment;
        targetName = BountyBoard.contracts[contract].alias();
    }
    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }
    @Override public int value() { return 0; }
    @Override public String name() { return Messages.get(this, "named", targetName); }
    @Override public String info() {
        if (!hunter && !claims.isEmpty() && BountyBoard.contracts[claims.iterator().next()] != null)
            return com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract.text(BountyBoard.contracts[claims.iterator().next()]);
        return Messages.get(this, "hunter", targetName, payment);
    }
    @Override public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero); actions.remove(AC_THROW); return actions;
    }
    public int state() {
        if (hunter || claims.isEmpty()) return 0;
        BountyBoard.Contract c = BountyBoard.contracts[claims.iterator().next()];
        return c == null ? 0 : c.paid ? 2 : c.complete ? 1 : 0;
    }
    @Override public boolean isSimilar(Item item) {
        if (!(item instanceof Warrant)) return false;
        Warrant other = (Warrant)item;
        return hunter == other.hunter && payment == other.payment && targetName.equals(other.targetName)
                && state() == other.state() && (hunter || claims.equals(other.claims));
    }
    @Override public Item merge(Item item) {
        if (isSimilar(item) && item != this) {
            Warrant other = (Warrant)item; claims.addAll(other.claims); quantity = claims.size();
            other.claims.clear(); other.quantity = 0;
        }
        return this;
    }
    @Override public Item split(int amount) {
        if (amount <= 0 || amount >= claims.size()) return null;
        Warrant split = new Warrant(); split.hunter = hunter; split.payment = payment; split.targetName = targetName;
        java.util.Iterator<Integer> it = claims.iterator();
        while (amount-- > 0) { split.claims.add(it.next()); it.remove(); }
        quantity = claims.size(); split.quantity = split.claims.size(); return split;
    }
    public static boolean ownedContract(int index) {
        for (Item item : Dungeon.hero.belongings) if (item instanceof Warrant
                && !((Warrant)item).hunter && ((Warrant)item).claims.contains(index)) return true;
        return false;
    }
    public static void retireContract(int index) {
        ArrayList<Warrant> papers = new ArrayList<>();
        for (Item item : Dungeon.hero.belongings) if (item instanceof Warrant
                && !((Warrant)item).hunter && ((Warrant)item).claims.contains(index)) papers.add((Warrant)item);
        for (Warrant paper : papers) paper.detachAll(Dungeon.hero.belongings.backpack);
    }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b); b.put("hunter", hunter); b.put("payment", payment); b.put("target_name", targetName);
        b.put("claims", claims.stream().mapToInt(Integer::intValue).toArray());
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); hunter = b.getBoolean("hunter"); payment = b.getInt("payment"); targetName = b.getString("target_name");
        claims.clear(); for (int id : b.getIntArray("claims")) claims.add(id); quantity = claims.size();
    }
}
