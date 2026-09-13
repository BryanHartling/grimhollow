# Painted hero and inventory acceptance - v1.2.0

Runtime/art source checkpoint: `c9f575ee58e5fdcb09dae09dbbcfa8b631d3a9ea`; hero checkpoint: `6cccbb982a9370f4b065cd4b647cf924d5dc9e1c`. This continuation adds nine painted hero sheets (21 pose slots across eight armor rows) and 202 named inventory cells. All gameplay, balance, content, animation timings, fog sampling, input and logical geometry remain unchanged. Thirty committed imagegen source sheets now reconstruct twenty-three shipping atlases, including the prior world pass.

Desktop and Android builds pass; JUnit tests=5 failures=0 errors=0 skipped=0; all-nine-hero smoke Runs=90 failures=0. Local fog, remembered-terrain, sprite, icon, effects and handler checks pass. **The unchanged contrast gate 45 fails 30/76 local comparisons.** This is not a claim of full visual acceptance or a green CI workflow.

The initial inventory GPU run exposed detached alpha residue above the sandals: height/tile=0.4375. The packer now removes alpha below 8/255 after downsampling so invisible pixels cannot enlarge occupancy bounds. All 381 items then passed the original 0.45-0.55 footprint band. The failed run remains in clean-build.log. No threshold was relaxed.

The all-nine-hero invocation includes Necromancer 10/0, Enchanter 10/0 and Psychic 10/0. These are groups within the 90-run command, not additional local class-only invocations. The workflow also runs each class separately and the combined three-class gate; exact-tag job results are supplied with delivery.

Test 44 reconstructs the declared painted replacements and retains original provenance for all other restoration assets and 78 upstream-derived character sheets. Test 25 retains every name, ID and art-index remap; only the 202 explicitly named source-cell hashes change. Retired iterative-art tests remain retired. Incidental test-41 output from the reused screenshot fixture is not claimed as an active liquid gate.

`painted-world.html` links both art boards and actual current `recovery/<region>/lit.png` captures. Each region includes its measurements and 16 walking captures. Monster/NPC sheets, class splash paintings, remaining inventory families, title/UI and approved Sewer doors/torches retain their prior artwork. Chests/mimics and multi-state item families stay together until their companion frames are ready.

| Test | Status | Actual output or reason |
|---|---|---|
| 1 | PASS | Retained v1.0.2 fresh desktop-only checkout result; not repeated locally in this art pass. Current title and real renderer launch pass. |
| 2 | PASS | desktop:dist core:test android:assembleDebug core:smokeRun -PsmokeUpstream=true: BUILD SUCCESSFUL in 1m54s; JUnit tests=5 failures=0 errors=0 skipped=0; final alpha-packing rebuild: BUILD SUCCESSFUL in 25s. |
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
| 21 | permanent known issue | Test 45 remains active and fails locally. Android CI passed for source c9f575ee5; the exact delivered tag CI is reported separately. Checks are unchanged. |
| 22 | PASS | aapt: com.grimhollow.dungeon; label Grimhollow; versionCode 930; versionName 1.2.0-INDEV. |
| 23 | PASS | PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow. |
| 24 | PASS | TEST 24: heroes=9 mob sprites=114 failures=0. |
| 25 | PASS | TEST 25: 381 named IDs + 60 icons; Waterskin=480, MindVision=eye@98 (ID 82), eleven section-9 and ten class items; failures=0. All 202 replacements are pinned to independently packed named source cells. |
| 26 | PASS | TEST 26: three stains and floor/chasm/water/trap placement failures=0. |
| 27 | PASS | TEST 27 PASS: 300 turns unchanged; 12 charges level=1; Wraith offered; hostile attacked within 2 turns. |
| 28 | RETIRED | Superseded by recovery: rendered-cache rebuild no longer produces the restored shipping world/characters. |
| 29 | RETIRED | Superseded by recovery: rerendering is prohibited; Blender/cache retained unused. |
| 30 | RETIRED | Superseded by recovery: generated character hue-distance gate replaced by upstream pixel provenance 44. |
| 31 | PASS | TEST 31: off pixel differences=0; 40 gas + 10 fire cells; 240 GPU-completed frames mean=0.7303ms, p95=0.8809ms; failures=0. |
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
| 43 | PASS | Five generated regions; 788 walking steps, 254 turns, 75 door openings; failures=0. Exact remembered-terrain UV equality remains enforced. |
| 44 | PASS | PAINTED assets=23 source sheets=30 failures=0; upstream-derived character sheets=78; restored assets=100; verified painted replacements=23; failures=0. All 482 packaged assets match JAR and APK bytes. |
| 45 | permanent known issue | Unchanged thresholds fail 30/76 current local pairs: Sewers 7/15, Prison 2/10, Caves 10/21, City 1/15, Halls 10/15. Actual metrics and images retained; previous pass was 29/76. |
| 46 | PASS | Runtime title controls=5, credits handlers=4, external opens=0, scene fetches=0; compiled classes=2876, guarded sink=1, HTTP/socket calls=0, failures=0. |
| 47 | PASS | All five regions, 120 camera configurations, 212316160 hidden and 8888320 visible pixels, 1399 door-transition frames; fogTexel/cell=1:1, worldUnits=16, lightQuad=aligned, failures=0. |

Current Windows floor/wall measurements (delta L >= 0.12 OR circular mean hue >= 40 degrees):

| Region | Delta L | Delta hue | Failing pairs |
|---|---|---|---|
| Sewers | 0.1971 | 3.63 degrees | 7/15 |
| Prison | 0.2863 | 12.59 degrees | 2/10 |
| Caves | 0.1628 | 2.11 degrees | 10/21 |
| City | 0.1386 | 37.21 degrees | 1/15 |
| Halls | 0.1179 | 23.23 degrees | 10/15 |

Environment pixels are unchanged in this continuation. The current live-room measurements, including the extra Caves failure, are reported without adjusting art or targets to chase the statistic. Human readability and animation review remain necessary.

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
