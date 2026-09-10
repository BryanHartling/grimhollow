# Known issues

- Stage 6a ships animated sewage, water and lava; Prison/Caves/City/Halls art, character redesign, items/title, enhanced effects and section 9 content remain pending in stages 6b-8.
- Tests 4 and 17 remain incomplete: 96 existing sheets lack pipeline sources and full character/item style and referenced-frame coverage are unfinished; the implemented inventory is 69 specifications.
- Test 41 uses actual runtime mixed variant/phase tiling for the unchanged 0.35 autocorrelation ceiling because identical tile copies necessarily correlate 1; centre-depth contrast is adjusted to 0.02-0.04 after two visual failures, as documented in CHANGES.md.
- Characters and mobs retain rejected POC or procedural checkpoint art pending stage 6d; special terrain retains its procedural base, including bridge surfaces, and Bone Prison still uses barricade visuals.
- Tests 6, 7 and 8 are NOT RUN in full: representative talent hooks and all nine armor abilities execute, but exhaustive selection and actual Tengu/crown UI flows were not driven.
- Test 9 is NOT RUN in full: Bone Prison and Force Wall restoration pass representative scenarios, but Wand of Bone belongs to the undelivered content stage.
- Tests 16, 18, 19, 30 and 42 are NOT RUN in full: complete sprite-index coverage, five-region luminance/room gates, floor-15 timing and regional mob-histogram checks remain pending; Prison/Halls screenshots currently certify liquid appearance only.
- Tests 31 and 32 are NOT RUN because enhanced effects and scorch decals belong to stage 7; Halls retains its upstream ember particles around the new lava.
- Test 15 reports Runs=30 failures=0 for the three new classes; the retained nine-hero result is Runs=90 failures=0, using upstream generation/debug descent rather than simulated player combat and stair search.
- Test 21 remains blocked by the full art gate until stage 6e; checks remain enabled and platform build artifacts still upload.
- Tests 20, 22 and 23 retain checkpoint results per section 14; Android device gameplay remains untested.
- GitHub Actions dispatch/settings requests previously returned HTTP 403; push-triggered workflows are the working route and may appear after a delay.
- Restricted Git/network/Blender/Java commands use the documented escalation route and Gradle --no-daemon; fetch and the .git scratch write/delete succeeded, while optional sandboxed Get-CimInstance inspection was denied and existing command output supplied the needed status.
- Test 38 remains FAIL for the expanded local intake: 18 of 33 supplied images lack verified CC0/public-domain redistribution permission and remain excluded from Git/optimization; U17 has unresolved game provenance and U08/U09 are alternate crops; 70 licensed reference files are eligible.
- Reference-fit limitations remain recorded in SOURCES.md, including amber sewer water, equivalent culvert architecture, unlit maps, a torch without a wall cone, an unhooded statue, a burned-ground study and a blood stain of unverified age.
- Test 29 passes for all 104 new liquid/ripple frames with maximum pHash distance 0; approved Sewers caches were skipped, the prior 288-frame result is retained, and the reviewed liquid cache was restored after verification.
