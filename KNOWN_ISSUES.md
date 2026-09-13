# Known issues

- The painted environment pass is undergoing the unchanged test 45 room measurement (0.12 mean luminance or 40 degrees mean hue); the prior restored-art result was 47/76 failing pairs and is historical, not the new art result.
- The new visual-overhaul request authorizes painted world art; all animated hero/monster sheets, item icons, the approved title/UI and Sewer doors/torches remain unchanged. Their pixel styling is still visible beside the new environment art.
- Waterskin already resolves to its catalogued bag at atlas cell 480; no index mismatch was reproduced. Its unchanged artwork may still be visually unclear. Test 25 checks named pixel identities, not human approval of the art.
- Tests 6, 7 and 8 carry permanent known issues: representative talent hooks and all nine armor abilities execute, but exhaustive talent selection and actual Tengu/crown UI flows remain unrun.
- Tests 16 and 19 carry permanent known issues: rendered sprite/item/talent checks cover representative contracts, but exhaustive every-code-path frame coverage and the floor-15 1,000-turn timing scenario remain unrun.
- Test 38 carries a permanent known issue: 18 of 33 supplied reference images lack verified CC0/public-domain redistribution permission and remain excluded from Git; U17 has unresolved game provenance and U08/U09 are alternate crops; 70 licensed files are eligible.
- Scripted floor descent, class/content/Vault checks and recovery corridor walks do not replace a complete player-driven campaign, Android device testing, Vault combat, reward dialogs or hazard traversal.
- The named GDD-build-spec-v0.9.md was absent from the repository and supplied Downloads; recovery was applied to the actual v1.0.0 tree and the existing GDD-one-shot-build-spec.md.
- Credits links stay within this repository; its README attribution anchors provide the upstream project links, reconciling required credits with the repository-only navigation gate.
- Restricted Git/Java/network operations use the documented authorized escalation route and Gradle --no-daemon; javap initially failed a sandboxed jar read and was retried through that route. CIM process listing was denied in this run; Get-Process supplied the needed inventory. Earlier failed builds and test-cleanup attempts remain in the verification logs.
