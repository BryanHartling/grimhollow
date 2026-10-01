// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem;
import com.watabou.utils.Bundle;

/** Real gameplay regressions for Spellguard, scroll scribing and elemental caches. */
final class ScribingRoomsScenario {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError("Scribing/rooms: " + message);
    }

    static void scribing() {
        GamesInProgress.selectedClass=HeroClass.ENCHANTER;
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);SmokeRun.clearArena();
        Hero hero=Dungeon.hero;
        SigilBrush brush=hero.belongings.getItem(SigilBrush.class);
        BlankParchment paper=new BlankParchment();paper.quantity(10);paper.collect();
        Scroll.initLabels();
        com.shatteredpixel.shatteredpixeldungeon.journal.Catalog.setSeen(ScrollOfIdentify.class);
        check(BlankParchment.recipes().isEmpty(),"historical journal knowledge is not a recipe");
        Dungeon.energy=100;int charges=brush.charges();float time=hero.cooldown();
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"unknown recipe rejected");
        check(Dungeon.energy==100&&paper.quantity()==10&&brush.charges()==charges&&hero.cooldown()==time,"invalid selection spends nothing");
        new ScrollOfIdentify().identify();new ScrollOfTransmutation().identify();new ScrollOfUpgrade().identify();new ScrollOfEnchantment().identify();
        check(BlankParchment.recipes().contains(ScrollOfIdentify.class)&&BlankParchment.recipes().contains(ScrollOfTransmutation.class)
                &&!BlankParchment.recipes().contains(ScrollOfUpgrade.class)&&!BlankParchment.recipes().contains(ScrollOfEnchantment.class),"regular pool and forbidden upgrade/exotic recipes");
        check(BlankParchment.scribe(hero,ScrollOfIdentify.class),"known regular scroll written");
        check(Dungeon.energy==88&&paper.quantity()==9&&brush.charges()==charges-1&&hero.cooldown()==time+3,"one parchment, charge, 12 energy and exactly three turns");
        check(hero.belongings.getItem(ScrollOfIdentify.class).isIdentified(),"written output is known");
        time=hero.cooldown();check(BlankParchment.scribe(hero,ScrollOfTransmutation.class)&&Dungeon.energy==68&&hero.cooldown()==time+3,"Transmutation costs 20 energy and three turns");
        Dungeon.energy=11;time=hero.cooldown();charges=brush.charges();
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class)&&paper.quantity()==8&&brush.charges()==charges&&hero.cooldown()==time,"insufficient energy is atomic");
        Dungeon.energy=100;brush.gainCharge(-100);
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"empty Brush rejected");brush.gainCharge(10);
        Buff.affect(hero,MagicImmune.class);check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"suppressed Brush rejected");Buff.detach(hero,MagicImmune.class);
        hero.belongings.artifact=null;hero.belongings.backpack.items.add(brush);
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"unequipped Brush requires Wandering Brush");
        hero.talents.get(1).put(Talent.DUAL_INSCRIPTION,1);
        check(BlankParchment.scribe(hero,ScrollOfIdentify.class),"Wandering Brush permits carried Scribe");
        hero.heroClass=HeroClass.WARRIOR;check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"other classes cannot write");hero.heroClass=HeroClass.ENCHANTER;
        // Actual shared recycling path covers pot, Toolkit and Alchemize callers.
        ScrollOfIdentify scroll=new ScrollOfIdentify();scroll.quantity(4);hero.belongings.backpack.items.add(scroll);
        int before=paper.quantity(),energy=Dungeon.energy;
        WndEnergizeItem.energizeOne(scroll);
        check(scroll.quantity()==3&&paper.quantity()==before+1&&Dungeon.energy==energy+6,"recycle one returns one parchment and normal energy");
        WndEnergizeItem.energizeAll(scroll);
        check(!hero.belongings.contains(scroll)&&paper.quantity()==before+4&&Dungeon.energy==energy+24,"recycle stack returns one sheet per scroll");
        WndEnergizeItem.energizeAll(scroll);
        check(paper.quantity()==before+4&&Dungeon.energy==energy+24,"stale recycling selection cannot duplicate energy or parchment");
        ScrollOfEnchantment exotic=new ScrollOfEnchantment();hero.belongings.backpack.items.add(exotic);
        WndEnergizeItem.energizeAll(exotic);check(paper.quantity()==before+5,"exotic recycling also returns parchment");
        com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder holder=new com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder();
        check(holder.canHold(paper),"parchment fits scroll holder");
        check(((BlankParchment)paper.duplicate()).quantity()==paper.quantity(),"paper stack save/load");
        int opportunities=0;
        for(int seed=0;seed<100;seed++){
            int count=0;
            for(int depth=1;depth<=26;depth++){
                boolean spawn=BlankParchment.spawnsOn(depth,0,HeroClass.ENCHANTER,seed);
                check(!spawn||depth>=2&&depth<=24&&depth%5!=0,"no boss/endgame parchment");
                check(!BlankParchment.spawnsOn(depth,1,HeroClass.ENCHANTER,seed)&&!BlankParchment.spawnsOn(depth,0,HeroClass.WARRIOR,seed),"no other-class or branch parchment");
                if(spawn)count++;
            }
            check(count>=1&&count<=5&&BlankParchment.spawnsOn(2,0,HeroClass.ENCHANTER,seed),"early sheet and regional bounds");opportunities+=count;
        }
        check(opportunities>=340&&opportunities<=450,"seeded parchment average remains modest");
        System.out.println("TEST 63 PASS: Scribe knowledge, exclusions, atomic costs, three turns, carried Brush, recycling one/stack/exotic/stale, bag/save and seeded parchment opportunities="+opportunities+"/100 runs");
    }

    static void spellguard() {
        GamesInProgress.selectedClass = HeroClass.ENCHANTER;
        Dungeon.init(); Dungeon.switchLevel(Dungeon.newLevel(), -1); SmokeRun.clearArena();
        Hero hero = Dungeon.hero;
        for (Buff buff : hero.buffs()) buff.detach();
        ClothArmor armor = new ClothArmor(); hero.belongings.armor = armor;
        hero.HT = 10000;
        for (int rank = 0; rank <= 2; rank++) {
            hero.talents.get(1).put(Talent.SPELLGUARD, rank);
            armor.inscribed = new Obfuscation(); armor.inscriptionTurns = 10;
            hero.HP = 10000; hero.damage(100, new Warlock.DarkBolt());
            check(hero.HP == 9900 + 10*rank, "actual magical damage rank " + rank);
            hero.HP = 10000; hero.damage(100, new Rat());
            check(hero.HP == 9900, "physical damage unchanged");
        }
        armor.inscribe(new Obfuscation()); armor.inscribed = null;
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "permanent glyph alone does not qualify");
        armor.runeEtching = new RuneEtching();
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "rune alone does not qualify");
        armor.inscribed = new Obfuscation(); armor.inscriptionTurns = 0;
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "expired inscription does not qualify");
        armor.inscriptionTurns = 1;
        EnchanterMagic magic = Buff.affect(hero, EnchanterMagic.class); magic.act();
        check(armor.inscribed == null && armor.inscriptionTurns == 0, "real expiration clears inscription");
        armor.inscribed = new Obfuscation(); armor.inscriptionTurns = 10;
        hero.belongings.armor = null;
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "unworn inscription does not qualify");
        hero.belongings.armor = armor; Buff.affect(hero, MagicImmune.class);
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "magic suppression");
        Buff.detach(hero, MagicImmune.class);
        Bundle legacy = new Bundle(), tier = new Bundle(); tier.put("OVERLOAD", 2); legacy.put("talents_tier_2", tier);
        Talent.restoreTalentsFromBundle(legacy, hero);
        check(hero.pointsInTalent(Talent.SPELLGUARD) == 2, "old Overload ranks migrate");
        Bundle saved = new Bundle(); Talent.storeTalentsInBundle(saved, hero); Talent.restoreTalentsFromBundle(saved, hero);
        check(hero.pointsInTalent(Talent.SPELLGUARD) == 2, "new Spellguard ranks persist");
        System.out.println("TEST 62 PASS: Spellguard damage ranks, physical damage, expiry, equipment, glyph/rune exclusion, suppression and old/new save ranks");
    }
}
