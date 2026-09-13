# Painted presentation pass

The September 2026 visual-overhaul request supersedes the recovery run's prohibition on new art. Gameplay, content, animation timings, camera, input, logical 16-unit tiles and fog sampling remain unchanged.

Original painted material and prop source sheets are made with the built-in imagegen tool. Exact prompts are in `prompts.json`; selected, unmodified tool outputs are committed in `sources/`. They contain no Diablo assets. The direction uses worn natural materials, broad value groups, restrained colour and warm light.

Source generation is an authored step, not a reproducible AI call. The offline atlas packer is deterministic from the committed source sheets and pinned Git templates. CI never invokes a generator, API, Blender or the rejected artgen loops. Packing consists of cropping, resizing and applying the game's existing tile silhouettes/cutouts. It does not change terrain selection or game rules.

Judge the result in real lit rooms, at play scale. A colour statistic is supporting evidence, not aesthetic approval. Preserve existing title artwork, UI/talent icons, item semantics, and the approved Sewer doors and wall torches. Animated characters and special quest art stay intact until there is a coherent replacement with all required frames.

Run: python tools/painted/pack.py (write) or python tools/painted/pack.py --check (verify only). Requires Pillow 12.3.0 and numpy 2.3.5. Thirteen shipping atlases reconstruct from eight source sheets and pinned Git layout templates. New artwork is distributed with this project under GPL-3.0-or-later. Image generation is not repeated in CI.
