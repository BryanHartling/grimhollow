# Known issues

- Recovery test 45 fails on the restored historical terrain in all five regions; the unchanged per-pair thresholds are 0.12 mean luminance or 40 degrees mean hue, and the release is not accepted while this gate fails.
- The recovery's exact-restoration rule and prohibition on new art work prevent recoloring the historical tiles to force test 45 green; CI retains that failure instead of lowering the target or selecting a passing-only sample.
- Tests 6, 7 and 8 carry permanent known issues: representative talent hooks and all nine armor abilities execute, but exhaustive talent selection and actual Tengu/crown UI flows remain unrun.
- Tests 16 and 19 carry permanent known issues: rendered sprite/item/talent checks cover representative contracts, but exhaustive every-code-path frame coverage and the floor-15 1,000-turn timing scenario remain unrun.
- Test 38 carries a permanent known issue: 18 of 33 supplied reference images lack verified CC0/public-domain redistribution permission and remain excluded from Git; U17 has unresolved game provenance and U08/U09 are alternate crops; 70 licensed files are eligible.
- Scripted floor descent, class/content/Vault checks and recovery corridor walks do not replace a complete player-driven campaign, Android device testing, Vault combat, reward dialogs or hazard traversal.
- The named GDD-build-spec-v0.9.md was absent from the repository and supplied Downloads; recovery was applied to the actual v1.0.0 tree and the existing GDD-one-shot-build-spec.md.
- Credits links stay within this repository; its README attribution anchors provide the upstream project links, reconciling required credits with the repository-only navigation gate.
- Restricted Git/Java/network operations use the documented authorized escalation route and Gradle --no-daemon; javap initially failed a sandboxed jar read and was retried through that route. Earlier failed builds and test-cleanup attempts remain in the verification logs.
