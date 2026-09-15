# Class paintings, trap plates and launchers - v1.6.0

`presentation.py` compiles eleven new authored sources into nine 1600x900 selection paintings, one 384x384 portrait sheet, 63 existing trap cells, and 55 desktop/Android launcher resources. `presentation-prompts.json` records the exact built-in imagegen prompts. The face crop in each of the nine 128px portrait cells is taken directly from that class's painting. Trap shape and color indices, disabled states, and terrain below the seven trap rows are preserved. Android foreground artwork fits the 66dp adaptive safe zone; debug builds use the same recognizable emblem.

Run `python tools/painted/pack.py` to package and `python tools/recovery_assets.py --check` to verify all source-derived assets. The packer reconstructs 112 game images from 96 source sheets plus 55 launcher resources. Source generation is an authored step; packing and CI use only committed inputs. These original source paintings and their derived assets are distributed under GPL-3.0-or-later with the project.

World creature atlases retain their painted key poses. The runtime holds enemy idle frames steady while advancing action animations unchanged; no creature or balance regeneration occurs.

# Inventory and interface continuation - v1.5.0

`interface_art.py` packs three authored source sheets into shared bronze/leather and pewter nine-patches, enamel controls, glass status bars, equipped-slot borders and 32 navigation glyphs. `PaintedInterface` keeps four texture pixels per existing UI unit and preserves logical margins, hit areas and layout. `interface-prompts.json` records the exact prompts and glyph order. Native screenshots in `verification/interface/` exercise the real windows in landscape and portrait, including long scrollable item descriptions and the five-spell wheel.

`inventory.py` and `inventory_families.py` now cover all 381 public item names at their original IDs: 380 distinct 64px cells, with DART/DARTS remaining aliases. Seven added sheets supply all artifact states, coated-dart silhouettes, common/exotic rune scrolls, botanicals, stones, alchemy, crafted spells, foods and quest remnants. `items-continuation.json` pins the accepted cells; the last crafted-spell cell was rejected and its elixir comes from `quest-remnants` instead. Glass and wax recoloring preserves the game's identification families; monochrome holders reuse the corresponding authored item. The independently packed test-25 hashes include each cell's texture size. The 60 small identification overlays remain unchanged.

The offline packer reconstructs 102 shipping images from 85 source sheets. New sources and exact prompts are committed; no generation API or Blender is needed in CI. The old sections below describe earlier checkpoints.

# Living dungeon continuation

`monsters.py` packs 129 authored forms/states into all 71 shipping creature atlases. `monsters.json` pins source rows, native frame sizes and exact indices; untouched rectangles come from `v1.2.0-painted-assets`. Source alpha connectivity extracts complete sprites even where a limb crosses a nominal grid boundary. Packing fits all poses at one scale per form, anchors their feet, and removes faint downsampling residue. Small offline pose offsets supply breathing/stride variation without changing game animation definitions. Shared character draw UVs have half-texel guards, preventing smooth sampling from leaking another row into a pose while retaining the public frame rectangles. `monsters-prompts.json` records all 34 accepted creature source sheets. The rejected opaque checkerboard variant was not imported.

Health, target and status widgets use cached standing-body alpha bounds so oversized transparent pose cells do not move labels away from the creature. This changes display layout only.

`title.py` packs the crypt painting, transparent engraved wordmark and mist ribbon. `TitleBackground` animates mist, brazier halos and embers from elapsed display time without using gameplay randomness. Existing menu handlers are unchanged. Exact source prompts are in `title-prompts.json`.

# Door and vegetation continuation

`terrain_details.py` assembles matching door families and three readable vegetation states from two new source sheets. `details-prompts.json` records the exact built-in prompts. The upper/lower vegetation slices share one source silhouette, and sideways door thresholds are paving rather than another leaf. Existing terrain IDs, overlays and gameplay transitions remain unchanged.

# Painted presentation pass

Inventory continuation: `inventory.py` replaces 202 declared atlas cells from twelve original painted sheets plus the hero armor sources, and reconstructs the test-25 reference hashes from those sources. The pinned names, IDs, art-index remaps, all 60 identification icons and all remaining item pixels stay unchanged. `items.json` and `items-prompts.json` identify each source cell. Four chest cells now share their closed art with the matching mimic variants. Horn/chalice/rose upgrade families and coated darts retain their existing art. Waterskin cell 480 now depicts a capped leather water canteen. The packer removes alpha below 8/255 after downsampling so detached Lanczos residue cannot inflate runtime occupancy bounds; meaningful antialiasing remains. No stats or item behavior change.

The follow-up pass extends this approach to nine heroes. `actors.py` packs 21 poses across eight armor rows from ten painted cutout source sheets. The original HeroSprite pose indices, frame rates and callbacks remain intact. Cloth, leather, mail, scale, plate and class armor use distinct authored torso pieces; heads, sleeves and capes retain each class identity. The offline rig is an atlas compiler only and does not run in the game. Exact prompts are in `actors-prompts.json`.

The September 2026 visual-overhaul request supersedes the recovery run's prohibition on new art. Gameplay, content, animation timings, camera, input, logical 16-unit tiles and fog sampling remain unchanged.

Original painted material and prop source sheets are made with the built-in imagegen tool. Exact prompts are in `prompts.json`; selected, unmodified tool outputs are committed in `sources/`. They contain no Diablo assets. The direction uses worn natural materials, broad value groups, restrained colour and warm light.

Source generation is an authored step, not a reproducible AI call. The offline atlas packer is deterministic from the committed source sheets and pinned Git templates. CI never invokes a generator, API, Blender or the rejected artgen loops. Packing consists of cropping, resizing and applying the game's existing tile silhouettes/cutouts. It does not change terrain selection or game rules.

Judge the result in real lit rooms, at play scale. A colour statistic is supporting evidence, not aesthetic approval. The latest review authorizes replacing the title and door artwork. Preserve UI/talent icons, item semantics and approved wall torches. All 71 shipping creature sheets now use painted art: 129 forms including friendly summons, NPCs, rare variants, six statue tiers, ward tiers, holiday Rat Kings and Vault encounters. Shared reflections and revenants use painted hero/ghoul sheets. Unreferenced historical hero aliases and class splashes retain their approved previous art. Mimic disguises share their authored closed poses with the matching chest items.

Run: python tools/painted/pack.py (write) or python tools/painted/pack.py --check (verify only). Requires Pillow 12.3.0 and numpy 2.3.5. Ninety-nine shipping images reconstruct from seventy-five source sheets and pinned Git layout templates. New artwork is distributed with this project under GPL-3.0-or-later. Image generation is not repeated in CI.

The 1.4.0 pass adds 86 painted status emblems and three overhead symbols, retaining 7/16 logical HUD dimensions and dynamic buff colors. Creature display heights now distinguish heroes (18.125 world units), humanoids (14.5), rats (8.15625) and large creatures/bosses; collision stays at 16 world units. Torch foreground pixels are isolated with the existing committed render alpha mask and composited over the current regional wall. No renderer or generation loop runs during packaging. `painted_rects` in the output manifest lets the GPU acceptance check reject untouched animation/variant frames.
