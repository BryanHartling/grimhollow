# Journal title - v1.29.16

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 28s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. The existing bounty gate resolves all **27 landmark titles/descriptions before and after save/load**, including the saved COLE note and its matching Bounty Board actor name.
- Final existing native bounty suite: **BOUNTY UI PASS; failures=0**, exit 0 in landscape/mouse and portrait/touch. Each scrolls to and clicks the actual Bounty Board journal icon, checks the rendered title and description for unresolved text, and captures the corrected popup. Both captures are visually reviewed and retained under `verification/interface/{landscape,portrait}/bounty-board-journal-description.png`. Existing contract, urgency, payment, staged betrayal, personal poster and ranking checks remain passing.
- The first native attempt failed because the new fixture cast a grid component to Visual. Correcting its coordinate lookup uses the actual GridItem bounds; the subsequent native-only rebuild **succeeded in 30s** and both complete suites passed. Production code and the passing Android/headless build were unchanged by that repair. This failed attempt is not counted as passing.
- Final compiled audit: **classes=3064 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Windows launcher `desktop/build/windows/1.29.16/Grimhollow/Grimhollow.exe`; its bundled JAR matches the tested JAR, SHA256 **2ED5851284F0681CA9546F0A23E89628326B50F18F61DEB9B104C118D6C49FB5**. Fresh APK manifest **1.29.16-INDEV / versionCode 1015**, rebuilt **2026-10-10 00:46 EDT**, SHA256 **C933F76EAEFB310BED8F7C550437F505E4C93D5EC19E9450922A4BE31159348C**.
- Physical Samsung tablet remains **NOT RUN**. No gameplay or art changes. Test 45 remains **Abandoned: test failed**, not rerun. Every CI check remains enabled; exact release CI is reported at delivery.

# Soul, descriptions and playtest follow-up - v1.29.15

