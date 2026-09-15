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
