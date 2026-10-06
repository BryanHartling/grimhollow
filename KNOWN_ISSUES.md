# Known issues

- Historical v1.26.0 branch run 37401404725 failed the live encounter fixture after its deliberate death at action 150; zero HP reached the render check before the actor's completion flag. The fixture now waits for that scheduled callback while retaining unexpected-death, encounter coverage, summon/pickup and 120-frame death assertions. The failed run remains available.

- v1.26.0: physical Samsung tablet and full campaign balance remain human review; native portrait input is not a device test. Revised descriptions fall back to English where translations are unavailable.
- v1.26.0 verification: the first compiled audit was sandbox-blocked from reading the packaged JAR; the existing escalation route passed. Native menu fixtures needed the new submenu routes and remembered pages; formula-copy assertions were superseded by thematic text checks. Initial fixture failures remain recorded locally and are not passing results.
- v1.26.0 local captures: a Windows user-mapped lock on tablet-hurl.png was resolved by moving the old generated capture aside and rerunning the unchanged landscape fixture. Native starts attempted during JAR replacement failed before launching and were rerun after the build completed.

- v1.25.1: the original crashing campaign saves were not provided; migration is verified with the exact missing-field condition in unit tests and a complete disk save/load. Existing saves explicitly marked Playtest keep that flag; older automatic flags cannot safely be distinguished from intentional testing.
- v1.25.1: per-item hero weapon grip/axis checks are RETIRED by the user's approved generic painted-pose fallback. Weapon-dependent inventory overlays no longer ship; armor, facing, movement/action rendering and secondary/thrown combat context remain checked. Physical Samsung tablet visual review remains with the user.
- v1.25.1 local environment: the first compiled audit omitted the session JDK setup and could not locate javap; it passed after loading tools/env.ps1. Windows briefly denied clipboard polling during native number entry; both orientation fixtures completed with failures=0 and no check changed.

- v1.25.0: Wayward Chart reward balance and physical Samsung tablet review remain human playtesting; generated runs and native portrait input are not a full campaign or hardware test. New text falls back to English in other locales. Already generated floors do not gain entry rerolls; paid Magic Mapping can still fulfill an unclaimed regional promise on an eligible floor.
- v1.25.0 local packaging: the open Windows launcher locked its JAR during replacement. The final package is in `desktop/build/windows/1.25.0-release/Grimhollow`; missing support files in the original folder were restored without touching locked files or interrupting the active game. CI uses the normal clean version folder.

- v1.24.0: Doubloon/Golden Mimic campaign balance and physical Samsung tablet playtesting remain with the user; automated generated runs and native portrait input are not a complete campaign or hardware test. New text falls back to English in other locales. Regional cache plans and already-generated shop/floor stock are not rerolled by equipping the coin later.
- v1.24.0 local Halls walking verification initially stopped at cell 655 before moving to 690; the identical build and assertion passed all 114 steps using isolated test preferences. The initial interruption remains unexplained; no game rule or check was altered to obtain the passing result.
- v1.24.0 tag CI run 37331589801 failed the unchanged effects timing gate (mean 1.6659 ms, p95 2.2813 ms; both must be below 2 ms). The identical commit's branch run 37331499034 passed all seven jobs, with effects mean 0.9589 ms and p95 1.2325 ms. GitHub denied the unchanged failed-job retry with HTTP 403 (token lacks Actions rerun access); both runs remain available and no check was weakened.

- Historical v1.23.3 tag v1.23.3-readable-controls and branch CI runs 37250259548/37250259385 failed live key pickup because text was measured on the actor thread. The readable-controls-fix follow-up queues the HUD refresh on the render thread; the failed runs remain available and checks are unchanged.

- v1.23.3 environment: automatic approval review timed out on three parallel native-test/package commands; individual retries succeeded through the existing escalation route. No check was disabled.

- v1.23.3: a complete description-input failure did not reproduce in the native Lantern fixture; visible scroll controls and final-line padding address discoverability and clipping. Physical Samsung tablet verification of the key row and long descriptions remains with the user.

