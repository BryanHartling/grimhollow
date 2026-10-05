# Grimhollow

**Playtest repairs v1.25.1:** older saved games without elemental treasury plans load safely. Device-wide balance settings keep applying across games, while Playtest tools now require explicit activation per save. Tuned games remain excluded from rankings and badges. New Enchanters have exactly three starting weapon enchantments and three armor glyphs; free Rune Etching rolls no longer expand their permanent library. Previously learned knowledge is preserved.

Heroes use their generic painted poses instead of floating weapon icons. Haste has short soft wisps at the boots during movement, fading when stationary; mechanics and timing are unchanged.

**Wayward Chart v1.25.0:** a new carried trinket points to painted treasure mounds without exposing nearby terrain. Strengthening it improves discovery and treasure. Magic Mapping settles an unfulfilled regional promise; a defeated dragon has one additional overlooked pocket. Taking treasure can fade an explored room on any visited floor in that region. Earlier losses remain until those rooms are revisited or their floors mapped. Terrain, exploration flags, visible cells, doors, stairs and known hazards are not rewritten.

**Playtest > Balance tuning > Wayward Chart** exposes discovery at all four ranks, gold, item counts, quality comparisons and the size of faded room memories. Settings persist across games. Its painted Chart, treasure mound states, treasure X and memory veil reproduce from [committed sources and prompts](tools/painted/wayward-prompts.json) through the offline packer; CI needs no generation service.

**Fickle Doubloon v1.24.0:** a new artifact grows through coin flips, favors or hinders the hero's combat rolls, rewards held gold, improves eligible ordinary loot and invites thief pursuit. Black Spot copies rig the wager against you. At its highest level, the coin can improve future shops and treasure generation, including elemental caches without relaxing their one-per-type limits.

Feeding it to a Hatchling makes an irreversible wager: Heads trades both items for a directable Golden Mimic guardian; Tails releases a stronger hostile mimic carrying the recoverable coin. The guardian steals finite gold once per eligible enemy, roots ordinary enemies, collects visible safe loose gold and retreats into its harness when defeated. Recovery needs both time and entry to a different floor. Neither outcome permanently removes an artifact slot.

**Playtest > Balance tuning > Fickle Doubloon / Golden Mimic companion** exposes 20 adjustable values, including Heads chance, charging, fortune, theft resistance, room/cache chances, special shop rolls, guardian health/damage, stealing, roots, recovery and collection range. Settings persist across games on this device and mark affected runs as custom balance without automatically enabling tools. Reset restores the agreed defaults for future games.

The two painted coin faces and companion harness are packed from `tools/painted/sources/items/doubloon.png`, with the generation prompt in `tools/painted/doubloon-prompts.json`. The unused neutral concept is excluded from the shipped atlas. The coin initially displays Heads without granting an active Favor effect, then displays its last flip. The offline packer regenerates the item atlas and semantic hashes without a generation service. Older item IDs and cells retain their positions.

**Readable controls v1.23.3:** collected keys have a separate framed HUD row with larger painted icons and counts. Long item, creature and talent descriptions have visible scroll arrows and a bronze scrollbar, with touch dragging/mouse-wheel input and extra final-line padding. Ashlight feeding text uses plain readable hyphens. All four raised servant types decay for **15% of maximum health per turn, rounded up**, after their binding expires.

**Bone wall and Horror v1.23.2:** Wand of Bone/Bone Prison have stable painted barricades and an examination result with remaining turns. The Horror has modestly stronger combat damage and a separate stronger sleeping-prey pounce, with the same damage tuning setting. Its floor omen is a framed six-second notice; the larger ambush warning remains visible until you respond. Detection, response fairness and recovery limits are unchanged. Balance still requires campaign playtesting.

Latest local Windows launcher: `desktop/build/windows/1.25.1/Grimhollow/Grimhollow.exe`; APK: `android/build/outputs/apk/debug/android-debug.apk`. The previous Chart checkpoint remains under `windows/1.25.0-release`.

**Talent panel v1.23.1:** rounded bronze studs replace the blocky gold rank markers. Larger ivory available, bronze spent and hollow future tier markers accompany an explicit available/spent count. Random allocation has a brass shuffle medallion and a larger hit area. Allocation rules, point costs and caps are unchanged.

v1.23.1 corrects equipment grips on all nine painted heroes, including the Necromancer's Sickle. Weapons anchor to visible palms rather than robe edges; curved/narrow handles use their actual painted positions. Short blades hang naturally and hooked blades/polearms stay upright. Existing checks cover all eight armor appearances and both facings. Gameplay and source paintings are unchanged.

**Haste after freezing:** thawing in water leaves five turns of Chill. Haste does not remove it, so a hasted step can still take two thirds of a turn and trigger a red sentry's travel interruption. Dry-ground thawing does not leave Chill. Potion/status descriptions now explain this; the original save is unavailable, but the timing sequence is reproduced.

Talent/grip checkpoint Windows launcher: `desktop/build/windows/1.23.1/Grimhollow/Grimhollow.exe`; the latest APK path is listed above.

v1.23.0 refines the playtesting build: hero-level refreshing Necrotic Touch, gradual minion decay, Phylactery spell information, visible golden talent sockets and HUD keys, corrected equipment grips and torch depth, painted pointer/stairs/well/statue, restrained high-contrast identity symbols and a framed update screen. Smooth text is the default; a player's explicit font preference is preserved. All nine painted heroes from v1.22.6 remain.

Windows launcher: `desktop/build/windows/1.23.0/Grimhollow/Grimhollow.exe`; Android: `android/build/outputs/apk/debug/android-debug.apk`. See [acceptance evidence](verification/ACCEPTANCE.md), [known issues](KNOWN_ISSUES.md), [presentation artwork](verification/interface/presentation-polish.png), [identification symbols](verification/interface/identity-emblems.png) and the nine-hero previews under `verification/heroes/`. Physical Samsung tablet and campaign balance review remain with the user.

**Necromancer:** Necrotic Touch deals hero-level damage per turn for one/two turns, refreshed without stacking. Once a servant's binding expires it loses 15% of maximum health, rounded up, each turn; it does not explode or revive on decay death. Phylactery actions and summon options have information buttons, and the hero handbook explains Wither and the servants. Curse-dependent talents explicitly refer to Phylactery curses rather than arbitrary debuffs.