- Release tag **v1.29.15-soul-and-playtest**, commit **087ec2aac3276b2bd57d3beddf581019ec5ee3ce**: [tag CI run 38006776619](https://github.com/BryanHartling/grimhollow/actions/runs/38006776619) and [branch CI run 38006776565](https://github.com/BryanHartling/grimhollow/actions/runs/38006776565) passed **all seven jobs each**: Windows/Linux desktop, Android, all three ten-run class gates and the combined thirty-run gate. Full checks remain enabled.
- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 22s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. Soul Sustenance covers both ranks, full-before-kill eligibility, hero/minion attribution, bonus charges, excluded summons/replacements, normal hunger/no healing, legacy ranks and saved floor/branch budgets. Bone Legion covers all four servants, 5/10/15 extra binding turns, the rank-three-only slot, saved timers and legacy rank-one investments.
- Final additional `core:test core:smokeRun -PsmokeClass=NECROMANCER`: **BUILD SUCCESSFUL in 45s; Runs=10 failures=0**, including inventory delivery, full-backpack floor fallback, immutable personal poster serialization and once-only handover/payment. Final poster-only Windows/Android rebuild: **BUILD SUCCESSFUL in 52s**.
- Horror scenario: **60/64** victories in cornered floor-11 fights with ordinary Cave Spinners at default damage, including poison. **200/200** full-health floor-two rats die to predation at 100%/200% settings. Regional health/damage, ordinary retaliatory attacks, prior-save injury/recovery fractions, no resurrection, finite healing and wanted-quarry protection pass. Native Horror review passes both orientations: eight ambush directions, no bronze marker, optional popup, retained log, fresh-action response, entity-only detection, remains/Bone Wall examination and saved toggle; **TEST 60 NATIVE PASS; failures=0**.
- Reported seed **3848062306978** plans **Voss the Cellbreaker on dungeon floor 9**. All three accepted quarries appear on their promised floors and survive disk reload. Blocked placement cannot consume urgency; orphaned unfinished target records recover; existing wounded actors rebind without duplicate stats, healing or clock reset. Completed targets cannot respawn. The user's original tablet save was unavailable, so its exact historical failure is not claimed as reproduced.
- Final native Bounty review passes both landscape/mouse and portrait/touch: payment dialogue remains unpaid until **Accept bounty**, then a separate betrayal dialogue ends **Cole hands you a new bounty**, then the hero's Wanted Poster appears and is kept. Both actual Prison bosses, mask rewards, no automatic boss-death settlement, no repeated payment/handover, Cole departure, inventory poster and ranked Deeds/archive pass; **BOUNTY UI PASS; failures=0**. The final poster uses the existing full-resolution class painting. Selected captures are retained under the existing verification interface folders.
- Balanced-description native interface and class-presentation suites pass both orientations, with real long-description scrolling and explicit rank progression. Lantern/Chart values and Rune Etching placeholders resolve. The presentation fixture waits for the initial hero action before rebuilding its review room. No gameplay changes beyond the approved talents/Horror/bounty fixes, no generated art and no terrain contrast test.
- Final compiled audit: **classes=3064 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Windows launcher `desktop/build/windows/1.29.15/Grimhollow/Grimhollow.exe`; bundled JAR matches the tested JAR, SHA256 **7AEF4B47467860E15BB3E3646FAE157BFF3CDE5D5880099D839602DE1FF35A3B**. APK `android/build/outputs/apk/debug/android-debug.apk`, manifest **1.29.15-INDEV / versionCode 1014**, rebuilt **2026-10-09 19:51 EDT**, SHA256 **276A688C50B7A23CD122BFF016CEDEF7D9A250E5E1A15393A98F7440E61C4838**.
- Physical Samsung tablet and full-campaign balance remain **NOT RUN**. Test 45 remains **Abandoned: test failed** and is not rerun. Initial formatting/headless fixture failures were repaired; they are not counted as passing. Every CI check remains enabled; exact release CI is reported at delivery.

# Quest pressure - v1.29.13

- Release tag **v1.29.13-quest-balance**, game commit **b3a98cc99d53b262f71d79e2a61391c5705bc681**: [CI run 37973562389](https://github.com/BryanHartling/grimhollow/actions/runs/37973562389) passed **all seven jobs**: Windows/Linux desktop, Android, each ten-run class gate and the combined thirty-run gate. Full art provenance, compiled audit, five-region fog, live encounters, both native expedition layouts and class-selection/progression passed on this exact release.
- The identical commit's [branch run 37973562376](https://github.com/BryanHartling/grimhollow/actions/runs/37973562376) failed unchanged effects timing test 31: **mean 1.8352 ms / p95 2.4639 ms** against the strict **<2 ms** gate; every fog assertion still passed. Portrait class selection also hit **ConcurrentModificationException at Hero.act:874** while the screenshot fixture rebuilt its room. Windows, Android, remaining interfaces and all four headless jobs passed. GitHub denied the failed-job retry with **HTTP 403**. The failed run is retained; no checks or thresholds are weakened. This evidence update is documentation only and does not alter the tagged game or packages.
- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 10s; Runs=30 failures=0**. Eight JUnit tests, zero failures/errors. All three class scenarios preserve seven HP, fractional hunger 350.25, starvation debt 0.75 and Well Fed duration 137 through the real inventory exchange and disk reload. Actual Escape Crystal departure preserves five HP, starving hunger and debt 0.875 with the same Hunger and Regeneration actors/timers. Transient Paralysis is cleared and class state survives.
- Actual Vault scoring with fully explored terrain and an opened token door: **untouched guardian 2250, half-damaged 2600, one HP remaining 2950, defeated without statue 3000, statue 4000**. Scores increase with damage and retain existing rounding/thresholds. No guardian activation reward. Existing old-save entrance, Playtest arrival and return-route checks remain green.
- Expedition scenario: **125% claw/breath/wing impact damage; 64 seeds of reliable nearby wingbeat selection without windup damage; three/five-turn cooldowns; fixed cone, occlusion, two-cell push, persistent health and no healing PASS**. Newly generated caverns contain exactly the configured guaranteed ration counts **0/1/3/8**, retain supplies/settings after reload, and reset to three. Existing population pressure, finite treasure and no renewable rewards checks pass.
- Existing native expedition review: **TEST 59 NATIVE PASS**, exit 0 in landscape and portrait. The actual Guaranteed cavern rations control changes from three to one; both input screenshots are visually reviewed. NPC potion exchange, map entry, continue, fall, blocked climb, boss clear, scheduled victory and pointer return to the original City all pass.
- Compiled handler/network audit: **classes=3057 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. No art or CI checks change. Windows launcher: `desktop/build/windows/1.29.13/Grimhollow/Grimhollow.exe`; bundled JAR matches the tested JAR, SHA256 `0CB9A94085B499D9BB580178FE8A81DFBC81DEEA397C0AD64C7D548E3FE5BDEE`. Fresh APK manifest **1.29.13-INDEV / versionCode 1012**, built **2026-10-09 14:23 EDT**, SHA256 `C5AF18908859C9F7A7A18221E708098241AA0A531FC3EF22CA8C173C6BF8C6FE`.
- The first combined gate failed because an earlier Psychic fixture removed Hunger before this scenario; explicitly creating its initial nutrition actors repaired the fixture. That failed attempt is not counted as passing. Physical Samsung tablet and full-campaign balance remain **NOT RUN**. Prison bosses are unchanged. Test 45 remains **Abandoned: test failed**, not rerun. Exact release CI is reported at delivery.

# Vault travel and furnishings - v1.29.12

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 34s; Runs=30 failures=0**. Eight JUnit tests, zero failures/errors. All three class scenarios pass actual Vault generation/save/load, equipment exchange, recovery of old entrance metadata without changing live actors, normalization of requested depths 16/17/18/19 to the saved Imp floor, moving off arrival stairs in a completed Playtest Vault, no descending stairs, and actual InterlevelScene return to the saved Imp portal. Existing office tests cover ten generated layouts per class, a complete lower rug, old mixed-layer repair, idempotence, preserved stock and saved layers.
- Final package-only rebuild after removing the obsolete lower-banner inspection export and correcting native fixture output: **BUILD SUCCESSFUL in 49s**. Gameplay code is unchanged from the passing combined gate.
- Existing native room review passes both landscape and portrait: **VAULT FURNISHINGS NATIVE PASS**, **VAULT WALK NATIVE PASS**, **REGIONAL SCENERY PASS; failures=0**, exit 0 each. Wall hangings are confined to plain wall cells, not walkable paving or torch decorations; the actual completed-Vault hero moves without a branch transition. Office native review checks the full nine-piece lower rug and rendering order; **BOUNTY UI PASS; failures=0**, exit 0 in both orientations. All four office/Vault captures are visually reviewed.
- Complete offline provenance/reconstruction: **PAINTED assets=1936 source sheets=182 launcher resources=55 failures=0; TEST 44 failures=0**. The obsolete `city_quest-82` lower-banner preview is removed from the atlas lookup, shipped files and provenance manifest. No source painting is generated; only City quest wall-hanging pixels and their inspection exports change. Compiled audit: **classes=3057 guarded browser sinks=1 HTTP/socket calls=0 failures=0**.
- Windows launcher: `desktop/build/windows/1.29.12/Grimhollow/Grimhollow.exe`. Packaged JAR matches the tested desktop JAR, SHA256 `E9C37BBCFD86B26928F6C7132FBDD9D89787A6215EBAFDA2710324BE31B72746`. APK manifest **1.29.12-INDEV / versionCode 1011**, rebuilt **2026-10-09 11:29 EDT**, SHA256 `1564577366E8402F36BFE533C96A4907207E178AEC1F25C1D8CD5E9C3F193B98`.
- Initial restricted Gradle failed to connect to its daemon; the authorized route passed. A restricted packer was stopped after making no progress, then completed on the authorized route. The first compiled audit lacked the session JDK environment; loading `tools/env.ps1` repaired it. Failed attempts are not counted as passing.
- Physical Samsung tablet and the user's exact save are **NOT RUN**. Dragon/Vault balance recommendations, including the existing remaining-health partial-score defect, are not implemented in this rendering/travel patch. Test 45 remains **Abandoned: test failed**, not rerun. Full CI stays enabled; the exact release result is reported at delivery.

# Cavern pressure - v1.29.11

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 22s; Runs=30 failures=0**. RecoveryTest (3) and LightMapTest (5) have zero failures, errors or skips. Existing test 59 exercises surviving scavengers/hatchlings, hidden and distant arrivals, one living mother, the nine-spider cap, randomized 60-100-turn actor rescheduling (including blocked/capped attempts), replacement mothers hatching after the first victory, unchanged finite supplies, zero renewable XP/ordinary/bonus loot with an active +10 Ring of Wealth, permanent climb access, saved remaining time across actual floor transitions, old saves without a timer, disable-at-zero and tuned interval bounds. All three class scenarios pass, alongside the existing mine and content gates.
- Existing native expedition review passes landscape and portrait: **TEST 59 NATIVE PASS**, exit 0 each. Actual NPC exchange, map use, continuing a save, falling, blocked first climb, survivor count after Broodmother death, successful climb, dragon victory transport and return to the original City remain passing. Both emitted a non-fatal Win32 clipboard-access warning during the existing numeric-input fixture. These are native scripted checks, not physical tablet play.
- Compiled audit: **classes=3056 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Windows package completed; its bundled JAR matches the tested `desktop-1.29.11.jar`, SHA256 `7341862E089169DE6BE3BBBC3F6DB55B91C2DCCEB95FD3A66DC1B3228A0E6D44`. APK actual manifest **1.29.11-INDEV / versionCode 1010**, rebuilt **2026-10-09 08:31 EDT**, SHA256 `AF19372F74B4A35087271670888A2A24770CB959992C13A61B45E1EC4D02E88F`.
- Failed attempts are not counted as passing: the initial sandboxed build failed at Android's `C:\.android` setup; the authorized route completed the build. The first replenishment regression caught `beckon()` touching a sprite before attachment; a direct target assignment then failed compilation because that field is protected. Typed, silent patrol initialization fixes both without altering upstream Mob visibility or weakening checks.
- Physical Samsung tablet and full-campaign pressure balance are **NOT RUN**. Old saves retain their floor, supplies and surviving population; spiders already removed by an earlier build are not restored. The former safe-cavern outcome is superseded by the user's replenishment request. No artwork changed or was generated; test 45 remains **Abandoned: test failed**, not rerun. Full CI stays enabled; its release result is reported at delivery.

# Mine ore - v1.29.10

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 28s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. Each class checks 208 mining cases: 100 seeds for each implemented quest variant, plus seed 4507775544315 at depths 11-14 for both variants. All have 45-47 ore, two treasure pockets with 4-5 pieces each, diggable boundary positions and protected quest ore. The supplied seed's eight mine snapshots retain their terrain and loose ore across Bundle save/load.
- Original-generation reproduction of the reported **crystal** seed, before the placement fix:

| Blacksmith dungeon floor | Total ore | Completely buried ore | Ore in the two treasure pockets |
|---|---|---|---|
| 11 | 45 | 4 | 4 / 4 |
| 12 | 46 | 4 | 4 / 5 |
| 13 | 47 | 7 | 5 / 3 |
| 14 | 45 | 2 | 4 / 4 |

These are regenerated levels, not the tablet save. No total-ore shortage was reproduced. Magic Mapping skips completely buried rock; crystal treasure pockets contain veins rather than ore chests. Original room-placement checks fail on 57 pockets across the 208 cases because a second random coordinate overwrites deposits. The fixed code uses the validated cell. A later regression caught an optional extra deposit overshooting the total by one; its remaining-budget guard is corrected.

- Existing native room review passes landscape and portrait: **MINE ORE NATIVE PASS** for gnoll and crystal, then **TEST 57 ROOMS PASS** and **REGIONAL SCENERY PASS; failures=0** in each. It invokes the real animated mining callback on side-facing ore, verifies one piece is collected or remains in a heap, and checks the mined terrain. Initial native attempts used a Playtest-only teleport in an ordinary fixture save; corrected fixture placement retains the production mining path. Only this desktop fixture repair required a subsequent `desktop:dist` build, successful in 27s.
- Ore on side cutaways previously retained only 58-122 changed pixels, almost entirely clipping the centered mineral. The existing packer fits the same authored mineral to the exposed side/rim masks and asserts at least 160 visible changed pixels on each exposed internal variant. Fully buried interiors retain black masks. **PAINTED assets=1937 source sheets=182 launcher resources=55 failures=0; TEST 44 failures=0**. No new art was generated. Only the two mine atlas PNGs changed; their APK bytes and provenance manifest match the shipping assets.
- Final compiled audit: **classes=3055 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Windows launcher: `desktop/build/windows/1.29.10/Grimhollow/Grimhollow.exe`; tested JAR SHA256 `0394F1C19D2DE8B9AD63B0C6C1B48F9EDA396C265AD973BC74504C7C798FC20C`. APK actual manifest **1.29.10-INDEV / versionCode 1009**, rebuilt **2026-10-08 17:23 EDT**, SHA256 `E8644D08F9DAD7A9D9048614E2AB67858AFDB0A7600D243B43A6757DFE8461A4`. Final mine captures in both orientations are visually reviewed.
- The exact tablet save, collection history and physical tablet play are **NOT RUN**. Existing mine geometry and ore are not rerolled; the visual correction applies to old saves, while placement corrections apply to new mines. Test 45 remains **Abandoned: test failed**, not rerun; prior retired tests retain their status. Full CI remains enabled and its exact release result is reported at delivery.

# Telekinesis, stealth and motion - v1.29.9

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 13s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. Test 48 checks every Crystal level 0-10 for Push/Hurl distance, wall/creature impact damage and status, landing traps, Hurl's intermediate hidden/visible traps with correct victim occupancy, no duplicate landing activation, lethal/relocating interruption, first chasm edge and boss rules. Existing content checks exercise actual Cloak stealth, potion invisibility, absent line of sight and resumed Chainwarden pulls, plus rejection of targeted traps while cloaked.
- Existing native geometry/recovery check: **tests 24-26, 34, 36 PASS; failures=0**. Haste has **68 changed boot pixels, 0 upper-body pixels** and matches the ordinary sprite when standing or after expiry. The painted drinking bottle contributes **501 visible pixels** at the enlarged review scale; its visible painting fits 3.8x5.5 world units and is cleared after the operation. Foresight and Wide Search have distinct primary imagery. Existing hero/creature, item semantics, terrain overlay, UI/talent and 100-step Sewers remembered-terrain checks remain passing.
- Existing native Horror fixture, landscape/mouse and portrait/touch: **TEST 60 NATIVE PASS; failures=0**. All eight directions honor popup ON/OFF with no bronze directional marker. The existing actual pointer travel interruption, fresh-action evasion, log-history warning, detection without terrain reveal, inspection, remains/bone-wall checks and saved menu toggle remain. Left/right captures wait for the renderer to present the cleared game view. These are scripted native tests, not a physical Samsung tablet session.
- Offline provenance: **PAINTED assets=1937 source sheets=182 launcher resources=55 failures=0; TEST 44 failures=0**. Only Foresight's atlas cell and inspection painting changed, reproducibly from existing committed paintings; no new generative art. Compiled audit: **classes=3055 guarded browser sinks=1 HTTP/socket calls=0 failures=0**.
- Windows launcher: `desktop/build/windows/1.29.9/Grimhollow/Grimhollow.exe`; packaged and tested JAR SHA256 both `DB713558AD507A829B46DC23F664C94AC2E34784E3D5098C664CAD39119AA89B`. APK `android/build/outputs/apk/debug/android-debug.apk` has actual manifest **1.29.9-INDEV / versionCode 1008**, rebuilt **2026-10-08 12:31 EDT**. Final drinking, Haste and left/right warning captures are visually reviewed.
- Initial extended-fixture attempts needed checked-exception handling, an unpaused drinking sprite and opaque-paint rather than padding size, plus capture timing after both menu closure and the warning's next rendered frame. The enlarged bottle capture uses the diagnostic camera's actual projection matrix and an explicit pixel comparison with the same pose without the bottle. Failed attempts are not counted as passing. No game check or threshold was weakened.
- Physical Samsung tablet and a full human campaign remain **NOT RUN**. Test 45 remains **Abandoned: test failed**, not rerun. Prior retired tests and permanent known issues retain their checkpoint status; full CI remains enabled, with the exact release result reported at delivery.

# Horror warning presentation and toggle - v1.29.8

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 45s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. The existing shared-tuning scenario covers the popup's default ON, ON/OFF labels, persisted OFF and reset to ON.
- Existing native Horror fixture passes in landscape/mouse and portrait/touch: **TEST 60 NATIVE PASS; failures=0**. The default popup remains visible until a response; switching OFF during that live warning hides it, retains the exact log warning and preserves the interrupted auto-travel and safe fresh-action evasion. Actual menu clicks switch OFF/ON and preference reload confirms both values. Existing detection, inspection, remains and bone-wall checks also pass. Reviewed captures: `verification/interface/{landscape,portrait}/horror-warning.png`, `horror-warning-popup-off.png`, `horror-balance-popup-off.png`, `horror-balance-tuning.png`.
- The bronze directional ring and its callback are removed. No artwork is generated or changed. The floor-entry omen, detection silhouettes, sound/log cues and ambush behavior remain unchanged. Initial build of the extended fixture failed on an unqualified message reference; it was corrected before the final passing run.
- Windows launcher: `desktop/build/windows/1.29.8/Grimhollow/Grimhollow.exe`; packaged and tested JAR SHA256 both `F5EEE2A2F037F6FCFDD7C523851DC0BA546F9AD64685318B4B6041E978CEECE8`. Actual Android APK manifest is **1.29.8-INDEV / versionCode 1007**, rebuilt **2026-10-08 08:33 EDT**, at `android/build/outputs/apk/debug/android-debug.apk`.
- Physical Samsung tablet and a full human campaign are **NOT RUN**. Terrain contrast test 45 stays **Abandoned: test failed**, not rerun. Full CI remains enabled; exact release results are reported at delivery.

# Regional scenery and grouped bounty crews - v1.29.7

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 19s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. The existing Bounty scenario also checks tight crew placement, distinct role skins and save/load, 40 turns of generated-level patrol with passable positions, follower destinations and leader waiting, shared hunting alerts, and unchanged member resources/Warrants. Fixture grass/traps and water-ripple rendering are isolated from these headless navigation assertions; native room rendering remains checked separately.
- Final native room fixture in landscape/mouse and portrait/touch: **REGIONAL SCENERY PASS; failures=0** in both. It exercises generated floor-15 locked/open gate refresh and pressure plates; floor-20 statues, stairs, continuous pillars, skull stacks and the actual earned Imp shop; generated Vault arrival frame 41; the animated barrier's 64px texture frames in 16-unit world cells; and the painted Amulet celebration with image/button bounds. Existing ritual completion, forge, mine, sight and floor-five checks also pass. Final captures are retained under `verification/interface/{landscape,portrait}/` and visually reviewed.
- Full offline reconstruction: `python tools/recovery_assets.py --check` reports **PAINTED launcher resources=55; assets=1937 source sheets=182 failures=0; TEST 44 failures=0**. Three original source paintings, exact prompts and the deterministic packer are committed. CI needs neither image generation nor Blender.
- Final compiled-handler audit: **classes=3055 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Final distribution and Windows packaged JAR both have SHA256 `A71A5192245EB13F1CDC131ED3871B71BD2A960FD619466C2452270730E9ADD2`. Windows launcher: `desktop/build/windows/1.29.7/Grimhollow/Grimhollow.exe`; Android: `android/build/outputs/apk/debug/android-debug.apk`. After the final camera-fixture adjustment, `desktop:dist` passes in 34s; the unchanged Android production build passes.
- Physical Samsung tablet and a full player-driven campaign remain **NOT RUN**. Terrain contrast test 45 remains **Abandoned: test failed**, not rerun. Initial fixture/export failures were corrected before the final checks and are not counted as passing. Every CI check remains enabled; exact release CI is reported at delivery.

# Cole's Prison stairs meeting - v1.29.6

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 56s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. Existing Bounty component 4 covers both bosses, completion without payment/betrayal, one waiting Cole with accessible stairs, waiting-claim save/load, once-only payment and departure/reload. Ranked Deeds serialization and historical posters still pass.
- Existing native Bounty fixture in landscape/mouse and portrait/touch: **BOUNTY UI PASS; failures=0** in both. Each actual Prison boss death leaves the claim unpaid and shows no betrayal window. Cole is placed one cell from the exit; invoking his NPC interaction opens one dialogue, pays once and records the Wanted notice. Closing removes him, and subsequent ready/arrival callbacks cannot respawn him. Existing acceptance, receipts, urgency, settlements, final sanctums and ranking checks also pass. This is scripted desktop evidence, not a physical tablet playtest.
- Compiled-handler audit: **classes=3054 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Windows packaged JAR matches the tested distribution, SHA256 `11873A7C28F17ADF7510CFAD9C8C75DD63A9EA5311891B4F1D34F5D380E6F447`. Windows launcher: `desktop/build/windows/1.29.6/Grimhollow/Grimhollow.exe`; Android: `android/build/outputs/apk/debug/android-debug.apk`. New stairs/conversation captures are retained in `verification/interface/{landscape,portrait}/bounty-cole-*.png`.
- Physical Samsung tablet and a full human campaign are NOT RUN. Terrain contrast test 45 remains Abandoned: test failed and was not rerun. Exact release CI is reported at delivery.

# Ranked deeds and archived posters - v1.29.5

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 54s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. The existing Bounty scenario checks all three Wandmaker delivery variants, retrieval vs delivery, Ghost completion, actual dark-gold return and guardian/commission, Imp completion, actual paid claims, Wanted snapshot, entry vs survival/dragon death, disk/run/record serialization, independent runs, read-only poster copies, truthful old-record fallback, and merged/saved/collected death-payout receipts. Uncollected cash is not counted as received.
- Final existing native Bounty fixture in **1280x720 landscape/mouse** and **720x1061 portrait/touch**: **BOUNTY UI PASS; failures=0**. Both exercise the real ranking row and Deeds tab, open the saved betrayal notice and actual paid boss poster, close the read-only poster, scroll representative deeds and handle an older record without saved hero/history. Existing quest/UI/boss reward checks remain. These screenshots represent a fixture's quest facts, not a human-played campaign.
- Compiled-handler audit: **classes=3054 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Final Windows launcher is `desktop/build/windows/1.29.5/Grimhollow/Grimhollow.exe`; Android APK is `android/build/outputs/apk/debug/android-debug.apk`. The packaged JAR must match the tested distribution SHA256, verified before delivery. Final capture pairs are `verification/interface/{landscape,portrait}/rankings-*.png` and are visually reviewed.
- Initial native attempts exposed a missing scroll camera and a fixture lookup that did not descend into the scroll content; both are fixed. Optional old-save metadata reads were guarded and the new Imp fixture includes its normal empty reward list. Failed attempts are not counted as passing. The final gate has only the existing deliberate elemental-plan migration fixture diagnostic.
- Physical Samsung tablet and a full human campaign remain **NOT RUN**. Terrain contrast test 45 stays **Abandoned: test failed**, not run. Older finished rankings have no evidence to reconstruct deeds; ongoing old saves label imported history partial. Gameplay, art and scoring rules are unchanged. Release CI remains fully enabled and its exact tag result is reported at delivery.

# Bounty receipts, urgency and Legendary cash - v1.29.4

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 47s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. The existing Bounty scenario checks receipt amount/quarry/bonus in log history, duplicate/reloaded claim protection, fractional countdowns, arrival start, continued elapsed time elsewhere, kill-lock and expiry, Warrant/journal clock text, Legendary/Rare equality, no Legendary urgency, later debt settlement, retained quotes, actual old-format serialization and settled/zero-priced migration exclusions.
- Final existing native Bounty fixture in **1280x720 landscape/mouse** and **720x1061 portrait/touch**: **BOUNTY UI PASS; failures=0**. Both exercise two independent HUD clocks, expiry, HUD tap opening the matching poster, key/enemy/viewport bounds, real target death, the actual Return Warrant button, 720-gold receipt including 120-gold bonus, duplicate-payment rejection, and completed/returned crimson CLAIMED stamps. Existing greetings, all eight notices, quarry skins/reload, visibility, purchase, journal/artwork, tuning, settlements and both actual boss rewards also pass.
- Offline provenance: **PAINTED assets=1671 source sheets=179 failures=0; TEST 44 failures=0**. The original stamp painting and exact imagegen prompt are committed; the existing poster packer recreates its transparent 1024x384 asset. Compiled audit: **classes=3048 guarded browser sinks=1 HTTP/socket calls=0 failures=0**.
- Windows `desktop/build/windows/1.29.4/Grimhollow/Grimhollow.exe` is packaged with the final tested JAR; its SHA256 matches `215A782CB54849FDEC3065781AF89184569C7CAF41E5F2F5E1C6B6E66CB5399D`. Android debug APK builds. Native receipt/HUD/stamp captures under `verification/interface/{landscape,portrait}/` are visually reviewed. Physical Samsung tablet review is **NOT RUN**; test 45 remains **Abandoned: test failed**, not run.
- The first HUD fixture attempt incorrectly required a popup, and an initial old-save fixture referenced an unavailable JSON dependency. Both are corrected in the existing fixtures; failed attempts are not counted as passing. CI retains every check, with the exact release result reported at delivery.

# Bounty floor references - v1.29.3

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeClass=ENCHANTER --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 25s; Runs=10 failures=0**. Eight JUnit tests have zero failures/errors. The existing smoke run includes all Bounty scenarios, including ten generated offices and Warrant acceptance, save/load and rewards. No new harness or gameplay rules.
- Existing native portrait Bounty review: **BOUNTY UI PASS; failures=0**. All eight notices, text/layout, acceptance, Warrant, journal, artwork, visibility and boss rewards pass. The actual Warrant capture `verification/interface/portrait/bounty-warrant.png` is visually reviewed and reads **Last seen on Prison floor 2 (Dungeon floor 7).** The shared location formatter also supplies posters, bounty journal entries and Playtest status. Existing numbering is unchanged.
- `tools/package-windows.ps1` produces `desktop/build/windows/1.29.3/Grimhollow/Grimhollow.exe`; Android debug APK builds. Physical tablet review is **NOT RUN**. Artwork is unchanged, and test 45 remains abandoned. Full CI remains enabled, with the exact release result reported at delivery; unchanged gates are not claimed as locally rerun.

# Named quarry paintings - v1.29.2

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 20s; Runs=30 failures=0**. Eight JUnit tests report zero failures/errors. No combat, quest or animation timing changes.
- `python tools/recovery_assets.py --check`: **PAINTED assets=1670 source sheets=178 failures=0; launcher resources=55; TEST 44 failures=0**. Six original paintings and exact prompts are committed. Existing world creature art reproduces identically; the new atlases preserve their species' frame dimensions and normalized animation rectangles. Offline assembly needs no generation service.
- Native `-Dgrimhollow.interfaceReview=true -Dgrimhollow.bountyReview=true` passes **1280x720 landscape** and **720x1061 portrait**: **WANTED SKINS: six distinct textures; ordinary species unchanged; save/load and animation timing/layout preserved; failures=0**, followed by **BOUNTY UI PASS; failures=0**. Coverage includes eight centered notices/completion stamp, mouse/touch acceptance and purchase, unseen/invisible seal suppression, Warrant/journal/artwork, tuning, settlements and both actual boss rewards. Native named-quarry lineups and all six matching portrait notices are visually reviewed.
- Native `-Dgrimhollow.geometryTests=true --smoke-sewers` reports **TEST 24 heroes=9 mob sprites=134 steady idle checks=134 failures=0**, **eightfold source density atlases=83 maximum texture dimension=4096 failures=0**, complete animation coverage and passing tests **24-26, 34, 36**. Existing 12,960 generic hero draws, 397 named item indices, 60 identity icons, 113 custom skill icons, 15 plants and bronze rank/key layouts remain passing. Test 35 retains its recovery retirement.
- `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.29.2.jar`: **TEST 46 compiled handlers: classes=3046 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. `tools/package-windows.ps1` produces `desktop/build/windows/1.29.2/Grimhollow/Grimhollow.exe`; packaged and tested JAR SHA256 match **B34DACFA588E9A8CF8177324A2545879D0A0A7A95A934F1AE8518F121E51D3BF**. Android debug APK builds. Physical tablet review is **NOT RUN**; test 45 remains **Abandoned: test failed**, not run. Unchanged fog/effects/live-encounter checks remain enabled in the full CI workflow; their exact release result is reported at delivery.

# Wanted poster refinement - v1.29.1

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 18s; Runs=30 failures=0**. The subsequent text-spacing build runs `core:test desktop:dist android:assembleDebug`, **BUILD SUCCESSFUL in 1m 16s**, with **8 JUnit tests, zero failures/errors**. The final seal-label alignment packages desktop/Android successfully in **43s**. No combat or quest rules change.
- `python tools/recovery_assets.py --check`: **PAINTED assets=1639 source sheets=172 failures=0; launcher resources=55; TEST 44 failures=0**. Two new original source paintings, exact prompts and offline poster packing are committed. Creature portraits use existing original paintings in 512px atlas frames; ordinary world sprites/indicators remain unchanged.
- Final native `-Dgrimhollow.interfaceReview=true -Dgrimhollow.bountyReview=true` in **1280x720 landscape** and **720x1061 portrait** reports **BOUNTY UI PASS; failures=0**. The existing fixture asserts centered ink lines, readable inter-word gaps, outline-free lettering, the requested vertical hierarchy and larger portrait/seal frames for all seven notices and the completed poster. Real mouse/touch acceptance/purchase, all nine greetings/betrayals, seals, Warrant/journal/artwork, tuning, settlements and both actual boss rewards also pass. Common, rare and legendary portrait captures are visually reviewed under `verification/interface/`.
- `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.29.1.jar`: **TEST 46 compiled handlers: classes=3039 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. `tools/package-windows.ps1` produces `desktop/build/windows/1.29.1/Grimhollow/Grimhollow.exe`; its packaged JAR hash matches the final tested JAR. Android debug APK builds. Physical tablet review is **NOT RUN**; test 45 remains **Abandoned: test failed**, not run.
- Visual review caught cramped legacy spacing after removing the outline and a seal label above its plaque. Both are corrected and the final native checks pass. Initial sandbox compilation failed on a protected cached dependency; the established authorized build route succeeded. CI checks are unchanged and the exact tag result is reported at delivery. Named bounty creature variants were discussed, not implemented.

# Quest and interface presentation - v1.29.0

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 20s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. Final game changes rebuild desktop and Android successfully in **1m 46s**; the desktop-only inspection-fixture repair rebuilds successfully in **22s**. Existing Bounty scenarios cover ten generated office layouts, reachability, five separated display cells, decoration persistence and idempotent stock-preserving migration. Existing Hatchling scenarios cover Blank Parchment's 5-gold unit value, minor-meal eligibility and one-sheet consumption.
- `python tools/recovery_assets.py --check`: **PAINTED assets=1635 source sheets=170 failures=0; launcher resources=55; TEST 44 failures=0**. Four new original paintings, exact prompts and offline packing are retained. The audit includes source-resolution previews and the actual engraved Bloodmark motif. The first preview check failed on stale low-resolution exports; repacking repaired them without weakening the check.
- Native geometry reports **TEST25 items=397 identification icons=60 failures=0**, **PAINTED HERO SELECTORS symbols=92**, **unique skills=113 plants=15**, and passes tests **24-26, 34, 36**, the bronze rank/key layouts and lighting on/off. Retained hero talent frames are 64 texture pixels in 16 logical units. Test 35 retains its recovery retirement.
- The existing artwork fixture passes **13 subjects** in **1280x720 landscape** and **720x1061 portrait**: **ARTWORK UI PASS; failures=0**, including identified consumable emblems in both inspection and enlarged artwork, unknown identity suppression, retained talent source artwork, native mouse/touch, return-window preservation and no turns/charges/identification changes. An initially misconfigured known-potion fixture failed; the fixture now explicitly establishes and restores unknown knowledge instead of relaxing the assertion.
- The existing Bounty fixture passes both orientations: **BOUNTY UI PASS; failures=0**, covering all nine greetings/betrayals, seven parchment posters/completion stamp, mouse/touch acceptance and purchase, unseen/invisible seal suppression, Warrant/journal/artwork, tuning, settlements, both actual boss deaths/mask rewards and generated floors 25/26. Final office, poster, Yog platform and Amulet sanctum captures under `verification/interface/{landscape,portrait}/` are visually reviewed.
- `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.29.0.jar`: **TEST 46 compiled handlers: classes=3038 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Windows `desktop/build/windows/1.29.0/Grimhollow/Grimhollow.exe` packages the final JAR and bundled runtime; the Android debug APK builds. Physical Samsung hardware and full campaign balance are **NOT RUN**. Unchanged fog/effects/live-encounter gates are left to the complete CI workflow; no local pass is claimed for checks not run. Test 45 remains **Abandoned: test failed**, not run.
- Final CI status is reported with the release tag. No combat, quest rewards or difficulty tuning changed. Existing cluttered offices may retain sale positions rather than displacing player belongings.

# Artwork inspection - v1.27.0

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 13s; Runs=30 failures=0**. Eight JUnit tests report zero failures/errors. The creature pointer-priority follow-up rebuilds desktop/Android successfully in 44s; the isolated existing-fixture mode changes desktop verification only and rebuilds in 23s.
- `python tools/recovery_assets.py --check`: **PAINTED assets=1283 source sheets=161 failures=0; launcher resources=55; TEST 44 failures=0**. The 1,128 added inspection exports and rectangle lookup reproduce from existing sources. Default world, creature, item and UI atlas pixels/indices are unchanged. No art is generated.
- `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.27.0.jar`: **TEST 46 compiled handlers: classes=3010 guarded browser sinks=1 HTTP/socket calls=0 failures=0**.
- The existing native interface fixture's artwork portion (`-Dgrimhollow.interfaceReview=true -Dgrimhollow.artworkReview=true -Dgrimhollow.interfacePortrait=false/true`) passes in **1280x720 landscape** and **720x1061 portrait**: **ARTWORK UI PASS: 11 subjects, source-resolution exports/fallback, native mouse/touch, modal return, independent scale, no turns/charges/identification changes; failures=0**. Subjects are an item, unknown potion, creature, plant, trap, terrain, talent, buff, journal note, hero painting and composite tile. Both enforce image/window bounds, preserved originating window and source frame, inventory invariants and disposal of the open preview texture. Captures are `verification/interface/{landscape,portrait}/artwork-*.png` with representative captures visually reviewed.
- Initial local full interface attempts passed the earlier menu/readability checks, then exposed creature input priority or Windows clipboard contention in the unrelated issue-report copy check. Priority is corrected; the artwork checks run separately through the same fixture and all pass. Full CI continues to run the entire fixture with the new artwork assertions at its end. No clipboard assertion is disabled or declared passing locally. Physical Samsung hardware is **NOT RUN**.
- Windows `desktop/build/windows/1.27.0/Grimhollow/Grimhollow.exe` packages the final code, painted icon and bundled runtime; Android debug APK builds. Composite/retained small artwork is enlarged from its exact native frame when no larger source exists. Test 45 stays **Abandoned: test failed**, **NOT RUN**. Earlier numerical subjects retain their checkpoint/retired statuses below; unchanged region/fog/effects gates are verified by CI separately.

# Mystery and Playtest usability - v1.26.0

- Existing generated gate and JUnit: `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain` reports **BUILD SUCCESSFUL in 1m 38s; Runs=30 failures=0**. Eight JUnit tests have zero failures/errors. Existing scenarios cover individual/bulk Waterskin refill, device-wide exclusions and pool safeguards (including 200 subtype-filtered Caves rotations), Golden Mimic directions, Precognition/Dead Man's Hand ordering and Rebuff ranks, refresh, persistence, expiry and failed casts.
- Native landscape **1280x720** and portrait **720x1061** interface checks pass menu navigation, page persistence, main/resume, potion exclusions and Class Items/Focus Crystal. Both also pass the modal hunger warning before food consumption, clearing queued actions/rest, and requiring a fresh response. Room checks pass generated floor-five locked/open gateway refresh and painted Rat King cushion. Captures in `verification/interface/{landscape,portrait}/` are reviewed after genuine rendering frames.
- Both orientations pass the existing polish, Horror and Expedition fixtures: long-description touch/wheel/arrow input, last-line access, four-key HUD, independent armor/weapon Etching, journal icons, Scribe, elemental mechanisms, Doubloon/Chart actions and Golden Mimic guard/follow commands through the harness and examination. Gold collection has no healing effect.
- Existing geometry passes **12,960** generic hero/armor/facing/loadout/pose draws, nine heroes, 126 steady creature forms, 394 named item indices, 60 identity emblems and the bronze point/key layouts. Effects pass six painted particle families including the actual Living Earth missile particle, 16 Speck kinds, rays and scorch behavior. Unchanged test 31: **off pixel differences=0; mean=0.6276ms; p95=0.9340ms; failures=0**, both below 2ms.
- Existing test 47 passes all five regions before/after generated walking and door transitions: three zooms, four pan positions, every-cell framebuffer assertions, **fogTexel/cell=1:1; worldUnits=16; lightQuad=aligned; failures=0**. Existing remembered-terrain test 43 also passes all five. Test 45 remains **Abandoned: test failed**, not run.
- Full offline provenance: `python tools/recovery_assets.py --check` reports **PAINTED assets=155 source sheets=161 failures=0; launcher resources=55; TEST 44 failures=0**. The three new source paintings and exact prompts are committed. Native Windows `1.26.0/Grimhollow/Grimhollow.exe` is packaged with the painted icon/runtime; its isolated launch exits **0**, renders Sewers with lighting on/off and reports PASS. Android debug APK builds; no physical tablet test is claimed.
- Final `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.26.0.jar` reports **TEST 46 compiled handlers: classes=3007 guarded browser sinks=1 HTTP/socket calls=0 failures=0**.
- Numeric-copy checks for Ashlight feeding, Hatchling hunger, Rune Etching and talent ranks are **RETIRED - superseded by the requested mystery presentation**. Replacement checks require resolved thematic text, distinct progression, live hunger cues and scrolling; effect magnitudes and gameplay assertions remain. Earlier page-route, old-formula and capture-lock attempts failed, then corrected fixtures passed. No threshold or game assertion was weakened. English fallback and physical Samsung review remain noted in KNOWN_ISSUES.md.
- Release CI retains all seven jobs (Linux/Windows desktop, Android, combined and three class gates). Its published result is reported with the delivered tag; local evidence above is not a claim about a remote run.
- Initial branch run **37401404725** passed Windows/Android, Linux provenance/JUnit/audit/fog/geometry/effects and all interface, room, Horror, Expedition, Vault and class-selection steps, but failed the live encounter fixture at its intentional death after action 150. Local replay passed **150 actions, 128 steps, 21 attacks, actor drops/pickup, summon and 120 death frames**. A scheduled-death barrier fixes that fixture race without weakening any assertion or changing the game. The failed run remains available; the follow-up is verified separately.
- The synchronized fixture is rebuilt (**BUILD SUCCESSFUL in 28s**) and passes two isolated native repeats: **150 actions / 126 steps / 23 attacks** and **150 actions / 117 steps / 32 attacks**, both with actor drops/pickup, summon, **120 death frames; failures=0**. The current Windows launcher is repackaged. All three individual CI class gates and the combined **Runs=30 failures=0** gate also passed on the initial commit; its overall run remains FAIL because of the fixture race.
- Game commit **789f0cdd46d86a0e48c6c17a3603fd311c5238f7**, tag **v1.26.0-mystery-playtest**: [branch run 37403674554](https://github.com/BryanHartling/grimhollow/actions/runs/37403674554) is **SUCCESS, all seven jobs** (both desktops, Android, combined and each class). [Tag run 37403674799](https://github.com/BryanHartling/grimhollow/actions/runs/37403674799) is **FAIL** only at the unchanged test-31 effects timing gate: mean **1.5280ms**, p95 **2.0028ms**, required both **<2ms**; every fog cell/region and the repaired 150-action/120-death-frame encounter checks pass. GitHub rejects its failed-job retry with **HTTP 403**. Preserve the failed run and strict threshold. This report-only follow-up does not change packaged code; any later CI result is distinct from the validated game commit.

# Key HUD, complete descriptions and minion decay - v1.23.3

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 9s; Runs=30 failures=0**. Seven JUnit tests have zero failures/errors. Final Update Log/interface packaging passes in **45s**; Windows `1.23.3/Grimhollow/Grimhollow.exe` is packaged with its icon/runtime and the updated APK is built.
- Initial tag/branch CI runs **37250259548/37250259385** caught a real pickup regression: `Text measured from the actor thread!` while building the new key count. The follow-up queues scene-safe HUD updates on the render thread. `core:test desktop:dist android:assembleDebug --no-daemon --console=plain` passes in **2m 32s**, and Windows packaging succeeds. The strengthened existing native encounter fixture passes **150 actions, 121 steps, 28 attacks, real actor-thread key pickup, summon/drop/pickup and 120 death frames; failures=0**. Portrait and landscape input/key fixtures pass again. Failed runs remain available; no checks are weakened.
- Existing Necromancer scenario asserts **15% maximum-health decay, rounded up**, for all four servant types at every maximum health from 1 to 200. A full-health 19-HP skeleton loses exactly three HP per tick and dies on the seventh decay tick; save persistence, countdown, grace/cap and no expiry explosion/revival remain green. All four servant and summon descriptions use the new rate.
- Native landscape **1280x720** and portrait **720x1061** polish fixtures pass **LONG ITEM INPUT PASS**: Vorpal Quarterstaff, Ashlight Lantern and Phylactery, compact and natural viewports, real touch dragging, mouse wheel, repeated arrow clicks, final glyph/camera alignment and action bounds. The runtime Lantern description asserts the exact repaired feeding costs and absence of broken dash bytes/placeholders. Top/bottom screenshots are visually reviewed in the existing interface folders.
- Both orientations pass **HUD KEYS PASS**: actual pickup, all four current-floor key types/counts, separate row bounds, consumption, Notes save/load, past-floor reminder, branch exclusion and hidden empty row. Journal unread acknowledgement remains green. Captures: `verification/interface/{landscape,portrait}/collected-key-hud.png`.
- Native geometry passes: all 386 named item IDs/60 identity icons, nine heroes/126 forms, 38 bronze rank states, eight tier/width layouts and **10368** equipment draws; key counts and rendered glyphs survive three layouts. Existing generated Sewers memory walk passes **test 43**, 100 steps/24 turns/eight doors. Compiled handler audit passes **test 46**, 2984 classes, one guarded browser sink and zero HTTP/socket calls. **Test 45: Abandoned: test failed**, not run.
- The first full gate failed on unrelated generated loot beneath a headless sentry; remove that heap from the overwritten fixture cell and retain every timing assertion. Initial description fixtures incorrectly expected short Phylactery text to overflow; explicitly bounded viewports now exercise every item. Native capture setup was corrected to allow normal rendered frames before screenshots. Failed attempts are not passing gates. Physical Samsung tablet verification remains with the user.

# Bone-wall and Horror follow-up - v1.23.2

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain` passes in **2m 4s**, **Runs=30 failures=0**, seven JUnit tests with zero failures/errors. Final inspection-title packaging passes in **49s**; native Windows app is packaged with its icon/runtime.
- Existing test 60 now uses a full-health rat rather than a one-HP victim and adds **200 full-health floor-two rat pounces**, covering seeded actual attacks at **100% and 200%** damage. Every pounce kills the ordinary rat before retaliation. Stronger prey still survives and wakes; normal hero-facing damage is independently bounded in every region. Warning/response, invisibility, solitary hunting, recovery, escape routes, predation ownership/remains and save/load all remain green.
- Bone-wall identity and meaningful terrain inspection join the existing Wand of Bone scenario; blocking, bolt collision, five-turn expiration, save/load and floor-exit cleanup still pass. The wall's mechanics are unchanged.
- Existing native Horror scenario passes in **1280x720 landscape and 720x1061 portrait**: real pointer/touch travel interruption; the larger HUD notice remains visible until a fresh response; an actual evasive movement cancels damage. Entity sensing does not alter FOV, visited or mapped terrain. Actual Wand of Bone placement, new painted inspection icon and **five remaining turns** are asserted and visually reviewed in `verification/interface/{landscape,portrait}/`.
- `python tools/recovery_assets.py --check`: **152 painted assets, 155 source sheets, 55 launcher resources; TEST 44 failures=0**. Four bone-wall variants compile offline from the committed generated source and exact prompt. `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.23.2.jar`: **2982 compiled classes, one guarded browser sink, zero HTTP/socket calls, failures=0**.
- The first build failed because the new seeded regression used an unavailable `Random.seed` method; it now uses the project's scoped `pushGenerator/popGenerator` API. This failed attempt is not a passing gate. Human campaign balance and physical Samsung tablet review remain outstanding; the original rat save is unavailable. **Test 45: Abandoned: test failed**, not run.

# Talent trim and equipment follow-up - v1.23.1

- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain` passes in **2m 27s**, **Runs=30 failures=0**; seven JUnit tests have zero failures/errors. Final adaptive-panel packaging (`desktop:dist android:assembleDebug`) passes in **1m 26s**, and the native Windows launcher is packaged with its icon/runtime.
- Existing generated red-sentry scenario now exercises actual Frost attach/thaw and Potion of Haste on wet/dry ground: ten seeds, four approach directions, four modes, **160 traversals**. Clean/dry-thaw Haste has zero shots/interruptions; normal speed has **400 shots**; water-thaw Chill with Haste produces **600 interruptions**, with an initial step of **2/3 turn versus 1/3**. Thaw/speed/sentry mechanics are unchanged. The first headless potion fixture failed because it requested scene-only spell effects; using its ordinary offscreen effect path isolates the actual potion and timing behavior.
- Native Windows recovery/geometry probe passes: **HERO EQUIPMENT draws=10368 failures=0**, all nine heroes, all eight armor rows, both facings, twelve loadouts, six poses. Independent actual-pixel tests require the palm and weapon handle to be opaque, then check grip attachment and carry/attack direction. Initial Enchanter pose-14 anchors failed **176** draws; moving the declared hand from the fingertip onto the palm corrects the failure without altering the painting.
- Native **UI RANK/KEY** passes **38** bronze pip states, round-alpha/centre opacity, larger tier markers, modern shuffle atlas and **eight tier/width layouts**, including wrapped tier-4 points. Key/cursor/compass and torch-order checks pass; **54** grass-depth checks, **126** steady creatures, **386** item indices and **60** identity emblems remain green.
- `python tools/recovery_assets.py --check`: **151 painted assets, 154 source sheets, 55 launcher resources; TEST 44 failures=0**. The talent trim is original vector geometry in the offline packer. Existing character paintings/animation sheets are unchanged; both grip metadata files reproduce.
- Full native landscape **1280x720** and portrait **720x1061** interface probes pass, including real pointer/touch rank purchase, repeat-purchase cap protection, scrolling and the existing gameplay-menu regressions. Refreshed talent panels and both-facing Sickle/Bone Rod poses are visually reviewed; captures remain in the existing interface and equipment folders. Physical Samsung tablet review remains with the user.
- **Test 45: Abandoned: test failed**, not run. Exact remote CI is checked after pushing; checks remain unchanged.

# Playtesting patch - v1.23.0

- Final Windows/Android build: `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain` -> **BUILD SUCCESSFUL in 1m 47s** (the final spell-text packaging gate subsequently passes in **1m 22s**, **Runs=30 failures=0**), **Runs=30 failures=0**. The portrait-label layout build passes in **1m 14s**; final adaptive-panel Windows/Android packaging passes in **1m 10s**. Seven JUnit tests report zero failures/errors. Native Windows app packaged with its icon and runtime.
- Existing Necromancer gate covers fixed hero-level Necrotic Touch, one refreshed wound, exact first-tick damage, duration/budget and save/load; thrown attacks remain excluded. All four servant descriptions and nine spell descriptions resolve. Binding expiration now asserts living minions with exact decay loss, saved decay, eventual death without expiry explosion/revival, and retained cap/grace rules.
- Generated sentry rooms: ten seeds, four entrance sides and both movement speeds; **80 traversals**, hasted shots **0**, normal shots **400**. Uses existing red-sentry AI and generated prize routes. The first fixture selected stale unrelated chests and failed four seeds; selection now uses the generated room pedestal. No sentry gameplay change; the user's original encounter was not reproduced.
- Native renderer: **HERO EQUIPMENT draws=2160 failures=0** (nine classes, cloth/plate, both facings, ten loadouts, six poses); exact handle/pivot and Bone Rod/spear/shortsword tip direction checked. **UI RANK/KEY rendered sockets=38 failures=0**; filled sockets are bright gold, empty sockets dark; key counts/pixels survive three layouts. Painted cursor/compass dimensions and torch-before-actor layer order pass.
- Existing 24-26/34/36 geometry gates pass: nine hero sheets, 126 steady creature sprites, 386 item indices, 60 unique identification paintings, 113 skills, 15 plants and 63 trap color/state combinations. Grass depth remains **54 checks, failures=0**.
- Full `python tools/recovery_assets.py --check`: **150 painted assets, 154 source sheets, 55 launcher resources**; **TEST 44 failures=0**. Shipping art and both grip metadata files reproduce offline from committed paintings/prompts and immutable existing sources. Identification and new-control/landmark boards visually reviewed.
- Existing landscape/portrait polish checks exercise real mouse/touch navigation to the adaptive framed update screen and Continue, home Update Log, history, Feed, painted notifications, independent armor Etch, journal/keys, Scribe and elemental seals. The new smooth-font default preserves an explicit font preference. Final captures remain in `verification/interface/`.
- Initial protected-group helper access was corrected; smooth text exposed a portrait Update Log label overflow, fixed by sharing row width according to each label; a subsequent Windows mapped-file lock on a generated weapon capture was resolved by moving that capture aside and rerunning the unchanged geometry gate. These are failed attempts, not passing results.
- Test 45 remains **Abandoned: test failed**, not run. Physical Samsung tablet performance and campaign balance are human review; native portrait input is not a device playtest. Final remote CI is checked after push without weakening any check.

# Sprint component 7 - Psychic v1.22.6

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 1m 9s**; native Windows application packaged. Focused reproducibility: **PAINTED assets=1 source sheets=149 failures=0**.
- Existing geometry: **HERO EQUIPMENT: native draws=1728 cloth/plate, both facings, eight loadouts, movement/action poses, secondary and thrown capture; failures=0**. Nine heroes, 126 creature sprites, 386 items and 60 identity emblems pass. Source, fitted cloth/plate poses, animation preview and actual lit/unlit dungeon captures reviewed in verification/heroes/psychic/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run. Physical tablet and campaign review remain with the user.

# Complete sprint - v1.22.6

- All seven components and seven further hero batches delivered; Warrior/Enchanter prototype extends to all nine. Final `core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 9s**, **Runs=30 failures=0**, seven JUnit tests with zero failures/errors. Native Windows launcher packaged; Android APK built.
- Final existing native geometry: **HERO EQUIPMENT draws=1728 failures=0**, all nine heroes, cloth/plate, both facings, eight loadouts and six movement/action poses; secondary and thrown capture/restoration pass. **GRASS DEPTH checks=54 failures=0** for all nine bodies and both grass states with movement offsets; no omitted/duplicated partition cells, upper-body cover or missing foot blades at rest.
- Existing 24-26/34/36 gates pass: nine hero sheets, 126 creature sprites with steady idles, 386 item indices, 60 unique painted identity emblems, 113 custom skills and 15 plants. Existing semantic assertions cover Mind Vision/Identify/Magical Sight and all added item slots. No assertion or timing threshold weakened.
- Final full `python tools/recovery_assets.py --check`: **146 painted assets, 149 source sheets, 55 launcher resources; TEST 44 failures=0**. Artwork builds offline from committed paintings/prompts, including both grip metadata files. No generation service or Blender in CI.
- Refreshed **18 native dungeon captures** (nine heroes × cloth/plate), each with lighting on/off, all pass against v1.22.6. Sources, comparisons, poses/GIFs, native equipment and final lit crops reviewed. Evidence: `verification/heroes/sprint-summary.png`, `sprint-ingame.png`, per-hero folders and prototype weapon crops. The sprite artwork preserves game timing/world height; the class and behavior changes are the separately agreed sprint work.
- Physical Samsung tablet and full campaign balance remain human review. Test 45 stays **Abandoned: test failed**, not run. CI runs and logs are retained on GitHub; no check disabled.
# Sprint component 7 - Necromancer v1.22.5

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 44s**; native Windows application packaged. Focused reproducibility: **PAINTED assets=1 source sheets=148 failures=0**.
- Existing geometry: **HERO EQUIPMENT: native draws=1536 cloth/plate, both facings, eight loadouts, movement/action poses, secondary and thrown capture; failures=0**. Nine heroes, 126 creature sprites, 386 items and 60 identity emblems pass. Source, fitted cloth/plate poses, animation preview and actual lit/unlit dungeon captures reviewed in verification/heroes/necromancer/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run. Physical tablet and campaign review remain with the user.

# Sprint component 7 - Cleric v1.22.4

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 46s**; native Windows application packaged. Focused reproducibility: **PAINTED assets=1 source sheets=147 failures=0**.
- Existing geometry: **HERO EQUIPMENT: native draws=1344 cloth/plate, both facings, eight loadouts, movement/action poses, secondary and thrown capture; failures=0**. Nine heroes, 126 creature sprites, 386 items and 60 identity emblems pass. Source, fitted cloth/plate poses, animation preview and actual lit/unlit dungeon captures reviewed in verification/heroes/cleric/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run. Physical tablet and campaign review remain with the user.

# Grass foreground follow-up - v1.22.4

- Existing geometry gate extended: **GRASS DEPTH checks=54 failures=0**, all nine heroes, standing/rustled grass and movement offsets. Grass drawing has disjoint complete background/foreground partitions; the northern foreground tile cannot cover the head, while the current cell retains foot blades at rest.
- Live Cleric capture confirms the upper body remains visible in a grassy spawn cell. One additional cached tile batch reuses the grass texture; synchronized actor snapshots protect rendering during summons/despawns. No source pixels, terrain, fog or gameplay rules changed.

# Sprint component 7 - Duelist v1.22.3

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 46s**; native Windows application packaged. Focused reproducibility: **PAINTED assets=1 source sheets=146 failures=0**.
- Existing geometry: **HERO EQUIPMENT: native draws=1152 cloth/plate, both facings, eight loadouts, movement/action poses, secondary and thrown capture; failures=0**. Nine heroes, 126 creature sprites, 386 items and 60 identity emblems pass. Source, fitted cloth/plate poses, animation preview and actual lit/unlit dungeon captures reviewed in verification/heroes/duelist/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run. Physical tablet and campaign review remain with the user.

# Sprint component 7 - Huntress v1.22.2

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 43s**; native Windows application packaged. Focused reproducibility: **PAINTED assets=1 source sheets=145 failures=0**.
- Existing geometry: **HERO EQUIPMENT: native draws=960 cloth/plate, both facings, eight loadouts, movement/action poses, secondary and thrown capture; failures=0**. Nine heroes, 126 creature sprites, 386 items and 60 identity emblems pass. Source, fitted cloth/plate poses, animation preview and actual lit/unlit dungeon captures reviewed in verification/heroes/huntress/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run. Physical tablet and campaign review remain with the user.

# Sprint component 7 - Rogue v1.22.1

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 59s**; native Windows application packaged. Focused reproducibility: **PAINTED assets=1 source sheets=144 failures=0**.
- Existing geometry: **HERO EQUIPMENT: native draws=768 cloth/plate, both facings, eight loadouts, movement/action poses, secondary and thrown capture; failures=0**. Nine heroes, 126 creature sprites, 386 items and 60 identity emblems pass. Source, fitted cloth/plate poses, animation preview and actual lit/unlit dungeon captures reviewed in verification/heroes/rogue/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run. Physical tablet and campaign review remain with the user.

# Sprint component 7 - Mage v1.22.0

- Windows/Android build and seven JUnit tests: **BUILD SUCCESSFUL in 2m 29s**. Focused offline Mage export check: **assets=1 sources=143 failures=0**.
- Existing native geometry: **HERO EQUIPMENT draws=576 failures=0**, three revised heroes, cloth/plate, both facings, eight loadouts and six poses; nine hero/126 creature/386 item/60 identity checks pass. Source, armor comparison and poses visually reviewed. Live cloth/plate evidence is in erification/heroes/mage/native/.
- No gameplay or timings changed. Test 45 remains abandoned and is not run; physical tablet review is still required.

# Sprint component 6 - v1.21.5

- Final `core:test desktop:dist android:assembleDebug --no-daemon`: **BUILD SUCCESSFUL in 1m 15s**, seven JUnit tests pass; native Windows application packaged. Initial package-name compile errors were corrected before release.
- Existing native geometry gate: **HERO EQUIPMENT native draws=384, failures=0** across Warrior/Enchanter, cloth/plate, both facings, eight loadouts and six poses. Secondary-ability and thrown-weapon capture/restoration pass. Existing 24–26/34/36 gates pass. Layered hero measurements use the game's alpha blending; raw single-atlas semantic comparisons stay exact and unblended.
- Four fresh live dungeon captures (two heroes × cloth/plate) pass with lighting on/off. Painted sources, poses, native equipment and actual in-game captures were visually reviewed; handle locations corrected for reversed inventory blades and lowered caster weapons point away from the face. Evidence: `verification/heroes/pilot/weapon-aware.png`, poses/GIF and native folders.
- Full `tools/recovery_assets.py --check`: **146 painted assets, 142 sources, 55 launcher resources, TEST 44 failures=0**; includes deterministic hand/weapon grip metadata. Two reused draw quads, no added GPU textures; no combat callbacks/timings changed. Physical tablet validation remains human review; test 45 is abandoned and not run.
# Sprint component 5 - v1.21.4

- `core:test desktop:dist android:assembleDebug --no-daemon`: **BUILD SUCCESSFUL in 1m 13s**, seven JUnit tests pass. Final desktop rebuild after correcting a stale diagnostic label: **BUILD SUCCESSFUL in 34s**. Native Windows application packaged.
- Existing native geometry and polish fixtures: **exit=0**. Tests 24–26/34/36 pass: nine heroes, 126 creature sprites, 386 item indices, 60 dedicated identity emblems, 113 custom skills and 15 plants. Explicit source/pixel uniqueness and the dedicated guide serpent pass. Polish includes real independent armor/weapon Etch, journal and elemental treasury input checks.
- `python tools/recovery_assets.py --check`: **146 painted assets, 142 source sheets, 55 launcher resources; TEST 44 failures=0**. Art is compiled offline from committed paintings. Small-size visual board: `verification/interface/identity-emblems.png`; identified Mind Vision and Magical Sight now use separate atlas cells. Random unidentified appearances and intentional same-item aliases remain.
- Test 45 remains **Abandoned: test failed**, not run. Physical tablet review remains with the user; no claim of an Android device test.
# Sprint component 4 - v1.21.3

- Final `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 4s**; seven JUnit tests pass; **Runs=30 failures=0**. All existing spell/growth/domination/rank gates pass.
- **PSYCHIC WEAPON/DETECTION PASS**: weapon-only kit; all eleven melee level breakpoints and 0.8-turn timing; actual damage including Strength and speed is below suitable upgraded weapons at Strength 10/15/20; melee grants no charge-spending XP; rank 0/1/2 carried casting/recharge gates and cadence; legacy artifact-slot migration with chosen weapon retained or old Focus Ring stored; Crystal charge, level and XP preserved.
- Item markers assert radius, duration, fade, expiration, save/load and no revisit refresh without modifying heap discovery or surrounding terrain. Trap rolls are probabilistic, saved and cannot reroll while waiting/reloading; manual extension respects ordinary sight, independently of Mind Vision. Existing Seer three-cell through-wall enemy/secret discovery still passes.
- Native interface fixtures pass in portrait and landscape: **TEST 53 UI PASS ... failures=0** covers real Hurl enemy/direction selection and charge, scrollable Crystal/skill descriptions and bounds. Crystal weapon description/screenshots visually inspected. First headless run stopped on renderer-only discovery particles; guard visuals outside GameScene, keeping the actual discovery assertions intact.
- Test 45 remains **Abandoned: test failed**, not run. Physical tablet/campaign balance confirmation remains with the user. Full CI checked after push with unchanged gates.

# Sprint component 3 - v1.21.2

- **TEST 37 ARMOR PASS**: simultaneous weapon/armor runes, independent paid upgrades and category transfers, no rerolls on transfer, floor effects and accumulated knowledge, legacy unupgraded counterpart, crown ownership, carrier loss, resolved descriptions and save/load. Existing weapon transfer checks still pass. **Runs=30 failures=0**; seven JUnit tests pass.
- Desktop and Android: **BUILD SUCCESSFUL in 2m 3s**. Adapted the existing native Etch fixture to transfer an armor rune from stored armor while retaining the weapon attachment; portrait and landscape both pass **TEST 37 UI PASS ... failures=0** with real input. No new gameplay probe or acceptance document.
- No paid upgrade is granted by migration; the new counterpart starts at level zero. All prior checks and test-45 abandonment remain unchanged. Physical tablet review remains with the user.

# Sprint component 2 - v1.21.1

- Extended **TEST 60 Horror PASS** in all three class gates: routed dead-end escape, visible flight exclusion, concealed room-entry recovery, hero evasion without attack, physical bump interruption/occupancy, full-reveal exposure, skipped recovery at full health/spent allowance, save/load and the original finite-healing/warning/generation contracts. **Runs=30 failures=0**.
- `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain` plus the corrected diagonal-route fixture rerun: seven JUnit tests pass; final build **BUILD SUCCESSFUL in 51s**. Existing native Horror checks pass in landscape and portrait, **TEST 60 NATIVE PASS ... failures=0**. The first portrait invocation lacked the launcher's `interfaceReview` surface flag; its geometry assertion failed, then the correct tall-surface invocation passed.
- Damage remains at the existing default 100%; the Sewers headless warned burst is bounded at six damage. This is behavior verification, not a complete campaign balance verdict. Physical tablet and campaign danger remain human playtests.
- Test 45 remains **Abandoned: test failed**, not run. CI checks unchanged.

# Sprint component 1 - v1.21.0

- `gradlew.bat core:test desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 49s**, seven JUnit tests with zero failures. Packaged `desktop/build/windows/1.21.0/Grimhollow/Grimhollow.exe`.
- Existing native interface and polish scenarios each passed in landscape and portrait: **four exit=0**. **SHAMAN POSITION PASS** covers all three shaman variants plus Hexcaster retreat, teleport, knockback, occupancy, melee and examination. **JOURNAL UI PASS** includes a persistent book while carrying keys, separate unread badge, acknowledgement through real input and fitted journal pages. Talent input/rank limits remain **TEST 54 UI PASS ... failures=0**.
- Upper tall/furrowed grass is now in a dedicated layer before characters; the lower foreground remains after characters. No terrain rules or assets changed. Native talent-panel screenshots visually inspected; gold filled versus hollow ranks and available-point labels fit both layouts.
- Evidence: `verification/interface/{portrait,landscape}/enchanter-rank-cap.png` and `journal-notes.png`. Full local fixture output under `.local/sprint-component-1/`.
- Test 45 remains **Abandoned: test failed**, not run. Physical tablet confirmation remains a human playtest. Remote CI is checked after push with unchanged checks.

# In-game Playtest access - v1.20.2

| Gate | Result | Executed evidence |
|---|---|---|
| Windows / Android / unit tests | PASS | `gradlew.bat core:test desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 50s**; seven JUnit tests, zero failures/errors/skips. `tools/package-windows.ps1` produced `desktop/build/windows/1.20.2/Grimhollow/Grimhollow.exe`. |
| 52 home and in-game access | PASS | Existing native `--smoke-sewers` interface scenario, isolated landscape and portrait profiles: **exit=0** and **TEST 52 ... failures=0** in both. Actual pointer clicks verify no shortcut in an ordinary save, enable through the home entry, reopen via the run menu immediately, after saved-game reload and after floor travel. Menu bounds checked at 1280x720 and 720x1061. |
| Other checks in native interface scenario | PASS | Existing 36/51/53/54/55/56/58 interface checks and shaman-position/issue-report checks passed in both orientations. Outputs remain under `.local/playtest-1.20.2/`. No claim of a physical tablet test. |
| 45 terrain contrast | **Abandoned: test failed** | Not run. No artwork changed. |
| Full remote CI | Pending at commit | Triggered on push; retain the workflow unchanged. Earlier results below retain their original release provenance. |

# Deterministic hero export repair - v1.20.1

- The original branch run **36913560174** failed exact Linux reproduction of `hero_warrior.png`, `hero_enchanter.png` and their manifest hashes; the same commit passed that gate on Windows and another Linux runner. Its failure and `v1.20.0-hero-pilot` tag remain in place. No check is disabled or weakened.
- Replace CPU-dependent floating matrix solves with a 1/4096-pixel grid and exact integer determinants. Repack both heroes and the action previews from the same committed paintings; no new art or gameplay changes.
- Fresh local `desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 42s**. Fresh `tools/recovery_assets.py --check`: **145 assets, 137 source sheets, 55 launcher resources, failures=0; TEST 44 failures=0**. Packaged `desktop/build/windows/1.20.1/Grimhollow/Grimhollow.exe --smoke-title`: **exit=0** with an isolated settings directory.
- The gameplay/JUnit results below are the executed checks of this art-only pilot; the exporter repair does not change gameplay. Final-head CI is reported at delivery. Test 45 remains abandoned and was not run.
- Fresh native geometry: **heroes=9 mob sprites=126 steady idle checks=126 failures=0; TESTS 24-26, 34, 36 PASS**. All four final-build cloth/plate bridge-room captures passed and replace the original captures. The geometry fixture requires the repository working directory; an invocation from the isolated capture directory failed to find `item-semantics.json`, then passed when rerun from the repository.

# Warrior and Enchanter art pilot - v1.20.0

| Gate | Result | Executed evidence |
|---|---|---|
| Desktop / Android / JUnit / class smoke | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 50s**. JUnit: **7 tests, 0 failures, 0 errors, 0 skipped**. Combined class gate: **Runs=30 failures=0**. |
| 44 reproducible art | PASS | `python tools/recovery_assets.py --check`: **145 assets, 137 source sheets, 55 launcher resources, failures=0**; **TEST 44 ... verified painted replacements=145; failures=0**. Only the Warrior and Enchanter shipping atlases changed. |
| 24 geometry / animation coverage | PASS | Existing native GPU fixture: **heroes=9 mob sprites=126 steady idle checks=126 failures=0**, complete animation coverage; hero/rat height ratio **2.2325034**. All eight hero rows and existing pose indices remain valid. |
| Native rendering | PASS | Both heroes rendered in cloth and plate with lighting on/off, then captured in the existing generated bridge-room fixture (`-Dgrimhollow.iteration=true`, optional `-Dgrimhollow.heroArmorTier=5`) for unobscured review. Four lit captures visually inspected; full images and labelled crops are in `verification/heroes/pilot/native/` and `ingame-comparison.png`. |
| Other assertions in native geometry fixture | PASS | Tests **25, 26, 34, 36** passed: 386 items, 60 identification icons, 63 trap states, 113 skills, 15 plants, nine class paintings/descriptions and slot sizing. Test 35 remains retired. |
| Packaged Windows launch | PASS | `tools/package-windows.ps1` produced `desktop/build/windows/1.20.0/Grimhollow/Grimhollow.exe`; the actual launcher with bundled runtime and an isolated settings directory completed `--smoke-title` with **exit=0**. Android APK built; no physical tablet run is claimed. |
| 45 terrain contrast | **Abandoned: test failed** | Not run. No terrain art changes. |
| Aesthetic approval / physical tablet | Pending human review | This is the requested two-character proof of concept, not approval to replace the remaining seven heroes. Still idles and restrained baked poses preserve existing runtime timing; no new directional animation system. |
| Remote CI | Reported at delivery | Workflow and thresholds are unchanged. Prior test results below retain their own release provenance. |

# Painted Adventuring Notes - v1.19.2

| Gate | Result | Executed evidence |
|---|---|---|
| JUnit and class gates | PASS | `gradlew.bat core:test core:smokeRun --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 22s**, **Runs=30 failures=0**. |
| 64 treasury reminders | PASS | Existing fifteen-room/five-region scenario now also checks unseen-room exclusion, disguised-door exclusion, observed/mapped clue registration, revealed-door registration, duplicate prevention, save/load and floor persistence, and removal after opening. |
| Painted journal interface | PASS | Existing native polish fixture in portrait and landscape: **JOURNAL UI PASS: real HUD input, all 26 painted landmarks, chasm/water/treasury descriptions, notes/alchemy/catalog bounds and navigation icon dimensions**. Screenshots visually inspected; new evidence is `verification/interface/{portrait,landscape}/journal-note-*.png` and `journal-notes.png`. Other assertions in this fixture (37/61/63/64) also passed. |
| 44 asset provenance/reproduction | PASS | `python tools/recovery_assets.py --check`: **PAINTED assets=145 source sheets=135 failures=0**, 55 launcher resources; **TEST 44 ... verified painted replacements=145; failures=0**. |
| Windows and Android builds | PASS | `gradlew.bat desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 8s**. Windows package produced by `tools/package-windows.ps1`; Android version code 972. |
| 45 terrain contrast | **Abandoned: test failed** | Not run; remains excluded from local CLI and CI. No terrain art changed. |
| CI / physical tablet | Not claimed here | CI status is reported at delivery. Physical Samsung tablet review remains with the user. The prior branch run 36899034139 failed only Linux effects timing test 31 (p95 2.1246 ms versus the unchanged <2 ms limit); all fog checks in that run passed. |

# Current disposition - 2026-10-01

| Test | Status | Decision and preserved evidence |
|---|---|---|
| 45 terrain contrast | **Abandoned: test failed** | User requested that testing stop. Removed from Linux/Windows CI and the local CLI. Last recorded result: **17/76 failures** (Sewers 5/15, Prison 1/10, Caves 6/21, City 1/15, Halls 4/15). Historical measurements and implementation remain preserved. Not PASS; no further execution or release blocking. |

This disposition supersedes every older statement below requiring test 45 to run or remain enforced. Those dated results remain historical records. All other checks retain their existing status and execution, including fog/visibility tests 43/47 and art/handler tests 44/46. No contrast test was run for this change.

# Home-screen Playtest - v1.19.1

| Gate | Result | Executed evidence |
|---|---|---|
| Windows / Android / JUnit | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon`: **BUILD SUCCESSFUL in 2m 11s**; Android code 971. |
| Class gates | PASS | **Runs=30 failures=0**: Necromancer, Enchanter and Psychic each 10/0. |
| 52 home navigation / run tools | PASS | Actual portrait touch and landscape mouse: seven fitted title buttons, no active-run Playtest button, home menu with no hero loaded, shared balance input, safe empty slot selection, cancellation, saved-run loading with ordinary flags preserved, then God mode, item creation, class/progression and floor travel. **TEST 52 HOME PASS** and **TEST 52 UI ... failures=0** in both orientations. |
| 58 shared profile | PASS | Home edits persist without a hero or run mutation; bounds and nonempty item pools enforced; next game adopts customized profile. Existing seeded generation, saves, reset and sanitization checks pass. |
| Windows package | PASS | `tools/package-windows.ps1`; isolated `Grimhollow.exe --smoke-sewers` exits 0: **PASS: Sewer scene renders with dynamic lighting on and off.** |
| 45 and other historical gates | Prior status retained | Terrain contrast remains the known **17/76** failure. No art or fog changes; those checks were not rerun locally for this navigation patch. Physical tablet confirmation remains with the user. |
| CI | Reported at delivery | No check disabled or threshold changed. Full-green CI is not claimed while test 45 remains unresolved. |

Screenshots: [home screen](interface/landscape/playtest-home.png), [portrait menu](interface/portrait/playtest-home-menu.png), [home tuning](interface/portrait/playtest-home-balance.png), [save picker](interface/landscape/playtest-home-saves.png), [active-run menu](interface/portrait/playtest-run-menu.png).

# Enchanter craft and elemental treasuries - v1.19.0

All four authorized sprint components are implemented. The following results were executed locally against this release; earlier numbered results and retirements below retain their original scope. Physical Android playtesting is not claimed.

| Gate | Result | Executed evidence |
|---|---|---|
| Desktop / Android / JUnit | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon`: **BUILD SUCCESSFUL in 2m 29s**; seven JUnit tests, zero failures/errors. Android 1.19.0, code 970. Final desktop fixture synchronization rebuild: **BUILD SUCCESSFUL in 32s**. |
| Class gates | PASS | **Runs=30 failures=0**: Necromancer 10/0, Enchanter 10/0, Psychic 10/0. |
| 62 Spellguard | PASS | Actual magical damage at both ranks; physical damage unchanged; expiry, equipped armor, permanent glyph/rune exclusion, Magic Immunity and old/new talent save migration. |
| 63 Scribe | PASS | Current-run knowledge and recipe exclusions; atomic resource costs; exactly three turns; carried Brush talent; scroll recycling one/stack/exotic/stale ownership; parchment bag/save and seeded opportunities **386/100 runs**. Actual mouse/touch purchases pass in both orientations. |
| 64 elemental treasuries | PASS | Fifteen generated rooms across all five regions; independent ordinary secret slots, routes, all three types together with distinct clues, once-per-type save persistence, sealed/open save/load, search penalty, matching/wrong elements, full Waterskin cost, Skeleton Key **4/5/6**, finite depth-scaled loot. **16,948 rooms/10,000 seeds; 0/1/2/3 distribution [788,3241,4206,1765]**, mean **1.6948**. |
| 64 actual renderer interactions | PASS | Native landscape and portrait: concealed/revealed/open painted mechanisms, fitted descriptions, real Prismatic Light/Talisman/Mapping revelation without unlocking, actual Pour pointer targeting, Liquid Flame ignition and all four direct electrical items (Lightning Wand, Stone of Shock, Shocking Brew, Flashbang Bomb). |
| Journal / existing UI | PASS | Actual HUD/journal input, notes/alchemy/catalog bounds, sixteen 16-logical-pixel painted icons in both orientations. Existing polish tests 37/61 pass. Complete landscape interface run passes, including the repaired nearest-undiscovered-heap Hatchling fixture, balance menu and Hurl. |
| 43 / 47 fog, torch and walking | PASS | Five fresh regional renderer runs, **788 walking steps / 1,373 door-transition frames**, three zooms and four pans before/after walking. Every region: **fogTexel/cell=1:1 worldUnits=16 lightQuad=aligned failures=0**, including hidden/back-facing torches and draw-time FOV changes. Checks ran from an immutable JAR snapshot to avoid rebuilding a file in use. |
| 44 art reproduction | PASS | `python -X utf8 tools/recovery_assets.py --check`: **upstream-derived character sheets=7; restored assets=25; verified painted replacements=144; failures=0**. Offline packer: **source sheets=134**, launcher resources=55. |
| 46 compiled handler audit | PASS | `python -X utf8 tools/recovery_checks.py --jar ...`: **classes=2971 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. |
| Windows launcher | PASS | `tools/package-windows.ps1` produced `desktop/build/windows/1.19.0/Grimhollow/Grimhollow.exe`; isolated-save `--smoke-sewers` launch: **PASS: Sewer scene renders with dynamic lighting on and off.** Keep the whole application directory. |
| 45 terrain contrast | Permanent known issue - NOT PASS | `python -X utf8 tools/recovery_checks.py --all-regions` returned exit 1: **17/76 failed** (Sewers 5/15, Prison 1/10, Caves 6/21, City 1/15, Halls 4/15). Original thresholds remain enforced. |
| Release CI | Not claimed green | Exact release-head workflow status is reported in the delivery. The known terrain-contrast failure remains enabled; no workflow step was disabled or weakened. |
| Other tests / retirements | Prior status retained | Historical results below are not presented as newly executed. Physical tablet performance remains resolved per user playtesting; campaign balance remains for human review. |

Native evidence: [Scribe portrait](interface/portrait/enchanter-scribe.png), [journal landscape](interface/landscape/journal-catalog.png), [concealed mechanisms](interface/landscape/elemental-seals-concealed.png), [fountain description](interface/portrait/elemental-fountain-description.png), [activated seals](interface/landscape/elemental-seals-open.png). Fresh fog and contrast measurements remain under `verification/recovery/`.

# Torch visibility and armor Rune Etching - v1.18.3

This patch fixes hidden wall-facing flames/glows, corrects the Rune Etching format string, and allows the single rune to move between equipped melee weapons and armor. Armor uses the existing common-glyph pool and preserves permanent/temporary effects. No new art or terrain thresholds were introduced. Historical numbered results and retirements below remain in force.

| Gate | Result | Executed evidence |
|---|---|---|
| Desktop / Android / JUnit | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 35s**. Seven JUnit tests, zero failures/errors. Android version 1.18.3, code 969. After final player-text/desktop-diagnostic edits, `desktop:dist android:assembleDebug` also passed in 38s; core behavior was unchanged. |
| Class gates | PASS | **Runs=30 failures=0**: Necromancer 10/0, Enchanter 10/0, Psychic 10/0. |
| 37 / 54 armor Etching | PASS | All weapon enchantment/common-glyph descriptions resolve names, upgrade levels and literal percent signs. Actual transfers conserve one rune/upgrade even with Curse Infusion, do not reroll, reject invalid targets, preserve passive and temporary/permanent glyphs, suppress magic when immune, survive save migration and crown conversion, and recover on carrier loss. Permanent/etched proc rates 1.25x; temporary 2x. |
| 37 native picker / popup | PASS | Mouse selection through the landscape inventory and touch selection through the portrait bag attach the rune to armor. Both layouts reject the current weapon carrier; rendered glyph/upgrade/percent text resolves and fits. Final title shortened after visual review; screenshots inspected. Existing polish test 61 also passes in both orientations. |
| 43 / 47 torch faces, fog and walking | PASS | Five regions, **788 walking steps / 1379 door-transition frames**, three zooms and four pans before/after walking. Every region passed hidden/back-facing, remembered, mapped, exposed, obstructed and doorway torch cases in painted/legacy x dynamic/halo modes, plus a draw-time FOV change and a hidden-source light-map comparison. **fogTexel/cell=1:1 worldUnits=16 lightQuad=aligned**, failures=0. |
| 46 compiled handler audit | PASS | `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.18.3.jar`: classes=2960 guarded browser sinks=1 HTTP/socket calls=0 failures=0. |
| Windows launcher | Built | `tools/package-windows.ps1`: `desktop/build/windows/1.18.3/Grimhollow/Grimhollow.exe`. Keep its whole directory. |
| 45 terrain contrast | Permanent known issue - NOT PASS | `python tools/recovery_checks.py --all-regions` returned exit 1: **17/76 failed** (Sewers 5/15, Prison 1/10, Caves 6/21, City 1/15, Halls 4/15). Original 0.12 luminance / 40-degree hue thresholds and all pairs remain enforced. No art was changed to chase this result. |
| 44 art reproduction | Prior status retained | No source art, assets or packer changed; not rerun locally for this code patch. CI retains the check. |
| Full CI | Not claimed green | Existing contrast and stale Hatchling full-interface fixture failures remain enforced. Release-head CI is linked in the delivery; no full-green claim. Prior run 36858153350 confirmed Android and all class gates green, with the existing contrast/interface failures on desktop. |
| Other tests and retirements | Prior status retained | No new physical-tablet or campaign-balance verification is claimed. Spellguard remains queued, Psychic Pull skipped, tablet movement resolved per user playtesting. |

Native Etch screenshots: [portrait picker](interface/portrait/polish-armor-etch-picker.png), [portrait description](interface/portrait/polish-armor-etch-description.png), [landscape picker](interface/landscape/polish-armor-etch-picker.png), [landscape description](interface/landscape/polish-armor-etch-description.png). Fresh torch/fog/walking evidence is in the existing five [regional folders](recovery/).

# Terrain readability - v1.18.2

This is a visual correction for human review, not a declaration that terrain readability or test 45 passes. The existing source paintings, fog rules and gameplay are retained. Tablet movement performance is resolved per the user's playtesting (2026-09-30); no physical-device benchmark was performed here. Campaign balance stays under user review. Spellguard is queued, and Psychic Pull is skipped.

| Gate | Result | Executed evidence |
|---|---|---|
| Desktop / Android / JUnit | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 2s**. Seven JUnit tests, zero failures/errors. Android code 968, version 1.18.2. |
| Class gates | PASS | **Runs=30 failures=0**: ten seeded runs each for Necromancer, Enchanter and Psychic, including existing content/artifact/quest regressions. No balance values changed. |
| 43 / 47 fog, walking and doors | PASS | Fresh native recovery/fog checks for all five regions: **NATIVE_REGION_FAILURES=0**. 788 adjacent walking steps, 1252 door-transition frames, three zooms and four pans before/after walking in every region. **fogTexel/cell=1:1 worldUnits=16 lightQuad=aligned**. The host paused during Caves; the same process resumed and completed without a test reset. |
| 24-26 / 34 / 36 / 56 geometry | PASS | Nine heroes, 126 mob forms, 75 creature atlases, 385 item frames, 60 identification overlays, 63 trap variants, 113 skill icons and 15 plants; native checks report failures=0. |
| 44 source reproduction | PASS | `python tools/recovery_assets.py --check`: **PAINTED assets=140 source sheets=130 failures=0**, 55 launcher resources, **verified painted replacements=140 failures=0**. No generated source art or Blender run. |
| 46 runtime handlers | PASS | Actual title controls=6, credits handlers=4, allowed repository URLs=4, external opens=0, scene fetches=0, failures=0. Compiled call-site audit retains its earlier result and was not rerun for this visual patch. |
| Windows launcher | Built | `tools/package-windows.ps1` completed and produced `desktop/build/windows/1.18.2/Grimhollow/Grimhollow.exe`; gameplay was tested through the same packaged desktop jar. |
| 45 terrain contrast | Permanent known issue - NOT PASS | `python tools/recovery_checks.py --all-regions` returned exit 1: **17/76 failed**. Sewers **5/15**, Prison **1/10**, Caves **6/21**, City **1/15**, Halls **4/15**. Original luminance 0.12 / hue 40-degree thresholds and all terrain pairs remain enforced. The current Sewers fixture has six visible types rather than the prior seven; do not treat the older 25/82 aggregate as a matched comparison. |
| Full CI | Not green | Test 45 remains a blocker. The separately identified stale Hatchling full-interface fixture from v1.18.1 remains outstanding; this patch does not modify its assertion or gameplay. Exact release-head CI is linked in the delivery. |
| Other numbered tests / retirements | Prior status retained | This patch adds no acceptance test and claims no new campaign-balance or physical-tablet verification. Earlier numbered records below keep their stated scope and retirements. |

Fresh five-region lit captures, room measurements and walking sequences are in [recovery](recovery/). The restrained ambient/personal-light color preserves the existing painted materials, while torches/fire retain localized warmth. Flattened grass uses its existing painting at a taller low-profile footprint. Door/grass/decor averages and dark cave surfaces still have failing comparisons; human readability review remains required.

# Lurking Horror — v1.17.0

The three implementation components are complete. This is automated encounter coverage, not a claim of a balanced full campaign or physical Samsung tablet testing. Historical numbered results and retirements below remain in force; test 45 is still enforced.

| Gate | Result | Executed evidence |
|---|---|---|
| Final mechanics / desktop / Android | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon --console=plain`: **Runs=90 failures=0; BUILD SUCCESSFUL in 2m 46s**. Seven JUnit tests, no failures/errors. APK version code 964, version 1.17.0-INDEV. |
| Class gates | PASS | Ten seeded runs each for all nine heroes. Necromancer 10/0, Enchanter 10/0, Psychic 10/0, combined new classes 30/0 are included in 90/0. The earlier dedicated Necromancer component run also finished at 10/0. |
| 60 behavior / counterplay | PASS | Real AI acts: warning interrupts movement/rest and waits for readiness plus a paid response; free actions do not count. Attack follows current position; moving away/invisibility/reveal cancel it. Another sleeping hostile cancels it. City follow-up requires a second response. Ashlight +5/+6, intentional search, prismatic exposure, Mind Vision/Scry entity-only FOV, unchanged collision occupancy, Halls barricade-only phasing and controlled-kill attribution pass. |
| 60 persistence / predation / generation | PASS | All five region stats, lifetime 25% recovery, second-cycle cap, phase/allowance bundle and disk reload. One real prey attack; surviving prey wakes/retaliates; deaths grant no hero XP, expose species-specific remains only after exploration, and log one distant event. 1035/2000 regional rolls; 22 real generated placements across all five regions in the generation fixture; empty-room, one-per-region, branch and floor-one exclusions pass. |
| 60 native input / presentation | PASS | Existing `--smoke-sewers` runner, `grimhollow.interfaceReview=true`, `grimhollow.horrorReview=true`, `grimhollow.heroClass=WARRIOR`, portrait false/true: **TEST 60 NATIVE PASS ... failures=0** in both orientations. Real pointer travel stops before damage; fresh movement evades. Unknown-cell entity inspection reveals no terrain; popup bounds, remains/loot chooser and paged tuning menu pass. Screenshots use an isolated room; headless generation checks use real generated floors. |
| 43 / 47 fog and remembered terrain | PASS | Fresh native `grimhollow.recovery=true`, `grimhollow.fogTests=true` runs for regions 0–4: **failures=0**. 788 movement steps (100/202/200/172/114), 1384 door-transition frames, three zooms × four pans before/after walking in each region. Every-cell black/visible sampling, **fogTexel/cell=1:1 worldUnits=16 lightQuad=aligned**. |
| 24–26 / 34 / 56 geometry | PASS | Native renderer: nine heroes, 126 creature forms, 75 eightfold-density atlases, 385 item frames and 60 identification overlays; failures=0. |
| 31 / 32 / 56 effects | PASS | 40 gas + 10 fire cells, 240 GPU-completed frames: **mean=0.6454ms p95=0.8982ms failures=0**. Batched fire channel difference=0; painted particle families/rays and scorch placement pass. |
| 44 source reconstruction | PASS | `python tools/recovery_assets.py --check`: **PAINTED assets=137 source sheets=127 failures=0**; 55 launcher resources; **verified painted replacements=137 failures=0**. No generation service or Blender is required. |
| 46 packaged code | PASS | `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.17.0.jar`: **classes=2955 guarded browser sinks=1 HTTP/socket calls=0 failures=0**. Native title/credits handlers also pass. |
| Windows app | PASS | `tools/package-windows.ps1`; packaged `Grimhollow.exe --smoke-title` in a separate profile: **LAUNCHER_EXIT=0; RENDERED=TitleScene**. |
| 45 contrast | Permanent known issue | `python tools/recovery_checks.py --all-regions` on fresh Windows captures: **25/82 failed** (Sewers 8/21, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15). The fresh Sewers fixture includes a seventh terrain type, a trap; source art and thresholds are unchanged. |
| Full CI | Known contrast blocker | Full workflow remains subject to test 45. Exact release-head CI status and tag are reported at delivery; no check was disabled or weakened. |
| Other numbered subjects | Prior status retained | Existing headless/JUnit scenarios were rerun in the 90/0 gate. Prior render-loop retirements, campaign limitations and physical-device gaps remain as recorded below. |

Painted source and exact prompt: `tools/painted/sources/lurking-horror.png` and `tools/painted/lurking-horror-prompt.json`. Native evidence: `interface/{landscape,portrait}/horror-*.png`. The native warning fixture equips a cursed Talisman to prevent random passive detection from pre-empting the warning; deliberate detection has separate tests. Initial fixture failures (border-cell setup, headless FOV initialization and the localized rat label) were corrected before these passing runs.

# Playtest follow-up — v1.16.1

The numbered historical records below retain their stated limits and retirements. This patch changes Hatchling identification, shared tuning, expedition cavern generation/stair art, and the two Regrowth-only plant sprites. No physical Android tablet or full combat campaign was tested.

| Check | Result | Actual evidence |
|---|---|---|
| 55 / 58 / 59 mechanics | PASS | `gradlew.bat core:test core:smokeRun desktop:dist -PsmokeUpstream=true --no-daemon --console=plain`: **Runs=90 failures=0; BUILD SUCCESSFUL in 1m 16s**. All nine classes, including each added class at 10/0. |
| 55 identification | PASS | `TEST 55 IDENTIFY PASS`: bagged consumables/wands, equipped gear/artifacts and nested bags. Eating, upgrades and enchantments remain loose-only. |
| 58 shared settings | PASS | Disk-backed preference reload; current, old and new games use shared values; saved older values cannot undo a reset; malformed values clamp; legacy save migration; default RNG unchanged. |
| 59 cavern | PASS | Per class: 32 connected natural outlines, 640 safe fall samples, distant broodmother and nearer scavengers. Default 48 heaps: 24 bone/24 adventurer remains, 33 common +0 gear, two rings, three rations, four torches. Loot-only controls, tier/upgrade bounds and finite supplies verified. |
| 59 native expedition | PASS | Landscape and portrait: real NPC exchange/map/continue/fall/blocked climb/boss clear/victory/return input. Both upward exits keep their destinations. New cavern overview and loot-menu captures are under `interface/{landscape,portrait}/expedition-*.png`. Overview deliberately uses debug mapping; normal darkness has a separate capture. |
| 25 / 33 / 36 plants and interface | PASS | `BOTANY UI: sprouted plants=15 ... failures=0` in both orientations. Existing geometry checks confirm 15 correct plant indices/UVs and footprints, 113 skills, 385 item frames and 60 identification overlays. New plants visible in `painted-sprouted-plants.png`. |
| 24 / 26 / 34 / 56 | PASS | Native geometry, popup and handler checks; nine heroes, 125 creature forms, 74 eightfold-density atlases; failures=0. |
| 31 / 32 / 56 effects | PASS | 40 gas + 10 fire cells, 240 GPU-completed frames: **mean=0.9376ms p95=1.3068ms failures=0**. Batched flame max channel difference=0. |
| 44 artwork reconstruction | PASS | `python tools/recovery_assets.py --check`: **PAINTED assets=136 source sheets=126 failures=0; verified painted replacements=136 failures=0**. 55 launcher resources. |
| Desktop / Android build | PASS | `gradlew.bat core:test desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 43s**. Seven JUnit cases, zero failures/errors. Version 1.16.1-INDEV, Android code 963. |
| Windows package | PASS | `tools/package-windows.ps1` produced the bundled-runtime executable and portable ZIP; packaged `Grimhollow.exe --smoke-title` returned **LAUNCHER_EXIT=0; RENDERED=TitleScene**. |
| CI / test 45 | PENDING / existing known issue | Exact release commit CI status is reported in the delivery message. Test 45 remains enforced; its prior Windows result failed 22/76 comparisons. Other prior known issues have not been reclassified by this patch. |

# Dragon expedition release — v1.16.0 (all eight components)

The expedition is now enabled. The sections below this release record are historical checkpoints; their then-incomplete component lists do not describe this release. No physical Samsung tablet or full player-driven combat campaign was tested.

| Gate | Result | Actual command output / scope |
|---|---|---|
| Final mechanics, including 59 | PASS | `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon --console=plain`: **Runs=90 failures=0; BUILD SUCCESSFUL in 1m 40s**. Includes the real hunter exchange/entry, all eight exit locations across 64 maze seeds per hero, falls, brood limits, persistent dragon, hoard, two active trinkets, tuning and direct Playtest travel. |
| Class-only coverage | PASS | Necromancer 10/0, Enchanter 10/0, Psychic 10/0 and combined 30/0 are included in the nine-class 90/0 run. Exact release-head CI additionally executes each separate class job. |
| Final packaging and JUnit | PASS | After the shared density metadata fix: `core:test desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 47s** (the final spell-text packaging gate subsequently passes in **1m 22s**, **Runs=30 failures=0**). Seven JUnit tests, zero failures/errors. APK `com.grimhollow.dungeon`, code 962, version 1.16.0-INDEV, SDK 21/36. |
| 59 native quest flow | PASS | Existing `--smoke-sewers` runner with `grimhollow.interfaceReview=true` and `grimhollow.expeditionReview=true`: **TEST 59 NATIVE PASS** in landscape and portrait. Actual button input: exchange one healing potion, map warning/entry, disk continue, fall/Cripple, blocked climb, broodmother death/climb, scheduled dragon-victory transport, and pointer exit to the original City cell. Boss deaths are controlled fixtures, not a claim of a balanced combat campaign. |
| 59 native presentation/tuning | PASS | 1280×720 landscape and 720×1061 portrait framebuffer captures; popup bounds, painted creatures/platforms/hoard, normal cavern darkness, all three tuning sections and actual numeric edit. Victory arrives beside visible treasure; both bosses show the shared painted health bar after being damaged. |
| 24–26 / 34 / 36 / 56 | PASS | Native geometry run: 9 heroes, 125 existing mob forms, 74 creature atlases with eightfold density, 385 item frames, 60 identification overlays, 113 skill icons and 13 plants; failures=0. New expedition sprites also have their own test-59 presentation checks. |
| 31 / 32 / 56 effects | PASS | Native GPU-completed benchmark: 40 gas + 10 fire cells, 240 frames, **mean=0.4836ms p95=0.6858ms failures=0**; batch channel difference=0. Soft-alpha particle/ray and scorch checks pass. |
| 44 provenance / reconstruction | PASS | `python tools/recovery_assets.py --check`: **PAINTED assets=136 source sheets=124 failures=0**; 55 launcher resources; **verified painted replacements=136 failures=0**. Metadata repair changed no image pixels. |
| Native Windows launcher | PASS | `tools/package-windows.ps1`; bundled `Grimhollow.exe --smoke-title` in an isolated profile: **LAUNCHER_EXIT=0; RENDERED=TitleScene**. Portable ZIP also produced. |
| Physical Android device | NOT RUN | `adb devices -l` returned an empty list. Desktop portrait input is not a hardware performance test. |
| Full CI / 21 / 45 | Permanent known issue | Test 45 remains enforced with unchanged contrast thresholds; preceding CI is 22/76 failures. Exact release-head CI outcome is recorded in the annotated release tag and delivery. No failing check is disabled. |
| Other prior numbered subjects | Prior status retained | Existing headless/JUnit scenarios were rerun; the numbered 1–58 records below retain their stated scope, retirements and permanent known issues. Full campaign, exhaustive talent combat and reference licensing gaps are not claimed resolved. |

Component-7 CI exposed three missing density declarations (test 56) for the original dragon, broodmother and hunter. This release adds their logical layout metadata and uses the common character frame/filter path; the same check passes locally, with unchanged artwork and thresholds. Final review also repaired the missing boss bar for pre-alerted bosses and added native assertions for both. A native capture also found victory treasure offscreen; arrival was moved beside the hoard and rechecked in both orientations. Intermediate attempts remain in ignored `.local/expedition-*` diagnostics.

Screenshots: [dragon warning](interface/landscape/expedition-breath-warning.png), [cavern](interface/portrait/expedition-cavern-darkness.png), [actual hunter exchange](interface/portrait/expedition-city-offer.png), [entry warning](interface/portrait/expedition-entry-warning.png), [victory beside the hoard](interface/landscape/expedition-victory-arrival.png), [safe return](interface/portrait/expedition-safe-return.png). Sources/prompts are in `tools/painted/sources/expedition/` and `tools/painted/expedition-prompts.json`; the packer needs no image-generation service at build time.

| Component | Pushed checkpoint |
|---|---|
| 1 Foundation | v1.16.0-expedition-foundation |
| 2 Upper maze | v1.16.0-expedition-maze |
| 3 Cavern / broodmother | v1.16.0-expedition-cavern |
| 4 Persistent dragon | v1.16.0-expedition-dragon |
| 5 Hoard / retreat | v1.16.0-expedition-hoard |
| 6 Original painted presentation | v1.16.0-expedition-visuals |
| 7 Save-local tuning | v1.16.0-expedition-tuning |
| 8 Integration / release | v1.16.0-dragon-expedition |

# Dragon expedition — component 7 tuning checkpoint

`core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeClass=PSYCHIC --no-daemon --console=plain`: **BUILD SUCCESSFUL in 1m 25s; Runs=10 failures=0**; seven existing JUnit cases pass. Test 59 additionally verifies isolated offer RNG, boss HP/damage/cooldown, zero initial spinners/live brood, configured sight and guaranteed supplies, zero gold/artifact rewards, one upgraded equipment item and guaranteed non-duplicate trinket, disk persistence and reset. Existing baseline expedition assertions remain unchanged and pass.

Native portrait and landscape `expeditionReview` passes real pointer navigation through all three sections, paged controls and numeric input (480 → 720 HP), with saved screenshots `interface/*/expedition-tuning-*.png`. No art changed or art loop ran in component 7. Final campaign activation, native complete quest flow and release packaging remain component 8; existing contrast test 45 stays enforced.

# Dragon expedition — component 6 visual checkpoint

`core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeClass=NECROMANCER --no-daemon --console=plain`: **BUILD SUCCESSFUL in 2m 17s; Runs=10 failures=0**. The existing seven JUnit cases passed. Native `--smoke-sewers` with `grimhollow.interfaceReview=true` and `grimhollow.expeditionReview=true` passes at 1280×720 landscape and 720×1061 portrait, with isolated profiles. Test 59 checks new creature dimensions, message availability, sealed-hoard protection and popup bounds. Evidence: `interface/landscape/expedition-*.png` and `interface/portrait/expedition-*.png`.

`python tools/recovery_assets.py --check`: **PAINTED assets=136 source sheets=124 failures=0; TEST 44 verified painted replacements=136 failures=0**, plus 55 launch resources. Inspection covered corrected dragon/hunter text, broodmother silhouette, normal cavern darkness, timber/drop separation, continuous sealed hoard and map. No physical tablet or full player-led expedition is claimed. Campaign entry remains disabled pending components 7–8; test 45 is unchanged.

Intermediate repairs: a native fixture used `size()` instead of the engine collection's `size` field; an obsolete archived JAR was initially selected, then replaced with the explicit current JAR. Native inspection revealed incorrectly prefixed expedition messages, corrected before the passing evidence. New characters are accounted for by their verified painted-source metadata.

# Dragon expedition — components 1–5 development checkpoint

Campaign entry is disabled pending components 6 (bespoke visuals/presentation), 7 (balance controls), and 8 (native release verification). No new art was generated in these components. Version remains the existing 1.15.0 until release integration.

Local final command: `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon --console=plain` — **BUILD SUCCESSFUL in 2m 44s; Runs=90 failures=0**. All nine hero classes ran ten seeds. Existing seven JUnit cases passed. New test 59 runs for every class alongside existing scenarios, including 64 maze seeds per class.

| Gate | Status | Actual scope |
|---|---|---|
| 59 foundation | PASS | Isolated seeded City placement; exactly one healing potion; no repeat exchange; map/elixir; actual disk persistence; missing-state defaults. |
| 59 maze | PASS | All eight perimeter positions observed across 64 seeds; four-way connectivity of every platform; central arrival; loops and dead ends; nonflammable platform cells; actual saved layout retained. |
| 59 cavern | PASS | Open cavern and 2×2 pillars; forty finite bone heaps including three rations/four torches; six initial scavengers; live/lifetime brood caps; no hatchling loot/XP; fall damage/Cripple, Feather Fall, actual descent/climb and safe cleared reentry. |
| 59 dragon | PASS | Fixed warned cone, occlusion/range, no windup damage, three-turn breath minimum, two-cell wingbeat, healing rejection, single boss and wounded/cooldown persistence through disk save and cavern round trip. |
| 59 hoard | PASS | Wounded dragon relocates; retreat works while alive; original City return cell; victory schedules transport; travelling minion retained; no treasure before victory; reward quality; collected treasure never regenerates; two carried trinket passives and non-duplicate bonus selection. |
| New-class gates | PASS | Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined 30/0 included in all-nine 90/0. |
| Desktop / Android builds | PASS | `desktop:dist` and `android:assembleDebug`, final command above. No new native launcher package is advertised. |
| Existing tests 1–58 | Checkpoint scope retained | Existing headless/JUnit scenarios rerun; no fresh local rendered-art evidence, physical tablet tests or full campaign. Retired tests and permanent known issues below remain unchanged. |
| Full CI / test 45 | Permanent known issue | Existing contrast gate remains enforced. Exact-head workflow status is reported with the checkpoint delivery. |
| Components 6–8 | NOT RUN | No bespoke expedition art, tuning menu entries, native expedition screenshots or player-ready release yet. |

Intermediate repairs: generation-time transition lookup now uses the level being built rather than `Dungeon.level`; the headless Feather Fall fixture supplies its normal visual-only emitter. Tests retain production fall, damage, floor transition and persistence paths.

# Balance tuning - v1.15.0

New test **58 PASS** in the existing headless and native interface runners. Build command: `gradlew.bat core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon --console=plain` -> **BUILD SUCCESSFUL in 1m 1s; Runs=90 failures=0**. Seven JUnit tests, zero failures/errors. All nine heroes cover ten seeds; Necromancer 10/0, Enchanter 10/0, Psychic 10/0, combined added classes 30/0 are included. `tools/package-windows.ps1` creates the 1.15.0 native Windows application with the existing icon and bundled runtime.

Test 58 covers default seeded RNG equivalence, chance/multiplier boundaries, real enemy/gear/category generators, artifact uniqueness and exhaustion fallback, generation in five regions at zero population/optional floor loot while retaining supplies, high population, disk save/load, invalid saved settings, reset and new-run isolation. The existing OpenGL interface run passes in 1280x720 landscape with mouse input and 720x1061 portrait with touch input: menu traversal, paged category controls, numeric changes, saved values, reset and bounds. No physical Android tablet test is claimed.

[Portrait menu](interface/portrait/balance-tuning.png), [numeric control](interface/portrait/balance-tuning-value.png), [landscape enemy controls](interface/landscape/balance-tuning-enemies.png). The remaining numbered acceptance record below retains its checkpoint provenance; this settings patch changes no art or rendering thresholds. Contrast test 45 remains an enforced known failure. The initial new test compilation used a method rather than the engine collection's size field and was corrected before the passing run.

# Tablet quest rooms and terrain presentation - v1.14.2

Current delivery: **v1.14.2-tablet-rooms**. This section records fresh results; the complete numbered 1-56 checkpoint table below remains historical where not explicitly rerun here. No physical tablet was connected (`adb devices -l`: empty).

| Check | Result | Actual output / scope |
|---|---|---|
| Desktop/Android, 1/2/22 | PASS | `core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon --console=plain`: BUILD SUCCESSFUL in 1m 49s. Final packaging after text/fixture repairs: BUILD SUCCESSFUL in 45s. APK com.grimhollow.dungeon, code 960, 1.14.2-INDEV, SDK 21/36. Windows bundled-runtime launcher packaged. |
| Unit tests | PASS | Seven tests, zero failures/errors: RecoveryTest 2; LightMapTest 5. |
| Class gate, 5/15 | PASS | `Runs=90 failures=0`; Necromancer 10/0, Enchanter 10/0, Psychic 10/0, combined added classes 30/0 included in all nine classes. Existing class/content/Hatchling/defensive-sigil assertions ran. |
| Provenance/rebuild, 44 | PASS | PAINTED assets=130 source sheets=118 failures=0; launcher resources=55. TEST 44: upstream-derived character sheets=7; restored assets=25; verified painted replacements=130; failures=0. |
| 57: tablet rooms and quest | PASS | Native portrait and landscape: four real candle throws, only the fourth summons one elemental, embers drop, Wandmaker reward dialog and selected-wand completion. Generated smithy and both mining branches, 16-unit tile preview vertices, all 16 knowledge-dependent rail masks, no ore torch sprites/sources, removed source kills flame, painted depth/boss HUD. |
| 57: separate sight patch | PASS | Remote raised ally's cell enters hero FOV with Corpse Sense and leaves FOV when the ally is removed. The player's exact save is unavailable. |
| Fog/remembered terrain, 43/47 | PASS | Fresh Caves run: 200 steps, 85 turns, 23 doors; 12 cameras before and after walking (three zooms/four pans), all 2,067 cells, 422 door-transition frames, fog texel/cell=1:1, worldUnits=16, lightQuad=aligned, failures=0. Other regions retain checkpoint evidence locally and are rerun in CI. |
| Existing portrait interface suite | PASS | Real pointer/UI interactions, scrolling text, cast menus, Defensive Sigil, raised-undead lifetime, all three displaced-Shaman variants, Hatchling warning/next-action consumption, eight contained HUD effects and region transitions pass with exit 0. |
| Native Windows launcher | PASS | Packaged Grimhollow.exe launched the title smoke in an isolated user profile and exited 0. |
| CI / 21 / 45 | permanent known issue | Full workflow remains mandatory. Test 45 has an existing contrast failure; exact release-head CI is recorded in the annotated release tag and delivery. No checks were weakened. |

Test 45 remeasurement remains **22/76 failures**: Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15. Caves captures are fresh from this build; the other four regional captures retain their checkpoint provenance. The 0.12 luminance / 40-degree hue thresholds are unchanged.

Evidence: [ritual before summoning](interface/portrait/ritual-room.png), [readable circle instructions](interface/portrait/ritual-circle-popup.png), [summoned elemental](interface/portrait/ritual-elemental-summoned.png), [wand reward](interface/portrait/wandmaker-reward.png), [contained cage preview](interface/portrait/cage-popup.png), [smithy](interface/portrait/smithy-room.png), [painted mine and ore](interface/portrait/gnoll-mine-ore.png), [after mining](interface/portrait/gnoll-mine-after-mining.png), [rail preview](interface/portrait/rail-popup.png), [boss HUD](interface/landscape/painted-boss-bar.png). Corresponding portrait/landscape captures use the actual renderer; fixture exploration state is deliberately revealed for room inspection. Native fixture generation initially ran with an outgoing scene still registered and failed during a map callback; clearing that diagnostic scene as in InterlevelScene fixed the fixture, without changing production level generation.

# Feeding, HUD and creature-position fixes - v1.14.1

Release: **v1.14.1-playtest-fixes**. Runtime implementation: `c083db422`; the annotated release tag identifies the final source/evidence commit and exact-head CI result. No art generation or gameplay balance changes were made.

Fresh local results: **Runs=90 failures=0**, seven JUnit tests, Windows desktop/native launcher and Android packaging pass. Necromancer **10/0**, Enchanter **10/0**, Psychic **10/0**, combined new classes **30/0** are included in the all-nine gate. Native portrait and landscape suites and the geometry/effects suite pass. Physical Samsung tablet testing and the player's exact floor-11 cursed-wand encounter remain unavailable.

[Warning pauses movement and wraps eight effects](interface/portrait/hatchling-warning-and-hud.png), [readable meal text and painted HUD](interface/portrait/hatchling-meal-name.png), [landscape evidence](interface/landscape/hatchling-warning-and-hud.png). These captures also show the NPC bone-summoning particles already supplied by v1.14.0 when Enhanced Effects is enabled; the user screenshot was v1.13.0.

The native shaman regression first failed on stale animation overwriting a teleported Red Shaman's sprite position, then passed for all three variants after the shared tween cancellation fix. Fixture corrections restored a real Feeding actor after test class changes and ensured the test backpack had room; production Playtest behavior was not changed. Intermediate failures remain in ignored `.local/hatchling-fix-*` diagnostics. Tests 20, 38, 43-45 and 47 explicitly retain prior evidence locally; CI repeats the rendering/provenance gates. Test 45 remains an enforced known failure, not a passing result.

| Test | Status | Actual evidence and scope |
|---|---|---|
| 1 | PASS | Windows 1.14.1 desktop and bundled-runtime Grimhollow.exe launch (TitleScene exit=0). Native portrait touch and landscape mouse suites pass; screenshots include the actual feeding warning, next-action meal, painted resume/skull and eight contained status icons. |
| 2 | PASS | core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon: BUILD SUCCESSFUL in 2m 47s; Runs=90 failures=0; seven JUnit tests with zero failures/errors. Subsequent fixture-only desktop rebuild and native packaging also pass. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0: nine hero classes, ten seeds each, including starter kits and added-class scenarios. |
| 6 | permanent known issue | Representative hooks and native purchasing pass; Defensive Sigil ranks/cost/refresh/expiry/save compatibility are covered by test 56. Exhaustive every-talent combat coverage remains unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Existing Grasp heaps/traps/range tests pass; Hatchling scenario additionally checks bones at Crystal 3, unlocked chests at 7, and locked-container rejection. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | All nine classes save/load through floor 6. Crystal swap/re-equip retains level/charges; Precognition expenditure survives serialization. |
| 15 | PASS | Runs=90 failures=0. Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined new classes 30/0 as part of the all-nine gate. |
| 16 | permanent known issue | Fresh geometry covers 122 creature sprites and all 71 density-8 atlases; no dimension exceeds 4096. Red/Blue/Purple Shaman overlapping movement, teleport and knockback cases additionally pass. Exhaustive gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; not rerun locally. CI independently builds with desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced. Exact-head CI status and failing steps are recorded in the annotated v1.14.1-playtest-fixes tag and delivery; no checks disabled or weakened. |
| 22 | PASS | aapt: com.grimhollow.dungeon, versionCode=959, versionName=1.14.1-INDEV, label Grimhollow, minimum SDK 21, target 36. adb devices -l returned no devices; APK packaging is not a physical tablet playtest. |
| 23 | PASS | Native checks and packaged launcher use isolated .local/hatchling-fix-* profiles; headless saves use diagnostic slot 99. Existing player saves were not used. |
| 24 | permanent known issue | Fresh standard geometry: heroes=9, mob sprites=122, steady idle/travel checks=122, failures=0. Bilinear fringe sizing corrected two newly exposed short-creature failures without changing the acceptance band. Historical six 2x occupancy exceptions have not been remeasured locally. |
| 25 | PASS | TEST 25: items=384 identification icons=60 failures=0; named semantics and per-index hashes pass. New Hatchling occupies index 539. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 idle turns leave charges unchanged; 12 charges unlock Wraith at level 1; hostile attacked within 2 turns. Ghoul remains locked through +4, unlocks at +5 and heals 15% of health actually removed, capped against overkill. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Fresh: 240 GPU-completed frames, 40 gas + 10 fire cells, mean=0.6702ms p95=0.9588ms; effects-off pixel differences=0; batched-fire maximum channel difference=0. Unchanged 2ms gate. |
| 32 | PASS | TEST 32: three scorch sizes, actual floor fire expiration, water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 RANGED PASS: actual carried thrown-weapon and Spirit Bow inscriptions, thrown stack split/merge, arrow proc dispatch, ownership and melee-only filtering, unchanged duration. Trade knowledge, armor inscriptions and persistent-library checks also pass. Both orientations additionally pass actual equipment-picker armor selection, scrolled glyph selection, touch weapon/info, drag suppression and zero-charge rejection. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Native landscape/portrait suites exercise inventory, scroll bounds, all-rank descriptions, pointer purchases, Hurl, clipboard, Hatchling and the new shield/minion menus. Geometry covers nine splashes/portraits, talents, skills and ItemSlots. Nine-class selection/36 handbook-page coverage is retained from the prior checkpoint and repeated by CI. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Retained v1.14.0/local and prior CI five-region remembered-terrain and wall-cap results; this patch does not change terrain or fog. Not rerun locally in this patch; CI repeats all five regions. |
| 44 | PASS | Retained v1.14.0 offline reconstruction: assets=124 source sheets=115 launcher resources=55, failures=0. This patch reuses existing painted glyphs and changes no source PNGs or atlases. CI repeats reconstruction. |
| 45 | permanent known issue | Retained Windows measurement: 22/76 failed comparisons (Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15). Not remeasured locally in this rendering-behavior patch. Unchanged 0.12 luminance / 40-degree hue thresholds remain enforced; CI refreshes all five regions. |
| 46 | PASS | Fresh compiled handler audit: classes=2925, guarded browser sinks=1, HTTP/socket calls=0, failures=0; native runtime title controls=5, credits handlers=4, repository URLs=4, external opens=0, scene fetches=0, failures=0. |
| 47 | PASS | Retained prior five-region CI results: 24 zoom/pan cases per region, exact 1:1 fog ratio, aligned light quad, hidden cells black, visible cells unobscured. No fog changes or local rerun this patch; CI repeats all five. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Existing Ashlight feed/charge/shutter/invisibility/resistance/Flare/persistence suite passes. New Infernal Brew feeding and level-8 disguised-mimic burning thresholds pass. Real native artifact menus pass in both orientations. |
| 52 | PASS | Guarded Playtest, 314 item types/20 artifact caps, nine kits/progression, branch travel/save isolation pass. New checks consolidate populated duplicate pouches, preserve seeds across class changes/reload, and invoke real ascent into unvisited floors 5/10/15/20/25 after travel. |
| 53 | PASS | Existing artifact/feeding/mimic/rank/armor/barricade/mobile timing checks pass. Both native orientations pass real pointer handbook, all ranks in one description, long-scroll bounds, Hurl and unknown-source/remembered-wall-cap checks. |
| 54 | PASS | Ten Enchanter seeds cover proc rates, power/curses/other-class isolation, caps/refunds and Lucky stacking: later failed rolls preserve same-hit success, next attacks clear stale success, shared bow results survive, both Overcharge slots generate loot, eligible loot actually enters a heap, and overleveled enemies retain upstream loot exclusion. Both native orientations pass actual third-rank purchase and repeated cap rejection. |
| 55 | PASS | Fresh all-nine Hatchling scenarios and native portrait/landscape suites pass. Regression asserts display titles rather than Java identities; warnings cancel long movement/rest and wait for real Hero readiness, then the next player turn feeds. Slow-action and saved-warning barriers are covered. Native log: Your hatchling mimic devours your shortsword. It sniffs the air, sensing treasure nearby. Existing hierarchy, benefits, debt, transformation, theft and persistence checks remain passing. |
| 56 | PASS | Fresh balance/class/effect regressions pass. Native effects cover five families and 16 Speck kinds including NPC RATTLE. Actual Necromancer summoning emitter captured. Shaman overlap regression checks all three variants, sprite-to-cell alignment, Actor occupancy, melee eligibility and actual examine targets. Eight status icons stay inside the HUD in portrait/landscape; painted resume/skull resolve. Exact reported floor-11 save and physical tablet remain unavailable. |

# Painted effects and class balance - v1.14.0

Release: **v1.14.0-effects**. Main implementation begins at `74b81abec`; the tag identifies the final source/evidence commit. The new source art was produced with built-in image generation; exact prompts and source PNGs are committed under `tools/painted/`.

Fresh local results: **Runs=90 failures=0**, seven JUnit tests, Windows desktop/native launcher and Android packaging pass. The all-nine run includes Necromancer **10/0**, Enchanter **10/0**, Psychic **10/0**, combined new classes **30/0**. These are generated-floor/scripted scenarios, not a complete campaign or physical Android playtest. No tablet was attached (`adb devices -l`: empty).

[Painted effects and sharper creatures](interface/landscape/creatures-and-tengu-effects.png), [denser gas with readable loot](interface/landscape/hatchling-sense-and-gas-loot.png), [Defensive Sigil menu](interface/portrait/defensive-sigil-menu.png), [active ward](interface/portrait/defensive-sigil-active.png), [Raise Dead](interface/portrait/raise-dead-menu.png), [undead time remaining](interface/portrait/raised-undead-lifetime.png).

Two short-creature occupancy failures exposed by denser exports were repaired in the shared bilinear-fringe sizing calculation; all standard geometry checks then passed. Native review also caught and replaced the old lightning core underneath its painted halo. Checks were not relaxed. Test 45 remains an enforced known failure. Prior results explicitly described as retained below were not rerun locally; CI repeats their standard gates.

| Test | Status | Actual evidence and scope |
|---|---|---|
| 1 | PASS | Windows 1.14.0 desktop and packaged Grimhollow.exe launch; native launcher TitleScene exit=0. Landscape mouse and portrait touch exercise the new Brush action, shield inspection, Raise Dead menu and minion lifetime text alongside existing menus. |
| 2 | PASS | core:test core:smokeRun -PsmokeUpstream=true -PdesktopOnly=true --no-daemon: Runs=90 failures=0, seven JUnit tests with zero failures/errors. Final desktop:dist android:assembleDebug: BUILD SUCCESSFUL in 2m 23s. jpackage and bundled-runtime launcher pass. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0: nine hero classes, ten seeds each, including starter kits and added-class scenarios. |
| 6 | permanent known issue | Representative hooks and native purchasing pass; Defensive Sigil ranks/cost/refresh/expiry/save compatibility are covered by test 56. Exhaustive every-talent combat coverage remains unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Existing Grasp heaps/traps/range tests pass; Hatchling scenario additionally checks bones at Crystal 3, unlocked chests at 7, and locked-container rejection. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | All nine classes save/load through floor 6. Crystal swap/re-equip retains level/charges; Precognition expenditure survives serialization. |
| 15 | PASS | Runs=90 failures=0. Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined new classes 30/0 as part of the all-nine gate. |
| 16 | permanent known issue | Fresh geometry covers all 122 concrete creature sprites, steady idle/travel and animation rectangles. All 71 creature atlases use density 8 and remain at most 4096 pixels per side. Exhaustive gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; not rerun locally. CI independently builds with desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced. Exact-head CI results and failing steps are recorded in the annotated release tag and delivery; no checks disabled or weakened. |
| 22 | PASS | aapt: com.grimhollow.dungeon, versionCode=958, versionName=1.14.0-INDEV, label Grimhollow, minimum SDK 21, target 36. |
| 23 | PASS | Native checks and packaged launcher use isolated .local/effects-* profiles; headless saves use diagnostic slot 99. Existing player saves were not used. |
| 24 | permanent known issue | Fresh standard geometry: heroes=9, mob sprites=122, steady idle/travel checks=122, failures=0. Bilinear fringe sizing corrected two newly exposed short-creature failures without changing the acceptance band. Historical six 2x occupancy exceptions have not been remeasured locally. |
| 25 | PASS | TEST 25: items=384 identification icons=60 failures=0; named semantics and per-index hashes pass. New Hatchling occupies index 539. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 idle turns leave charges unchanged; 12 charges unlock Wraith at level 1; hostile attacked within 2 turns. Ghoul remains locked through +4, unlocks at +5 and heals 15% of health actually removed, capped against overkill. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Final ray build: 240 GPU-completed frames, 40 gas + 10 fire cells, mean=0.9399ms p95=1.2642ms; effects-off pixel differences=0; batched-fire maximum channel difference=0. Unchanged 2ms gate. |
| 32 | PASS | TEST 32: three scorch sizes, actual floor fire expiration, water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 RANGED PASS: actual carried thrown-weapon and Spirit Bow inscriptions, thrown stack split/merge, arrow proc dispatch, ownership and melee-only filtering, unchanged duration. Trade knowledge, armor inscriptions and persistent-library checks also pass. Both orientations additionally pass actual equipment-picker armor selection, scrolled glyph selection, touch weapon/info, drag suppression and zero-charge rejection. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Native landscape/portrait suites exercise inventory, scroll bounds, all-rank descriptions, pointer purchases, Hurl, clipboard, Hatchling and the new shield/minion menus. Geometry covers nine splashes/portraits, talents, skills and ItemSlots. Nine-class selection/36 handbook-page coverage is retained from the prior checkpoint and repeated by CI. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Fresh Sewers: 100 walked steps, 23 turns, eight door openings; remembered terrain and wall-cap checks pass. Prior other-four-region results retained; CI repeats all five on this code. |
| 44 | PASS | PAINTED assets=124 source sheets=115 launcher resources=55 failures=0; TEST 44 upstream-derived character sheets=7 restored assets=29 verified painted replacements=124 failures=0. Full offline reconstruction run after the ray additions. |
| 45 | permanent known issue | Recomputed Windows evidence: 22/76 failed comparisons (Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15). Sewer captures are fresh; other four regions retain prior captures. Thresholds 0.12 luminance / 40 degrees hue remain enforced. CI refreshes all five regions. |
| 46 | PASS | Compiled handler audit: classes=2924, guarded browser sinks=1, HTTP/socket calls=0, failures=0. Runtime title controls=5, credits handlers=4, repository URLs=4, external opens=0, scene fetches=0, failures=0. Final ray changes add no network calls; CI repeats the audit. |
| 47 | PASS | Fresh Sewers: 24 zoom/pan cases, 151 door-transition frames, exact 1:1 world fog ratio and aligned light quad; hidden cells black, visible cells unobscured, failures=0. Prior other-four-region results retained; CI repeats all five. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Existing Ashlight feed/charge/shutter/invisibility/resistance/Flare/persistence suite passes. New Infernal Brew feeding and level-8 disguised-mimic burning thresholds pass. Real native artifact menus pass in both orientations. |
| 52 | PASS | Guarded Playtest, 314 item types/20 artifact caps, nine kits/progression, branch travel/save isolation pass. New checks consolidate populated duplicate pouches, preserve seeds across class changes/reload, and invoke real ascent into unvisited floors 5/10/15/20/25 after travel. |
| 53 | PASS | Existing artifact/feeding/mimic/rank/armor/barricade/mobile timing checks pass. Both native orientations pass real pointer handbook, all ranks in one description, long-scroll bounds, Hurl and unknown-source/remembered-wall-cap checks. |
| 54 | PASS | Ten Enchanter seeds cover proc rates, power/curses/other-class isolation, caps/refunds and Lucky stacking: later failed rolls preserve same-hit success, next attacks clear stale success, shared bow results survive, both Overcharge slots generate loot, eligible loot actually enters a heap, and overleveled enemies retain upstream loot exclusion. Both native orientations pass actual third-rank purchase and repeated cap rejection. |
| 55 | PASS | Hatchling scenarios execute for all nine classes: food/protection/stack hierarchy, warning interruption, cauldron 10/15/20 costs and persistence, once-per-run catalyst, all benefit/Item Sense tables, curse/rare probability trials, permanent upgrades, debt/partial gold, transformation/Crystal chance, actual +10 Wealth reward, standard/Golden/Ebony/Crystal AI, theft/recovery/escape and save/load. Native inspect scrolling, item-only detection and gas-loot visibility pass in both orientations. Full-campaign balance remains untested. |
| 56 | PASS | New headless checks cover 81 current/fallen class pairs, ordinary bones loot, all four undead lifetime descriptions, Ghoul +5 unlock/15% capped healing, and Defensive Sigil costs, both ranks, refresh, damage, six-turn expiry, no-charge/magic-immune/unequipped failures and legacy talent/save compatibility. Native checks cover 71 density-8 atlases, five painted particle families, 15 Speck meanings, six ray/wound/ripple types, and the shield/Raise Dead/countdown UI in both orientations. |

# Hatchling Mimic and playtest presentation - v1.13.0

Release: **v1.13.0-hatchling**. Implementation commits begin at `df061db26` and `23542341`; the release tag identifies the final report and fixture corrections. The requested extra reward uses the real `RingOfWealth.genEquipmentDrop(10)`, not a separate legendary pool.

Fresh local gates: all nine classes **Runs=90 failures=0**; Necromancer **10/0**, Enchanter **10/0**, Psychic **10/0**, combined added classes **30/0**. Seven JUnit tests, desktop/APK builds, both native interface orientations, all five fog/walking suites, full offline art reconstruction and compiled network audit were executed. No physical Samsung tablet was connected, and no complete campaign balance claim is made.

[Hatchling hunger and description](interface/portrait/hatchling-description.png), [description bottom](interface/portrait/hatchling-description-bottom.png), [object-only sensing and gas loot](interface/landscape/hatchling-sense-and-gas-loot.png), [Prison loading painting](interface/landscape/loading-prison.png), [Caves loading painting](interface/landscape/loading-caves.png), [City loading painting](interface/portrait/loading-city.png). All five regional paintings are packed and verified; fresh native transition captures are provided for these three regions.

Intermediate failures were fixed rather than suppressed: a headless loot fixture required its scene/sprite setup; newly spawned mimics now set intent before sprite creation; regional transitions use the normal interlevel scene; transition capture draws that scene rather than the previous framebuffer; the remembered-cap test now expects concealed unknown neighbors; and the final catalyst test corrected an API-name typo. Detailed local diagnostics are preserved in ignored `.local/hatchling-*.txt` and `.local/hatchling-native-verification.log`. CI retains its failing runs. Test 45 remains an enforced known failure, **22/76** local comparisons.

| Test | Status | Actual evidence and scope |
|---|---|---|
| 1 | PASS | Windows 1.13.0 desktop launches. Native landscape mouse and portrait touch exercise actual item menus, progression, reports, Hatchling description/scrolling and item-only detection. Packaged Grimhollow.exe launched with its bundled runtime and rendered TitleScene (exit 0); capture: interface/native-launcher/title.png. |
| 2 | PASS | core:test core:smokeRun desktop:dist android:assembleDebug -PsmokeUpstream=true --no-daemon: BUILD SUCCESSFUL in 1m 34s; seven JUnit tests, zero failures/errors; Runs=90 failures=0. jpackage succeeds and the native executable renders TitleScene. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0: nine hero classes, ten seeds each, including starter kits and added-class scenarios. |
| 6 | permanent known issue | Representative hooks pass; new test 54 covers the reported Master Craft purchase, all four Overcharge talent caps and legacy refund. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Existing Grasp heaps/traps/range tests pass; Hatchling scenario additionally checks bones at Crystal 3, unlocked chests at 7, and locked-container rejection. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | All nine classes save/load through floor 6. Crystal swap/re-equip retains level/charges; Precognition expenditure survives serialization. |
| 15 | PASS | Runs=90 failures=0. Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined new classes 30/0 as part of the all-nine gate. |
| 16 | permanent known issue | Fresh geometry checks cover all 122 concrete creature sprites, steady idle/travel, NPC/reflected forms and item/talent contracts. Exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; not rerun locally. CI independently builds with desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced. Final exact-head CI and all failing steps are recorded in the annotated release tag and delivery; no checks disabled or weakened. |
| 22 | PASS | aapt: com.grimhollow.dungeon, versionCode=957, versionName=1.13.0-INDEV, label Grimhollow, minimum SDK 21, target 36. |
| 23 | PASS | Native checks and packaged launcher use isolated .local/hatchling-* profiles; headless saves use diagnostic slot 99. Existing player saves were not used. |
| 24 | permanent known issue | Fresh standard geometry: heroes=9, mob sprites=122, steady idle/travel checks=122, failures=0. Previously documented six retained 2x occupancy exceptions remain unresolved. |
| 25 | PASS | TEST 25: items=384 identification icons=60 failures=0; named semantics and per-index hashes pass. New Hatchling occupies index 539. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | 240 GPU-completed frames with 40 gas and 10 fire cells: mean=0.5172ms p95=0.6869ms; Effects-off pixel differences=0 and batched-fire maximum channel difference=0. Unchanged 2ms gate; light-map interpolation JUnit tests pass. |
| 32 | PASS | TEST 32: three scorch sizes, actual floor fire expiration, water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 RANGED PASS: actual carried thrown-weapon and Spirit Bow inscriptions, thrown stack split/merge, arrow proc dispatch, ownership and melee-only filtering, unchanged duration. Trade knowledge, armor inscriptions and persistent-library checks also pass. Both orientations additionally pass actual equipment-picker armor selection, scrolled glyph selection, touch weapon/info, drag suppression and zero-charge rejection. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Fresh landscape/portrait native inventory, scroll bounds, all-rank descriptions, pointer purchases, Hurl, clipboard, Hatchling inspect/sense and gas-loot presentation pass. Geometry covers nine splashes/portraits and talent/ItemSlot contracts. Nine-class selection and 36 handbook-page pointer suites passed CI at 2354234; final CI repeats them. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five generated regions: 788 walking steps, 254 turns, 75 door openings; remembered terrain and known wall-cap failures=0. Expected cap stitching conceals unknown neighbors; independent native assertions verify unknown terrain cannot alter visible geometry. |
| 44 | PASS | PAINTED assets=121 source sheets=112 launcher resources=55 failures=0; TEST 44 upstream-derived character sheets=7 restored assets=29 verified painted replacements=121 failures=0. Full offline reconstruction was run. |
| 45 | permanent known issue | Fresh Windows measurements fail 22/76 comparisons: Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15. Values and captures are in verification/recovery/*/distinctness.json. Thresholds 0.12 luminance / 40 degrees hue remain enforced; no palette retuning. |
| 46 | PASS | Compiled handlers: classes=2920 guarded browser sinks=1 HTTP/socket calls=0 failures=0. Runtime title controls=5 credits handlers=4 repository URLs=4 external opens=0 scene fetches=0 failures=0. |
| 47 | PASS | Five regions, 120 zoom/pan camera cases, 1378 door-transition frames; every-cell black/visible checks, exact 1:1 world fog ratio and aligned light quad pass. Known-wall black blockers=0; unknown-neighbor wall changes leave known geometry invariant. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Existing Ashlight feed/charge/shutter/invisibility/resistance/Flare/persistence suite passes. New Infernal Brew feeding and level-8 disguised-mimic burning thresholds pass. Real native artifact menus pass in both orientations. |
| 52 | PASS | Guarded Playtest, 314 item types/20 artifact caps, nine kits/progression, branch travel/save isolation pass. New checks consolidate populated duplicate pouches, preserve seeds across class changes/reload, and invoke real ascent into unvisited floors 5/10/15/20/25 after travel. |
| 53 | PASS | Existing artifact/feeding/mimic/rank/armor/barricade/mobile timing checks pass. Both native orientations pass real pointer handbook, all ranks in one description, long-scroll bounds, Hurl and unknown-source/remembered-wall-cap checks. |
| 54 | PASS | Ten Enchanter seeds cover proc rates, power/curses/other-class isolation, caps/refunds and Lucky stacking: later failed rolls preserve same-hit success, next attacks clear stale success, shared bow results survive, both Overcharge slots generate loot, eligible loot actually enters a heap, and overleveled enemies retain upstream loot exclusion. Both native orientations pass actual third-rank purchase and repeated cap rejection. |
| 55 | PASS | Hatchling scenarios execute for all nine classes: food/protection/stack hierarchy, warning interruption, cauldron 10/15/20 costs and persistence, once-per-run catalyst, all benefit/Item Sense tables, curse/rare probability trials, permanent upgrades, debt/partial gold, transformation/Crystal chance, actual +10 Wealth reward, standard/Golden/Ebony/Crystal AI, theft/recovery/escape and save/load. Native inspect scrolling, item-only detection and gas-loot visibility pass in both orientations. Full-campaign balance remains untested. |

# Playtest fixes and tablet movement - v1.12.0

Source implementation: `80cbc0eb270e629f948ab9b5e9b7ef6f84301dba`. Intended tag: **v1.12.0-playtest-fixes**. All eleven playtest categories are addressed; see CHANGES.md for scope and compatibility details. No on-device Samsung SM-T830 performance claim is made: adb listed no attached devices.

Fresh results: **Runs=90 failures=0**, seven JUnit tests, Windows desktop and Android APK builds, both native interface orientations, all five generated-region fog/walking suites, exact offline asset reconstruction and compiled network-handler audit. Necromancer **10/0**, Enchanter **10/0**, Psychic **10/0**, combined new classes **30/0** (within the 90-run gate). Test 45 remains a known enforced contrast failure, **34/82** Windows comparisons. No thresholds or CI checks were disabled.

[Portrait creatures/effects](interface/portrait/creatures-and-tengu-effects.png), [landscape creatures/effects](interface/landscape/creatures-and-tengu-effects.png), [copyable report](interface/portrait/copyable-issue-report.png), [inventory](interface/portrait/inventory.png), [all-rank skill description](interface/landscape/tablet-talent-rank1.png). These are native desktop captures, including a controlled smoke/spark/arc presentation, not a physical tablet or complete Tengu fight.

Intermediate failures remain in clean-build.log: the new Lucky loot fixture initially tried to emit gold particles without a scene; native fixture additions needed compile corrections, an overflowing-only scroll assertion, and restoration of an independent exit fixture's terrain. The final scenarios pass. A simultaneous append to the existing log was denied by Windows file sharing; interface output was retained in memory and appended after the region run closed the file. No extra log files were introduced. CI 36180856282 then caught an obsolete Armor-button test and the isolated Enchanter loot fixture's missing camera. After correcting those fixtures, both complete presentation suites pass and the standalone Enchanter gate reports Runs=10 failures=0; desktop/APK repackaging reports BUILD SUCCESSFUL in 51s. No gameplay code was changed for these fixture corrections.

| Test | Status | Actual evidence and scope |
|---|---|---|
| 1 | PASS | Windows 1.12.0 launches. Native landscape mouse and portrait touch cover inventory, scrolling progression, talent purchases, copyable reports and Playtest controls. |
| 2 | PASS | core:test core:smokeRun desktop:release android:assembleDebug -PsmokeUpstream=true --no-daemon: BUILD SUCCESSFUL in 56s. Seven JUnit tests, zero failures/errors. Final desktop-only fixture rebuild: BUILD SUCCESSFUL in 37s. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0: nine hero classes, ten seeds each, including starter kits and added-class scenarios. |
| 6 | permanent known issue | Representative hooks pass; new test 54 covers the reported Master Craft purchase, all four Overcharge talent caps and legacy refund. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Grasp still collects heaps and activates/removes visible traps; all level 0-10 utility boundaries and unseen-target rejection pass. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | All nine classes save/load through floor 6. Crystal swap/re-equip retains level/charges; Precognition expenditure survives serialization. |
| 15 | PASS | Runs=90 failures=0. Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined new classes 30/0 as part of the all-nine gate. |
| 16 | permanent known issue | Fresh native checks cover all 122 concrete creature sprites, animation rectangles, steady idle/travel poses, reflected/NPC forms and item/talent contracts. Exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced. CI 36180856282 also exposed two test-fixture defects (old Armor button selector and missing standalone headless camera), corrected and rerun locally. Final exact-head CI results are recorded in the annotated release tag and delivery. |
| 22 | PASS | aapt: com.grimhollow.dungeon, versionCode=956, versionName=1.12.0-INDEV, label Grimhollow, minimum SDK 21, target 36. |
| 23 | PASS | Native runs use isolated .local/playtest-v112-* homes; headless saves use diagnostic slot 99. Existing player saves were not used. |
| 24 | permanent known issue | Fresh standard geometry: heroes=9, mob sprites=122, steady idle/travel checks=122, failures=0. Previously documented six retained 2x occupancy exceptions remain unresolved. |
| 25 | PASS | TEST 25: items=383 identification icons=60 failures=0; all per-index hashes and named semantics checked. Repainted Strength overlay uses a fist; Mind Vision uses eye art at index 98. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | TEST 31: off pixel differences=0; individual fire reference maximum channel difference=0; 40 gas + 10 fire cells, 240 GPU-completed frames mean=0.8130ms p95=1.2621ms failures=0. The 2ms gate is unchanged. LightMapTest separately bounds the reduced light-grid interpolation error to 10/255. |
| 32 | PASS | TEST 32: three scorch sizes, actual floor fire expiration, water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 RANGED PASS: actual carried thrown-weapon and Spirit Bow inscriptions, thrown stack split/merge, arrow proc dispatch, ownership and melee-only filtering, unchanged duration. Trade knowledge, armor inscriptions and persistent-library checks also pass. Both orientations additionally pass actual equipment-picker armor selection, scrolled glyph selection, touch weapon/info, drag suppression and zero-charge rejection. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Fresh native landscape/portrait inventory, all rank descriptions without toggles, scroll bounds, Hurl, capped talent purchases and actual clipboard copy pass. Geometry checks cover nine splashes/portraits, all talents and ItemSlots. Fresh presentation checks exercise all nine selections, matching paintings, second-selection/info actions and 36 handbook pages in each orientation; native armor/weapon inscription library selection, scrolling and charge checks also pass. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five generated regions: 788 walking steps, 255 turns, 75 door openings; remembered floor/water/grass/door/chasm and retained known wall-cap checks all have failures=0. |
| 44 | PASS | PAINTED launcher resources=55; PAINTED assets=116 source sheets=106 failures=0; TEST 44: upstream-derived character sheets=7; restored assets=29; verified painted replacements=116; failures=0. |
| 45 | permanent known issue | Fresh Windows measurements fail 34/82 comparisons: Sewers 11/21, Prison 1/10, Caves 8/21, City 5/15, Halls 9/15. Full delta-L/hue values are in verification/recovery/*/distinctness.json and clean-build.log. Thresholds 0.12 luminance / 40 degrees hue remain enforced; no terrain palette retuning. |
| 46 | PASS | Compiled handlers: classes=2911 guarded browser sinks=1 HTTP/socket calls=0 failures=0. Runtime title controls=5 credits handlers=4 repository URLs=4 external opens=0 scene fetches=0 failures=0. |
| 47 | PASS | Five regions, 120 zoom/pan camera cases, 1369 door-transition frames; every-cell black/visible tests, exact 1:1 world fog ratio and aligned lighting quad pass. Known-wall edge checks have zero black blockers. Native UI also checks cap retention and unknown-source exclusion. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Existing Ashlight feed/charge/shutter/invisibility/resistance/Flare/persistence suite passes. New Infernal Brew feeding and level-8 disguised-mimic burning thresholds pass. Real native artifact menus pass in both orientations. |
| 52 | PASS | Guarded Playtest, 313 item types/20 artifact caps, nine kits/progression, branch travel/save isolation pass. New checks consolidate populated duplicate pouches, preserve seeds across class changes/reload, and invoke real ascent into unvisited floors 5/10/15/20/25 after travel. |
| 53 | PASS | Existing artifact/feeding/mimic/rank/armor/barricade/mobile timing checks pass. Both native orientations pass real pointer handbook, all ranks in one description, long-scroll bounds, Hurl and unknown-source/remembered-wall-cap checks. |
| 54 | PASS | Ten Enchanter seeds cover proc rates, power/curses/other-class isolation, caps/refunds and Lucky stacking: later failed rolls preserve same-hit success, next attacks clear stale success, shared bow results survive, both Overcharge slots generate loot, eligible loot actually enters a heap, and overleveled enemies retain upstream loot exclusion. Both native orientations pass actual third-rank purchase and repeated cap rejection. |

# Enchanter and talent purchasing - v1.11.2

Source implementation: `c8dcfca54`. Intended tag: **v1.11.2-enchanter-talents**. Permanent enchantments/glyphs proc 25% more often, and temporary inscriptions/sigils (including Rune Etching) twice as often, from level one. Bonuses affect activation chance only. Talent purchases now reject duplicate/stale offers and enforce maximum ranks in both the UI and hero model.

Fresh verification: **Runs=90 failures=0** (ten per hero; Necromancer 10/0, Enchanter 10/0, Psychic 10/0, combined new classes 30/0), six JUnit tests, Windows desktop and Android APK builds. The final native landscape mouse and portrait touch suites pass tests 36/51/52/53/54, including actual hero-screen purchases. Failed intermediate checks are retained in clean-build.log: an initial forwarding fix swallowed handbook taps, and a bare test-window fixture failed the purchase check; the delivered implementation passes with the real WndHero screen. No physical Android playtest is claimed.

[Portrait cap evidence](interface/portrait/enchanter-rank-cap.png), [landscape cap evidence](interface/landscape/enchanter-rank-cap.png), [upgrade offer](interface/portrait/enchanter-upgrade-offer.png). Test 45 is a retained contrast failure; this patch changes no art or contrast thresholds. Unchanged historical checks below are explicitly retained, not presented as newly executed. CI repeats its configured checks on the delivered head.

| Test | Status | Actual evidence and scope |
|---|---|---|
| 1 | PASS | Windows 1.11.2 launches; native landscape mouse and portrait touch exercise the actual hero upgrade screen, one-popup dispatch, third-rank purchase and repeated cap rejection. |
| 2 | PASS | core:test, core:smokeRun -PsmokeUpstream=true, desktop:release and android:assembleDebug: BUILD SUCCESSFUL. Six JUnit tests, zero failures/errors. Final UI rebuild and both native interface suites pass. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0: nine hero classes, ten seeds each, including starter kits and added-class scenarios. |
| 6 | permanent known issue | Representative hooks pass; new test 54 covers the reported Master Craft purchase, all four Overcharge talent caps and legacy refund. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Grasp still collects heaps and activates/removes visible traps; all level 0-10 utility boundaries and unseen-target rejection pass. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | All nine classes save/load through floor 6. Crystal swap/re-equip retains level/charges; Precognition expenditure survives serialization. |
| 15 | PASS | Runs=90 failures=0. Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined new classes 30/0 as part of the all-nine gate. |
| 16 | permanent known issue | Retained v1.11.1 evidence (not rerun locally in this focused patch): All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Previous checkpoint CI fails only the enforced test-45 contrast gate. Exact-head CI will be checked after this commit and recorded in the release tag and delivery; no checks are disabled or weakened. |
| 22 | PASS | aapt: com.grimhollow.dungeon, versionCode=955, versionName=1.11.2-INDEV, label Grimhollow, minimum SDK 21, target 36. |
| 23 | PASS | Native runs use isolated .local/enchanter-rate-* homes; headless saves use diagnostic slot 99. Player saves were untouched. |
| 24 | permanent known issue | Retained v1.11.1 evidence (not rerun locally in this focused patch): Current standard geometry: nine heroes, 122 creature sprites/steady idles, no failures. The previously recorded six retained 2x occupancy exceptions remain unresolved. |
| 25 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): TEST 25: items=383 identification icons=60 failures=0. |
| 26 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): Three stains and floor/chasm/water/trap placement: failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): TEST 31: off pixel differences=0; individual fire reference max channel difference=0; 40 gas + 10 fire cells, 240 GPU-completed frames mean=0.3841ms p95=0.6002ms failures=0. The 2ms gate is unchanged. |
| 32 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): Three scorch sizes, actual floor fire expiration, water/chasm rejection: failures=0. |
| 33 | PASS | Existing Enchanter trade knowledge, armor inscriptions and persistent library scenarios pass in the nine-class run. Native library coverage is retained and repeated by CI. |
| 34 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): Old/future versions, portrait exception and deletion: failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Native landscape/portrait inventory, skill previews, scroll bounds and one-offer capped talent purchases pass. Nine-class presentation selection coverage is retained from v1.11.1 and repeated by CI. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): Five generated regions: 788 walking steps, 255 turns, 75 door openings; remembered-terrain checks have failures=0 in every region. |
| 44 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): PAINTED launcher resources=55; PAINTED assets=115 source sheets=106 failures=0; TEST 44: upstream-derived character sheets=7; restored assets=29; verified painted replacements=115; failures=0 |
| 45 | permanent known issue | Retained v1.11.1 evidence (not rerun locally in this focused patch): Fresh Windows measurements fail 34/82 comparisons: sewers 11/21, prison 1/10, caves 8/21, city 5/15, halls 9/15. Thresholds 0.12 luminance / 40 degrees hue remain enforced. No palette retuning. |
| 46 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): TEST 46 compiled handlers: classes=2905 guarded browser sinks=1 HTTP/socket calls=0 failures=0; runtime title/credits handlers also pass. |
| 47 | PASS | Retained v1.11.1 evidence (not rerun locally in this focused patch): Five regions, 120 zoom/pan camera cases, 1393 door-transition frames; every-cell black/visible tests and exact 1:1 world fog ratio pass. Known-wall edge checks report zero black blockers. Test 53 also rejects wall/door/prop overhangs from unknown source cells. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Existing Ashlight feed/charge/shutter/invisibility/resistance/Flare/persistence suite passes. New Infernal Brew feeding and level-8 disguised-mimic burning thresholds pass. Real native artifact menus pass in both orientations. |
| 52 | PASS | Guarded persistent Playtest, god mode, 313 item types/20 artifact caps, nine class kits, progression, floor/quest travel and save isolation pass; real pointer controls pass in both orientations. |
| 53 | PASS | Artifact swap/charger persistence, Infernal feeding, mimic Flare, rank effects, 214 distinct rank descriptions, 27 armor paths with four talents/four ranks, barricade orientation and mobile camera pacing pass. Actual touch handbook/drag/rank1/rank4/long-scroll/Hurl input and unknown-source overhang checks pass in both orientations. |
| 54 | PASS | TEST 54 PASS in ten Enchanter seeds: permanent 1.25x, inscriptions/sigils 2x, half-strength Rune Etching 2x; chance-only, unchanged power/curses/other classes; three-rank cap, legitimate four-rank armor and legacy refund. TEST 54 UI PASS in landscape mouse and portrait touch on the actual WndHero screen. |

