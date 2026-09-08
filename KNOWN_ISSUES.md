# Known issues

- Stages 1, 2, 2.5, 3, 4 and 4.5 are complete; Runecraft and the rendered proof of concept are next. Full art coverage, transient lighting and section 9 content remain undelivered.
- Tests 4 and 17 fail: `Validated 59 generated specifications; 2 failures`; 103 existing sheets lack pipeline sources, and full character/item style and referenced-frame coverage remain unfinished. All 59 generated assets pass the subset validator.
- New heroes and minions use transitional pipeline art; full character art and four remaining regions belong to stage 6. Bone Prison uses barricade visuals; Force Wall has a generated transparent effect.
- Full tests 6, 7 and 8 are NOT RUN: scripted talent maps and representative hooks pass, and all nine new armor abilities execute, but exhaustive talent-selection and actual Tengu/crown UI flows were not driven.
- Test 9 is NOT RUN in full: Bone Prison and Force Wall restore correctly, but Wand of Bone belongs to the undelivered content stage.
- Tests 16, 18 and 19 are NOT RUN: complete sprite-index coverage, five-region luminance and floor-15 turn-performance measurements remain pending; Android device gameplay is untested.
- Test 15 passes with `Runs=30 failures=0` through the upstream generator/debug descent route; the harness does not play combats or search for stairs as a player would.
- Test 21 remains failing because the full art gate is retained; separate class gates and the combined three-class gate pass locally. Platform artifacts upload despite the overall workflow's art failure.
- GitHub Actions dispatch/settings requests return HTTP 403 with the supplied token; push-triggered workflows work but may appear after a delay.
- Sandbox fetch/.git writes require the documented escalation route; both continuation checks passed there. Use an elevated fresh Gradle process (`--no-daemon`) to avoid cached sandbox-daemon AccessDeniedException errors.
- One compile approval review timed out; its permitted retry succeeded. A later runner spawn timed out; the existing build session remained available and completed.
