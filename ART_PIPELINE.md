# Reproducible art pipeline

The v4.0.0 integration adds 24 editable palette-vector specifications for changed special terrain, interface icons and quest/enemy sprites, and updates the existing item source. All 97 current specifications rebuild byte-for-byte. `python tools/artgen/import_upstream_layout.py --upstream-v4` recreates this import from pinned commit `2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f`; normal builds consume only committed JSON and cached renders. Original alpha is retained, including occlusion shadows. The import preserves the five approved region atlases, original spell IDs and class item slots 512-521; four elemental summon variants occupy 522-525. These GPL upstream silhouettes are provisional game art, excluded from CC0 reference optimization, and do not certify the later native 64px character/item redesign. Full validation still reports 77 uncovered sheets and unfinished style/frame gates.

Stage 1 generates Sewer tile/wall atlases, six upstream hero atlases (48x60 frames), the item atlas (32x32 frames), Grimhollow banners and a stone/bone title composition, launcher icons, a vignette and blood decals. It is **not the complete §5 asset set**. Remaining assets are upstream or unimplemented; the default validator reports incomplete coverage as a failure.

Requires Python 3.11+ (host: 3.12.14), Pillow 12.3.0, NumPy 2.3.5:

```powershell
python -m pip install -r tools/artgen/requirements.txt
python tools/artgen/build.py
python tools/artgen/validate.py
```

Host Python: `C:\Users\Hartl\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`. Add its directory to PATH or invoke explicitly.

Each `tools/artgen/specs/*.json` supplies output dimensions, palette roles, silhouette primitives, seed, and animation transforms (empty for static sheets). Sewer silhouettes are editable horizontal vector runs converted from GPL upstream v3.3.8. The normal build reads JSON only and uses coordinate-hashed grain, palette shading and per-frame exposure limits. It never downloads art or calls a model. Icons are original ellipse/polygon/rectangle primitives. `import_upstream_layout.py` reproduces the initial JSON import from the pinned Git commit; do not rerun it after editing those JSON sources.

Terrain frames are 64×64 with upstream indices preserved in 1024×1024 sheets. Terrain and walls are separate drop-in files. Logical world units remain 16 per tile to preserve upstream camera, physics and UI behavior. Texture frame resolution is independent. The present art retains upstream silhouettes and is a first-pass recoloring/texturing, not the completed art direction.

`build.py` skips both `name.png.lock` and `name.lock`. Locked human replacements require review rather than automatic certification. PNG bytes contain no timestamps. Use pinned dependency versions for byte-identical results; Pillow generates the ICO/ICNS containers too.

`validate.py --generated-only` is a **subset diagnostic** for dimensions, CIE76 palette distance, per-tile luminance, nonempty outputs and source reconstruction. CI uses the full default command. Character outlines, occupancy, animation-index coverage and item semantics remain unfinished and reported; a subset pass does not satisfy tests 4, 16 or 17.

Hero sources keep upstream animation/armor indices, fit the silhouette into a 48x60 frame and add a 2px dark outline. Items retain atlas indices at twice the upstream texture size; their logical footprint is 8x8. Title lettering is the explicit exception to the no-text asset rule in section 5.4.

Stage 2 adds a transitional Necromancer sheet from the Mage vector silhouette, 24 Necromancer talent pictograms, and an appended item row for Bone Rod, Phylactery and class armor. Full new-character and minion art remains stage 3. All 47 generated specifications reconstruct from JSON; the full inventory gate remains intentionally failing.

## Calibrated Sewers iteration (stage 5.7)

Blender 4.5.13 LTS remains at `.toolchain/blender/blender.exe`. The six JSON files in
`tools/artgen/blender/params/` drive floor, wall, water, door, rubble and wall torch.
Camera, tile framing and palette limits stay fixed. Door, decor and torch keep their
approved stage-5.6 geometry as round zero. Character rigs, grass and other regions
are unchanged. CI consumes committed render-cache PNGs and never needs Blender.