# Tablet playtest fixes - v1.11.1

Source checkpoint: `e28a3bf598006875875a3798522aefbd32002b2b`. Intended tag: **v1.11.1-tablet-fixes**. All requested fix categories are implemented; the clarification keeps three armor paths per class. See CHANGES.md for exact rank values and rendering/input changes.

The complete nine-class smoke run reports **Runs=90 failures=0**; six JUnit tests pass. Desktop and Android builds pass. Native landscape and portrait test 53 uses real pointer/touch dispatch, not direct skill callbacks. It found and corrected the original lost second Hurl selection, swallowed handbook taps and a description-camera resize error. Fresh five-region fog/walking checks and the unchanged geometry/effects gates pass. No physical tablet was attached (`adb devices -l` returned no devices), so device smoothness and campaign balance remain player-review items.

[Rank preview](interface/portrait/tablet-talent-rank4.png), [Hurl](interface/portrait/tablet-hurl.png), [barricades and upgrade effect](interface/portrait/tablet-upgrade.png), [long-description scrolling](interface/portrait/tablet-long-description.png). Actual outputs, including intermediate failures and the final successful checks, are retained in clean-build.log. Existing player saves were not used.

This table reports all tests 1-53. Retired checks and permanent limitations remain explicit; retained-only coverage is identified. CI repeats its configured suite on the delivered head, and its exact result is included in the delivery response.