**Haste/red sentry:** no rule change. The generated-room regression passes all four approach directions with no hasted shots and retains normal-speed danger. This does not reproduce the reported campaign encounter; Haste remains speed, not immunity, and slowed movement, searching and other actions can still allow the sentry to finish charging.

v1.19.2 refreshes Adventuring Notes with painted terrain/landmark icons and fitted NPC portraits. Discovered elemental treasuries receive persistent fire, water or lightning reminders, including preparation tips, until their seals are opened. The home-screen Update Log describes these releases.

**Terrain contrast test 45: Abandoned: test failed.** At the user's request (2026-10-01), it no longer runs locally or in CI. The last 17/76 failed comparisons remain historical evidence, not a passing result. This supersedes older test-45 enforcement statements below; fog, visibility, geometry, art-provenance and gameplay checks remain active.

**Playtest access (v1.20.2):** choose **Playtest** on the home screen. Balance tuning works without loading a run and applies across games. Choose **Open tools for a saved run**, or **Start a new run for testing**, to load a dungeon and open God mode, item creation, progression and travel controls. Select **Enable Playtest for this save** once; its in-game menu then includes **Playtest** for reopening all tools without leaving the dungeon. Existing Playtest saves already have access, which persists across reloads and floor changes. Ordinary saves do not show the shortcut. Opening the menu alone does not mark a save as a Playtest.

**Enchanter craft and elemental treasuries v1.19.0:** Spellguard replaces Overload (10%/20% less magical damage while worn armor has a live temporary inscription). Journal/menu controls and journal categories now use painted artwork.

**Scribe:** use Sigil Brush or Blank Parchment to write a regular scroll identified this run, without a pot. Each action takes **3 turns, 1 parchment, 1 Brush charge and 12 energy** (20 for Transmutation). Upgrade and exotic scrolls cannot be written. Recycling any scroll returns one parchment per scroll; Enchanter can also find a few blank sheets. Wandering Brush permits inventory scribing.

**Elemental treasuries:** look for a brazier, empty fountain or lightning rod. Apply fire, create water (or **Pour** a full Waterskin), or apply electricity to open its hidden door. Searching has half its normal chance; mapping, prismatic light and foresight reveal the door but leave its seal locked. Skeleton Key bypasses cost **4/5/6 charges**. Each type can appear **once per run**, on independent rolls that increase by region, averaging **1.69 rooms** over 10,000 seeds. Ordinary secret-room slots are unchanged. Gold, chest count and equipment quality increase with depth. New controls are in **Playtest > Balance > Elemental treasuries**; existing generated floors are not retrofitted.


**Torch and armor Etching v1.18.3:** wall flames and their light require sight of the wall's facing side, so viewing its back cannot reveal a torch in an unseen room. Rune Etching descriptions resolve the effect name, upgrade and 25% bonus correctly.

**Sigil Brush > Etch** now offers your equipped melee weapon or armor. This moves the same single rune and its one carried upgrade for **one turn, no charge**. Armor receives a common glyph (Obfuscation, Swiftness, Viscosity or Potential), alongside its permanent glyph and temporary inscription. The weapon enchantment and armor glyph roll once on entering a floor; switching carriers never rerolls them. Existing weapon runes and saves remain compatible.

**Terrain readability v1.18.2:** restrained regional ambient color and a nearly neutral personal light preserve the painted material colors; torches and fire retain their warm light. Flattened grass has a fuller spread of low leaves. Visibility and gameplay are unchanged. This is a visual correction for review, **not a passing declaration for test 45**; its thresholds remain enforced.

**Current plan:** tablet movement performance is **resolved per user playtesting**. Campaign balance remains with the user for continued playtesting. Psychic Pull is skipped. **Spellguard is implemented in v1.19.0**, replacing Overload as described above. These decisions supersede older pending-performance and replacement-choice notes below. The Hatchling UI fixture now explicitly marks its test heap undiscovered; see [known issues](KNOWN_ISSUES.md).

**v1.18.1 balance and readability:** the home screen now opens **Update Log** directly. Target portraits fit the creature rather than its transparent frame. Healing hearts are red, with painted combat/pickup/spell notifications. Hatchling manual feeding requires hunger (last quarter of its interval); Item Sense marks one nearest undiscovered loot pile for 20 turns within 5/8/12/16 cells. Each meal upgrades OR enchants, favoring upgrades 75% when both are possible. Exceptional upgrades stay +2; rings remain Exceptional; identification only reaches owned belongings.


**Playtest polish v1.18.0:** open **Menu → Message History** to scroll through the latest 500 messages; history follows the save across floors and reloads. Examine the **Hatchling Mimic → Feed** to choose an eligible loose item. Feeding costs one turn, applies its usual effects, and restarts the hunger interval. Automatic feeding remains active.

All nine heroes now identify their starting belongings; Enchanter adds three steel-tipped darts. Defensive Sigil lasts 6/10 turns, Rune Etching works at full strength with +25% proc chance, Resonance is 1.2x/1.3x, Wandering Brush allows inventory casting and 50%/75% recharge, and Appraisal gives collected gear a one-time 20%/30% identification roll. Existing talent ranks carry over. Overload was subsequently replaced by Spellguard in v1.19.0. Stairs and eating use new painted art; garden motes are soft, and the hidden Rat King crown no longer reveals its room.

**Update Log v1.17.1:** the welcome-screen Update Log now presents Grimhollow's shipped releases, newest first, with one upstream 4.0 foundation entry. It covers the new heroes, painted visuals, artifacts and trinket, expedition, Lurking Horror, Playtest tools, balance changes and repairs. Long entries scroll in portrait and landscape.

**Lurking Horror v1.17.0:** a rare living ambush predator can stalk one newly generated ordinary floor in each region. An arrival omen hints at its presence. Its attack warning stops automatic movement and leaves you a fresh action to evade, reveal it, or turn invisible. Once exposed it stays vulnerable through flight and recovery; it can recover only 25% of its maximum health over its lifetime. Mind Vision shows the creature without revealing nearby terrain. Search, Talisman Scry, prismatic light and an open Ashlight Lantern at +6 also counter it.

