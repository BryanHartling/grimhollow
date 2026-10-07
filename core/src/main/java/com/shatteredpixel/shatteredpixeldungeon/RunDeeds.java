// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.*;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import java.util.ArrayList;
import java.util.EnumSet;

/** Historical facts only: independent of scores, quest actors and later runs. */
public class RunDeeds implements Bundlable {
    public enum Deed {
        GHOST_QUARRY, GHOST_REWARD, DUST_FOUND, EMBERS_FOUND, ROTBERRY_FOUND,
        DUST_RETURNED, EMBERS_RETURNED, ROTBERRY_RETURNED, TROLL_GOLD,
        TROLL_BOSS, TROLL_COMPLETED, IMP_COMPLETED, VAULT_RETURNED,
        EXPEDITION_ENTERED, BROODMOTHER_SLAIN, DRAGON_SLAIN, DRAGON_ESCAPED,
        COLE_DEFEATED, COLE_SETTLED, COLE_ESCAPED
    }
    private static RunDeeds current = new RunDeeds();
    public final EnumSet<Deed> deeds = EnumSet.noneOf(Deed.class);
    public final ArrayList<Notice> notices = new ArrayList<>();
    public boolean partial, wanted;
    public HeroClass wantedClass;
    public String wantedName = "";
    public int wantedBounty;

    public static void reset() { current = new RunDeeds(); }
    public static void record(Deed deed) { current.deeds.add(deed); }
    public static void pickedUp(Item item) {
        if (item instanceof CorpseDust) record(Deed.DUST_FOUND);
        if (item instanceof Embers) record(Deed.EMBERS_FOUND);
        if (item instanceof Rotberry.Seed) record(Deed.ROTBERRY_FOUND);
    }
    public static void wantedPoster() {
        current.wanted = true;
        current.wantedClass = Dungeon.hero.heroClass;
        current.wantedName = Dungeon.hero.name();
        current.wantedBounty = BountyBoard.heroBounty;
    }
    public static void paid(BountyBoard.Contract contract, int gold) {
        if (contract == null || gold <= 0) return;
        current.notice(contract).collected = gold;
    }
    public static Notice receipt(BountyBoard.Contract contract, int gold) {
        Notice receipt = new Notice(); receipt.update(contract); receipt.collected = gold; return receipt;
    }
    public static void collected(Notice receipt) { paid(receipt.poster(), receipt.collected); }
    private Notice notice(BountyBoard.Contract c) {
        for (Notice n : notices) if (n.index == c.index) { n.update(c); return n; }
        Notice n = new Notice(); n.update(c); notices.add(n); return n;
    }
    public Notice highestPaid() {
        Notice best = null;
        for (Notice n : notices) if (n.collected > 0 && (best == null || n.collected > best.collected)) best = n;
        return best;
    }
    public static String text(Deed deed) { return Messages.get(RunDeeds.class, deed.name().toLowerCase(java.util.Locale.ROOT)); }