| Test | Status | Actual evidence and scope |
|---|---|---|
| 1 | PASS | Windows 1.11.1 launches; real mouse/touch interface flows pass in landscape and portrait, including Hurl and scrolling skill previews. |
| 2 | PASS | desktop:dist + android:assembleDebug + core:test + all-class smoke: BUILD SUCCESSFUL; six JUnit tests, zero failures/errors. Final packaging after visual fixes also succeeds. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0: nine hero classes, ten seeds each, including starter kits and added-class scenarios. |
| 6 | permanent known issue | Both class talent tiers are browsable for all nine heroes; representative hooks pass. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Grasp still collects heaps and activates/removes visible traps; all level 0-10 utility boundaries and unseen-target rejection pass. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | All nine classes save/load through floor 6. Crystal swap/re-equip retains level/charges; Precognition expenditure survives serialization. |
| 15 | PASS | Runs=90 failures=0. Necromancer 10/0, Enchanter 10/0, Psychic 10/0; combined new classes 30/0 as part of the all-nine gate. |
| 16 | permanent known issue | All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Full CI retains the test-45 failure. Final exact-head CI results and stage tag are reported at delivery; no checks are disabled or weakened. |
| 22 | PASS | aapt: com.grimhollow.dungeon, versionCode=954, versionName=1.11.1-INDEV, label Grimhollow, minimum SDK 21, target 36. |
| 23 | PASS | Native runs use isolated .local/tablet-* user homes; headless saves use diagnostic slot 99. Player saves were untouched. |
| 24 | permanent known issue | Current standard geometry: nine heroes, 122 creature sprites/steady idles, no failures. The previously recorded six retained 2x occupancy exceptions remain unresolved. |
| 25 | PASS | TEST 25: items=383 identification icons=60 failures=0. |
| 26 | PASS | Three stains and floor/chasm/water/trap placement: failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | TEST 31: off pixel differences=0; individual fire reference max channel difference=0; 40 gas + 10 fire cells, 240 GPU-completed frames mean=0.3841ms p95=0.6002ms failures=0. The 2ms gate is unchanged. |
| 32 | PASS | Three scorch sizes, actual floor fire expiration, water/chasm rejection: failures=0. |
| 33 | PASS | Existing Enchanter trade knowledge, armor inscriptions and persistent library scenarios pass in the nine-class run. Native library coverage is retained and repeated by CI. |
| 34 | PASS | Old/future versions, portrait exception and deletion: failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Nine splashes/portraits/talent icons and ItemSlots pass; interface and test-53 skill taps/rank previews/scrolling pass in both orientations. All-nine selection flow is retained and repeated by CI. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five generated regions: 788 walking steps, 255 turns, 75 door openings; remembered-terrain checks have failures=0 in every region. |
| 44 | PASS | PAINTED launcher resources=55; PAINTED assets=115 source sheets=106 failures=0; TEST 44: upstream-derived character sheets=7; restored assets=29; verified painted replacements=115; failures=0 |
| 45 | permanent known issue | Fresh Windows measurements fail 34/82 comparisons: sewers 11/21, prison 1/10, caves 8/21, city 5/15, halls 9/15. Thresholds 0.12 luminance / 40 degrees hue remain enforced. No palette retuning. |
| 46 | PASS | TEST 46 compiled handlers: classes=2905 guarded browser sinks=1 HTTP/socket calls=0 failures=0; runtime title/credits handlers also pass. |
| 47 | PASS | Five regions, 120 zoom/pan camera cases, 1393 door-transition frames; every-cell black/visible tests and exact 1:1 world fog ratio pass. Known-wall edge checks report zero black blockers. Test 53 also rejects wall/door/prop overhangs from unknown source cells. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Existing Ashlight feed/charge/shutter/invisibility/resistance/Flare/persistence suite passes. New Infernal Brew feeding and level-8 disguised-mimic burning thresholds pass. Real native artifact menus pass in both orientations. |
| 52 | PASS | Guarded persistent Playtest, god mode, 313 item types/20 artifact caps, nine class kits, progression, floor/quest travel and save isolation pass; real pointer controls pass in both orientations. |
| 53 | PASS | Artifact swap/charger persistence, Infernal feeding, mimic Flare, rank effects, 214 distinct rank descriptions, 27 armor paths with four talents/four ranks, barricade orientation and mobile camera pacing pass. Actual touch handbook/drag/rank1/rank4/long-scroll/Hurl input and unknown-source overhang checks pass in both orientations. |

