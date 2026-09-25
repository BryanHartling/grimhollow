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