```powershell
python tools/artgen/build.py --render --asset sewers
# Render only active classes after others converge; comma-separated names are supported.
python tools/artgen/build.py --render --asset floor,wall_torch
python tools/artgen/iteration.py --preview --class floor wall_torch
# View .local/iteration-review.png before writing a judgment for each active class:
# {"floor":{"score":0.8,"reason":"One-line visual judgment","gap":"Next material change"}}
java '-Dgrimhollow.iteration=true' -jar desktop/build/libs/desktop-0.4.3.jar --smoke-sewers
python tools/artgen/iteration.py --round 1 --class floor wall_torch --vision .local/vision.json
python tools/artgen/iteration.py --summary --check
python tools/artgen/validate.py --rebuild
python tools/artgen/validate.py --rerender --generated-only
```

Use the next unused round number. Every class gets at least six rounds; stop when
its composite is at least 0.90 and all gates pass, or at round fourteen. The weights
are numeric 0.30, structural 0.30 and vision 0.40. Every round retains its parameters,
three-variant measurements, cached-frame hashes, actual room measurements, render
and at-most-three-line critique with the vision number and reason. A wrong-kind
judgment cannot exceed 0.30. The highest-scoring passing round is selected, with
latest round breaking ties. Raw caches for selected rounds are committed; rerendering
is subject to the existing two-bit pHash tolerance rather than byte identity.

`summary.png` shows the top reference subject, procedural source, approved pass as
round zero, best passing round and score history. It also lists each class's first
vision judgment above 0.7. Historical stage-5.6 files remain in each class's
`stage-5.6/` directory; the original `verification/render-poc.png` is preserved.

Floor geometry uses periodic unequal Voronoi polygons with narrow mortar. Its
mineral and moss material fields are periodic. Walls use horizontal courses with
explicit top and damp value bands. Water is a continuous mesh with a broad depth
field and smooth neutral reflected light. The postprocessor preserves absolute
surface values; water uses a continuous tone curve within the unchanged CIE76
palette limit. Full material relief is fitted into partial-height wall and bank
components, keeping upstream atlas indices and logical footprints. No downloaded
reference pixels or material maps are baked into assets.

Structural measurements use RGB luminance weights 0.2126/0.7152/0.0722. Floor
orientation uses nine 20-degree bins, none above 30%; wall horizontal energy uses
edge directions within 22.5 degrees of horizontal. The damp-band wording is read
conservatively as at least 0.10 darker than the body. Stone areas come from eroded
cores grown back within the original stone mask, separating one-pixel sampling
necks without inflating individual stones. This correction begins in round six;
previous rounds retain their original measurements. Alpha is excluded from prop
statistics. Torch references are cropped to the visible subject; decor's material
scan fills its subject box. Seams compare wrapped gradients with interior p95 plus
one quantization byte; props require a transparent perimeter. Every round also
validates the complete implemented 64-specification atlas subset, including partial
components. The separate full inventory gate remains active and failing for future
art coverage.

Board numeric scores retain the original fixed 7x7 sample and actual runtime light
equations. They are distinct from the required global and room gates, which use
actual OpenGL screenshots and camera-derived visible-cell masks. The generated
WaterBridgeRoom is seed 417, bounds (1,15)-(10,22), containing fifteen water cells,
a bridge, two doors, two rubble cells and one torch. Terrain is unchanged; the smoke
fixture poses the hero, a passive rat and a healing potion on three floor cells.
Lighting is on and zoom is the runtime default. Settings are restored afterward.

The six room measurements use all visible cells: wall/floor population means and
class-median representative cell pHashes; median flagstone area at screen resolution;
median differences across every adjacent water/floor pair; four-neighbour outer and
inner visible rings; visible screenshot mean and standard deviation; and p95 absolute
luminance differences in each subject box against a same-frame terrain-only capture.
The paired capture redraws without updating or moving the subjects. Wall layers now
sample the same cached light map as the floor, preserving draw order for occlusion.
This shared fix now applies to Sewers and Prison; light radii stay unchanged.

The raw screenshot is `verification/iteration/sewers-ingame.png`; the paired background
is `sewers-terrain.png`, with camera masks in `room.json`. `sewers-gate.png` prints all
six measurements beneath the screenshot, and `room-gate.json` records their values.
Test 39 validates current shipped variants against the chosen rounds; test 40 checks
all six screenshot gates. CI runs both on committed evidence and repeats the live room
measurements, including the halo, after its standard Linux OpenGL geometry capture.
This room gate does not certify the separate five-region or mob-histogram tests.