---

# In-game playtest controls - v1.11.0

Source checkpoint: `7717265a9`. Intended tag: **v1.11.0-playtest**. This adds opt-in testing controls; it changes no art or ordinary-run balance. Open the pause menu -> Playtest -> Enable. Mode and optional god power persist in the save, which is visibly marked and excluded from rankings, badges, catalog credit and bones. Player saves were not used: native checks have isolated user homes and headless checks use diagnostic slot 99.

`gradlew.bat desktop:dist android:assembleDebug core:test core:smokeRun -PsmokeUpstream=true --no-daemon --console=plain`: **BUILD SUCCESSFUL in 5m 8s; Runs=90 failures=0**. All nine heroes run ten seeds; test 52 runs once per class. Six JUnit tests pass with no failures/errors/skips. Final packaging after adding loading labels also succeeds in 30s. APK metadata: **com.grimhollow.dungeon, code 953, 1.11.0-INDEV**. No physical Android play is claimed.

| Test | Status | Actual output and scope |
|---|---|---|
| 52 | PASS | `TEST 52 PASS`: guarded persistent mode; ordinary damage unchanged; god damage/death protection; all 313 catalog types instantiate; 20 artifacts respect native caps and save/load; all nine class kits/subclasses/armor/talents; main regions/boss/final floors, Vault/Mine and return; rebuilding, creature placement, map reveal, teleport, recovery; save marker and ranking/catalog isolation. |
| 52 UI | PASS | `TEST 52 UI ... failures=0` in landscape 1280x720 and portrait 720x1061. Actual mouse/touch events enable mode/god, search Ashlight and create +10, set hero level 24, select Seer and armor, travel to floor 21, switch to Enchanter, disable god and inspect the marked save. Existing interface/readability/Ashlight native checks also pass. |

