# Regional scenery - v1.29.7

`regional_scenery.py` packs the original built-in imagegen paintings in `sources/regional-v1297/` into the Caves/City boss and Imp quest atlas contracts. Exact prompts and GPL-3.0-or-later provenance are in `regional-v1297-prompts.json`. Gate and stairs are continuous paintings split across their existing footprints. Statue/prop cells carry genuine alpha, without inherited pixel backgrounds. `quest_rooms.py` uses the same authored rectangular quenching trough for the forge. `pack.py --regional` packages this component and inspection exports; the normal full offline provenance check remains required. CI requires neither generation nor Blender. Logical world cells remain sixteen units.

# Artwork inspection - v1.27.0

`artwork_previews.py` deterministically exports 1,128 256px inspection images from the existing paintings; `pack.py --previews` writes only `artwork/`, its rectangle lookup and the manifest. Full `python tools/recovery_assets.py --check` reconstructs **1,283 assets from 161 source sheets**, plus 55 launcher resources, and checks the lookup bytes. No image generation or Blender is needed. Existing source licenses and project GPL-3.0-or-later apply.

Inventory packing now takes an optional cell size, including the same family tint/coating rules and aliases. Its default 64px export is unchanged. Traps likewise support a larger export with the same shape/color contract. Creature previews select the same source pose as the displayed animation rectangle. Status tints remain runtime-controlled. Terrain upgrades require an exact match to a shipped material cell; custom composites retain their current texture/frame. The lookup contains no item-type discovery logic. Hero face crops lead to their existing 1600x900 class paintings.

Runtime loads one picture only while its modal is open; it participates in Android texture reload and is removed/disposed on close. The original image scale, logical units, filtering and gameplay assets remain untouched. Original paintings bound the available detail: enlargement does not invent missing details in retained small assets. The additional PNGs add about 73 MiB uncompressed to the asset set.

# Mystery presentation - v1.26.0

`mystery.py` cuts one authored Sewers descent gateway into the existing three-by-five logical footprint, with a locked-state center composed from the previously approved padlock painting. `playtest_presentation.py` replaces only the royal cushion cell; `botany_skills.py` places the new Rebuff at the stable WRENCH icon index. Sources and exact built-in imagegen prompts are in `sources/mystery/`. `pack.py --mystery` exports this component; full `--check` verifies all 155 assets, 161 sources and launcher resources without generation. Artwork is released with the project under GPL-3.0-or-later.

# Bone-wall presentation - v1.23.2

`bone_wall.py` packs four authored sections from `sources/terrain/bone-wall.png` into `environment/painted_bone_wall.png`. The exact built-in `image_gen` prompt, transparency setting and GPL-3.0-or-later release are recorded in `bone-wall-prompt.json`. The wall's cell chooses one stable arrangement; there is no continuously cycling wall animation or runtime generation. The normal full offline provenance gate verifies the atlas.

# Equipment grip correction - v1.23.1

`talent_trim.py` draws original GPL-3.0-or-later bronze talent frames, round rank/point studs and a shuffle medallion into `interfaces/painted_talents.png`. It uses deterministic antialiased vector geometry and the existing authored panel trim; the normal offline packer and provenance validator reproduce it without an image generation service.

`hero_equipment.py` anchors equipment to the visible palms in all eight armor rows. Handle coordinates are reviewed against opaque pixels in the existing inventory paintings; curved blades use their shaft/collar axis. `HeroEquipment` carries short blades downward and hooked blades, rods and polearms upright beside the body, with unchanged target-facing attacks. The existing native gate includes Sickle and War Scythe, all eight rows, both facings and movement/attack poses. No painting or animation atlas was regenerated.

# Playtesting presentation - v1.23.0

`playtest_presentation.py` packs committed cutouts from `sources/playtest-v123/` into the mouse pointer, stair direction marker, Distant Well and Rat King statue. Exact built-in generation/edit prompts are in `playtest-v123-prompts.json`; the original statue edit source is retained. All are GPL-3.0-or-later. Unchanged Rat King pillow/decor cells come from the existing immutable recovery base. `journal.py` uses the same well painting for its landmark. `identification.py` preserves the sixty separate paintings in a restrained high-contrast ivory/gold range. The full offline check is unchanged.

`equipment-grips.json` now records handle and blade-tip coordinates. `HeroEquipment` aligns that real axis to the class pose and mirrors it with the hero. Existing native checks cover ten loadouts in both facings, including Bone Rod, spear and shortsword; no attack timing changes.

# Individual painted heroes - v1.22.6

All nine classes now use complete painted figures from `sources/hero-pilot/`, with eight fitted armor variants each. Exact built-in imagegen prompts and portrait references are in `hero-pilot-prompts.json`. Sources and derivatives are GPL-3.0-or-later. Each source was generated and reviewed as a separate hero batch; no generator is required in builds or CI.