- v1.23.2: the user's exact rat encounter/save is unavailable; full-health floor-two rats are tested at normal and doubled Horror damage. The new regional combat ranges still need human campaign balance review, and physical Samsung tablet warning/art review remains with the user.

- v1.23.1 reproduces the reported Haste travel pauses after thawing in water: five turns of Chill still slow actions and trigger the sentry interruption. This matches the immutable upstream rules; Haste does not cleanse Chill. The original save was unavailable. The prior v1.23.0 unreproduced-encounter note below is superseded by this timing diagnosis.
- v1.23.1 painted-hand and actual-handle checks correct the v1.23.0 grip regression; physical Samsung tablet review of the new carry poses remains with the user. No character painting or gameplay rule changed.
- Historical v1.23.0 tag CI run 37240922970 failed effects timing at mean 1.4671 ms / p95 2.0726 ms against the unchanged 2 ms gate; the same commit's branch run 37240922993 passed all seven jobs (effects p95 1.3890 ms). Both runs remain available.

- v1.23.0 local verification briefly hit a Windows user-mapped PNG lock; moving the generated capture aside and rerunning the unchanged geometry gate succeeded. No game or check was altered for it.

- v1.23.0: physical Samsung tablet and full campaign balance remain human playtesting; native portrait touch is not an Android device run. Smooth text is the default, but an explicit pixel-font setting is preserved.
- v1.23.0: reported Haste/red-sentry encounter not reproduced; generated-room tests pass all four approaches without hasted shots. Sentry gameplay is unchanged; Haste remains speed rather than immunity.

- Sprint v1.22.6: native desktop/headless/build checks do not replace physical Samsung tablet and campaign balance review. All nine figures and equipped-weapon layers are delivered; animation remains restrained offline deformation, with existing facing and timings.
- Historical v1.21.1 CI run 37213690685 failed the unchanged effects timing gate (mean 1.7095 ms, p95 2.1713 ms, required both <2 ms). Later rune, Psychic and icon checkpoints passed full CI; no check was weakened.

- Historical v1.20.1 revised Warrior/Enchanter only. Superseded by the nine individually painted heroes in v1.22.6; the original checkpoint and previews remain in Git history.
- v1.20.0 branch CI run 36913560174 failed exact reproduction of the two new hero atlases on Linux, while the same commit passed on another Linux runner and Windows. v1.20.1 replaces floating matrix solves with fixed-grid integer arithmetic; the failed run and original tag remain available, and checks are unchanged.
- v1.20.0: the sandbox denied the initial `.git` scratch write and fetch; the documented authorized escalation route succeeded. No credentials are stored in committed files.

- v1.19.2: physical Samsung tablet review of the refreshed journal icons remains with the user. Treasury reminder text uses English fallback where translations are unavailable.
- Prior branch CI run 36899034139 failed Linux effects timing test 31: p95 2.1246 ms exceeds its unchanged <2 ms limit; all fog checks passed. Journal changes do not alter the effects renderer. The timing gate remains active.

- **Terrain contrast (test 45): Abandoned: test failed.** User decision, 2026-10-01: stop testing it. Removed from Linux/Windows CI and the local CLI. Last recorded result: 17/76 failed comparisons (Sewers 5/15, Prison 1/10, Caves 6/21, City 1/15, Halls 4/15). Historical measurements remain preserved; this is not PASS and is no longer an active issue or release gate. This decision supersedes all older instructions to enforce or rerun test 45.

- v1.20.2: Enabled Playtest saves can reopen tools from the in-game menu. Physical tablet confirmation remains with the user.

- GitHub denied cancellation of superseded v1.19.1 workflows 36897952329 and 36897952033 (HTTP 403); those runs retain the old workflow definition. Subsequent commits no longer schedule test 45.

