# Grimhollow

GPL-3.0-or-later derivative of [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon), now incorporating **v4.0.0**, commit `2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f`. The fork began at v3.3.8; upstream history and Java packages are preserved.

**Stage 8: all numbered development stages implemented.** Nine heroes are playable: six upstream classes plus Necromancer, Enchanter and Psychic, each with two subclasses, talents and three armor abilities. All 80 character atlases use native silhouettes; 380 item props, special-room materials and the title come from the reproducible pipeline. Review [the characters](verification/characters.png), [the items](verification/items.png), and [the enhanced effects](verification/effects.png). Settings → Display → Enhanced effects restores the original rendering when disabled. See [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for acceptance gaps and [the v0.8 specification](GDD-one-shot-build-spec.md).

**v4 content integration, version 0.5.2-v4:** the Ambitious Imp's expanded Vault quest, elemental boss, hazards, patrol AI, quest loot and equipment exchange are included. Weapon pools include Venomous, Vorpal, Eldritch and Crystal enchantments plus Pressurized and Wondrous curses. Upstream item balance, combat, save/load, generation, UI and audio fixes are incorporated. The Vault mirror supports all nine heroes. Approved regional atlases remain unchanged; v4 special-room and item visuals regenerate from palette-vector sources. The visual correction replaces stamped liquid highlights with reflections from travelling surface waves; tests 39-42 now pass locally, including the five-room vision review. The complete art gate and all platform jobs passed at v0.6.0-art-complete.

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
java -jar desktop\build\libs\desktop-1.0.0.jar
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
.\gradlew.bat core:test -PdesktopOnly=true
.\gradlew.bat desktop:run -PdesktopOnly=true --args=--smoke-title
.\gradlew.bat desktop:run -PdesktopOnly=true --args=--smoke-sewers
.\gradlew.bat core:smokeRun -PdesktopOnly=true
python tools/artgen/validate.py
```

The optional desktop probe renders actual OpenGL frames into `.local/acceptance/` and exits. The Sewer probe uses test save slot 99. The default headless gate targets the three new heroes. `-PsmokeUpstream=true` runs all nine classes, including the six retained classes: generate floors 1–6, save/load, ten seeds each. This diagnostic does not count as new-class acceptance or simulated combat.

The same headless gate now checks City/Vault generation, mirror rewards, equipment and charge restoration, save/load, quest completion/shop state and serialization of the six new enchantments/curses. `java -Dgrimhollow.vault=true -jar desktop/build/libs/desktop-1.0.0.jar --smoke-sewers` exercises the real Vault arena trigger, its three rendered boss forms and scripted death/door unlocking. These checks do not constitute a player-driven quest or boss fight. Gradle 9.5.0 and Android Gradle Plugin 9.2.0 are inherited from v4 and run with the existing JDK 17/SDK 36 toolchain; use `--no-daemon` for every local Gradle invocation.

CI runs Linux/Windows desktop builds, JUnit, full art validation, Android packaging, and the headless gate. Failed gates remain active; jars upload even when a later acceptance gate fails. Artifact retention: 14 days.

Art: [ART_PIPELINE.md](ART_PIPELINE.md). Dynamic lighting is in Settings → Display, upstream's graphics tab. Ambient, hero, decorative-wall and persistent blob sources are implemented; enhanced effects are available independently of the dynamic-lighting toggle.

## Attribution

Original Pixel Dungeon by Oleg Dolya; Shattered Pixel Dungeon by Evan Debenham and contributors. All new code/art is GPL-3.0-or-later. See [LICENSE.txt](LICENSE.txt) and preserved copyright headers. No Blizzard assets, names or text were imported.

For continuation builds on this host, add `--no-daemon` to avoid reusing a Gradle daemon launched under a different sandbox context.

## Necromancer checkpoint

Choose Necromancer in hero selection. Kills charge the equipped Phylactery; click it to open the spell circle. Skeletons follow and fight automatically. Tengu's mask offers Deathspeaker (an additional minion slot, Revenants and shared buffs) or Hexweaver (four curses). Upgrade armor with the Dwarf King's crown for Corpse Explosion, Death Pact or Bone Prison.

Run the class-specific gate with `gradlew.bat core:smokeRun -PsmokeClass=NECROMANCER -PdesktopOnly=true --no-daemon`. It exercises class features and generator/debug descent to floor 6 for ten seeds. The default `core:smokeRun` covers all three new classes and reports `Runs=30 failures=0`. CI preserves that gate and the full art validator, with separate class gates for Necromancer, Enchanter and Psychic. Both platform builds upload their artifacts even when later-stage gates fail.

Phylactery starts with one charge and restores a minimum of one on first arrival at each floor. Only spending spell charges levels it; kills replenish charges. Raise Dead offers Skeleton, Wraith (artifact level 1), Ghoul (3), and Deathspeaker Revenant (6).

Rendering tests 24–26: run the desktop jar with Java option -Dgrimhollow.geometryTests=true and argument --smoke-sewers. The existing hidden OpenGL runner checks all sprite types and both item atlases using an offscreen framebuffer.

Enchanter: use the Sigil Brush to inscribe known enchantments or glyphs, weaken enemies, and cast subclass spells. The Artificer rerolls and reinforces gear; the Scrivener blesses allies, silences casters, and fractures armor. Class gate: gradlew.bat core:smokeRun -PsmokeClass=ENCHANTER -PdesktopOnly=true --no-daemon.

Psychic: use the Focus Crystal to retrieve items, trigger traps and glimpse enemies. The Puppeteer dominates or frightens enemies; the Seer sees nearby threats through walls and hurls enemies. Telekinetic Force strengthens thrown weapons. Class gate: `gradlew.bat core:smokeRun -PsmokeClass=PSYCHIC -PdesktopOnly=true --no-daemon`.

Double-click `tools/play.bat` to launch the newest desktop jar without a console. Double-click `tools/rebuild.bat` to build it with the repository's JDK and then launch it. Both resolve their own paths and need no PowerShell session.

The stage-5 proof of concept uses Blender 4.5.13 LTS at `C:\Users\Hartl\Documents\grimhollow\.toolchain\blender\blender.exe`. `python tools/artgen/install_blender.py` installs the pinned portable release and checks its official SHA-256 manifest; the [Blender 4.5 LTS release page](https://www.blender.org/releases/4-5/) documents the release family.

Run `python tools/artgen/build.py --render` to render the active environment sources with Eevee, then post-process the output; approved Sewers classes remain locked. Character source geometry is in `tools/artgen/characters.py`; `character_catalog.py` compiles material choices and existing Java animation indices into the committed specifications. The rejected POC rigs are retained under `tools/artgen/blender/experimental/` and are accessible only with explicit `--asset experimental/<name>`. Normal builds and CI reuse committed environment renders and paint native character vectors without Blender.

`python tools/artgen/validate.py --generated-only --rebuild` checks the implemented assets and byte-for-byte rebuilds without Blender. `--rerender` additionally rerenders the cache and enforces at most two changed pHash bits per frame. The unqualified validator remains the full-delivery gate and reports unfinished stage-6 coverage.

`verification/render-poc.png` and `render-poc-ingame.png` preserve the historical POC. Current character review is `verification/characters.png`; current lit room captures are under `verification/iteration/`. The stage-6d validator checks every native pose, its intact two-pixel outline, standing occupancy, non-static animations and regional 16-pixel hue distances.

PNG serialization uses fixed Sub filtering and Python standard-library Huffman-only compression, including the PNG payloads inside launcher ICO/ICNS files, to keep Windows and Linux post-process output identical.


Stage 5.7 iterates only the six Sewers environment/prop classes. Review
`verification/iteration/summary.png` and `verification/iteration/sewers-gate.png`,
which prints the six room measurements below the actual lit screenshot. The raw
image remains `verification/iteration/sewers-ingame.png`.
Parameters, score histories, visual critiques and regeneration commands are documented in
[ART_PIPELINE.md](ART_PIPELINE.md). No subsequent art stage is started by these commands.

The five approved Sewers classes are now locked. Animated sewage, clean water and
lava use four variants with eight frames each, separate cell phases, and an entry
ripple. Review `verification/iteration/liquids/` and the lit Sewers image above;
the Prison and Halls subfolders contain liquid previews with their existing region
art. These previews do not certify the later regional art gates.

Stage 6e keeps every special-room cell at 16 logical units while replacing its
texture material at 64px. Talent and identification glyphs are native 32px
pictograms. Item IDs, animation timing, collision and quest layouts retain v4
contracts. `python tools/artgen/build.py` regenerates the complete 169-sheet
inventory from source and committed caches; CI does not install Blender.

Enhanced effects are generated by `python tools/artgen/build.py --render --asset effects`; CI rebuilds the single 1024×960 atlas from committed renders without Blender. `verification/effects.png` shows all strips. The existing desktop renderer runs tests 31–32 with `-Dgrimhollow.effectsTests=true --smoke-sewers`, including the disabled-path pixel comparison and 40-gas/10-fire timing.

## Added dungeon content

The ordinary dungeon pools now include Leech, Echo, Withering and rare Dark Blessing curses; Wands of Necrosis, Gravity and Bone; three tiered scythes; Bone Armor; cosmetic leather variants; and the Hourglass of Ashes. Craft Soulfire with a Potion of Liquid Flame, Scroll of Terror and six energy. Soulfire damages fire-immune creatures and frightens targets in its three-by-three area.

The Hourglass holds ten charges and regenerates one every 30 turns, dropping by two turns per upgrade. One charge removes recently gained positive enemy buffs and refunds your last action. You must spend the refunded time before another paid action becomes eligible. Corrosion damage upgrades it, starting at 20 damage and adding ten to the next threshold each level.

Eligible ordinary mobs have a 10% chance to be cursed, with 30% more health, a persistent self-curse, five-turn curse transmission on hit, and one extra floor-scaled loot item. Hexcasters appear as rare rotation additions on floors 11–20, alternate two ranged curses, retreat from melee, and drop a wand 25% of the time. Chainwarden replaces Tengu in 30% of floor-10 generations, retaining the arena and rewards while using rooting chain traps and a two-cell pull every four turns.

The existing `core:smokeRun` gate also exercises the new content, including real Wand of Bone save/load cleanup, Soulfire against a fire elemental, Hourglass refund persistence, curse and armor serialization, and enemy mechanics. These scripted checks are distinct from a complete player-driven campaign and Android device testing.
