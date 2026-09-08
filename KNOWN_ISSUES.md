# Known issues

- Stages 1, 2, 2.5 and 3 are complete; stage 4 has not started. Psychic, full art coverage and section 9 content remain undelivered.
- Tests 4 and 17 fail: `Validated 48 generated specifications; 3 failures`; 103 existing sheets lack pipeline sources, the Psychic hero sheet is absent, and complete style/frame coverage remains unfinished.
- The Necromancer and its minions use transitional art; full new-character art, four remaining regions and transient lighting belong to stage 5. Bone Prison currently uses barricade visuals for its opaque, nonflammable terrain.
- Full tests 5-9 and 14 are not run for all specified content; Necromancer kit, talent hooks, subclass spell sets, armor execution and curse/minion save-load are exercised by its smoke gate. The actual Tengu/crown UI selection flows have not been driven in automation.
- Full test 15 remains failing for the absent Psychic; the Necromancer-only gate reports `Runs=10 failures=0` using generator/debug descent, not a combat-playing bot.
- Tests 11-13, 16, 18 and 19 are NOT RUN: later-class behavior, full sprite-index coverage, five-region luminance and turn-performance measurements remain pending; Android device gameplay is also untested.
- Test 21 remains failing because the full art and three-class CI gates are retained. A separate Necromancer-only CI job provides the stage 2 gate; platform artifacts remain available from the failing overall workflow.
- GitHub Actions dispatch/settings requests return HTTP 403 with the supplied token; push-triggered workflows work but may appear after a delay.
- Sandbox fetch/.git writes require the documented escalation route; both continuation checks passed there. Use an elevated fresh Gradle process (`--no-daemon`) to avoid cached sandbox-daemon AccessDeniedException errors.