- v1.19.0: physical Samsung tablet review and campaign balance of Scribe, Spellguard and elemental treasures remain with the user; native portrait/touch checks are not physical-device testing.
- Elemental rooms and loose parchment are added only to newly generated floors. Already generated floors are not retrofitted. New text uses English fallback where translations are unavailable.
- v1.18.3: armor Etching and hidden-torch repairs remain covered by automated desktop/headless checks; physical Samsung tablet confirmation remains with the user.
- Current plan (2026-09-30): tablet movement performance is resolved per the user's playtesting. Balance remains under the user's campaign playtesting; no balance changes are part of this terrain patch.
- Spellguard replaces Overload in v1.19.0: 10%/20% magical damage reduction requires a worn, temporarily inscribed armor piece. Psychic Pull remains skipped.
- Terrain readability v1.18.2: restrained lighting and fuller flattened grass remain shipped. Its failed numerical contrast test is now abandoned as recorded above; no art was changed for this decision.
- Resolved in v1.19.0: the stale full-interface Hatchling fixture now marks its test heap undiscovered before checking nearest-item sensing; the complete landscape interface test passes. No gameplay rule or assertion was weakened.

## Historical release limitations

The dated entries below retain their original verification scope. Their test-45 enforcement statements are superseded by its abandonment above. Their pending tablet-performance notes are superseded by the user's resolution above; they are not reopened tasks or claims that automated tests measured the physical tablet.

- v1.18.1 Hatchling balance and notification/UI changes need physical Samsung tablet playtesting; native portrait/landscape checks do not substitute for a device. Existing test-45 terrain contrast failures remain enforced.
- Old floor-wide Hatchling Item Sense expires when loading a pre-v1.18.1 save; the next meal selects one scent. New text falls back to English where translations are unavailable.

- Playtest polish v1.18.0: Overload is unchanged pending a replacement choice; new text uses English fallback. Physical Samsung tablet playtesting is outstanding. Message History cannot recover messages discarded before this update.

- Lurking Horror v1.17.0 uses a 50% regional roll, but skips a selected floor with no suitable empty room; it is not retroactively inserted into generated floors. Encounter balance and physical Samsung tablet performance still require human playtesting.
- Lurking Horror text currently falls back to English in other locales. Its fleeing/recovery poses are deliberately steady; the new attack animation and detection overlay do not animate terrain or alter movement timing.
- Dragon expedition v1.16.0 is enabled and all eight components are implemented. Campaign combat balance and Samsung tablet performance still require human playtesting; automated boss defeats use controlled fixtures and are not a complete player-driven campaign.
- The hunter appears only during creation of its seeded City floor (16–19); an existing save that already generated that floor will not gain the NPC retroactively. A new run gives the normal quest path; Playtest also provides direct expedition travel.
- Playtest rebuild resets a floor, not the expedition's saved victories or one-time reward flag. Use a fresh test save to repeat the entire quest. The dragon never heals; previously used brood and collected rewards stay consumed on ordinary revisits.
- Expedition text currently uses English fallback in other locales. The APK is a debug-signed sideload build; no physical Android device was connected for this run.
- Existing terrain contrast test 45 remains enforced and unresolved: fresh v1.17.0 Windows captures fail 25/82 comparisons (Sewers 8/21, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15); the Sewers sample now includes a trap. Art and thresholds are unchanged; exact release-head CI status is reported at delivery.

- Version 1.15.0 balance tuning passes headless generation/persistence and native portrait-touch/landscape-mouse checks; physical Samsung tablet input and campaign balance under custom settings still need playtesting. Settings are save-local, and generation changes need a new or rebuilt floor. Existing contrast test 45 remains enforced and is not repaired by this settings patch.

- Version 1.14.2 passes native portrait/landscape quest-room checks and builds Android, but no physical Samsung tablet is connected; the painted mine/workshop/ritual/HUD changes still need tablet visual and movement review.
- The v1.13 screenshot's isolated illuminated patch is consistent with Corpse Sense sharing a raised ally's sight; the native test verifies that behavior and removes the patch when the ally is removed, but the original save is unavailable for exact reproduction. Chainwarden is an intentional 30% alternate Prison boss.
- The corrected summoning-circle instructions are English; other locales retain their existing translations. Earlier contrast test 45 remains enforced with its known failures; this patch does not claim full CI is green.
- Version 1.14.2 Windows test 45 remains 22/76 failures (Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15); Caves was freshly rendered and the other four regional captures retain checkpoint provenance. The fresh Caves fog/walking/door tests pass without changing either contrast threshold.

