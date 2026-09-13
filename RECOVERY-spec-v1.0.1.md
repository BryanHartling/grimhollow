# Grimhollow — Recovery specification (v1.0.1)

This document supersedes §12 stages 9 and 10, and the art-scope portions of §15, in `GDD-build-spec-v0.9.md`. Everything else in that spec stands. **No generative iteration loop runs in this stage.** `tools/artgen/` and the Blender pipeline are retained in the repository but are not invoked to produce shipped art.

## Why

Human playtesting of `v1.0.0-content-complete` found the build unplayable for two independent reasons:

1. **Remembered-terrain bug.** Cells that are explored but not currently visible are repainted with wall tiles instead of their remembered terrain. Because the field of view travels with the hero, a false wall appears to follow the hero and obscure the path already walked. Evidence: screenshot showing grey horizontally-coursed wall tiles filling a corridor the hero had just traversed, adjacent to the olive wall tiles that are the region's actual walls.
2. **Terrain indistinguishability.** Floor, water, grass, wall, and props in the same room converged on the same hue and value band (olive/brown, luminance ~0.10–0.18). Every gate measured a tile against a reference or against one other tile in isolation; none measured whether the terrain types in a single room could be told apart. They cannot.

Separately, character art, mob art, and class splashes produced by the procedural pipeline are rejected: flat vector shapes with featureless faces, judged worse than upstream's 16px sprites and painted splashes. Three runs have established that this pipeline produces acceptable surfaces (tiles, doors, torches, title art) and unacceptable figures.

## Scope of this stage

### 1. Fix the remembered-terrain bug (highest priority)
- Diagnose the visibility-mask handling in the wall tilemap and any relief or wall-raise pass added during stages 6b–6c. The fault is that non-visible-but-explored cells receive wall geometry rather than their remembered terrain.
- Fix so that remembered cells render their own terrain, dimmed, exactly as upstream does; never-seen cells stay black.
- **Test 43:** in a generated level, walk a fixed path, then assert that every explored-but-not-visible cell's rendered tile matches its terrain type (not a wall tile), for floor, water, grass, door, and chasm cells. Run on all five regions.

### 2. Revert world art to the last legible state
- Restore terrain, liquid, grass, and prop art to the state at tag `v0.3.2-fixup2`, scaled to current geometry.
- **Keep from later work:** the title screen (wordmark and background), the Sewers door, the wall torch, and the UI/talent icons. These were reviewed and approved.
- Remove the stage 6b–6c regional tile sets from shipped assets. They remain in the render cache and git history.

### 3. Revert characters, mobs, and splashes to upstream art
- All hero, mob, boss, and minion sheets revert to upstream Shattered Pixel Dungeon art (GPL-3.0, already credited), scaled cleanly to the current frame geometry with no restyling.
- The three new classes (Necromancer, Enchanter, Psychic) and their minions reuse and **recolor** existing upstream sheets, choosing the closest silhouette (e.g. a robed caster base for Necromancer and Enchanter). Recolor only — no new geometry, no vector reconstruction.
- Class splashes: new classes reuse the closest upstream splash, or display no splash panel, until real art exists. Do not generate splashes.
- **Test 44:** every hero and mob sprite's pixel content derives from an upstream sheet or a palette-swap of one; no procedurally generated character art ships.

### 4. Terrain distinctness gate (the check that was missing)
- **Test 45:** in a lit in-game screenshot of a generated room containing at least four terrain types, every pair of terrain types present must differ by **either** ≥ 0.12 mean luminance **or** ≥ 40° mean hue. Props and decor count as a type. Run per region; all five must pass.
- This replaces the per-tile isolated comparisons as the primary readability check. A tile set that passes every other art test and fails this one is not shippable.

### 5. Title menu cleanup (completing §13.6)
- Remove the **Feedback**, **News**, and **Changes** buttons entirely.
- Replace **About** with **Credits**: Oleg Dolya (Pixel Dungeon), Evan Debenham and contributors (Shattered Pixel Dungeon), links to their projects, the GPL-3.0 notice, and a link to this repository. Nothing else.
- Keep **Enter the Dungeon**, **Rankings**, **Journal**, **Settings**. Journal is in-game content and stays; verify it links nowhere external.
- **Test 46 (replaces the grep-based test 35):** no scene contains a control that opens an external URL other than this repository, and no scene fetches from a host other than this repository's. Assert by inspecting handlers and network calls, not by string matching.

### 6. Verify playability
- After the above, produce a lit screenshot per region and a short recorded sequence (or a series of stepwise screenshots) of a hero walking a corridor, turning, and opening a door, so the remembered-terrain fix is visible in motion.
- Run the full acceptance set. Tests whose subject was reverted (region iteration histories, liquid animation, character readability) are marked **RETIRED — superseded by recovery** in the report rather than failed, with a one-line reason.

## Out of scope for this stage
- Any generative or iterative art production.
- Any new art direction work. The Diablo-style look is deferred to a separate effort outside this pipeline.
- Any gameplay, balance, or content change. Classes, talents, items, curses, mobs, and §9 content are unchanged.

## Acceptance
Tests 43, 44, 45, 46 pass. Tests 1–42 pass, carry a permanent known-issue entry, or are marked retired-by-supersession. CI green. The delivery report states which applies to each.

Tag `v1.0.1-recovery`.