Selected rounds: floor 07 (0.931), wall 06 (0.929), water 05 (0.905), door 06 (0.957),
decor 05 (0.920), torch 13 (0.863). Recorded counts are 7/6/6/6/6/14. First vision above
0.7 is round 1 for every class except water, which reaches it at round 4. The torch
ends at the fourteen-round limit: the existing radius-three source still has residual
light at two tiles, and distant walls remain warm rather than blue. These numeric
reference misses are retained; no targets were reduced to certify convergence.

## Stage 6a: animated liquids

The v0.8 review locks floor, wall, door, decor and torch from stage 5.7. Their
parameter files, raw renders and packed Sewers terrain are pinned in
`tools/artgen/blender/locks.json`; Blender skips these approved sources.
The rejected static water rounds remain historical evidence only.

`python tools/artgen/build.py --render --asset liquids` renders four variants by
eight frames of sewage, clean water and lava, plus eight entry-ripple frames.
`blender/liquids.py` directly authors displaced surfaces and disconnected, open
crest ribbons; no reference pixels or parameter-search loop are used. Runtime
variant/phase hashing breaks both horizontal and diagonal synchronization. Sewers
uses sewage; Prison, Caves and City use clean water; Halls uses emissive lava.
The terrain remains upstream water for all movement and interaction rules.

`python tools/artgen/iteration.py --liquids --check` validates every frame and
requires a recorded room vision judgment rejecting visibly repeated features.
Autocorrelation retains the 0.35 ceiling, measured on the actual mixed variant/phase
3x3 runtime composition: identical tile copies would necessarily correlate 1.
Two room reviews rejected the specified centre-depth contrast; the accepted numeric
adjustment is 0.02-0.04, preserving the gradient without tile outlines.
Critiques and the review sheet use the existing round format under
`verification/iteration/liquids/`.

`python tools/artgen/validate.py --generated-only --rerender --asset liquids`
checks all 104 new raw frames against the two-bit pHash tolerance, then restores
the reviewed cache and rebuilds it. CI uses only committed renders; it runs test 41
on both desktop platforms and the runtime phase checks in the existing Linux
OpenGL runner. No Blender installation is required by CI.


## Stage 6b: Prison

Prison has independent parameters under `blender/params/prison/`, authored models
in `blender/regions.py`, and eighteen visible frames plus six material masks under `render_cache/prison/`.
The dressed floor and heavy courses use dry grey stone; the door is an iron
barred gate, decor is linked manacles, and the light source is an enclosed candle
lantern. Door and prop geometry is fixed after round zero; later rounds adjust
materials and value range. `--render --asset prison` scopes Blender to these
sources and never touches the locked Sewers cache.

`python tools/artgen/iteration.py --region prison --check` checks the five class
histories and the actual lit Prison room. The existing desktop runner accepts
`-Dgrimhollow.region=1` with `-Dgrimhollow.iteration=true` and `--smoke-sewers`.
Evidence and the comparison sheet are under `verification/iteration/prison/`.
CI validates the committed evidence on both desktop platforms and repeats the
live Prison room gate on Linux. Stage 6c extends test 42 to the other three regions.

Prison selected rounds: floor 06 (0.952), wall 05 (0.946), door 08 (0.926), decor 07 (0.929), lantern 05 (0.944). Recorded counts are 6/6/8/7/6; all classes first exceed vision 0.7 at round 1. The remaining door iron/wall reference score misses the 0.15 gap by 0.014, retained in its critique rather than lowering the target.

## Stage 6c: Caves, City and Halls

Each region has its own floor, wall, door, decor and light-source parameters under
`blender/params/<region>/` and 24 raw frames/material masks in its render cache.
Caves uses packed earth, fractured rock and timber supports. City uses worn inset
slabs, dressed courses, arches and paired candles. Halls uses dark dressed stone,
obsidian gates, inscribed ritual rings, bone furnishings and braziers.

`python tools/artgen/build.py --render --asset caves,city,halls` regenerates only
these regions. The locked Sewers sources and the accepted Prison cache are not
rendered. `python tools/artgen/validate.py --rebuild --rerender --asset caves,city,halls`
checks all 72 regional frames, restores the reviewed cache, and checks the normal
post-process rebuild. Full art coverage remains the separate stage-6e gate.

