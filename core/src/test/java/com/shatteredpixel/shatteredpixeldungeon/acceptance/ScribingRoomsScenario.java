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
import com.watabou.utils.Bundle;

/** Real gameplay regressions for Spellguard, scroll scribing and elemental caches. */
final class ScribingRoomsScenario {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError("Scribing/rooms: " + message);
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