It may attempt one hunt of an ordinary sleeping creature. A distant death cry marks a real kill; exploring the cell reveals species-specific **fresh remains**, which can be examined separately from dropped loot. Killing a Horror teaches the bestiary entry and lets you recognize its wounds. **Menu → Playtest → Balance tuning → Lurking Horror** controls regional chance, damage, evasion, flight duration and recovery allowance; warning fairness is fixed. Its regional roll can be skipped if the chosen floor has no suitable empty room. Existing generated floors are not retrofitted.

For a quick encounter, use **Playtest → Spawn a creature → Lurking Horror** on an otherwise empty floor. It scales to the current region. God mode is optional; extra nearby hostiles deliberately prevent it from ambushing you.

**Playtest follow-up v1.16.1:** Hatchling identification reaches equipped gear and every bag. Balance settings are shared across games on this device. The expedition cavern has natural edges, safer fall arrivals, equipment-heavy remains and upward exits. Dewcatchers and seedpods now have distinct painted sprites. Existing cavern geometry and loot need a new or rebuilt floor.

**Dragon expedition v1.16.1:** find a wounded treasure hunter on one newly generated City floor (16–19). Give one Potion of Healing to receive an Expedition Map and an Elixir of Feather Fall. Open the map beside the hunter to enter a wooden-platform maze over a deep chasm. The only way back to the dungeon is through the Hoard Room's return exit, which remains usable even while the dragon lives.

The dragon flies, warns before breathing fire or sweeping you off a platform, and retains damage between visits. The lower cavern has rough natural edges, limited sight, six scavengers nearer the landing area, and a distant poisonous broodmother. Its 48 mixed bone piles and adventurer remains favor basic tier-1–3 equipment, with occasional rings, three guaranteed rations and four torches. The Cavern remains loot menu controls count, mix, tier ceiling and upgrades. Defeat her to clear the cavern and unlock the climb back to the center. Killing the dragon carries you to the hoard and unlocks its one-time treasure. The reward can include an artifact and an additional, distinct trinket; carried trinkets retain their normal simultaneous effects.

For rapid testing, open **Menu → Playtest → enable for this save → Travel to any floor / quest branch** and select the expedition platforms, cavern or hoard. God mode is optional. **Balance tuning** has four expedition sections with 29 bounded controls for the offer, bosses, sight, falls, brood limits, supplies and rewards. Rebuilding floors does not reset completed quest history or recreate collected hoard rewards; start a new test save for a fresh expedition.

Existing saves remain compatible, but the hunter is not retroactively inserted into an already generated City floor. A new run is the reliable way to experience the normal quest. All eight components have committed checkpoints; see [verification](verification/ACCEPTANCE.md) and [known issues](KNOWN_ISSUES.md). The existing terrain-contrast gate remains enforced and is still a known failure; no full green CI or physical-tablet playtest is claimed.

Local launchers after building: `desktop/build/windows/1.22.6/Grimhollow/Grimhollow.exe` (keep its whole folder), `desktop/build/libs/desktop-1.17.1.jar`, and `android/build/outputs/apk/debug/android-debug.apk`. Android is a debug-signed sideload build.

Original expedition paintings, exact generation prompts and the offline packing recipe are in [sources](tools/painted/sources/expedition/), [prompts](tools/painted/expedition-prompts.json), and [packer](tools/painted/expedition.py). Native screenshots are in `verification/interface/{landscape,portrait}/expedition-*.png`; CI reconstructs the committed sources without an image-generation service.

**Balance tuning v1.15.0:** open the game menu > **Playtest** > enable it for this save > **Balance tuning**. God mode is optional. Twenty-seven controls adjust enemy population/respawn speed, rare variants, curse-bound chance/bonus health/bonus loot, Hexcaster rotations, Chainwarden, random floor loot, ordinary enemy drops, equipment tiers/upgrade/curse/enchantment rates, rare enchantments, Bone Armor substitution, and eleven item-category weights. Each entry shows its current value and explains its range, default, and scope.

Balance settings apply to every game on this device, including existing saves and new games, and survive app restarts. Runs using custom balance are marked Playtests; god mode and direct actions remain save-local. Reset restores current Grimhollow defaults across all games; existing Playtest markers remain and future default runs can be ordinary games again. Older saves cannot restore superseded tuning. Newly generated content uses the new values; existing gear, enemies and visited floors are not rebuilt automatically. Use Playtest's floor rebuild when comparing generation settings. Guaranteed progression supplies and quest rewards are not removed or multiplied. Randomized reward equipment can still use the quality and tier settings. Custom category weights use independent weighted category draws in place of the general category deck; specific-item decks and artifact uniqueness remain, including the normal ring fallback when artifacts are exhausted. Set curse-bound chance, Hexcaster chance and Chainwarden chance to zero to test without new spawns of those Grimhollow enemies.

**Tablet rooms and quest visuals v1.14.2:** corrected ritual/smithy atlas layouts, contained terrain-inspection images, painted mining floors/boulders/crystals/ore and workshop props, connected mine rails and bookshelves, matching laboratory floor, and painted boss/floor indicators. Ore walls no longer emit torch flames or torch light, and removing a torch's terrain removes its flame. Chainwarden remains the intentional alternate Prison boss; Corpse Sense retains remote minion vision. [Changes](CHANGES.md) and [verification](verification/ACCEPTANCE.md).

**Hatchling and HUD fixes v1.14.1:** readable item names in feeding messages, a warning that stops queued actions and waits for player control, contained status icons, and painted resume/enemy indicators. This update includes the v1.14.0 particle and creature improvements below. [Full changes](CHANGES.md).

**Painted effects and balance v1.14.0:** soft grass, curse, electricity and related particle effects; clearer gas; sharper exports across the creature roster. Enchanter Field Repair becomes **Defensive Sigil** in the Brush menu: one charge and one turn for **6/10 shielding**, lasting up to six turns and refreshing without stacking. Ghoul unlock moves to **Phylactery +5**, with **15%** hero healing. Raised-undead inspection shows time remaining; the summoning menu now says **Raise Dead**. Remains give only the current class's keepsake. [Changes and balance decisions](CHANGES.md).

**Hatchling Mimic and presentation v1.13.0:** a hungry carried trinket with permanent item benefits, escalating gold demands, mimic kinship and theft. Its transformation/escape reward uses the actual **+10 Ring of Wealth equipment pool**. Upgrade at the cauldron for **10 / 15 / 20 energy**. Inspect it for hunger clues and its next gold demand. The Playtest item picker now includes 314 item types, including the Hatchling.

