// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import java.util.ArrayList;

/** Player-facing release history, curated from CHANGES.md, README.md and verified deliveries.
 * Keep newest releases first; describe shipped behavior, not superseded plans or build stages. */
public final class GrimhollowChanges {
    private GrimhollowChanges() {}

    private static ChangeInfo release(ArrayList<ChangeInfo> list, String version, String summary) {
        ChangeInfo info=new ChangeInfo(version,true,summary);
        info.hardlight(Window.TITLE_COLOR);
        list.add(info);
        return info;
    }
    private static void note(ChangeInfo release, Icons icon, String title, String text) {
        release.addButton(new ChangeButton(icon.get(),title,text));
    }
    public static void addAllChanges(ArrayList<ChangeInfo> list) {
        ChangeInfo r=release(list,"v1.17.1","Grimhollow's update log.");
        note(r,Icons.JOURNAL,"Our journey so far",
                "The update log now follows Grimhollow's own releases, from its three new heroes to the Lurking Horror. "
                +"Browse the entries below for new adventures, equipment, painted artwork, balance changes and playtest fixes. "
                +"The upstream integration is summarized in one entry at the end.");

        r=release(list,"v1.17.0","The Lurking Horror.");
        note(r,Icons.SKULL,"A solitary predator",
                "A rare living predator can stalk one ordinary floor in each region. An arrival omen hints at its presence. "
                +"Its ambush warning interrupts running and resting, leaving you a fresh action to move out of reach, reveal it or turn invisible. "
                +"It follows your current position rather than striking an abandoned cell. Another nearby hostile prevents its ambush.\n\n"
                +"Revealing it forces an exposed retreat and recovery before it can hide again. It can heal only a quarter of its maximum health over its lifetime. "
                +"Later regions bring stronger attacks and separately warned follow-up strikes; in the Halls it can flee through wooden barricades.");
        note(r,Icons.MAGNIFY,"Detection and fresh remains",
                "Mind Vision and Talisman scry reveal the Horror itself without uncovering surrounding terrain. Searching, prismatic light and an open Ashlight Lantern at +6 also counter it.\n\n"
                +"It may attempt one hunt of an ordinary sleeping creature. An actual distant kill can leave a death cry and fresh, species-specific remains. "
                +"Examine the remains separately from any dropped loot; after your first Horror kill, you can recognize its wounds. "
                +"Playtest includes direct spawning and controls for its regional chance, damage, evasion, flight and recovery.");

        r=release(list,"v1.16.1","Cavern salvage and shared settings.");
        note(r,Icons.STAIRS,"Expedition refinements",
                "The lower cavern has rough natural edges, safer fall arrivals and a broodmother farther from the landing area. "
                +"Both return routes use upward stairs. Mixed bone piles and adventurer remains favor common weapons, armor and ranged equipment, "
                +"with occasional rings and finite guaranteed food and torches. There is more salvage than one backpack can carry. "
                +"Playtest can tune the remains count, loot mix, equipment tier and upgrades.");
        note(r,Icons.PREFS,"Lasting discoveries and clearer plants",
                "Balance settings now apply across every game on this device and survive restarts; god mode stays local to its save. "
                +"The Hatchling can identify equipped items and belongings in every bag, while eating and equipment improvements retain their loose-inventory rules. "
                +"Dewcatchers and seedpods now have distinct painted forms.");

        r=release(list,"v1.16.0","The dragon expedition.");
        note(r,Icons.STAIRS_CHASM,"A map and a warning",
                "A wounded treasure hunter on a newly generated City floor offers an Expedition Map and Feather Fall in exchange for a Potion of Healing. "
                +"The map leads to a wooden-platform maze over a vast chasm, with an exit in one of eight positions. "
                +"The only retreat to the dungeon is through the Hoard Room's return exit, even while the dragon is alive.\n\n"
                +"The flying dragon warns before breathing fire or sweeping you from a platform. Its wounds persist across the expedition. "
                +"A fall sends you into the dark cavern below; the central descent offers a deliberate route down.");
        note(r,Icons.GOLD,"The cavern and the hoard",
                "Explore around stone pillars, fallen adventurers and cave spinners in limited light. A poisonous broodmother guards the climb back; "
                +"defeating her ends the brood threat and unlocks the return to the platform center.\n\n"
                +"The dragon protects a visible treasure hoard. Killing it carries you to the hoard and unlocks a one-time cache of exceptional loot, "
                +"potentially including an artifact and an additional trinket. Carried trinkets work together under their usual rules. "
                +"Playtest includes branch travel and controls for the bosses, supplies and rewards.");

        r=release(list,"v1.15.0","Tune the dungeon.");
        note(r,Icons.PREFS,"Balance tuning",
                "Playtest's balance menu controls enemy density and respawns, rare and curse-bound creatures, Hexcasters, Chainwarden, "
                +"floor loot, ordinary enemy drops, equipment tiers, upgrades, curses, enchantment rarity and item-category weights. "
                +"Each setting explains its limits and scope; reset restores Grimhollow defaults. "
                +"Generation changes affect future rolls and new or rebuilt floors. Guaranteed quest supplies remain protected. "
                +"These settings became shared across games in v1.16.1.");

        r=release(list,"v1.14.0-1.14.2","Effects, class balance and quest repairs.");
        note(r,Icons.BUFFS,"Painted effects and sharper creatures",
                "Grass rustling, curses, electricity, debris, droplets, mist, coins, frost and fire use softer painted particles. "
                +"Lightning and death, light and healing beams have painted cores. Gas is more visible while known loot remains readable through it. "
                +"Sharper exports cover creatures, merchants, NPCs, summons, bosses and sentries. "
                +"Raised-undead inspection shows remaining binding time; the Phylactery's summon menu is called Raise Dead.");
        note(r,Icons.TALENT,"Defensive Sigil and Ghouls",
                "Enchanter's Field Repair becomes Defensive Sigil: spend one Brush charge and one turn for 6/10 shielding at ranks 1/2, "
                +"lasting up to six turns. Recasting refreshes it instead of stacking. "
                +"Ghouls unlock at Phylactery +5 and heal their master for 15% of damage dealt. "
                +"Remains offer keepsakes appropriate to the current hero rather than unrelated class items.");
        note(r,Icons.CHANGES,"Tablet and quest fixes",
                "Hatchling meals show readable item names, and feeding warnings stop queued or held movement before giving the player a fresh action. "
                +"Status icons wrap within the panel; the enemy counter and movement-resume arrow use painted art. "
                +"Interrupted movement and knockback no longer leave a Shaman's image away from its occupied cell.\n\n"
                +"Repaired the wandmaker's summoning circle and candle instructions, workshop and mine atlas layouts, and overflowing terrain previews. "
                +"Rails and shelves join across cells; cauldrons match laboratory floors. Mining ore no longer emits torch flames or leaves floating fire. "
                +"Boss health bars and floor indicators have painted frames.");

        r=release(list,"v1.13.0","Hatchling Mimic and a new dungeon story.");
        note(r,Icons.BACKPACK_LRG,"Hatchling Mimic",
                "A new carried trinket eats eligible loose belongings and can identify, upgrade or enchant what remains. "
                +"Its feeding cycle speeds up with its three cauldron upgrades, costing 10/15/20 energy. "
                +"Examine it for hunger clues and the next gold demand. A warning gives you time to act.\n\n"
                +"Protected equipment, bags and their contents, quest items and class focuses are safe from feeding. "
                +"If food runs out it eats gold in a permanently doubling sequence. Consuming a loose artifact transforms it; "
                +"insufficient gold makes it escape. The later encounter's special reward uses the equipment pool of a +10 Ring of Wealth. "
                +"Ordinary and Golden Mimics recognize its kinship, Ebony Mimics enrage, and Crystal Mimics may try to steal it. "
                +"Item Sense reveals objects without mapping their surroundings.");
        note(r,Icons.JOURNAL,"Grimhollow's story and discoveries",
                "New regional introductions and journal lore tell Grimhollow's own story, accompanied by five painted loading screens. "
                +"Journal reference discoveries persist between games; potion, scroll and ring identities still reset for each run. "
                +"Grasp can open bones at Crystal level 3 and ordinary unlocked chests at level 7. "
                +"Known loot remains visible beneath gas, raised skeletons are sharper, and long descriptions scroll to their last line. "
                +"The Windows app has a custom icon and bundled runtime, and fullscreen remains open when focus changes.");

        r=release(list,"v1.12.0","Ranged inscriptions and smoother movement.");
        note(r,Icons.TALENT,"Inscribe and Lucky",
                "Inscribe supports carried thrown weapons and the Spirit Bow, filtering out any enchantment marked as melee-contact-only. "
                +"Thrown stacks preserve their inscriptions when split or merged. "
                +"Lucky now keeps a successful roll when multiple permanent, inscribed or etched effects are checked, including Overcharge. "
                +"Its description explains killing-hit and loot-eligibility requirements. "
                +"Skill descriptions show all ranks together instead of separate rank-toggle buttons.");
        note(r,Icons.DISPLAY,"Movement, travel and interface repairs",
                "Movement lighting updates use less work, with steadier creature poses and sharper Brutes and Shamans. "
                +"Tengu's smoke and electricity use painted effects; wall torches emit fire instead of inherited pipe drips. "
                +"Inventory numbers are larger and identification icons match their items. "
                +"Known walls remain at fog boundaries without revealing unknown overhangs.\n\n"
                +"Issue dialogs offer copyable diagnostic text. Class switching reuses the starter pouch and merges old duplicates without losing contents. "
                +"Travel into an unvisited floor chooses a valid arrival cell instead of placing the hero outside the map.");

        r=release(list,"v1.11.1-1.11.2","Class progression and tablet fixes.");
        note(r,Icons.TALENT,"Enchanter and meaningful talent ranks",
                "Enchanter's permanent enchantments and glyphs trigger 25% more often; temporary inscriptions and Rune Etching trigger twice as often. "
                +"These are chance bonuses, capped at certainty. "
                +"Talent purchases reject duplicate or stale offers, enforce rank limits and refund excess ranks from older saves.\n\n"
                +"Each new class retains three armor paths, each with four talents of four ranks. Repeated or ineffective ranks now give distinct benefits, "
                +"with explicit descriptions for Focused Mind, Far Reach, Precognition, Treasure Sense and the other class and armor talents.");
        note(r,Icons.CONTROLLER,"Touch, targeting and equipment",
                "Android steps have smoother visual timing and camera follow. Barricades span horizontal and vertical passages. "
                +"Handbook skills accept taps, and long skill, item and status descriptions scroll. "
                +"Hurl preserves its enemy-then-direction targeting. The Focus Crystal and Sigil Brush can be unequipped while retaining their growth and charges.\n\n"
                +"Infernal Brew supplies three Ashlight feeding units. Level-8 Flare ignites disguised hostile mimics. "
                +"Upgrade scrolls use a painted rising-gold effect.");

        r=release(list,"v1.11.0","In-game Playtest menu.");
        note(r,Icons.PREFS,"Build a test run",
                "Enable Playtest from the game menu to use optional god mode, create and edit items, recharge artifacts, "
                +"set hero level and strength, switch classes, choose subclasses and armor paths, and allocate talents. "
                +"Travel to main floors and quest branches, rebuild a floor, reveal the map, place creatures, teleport or set resources. "
                +"These saves are labeled Playtests and excluded from rankings, badges and bones. "
                +"Rebuilding a floor does not reset completed quests or collected one-time rewards.");

        r=release(list,"v1.10.0-1.10.1","Nine individual hero silhouettes.");
        note(r,Icons.DISPLAY,"Painted heroes in motion",
                "All nine heroes have individual proportions, garments, stances, strides and attack or casting gestures, with sharper painted frames. "
                +"The Warrior is broad and planted; the Rogue crouches; the Huntress draws her bow; the Duelist lunges. "
                +"Mage, Cleric, Necromancer, Enchanter and Psychic have distinct clothing and casting poses. "
                +"Each face and class palette remains recognizable as armor improves. World size and action timing stay consistent.");

        r=release(list,"v1.9.0-1.9.1","Ashlight Lantern and inscription input.");
        note(r,Icons.ALCHEMY,"Ashlight Lantern",
                "A fire-fed artifact with a free, persistent shutter. Feed Liquid Flame, Dragon's Breath or Soulfire to grow it; "
                +"charges accumulate in low ambient light, faster while shuttered. "
                +"Open light extends sight and attracts attention. Flare blinds nearby enemies, later igniting terrain and creatures. "
                +"Higher levels repel hostile wraiths, reveal secrets in direct light, and grant fire resistance or immunity. "
                +"Invisibility stays absolute, including the Cloak; using an open lantern with the Cloak doubles its charge drain. "
                +"Weapon and armor inscription rows now respond correctly to mouse and touch, including after scrolling.");

        r=release(list,"v1.8.0-1.8.1","Readability, inspection and scouting.");
        note(r,Icons.MAGNIFY,"Read the battlefield",
                "Locked doors have clear lock markers, and ground items, especially keys, have stronger outlines. "
                +"Gas uses soft overlapping clouds; altar motes and environmental particles are larger. "
                +"Awards have distinct painted medals. Repeated inspection can be locked by pressing Examine twice, including for shops and inventory.\n\n"
                +"The Focus Crystal extends Grasp at levels 3/7 and Glimpse at levels 5/10. "
                +"Gas overdraw was reduced and flame particles batched to lower rendering cost without changing hazard rules.");

        r=release(list,"v1.7.0","Plants, skill icons and lasting knowledge.");
        note(r,Icons.GRASS,"Painted growth and spellcraft",
                "Sprouted plants have distinct painted forms. New-class talents, subclasses, armor abilities and spells have custom icons. "
                +"Enchanter starts knowing Obfuscation, Swiftness and Viscosity for armor inscription alongside its starter weapon enchantments. "
                +"Learned inscriptions persist across floors and saves; only Rune Etching's active effect rerolls. "
                +"The alphabetized inscription library scrolls, includes descriptions and explains unavailable casts.");

        r=release(list,"v1.6.0","Portraits, handbook, traps and icons.");
        note(r,Icons.TALENT,"Choose your hero",
                "Nine matching painted portraits and class splashes replace shared character art. "
                +"Character selection presents each hero's theme; selecting again or opening information shows Profile, Growth, Paths and Armor. "
                +"Duelist is selectable without the old badge lock. "
                +"Enemy idle poses are steady, traps have painted mechanisms, and a shared iron-and-amber emblem supplies Windows and Android launcher icons.");

        r=release(list,"v1.5.0","Psychic growth and a painted interface.");
        note(r,Icons.TALENT,"Psychic and Enchanter openings",
                "Psychic thrown weapons use an effective upgrade floor of +1/+2/+3/+4/+5 at hero levels 1/6/12/18/24, "
                +"improving damage and durability without stacking with real upgrades. "
                +"The Focus Crystal starts with three charges and gains levels through charges spent. Push gains distance and riders as the Crystal grows; "
                +"Seer's Hurl stays stronger, and Puppeteer gains direction and lasting floor-bound enthrallment.\n\n"
                +"Enchanter starts with three Brush charges and inscription knowledge of Blazing, Shocking and Chilling, "
                +"without identifying those effects on found gear. Curse-bound creature descriptions explain their aura and curse.");
        note(r,Icons.BACKPACK_LRG,"Inventory and menus",
                "Painted bronze and leather panels, enamel buttons, equipment borders, glass status bars and navigation symbols refresh the interface. "
                +"Item artwork covers weapons, armor, artifacts and their states, potions, scrolls, seeds, stones, spells and quest objects. "
                +"Item descriptions scroll while their actions remain accessible, and class spell menus fit portrait and landscape.");

        r=release(list,"v1.1.0-1.4.0","The painted world and complete bestiary.");
        note(r,Icons.DISPLAY,"A darker painted dungeon",
                "Painted terrain, animated water, walls, doors, grass states, props and lighting replace the earlier visual style across all five regions. "
                +"The title screen gains painted art and animation. Creatures, bosses, NPCs, summons, mimics and sentries receive matching painted artwork, "
                +"with species-specific scale, larger heroes, cleaner texture edges and better health-bar placement. "
                +"Status emblems and alert cues are painted, and wall torches match the regional masonry.");
        note(r,Icons.CHANGES,"Crashes, text and item identity",
                "Fixed the render-thread crash encountered during Necromancer play, including item drops and death effects. "
                +"Repaired missing names and formatting in descriptions. Gravity and Blast Wave now display their separate names and correct descriptions, "
                +"so a pulling wand is no longer mislabeled as a knockback wand.");

        r=release(list,"v1.0.1-1.0.2","Rendering recovery.");
        note(r,Icons.MAGNIFY,"Fog, terrain and item repairs",
                "Corrected remembered terrain, fog scale, camera alignment and lighting during movement and door transitions. "
                +"Fixed item-sheet mismatches, including Waterskin and Mind Vision icons. "
                +"Restored the earlier world art and upstream characters as a recovery baseline; the later painted releases superseded those temporary visuals. "
                +"Menus and project links were cleaned up while preserving attribution.");

        r=release(list,"Foundation-v1.0.0","Three new heroes and Grimhollow content.");
        note(r,Icons.TALENT,"Necromancer, Enchanter and Psychic",
                "Grimhollow adds three heroes to the original six, each with its own equipment, talents, two subclasses and three armor paths. "
                +"Necromancer raises servants and casts curses through the Phylactery, with Deathspeaker and Hexweaver paths. "
                +"Enchanter uses the Sigil Brush, Runecraft and Rune Etching, with Artificer and Scrivener paths. "
                +"Psychic uses the Focus Crystal for telekinesis and perception, with Puppeteer and Seer paths. "
                +"Class progression, minion behavior, charges and saved knowledge received subsequent playtest revisions recorded above.");
        note(r,Icons.ALCHEMY,"Equipment, enemies and curses",
                "New content includes Wands of Necrosis, Gravity and Bone; the Soulfire spell; Bone, Reaper's and Grave Scythes; "
                +"Bone Armor; and the Hourglass of Ashes. Added item curses include Leech, Echo, Withering and Dark Blessing. "
                +"Rare Hexcasters, curse-bound creature variants and the alternate Prison boss Chainwarden bring new threats. "
                +"Painted effects, blood and scorch marks, dynamic lighting and the Grimhollow title and identity grew through the foundation releases.");

        release(list,"Upstream foundation","Incorporated Shattered Pixel Dungeon 4.0's expanded Imp/Vault quest, content additions, enchantments, curses and bug fixes.");
    }
}
