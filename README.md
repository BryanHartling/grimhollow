# Grimhollow

**Tablet playtest fixes v1.11.1:** touch movement uses a longer visual step and smoother camera following; barricades span both corridor orientations and unknown neighboring walls no longer disclose their overhangs. Hurl retains its direction-selection step. The Focus Crystal and Sigil Brush can be unequipped; stored growth/charges remain, while casting and passive charging require equipping them.

The class handbook now accepts actual taps on skills. Talent windows offer rank buttons and scrollable descriptions; long titled descriptions also scroll. Each of the three added classes keeps three armor paths, with four talents of four ranks per path. Focused Mind adds 1/2/3 capacity; Far Reach adds 1/2/4 Grasp range; Precognition prevents one/two qualifying hits per floor; Treasure Sense adds loot/hidden doors/hidden traps across its three ranks. Kinetic Surge grants 12.5/25/37.5/50 percent thrown damage during Mind Meld. All added-class rank descriptions state their effects explicitly.

Infernal Brew feeds Ashlight **three units**, giving three early levels or the normal two-unit cost per level above +6. A level-8 Flare also reveals and burns disguised hostile mimics. Open light alone does not burn creatures. Upgrade scrolls now show a rising gold sigil effect. See [changes](CHANGES.md), [verification](verification/ACCEPTANCE.md), and [known limitations](KNOWN_ISSUES.md). Install the new APK over the existing app to preserve saves; physical tablet feel still needs player review.

**In-game playtesting v1.11.0:** open the pause/game menu (Escape on desktop), choose **Playtest**, then **Enable Playtest for this save**. Controls use paged buttons in desktop and portrait layouts. Enabling permanently marks that save `PLAYTEST` and excludes it from rankings, badges, catalog credit and bones; other saves remain normal. God mode is optional and can be turned off for balance testing.

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
java -jar desktop\build\libs\desktop-1.11.0.jar
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
python tools/recovery_checks.py --all-regions
python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.7.0.jar
```

Repeat the recovery renderer with region indices 1–4 for Prison, Caves, City and Halls. It walks normal adjacent moves on generated terrain in diagnostic slot 99, captures remembered terrain and verifies remembered-cell fog compositing. Test 47 checks all pixels of every visible and never-seen cell in five generated regions, before and after walking, at three zooms and four camera offsets, plus the shared wall-light shader during intermediate door frames. Test 25 pins every named item/identification index to existing atlas pixels and checks all section 9 items. Waterskin maps to its painted capped leather canteen at 480. Potion bottle colours retain their randomized identification mapping. Test 45 measures every pair of types in each lit room, including decor; failures remain active in CI. Test 44 reconstructs expected pixels in memory from Git objects and does not overwrite assets. Test 46 inspects compiled invocation sites and exercises actual menu handlers with a recording network adapter.


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

Phylactery starts with one charge and restores a minimum of one on first arrival at each floor. Only spending spell charges levels it; kills replenish charges. Raise Dead offers Skeleton, Wraith (artifact level 1), Ghoul (3), and Deathspeaker Revenant (6).

Rendering tests 24–26: run the desktop jar with Java option -Dgrimhollow.geometryTests=true and argument --smoke-sewers. The existing hidden OpenGL runner checks all sprite types and both item atlases using an offscreen framebuffer.

Enchanter: use the Sigil Brush to inscribe known enchantments or glyphs, weaken enemies, and cast subclass spells. The Artificer rerolls and reinforces gear; the Scrivener blesses allies, silences casters, and fractures armor. Class gate: gradlew.bat core:smokeRun -PsmokeClass=ENCHANTER -PdesktopOnly=true --no-daemon.

Psychic: use the Focus Crystal to retrieve items, trigger traps and glimpse enemies. The Puppeteer dominates or frightens enemies; the Seer sees nearby threats through walls and hurls enemies. Telekinetic Force strengthens thrown weapons. Class gate: `gradlew.bat core:smokeRun -PsmokeClass=PSYCHIC -PdesktopOnly=true --no-daemon`.

Double-click `tools/play.bat` to launch the newest desktop jar without a console. Double-click `tools/rebuild.bat` to build it with the repository's JDK and then launch it. Both resolve their own paths and need no PowerShell session.

## Archived art work

`tools/artgen/`, the Blender pipeline, render caches and earlier verification images remain for history. Their generated world/character/liquid checks are **RETIRED — superseded by recovery**; do not run their build commands to restore this release. See [ART_PIPELINE.md](ART_PIPELINE.md) for provenance and historical instructions. Current assets restore through `python tools/recovery_assets.py --world --characters`, using only Git extraction, integer nearest-neighbour scaling, approved rectangular patches and fixed palette substitutions.

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
