# Grimhollow

GPL-3.0-or-later derivative of [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon), based on **v3.3.8**, commit `7b8b845a76fe76c6b7c031ae9e570852411f56db`. Upstream history and Java packages are preserved.

**Stages 1, 2, 2.5, 3 and 4 complete.** Nine heroes are playable: the six upstream classes plus Necromancer, Enchanter and Psychic, each with two subclasses, talents and three armor abilities. Stage 5 art coverage and section 9 content remain pending. See [KNOWN_ISSUES.md](KNOWN_ISSUES.md) and the [v0.3 specification](GDD-one-shot-build-spec.md).

## Build on Windows

Requires Temurin **JDK 17**. Install JDK and Android tools locally, then configure the session:

```powershell
.\tools\bootstrap-windows.ps1
. .\tools\env.ps1
.\gradlew.bat desktop:run -PdesktopOnly=true
```

Desktop-only excludes the Android module and Android plugin and works with no SDK or `ANDROID_HOME`. For a runnable jar:

```powershell
.\gradlew.bat desktop:dist -PdesktopOnly=true
java -jar desktop\build\libs\desktop-0.3.0.jar
```

`desktop:dist` aliases upstream's `desktop:release` fat-jar task. `desktop:run` supplies required launcher metadata. Linux/macOS use `./gradlew`; on macOS the run task adds `-XstartOnFirstThread`.

Full build needs SDK platform 36 and build-tools 36.0.0:

```powershell
. .\tools\env.ps1
.\gradlew.bat desktop:dist core:test android:assembleDebug
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

CI runs Linux/Windows desktop builds, JUnit, full art validation, Android packaging, and the headless gate. Failed gates remain active; jars upload even when a later acceptance gate fails. Artifact retention: 14 days.

Art: [ART_PIPELINE.md](ART_PIPELINE.md). Dynamic lighting is in Settings → Display, upstream's graphics tab. Ambient, hero, decorative-wall and persistent blob sources are implemented; transient effects remain incomplete.

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

Run `python tools/artgen/build.py --render` to create scenes and render the POC with Eevee, then post-process the output. The scripts in `tools/artgen/blender/` define the models, joint animation, seeded materials, fixed camera and lights. The committed PNG frames in `tools/artgen/render_cache/` are the input to normal `python tools/artgen/build.py` runs. Normal builds and CI do not invoke or need Blender. Set `BLENDER` to an alternate executable path when rerendering on another machine.

`python tools/artgen/validate.py --generated-only --rebuild` checks the implemented assets and byte-for-byte rebuilds without Blender. `--rerender` additionally rerenders the cache and enforces at most two changed pHash bits per frame. The unqualified validator remains the full-delivery gate and reports unfinished stage-6 coverage.

The POC covers Sewers floor, wall, water, grass and doors; rat and crab; eight Necromancer armor rows; and dedicated skeleton/ghoul minions. `verification/render-poc.png` is preserved as the historical POC and compares each asset with its procedural source at 4x. `java -Dgrimhollow.renderPoc=true -jar desktop/build/libs/desktop-0.4.0.jar --smoke-sewers` uses the existing desktop probe to capture `verification/render-poc-ingame.png`. Other regions, remaining character/item art and enhanced effects await the next approved stage.

PNG serialization uses fixed Sub filtering and Python standard-library Huffman-only compression, including the PNG payloads inside launcher ICO/ICNS files, to keep Windows and Linux post-process output identical.


Stage 5.6 iterates only the six Sewers environment/prop classes. Review
`verification/iteration/summary.png` and `verification/iteration/sewers-ingame.png`.
Parameters, score histories, visual critiques and regeneration commands are documented in
[ART_PIPELINE.md](ART_PIPELINE.md). No subsequent art stage is started by these commands.