`hero_pilot.py` uniformly fits each body, keeps faces rigid and idles identical, and deforms the existing action poses with restrained class-specific profiles. Integer determinant inversion on a fixed grid preserves cross-platform reproduction and rejects folded triangles. `pack.py --hero NAME` exports a single class; `--hero NAME --check` verifies it. The unchanged full CI command is `python tools/recovery_assets.py --check`.

`hero_equipment.py` exports pose hand anchors into `sprites/hero-grips.json`; `HeroEquipment` renders equipped, ability-selected and thrown gear using two reused quads from existing item and hero textures. Armor variants retain their painted empty hands and get fingers over the grip. No runtime mesh, new hero textures, actor, combat callback or timing change is introduced.

`hero_pilot.py --review NAME` exports armor comparisons, poses and animation GIFs. The portfolio also produces `verification/heroes/sprint-summary.png` and `sprint-ingame.png`, with labelled sizes and actual native crops. The existing native geometry runner exercises all nine bodies/loadouts and grass depth. Upper-body grass overlap is sorted behind visible actor silhouettes; lower blades remain around their feet, using one additional cached batch of the existing grass texture. Physical tablet review remains human playtesting. Test 45 remains abandoned.

The sections below record earlier releases; the current nine-hero exporter supersedes the two-hero and older cutout rigs.
# Adventuring Notes — v1.19.2

`journal.py` packs sixteen landmark paintings from `sources/sprint/journal-landmarks.png` into `interfaces/painted_landmarks.png`. The exact built-in imagegen prompt and row-major names are in `journal-landmarks-prompt.json`; source and derivatives are GPL-3.0-or-later. Treasury reminders reuse the three committed elemental mechanism paintings. Existing painted NPC sprites are fitted at runtime without animation padding. The offline packer and normal provenance check reproduce the assets without generation. Terrain contrast test 45 remains abandoned.

# Enchanter craft and elemental caches — v1.19.0

`journal.py` packs sixteen original navigation/category paintings and the existing leather/brass materials into the journal atlas, HUD buttons and key display. `botany_skills.py` replaces Overload with its own Spellguard painting. Sources are in `sources/sprint/`; exact built-in imagegen prompts are in `sprint-prompts.json`. New paintings and derivatives are GPL-3.0-or-later. The normal offline packer reproduces the outputs; UI geometry remains in the existing logical units.

# Terrain readability — v1.18.2

`terrain_details.py` packs the existing flattened-grass painting into a 60x34 footprint instead of 60x20. It remains below both standing states and exposes paving around the leaves. No source painting, terrain index, collision or alpha stencil is replaced; the standard offline packer reproduces the change. Regional ambient and personal-light corrections live in the renderer, preserving localized torch/fire colors and all visibility rules. Historical measurements remain archived; test 45 is now Abandoned: test failed and is not run.

# Playtest polish — v1.18.0

`sources/playtest-polish/` contains three original built-in imagegen paintings: ascending stairs, descending stairs, and the eating icon. Exact prompts and GPL-3.0-or-later provenance are in `playtest-polish-prompts.json`. `pack.py` composites stair frames 16/17/22 across the five region atlases and inherited expedition terrain, and exports the transparent 64px meal to `effects/painted_food.png`. Garden motes reuse the existing painted particle atlas. All outputs rebuild offline; world units and stair behavior are unchanged.

# Lurking Horror — v1.17.0

`sources/lurking-horror.png` is an original four-pose transparent painting produced with built-in imagegen. The exact prompt and provenance are in `lurking-horror-prompt.json`. `horror.py` reproducibly extracts the idle, travel, strike and collapsed poses with a shared scale and foot anchor into a 512px atlas. The sprite has a steady idle and a short attack animation; eightfold texture density preserves the existing logical world units. Source and derivatives are GPL-3.0-or-later. Fresh victim remains reuse the existing painted species death pose. `pack.py` and `recovery_assets.py --check` require no model, network or Blender run.

# Regrowth plants — v1.16.1

`botany_skills.py` now fills the two previously retained plant cells (Dewcatcher 125 and Seedpod 126) from individual transparent paintings. The dewcatcher uses blue-green cupped leaves and silver water drops; the seedpod uses a dry ochre three-capsule silhouette. Sources are in `sources/botany-skills/`; exact built-in imagegen prompts are in `regrowth-plants-prompts.json`. The offline packer fits each at the same 64px tile density and foot anchor as the other 13 plants. No harvesting code or plant index changes. New source paintings and derivatives are GPL-3.0-or-later.

# Dragon expedition - component 6