Five original painted loading screens and a new regional story accompany fixes for unknown-wall leakage, loot visibility beneath gas, scrolling skill descriptions, sharpened skeletons and persistent journal discoveries. Grasp opens bones at Crystal level 3 and ordinary unlocked chests at level 7.

For the native Windows launcher with the custom taskbar icon and bundled Java, build `desktop:dist`, then run `powershell -NoProfile -ExecutionPolicy Bypass -File tools/package-windows.ps1`. Launch through the existing Grimhollow shortcut or `tools/play.bat`; it selects the matching packaged executable. Fullscreen now stays open when another application gains focus.

**Enchanter and talent fix v1.11.2:** permanent enchantments/glyphs trigger 25% more often; temporary inscriptions and Rune Etching trigger twice as often. Chance-only bonuses start at level one. Talent purchases enforce rank caps and reject duplicate/stale offers; valid tier-four talents still have four ranks.

**Tablet playtest fixes v1.11.1:** touch movement uses a longer visual step and smoother camera following; barricades span both corridor orientations and unknown neighboring walls no longer disclose their overhangs. Hurl retains its direction-selection step. The Focus Crystal and Sigil Brush can be unequipped; stored growth/charges remain, while casting and passive charging require equipping them.

The class handbook now accepts actual taps on skills. Talent windows offer all-rank scrollable descriptions; long titled descriptions also scroll. Each of the three added classes keeps three armor paths, with four talents of four ranks per path. Focused Mind adds 1/2/3 capacity; Far Reach adds 1/2/4 Grasp range; Precognition prevents one/two qualifying hits per floor; Treasure Sense adds loot/hidden doors/hidden traps across its three ranks. Kinetic Surge grants 12.5/25/37.5/50 percent thrown damage during Mind Meld. All added-class rank descriptions state their effects explicitly.

Infernal Brew feeds Ashlight **three units**, giving three early levels or the normal two-unit cost per level above +6. A level-8 Flare also reveals and burns disguised hostile mimics. Open light alone does not burn creatures. Upgrade scrolls now show a rising gold sigil effect. See [changes](CHANGES.md), [verification](verification/ACCEPTANCE.md), and [known limitations](KNOWN_ISSUES.md). Install the new APK over the existing app to preserve saves; physical tablet feel still needs player review.

**In-game playtesting v1.11.0:** open the pause/game menu (Escape on desktop), choose **Playtest**, then **Enable Playtest for this save**. Controls use paged buttons in desktop and portrait layouts. Enabling permanently marks that save `PLAYTEST` and excludes it from rankings, badges, catalog usage credit and bones; other saves remain normal. God mode is optional and can be turned off for balance testing.

- **Create items:** search 313 concrete item types or browse categories, including artifacts, class focuses, trinkets, crafted spells, weapons, armor, potions and quest items. Set stack size, upgrade level, identification and curse state. Keys use the current floor; inventory overflow drops at your feet.
- **Edit equipment:** upgrade/downgrade, identify, recharge, bind/unbind curses or choose weapon enchantments and armor glyphs. Artifact levels map to each artifact's native cap; trinkets stop at +3.
- **Hero setup:** change among nine classes with their starter kits, set level 1–30 and strength 1–50, select subclasses and armor abilities, and maximize/reset talents. Enchanters can learn the full inscription library. Class changes keep your floor, level, strength and backpack; old equipment is collected or dropped, and temporary effects/allies are cleared.
- **Floor travel:** jump to floors 1–26, mines or Vault branches, return to visited floors, or rebuild the current floor to repeat encounters. Bosses use their proper generated arenas. Reveal the map/secrets, teleport to an empty walkable cell, or place regular and rare creatures.
- **Recovery/resources:** restore health/food, clear harmful effects, refill charges, identify carried items, or set gold and alchemy energy.

For a quick late-game test: enable Playtest, turn on God mode, set level and strength, select a subclass/armor ability, create or upgrade equipment, then travel to floor 21. Turn God mode off when ready to test balance. Set talents after changing level, subclass or armor ability, because those actions reset allocations. Direct Vault travel keeps your loadout for sandbox testing; use this menu to return. Use the normal quest entrance when testing the Vault's equipment-stripping/reward flow. Rebuilding a floor replaces that floor's terrain, creatures and loot; it does not reset completed quests.

**Nine hero art batches v1.10.1:** each hero now has individual proportions, silhouette, posture, stride and attack/cast gestures. Broad Warrior shoulders, the Rogue's forward guard, the Duelist's lunge and four distinct caster garments retain each face and cloth palette through armor upgrades. Painted 96x120 frames preserve more source detail at the same world height and unchanged action timing. [All-nine comparison](verification/heroes/lineup.png), [individual animation and pose reviews](verification/painted-world.html), [source prompts](tools/painted/hero-rigs-prompts.json).

**Inscription input v1.9.1:** real mouse/touch events now reach the Enchanter's scrollable weapon and armor inscription rows. Info buttons, scrolling and drag cancellation work; no knowledge or balance rules changed.

**Ashlight Lantern v1.9.0:** a new fire-fed artifact with painted open and shuttered states. Feed identified Liquid Flame (1 unit), Dragon's Breath or Soulfire (2) to reach levels 0–10; fourteen units reach the cap. Charges refill only away from environmental light, twice as fast when shuttered. Open light expands sight and enemy awareness; Flare blinds nearby enemies, gaining terrain/enemy ignition with level. Later levels repel hostile wraiths, reveal directly lit secrets and resist fire. Shuttering is free and persists across equipment changes and saves. Invisibility always wins: the Cloak stays fully invisible, with twice the charge drain while the lantern is open. [Rules and interactions](CHANGES.md), [exact painted-source prompt](tools/painted/ashlight-prompts.json).