- Version 1.14.1 reproduces and fixes Shaman sprite displacement from overlapping walk/teleport animations and covers knockback overlap; the original floor-11 save and exact cursed-wand roll were unavailable, so the complete reported encounter is not claimed reproduced.
- Version 1.14.1 HUD and feeding regressions run in the native renderer with portrait touch and landscape mouse input; no physical Samsung tablet was connected. NPC bone particles require Enhanced Effects, as in v1.14.0; the legacy effects-off option remains available.

- Version 1.14.0 Defensive Sigil and Ghoul tuning require campaign playtesting. Psychic strength has not been changed without a specific ability/encounter diagnosis; the rare floor-six tier-five weapon roll remains an intentional upstream loot possibility.
- The new particles, denser gas and universally sharper creature exports require human visual review and Samsung SM-T830 performance testing; desktop rendering is not a physical tablet test. New Defensive Sigil text falls back to English in other locales.

- Hatchling feeding and permanent-upgrade balance still require campaign playtesting; the 10/15/20 energy curve, permanently escalating gold demand and actual +10 Wealth equipment reward follow the approved design. No physical Samsung tablet run is claimed.
- The new regional story and Hatchling text currently use English fallback in other locales; existing translated mechanical descriptions remain.

- Enchanter v1.11.2 proc rates and talent purchasing pass deterministic and native mouse/touch regressions; early-campaign balance still needs player review. Chance bonuses do not strengthen always-active enchantments/glyphs or curses. No physical Android playtest is claimed.

- Samsung SM-T830 movement smoothness remains unverified on hardware: this run's adb devices -l returned an empty list. Version 1.12.0 reduces light-map work per movement step sixteenfold and steadies creature travel poses; native desktop/touch coverage is not a physical tablet performance measurement.
- Lucky grants extra consumables only on eligible killing hits; max-level Playtest heroes do not get loot from low-level enemies because upstream loot eligibility is preserved. No full-campaign Lucky drop-rate study is claimed.
- Playtest floor rebuilding resets the current terrain, creatures and loot, not global quest history. Direct Vault travel preserves the testing loadout; use the Playtest menu to leave, and use normal quest entry to test equipment stripping/rewards. No physical Android session or exhaustive item-by-item combat campaign is claimed.
- All nine hero redesign batches are implemented; human review of their new silhouettes, proportions and gestures remains pending. They preserve the existing single mirrored facing and action timing, rather than introducing a multi-direction animation system.
- The prematurely published v1.8.0-readability tag is preserved: Linux test 31 failed at mean 2.2395ms / p95 3.0285ms despite passing locally. Halving gas alone still failed Linux p95 at 2.5816ms in run 35947743192. The final v1.8.1 patch batches unchanged flame/ember particles: exact-head CI 35949405176 passes test 31 at mean 0.9583ms / p95 1.2911ms, with zero channel difference from individual draws. Only test 45 fails that run; its Linux sample is 31/76, versus Windows 34/82. The 2ms gate is unchanged.
- Ashlight's fourteen-unit feeding curve and typical campaign level need human playtesting; no full campaign or physical Android device session is claimed. Ordinary fire protection intentionally does not change Soulfire's existing immunity-piercing rule.
- Inscription migration preserves knowledge stored in an older save; it cannot reconstruct random sigils discarded by earlier versions before that save. Identified enemy-curse casting remains an unimplemented design proposal.

