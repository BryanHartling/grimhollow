# Painted presentation pass

Inventory continuation: `inventory.py` replaces 202 declared atlas cells from twelve original painted sheets plus the hero armor sources, and reconstructs the test-25 reference hashes from those sources. The pinned names, IDs, art-index remaps, all 60 identification icons and all remaining item pixels stay unchanged. `items.json` and `items-prompts.json` identify each source cell. Chests/mimics, horn/chalice/rose upgrade families and coated darts stay together on their existing art; their unused source concepts remain available for the later companion pass. Waterskin cell 480 now depicts a capped leather water canteen. The packer removes alpha below 8/255 after downsampling so detached Lanczos residue cannot inflate runtime occupancy bounds; meaningful antialiasing remains. No stats or item behavior change.

The follow-up pass extends this approach to nine heroes. `actors.py` packs 21 poses across eight armor rows from ten painted cutout source sheets. The original HeroSprite pose indices, frame rates and callbacks remain intact. Cloth, leather, mail, scale, plate and class armor use distinct authored torso pieces; heads, sleeves and capes retain each class identity. The offline rig is an atlas compiler only and does not run in the game. Exact prompts are in `actors-prompts.json`.

The September 2026 visual-overhaul request supersedes the recovery run's prohibition on new art. Gameplay, content, animation timings, camera, input, logical 16-unit tiles and fog sampling remain unchanged.

Original painted material and prop source sheets are made with the built-in imagegen tool. Exact prompts are in `prompts.json`; selected, unmodified tool outputs are committed in `sources/`. They contain no Diablo assets. The direction uses worn natural materials, broad value groups, restrained colour and warm light.

Source generation is an authored step, not a reproducible AI call. The offline atlas packer is deterministic from the committed source sheets and pinned Git templates. CI never invokes a generator, API, Blender or the rejected artgen loops. Packing consists of cropping, resizing and applying the game's existing tile silhouettes/cutouts. It does not change terrain selection or game rules.

Judge the result in real lit rooms, at play scale. A colour statistic is supporting evidence, not aesthetic approval. Preserve existing title artwork, UI/talent icons, item semantics, and the approved Sewer doors and wall torches. Monster/NPC sheets, class splashes and special quest art stay intact until there is a coherent replacement with all required frames.

Run: python tools/painted/pack.py (write) or python tools/painted/pack.py --check (verify only). Requires Pillow 12.3.0 and numpy 2.3.5. Twenty-three shipping atlases reconstruct from thirty source sheets and pinned Git layout templates. New artwork is distributed with this project under GPL-3.0-or-later. Image generation is not repeated in CI.