Screenshots: [desktop menu](interface/landscape/playtest-menu.png), [item creation](interface/landscape/playtest-create.png), [portrait menu](interface/portrait/playtest-menu.png), [Enchanter in Halls](interface/landscape/playtest-enchanter-halls.png), [save marker](interface/portrait/playtest-save.png). Actual output, including intermediate failures, is preserved in `clean-build.log`.

Tests 1-51 retain the individually dated evidence and permanent limitations below except that build/smoke/save/UI checks above were rerun on 1.11.0. Art, lighting, fog and the numerical terrain-distinctness threshold were not changed. Test 45 remains an enforced known failure, not a green full-workflow claim. CI runs its existing Windows/Linux/Android suite and the extended pointer/headless paths on the delivered head; exact run results are reported at delivery. Direct Vault travel preserves the testing loadout and does not replace normal quest-flow testing; floor rebuild does not reset global quest history.

# Nine hero art batches - v1.10.1

All nine heroes now have separately reviewed body proportions, silhouettes, cloth color identity, stances, strides and attack/cast gestures. The eight following batches have individual commits; the Necromancer's front armor panel was also corrected. This art pass changes no gameplay, world height, frame sequence, action duration or callbacks. The approved matching portraits are retained. [Lineup](heroes/lineup.png), [actual renderer crops](heroes/ingame-lineup.png), [individual before/after, animation and full screenshots](painted-world.html), [three exact built-in imagegen prompts/source references](../tools/painted/hero-rigs-prompts.json).