`expedition.py` packs six original built-in imagegen paintings from `sources/expedition/`: four dragon poses, four broodmother poses, the wounded hunter, timber, a single continuous hoard, and the expedition map. `expedition-prompts.json` records exact prompts, including the spider alpha cleanup. The source PNGs and derivatives use GPL-3.0-or-later. Alpha-connected extraction preserves wings/legs crossing a nominal source quadrant; all poses use one common scale. The two bosses have steady idle/travel poses and action-only animation. Existing creatures and regions are unchanged. `pack.py` and `recovery_assets.py --check` reproduce all outputs offline; no model or Blender runs in builds or CI.

# Tablet quest rooms and connected details - v1.14.2

`quest_rooms.py` restores the v4 Prison (16 columns) and Caves quest (4 columns) atlas layouts and packs three new authored sheets: `ritual-mark.png`, `tablet-room-props.png`, and `mine-workshop.png`. Exact prompts are in `tablet-rooms-prompts.json` and `mine-workshop-prompt.txt`. Their derivatives include a continuous five-cell ritual overlay with cardinal candle sockets, separate table, single mine hatch, furnace/workbench, and both mine tile atlases. Existing Caves materials, boulder and crystal paintings supply geology. Sixteen rail masks use edge-spanning modules and continuous shelf fronts replace isolated cabinets. The boss and floor HUD reuse established painted interface materials. Source PNGs and derivatives use GPL-3.0-or-later; generation is never required during packaging or CI.

# Painted effects and universal creature export - v1.14.0

`particles.py` packs committed neutral particle motifs, four colored beam ribbons and existing status/UI symbols into three 256px effect atlases. The shared PixelParticle/Speck paths use these textures only with enhanced effects enabled; lightning/rays retain their timing and logical geometry. `botany_skills.py` replaces Field Repair with the original Defensive Sigil icon. Exact built-in image-generation prompts: `particles-prompts.json`, `rays-prompt.json`, `defensive-sigil-prompt.json`. New source PNGs are in `sources/particles/`, licensed GPL-3.0-or-later.

All 71 creature atlases now use density 8 directly from existing original paintings, including NPCs, bosses, summons, sentries and wards. No animation callback or game rule changes in the packer. Runtime sizing includes the bilinear alpha fringe, while frame layouts are derived from the original source dimensions. The complete offline check reproduces **124 game images from 115 source PNGs**, plus **55 launcher resources**: `python tools/recovery_assets.py --check`.

# Regional paintings and Hatchling - v1.13.0

Five original paintings in `sources/regions` provide the Sewers, Prison, Caves, City and Halls transitions. `regions.py` crops them reproducibly to 1600x900; the runtime fills both landscape and portrait viewports around the central focal point. `sources/items/hatchling.png` supplies inventory cell 539. Exact built-in imagegen prompts are in `hatchling-region-prompts.json`; all six source images and their derivatives are GPL-3.0-or-later.

The raised skeleton atlas is packed at density 8 directly from the existing original monster painting, preserving its world size and action timing. The full offline check now reconstructs **121 game images from 112 sources**, plus **55 launcher resources**. Run `python tools/recovery_assets.py --check`; no image-generation service or Blender is needed in CI.

# Warrior / Enchanter anatomy pilot - v1.20.1

`hero_pilot.py` overrides only Warrior and Enchanter. Each source sheet in `sources/hero-pilot/` contains eight coherent, fully painted armor variants derived from the matching original selection portrait. The packer crops their transparent gutters, fits each figure uniformly and uses a small offline deformation mesh for existing poses. No shared chest panel is pasted over either figure. Idle frames are identical, faces remain rigid during walking, and legs/coat/arms use restrained class-specific motion. The other seven rigs and all runtime timings are unchanged. This is a bounded visual trial awaiting human review.

Exact built-in imagegen prompts and identity references are in `hero-pilot-prompts.json`; new sources and derivatives are GPL-3.0-or-later. `python tools/painted/pack.py --hero warrior` and `--hero enchanter` package the individual atlases. `python tools/painted/hero_pilot.py --review warrior` (or `enchanter`) reproduces comparisons against `5d1ecc2a7`, all armor rows, pose sheets and animation GIFs in `verification/heroes/pilot/`. GIF walking uses tablet cadence rounded to 10ms; combat poses use existing timings. Native screenshots come from the existing desktop smoke renderer, with optional `-Dgrimhollow.heroArmorTier=5` for plate.

# All nine hero rigs - v1.10.1