- Test 45 retains its 0.12 luminance / 40-degree hue thresholds. Version 1.14.0 remeasurement of Windows evidence (fresh Sewers, retained other-region captures) fails 22/76 comparisons: Sewers 5/15, Prison 1/10, Caves 8/21, City 3/15, Halls 5/15. Unknown-neighbor wall concealment changes the sampled pixels; no terrain palette or threshold was changed to chase this metric. The gate remains enforced in CI and human readability review is still required.
- Nine hero sheets, all 381 named inventory IDs (380 distinct cells), all 71 creature atlases (129 forms), 86 status emblems, three overhead symbols, shared interface frames and 32 navigation glyphs now use painted art. Class splashes and portraits now have nine original matching paintings, traps have 63 painted states, and Windows/Android launchers use a new emblem. Sprouted plants, all 113 added-class skill icons and all 98 visible awards now use painted art. All 60 identification overlays now use existing painted status sources; upstream talent/region/credit icons and special-room artwork retain prior art; this is not a replacement of every game image.
- Chests share closed art with their matching mimic variants; horn/chalice/rose states, coated darts and the remaining item families now have painted sources. Human review of the portraits, class handbook, quiet idle presentation, trap plates, launchers, plants, new skill icons and readability changes remains pending; in-game action animation retains the prior key poses.
- The reported glowing Rogue rat is consistent with the existing curse-bound variant, now explicitly labeled in inspection and status UI; the exact player encounter was unavailable, so this is a likely explanation rather than a reproduced event.
- Psychic progression, Push/Hurl and domination pass deterministic regression scenarios; full-campaign balance of the added Grasp/Glimpse utility and the strength of level-six chasm removal still need player testing. No chasm rider was moved on the basis of those mechanical checks alone.
- An additional 2x render found six retained creature sprites just above test 24's 0.95 occupancy ceiling (Caustic Slime, Fetid Rat, Fungal Spinner, Hermit Crab, Piranha and Snake; measured 0.9545-1.0). The standard 3x geometry run passes; no creature art or acceptance threshold was changed in this presentation pass.
- Tests 6, 7 and 8 carry permanent known issues: representative talent hooks and all nine armor abilities execute, all 36 class-preview handbook pages are exercised, but exhaustive in-run talent selection and actual Tengu/crown reward flows remain unrun.
- Tests 16 and 19 carry permanent known issues: declared creature animation rectangles, sprite/item/talent contracts and reflected/NPC variants are checked, but exhaustive every-code-path gameplay coverage and the floor-15 1,000-turn timing scenario remain unrun.
- Test 38 carries a permanent known issue: 18 of 33 supplied reference images lack verified CC0/public-domain redistribution permission and remain excluded from Git; U17 has unresolved game provenance and U08/U09 are alternate crops; 70 licensed files are eligible.
- Scripted floor descent, class/content/Vault checks and recovery corridor walks do not replace a complete player-driven campaign, Android device testing, Vault combat, reward dialogs or hazard traversal.
- The named GDD-build-spec-v0.9.md was absent from the repository and supplied Downloads; recovery was applied to the actual v1.0.0 tree and the existing GDD-one-shot-build-spec.md.
- Credits links stay within this repository; its README attribution anchors provide the upstream project links, reconciling required credits with the repository-only navigation gate.
- Restricted Git/Java/network operations use the documented authorized escalation route and Gradle --no-daemon; javap initially failed a sandboxed jar read and was retried through that route. An earlier CIM process-listing denial used Get-Process instead. Current Git/network/build work used that route without an approval pause; historical failures remain in the prior run records and current diagnostics are preserved under ignored .local/hatchling-* files.
- This run encountered an old empty .git/index.lock; its timestamp predated the active read-only Git processes by more than 25 minutes. Removing that stale lock through the documented escalation route restored normal commits.
- The stored Git credential helper returned no credential. The user-supplied token authenticated successfully; only a Windows DPAPI-encrypted copy is stored in ignored .local/github-token.dpapi, and Git/CI access uses process-local authorization headers. No token is committed.

- Version 1.16.1 natural cavern layout, separated initial broodmother and equipment-heavy remains apply to new or Playtest-rebuilt caverns; saved floors keep their layout and existing loot, while their stair artwork is corrected on load. Physical-tablet performance and expedition campaign balance still require human playtesting.