`gradlew.bat desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL in 34s**. Native lit/unlit generated-room renders ran for **all nine classes**, each selected explicitly with `grimhollow.heroClass`, generated seeds 418/419, terrainEdits=0, default zoom 3. Full screenshots live under each `verification/heroes/CLASS/`. The existing geometry suite validates all **1,512 hero pose/armor rectangles**, nine heroes and **122 creature sprites/steady idles, failures=0**; 383 named items, 60 identification overlays, talents, save portraits, nine splashes and ItemSlots pass. Test 31: **mean 0.4271ms / p95 0.6815ms, failures=0**, zero channel difference for the individual fire reference. Full `tools/recovery_assets.py --check`: **115 painted assets, 106 source sheets, 55 launchers, failures=0**. APK: **com.grimhollow.dungeon, code 952, 1.10.1-INDEV**. Existing Windows shortcut selects the newest jar by modification time. No physical Android session or full campaign is claimed.

The Necromancer-only art checkpoint `b9f7f187112666d85cfc7fdde15f62beac030c7b` is tagged **v1.10.0-necromancer-art**; CI [35996383670](https://github.com/BryanHartling/grimhollow/actions/runs/35996383670) passes Android, each class 10/0, combined 30/0, actual inscription pointer checks in both orientations, reconstruction, geometry, effects (p95 1.5859ms), all five-region fog/terrain paths and native encounters. Only test 45 fails (Windows 34/82, Linux 31/76). Its additional local live encounter replay completes **150 actions, 129 steps, 20 attacks, actor drop/pickup, summon and death rendering; failures=0**. Final all-nine-art release CI is inspected before tagging and reported at delivery. Other tests 1-51 retain the dated evidence and permanent limitations in the table below; no acceptance check is weakened.

# Hero art batch 1: Necromancer - v1.10.0

The Necromancer is the first completed silhouette/animation redesign. All nine hero sheets have sharper 96x120 source-derived frames; the other eight keep their previous poses pending individual batches. This changes no gameplay, frame sequencing, action duration or world-space footprint. The existing native geometry check now covers all 1,512 hero action/armor rectangles, including unchanged logical 12x15 image dimensions.

`gradlew.bat desktop:dist android:assembleDebug --no-daemon --console=plain`: **BUILD SUCCESSFUL**. Packaging was repeated after the full atlas pack finished to avoid a resource-write race. Native `--smoke-sewers` with `grimhollow.heroClass=NECROMANCER`, geometry and effects checks: **heroes=9; mob sprites=122; failures=0**; items=383 and identification icons=60 pass; all portrait/splash/talent/ItemSlot checks pass. Hero/rat height ratio remains 2.2222223. Effects: mean 0.5244ms / p95 0.6811ms, zero channel difference from individual fire draws, unchanged 2ms limit. `tools/recovery_assets.py --check`: **115 painted replacements, 104 source sheets, 55 launcher resources, failures=0**. APK metadata: com.grimhollow.dungeon, code 951, 1.10.0-INDEV. Physical Android play is not claimed.

The existing generated-room capture selected seed 418 with no terrain edits, lighting on and default zoom 3; water runtime checks report 8,192 phase/frame checks and zero synchronization failures. [Comparison](heroes/necromancer/comparison.png), [animated review](heroes/necromancer/animation.gif), [pose/armor sheet](heroes/necromancer/poses.png), [actual lit game](heroes/necromancer/sewers-lighting-on.png). New source art uses the built-in imagegen edit mode; [exact prompt/references](../tools/painted/hero-rigs-prompts.json) and the offline rig are committed. The rest of tests 1-51 retain the explicitly dated evidence below; CI repeats its configured suite on this release head. Exact commit and run are reported at delivery.

Completed preceding checkpoints: **v1.9.0-ashlight** at `d8c636e43f091a3162d15124417eb07241a800a3`, CI [35993938726](https://github.com/BryanHartling/grimhollow/actions/runs/35993938726); **v1.9.1-inscription-input** at `2c0cb62dca6a50c6a78490e235eeb5ee60aac2ff`, CI [35994724771](https://github.com/BryanHartling/grimhollow/actions/runs/35994724771). Both build Android, pass each class gate 10/0 and combined 30/0. Ashlight's final Linux effect p95 is 1.7073ms; inscription's is 1.1152ms. The inscription head also passes actual-pointer test 33 in landscape and portrait. Only test 45 fails: Ashlight Windows 34/82, Linux 35/82; inscription Windows and Linux 34/82. These are each run's actual sampled comparisons; no threshold was weakened.

# Inscription input - v1.9.1

The reported Enchanter failure was reproduced with real pointer dispatch: `AssertionError: Actual armor inscription click failed`. Earlier native checks invoked callbacks directly and did not cover this route. The ScrollPane now forwards completed clicks in scrolled content coordinates. Drag gestures remain owned by the controller and cannot cast.

`gradlew.bat desktop:dist android:assembleDebug core:smokeRun -PsmokeClass=ENCHANTER --no-daemon --console=plain`: **BUILD SUCCESSFUL; Runs=10 failures=0**. The existing native presentation harness now checks mouse armor selection, touch weapon selection and info, a scrolled/offset bottom glyph, drag suppression and zero-charge rejection. Landscape and portrait both report `TEST 33 POINTER ... failures=0`. Its nine-class selection/handbook and plant checks also pass. No gameplay, knowledge, charge or duration rules changed. Earlier failure output is preserved in `clean-build.log`.

The full numbered table below remains the artifact checkpoint except that test 33 now includes these stronger passing input checks and desktop/Android builds use version 1.9.1/code 950. Final commits, tags and exact-head CI results are reported at delivery.

# Ashlight Lantern - v1.9.0

Painted-source checkpoint: `370e78808`. Intended release tag: `v1.9.0-ashlight`. Exact delivered commit and CI results are reported with delivery; the unchanged terrain-distinctness failure prevents a full-green claim.

`gradlew.bat desktop:dist android:assembleDebug core:test core:smokeRun -PsmokeUpstream=true --no-daemon --console=plain`: **BUILD SUCCESSFUL; Runs=90 failures=0**. Six JUnit tests pass. Test 51 executes real feeding, passive charging, movement denial, Flare, immunity, equipment and save/load paths. Native landscape/portrait checks use the existing interface harness. The unchanged geometry/effects suite, source reconstruction and compiled network audit also pass. Actual outputs and earlier failures are retained in `clean-build.log`.

Verification caught and fixed a feed-only artifact restore-level issue and a six-button footer reflow issue. An initial barricade test was behind tall grass; its fixture was corrected without weakening line-of-sight requirements. The native feeding check handles both the landscape inventory pane and portrait bag window. The user's latest Cloak rule replaces the originally proposed partial visibility: invisibility remains absolute, with doubled charge-timer drain while the lantern is open.

CI 35952182848 at `025f562d4` passes Android, all three individual class gates (10/0 each), combined (30/0), both Ashlight UI orientations, reconstruction and rendering (Linux mean 0.9918ms / p95 1.2581ms). Only the retained test-45 contrast gate fails: Windows 34/82, Linux 31/76. A final Trinity guard prevents a temporary spirit copy granting free artifact levels; its desktop/APK build and Psychic gate pass (10/0). The long recorded build duration includes a host interruption; the quota API timed out. Exact final-head CI is checked before tagging.

The table distinguishes current coverage from retained checkpoint evidence. It does not claim a full campaign, physical Android play, or exhaustive talent/reward flows. CI repeats its configured checks on the delivered source. [Visual review](painted-world.html), [exact source prompt](../tools/painted/ashlight-prompts.json), [known issues](../KNOWN_ISSUES.md).

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Windows 1.9.0 jar launches; actual Ashlight menu, scrolling lore, free shutter, ingredient selection/feeding and Flare render in landscape and portrait. |
| 2 | PASS | desktop:dist + android:assembleDebug + core:test + all-class smoke BUILD SUCCESSFUL; six JUnit tests, zero failures/errors/skips; physical Android play not run. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Current Runs=90 failures=0 across nine heroes. Ashlight scenario executes once per class. |
| 6 | permanent known issue | Both class talent tiers are browsable for all nine heroes; representative hooks pass. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Grasp still collects heaps and activates/removes visible traps; all level 0-10 utility boundaries and unseen-target rejection pass. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | Current all-nine-class floor-6 save/load and Ashlight level/partial feeding progress/capacity/charge/shutter round trips pass. |
| 15 | PASS | Runs=90 failures=0; ten seeds per class. Necromancer, Enchanter and Psychic each 10/0 in this run; scripted descent, not a campaign. |
| 16 | permanent known issue | All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced. Exact-head CI results are reported with delivery. Prior readability CI 35949405176 has only the terrain-contrast failure. |
| 22 | PASS | aapt: com.grimhollow.dungeon versionCode=949 versionName=1.9.0-INDEV; launcher/package conventions unchanged. |
| 23 | PASS | Native renderer uses the documented Grimhollow save location under isolated user.home folders; player saves are untouched. |
| 24 | permanent known issue | Current standard 3x: heroes=9, mob sprites=122, steady idle checks=122, failures=0. Every tested enemy idle holds for 600 frames. Retained extra-2x issue: six creature sprites at 0.9545-1.0 exceed 0.95. |
| 25 | PASS | TEST 25: items=383 identification icons=60 failures=0. Two new painted Ashlight states occupy 537/538; all other 542 atlas cells are byte-identical to the prior atlas. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS locally | TEST 31: off pixel differences=0; 40 gas + 10 fire cells, 240 GPU-completed frames mean=0.6578ms p95=0.8313ms failures=0; individual fire reference max channel difference=0. CI repeats the unchanged 2ms gate. |
| 32 | PASS | Three scorch sizes, actual floor fire expiration and water/chasm rejection: failures=0. |
| 33 | PASS | Existing Enchanter trade knowledge, armor inscriptions and persistent library scenarios pass in the nine-class run. Native library coverage is retained and repeated by CI. |
| 34 | PASS | Old/future version rejection, portrait exception and deletion: failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Landscape 1280x720 and portrait 720x1061: readability checks remain green; Ashlight six-action menu, lore/rider scrolling, ingredient selection and open/closed icons fit. The artifact exposed and fixed a repeated button-layout rounding error. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS, retained checkpoint | Five generated regions: 788 walking steps, 255 turns, 75 door openings; remembered terrain checks pass at readability checkpoint. CI repeats on the artifact commit. |
| 44 | PASS | PAINTED assets=115 source sheets=103 failures=0; launcher resources=55. TEST 44 failures=0. Offline reconstruction needs no generation service. |
| 45 | permanent known issue | Fresh Windows samples fail 34/82 pairs: Sewers 11/21, Prison 1/10, Caves 8/21, City 5/15, Halls 9/15. Thresholds remain 0.12 luminance / 40 degrees hue; no palette retuning. |
| 46 | PASS | TEST 46 compiled handlers: classes=2886 guarded browser sinks=1 HTTP/socket calls=0 failures=0. Runtime handler coverage retained and repeated by CI. |
| 47 | PASS, retained checkpoint | Five regions, 120 cameras, 1393 door-transition frames; 797 known-wall observations, 177 with unknown cells below, zero black blockers. CI repeats the same gate; Ashlight separately checks extended FOV/LOS in test 51. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |
| 51 | PASS | Fourteen feed units to level 10; feed-only XP; dark-only charging; free persistent shutter; +2/3/4 sight and awareness; absolute potion/Cloak invisibility with 2x Cloak drain; every Flare tier 0–10, walls, special terrain, ally safety, actual fire damage; hostile wraith pathing; secrets exclude Mind Vision; real save/load and unchanged existing artifact weights. Native actions pass in both orientations. |

---

# Readability and repeated inspection - v1.8.1

Art-source checkpoint: `df8104413`. Corrected release tag: `v1.8.1-readability`. Exact delivered commit and CI results are reported with delivery; the enforced test-45 failure prevents a full-green claim.

The prematurely published `v1.8.0-readability` tag and [its failing CI run](https://github.com/BryanHartling/grimhollow/actions/runs/35945765921) remain available. Linux test 31 failed (mean 2.2395ms / p95 3.0285ms), in addition to test 45, although local timing passed. Halving gas alone still failed Linux p95 at 2.5816ms in run 35947743192. The final 1.8.1 patch also batches equal-alpha flame/ember particles without changing their number or simulation, or the 2ms gate. The individual-draw pixel reference differs by zero in the local run. Patch local output: `TEST 31: off pixel differences=0; 40 gas + 10 fire cells, 240 GPU-completed frames mean=0.4672ms p95=0.6894ms failures=0`.

Local commands: `gradlew.bat core:test core:smokeRun -PsmokeUpstream=true --no-daemon --console=plain`; `gradlew.bat desktop:dist android:assembleDebug core:smokeRun -PsmokeClass=PSYCHIC --no-daemon --console=plain`; native `--smoke-sewers` with `grimhollow.interfaceReview=true` in both orientations, `geometryTests=true`, `effectsTests=true`, and `recovery=true` plus `fogTests=true` across regions 0-4; `python tools/recovery_assets.py --check`; `python tools/recovery_checks.py --jar desktop/build/libs/desktop-1.8.0.jar`; `python tools/recovery_checks.py --all-regions`. Actual outputs, including intermediate failures, remain in `clean-build.log`.

Patch commands: `gradlew.bat desktop:dist android:assembleDebug --no-daemon --console=plain`, followed by the existing native geometry/effects and landscape/portrait interface checks against `desktop-1.8.1.jar`. The final UI screenshots correct the initial long prompt and sideways-door marker placement. The Crystal utility growth preserves the pre-existing sight-based reach rather than reducing it to the initially suggested four cells. No other balance/content rules changed. Two built-in imagegen sources, exact prompts and the offline packer provide 98 distinct awards plus lock/mist/drop sprites. [Visual review](painted-world.html), [source prompts](../tools/painted/readability-prompts.json), [known issues](../KNOWN_ISSUES.md).

This table combines current results above with explicitly retained checkpoint coverage for unchanged subjects. It does not claim a full campaign, exhaustive talent/reward flows or physical Android-device testing. CI repeats its configured checks on the release commit.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Windows 1.8.1 jar launches; actual game/inventory/Examine/badge windows render in landscape and portrait. |
| 2 | PASS | desktop:dist + android:assembleDebug BUILD SUCCESSFUL; existing six JUnit tests passed before the gas-only patch. Android versionCode 948, 1.8.1-INDEV; device play not run. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Runs=90 failures=0 across nine classes, plus the final corrected Psychic utility progression rerun: Runs=10 failures=0. |
| 6 | permanent known issue | Both class talent tiers are browsable for all nine heroes; representative hooks pass. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | Grasp still collects heaps and activates/removes visible traps; all level 0-10 utility boundaries and unseen-target rejection pass. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | Final all-nine-class active-state persistence and floor-6 round trips: Runs=90 failures=0. |
| 15 | PASS | Runs=90 failures=0; ten seeds per class. New-class groups each 10/0; this is scripted generation/descent, not a campaign. |
| 16 | permanent known issue | All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced. v1.8.0 additionally failed Linux test 31; corrected exact patch-head CI results and artifact links are reported with delivery. |
| 22 | PASS | Package com.grimhollow.dungeon; label Grimhollow; versionCode 948; versionName 1.8.1-INDEV; adaptive launcher entry present. |
| 23 | PASS | Native renderer uses the documented Grimhollow save location under isolated user.home folders; player saves are untouched. |
| 24 | permanent known issue | Current standard 3x: heroes=9, mob sprites=122, steady idle checks=122, failures=0. Every tested enemy idle holds for 600 frames. Retained extra-2x issue: six creature sprites at 0.9545-1.0 exceed 0.95. |
| 25 | PASS | 381 named item IDs and 60 identification overlays pass semantic/geometry checks. Ground-loot rims tested for ordinary/shop heaps and excluded for hidden loot. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS locally; CI pending | v1.8.1: off pixel differences=0; 40 gas + 10 fire cells, 240 GPU-completed frames: mean=0.4672ms p95=0.6894ms, failures=0. v1.8.0 Linux failed; final patch CI results are reported with delivery. |
| 32 | PASS | Three scorch sizes, actual floor fire expiration and water/chasm rejection: failures=0. |
| 33 | PASS | Existing Enchanter trade knowledge, armor inscriptions and persistent library scenarios pass in the nine-class run. Native library coverage is retained and repeated by CI. |
| 34 | PASS | Old/future version rejection, portrait exception and deletion: failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Native landscape 1280x720 and portrait 720x1061: repeated loot/shop/two inventory inspections; toggle/Back exit; no turns, gold, charges or movement; prompt bounds; opened-door lock removal; 98 badges, failures=0. Existing sprites, skills and plants also pass. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five generated regions: 788 walking steps, 255 turns, 75 door openings; remembered terrain checks pass. |
| 44 | PASS | PAINTED assets=115 source sheets=102 failures=0; launcher resources=55. TEST 44 failures=0. Packaged jar/APK: 230 image comparisons, mismatches=0. |
| 45 | permanent known issue | Fresh Windows samples fail 34/82 pairs: Sewers 11/21, Prison 1/10, Caves 8/21, City 5/15, Halls 9/15. Thresholds remain 0.12 luminance / 40 degrees hue; no palette retuning. |
| 46 | PASS | Compiled handlers: classes=2881 guarded browser sinks=1 HTTP/socket calls=0 failures=0. Runtime title/credits checks also passed in the generated Sewers run. |
| 47 | PASS | Five regions, 120 camera configurations, 1393 door-transition frames: failures=0. Fog remains one texel per 16 world units. Edge checks examined 797 known-wall observations (177 with unexplored cells below), zero black blockers. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | All levels 0-10: Grasp preserves sight range and adds 1/2 cells at Crystal levels 3/7, requires visibility and rejects out-of-range targets without cost; Glimpse lasts 5/7/9 turns at 0/5/10. Existing XP thresholds, upgrade exclusions and persistence pass. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |

---

# Sprouted plants and persistent inscriptions - v1.7.0

Art checkpoint: `f08e2140c`; intended release tag: `v1.7.0-botany-and-sigils`. Exact final source hash, CI results and artifacts are reported at delivery. Test 45 remains enforced; no full-green CI claim is made.

`gradlew.bat desktop:dist android:assembleDebug core:test core:smokeRun -PsmokeUpstream=true --no-daemon --console=plain` succeeded: **Runs=90 failures=0**, six JUnit tests with zero failures/errors/skips. Test 33 additionally executes starter armor inscription, glyph/weapon type rejection without spending, permanent-plus-temporary coexistence, identification order independent of the catalog, five stair transitions, old temporary-knowledge migration and save/load. The first run exposed an optional-field migration error, fixed; its failure is preserved in clean-build.log. The unusually long successful Gradle duration includes a host interruption.

`python tools/recovery_assets.py --check`: **PAINTED assets=113 source sheets=100 failures=0; launcher resources=55; TEST 44 failures=0**. Packaged jar/APK images: **226 comparisons, mismatches=0**. Android package com.grimhollow.dungeon, versionCode 946, versionName 1.7.0-INDEV; device play not run.

Native `grimhollow.presentationReview=true` passed landscape and portrait: each exercised nine selections, 36 handbook pages and Duelist Start, then **13 sprouted plants, two actual armor-inscription clicks, all 13 armor glyphs and a moved/scrolled library, failures=0**. Native `grimhollow.geometryTests=true` passed, including **113 unique skill icons and 13 plant visuals**, exact plant cell indices and 16-unit logical size. All 122 steady idle checks and 381 item identities still pass. The historical extra-2x occupancy failures remain documented.

The complete 1-50 checkpoint table below retains its limitations and retired subjects. Tests 1/2/5/9-15/22/24-27/33/34/36/37/44/48-50 have the current local results above; test 24 still carries its historical extra-2x limitation. Other configured checks run on exact-head CI. Tests 6/7/8/16/19/21/38/45 remain known issues for the documented reasons.

Starter armor glyphs: Obfuscation, Swiftness and Viscosity. Learned sigils persist; Deep Knowledge discovers new effects only on first visiting a floor. Rune Etching rerolls its active attached effect. No enemy-curse casting, plant mechanics or other combat balance changes were added. [Current visual review](painted-world.html); [source prompts](../tools/painted/botany-skills.json).

---

# Painted heroes, quiet enemies and traps - v1.6.0

Art checkpoint: `c6248dbbf`; release tag: `v1.6.0-painted-heroes`. Exact delivered source hash, CI results and artifact links are reported with delivery. CI still enforces the outstanding terrain-contrast gate; this release makes no full-green claim.

Eleven new built-in imagegen sources supply nine original class paintings with matching face crops, seven trap mechanisms and the desktop/Android launcher emblem. All outputs compile from committed inputs; no AI or Blender runs in CI. The class handbook shows Profile, Growth, Paths and Armor immediately. Duelist is selectable; the other existing class unlocks still control Start, while all nine classes can be previewed. Enemies hold a steady resting pose; movement, combat, status logic and death callbacks retain their behavior.

Local commands: `gradlew.bat desktop:dist android:assembleDebug core:test --no-daemon --console=plain`; `gradlew.bat core:smokeRun -PsmokeUpstream=true --no-daemon --console=plain` returned **Runs=90 failures=0**. The three new-class groups each passed ten runs in that command; final CI runs each alone and combined. `python tools/recovery_assets.py --check` passed. Native `--smoke-sewers` with `grimhollow.presentationReview=true` ran both desktop and portrait layouts; `grimhollow.geometryTests=true` passed the exact new idle/portrait/trap contracts and the retained geometry gates. Failures found and corrected during implementation are preserved in the existing build log.

Live encounter check: **150 actions, 117 steps, 32 attacks, actor drops/pickup and summon exercised, 120 death frames, failures=0**. This validates that steady idle poses do not stall action callbacks.

Windows shortcut: `C:/Users/Hartl/OneDrive/Desktop/Grimhollow.lnk`, verified to target `tools/play.bat` in this checkout with the new Windows icon. Android builds include main/debug legacy, adaptive and monochrome launcher resources; actual Android-device play remains unrun.

Rendered evidence: [class selection and handbook review](painted-world.html), [landscape](interface/landscape/), [portrait](interface/portrait/). Tests described as retained below were not independently repeated locally in this presentation pass; final CI repeats its configured checks. Class/content checks are scripted tests, not a completed player campaign.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Windows 1.6.0 jar launches; native title, nine class selections and a real Duelist Start/Continue reach the dungeon. |
| 2 | PASS | desktop:dist + android:assembleDebug BUILD SUCCESSFUL; six existing JUnit tests, zero failures/errors/skips. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | All nine hero kits: Runs=90 failures=0; ten seeds per class. |
| 6 | permanent known issue | Both class talent tiers are browsable for all nine heroes; representative hooks pass. Exhaustive in-run talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | All 18 subclass previews and their tier-three skills are available. Actual Tengu reward selection remains unrun. |
| 8 | permanent known issue | All 27 armor-ability previews and tier-four skills are available. Actual crown reward flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | TESTS 11-13 PASS: Grasp heap/trap/empty-cell cases. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | Final all-nine-class active-state persistence and floor-6 round trips: Runs=90 failures=0. |
| 15 | PASS | Runs=90 failures=0; ten seeds per class. New-class groups each 10/0; this is scripted generation/descent, not a campaign. |
| 16 | permanent known issue | All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Recovery and the later painted-source direction supersede the old procedural style gate; source reconstruction is test 44. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | The unchanged test-45 contrast gate remains enforced. Exact release-head CI results and artifact links are reported with delivery. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; versionCode 945; versionName 1.6.0-INDEV; adaptive launcher entry present. |
| 23 | PASS | Native renderer uses the documented Grimhollow save location under isolated user.home folders; player saves are untouched. |
| 24 | permanent known issue | Current standard 3x: heroes=9, mob sprites=122, steady idle checks=122, failures=0. Every tested enemy idle holds for 600 frames. Retained extra-2x issue: six creature sprites at 0.9545-1.0 exceed 0.95. |
| 25 | PASS | Current GPU check: 381 named items, 60 identification icons and all 63 trap shape/color/inactive combinations, failures=0. Trap UVs assert the exact half-texel inset and 16-unit logical dimensions. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Retained effects on/off and GPU timing coverage; effect code unchanged. Final CI repeats the existing effects checks. |
| 32 | PASS | Retained floor-fire expiration and scorch placement result; no effect code changed. CI reruns the unchanged checks. |
| 33 | PASS | Level-one Inscribe offers Blazing/Shocking/Chilling with an empty discovery catalog, leaving it unchanged; no starting Enchantment scroll; existing Runecraft cases pass. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | In each orientation: classes=9, matchingPortraits=9, firstSelections=9, secondSelections=9, infoButtons=9, handbookPages=36, duelistStart=true, trapShapes=7, failures=0. All talent/ItemSlot checks also pass. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Retained five-region remembered-terrain/walking checks; world and fog code unchanged. Final CI reruns the walking and door checks. |
| 44 | PASS | PAINTED assets=112 source sheets=96 failures=0; launcher resources=55; recovery provenance failures=0. Packaged jar/APK painted images: 224 comparisons, zero mismatches. |
| 45 | permanent known issue | Unchanged thresholds; retained Windows captures fail 26/82 pairs: Sewers 7/21, Prison 1/10, Caves 8/21, City 3/15, Halls 7/15. Final CI separately measures newly rendered rooms. |
| 46 | PASS | Compiled audit: classes=2876, guarded browser sinks=1, HTTP/socket calls=0, failures=0. Runtime title/credits handler coverage is retained and rerun in CI. |
| 47 | PASS | Retained all-five-region/120-camera alignment gate: fog texel/cell=1:1 at 16 world units. No fog or lighting-quad changes; CI reruns it. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | 12 spent charges -> level 1 + 2 XP; 300 idle turns -> no growth; all ten thresholds, upgrade exclusions and persistence. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |

---

# Psychic and painted interface - v1.5.0

Mechanics checkpoint: `1f7152c1cd7a795b721dfddd527fd7245503e4ff`, committed and pushed. Release tag: `v1.5.0-psychic-and-interface`. Exact delivered source hash and CI results are in the delivery response. The full workflow remains subject to the unchanged failing terrain-contrast gate.

The requested Psychic upgrade floor, spending-based Crystal levels, Push/Hurl tiers and Puppeteer control now execute in the existing smoke harness. Enchanter starts with three inscription choices and three Brush charges. The reported glowing Rogue rat is consistent with an existing curse-bound variant; inspection now explains it, but the exact encounter was unavailable. Level-six chasm removal remains as requested; deterministic checks are not a full-campaign balance test.

The presentation pass completes all 381 named item IDs (380 distinct cells) at 64px and adds painted shared frames, equipped-slot borders, status bars, 32 navigation symbols and painted bag tabs. Long item descriptions scroll with their action buttons visible; the class spell wheel adapts to the available screen. Sources and prompts are committed and packed offline. World, creature and title paintings retain their previous pixels. Original class splashes, identification overlays, talent/region/credit icons and special-room art remain.

Local verification: all-nine-class smoke `Runs=90 failures=0`; final desktop/Android builds and six JUnit tests pass. New-class groups each passed 10/0 within the all-nine command; the separate Psychic-only mechanics command also passed 10/0. The earlier mechanics checkpoint's CI independently passed Necromancer, Enchanter and Psychic 10/0 and combined 30/0. Exact final-head class gates are reported with delivery.

Native interface evidence lives in `verification/interface/landscape` and `portrait`, using isolated saves and the actual desktop and mobile layouts. During review, the tab icon reset and item-scroll camera alignment were corrected. The first portrait configuration inherited fullscreen; the fixture now requires a tall surface. An additional 2x sprite run exposed six retained creature occupancy failures; they remain explicitly recorded below and in KNOWN_ISSUES.md. The independent portrait UI gate passed at 720x1061, and the desktop UI gate passed at 1280x720, including tab scale and moved scroll-camera alignment. Those UI checks are separate from the sprite-size check. No acceptance threshold was lowered.

The initial final build caught two fixture errors (protected Group traversal and an exotic scroll package path), corrected before delivery. Existing logs retain failed and successful outputs. An unrelated CI SDK setup failure requested the obsolete `tools` package; the workflow now requests `platform-tools` explicitly and retains the compile-SDK/build-tools installation and every acceptance step.

Rows marked retained are checkpoint evidence, not claims of an additional local rerun. The existing CI repeats world/fog/effects/provenance/menu checks on the delivered source.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Current runnable jar and TitleScene launch on Windows; fresh-checkout result retained. |
| 2 | PASS | Final desktop release + Android debug + six JUnit tests: BUILD SUCCESSFUL in 1m; zero JUnit failures/errors/skips. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | All nine hero kits: Runs=90 failures=0; ten seeds per class. |
| 6 | permanent known issue | Representative hooks pass; exhaustive talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | Subclass hooks pass; actual Tengu reward selection flow remains unrun. |
| 8 | permanent known issue | Nine armor abilities execute; actual crown selection flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | TESTS 11-13 PASS: Grasp heap/trap/empty-cell cases. |
| 12 | PASS | Hero levels 1/7/8/16/24/30 -> +1/+2/+2/+3/+5/+5; damage, durability, strength, descriptions and no stacking. |
| 13 | PASS | Old Amok behavior and new level-six/level-eight control paths; see test 50. |
| 14 | PASS | Final all-nine-class active-state persistence and floor-6 round trips: Runs=90 failures=0. |
| 15 | PASS | Runs=90 failures=0; ten seeds per class. New-class groups each 10/0; this is scripted generation/descent, not a campaign. |
| 16 | permanent known issue | All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Superseded by recovery: generated style requirements do not apply to restored upstream character/world pixels. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Test 45 remains enforced and fails 26/82 local pairs. Exact release-head CI is reported with delivery; no full-green claim. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; versionCode 944; versionName 1.5.0-INDEV. |
| 23 | PASS | Native renderer uses the documented Grimhollow save location under isolated user.home folders; player saves are untouched. |
| 24 | permanent known issue | Standard 3x: nine heroes, 122 creature sprites, failures=0. Extra 2x render: six retained sprites exceed the 0.95 occupancy ceiling, measured 0.9545-1.0; threshold unchanged. |
| 25 | PASS | All 381 named item IDs now use painted 64px cells; 60 identification overlays remain 32px. GPU identities, all section-9/class items and ItemSlot sizing: failures=0 at both 2x and 3x. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Retained v1.4.0 effects evidence: off differences=0; 240 GPU-completed frames mean=0.6134ms p95=0.7963ms; CI reruns the unchanged checks. |
| 32 | PASS | Retained floor-fire expiration and scorch placement result; no effect code changed. CI reruns the unchanged checks. |
| 33 | PASS | Level-one Inscribe offers Blazing/Shocking/Chilling with an empty discovery catalog, leaving it unchanged; no starting Enchantment scroll; existing Runecraft cases pass. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | Nine splashes/descriptions/portraits, all talents and ItemSlots, 178 status draws: failures=0. Native UI also checks visible windows, tab scale and moved scroll-camera alignment. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Retained five-region walking evidence: 788 steps, 255 turns, 75 door openings, failures=0; world rendering unchanged, CI reruns it. |
| 44 | PASS | 102 images from 85 committed source sheets pack successfully. Initial full reconstruction check passed; final HUD layout is packed by the same compiler and CI verifies exact reconstruction. JAR/APK assets: 970 comparisons, zero mismatches. |
| 45 | permanent known issue | Thresholds unchanged: 26/82 pairs fail. Sewers 7/21, Prison 1/10, Caves 8/21, City 3/15, Halls 7/15. Fresh Sewers room includes a trap type, so not a controlled comparison with prior 25/76. |
| 46 | PASS | Final compiled audit: classes=2885, guarded browser sinks=1, HTTP/socket calls=0, failures=0. Native title/settings screens render; runtime handler coverage is retained and rerun by CI. |
| 47 | PASS | Retained five-region fog evidence: 120 camera configurations; fogTexel/cell=1:1, worldUnits=16, lightQuad aligned. No fog/camera shader code changed; CI reruns all five regions. |
| 48 | PASS | Push/Hurl exact movement and every collision/trap/chasm/boss rider at Crystal levels 0-10. |
| 49 | PASS | 12 spent charges -> level 1 + 2 XP; 300 idle turns -> no growth; all ten thresholds, upgrade exclusions and persistence. |
| 50 | PASS | Level-six direction/follow/attacks; level-eight 15-turn survival, permanent single-floor slot, replacement release, save/load, stairs, normal kill ownership and boss immunity. |

---

# Psychic and Enchanter playtest balance checkpoint

Windows gate: `gradlew.bat :core:test :core:smokeRun -PsmokeUpstream=true :desktop:release :android:assembleDebug --no-daemon --console=plain` -> `BUILD SUCCESSFUL`; `Runs=90 failures=0`. A separate Psychic-only rerun also passed `Runs=10 failures=0`. The earlier full run found a knockback boundary bug and a headless fixture that did not finish attack animation after movement; both were corrected before this passing run. All other acceptance results below are retained checkpoint evidence until the interface pass is verified.

- Test 12: PASS, hero levels 1/7/8/16/24/30 yield effective upgrades +1/+2/+2/+3/+5/+5; missile and coated-dart damage, strength and durability match real upgrades without stacking, with descriptions checked.
- Test 33: PASS, level-one Inscribe offers Blazing/Shocking/Chilling with an empty discovery catalog and leaves that catalog unchanged; no starting Enchantment scroll.
- Test 48: PASS, Push and Hurl at every Crystal level 0-10: exact distances, no direct damage, walls, occupied cells, collision statuses, hidden traps, first chasm edge and boss rules.
- Test 49: PASS, 12 charges spent produces level 1 + 2 XP; 300 idle turns produce no XP; cumulative thresholds 10/25/45/70/100/135/175/220/270/325; external upgrading rejected; saved XP/level restored.
- Test 50: PASS, level-six direction/follow/attacks and 15-turn expiry; level-eight permanent single-floor control, replacement release, save/load, ascent/descent, normal kill ownership and no boss control.

Crystal level-six chasm removal remains on its requested track; deterministic mechanic checks are not evidence of full-campaign encounter balance. The glowing Rogue rat could have been an existing curse-bound variant; the exact player encounter was unavailable. Its visible identity and inspection explanation now distinguish the trait from an unexplained debuff.

---

# Painted bestiary and text repair - v1.4.0

Source commits: `9db9fe525` (wand/text correction) and `99a1f4371` (painted creatures, status icons, display scale, torch materials and verification). Delivery adds current evidence and documentation. Release tag: `v1.4.0-painted-bestiary`. The initial HTTP 401 and replacement-token HTTP 403 publication failures are resolved: after the user updated repository permissions, authenticated fetch and a GitHub push dry run both succeeded for the branch and tag. Credentials remain process-local. The previous attempts remain recorded in ci-status.log; publication and exact delivered-head CI results are reported with delivery.

All 71 creature atlases now contain 129 authored forms, including friendly skeletons/ghouls, Sad Ghost, all NPCs, mimic families, sentries, ward tiers, statue armor tiers, rare/quest variants and Vault encounters. Shared hero reflections and ghoul-derived revenants also use painted sheets. Nine heroes remain painted and display 25% taller; smaller wildlife and larger monsters have species proportions. Movement, collision, combat timings, balance, content and saves are unchanged. Four chest items use their matching mimic closed art, including the ordinary mimic's occasional twitch. 86 buff emblems and three overhead symbols retain their logical HUD sizes and runtime tint. Approved torch fixture pixels now sit over each region's current masonry.

The reported uncursed Blast Wave was an uncursed **WandOfGravity** in the copied player save. Its missing message namespace caused superclass name/description fallback and a mismatched format string. Correcting 49 message prefixes fixes added item, curse, mob, trap and settings text. The existing smoke suite now checks ten named item descriptions and nine curse/enemy/trap descriptions. Actual output: `PASS WANDS: Gravity pulls 2; collision gives Vertigo; Blast Wave pushes 3 without curse; distinct names and formatted descriptions`. No wand mechanics were changed.

Final local verification: Windows desktop and Android builds pass; six JUnit tests pass; all nine hero groups pass `Runs=90 failures=0`. This contains Necromancer 10/0, Enchanter 10/0 and Psychic 10/0; they are groups of the all-nine invocation, not additional local class-only commands. The unchanged CI defines all three separately and the combined three-class gate; exact delivered-head results are reported with delivery.

Native crash regression: the private copy of the player's Necromancer save completed 150 actions, 137 movement steps and 12 attack actions. Fresh seed 417 completed 150 actions, 122 movement steps and 27 attack actions. Both exercised actor-thread drops/pickup/summoning and 120 frames after forced death, failures=0. The opt-in fixture grants 1000 health for coverage. Original player saves were untouched. The real Vault renderer passed arena trigger, FIRE/FROST/SHOCK forms, scripted boss death and treasure-door unlock; this is not a played boss fight.

The GPU audit first exposed an uncovered Vault DM-200 row and poor sentry/pylon source packing. A complete Vault source and isolated pylon poses now pack all referenced frames; stationary actors fit only the poses they use. Tiny foreign particles are excluded during source connectivity extraction. The final check draws 122 creature sprite types, including nested NPC/ability sprites, and checks their declared animation rectangles against the painted manifest. Initial failures remain in the logs.

Test 43 then sampled green movement-help text over an unseen world cell at screen (144,135), RGBA 000400FF, after the enlarged hero shifted camera framing. The fog itself passed test 47. The corrected test draws the real world before/after fog separately from the HUD, as test 47 does; it retains exact-black/remembered-pixel assertions and now includes formerly excluded screen margins. The final five-region walking/fog run passes. No game fog code changed.

**The existing numeric contrast gate 45 still fails 26/82 local comparisons and remains enforced in CI.** Source reconstruction passes for 99 images from 75 committed source sheets. CI needs no art generator or Blender. The full local evidence is appended to clean-build.log, art-validation.log, new-class-smoke.log, reproducibility.log, junit-summary.log and apk-identity.log. The incidental retired test-41 output from the review screenshot runner does not reactivate that gate.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Retained fresh desktop-only checkout result; current TitleScene and final runnable jar launch verified locally. |
| 2 | PASS | desktop:dist core:test android:assembleDebug --no-daemon: BUILD SUCCESSFUL in 1m 46s. Final desktop fixture rebuild: BUILD SUCCESSFUL in 22s. JUnit 6 tests, zero failures/errors/skips. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | Final core:smokeRun -PsmokeUpstream=true --no-daemon: Runs=90 failures=0; all nine hero kits. |
| 6 | permanent known issue | Representative hooks pass; exhaustive talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | Subclass hooks pass; actual Tengu reward selection flow remains unrun. |
| 8 | permanent known issue | Nine armor abilities execute; actual crown selection flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | TESTS 11-13 PASS: Grasp heap/trap/empty-cell cases. |
| 12 | PASS | TESTS 11-13 PASS: thrown damage at the required levels. |
| 13 | PASS | TESTS 11-13 PASS: domination retargeting and duration. |
| 14 | PASS | Final all-nine-class active-state persistence and floor-6 round trips: Runs=90 failures=0. |
| 15 | PASS | Runs=90 failures=0; ten seeds per class. New-class groups each 10/0; this is scripted generation/descent, not a campaign. |
| 16 | permanent known issue | All 122 concrete creature sprite draws, declared animation rectangles, statue tiers, items and talents checked; exhaustive every-gameplay-path coverage remains unrun. |
| 17 | RETIRED | Superseded by recovery: generated style requirements do not apply to restored upstream character/world pixels. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | Unchanged test 45 fails 26/82 locally and remains enforced in CI. Desktop and Android packages build; exact delivered-head CI results are reported with delivery. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; versionCode 943; versionName 1.4.0-INDEV. |
| 23 | PASS | PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow. |
| 24 | PASS | TEST 24: heroes=9 mob sprites=122 failures=0; all creature animation references painted. Hero/rat height ratio=2.2222223. Intentional species display heights; same 0.85-0.95 occupancy band. |
| 25 | PASS | TEST 25: 381 named item IDs + 60 identification icons, Waterskin=480, MindVision=eye@98 (ID82), all eleven section-9 and ten class items; failures=0. Four chest cells now share their mimic source. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | TEST 31: off pixel differences=0; 40 gas + 10 fire cells; 240 GPU-completed frames mean=0.6134ms p95=0.7963ms failures=0. |
| 32 | PASS | TEST 32: three scorch sizes, actual floor fire expiration, water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 PASS: scroll/stone offers, exclusions, cancel/apply, subclasses and history persistence. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | TEST 36: nine splashes, descriptions, portraits, all talents and ItemSlots failures=0; 178 large/small painted status draws preserve logical sizes. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five final regions: 788 steps, 255 turns, 75 door openings; exact remembered-terrain UV and world/fog pixel comparisons failures=0. |
| 44 | PASS | PAINTED assets=99 source sheets=75 failures=0; upstream-derived sheets=7, restored assets=29, painted replacements=99; failures=0. All 484 packaged assets match JAR and APK bytes (968 comparisons, zero mismatches). |
| 45 | permanent known issue | Thresholds unchanged: 26/82 pairs fail. Sewers 7/21, Prison 1/10, Caves 8/21, City 3/15, Halls 7/15. Fresh Sewers room includes a trap type, so not a controlled comparison with prior 25/76. |
| 46 | PASS | Runtime title controls=5, credits handlers=4, external opens=0, scene fetches=0. Final compiled classes=2877, guarded browser sinks=1, HTTP/socket calls=0, failures=0. |
| 47 | PASS | Final five-region run: 120 camera configurations, 212316160 hidden and 8888320 visible pixels, 1398 door-transition frames; fogTexel/cell=1:1 worldUnits=16 lightQuad=aligned failures=0. |

Current final Windows floor/wall measurements (delta luminance >= 0.12 OR circular mean hue >= 40 degrees):

| Region | Delta luminance | Delta hue | Failing pairs |
|---|---|---|---|
| Sewers | 0.1842 | 4.90 degrees | 7/21 |
| Prison | 0.2876 | 12.48 degrees | 1/10 |
| Caves | 0.1656 | 2.42 degrees | 8/21 |
| City | 0.1375 | 36.49 degrees | 3/15 |
| Halls | 0.1177 | 23.48 degrees | 7/15 |

Human art/readability review, a complete campaign and Android device testing remain outstanding. This completes the requested bestiary, proportion, wall-fixture, text and status presentation pass.

---

# Native crash repair - v1.3.2

The reported shutdown was reproduced on a private copy of the player's Necromancer save (seed 5039256467331). The unfixed renderer aborted in `SHPD Actor Thread`: `No context is current`, through `Texture.bind -> SmartTexture.filter -> ItemSprite.frame -> Level.drop`. This is a native OpenGL abort, not a game-over or a Java gameplay exception. The user's original save files were left untouched.

The fix defers texture filtering until the rendering thread binds the texture. It covers all existing SmartTexture callers and preserves requested filters across texture recreation. Shipping artwork, gameplay, balance and save formats are unchanged.

Actual checks run for this repair:

- Windows desktop and Android debug: `desktop:dist core:test android:assembleDebug core:smokeRun -PsmokeClass=NECROMANCER --no-daemon`; BUILD SUCCESSFUL in 2m 2s.
- JUnit: six tests, zero failures/errors/skips, including actor-thread filter requests, render-thread GPU application and texture reload.
- Necromancer class-only headless: `Runs=10 failures=0`.
- Copied saved-level renderer: 150 actions, 128 movement steps, 21 attack actions; actor-thread item drops/pickup and skeleton summon; 120 frames rendered after forced death; failures=0. This opt-in fixture grants 1000 health to extend coverage; it is not a campaign/balance playtest.
- Fresh ordinary dungeon (seed 417): 150 actions, 126 movement steps, 24 attack actions; actor drops/pickup/summon and 120 death frames; failures=0.
- Final renderer geometry: nine heroes, 114 monster sprites, 381 item IDs, 60 icons; tests 24-26, 34 and 36 failures=0.
- Final desktop/JAR and Android/APK: all 484 packaged assets match source bytes, mismatches=0. No artwork changed.
- Compiled menu/network audit: classes=2877, guarded browser sinks=1, HTTP/socket calls=0, failures=0.

The existing desktop fixture now retains hostile AI for this regression and is included in Linux CI. Native failure output and follow-up validation are appended to clean-build.log. Initial fresh-settings replays failed their combat-coverage assertion because first-launch settings created the sealed tutorial room, where search is disabled. The encounter fixture now disables the tutorial in its isolated settings before generating a normal level; it also permits normal item pickups/search input. No acceptance threshold was lowered.

Tests 1-47 retain the full status table below, except that test 2 now builds version 1.3.2/code 942, the JUnit count is six, and the current Necromancer-only result is 10/0. Other historical checks are retained results unless explicitly rerun above. Test 45 remains an enforced known failure; no art was changed to address it. Exact final-tag CI is reported with delivery.

---

# Living dungeon acceptance - v1.3.1

The v1.3.1 follow-up anchors health bars and status icons to visible standing-body bounds. Its final build, all 114 monster/nine hero geometry checks, item/placement/menu gates, actual title/monster screenshots and compiled handler audit pass. Art is unchanged from `v1.3.0-living-dungeon` (`5229c1f526821fd9ad414c4873532702e8f70fa4`); the complete local five-region walking/fog/effects run and contrast measurements below were recorded at that checkpoint and are retained. Exact-tag CI repeats those checks.

Runtime/art source checkpoint: `d9190cd097bf5dc01fcf1f2a4ff745d4c8aa107b`. Door/vegetation checkpoint: `5ba2a96aa`; title checkpoint: `08d2ed2aa`. This pass adds coherent door transitions, tall/parted/flattened vegetation, an animated painted crypt title, and 40 monster forms/states across 32 atlas families. Gameplay, balance, content, logical geometry, camera, input and existing combat animation timings/callbacks remain unchanged.

Desktop and Android builds pass; final JUnit tests=5 failures=0 errors=0 skipped=0. The all-nine-hero smoke command ran 90/0 with the same gameplay sources, before the final graphics-only UV correction. The final actual renderer passes fog, walking, door transitions, all 114 monster sprites, all nine heroes, all 381 items/60 icons, effects and menu handlers. **The unchanged contrast gate 45 fails 25/76 local pairs.** This is not a green full-workflow or complete human-playability claim.

The first GPU check found five sprites sampling a strip of the next atlas row. A UV-only inset removed the strip but slightly stretched the image. The final fix pairs the half-texel UV inset with an equal mesh inset, keeping the public frame rectangle and pixel-to-world scale. All 114 sprites then passed the original band. Both intermediate failures remain in clean-build.log; no check was weakened or added.

The local 90-run command includes Necromancer 10/0, Enchanter 10/0 and Psychic 10/0 as groups, not additional class-only invocations. The unchanged CI also runs each class separately and the combined three-class gate; exact-tag results are supplied with delivery.

Test 44 reconstructs 58 shipping images from 45 committed source sheets and verifies remaining restoration pixels. It retains the 46 unaffected upstream-derived character sheets; all untouched rectangles within a painted shared monster atlas come from the pinned v1.2.0 tree. Test 25 preserves the prior named item contract. Retired art-loop gates remain retired; incidental test-41 output from the reused screenshot fixture is not an active liquid gate.

The updated `painted-world.html` links the title, monster and terrain boards and real lit-room captures. The title animates in the running game; its screenshot is a static GPU capture, not a recording. NPCs, remaining rare/quest creature frames, class splashes, some inventory families, UI/talent icons, approved wall torches and retained special-room artwork are still on their prior art. This pass does not replace every image or certify a complete campaign or Android-device playtest.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Retained v1.0.2 fresh desktop-only checkout result; not repeated locally in this art pass. Current title and real renderer launch pass. |
| 2 | PASS | desktop:dist core:test android:assembleDebug core:smokeRun -PsmokeUpstream=true --no-daemon: BUILD SUCCESSFUL in 1m 37s; Runs=90 failures=0. Final v1.3.1 body-bound layout rebuild: BUILD SUCCESSFUL in 1m 24s; JUnit tests=5 failures=0 errors=0 skipped=0. |
| 3 | RETIRED | Old procedural-art rebuild no longer ships; committed painted sources reconstruct exactly under test 44. |
| 4 | RETIRED | Old generated-style validator was superseded by source provenance/reconstruction 44 and room distinctness 45. |
| 5 | PASS | All nine hero kits; ten seeds each; Runs=90 failures=0. |
| 6 | permanent known issue | Representative hooks pass; exhaustive talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | Subclass hooks pass; actual Tengu reward selection flow remains unrun. |
| 8 | permanent known issue | Nine armor abilities execute; actual crown selection flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | TESTS 11-13 PASS: Grasp heap/trap/empty-cell cases. |
| 12 | PASS | TESTS 11-13 PASS: thrown damage at the required levels. |
| 13 | PASS | TESTS 11-13 PASS: domination retargeting and duration. |
| 14 | PASS | All nine classes: active-state save/load and floor-6 round trips; Runs=90 failures=0. |
| 15 | PASS | Runs=90 failures=0; includes ten runs of each new class; scripted generation/descent, not a played campaign. |
| 16 | permanent known issue | Representative sprite/item/talent coverage passes; exhaustive every-code-path frame audit remains unrun. |
| 17 | RETIRED | Superseded by recovery: generated style requirements do not apply to restored upstream character/world pixels. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Retained v1.0.2 SDK-unset desktop-only build result; build configuration unchanged except version metadata. New CI uses desktopOnly=true. |
| 21 | permanent known issue | The unchanged contrast gate 45 still fails (25/76 locally). Desktop and Android packages build. Exact delivered tag CI is reported separately; no workflow step or threshold changed. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; versionCode 941; versionName 1.3.1-INDEV. |
| 23 | PASS | PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow. |
| 24 | PASS | TEST 24: heroes=9 mob sprites=114 failures=0. Native 0.85-0.95 height band retained after paired half-texel UV/mesh guards removed adjacent-frame bleed. |
| 25 | PASS | TEST 25: 381 named IDs + 60 icons; Waterskin=480, MindVision=eye@98 (ID 82), eleven section-9 and ten class items; failures=0. Prior 202 painted item identities/pixels unchanged. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | TEST 31: off pixel differences=0; 40 gas + 10 fire cells; 240 GPU-completed frames mean=0.6219ms, p95=0.8760ms; failures=0. |
| 32 | PASS | Three scorch sizes; actual floor fire expiry; water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 PASS: scroll/stone offers, exclusions, cancel/apply, subclasses and history persistence. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | TEST 36: nine splashes, descriptions, portraits, all talents and ItemSlots failures=0. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Retained v1.3.0 local five-region run; 788 walking steps, 254 turns, 75 door openings; failures=0. Exact remembered-terrain UV equality remains enforced. |
| 44 | PASS | PAINTED assets=58 source sheets=45 failures=0; upstream-derived character sheets=46; restored assets=68; verified painted replacements=58; failures=0. All 484 packaged assets match final JAR and APK bytes. |
| 45 | permanent known issue | Unchanged thresholds fail 25/76 local pairs: Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 8/15. Actual metrics and captures retained; previous continuation was 30/76. |
| 46 | PASS | Runtime title controls=5, credits handlers=4, external opens=0, scene fetches=0; compiled classes=2875, guarded browser sink=1, HTTP/socket calls=0, failures=0. Final v1.3.1 compiled audit and actual title-handler checks both pass. |
| 47 | PASS | Retained v1.3.0 local five-region run: 120 camera configurations, 212316160 hidden and 8888320 visible pixels, 1408 door-transition frames; fogTexel/cell=1:1, worldUnits=16, lightQuad=aligned, failures=0. |

Current Windows floor/wall measurements (delta L >= 0.12 OR circular mean hue >= 40 degrees):

| Region | Delta L | Delta hue | Failing pairs |
|---|---|---|---|
| Sewers | 0.1965 | 3.70 degrees | 5/15 |
| Prison | 0.2869 | 12.69 degrees | 1/10 |
| Caves | 0.1642 | 2.28 degrees | 8/21 |
| City | 0.1386 | 38.10 degrees | 3/15 |
| Halls | 0.1179 | 23.05 degrees | 8/15 |

Human readability and animation review remain necessary. No art was altered to chase these room statistics.

---

## Historical fogfix checkpoint

# Rendering fix acceptance - v1.0.2

Runtime/test source checkpoint: `bcee035c2`. Tests 25, 43 and 47 pass locally; the unchanged numeric contrast gate 45 still fails and remains enforced in CI. The exact delivered commit, tag and CI result appear in the delivery response. No gameplay, balance, content or shipping asset bytes changed. No artgen/Blender loop ran.

Test 47 first failed on the unmodified recovery renderer: never-seen cell 229 at zoom 1/pan [32,32] contained nonblack edge pixels (0x010101, 0x030303, 0x050505). Fog used linear filtering across cell boundaries. Separately, the custom wall-light program cached a mutable camera by identity; matrix changes now invalidate that cache. Fog has exactly one nearest-sampled texel per 16-unit cell. The light-map quad retains the correct level bounds and its sample density is independent of texture art resolution.

Test 47 reads actual GPU pixels for every visible/never-seen cell, including cell edges and visible walls, over both an opaque witness background and the actual lit world. It changes the same camera object's zoom/pan, reads the uploaded wall-light camera matrix, checks light-map world bounds, repeats after normal walking, and checks intermediate door movement frames. It changes no level terrain or FOV flags. Test 43's existing remembered-terrain checks remain active.

The item atlas was not reverted in v1.0.1 and all 381 IDs match its committed catalog. Mind Vision's old identification cell contains a ring due to the historical name classifier; the film now reuses the existing eye at cell 98 while ID 82 remains stable. Waterskin already points to its bag at 480: no index mismatch was reproduced, and its art remains unchanged for human review. Test 25 pins all named cells to existing pixels and checks actual classes, all eleven section 9 items (including both leather variants), and the ten new-class items. Potion bottle colours remain randomized by their saved identification handler.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Fresh clone bcee035c2: RENDERED=TitleScene from desktop-1.0.2.jar; Android SDK environment unset. |
| 2 | PASS | desktop:dist core:test android:assembleDebug core:smokeRun: BUILD SUCCESSFUL in 1m34s; APK code 919. |
| 3 | RETIRED | Superseded by recovery: generated-art rebuild prohibited; source-pixel restoration is checked by 44. |
| 4 | RETIRED | Superseded by recovery: generated style validator replaced by restored-pixel 44 and room 45. |
| 5 | PASS | All nine kits execute; ten seeds per hero; Runs=90 failures=0. |
| 6 | permanent known issue | Representative hooks pass; exhaustive talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | Subclass hooks pass; actual Tengu reward selection flow remains unrun. |
| 8 | permanent known issue | Nine armor abilities execute; actual crown selection flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | TESTS 11-13 PASS: Grasp heap/trap/empty-cell cases. |
| 12 | PASS | TESTS 11-13 PASS: thrown damage at the required levels. |
| 13 | PASS | TESTS 11-13 PASS: domination retargeting and duration. |
| 14 | PASS | All nine classes: active-state save/load and floor-6 round trips; 90/0. |
| 15 | PASS | Runs=90 failures=0; includes all three new classes with ten seeds each; generation/debug descent, not player combat. |
| 16 | permanent known issue | Representative sprite/item/talent coverage passes; exhaustive every-code-path frame audit remains unrun. |
| 17 | RETIRED | Superseded by recovery: generated style requirements do not apply to restored upstream character/world pixels. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Fresh desktop-only clone bcee035c2 builds in 1m46s with ANDROID_HOME/ANDROID_SDK_ROOT unset; title launches. |
| 21 | permanent known issue | Test 45 remains active and failing; exact remote CI status and run URL are in the delivery response. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; versionCode 919; versionName 1.0.2-INDEV. |
| 23 | PASS | PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow. |
| 24 | PASS | TEST 24: heroes=9 mob sprites=114 failures=0. |
| 25 | PASS | 381 named item IDs and 60 icons match pinned RGBA references; every section 9 item and class item maps correctly; Mind Vision reuses eye cell 98. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Off pixel differences=0; 240 GPU-completed frames mean=0.7373ms, p95=1.0453ms. |
| 32 | PASS | Three scorch sizes; actual floor fire expiry; water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 PASS: scroll/stone offers, exclusions, cancel/apply, subclasses and history persistence. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | TEST 36: nine upstream splashes, descriptions, portraits, talent icons and ItemSlots failures=0. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five generated regions, seed 417: 788 normal moves, 254 turns, 75 opened doors; failures=0. |
| 44 | PASS | 87 upstream-derived character sheets; 122 restored assets; failures=0. All 481 shipping asset files match both jar and APK; zero asset changes. |
| 45 | permanent known issue | FAIL after fog/camera fix: Sewers 9/15; Prison 5/10; Caves 14/21; City 5/15; Halls 14/15. Total 47/76. No threshold or art changes. |
| 46 | PASS | 5 title controls; 4 repository-only Credits handlers; 2,876 compiled classes; 1 guarded browser sink; 0 HTTP/socket calls; failures=0. |
| 47 | PASS | All five generated regions; 3 zooms x 4 pans before/after walking = 120 configurations; 212,316,160 hidden and 8,888,320 visible pixels checked against FOV; 1,392 door frames; failures=0. |

Local headless suite: `Runs=90 failures=0` (ten seeds for each of nine heroes, including all three new classes). JUnit: five tests, zero failures/errors/skips. The separate class-only and combined CI jobs remain unchanged. Actual outputs and failed diagnostic attempts are appended to the existing verification logs.

| Region | Steps | Turns | Opened doors | Door transition frames | Test 45 failed pairs | Floor/wall delta luminance | Floor/wall delta hue |
|---|---|---|---|---|---|---|---|
| sewers | 100 | 23 | 8 | 150 | 9/15 | 0.1033 | 9.62 degrees |
| prison | 202 | 56 | 16 | 292 | 5/10 | 0.0919 | 14.32 degrees |
| caves | 200 | 85 | 23 | 429 | 14/21 | 0.0162 | 0.96 degrees |
| city | 172 | 54 | 18 | 337 | 5/15 | 0.0366 | 75.75 degrees |
| halls | 114 | 36 | 10 | 184 | 14/15 | 0.0276 | 15.25 degrees |

Every measured pair and per-type mean is recorded in `recovery/<region>/distinctness.json`; the lit screenshots and 80 stepwise walk images are from the actual Windows renderer. The threshold remains delta luminance >= 0.12 OR delta hue >= 40 degrees. Human contrast review is pending; these measurements do not establish legibility.

---

## Historical recovery checkpoint (superseded by the rendering fix)

# Recovery acceptance — v1.0.1

Runtime/test source checkpoint: `e4f1f9977`. **Recovery is not accepted:** test 45 fails in all five regions. The exact delivered commit, tag and remote CI run are stated in the delivery response. No artgen or Blender loop ran. No gameplay, balance or content changes were made. The existing historical report is preserved below.

Commands and all failed attempts are appended to the existing verification logs. `recovery/<region>/lit.png`, `room.json`, `distinctness.json`, `walk.json` and stepwise `walk-*.png` contain actual renderer evidence, not synthesized rooms. Each path uses generated terrain without terrain edits and normal adjacent hero moves; mobs/heaps are removed only from the diagnostic fixture.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Fresh checkout e4f1f9977: desktop:run --smoke-title; RENDERED=TitleScene; BUILD SUCCESSFUL in 11s. |
| 2 | PASS | desktop:dist core:test android:assembleDebug core:smokeRun: BUILD SUCCESSFUL in 28s; APK 1.0.1-INDEV/code 918. |
| 3 | RETIRED | Superseded by recovery: generated-art rebuild prohibited; source-pixel restoration is checked by 44. |
| 4 | RETIRED | Superseded by recovery: generated style validator replaced by restored-pixel 44 and room 45. |
| 5 | PASS | All nine kits execute; ten seeds per hero; Runs=90 failures=0. |
| 6 | permanent known issue | Representative hooks pass; exhaustive talent selection/hook scenarios remain unrun. |
| 7 | permanent known issue | Subclass hooks pass; actual Tengu reward selection flow remains unrun. |
| 8 | permanent known issue | Nine armor abilities execute; actual crown selection flow remains unrun. |
| 9 | PASS | TEST 9 PASS; temporary Bone/Force terrain persistence, expiry and floor-exit checks execute. |
| 10 | PASS | Necromancer minion cap and Second Grave checks pass in the existing class suite. |
| 11 | PASS | TESTS 11-13 PASS: Grasp heap/trap/empty-cell cases. |
| 12 | PASS | TESTS 11-13 PASS: thrown damage at the required levels. |
| 13 | PASS | TESTS 11-13 PASS: domination retargeting and duration. |
| 14 | PASS | All nine classes: active-state save/load and floor-6 round trips; 90/0. |
| 15 | PASS | Runs=90 failures=0; includes all three new classes with ten seeds each; generation/debug descent, not player combat. |
| 16 | permanent known issue | Representative sprite/item/talent coverage passes; exhaustive every-code-path frame audit remains unrun. |
| 17 | RETIRED | Superseded by recovery: generated style requirements do not apply to restored upstream character/world pixels. |
| 18 | RETIRED | Superseded by recovery: generated-region brightness target replaced by within-room distinctness 45. |
| 19 | permanent known issue | The floor-15 1,000-turn/20-mob timing scenario remains unrun. |
| 20 | PASS | Fresh desktop-only checkout builds in 1m37s and launches in 11s with ANDROID_HOME/ANDROID_SDK_ROOT unset. |
| 21 | permanent known issue | Mandatory room gate 45 fails; workflow keeps it active. Exact remote CI outcome is reported after push. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; code 918; 1.0.1-INDEV. |
| 23 | PASS | PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow. |
| 24 | PASS | TEST 24: heroes=9 mob sprites=114 failures=0. |
| 25 | PASS | TEST 25: items=381 identification icons=60 failures=0. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | Off pixel differences=0; 240 GPU-completed frames mean=0.8114ms, p95=1.3019ms. |
| 32 | PASS | Three scorch sizes; actual floor fire expiry; water/chasm rejection failures=0. |
| 33 | PASS | TEST 33 PASS: scroll/stone offers, exclusions, cancel/apply, subclasses and history persistence. |
| 34 | PASS | TEST 34: old/future version, portrait exception and deletion failures=0. |
| 35 | RETIRED | Superseded by recovery: source-string grep replaced by compiled/runtime handler and network test 46. |
| 36 | PASS | TEST 36: nine upstream splashes, descriptions, portraits, talent icons and ItemSlots failures=0. |
| 37 | PASS | TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment. |
| 38 | permanent known issue | 18 supplied images lack verified allowed redistribution licenses; excluded from Git; 70 licensed files eligible. |
| 39 | RETIRED | Superseded by recovery: rejected regional iteration histories remain archival; their art no longer ships. |
| 40 | RETIRED | Superseded by recovery: generated-room isolated metrics replaced by remembered-terrain 43 and distinctness 45. |
| 41 | RETIRED | Superseded by recovery: generated animated liquid atlas replaced by historical scrolling water. |
| 42 | RETIRED | Superseded by recovery: calibrated generated-region gates replaced by restored-region test 45. |
| 43 | PASS | Five generated regions, seed 417: 788 moves, 254 turns, 75 opened doors; all five remembered terrain types checked; failures=0. |
| 44 | PASS | 87 upstream-derived character sheets; 122 restored assets; failures=0; packaged jar/APK files match. |
| 45 | permanent known issue | FAIL: Sewers 9/15 pairs; Prison 6/10; Caves 15/21; City 5/15; Halls 14/15. Thresholds unchanged. |
| 46 | PASS | 5 title controls; 4 repository-only Credits handlers; 2,875 compiled classes; 1 guarded browser sink; 0 HTTP/socket calls; failures=0. |

The local nine-hero run contains ten passing runs each for Necromancer, Enchanter and Psychic (30 new-class runs total), plus 60 upstream-class runs. Separate class-only and combined CI jobs remain active; their exact remote status is reported after push. Local JUnit: five tests, zero failures/errors/skips. The existing rendered Vault arena/fire/frost/shock/death/unlock checks pass; this is not a player-driven fight.

| Region | Walked steps | Turns | Opened doors | Fog pixels verified | Never-seen black pixels | Failed distinctness pairs |
|---|---|---|---|---|---|---|
| sewers | 100 | 23 | 8 | 604 | 885 | 9/15 |
| prison | 202 | 56 | 16 | 1619 | 1608 | 6/10 |
| caves | 200 | 85 | 23 | 1912 | 1963 | 15/21 |
| city | 172 | 54 | 18 | 1369 | 1920 | 5/15 |
| halls | 114 | 36 | 10 | 1006 | 3247 | 14/15 |

Visual review: upstream hero pixels are restored; remembered paths remain visible with fog dimming, and water/grass/door forms are present in the five lit captures. The mean-color distinctions still fail the requested hard gate. Exact restoration and the prohibition on new art work prevent a corrective palette redesign in this run; the failed comparisons remain visible and enforced.

---

## Historical initial checkpoint (superseded)

# Acceptance report

Runtime/art source checkpoint: `945e0dd5cf41a96255a50f87732786bb3a5b347d`. Later delivery commits only update documentation, verification records, and build-support diagnostics. Clean clone at `.local/clean-check` was created from the checkpoint with complete upstream history. No test below represents Android device gameplay.

| Test | Result | Actual evidence or reason |
|---|---|---|
| 1 | PASS | Clean-clone `desktop:run --args=--smoke-title`: `RENDERED=TitleScene`; `BUILD SUCCESSFUL in 1m 10s`. |
| 2 | PASS | Clean-clone `android:assembleDebug`: `BUILD SUCCESSFUL in 1m 10s` (combined run). |
| 3 | PASS | Clean-clone `python tools/artgen/build.py`; `git status --porcelain` produced no output. This does not establish complete inventory. |
| 4 | FAIL | Full validator: `Validated 36 generated specifications; 5 failures.` |
| 5 | NOT RUN | New class kits are absent; full kit acceptance not implemented. |
| 6 | NOT RUN | New talents and hook scenarios are absent. |
| 7 | NOT RUN | New subclasses/Tengu reward flows are absent. |
| 8 | NOT RUN | New armor abilities are absent. |
| 9 | NOT RUN | Temporary Bone/Force terrain is absent. |
| 10 | NOT RUN | Raise Skeleton and Second Grave are absent. |
| 11 | NOT RUN | Grasp is absent. |
| 12 | NOT RUN | Telekinetic Force is absent. |
| 13 | NOT RUN | Dominate is absent. |
| 14 | NOT RUN | Required active inscriptions/curses/minions/domination are absent. |
| 15 | FAIL | `core:smokeRun -PdesktopOnly=true`: missing NECROMANCER/ENCHANTER/PSYCHIC enum constants; `Runs=30 failures=30`. |
| 16 | NOT RUN | Complete referenced-frame audit is not implemented. |
| 17 | FAIL | 104 existing sheets lack specs; three hero sheets absent; character/item style gates unfinished. |
| 18 | NOT RUN | Only the Sewer region was rendered; five-region luminance checks not run. |
| 19 | NOT RUN | Full 1,000-turn/20-mob lighting performance scenario not implemented. |
| 20 | PASS | Clean clone, ANDROID_HOME and ANDROID_SDK_ROOT unset: `desktop:run -PdesktopOnly=true --args=--smoke-sewers`; `BUILD SUCCESSFUL in 11s`. |
| 21 | FAIL | CI runs completed: Android succeeded; Linux/Windows desktop jobs failed full art validation; headless failed the three-class gate. Builds/JUnit/artifact uploads succeeded. |
| 22 | PASS | `aapt dump badging`: `package: name='com.grimhollow.dungeon'`; `application-label:'Grimhollow'`. |
| 23 | PASS | Runtime: `PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow`; settings.xml and game99 exist there. |

Additional diagnostics actually run: four JUnit light/vignette tests (zero failures/errors); 36 generated specifications (zero subset failures); six upstream classes × ten seeds (60 generator/save-load runs, zero failures). These do not replace missing acceptance scenarios. The descent harness uses direct debug level generation; it does not yet navigate stairs or simulate combat.

Build commands used the repository's Windows Gradle wrapper and Temurin 17. Clean-clone build and desktop-only logs, APK identity, full/subset art checks and new-class failures are committed alongside this report. Screenshots are local outputs in `.local/clean-check/.local/acceptance/`.

Progress: §12 stage 1 is partial; no complete stage boundary or passing release tag was reached. Stages 2–6 are untouched. Quota was checked before stage 1 (0% used); later checks showed 1% then 3% used. No quota exhaustion is claimed. The requested feature work is incomplete.