**Readability and inspection v1.8.1:** locked doors have painted lock markers, ground loot has contrasting edges, gas has soft cloud boundaries, and wall water/smoke, altar motes and all 98 awards use sharper painted detail. Known southern wall faces remain visible at the edge of unexplored terrain. Press Examine twice to keep inspecting tiles, shop goods and inventory; press again or Escape/Back from the dungeon to exit. Hold Examine to search. The Focus Crystal adds Grasp reach at levels 3/7 and longer Glimpse at levels 5/10. The patch halves gas-cloud overdraw and batches unchanged flame/ember particles after v1.8.0 exceeded the Linux effects budget. [Visual review](verification/painted-world.html), [verification and retained limitations](verification/ACCEPTANCE.md).

**Plants and inscriptions v1.7.0:** thirteen painted sprouted plants and 113 unique new-class skill icons complete this pass. Enchanter armor inscription now has three starter glyphs; all learned inscriptions remain available across floors and saves. The full library scrolls and offers descriptions. Only the Rune Etching's active effect rerolls. See [the visual review](verification/painted-world.html), [acceptance results](verification/ACCEPTANCE.md), and [exact art prompts](tools/painted/botany-skills.json).

GPL-3.0-or-later derivative of [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon), now incorporating **v4.0.0**, commit `2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f`. The fork began at v3.3.8; upstream history and Java packages are preserved.

**Painted heroes and traps v1.6.0:** nine distinct class paintings supply matching selection and in-game portraits. First selection shows the class theme and painting; a second selection or the info button opens Profile, Growth, Paths and Armor, with scrollable descriptions and inspectable talents. Duelist is selectable without the old badge lock. Other existing class unlock requirements still control Start, while every class can be previewed. Enemy idle poses are steady; movement, attacks and death animations retain their timing and callbacks.

Seven painted trap mechanisms preserve all nine color/state indices. One original iron-and-amber emblem supplies the Windows icon and Android legacy, adaptive and themed icons. Run `powershell -NoProfile -ExecutionPolicy Bypass -File tools/install-shortcut.ps1` to install the desktop shortcut; it launches the latest built jar through `tools/play.bat`.

Ninety-six committed source sheets reproduce 112 game images and 55 launcher resources. The exact built-in imagegen prompts are in [presentation-prompts.json](tools/painted/presentation-prompts.json). No generation service is needed to build or verify them. Class splashes now use these new paintings; older JPGs remain historical assets.

**Psychic and interface v1.5.0:** painted bronze/leather panels, enamel buttons, equipped-slot borders, glass status bars and 32 navigation symbols replace the shared interface graphics. All 381 named item IDs now resolve to painted 64px art, including complete artifact states, scrolls, darts, seeds, stones, crafted spells and quest objects. Item descriptions scroll while action buttons remain visible, and the class spell wheel fits portrait and landscape.

Psychic thrown weapons use a non-stacking effective upgrade floor of +1/+2/+3/+4/+5 at hero levels 1/6/12/18/24, reducing durability consumption. The Focus Crystal starts with three charges and gains levels only from charges spent; tiered Push, stronger Seer Hurl and floor-bound Puppeteer enthrallment provide its growth track. Enchanter starts with three Brush charges and Blazing/Shocking/Chilling inscription knowledge, without identifying found items. Curse-bound monster variants now explain their aura and permanent curse when inspected.

Eighty-five original source sheets and exact imagegen prompts are in [tools/painted](tools/painted/README.md). The offline packer rebuilds 102 shipping images, validates animation-frame coverage and checks item identities. CI requires no generator or Blender. See the [interface screenshots](verification/interface/), [visual review](verification/painted-world.html), [bestiary](verification/monsters.png) and [terrain states](verification/terrain-states.png). All 71 creature atlases (129 forms), nine heroes and status emblems retain their painted art. Class splashes, identification overlays, talent/region/credit icons and special-room artwork retain prior art.

The v1.4.0 namespace repair and v1.3.2 render-thread crash fix remain in place. Gravity and Blast Wave have separate verified descriptions and behavior. This pass changes only the requested Psychic/Enchanter rules and presentation; other classes, content and world rendering are retained.

**Rendering fix v1.0.2 retained:** fog has one nearest-sampled texel per 16-unit world cell. Shader camera caches track changing transforms, so walls and lighting follow camera pans and door movement. Painted textures use linear filtering with half-texel atlas guards; fog remains nearest-sampled. Mind Vision keeps the corrected eye symbol.

**Historical recovery baseline v1.0.1:** terrain and scrolling water were restored from `v0.3.2-fixup2`; characters use upstream v4 pixels with fixed palette swaps for the new classes. The approved title, Sewers doors/torches and UI/talent icons remain. The old art generators and Blender cache remain archived and unused; the current painted-source pass described above supersedes that art scope. Recovery acceptance is in [RECOVERY-spec-v1.0.1.md](RECOVERY-spec-v1.0.1.md).

The unchanged numerical terrain distinctness gate is measured separately from art review; see [KNOWN_ISSUES.md](KNOWN_ISSUES.md) and [the acceptance report](verification/ACCEPTANCE.md) for actual results. Actual lit rooms and corridor/turn/door screenshot sequences are in [verification/recovery](verification/recovery/).

All nine heroes, the expanded v4 Imp/Vault quest, new enchantments and curses, and Grimhollow's added content remain available.

## Build on Windows

Requires Temurin **JDK 17**. Install JDK and Android tools locally, then configure the session:

```powershell
.\tools\bootstrap-windows.ps1
. .\tools\env.ps1
.\gradlew.bat desktop:run -PdesktopOnly=true --no-daemon
```

Desktop-only excludes the Android module and Android plugin and works with no SDK or `ANDROID_HOME`. For a runnable jar:

```powershell
.\gradlew.bat desktop:dist -PdesktopOnly=true --no-daemon
java -jar desktop\build\libs\desktop-1.16.1.jar
```

`desktop:dist` aliases upstream's `desktop:release` fat-jar task. `desktop:run` supplies required launcher metadata. Linux/macOS use `./gradlew`; on macOS the run task adds `-XstartOnFirstThread`.

Full build needs SDK platform 36 and build-tools 36.0.0:

```powershell
. .\tools\env.ps1
.\gradlew.bat desktop:dist core:test android:assembleDebug --no-daemon
```

Paths on this host:

- `JAVA_HOME=C:\Users\Hartl\Documents\grimhollow\.toolchain\jdk-17` (Temurin 17.0.20.1+1).
- `ANDROID_HOME=C:\Users\Hartl\Documents\grimhollow\.toolchain\android-sdk`.
- `GRADLE_USER_HOME=C:\Users\Hartl\Documents\grimhollow\.gradle-user-home`.
- Python 3.12.14: `C:\Users\Hartl\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`.