    /** Import only evidence retained by a quest, never inferred from score or depth. */
    private static void collectKnown() {
        if (Ghost.Quest.processed()) record(Deed.GHOST_QUARRY);
        if (Ghost.Quest.completed()) record(Deed.GHOST_REWARD);
        if (Wandmaker.Quest.completed()) {
            switch (Wandmaker.Quest.type()) {
                case 1: record(Deed.DUST_RETURNED); break;
                case 2: record(Deed.EMBERS_RETURNED); break;
                case 3: record(Deed.ROTBERRY_RETURNED); break;
            }
        }
        if (Dungeon.hero != null) for (Item item : Dungeon.hero.belongings) pickedUp(item);
        if (Blacksmith.Quest.completed()) record(Deed.TROLL_COMPLETED);
        if (Blacksmith.Quest.bossBeaten()) record(Deed.TROLL_BOSS);
        if (Imp.Quest.isCompleted()) record(Imp.Quest.earnedShop() ? Deed.IMP_COMPLETED : Deed.VAULT_RETURNED);
        if (DragonExpedition.entered) record(Deed.EXPEDITION_ENTERED);
        if (DragonExpedition.spiderSlain) record(Deed.BROODMOTHER_SLAIN);
        if (DragonExpedition.dragonSlain) record(Deed.DRAGON_SLAIN);
        if (BountyBoard.resolved) {
            if (BountyBoard.outcome == 1) record(Deed.COLE_DEFEATED);
            else if (BountyBoard.outcome == 4) record(Deed.COLE_ESCAPED);
            else if (BountyBoard.outcome == 2 || BountyBoard.outcome == 3) record(Deed.COLE_SETTLED);
        }
        for (BountyBoard.Contract c : BountyBoard.contracts) if (c != null && c.accepted) current.notice(c);
        // Older ongoing saves retain the posted bounty, but not the face at betrayal.
        if (BountyBoard.betrayed && !current.wanted && Dungeon.hero != null) wantedPoster();
    }
    public static RunDeeds capture() {
        collectKnown();
        Bundle b = new Bundle(); b.put("copy", current);
        return (RunDeeds)b.get("copy");
    }
    public static void store(Bundle quests) { quests.put("run_deeds", capture()); }
    public static void restore(Bundle quests) {
        current = quests.contains("run_deeds") ? (RunDeeds)quests.get("run_deeds") : new RunDeeds();
        if (current == null) current = new RunDeeds();
        current.partial |= !quests.contains("run_deeds");
        collectKnown();
    }
    @Override public void storeInBundle(Bundle b) {
        String[] names = new String[deeds.size()];
        int index = 0;
        for (Deed deed : deeds) names[index++] = deed.name();
        b.put("deeds", names);
        b.put("notices", notices); b.put("partial", partial); b.put("wanted", wanted);
        if (wantedClass != null) b.put("wanted_class", wantedClass);
        b.put("wanted_name", wantedName); b.put("wanted_bounty", wantedBounty);
    }
    @Override public void restoreFromBundle(Bundle b) {
        deeds.clear(); notices.clear();
        for (String name : b.getStringArray("deeds")) {
            try { deeds.add(Deed.valueOf(name)); } catch (IllegalArgumentException ignored) { /* Future deed in an older build. */ }
        }
        for (Bundlable n : b.getCollection("notices")) if (n instanceof Notice) notices.add((Notice)n);
        partial = b.getBoolean("partial"); wanted = b.getBoolean("wanted");
        wantedClass = b.contains("wanted_class") ? b.getEnum("wanted_class", HeroClass.class) : null;
        wanted &= wantedClass != null;
        wantedName = b.getString("wanted_name"); wantedBounty = b.getInt("wanted_bounty");
    }

    /** A poster's value snapshot. No live mob, claim action or quest reference is retained. */
    public static class Notice implements Bundlable {
        public int index, species, floor, payment, collected;
        public boolean complete, returned, bonusEarned;
        public String alias = "", title = "";
        private void update(BountyBoard.Contract c) {
            index=c.index; species=c.species; floor=c.floor; payment=c.payment;
            complete=c.complete; returned=c.returned; bonusEarned=c.bonusEarned;
            alias=Messages.titleCase(c.alias()); title=c.title();
        }
        public BountyBoard.Contract poster() {
            BountyBoard.Contract c = new BountyBoard.Contract() {
                @Override public String alias() { return Notice.this.alias; }
                @Override public String title() { return Notice.this.title; }
            };
            c.index=index; c.species=species; c.floor=floor; c.payment=payment;
            c.accepted=true; c.complete=complete; c.returned=returned; c.bonusEarned=bonusEarned;
            return c;
        }
        @Override public void storeInBundle(Bundle b) {
            b.put("index",index); b.put("species",species); b.put("floor",floor); b.put("payment",payment);
            b.put("collected",collected); b.put("complete",complete); b.put("returned",returned);
            b.put("bonus",bonusEarned); b.put("alias",alias); b.put("title",title);
        }
        @Override public void restoreFromBundle(Bundle b) {
            index=b.getInt("index"); species=b.getInt("species"); floor=b.getInt("floor"); payment=b.getInt("payment");
            collected=b.getInt("collected"); complete=b.getBoolean("complete"); returned=b.getBoolean("returned");
            bonusEarned=b.getBoolean("bonus"); alias=b.getString("alias"); title=b.getString("title");
        }
    }
}
