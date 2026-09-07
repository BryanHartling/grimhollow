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