Google's current tools delegate sdkmanager to Android CLI. The bootstrap uses slash-separated package names to avoid the Windows wrapper splitting semicolons. Downloads are checked against publisher checksums. The CLI reported a nonzero result even after installing the packages; actual Gradle packaging subsequently succeeded. No system environment changes are needed.

## Install Android

Download `grimhollow-android-debug.zip` from GitHub Actions and extract the APK. Enable Developer Options and allow **Install unknown apps** for the app opening the APK, then open and install it. With USB debugging:

```powershell
adb install -r android\build\outputs\apk\debug\android-debug.apk
```

The exact debug application ID is `com.grimhollow.dungeon` (no `.indev` suffix); label **Grimhollow**. It installs alongside SPD. Desktop saves/settings use `%APPDATA%\.grimhollow\Grimhollow\`, distinct from upstream. Linux uses `$XDG_DATA_HOME/.grimhollow/grimhollow` (or `~/.local/share/.grimhollow/grimhollow`); macOS uses `~/Library/Application Support/Grimhollow`.

## Verify

```powershell
. .\tools\env.ps1
.\gradlew.bat core:test core:smokeRun -PsmokeUpstream=true -PdesktopOnly=true --no-daemon
python tools/recovery_assets.py --check
java "-Dgrimhollow.recovery=true" "-Dgrimhollow.fogTests=true" "-Dgrimhollow.region=0" "-Dgrimhollow.geometryTests=true" "-Dgrimhollow.effectsTests=true" -jar desktop/build/libs/desktop-1.7.0.jar --smoke-sewers
python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.7.0.jar
```

Repeat the recovery renderer with region indices 1–4 for Prison, Caves, City and Halls. It walks normal adjacent moves on generated terrain in diagnostic slot 99, captures remembered terrain and verifies remembered-cell fog compositing. Test 47 checks all pixels of every visible and never-seen cell in five generated regions, before and after walking, at three zooms and four camera offsets, plus the shared wall-light shader during intermediate door frames. Test 25 pins every named item/identification index to existing atlas pixels and checks all section 9 items. Waterskin maps to its painted capped leather canteen at 480. Potion bottle colours retain their randomized identification mapping. Test 45 is **Abandoned: test failed** and must not be run. Test 44 reconstructs expected pixels in memory from Git objects and does not overwrite assets. Test 46 inspects compiled invocation sites and exercises actual menu handlers with a recording network adapter.


The optional desktop probe renders actual OpenGL frames into `.local/acceptance/` and exits. The Sewer probe uses test save slot 99. The default headless gate targets the three new heroes. `-PsmokeUpstream=true` runs all nine classes, including the six retained classes: generate floors 1–6, save/load, ten seeds each. This diagnostic does not count as new-class acceptance or simulated combat.

The same headless gate now checks City/Vault generation, mirror rewards, equipment and charge restoration, save/load, quest completion/shop state and serialization of the six new enchantments/curses. `java "-Dgrimhollow.vault=true" -jar desktop/build/libs/desktop-1.7.0.jar --smoke-sewers` exercises the real Vault arena trigger, its three rendered boss forms and scripted death/door unlocking. These checks do not constitute a player-driven quest or boss fight. Gradle 9.5.0 and Android Gradle Plugin 9.2.0 are inherited from v4 and run with the existing JDK 17/SDK 36 toolchain; use `--no-daemon` for every local Gradle invocation.

The native-crash regression uses the existing desktop fixture with an isolated, disposable save home:

```powershell
java "-Duser.home=$PWD/.local/encounters" "-Dgrimhollow.encounterTests=true" "-Dgrimhollow.encounterFixture=true" -jar desktop/build/libs/desktop-1.7.0.jar --smoke-sewers
```

It keeps hostile AI and normal movement/combat, grants 1000 health for coverage, exercises actor-thread drops/pickup/summoning, and renders a forced death after 150 actions. Normal play never enables this fixture. The JUnit regression separately rejects all OpenGL calls from the actor thread and checks texture reload.

CI runs Linux/Windows desktop builds, JUnit, recovery provenance/handler/room checks, Android packaging, and the headless gates. Failed gates remain active; jars upload even when a later acceptance gate fails. Artifact retention: 14 days.

Art: [ART_PIPELINE.md](ART_PIPELINE.md). Dynamic lighting is in Settings → Display, upstream's graphics tab. Ambient, hero, decorative-wall and persistent blob sources are implemented; enhanced effects are available independently of the dynamic-lighting toggle.

## Attribution

Original Pixel Dungeon by Oleg Dolya; Shattered Pixel Dungeon by Evan Debenham and contributors. All new code/art is GPL-3.0-or-later. See [LICENSE.txt](LICENSE.txt) and preserved copyright headers. No Blizzard assets, names or text were imported.

For continuation builds on this host, add `--no-daemon` to avoid reusing a Gradle daemon launched under a different sandbox context.

## Necromancer checkpoint

Choose Necromancer in hero selection. Kills charge the equipped Phylactery; click it to open the spell circle. Skeletons follow and fight automatically. Tengu's mask offers Deathspeaker (an additional minion slot, Revenants and shared buffs) or Hexweaver (four curses). Upgrade armor with the Dwarf King's crown for Corpse Explosion, Death Pact or Bone Prison.

Run the class-specific gate with `gradlew.bat core:smokeRun -PsmokeClass=NECROMANCER -PdesktopOnly=true --no-daemon`. It exercises class features and generator/debug descent to floor 6 for ten seeds. The default `core:smokeRun` covers all three new classes and reports `Runs=30 failures=0`. CI preserves that gate and the recovery checks, with separate class gates for Necromancer, Enchanter and Psychic. Both platform builds upload their artifacts even when later-stage gates fail.

Phylactery starts with one charge and restores a minimum of one on first arrival at each floor. Only spending spell charges levels it; kills replenish charges. Raise Dead offers Skeleton, Wraith (artifact level 1), Ghoul (5), and Deathspeaker Revenant (6).

Rendering tests 24–26: run the desktop jar with Java option -Dgrimhollow.geometryTests=true and argument --smoke-sewers. The existing hidden OpenGL runner checks all sprite types and both item atlases using an offscreen framebuffer.

Enchanter: use the Sigil Brush to inscribe known enchantments or glyphs, weaken enemies, and cast subclass spells. The Artificer rerolls and reinforces gear; the Scrivener blesses allies, silences casters, and fractures armor. Class gate: gradlew.bat core:smokeRun -PsmokeClass=ENCHANTER -PdesktopOnly=true --no-daemon.

Psychic: use the Focus Crystal to retrieve items, trigger traps and glimpse enemies. The Puppeteer dominates or frightens enemies; the Seer sees nearby threats through walls and hurls enemies. Telekinetic Force strengthens thrown weapons. Class gate: `gradlew.bat core:smokeRun -PsmokeClass=PSYCHIC -PdesktopOnly=true --no-daemon`.

Double-click `tools/play.bat` to launch the newest desktop jar without a console. Double-click `tools/rebuild.bat` to build it with the repository's JDK and then launch it. Both resolve their own paths and need no PowerShell session.

## Archived art work

`tools/artgen/`, the Blender pipeline, render caches and earlier verification images remain for history. Their generated world/character/liquid checks are **RETIRED — superseded by recovery**; do not run their build commands to restore this release. See [ART_PIPELINE.md](ART_PIPELINE.md) for provenance and historical instructions. Historical recovery assets restored through `python tools/recovery_assets.py --world --characters`, using only Git extraction, integer nearest-neighbour scaling, approved rectangular patches and fixed palette substitutions.

The enhanced effects toggle and its existing tests 31–32 remain active. The normal grass sprite is restored; other shipped item/effect assets are retained.

## Added dungeon content

The ordinary dungeon pools now include Leech, Echo, Withering and rare Dark Blessing curses; Wands of Necrosis, Gravity and Bone; three tiered scythes; Bone Armor; cosmetic leather variants; and the Hourglass of Ashes. Craft Soulfire with a Potion of Liquid Flame, Scroll of Terror and six energy. Soulfire damages fire-immune creatures and frightens targets in its three-by-three area.

The Hourglass holds ten charges and regenerates one every 30 turns, dropping by two turns per upgrade. One charge removes recently gained positive enemy buffs and refunds your last action. You must spend the refunded time before another paid action becomes eligible. Corrosion damage upgrades it, starting at 20 damage and adding ten to the next threshold each level.

Eligible ordinary mobs have a 10% chance to be cursed, with 30% more health, a persistent self-curse, five-turn curse transmission on hit, and one extra floor-scaled loot item. Hexcasters appear as rare rotation additions on floors 11–20, alternate two ranged curses, retreat from melee, and drop a wand 25% of the time. Chainwarden replaces Tengu in 30% of floor-10 generations, retaining the arena and rewards while using rooting chain traps and a two-cell pull every four turns.

The existing `core:smokeRun` gate also exercises the new content, including real Wand of Bone save/load cleanup, Soulfire against a fire elemental, Hourglass refund persistence, curse and armor serialization, and enemy mechanics. These scripted checks are distinct from a complete player-driven campaign and Android device testing.

## Pixel Dungeon

Original game by **Oleg Dolya**: [Pixel Dungeon project](https://github.com/watabou/pixel-dungeon).

## Shattered Pixel Dungeon

By **Evan Debenham and contributors**: [Shattered Pixel Dungeon project](https://github.com/00-Evan/shattered-pixel-dungeon). Upstream sprites and paintings are used under GPL-3.0-or-later; [license](LICENSE.txt).

## Completed sprint: playtesting fixes and character presentation

Completed 2026-10-04: all seven components delivered. Components 1-6 are tagged v1.21.0 through v1.21.5; the seven further character batches are v1.22.0 Mage, v1.22.1 Rogue, v1.22.2 Huntress, v1.22.3 Duelist, v1.22.4 Cleric, v1.22.5 Necromancer and v1.22.6 Psychic. The following records the agreed scope. In-game Playtest access remains delivered in v1.20.2.

### Component 1 - Positioning, grass and notification fixes

- Repair Hexcaster's retreat path so logical movement and sprite movement stay synchronized. Exercise retreat, ordinary movement, knockback, teleportation, melee/projectile targeting and examination; apply any shared fix to related casters. A creature must occupy the cell in which it is drawn, including after interrupted animations.
- Correct tall/furrowed grass foreground placement and overlap: grass may cover feet and lower legs, while heads and torsos remain readable. Check all nine heroes and creature silhouettes, including during movement and at different zooms; preserve terrain and movement rules.
- Keep the journal symbol visible when notes arrive. Use a restrained unread badge/pulse without fading the symbol to zero or flashing the whole container. Display keys separately so they do not replace the book. Verify unread/acknowledged states and portrait/landscape layouts.
- Distinguish purchased talent ranks with solid bright gold and unpurchased ranks with dark hollow sockets. Clearly label available points; use shape and value contrast as well as color, retaining existing rank limits and input behavior.

### Component 2 - Lurking Horror behavior

- Preserve regional minimum flight durations, detection counters, the player's response opportunity before an ambush, ordinary cornered attacks and the lifetime healing limit of 25% maximum HP. Fix oscillation using a reachable escape destination and route memory; an escape route may briefly approach the hero if that is necessary to get out. Review danger at the default 100% damage setting after the behavior repair before proposing another damage increase.
- After minimum flight, enter recovery shadowmeld only outside ordinary hero sight and without an active full reveal. If still visible, continue fleeing. If already at full health or unable to heal further, skip recovery and return to stalking once eligible to regain shadowmeld.
- Once recovery shadowmeld starts, entering the room or seeing its cell normally does not reveal the Horror. It does not attack during recovery. It moves toward a reachable hiding spot, avoids the hero, and vacates doors/narrow corridors rather than standing in them. If no hiding spot is reachable, keep escaping or use ordinary defensive combat when exposed and cornered.
- A close evasion can produce "Something shifts nearby." at most once per recovery period, without a marker or terrain disclosure. An unavoidable physical encounter exposes it and interrupts hero movement without a free ambush or shared occupancy. Damage and existing full-reveal tools also expose it and resume fleeing; accrued healing is never reset.
- Heal at the existing slow cadence while hidden and mobile. End recovery as soon as full health is reached or the remaining lifetime healing allowance is exhausted; do not force an idle 50-turn wait. Return to stalking, with the usual warning before the next strike.
- Verify dead ends, blocked exits, looping paths, corridor clearance, ordinary room entry during recovery, tool reveals, collision, repeated recovery, phase timing and save/load. Preserve solitary-hunter and once-per-region generation rules.

### Component 3 - Simultaneous Enchanter etchings

- Support one weapon rune and one armor rune at the same time. Each carries its appropriate floor-changing enchantment/glyph alongside permanent and temporary inscriptions, at the existing full proc strength and class bonus.
- Track the two runes independently. Each retains its own paid upgrade level when transferred within its equipment category; transfers do not copy upgrades between weapon and armor or reroll their effects. Preserve learned inscription knowledge.
- Migrate existing saves without losing the current rune, gear or upgrades; add an unupgraded counterpart. Cover equipment changes, recovery of attachments, floor transitions, descriptions and save/load. Do not grant a free upgrade as part of migration.

### Component 4 - Psychic detection and Crystal weapon

- Replace Treasure Sense's permanent loot-surroundings mapping and floor-wide door/trap revelation with temporary item-only markers on first floor entry. Rank 1: radius 4, duration 15 turns; rank 2: radius 6, duration 20; rank 3: radius 8, duration 25. Fade markers visibly near expiration; reveal no surrounding terrain and do not renew the benefit by revisiting/reloading.
- Trap Sense becomes 15%/30% passive detection of searchable hidden traps in ordinary visible terrain, checked on their first entry into sight rather than rerolled while waiting or refreshing FOV. Preserve its existing remote-trap damage riders unless separately changed in balance review.
- Keep the Seer's automatic nearby enemy awareness and hidden-door/trap discovery within three cells, including through walls. Stack the talent by coverage: outside the guaranteed zone, Trap Sense still supplies probabilistic detection within normal sight.
- Manual search already guarantees ordinary searchable traps in range, so provide reach rather than an ineffective percentage bonus. Add one/two cells of manual trap-search reach by talent rank; for a Seer, extend from its three-cell guaranteed area to four/five cells. The extension is trap-only and respects sight/walls; it grants no extra terrain mapping or hidden-door range. Preserve non-searchable-trap rules for ordinary searching and existing full-reveal tools.
- Move Focus Crystal to the weapon slot as the Psychic's starting weapon, replacing Focus Ring, with its artifact-style charge system and usage-based levels 0-10. It occupies only the weapon slot. Base casting and full recharge require wielding it. Preserve spell/rider progression and prohibit Upgrade/Infusion/Toolkit levelling; melee hits neither spend charges nor award Crystal experience.
- Replace the two-rank Kinetic Reserve talent with **Unbound Focus**: rank 1 permits inventory casting and 50% carried recharge; rank 2 permits inventory casting and 75% carried recharge. Wielded recharge remains 100% of its applicable rate. Remove the former floor-entry charge restoration, migrate allocated ranks without changing point totals, and keep capacity/recharge descriptions accurate with other Psychic talents.
- Crystal melee damage grows modestly at every Crystal level. Proposed base curve: minimum `1 + floor(level/2)`, maximum `5 + floor(1.5*level)`, with a 0.8-turn attack and normal armor reduction. Milestones: level 0 = 1-5, 2 = 2-8, 5 = 3-12, 8 = 5-17, 10 = 6-20. These are starting balance values: verify sustained damage including Strength and attack speed so the Crystal remains dependable against late-game enemies but weaker than suitable conventionally upgraded weapons.
- Preserve existing Crystal levels, charges, experience and owned gear during migration from the artifact slot; do not overwrite a player's chosen weapon. Account for the freed artifact slot in balance scenarios. Verify wielded/carried casting at all talent ranks, charges, spell growth, melee timing, detection boundaries, marker expiry and persistence.

### Component 5 - Distinct icon artwork

- Refresh the snake/surprise-attack illustration in the Tome of Dungeon Mastery and audit the other guide illustrations for legacy art.
- Audit item identification emblems, inventory art and ability/talent symbols across categories. Every distinct type needs distinctive artwork; Identify, Mind Vision and Magical Sight are known reused-source examples. Family framing may remain consistent, but tinting the same emblem is insufficient. Keep each type's identity consistent wherever that same type is shown.
- Preserve randomized unidentified potion/scroll appearances and the identification game. Check source-art reuse and semantic mappings in the existing pipeline/validator, then visually review silhouettes at actual UI size; a raw pixel hash alone cannot establish meaningful uniqueness. Batch this work by category and regenerate assets through the established reproducible painted pipeline.

### Component 6 - Weapon-aware character prototype

Prototype on Warrior and Enchanter before carrying the approach to the other seven. Use separate painted weapon layers and class-specific grip/pose anchors so the displayed weapon follows equipped gear without repainting every armor/weapon combination. Cover empty hands, one-/two-handed melee, bows, staffs, the new Crystal weapon and thrown-weapon actions, including Duelist swaps. Preserve attack timing, movement, hitboxes and gameplay. Review layer order, transparency, armor clipping, facing, actual game-size readability and tablet texture/memory cost. Use category silhouettes first, adding individual weapon detail where readable.

### Component 7 - Remaining seven heroes, one batch per character

Delivered the Warrior/Enchanter direction to Mage, Rogue, Huntress, Duelist, Cleric, Necromancer and Psychic in separate building checkpoints. Preserve portrait identity; give each individual anatomy, silhouette, colors, fitted armor and restrained animation, with still idles. Integrate the reviewed weapon-layer approach. Produce cloth/plate comparisons, pose/animation previews and native game-size screenshots for each character; review each batch before expanding further.

### Delivery boundaries

Check remaining usage before each component and each hero batch; notify the user and stop at a committed boundary if the next will not fit. Finish components in order, with clean builds, descriptive commits and pushes at boundaries. Reuse and extend relevant existing checks, verify once at each component's end, and preserve all unrelated gates. Run affected class gates and the combined gate after gameplay changes; build Windows/Android and confirm unchanged CI checks at release boundaries. Asset work must reproduce offline from committed sources. Report actual executed results and any device-playtesting limits. Terrain contrast test 45 remains **Abandoned: test failed** and is not run. Implementation was authorized after this plan was agreed.
