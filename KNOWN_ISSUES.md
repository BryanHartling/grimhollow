# Known issues

- Stages 1, 2, 2.5, 3, 3.5, 4, 4.5 and 5 are complete; stopped for human review before stage 6. Full rendered-art coverage, enhanced effects and section 9 content remain undelivered.
- Tests 4 and 17 fail: `Validated 64 generated specifications; 2 failures`; 100 existing sheets lack pipeline sources, and full character/item style and referenced-frame coverage remain unfinished. The implemented 64-specification subset passes; cache-only rebuilding has zero byte differences.
- Rendered art covers the Sewers POC, rat, crab, eight Necromancer armor tiers and Skeleton/Ghoul minions; other characters and regions retain transitional or upstream art. Bone Prison uses barricade visuals; Force Wall has a generated transparent effect.
- Full tests 6, 7 and 8 are NOT RUN: scripted talent maps and representative hooks pass, and all nine new armor abilities execute, but exhaustive talent-selection and actual Tengu/crown UI flows were not driven.
- Test 9 is NOT RUN in full: Bone Prison and Force Wall restore correctly, but Wand of Bone belongs to the undelivered content stage.
- Tests 16, 18, 19 and 30 are NOT RUN: complete sprite-index coverage, five-region luminance, floor-15 turn-performance and regional mob-histogram measurements remain pending; Android device gameplay is untested.
- Tests 31 and 32 are NOT RUN because enhanced effects and scorch decals belong to stage 7.
- Test 15 passes with `Runs=90 failures=0` for all nine heroes through the upstream generator/debug descent route; the harness does not play combats or search for stairs as a player would.
- Test 21 remains failing because the full art gate is retained; separate class gates and the combined three-class gate pass locally. Platform artifacts upload despite the overall workflow's art failure.
- Tests 20 and 22 were not rerun locally in this continuation, as section 14 preserves their checkpoint results; final CI builds desktop and Android, but does not execute the no-SDK launcher or APK identity inspection.
- GitHub Actions dispatch/settings requests return HTTP 403 with the supplied token; push-triggered workflows work but may appear after a delay.
- Sandbox fetch/.git writes require the documented escalation route; both continuation checks passed there. Use an elevated fresh Gradle process (`--no-daemon`) to avoid cached sandbox-daemon AccessDeniedException errors.
- One compile approval review timed out; its permitted retry succeeded. A later runner spawn timed out; the existing build session remained available and completed.
