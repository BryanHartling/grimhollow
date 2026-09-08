# Known issues

- Stage 5.5 is delivered after the stages 1-5 checkpoint: 45 references and 10 companion images/maps; test 38 passes; stopped for human review with stage 5.6 and test 39 not started; full art coverage, enhanced effects and section 9 content remain pending.
- Tests 4 and 17 fail: `Validated 64 generated specifications; 2 failures`; 100 existing sheets lack pipeline sources, and full character/item style and referenced-frame coverage remain unfinished. The implemented 64-specification subset passes; cache-only rebuilding has zero byte differences.
- The unchanged POC covers Sewers, rat, crab, eight Necromancer armor tiers and Skeleton/Ghoul minions; human review rejected its visual quality and v0.6 assigns future Blender work to environments/props/items/effects and future characters to procedural silhouettes; Bone Prison still uses barricade visuals.
- Full tests 6, 7 and 8 are NOT RUN: scripted talent maps and representative hooks pass, and all nine new armor abilities execute, but exhaustive talent-selection and actual Tengu/crown UI flows were not driven.
- Test 9 is NOT RUN in full: Bone Prison and Force Wall restore correctly, but Wand of Bone belongs to the undelivered content stage.
- Tests 16, 18, 19 and 30 are NOT RUN: complete sprite-index coverage, five-region luminance, floor-15 turn-performance and regional mob-histogram measurements remain pending; Android device gameplay is untested.
- Tests 31 and 32 are NOT RUN because enhanced effects and scorch decals belong to stage 7.
- Test 15 passes with `Runs=90 failures=0` for all nine heroes through the upstream generator/debug descent route; the harness does not play combats or search for stairs as a player would.
- Test 21 remains failing because the full art gate is retained; separate class gates and the combined three-class gate pass locally. Platform artifacts upload despite the overall workflow's art failure.
- Tests 20 and 22 retain checkpoint PASS results under section 14; this references-only run does not repeat the no-SDK launcher or APK identity inspection, and Android device gameplay remains untested.
- GitHub Actions dispatch/settings requests return HTTP 403 with the supplied token; push-triggered workflows work but may appear after a delay.
- The stage 5.5 sandbox fetch and .git scratch write failed in the default sandbox and passed via the documented escalation route; network downloads and Git writes used that route; future local Gradle builds require `--no-daemon`.
- One compile approval review timed out; its permitted retry succeeded. A later runner spawn timed out; the existing build session remained available and completed.
- Reference fit limits are recorded per file in SOURCES.md: monochrome sewer, stone culvert equivalent, unlit material maps, torch holder without a wall-light cone, unhooded seated statue, burned-ground scorch study and blood stain of unverified age; test 38 establishes coverage/licenses/exclusion hashes, not artistic approval or stage 5.6 scores.
