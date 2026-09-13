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
