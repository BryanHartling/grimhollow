# Reproducible art pipeline

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

## Sewers reference iteration (stage 5.6)

Blender 4.5.13 LTS remains at `.toolchain/blender/blender.exe`. Six parameter files in
`tools/artgen/blender/params/` drive floor, wall, water, door, rubble decor and the static
wall-torch prop. The original camera, tile framing and palette stay fixed. Character rigs
and grass remain the POC until their separate approved stages. CI reads the committed
render cache and does not install or invoke Blender.

```powershell
python tools/artgen/build.py --render --asset sewers
python tools/artgen/iteration.py --round 1
# Inspect .local/iteration-review.png; write each round-NN.md (maximum three lines).
# Adjust only the class JSON parameters, render again, and record the next round.
python tools/artgen/iteration.py --check
python tools/artgen/validate.py --rebuild
python tools/artgen/validate.py --rerender --generated-only
```

Recorded rounds are immutable. Use the next unused round number (maximum 12); `--class`
records only that class. `--asset water`, `--asset decor`, and `--asset wall_torch` render
one class; `--asset door` includes both door states. Stop a class at composite >= 0.85 with
all applicable global checks passing, otherwise keep its best passing round at round 12.
The review summary preserves procedural and POC round-zero pixels and shows the best
passing round. There was no dedicated decor or torch render in the POC; those columns
show the actual former atlas frames. `verification/render-poc.png` is historical evidence
and is no longer overwritten by ordinary rebuilding.

Metrics use RGB luminance weights 0.2126/0.7152/0.0722, matching the existing validator.
Surface contrast is the p85-p15 dark-joint/stone separation; stone counts come from the
constructed mesh. Moss is segmented by green hue and flame by its location above the
holder. References without numbers inform visual critiques, not invented numeric scores.
Room-wide means/hue use the fixed 7x7 terrain composition in `iteration.py`, the actual
Sewers ambient and the existing hero/wall-source equations. Transparent prop backgrounds
are composited onto their terrain. This regional statistic is distinct from individual
water/stone luminance targets and does not certify five-region test 18. Character histogram
readability is outside this six-class loop; test 30 remains pending.

Every round checks all three variants for dimensions, alpha, palette distance, tile
mean/std and seams. Seam measurements compare the joined edge against interior edge
variation in a 2x2 tiling; isolated props must have a transparent perimeter. The shader
uses periodic coordinates and water is a single connected surface over all nine tiles.
No reference pixels or downloaded material maps are baked into game assets.

For the lit review screenshot, reuse the existing OpenGL runner:

```powershell
java -Dgrimhollow.iteration=true -Dgrimhollow.geometryTests=true -jar desktop/build/libs/desktop-0.4.2.jar --smoke-sewers
```

It selects an actual generated WaterBridgeRoom with water, a traversable bridge, door,
rubble and one decorative wall source, and changes no terrain cells. The captured frame
uses dynamic lighting and default zoom; settings are restored. The result is
`verification/iteration/sewers-ingame.png`. Enhanced animated effects remain stage 7.