Use `iteration.py --region <region> --check` for each class history and its six
room measurements. The existing OpenGL runner accepts region indices 2, 3 and 4
for Caves, City and Halls. It selects enclosed, unmodified generated rooms with
an inward-facing light fixture, and records all visible cells and paired terrain
pixels. The room captures, measured images and per-class round histories remain
under `verification/iteration/<region>/`. CI checks committed evidence on both
desktop systems and renders all five live rooms on Linux without Blender.

City uses the blue-grey ambient specified by the board and wider light pools.
Halls lava retains its cached emission while lighting nearby terrain. Numeric
clarifications for the dark Halls range are listed individually in CHANGES.md.

Selected regional rounds: Caves 06 for all five classes (scores 0.913-0.948); City 12 (0.944-0.952); Halls 12 (0.928-0.948). Recorded counts are 6, 12 and 12 per class respectively; every class first exceeds vision 0.7 at round 1. All five current room gates and all 25 class-history checks pass; liquid review 07 covers the final five-region captures.

## Stage 6d: native character silhouettes

`python tools/artgen/character_catalog.py` compiles the original material/form catalog
and the existing Java animation indices into 78 committed character specifications.
`python tools/artgen/build.py` paints their vector geometry directly at 48x60 or
96x96, emits shared runtime frame metadata, and recreates `verification/characters.png`.
There are no enlarged upstream pixel shapes in these character sources. All nine
heroes retain eight armor rows; the existing sprite classes retain animation indices
and timing. The painter supplies distinct idle, movement, action and death poses,
three/four material tone bands, a two-pixel dark outline and a soft ground shadow.
Reference-board D's armor planes, pointed hoods, shoulder drapes and restrained folds
guide the silhouettes. Bone, iron, cloth, fur and magical accents remain palette based.

The existing validator checks all native frames, standing height occupancy, complete
outlines, animation variation, palette distance and the unchanged test-30 hue threshold.
The regional populations include rare and quest enemies; Vault-only variants are
compared in City. Armor/enraged states of the same species are compared against other
species, not against themselves. The explicit unused Vault UNSTABLE enum slot has
frames but introduces no new encounter. Tests 24-26 and 34-36 still use the existing
OpenGL renderer and all 112 concrete sprite classes in its established inventory.

The old Blender character rigs live in `blender/experimental/`; explicit commands such
as `build.py --render --asset experimental/rat` can reproduce historical POC sources.
They are not called for shipped characters. Shared environment mesh primitives were
moved unchanged into `blender/primitives.py`; all environment render caches, including
the locked Sewers classes, retain their approved bytes. Full items and props coverage
remains the next stage's full-validator gate.


Stage 6e renders every public item index from `blender/items.json` with the original
mesh profiles in `blender/props.py`. Recompile the catalog with
`python tools/artgen/item_catalog.py` when Java item constants change; unknown
identities fail explicitly. `build.py --render --asset items,surfaces,title` updates
only those caches. The normal build packs 32px items with three source material
colors, a two-pixel outline and centered bounds; it never needs Blender.

Special-room atlases retain the GPL v4 semantic contours and cell IDs, with new
64px rendered relief materials. Their logical cells remain 16 units. The original
Sewers floor/wall/door/decor/torch caches and atlases are unchanged. Mathematical
occlusion/grid masks retain their pixel geometry. Native 32px semantic talent and
identification pictograms are drawn by `glyphs.py`; logical UI sizes remain 16/8.

The title uses an original extruded blackletter alphabet (heavy stems and fine
joiners, with only the G ornamented) and a modeled ruined nave. Required title and
end-state lettering is the explicit exception to the no-asset-text rule. Baseline
upstream FX shapes are reproducible palette-vector sources for the off path; the
next stage adds the enhanced rendered FX without changing mechanics.

Stage 8 adds Hexcaster and Chainwarden as native character specifications, plus
11 new Blender item models at unused indices 526-536. There are now 80 character
atlases, 380 distinct props and 381 public item indices; the four v4 elemental
summon spells retain 522-525. Leather variants reuse their parent mesh seed and
variant and change only materials. Bone Armor adds an ivory rib cage. Chainwarden
uses turquoise steel, an amber tabard and prison chains to distinguish it from
ordinary guards and thieves without changing the hue-distance gate.