`hero_rigs.py` now contains eight individual profiles and the Necromancer rig, with class-specific body proportions, silhouettes, guards, footwork and action gestures. `caster-robes.png` supplies separate Mage/Cleric/Enchanter/Psychic garments; its right two columns split at y=526 rather than 512. `armor-front.png` provides solid chest panels without the old empty arm sockets. Existing heads, class colors, limb paintings and matching portraits are retained. Exact built-in imagegen prompts/references for all three new sources are in `hero-rigs-prompts.json`.

`python tools/painted/actors.py --review all` reproduces nine comparisons, pose sheets, GIFs and the lineup. `python tools/painted/pack.py --hero NAME` packages one isolated character batch; the CI command remains the full `python tools/recovery_assets.py --check`, reconstructing 115 shipping images from 106 source sheets and 55 launcher resources. Inventory armor icons continue using their previous sources. Every game animation retains its frame indices, timing and callbacks; no new facing or runtime rig system is introduced.

# Hero rigs, batch 1 - v1.10.0

`hero_rigs.py` supplies the Necromancer's reviewed hunched posture, broad mantle, ankle-length robe and Phylactery gestures, preserving the elderly face in the existing portrait. `sources/actors/necromancer-v2.png` and `hero-rigs-prompts.json` record the original built-in imagegen edit and its references. The source has unequal row heights; `actors.parts` records reviewed transparent gutters. The other eight heroes keep their earlier rig until their individual batches.

All hero outputs now contain 96x120-pixel frames in 2048x1024 sheets, below the 4096 atlas ceiling. Composition uses original source cutouts at 192x240 and downsamples once; this is not enlargement of the old low-resolution PNG. Runtime world height, pose indices, eight armor rows and frame timing are unchanged. Creature density is independent. The full pack reconstructs 115 images from 104 sources and 55 launcher resources.

`python tools/painted/actors.py --review necromancer` reproduces the before/after, pose sheet and animation under `verification/heroes/necromancer`; the GIF uses the existing action timing and a shared palette. New source art and derivatives are GPL-3.0-or-later. No AI or Blender is required for packing or CI.

# Awards and readable effects - v1.8.0

`readability.py` composes 98 distinct 64px award medals from 42 authored reliefs and five material rims. A second output contains the painted padlock, soft mist and droplet used by the live renderer. The two source sheets are in `sources/readability`; exact built-in imagegen prompts are in `readability-prompts.json`. The offline packer asserts complete named badge coverage and uniqueness. No generator runs during builds or CI. These original source images and derivatives are GPL-3.0-or-later.

The full pipeline now reconstructs 115 game images from 102 source sheets, plus 55 launcher resources. Existing world art is preserved; readability comes from rendering changes and the two additional atlases.

# Botany and skill illustrations - v1.7.0

`botany_skills.py` packs four built-in imagegen source sheets from `sources/botany-skills`: 13 sprouted plants into the existing terrain-feature cells, and 113 unique talent/subclass/armor/spell/Etch icons into `interfaces/painted_skills.png`. `botany-skills.json` records exact prompts and cell identities. All crop, alpha, scale and uniqueness checks run offline; no model or Blender is used in CI. These original images and their packed derivatives are GPL-3.0-or-later.

The 100 source sheets reconstruct 113 game images and 55 launcher resources. Existing terrain outside the plant row and existing trap artwork are unchanged. Skills retain 16 logical units with 64px frames; upstream skill textures retain their original addressing.

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

## Enchanter craft and elemental treasury assets

Original imagegen sources in `sources/sprint/` and exact prompts in `sprint-prompts.json` supply Spellguard, Blank Parchment, sixteen journal/category icons and six elemental-mechanism states. `botany_skills.py`, `inventory.py`, `journal.py` and `elemental_cache.py` pack the committed paintings; `pack.py --check` verifies every shipping pixel without network or generation. Logical geometry remains unchanged. This release reconstructs 144 assets from 134 source sheets plus 55 launcher resources.

## Distinct identities and held equipment

v1.21.4 compiles sixty distinct identity paintings from `sources/sprint-icons/`, with exact prompts and semantic assignments in `sprint-icons-prompts.json`. Mind Vision no longer aliases Magical Sight. `identification.py` exports the atlas, source/hash contracts and small-size review; the guide serpent and Unbound Focus use the special sheet. `pack.py --icons` is a focused export; full `--check` remains the CI gate.

v1.21.5 prototypes held equipment on Warrior and Enchanter. `hero_equipment.py` exports deterministic per-pose hand anchors to `sprites/hero-grips.json`. The renderer reuses the existing item atlas and current hero armor pixels for the grip; it introduces no additional GPU textures or actor/gameplay changes. Equipped, ability-selected and thrown weapons use their own painted art, with category scale/swing, mirrored anchors and matching hero visibility/tint. No baked weapon remains in the Enchanter source. Production frame layout, combat callbacks and mobile cadence remain unchanged.
