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
| 21 | FAIL | Push succeeded; no workflow runs appeared. Settings lookup and dispatch returned HTTP 403. |
| 22 | PASS | `aapt dump badging`: `package: name='com.grimhollow.dungeon'`; `application-label:'Grimhollow'`. |
| 23 | PASS | Runtime: `PREFERENCES_PATH=C:\Users\Hartl\AppData\Roaming\.grimhollow\Grimhollow`; settings.xml and game99 exist there. |

Additional diagnostics actually run: four JUnit light/vignette tests (zero failures/errors); 36 generated specifications (zero subset failures); six upstream classes × ten seeds (60 generator/save-load runs, zero failures). These do not replace missing acceptance scenarios. The descent harness uses direct debug level generation; it does not yet navigate stairs or simulate combat.

Build commands used the repository's Windows Gradle wrapper and Temurin 17. Clean-clone build and desktop-only logs, APK identity, full/subset art checks and new-class failures are committed alongside this report. Screenshots are local outputs in `.local/clean-check/.local/acceptance/`.

Progress: §12 stage 1 is partial; no complete stage boundary or passing release tag was reached. Stages 2–6 are untouched. Quota was checked before stage 1 (0% used); a later check showed 1% used. No quota exhaustion is claimed. CI access is blocked and the requested feature work is incomplete.
