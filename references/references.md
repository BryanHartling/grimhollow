# Grimhollow reference board (`references/`)

**Purpose.** This folder is the target for the reference-guided render iteration in spec §15.8. Each image is a *source of qualities* (surface, value range, light behaviour, silhouette), never a thing to copy. No image here is a game asset, a game screenshot, or a copyrighted character. Real places, real materials, museum objects, and manuscript lettering only.

**Licensing.** The v0.6 continuation requires CC0 or public domain only, including photographs and material-map companions. StockCake, Unsplash, Pexels, royalty-free and attribution-only licenses are not accepted unless the specific file has a verified CC0/public-domain grant. Actual source URLs, licenses and retrieval dates are in `SOURCES.md`. Original search titles below are retained as target descriptions, not source attributions.

**How the loop uses this file.** For each asset class, the images marked with that class are the comparison set. The loop scores a render against them on the *measurable targets* listed, applies the *qualities* as a self-critique checklist, and never treats the *don't copy* line as a target. Weights are relative importance within a class.

**Naming convention.** Save each image as `ref-<NN>-<slug>.<ext>` using the number below.

---

## A. Regions — environments

### Sewers (floors 1–5)
Mood: damp, close, recently abandoned. Brick and rough stone, standing water, moss at every wet edge. Single warm light source, corners black.

| # | File | Source query / title | Intended use | Material | Qualities to extract | Measurable targets | Weight |
|---|---|---|---|---|---|---|---|
| 01 | `ref-01-catacomb-corridor.jpg` | "Paris catacombs stone corridor torchlight" — first result | Sewers wall + corridor mood; light falloff | Rough-cut limestone, mortar | Coarse irregular blocks; one warm source; falloff to black within ~4 tiles; dust in the light | Mean luminance 0.12–0.20; ≥ 60% of pixels below 0.15; hue centred amber 30–40° | 0.9 |
| 02 | `ref-02-catacomb-arch.jpg` | same query — second result | Sewers doorway and arch decor | Limestone, iron gate | Arch silhouette; gate as a dark negative shape; floor damp sheen near the light | Same as 01 | 0.5 |
| 03 | `ref-03-catacomb-wide.jpg` | "Uncovering the Enigmatic Catacombs of Paris" | Sewers room composition; how much wall vs floor is visible | Limestone | Ratio of lit floor to dark wall; texture readable only within light radius | Mean luminance 0.10–0.18 | 0.4 |
| 04 | `ref-04-sewer-tunnel-brick.jpg` | "Victorian brick sewer tunnel wet stone" — first result | **Sewers floor and wall primary reference** | Wet brick, mortar, sewage | Brick courses with dark mortar; wet horizontal line where water sits; green-brown standing water; reflection of light on the wet floor | Brick count 2–3 per 64 px tile edge; mortar darker than brick by ≥ 0.20 luminance; water hue 80–110° at saturation 0.25–0.40 | 1.0 |
| 05 | `ref-05-sewer-tunnel-vault.jpg` | same query — "Ramsalon, Underground" | Sewers wall height and vault feel; torch-on-brick highlight | Brick | Warm highlight on the near bricks, cool grey-blue in the distance; two-tone lighting | Warm/cool split: near pixels hue 25–40°, far pixels hue 200–230° | 0.6 |
| 06 | `ref-06-brick-culvert.jpg` | same query — "Brick culvert" | Sewers channel and bridge; where floor meets water | Brick, water | Hard edge between dry brick and water; algae line; water surface reads dark with small highlights | Water region luminance ≤ 0.12 with highlight speckle ≤ 3% of pixels | 0.7 |

### Prison (floors 6–10)
Mood: dry, cold, institutional. Dressed stone, iron, chains, bone piles. Candle or lantern light, yellow on grey.

| # | File | Source query / title | Intended use | Material | Qualities to extract | Measurable targets | Weight |
|---|---|---|---|---|---|---|---|
| 07 | `ref-07-crypt-vault.jpg` | "medieval crypt vaulted ceiling bones candlelight" — first result | **Prison wall and floor primary reference** | Dressed stone, bone | Regular slab courses; bone as pale accent against grey; candle glow small and yellow | Stone hue 30–50° at saturation ≤ 0.15; bone luminance 0.60–0.80; light source ≤ 5% of frame | 1.0 |
| 08 | `ref-08-crypt-cobweb.jpg` | same query — "Gothic Crypt Ambiance" | Prison decor: cobweb, dust, neglect | Stone, cobweb | Cobweb as faint diagonal lines; dust motes; extreme dark background | ≥ 70% pixels below 0.10 | 0.5 |
| 09 | `ref-09-crypt-treasury.jpg` | same query — "Medieval Treasury Chamber" | Prison special room (vault/treasury); chest and iron props | Stone, iron, gold accent | A single warm accent colour in an otherwise grey room | Accent (gold) ≤ 8% of pixels | 0.4 |
| 10 | `ref-10-chains-decay.jpg` | "rusted iron chains dungeon wall texture" — first result | **Prison chain and manacle props; iron material** | Rusted iron, stone | Rust as orange-brown flecks on near-black iron; chain links readable as a silhouette | Iron luminance 0.08–0.20; rust hue 15–30° | 0.9 |
| 11 | `ref-11-chains-weathered.jpg` | same query — second result | Prison wall-mounted chains; how chains hang | Iron | Catenary hang; shadow under each link | — | 0.5 |
| 12 | `ref-12-chains-stone.jpg` | same query — third result | Iron-against-stone contrast | Iron, stone | Value separation between iron and the wall behind it | Iron/stone luminance gap ≥ 0.15 | 0.5 |

### Caves (floors 11–15)
Mood: raw, warm, unstable. Earth and rock, timber supports, lantern light.

| # | File | Source query / title | Intended use | Material | Qualities to extract | Measurable targets | Weight |
|---|---|---|---|---|---|---|---|
| 13 | `ref-13-mine-tunnel.jpg` | "abandoned mine tunnel timber supports lantern" — first result | **Caves wall and support-beam primary reference** | Raw rock, timber | Timber as warm vertical accents against dark rock; low irregular ceiling; earth floor with rubble | Timber hue 20–35° at saturation 0.30–0.50; rock saturation ≤ 0.12 | 1.0 |
| 14 | `ref-14-mine-timber-shadow.png` | same query — "Timber And Shadow" | Caves lighting: a single lantern casting hard shadows from beams | Timber, rock | Hard-edged shadow; hot spot on the nearest beam | Highlight luminance ≥ 0.70 covering ≤ 4% of frame | 0.7 |
| 15 | `ref-15-mine-shaft.jpg` | same query — "Abandoned Mine Shaft" | Caves depth and chasm edges | Rock, timber | Fall-off into black below; rubble on ledges | Bottom third mean luminance ≤ 0.06 | 0.6 |

### City (floors 16–20)
Mood: grand and ruined. Columns, arches, patterned floors, broken statuary. Cooler, larger light pools.

| # | File | Source query / title | Intended use | Material | Qualities to extract | Measurable targets | Weight |
|---|---|---|---|---|---|---|---|
| 16 | `ref-16-cathedral-ruin.jpg` | "ruined gothic cathedral interior dark stone pillars" — first result | **City wall, column, and floor primary reference** | Dressed stone, rubble | Vertical rhythm of columns; floor tiles worn but patterned; light from above | Column spacing regular; floor pattern visible at ≥ 2 repeats per tile edge | 1.0 |
| 17 | `ref-17-cathedral-ruin-arches.jpg` | same query — "Gothic Cathedral Interior Ruins" | City arches and openings; sky-light ambient | Stone | Cooler ambient (blue-grey) with warm ground light | Ambient hue 200–230°; ground hue 30–45° | 0.7 |
| 18 | `ref-18-cathedral-dark.jpg` | same query — "Gothic Cathedral Darkness" | **Halls primary reference** (see below) and City late-floor mood | Dark stone | How much of a frame can be black and still read | ≥ 80% pixels below 0.08 | 0.8 |

### Halls (floors 21–25)
Mood: oppressive and ceremonial. Obsidian-dark stone, ritual geometry, bone furniture. Least light of any region.

| # | File | Source query / title | Intended use | Material | Qualities | Targets | Weight |
|---|---|---|---|---|---|---|---|
| 18 | (shared) `ref-18-cathedral-dark.jpg` | — | Halls darkness budget | Dark stone | See above | Mean luminance 0.06–0.12 | 1.0 |
| 07 | (shared) `ref-07-crypt-vault.jpg` | — | Halls bone decor (thrones, piles) | Bone | Bone as the only pale element | Bone ≤ 10% of pixels | 0.6 |

---

## B. Materials — tile surfaces (region-independent)

These drive the shared material library in §15.2. Prefer CC0 material scans (albedo + normal + roughness) from Poly Haven / ambientCG for the committed versions: search those sites for "cobblestone wet", "mossy stone", "brick wall damp", "rusty metal", "bone", "mud".

| # | File | Source query / title | Intended use | Material | Qualities to extract | Measurable targets | Weight |
|---|---|---|---|---|---|---|---|
| 19 | `ref-19-wet-stone-floor.jpg` | "wet cobblestone flagstone floor moss texture" — "Wet Stone Floor, Substance Designer" | **Wet stone specular reference for all regions' floors** | Wet stone | Flagstone size and irregularity; specular pooling in low spots; dark mortar | Flagstone 2–3 per 64 px; specular highlight ≤ 5% of pixels at ≥ 0.75 luminance; luminance std-dev ≥ 0.10 | 1.0 |
| 20 | `ref-20-moss-texture.jpg` | same query — "Moss Textures" | Moss edge treatment where water meets floor; grass tiles' base colour | Moss | Yellow-green with dark shadowed depth; not flat | Hue 75–100°; saturation 0.30–0.50; luminance std-dev ≥ 0.08 | 0.8 |
| 21 | `ref-21-mossy-cobble-seamless.jpg` | same query — "Rounded cobblestone with moss texture seamless" | Seamless-tile construction reference (this is what a finished tile should be) | Stone, moss | Rounded cobbles with moss in the gaps; tiles with no visible seam | Seam test: 2×2 tiling shows no repeated feature at the boundary | 0.7 |

---

## C. Lighting and effects

| # | File | Source query / title | Intended use | Qualities to extract | Measurable targets | Weight |
|---|---|---|---|---|---|---|
| 22 | `ref-22-torch-flame.jpg` | "torch fire embers smoke dark background photo" — first result | **Wall torch and fire-tile flame strip** (§15.6) | Flame shape: narrow base, licking tongues; colour from white core to amber to deep red edge; embers as scattered bright dots | Core ≥ 0.90 luminance ≤ 2% of flame area; edge hue 10–20° | 1.0 |
| 23 | `ref-23-torch-flame-2.jpg` | same query — second result | Flame animation variation frames | Same shape family, different tongue positions | — | 0.5 |
| 24 | `ref-24-torch-ancient.jpg` | same query — third result | Torch *prop* (the holder) and how it lights the wall behind it | Iron sconce as dark silhouette; wall lit in a cone above the flame | Lit cone luminance falls to ambient within 2 tiles | 0.7 |
| 05 | (shared) `ref-05-sewer-tunnel-vault.jpg` | — | Lighting overlay warm/cool split (§4.3) | Near warm, far cool | See row 05 | 0.8 |
| 34 | `ref-34-smoke-wisps.jpg` | "smoke wisps black background" | Gas blobs (§15.6) | Soft wisps, density variation, no hard outline | Opacity 0.15–0.85; soft edges | 1.0 |
| 35 | `ref-35-reeds-tall-grass.jpg` | "reeds tall grass wind" | Grass sway loop | Bent blades, clumps, varied stem heights | — | 1.0 |
| 36 | `ref-36-puddle-ripple.jpg` | "puddle ripple ring water dark" | Water ripple loop | Thin concentric rings, dark water, small reflected highlights | — | 1.0 |
| 37 | `ref-37-scorched-stone.jpg` | "scorched stone burn mark" | Scorch decals | Charred patches, broken soft edges, stone/ash value contrast | — | 1.0 |
| 38 | `ref-38-dried-blood-value.jpg` | "dried blood on stone" | Existing blood decals: value matching | Dark red residue, irregular borders, contrast with stone | — | 1.0 |


---

## D. Characters — silhouette and material references

Characters are **not** in the render-iteration loop (§15.8 scope). These references are for whoever produces character art (procedural pipeline redesign, image generation with a style lock, or an artist) and for the readability critique.

| # | File | Source query / title | Intended use | Qualities to extract | Notes |
|---|---|---|---|---|---|
| 25 | `ref-25-armor-museum.jpg` | "museum medieval skeleton armor display" — first result | Warrior/Duelist/Cleric armor tiers; metal material | Plate as a few large planes catching light; dark recesses; one highlight per plane | Public-domain equivalent: Wikimedia Commons "Metropolitan Museum armor" category |
| 26 | `ref-26-armor-worcester.jpg` | same query — Worcester Art Museum | Armor silhouette variety across tiers | Shoulder width and helm shape as the tier's silhouette change | — |
| 27 | `ref-27-armor-display.jpg` | same query — third result | Weapon props (items sheet) | Blade, haft, guard proportions at small size | — |
| 28 | `ref-28-hooded-monk-statue.jpg` | "hooded monk robe statue stone weathered" — first result | **Necromancer, Enchanter, Mage robe silhouette** | Hood as the single strongest silhouette cue; drape folds as 3–4 tone bands; face in shadow | The POC characters lacked exactly this: a hood reads at 16 px, a sphere does not |
| 29 | `ref-29-monk-statue-render.jpg` | same query — "Monk Statue, Blender Artists" | How a robed figure looks under a toon/step shader | Fold bands quantise cleanly; hood shadow is a hard shape | Rendered example; useful as a shading target, not a mesh source |
| 30 | `ref-30-seated-hooded-statue.jpg` | same query — third result | Idle/seated pose reference; cloth over a base form | Base form (block) with cloth draped over; that is the modelling approach that works from primitives | Supports a primitives-plus-drape rig if characters are ever revisited in Blender |
| 39 | `ref-39-human-skeleton.jpg` | "human skeleton anatomical museum" | Skeleton/Ghoul/Revenant; Halls bone decor | Rib cage, limb/joint proportions, pale bone contrast | Museum/anatomical reference only |
| 40 | `ref-40-rat-dark-fur.jpg` | "rat close up dark fur" | Sewers rat silhouette/material | Compact body, pointed muzzle, ears, short fur highlights | Real animal photograph |
| 41 | `ref-41-crab-top-view.jpg` | "crab top view" | Sewers crab silhouette/material | Broad shell, separated legs, paired claws | Dorsal animal photograph |
| 42 | `ref-42-carrion-crow.jpg` | "carrion crow" | Wraith silhouette inspiration | Beak, folded wing, tapered tail, black silhouette | Corvid shape inspiration only |


---

## E. Title, UI lettering, and iconography

| # | File | Source query / title | Intended use | Qualities to extract | Measurable targets |
|---|---|---|---|---|---|
| 31 | `ref-31-illuminated-initial.jpg` | "medieval blackletter manuscript illuminated initial gothic lettering" — first result | **Grimhollow wordmark** (§5.4): letterform weight and the single decorated initial | Heavy vertical strokes; thin hairlines; one ornamented capital; the rest plain | Wordmark stroke contrast ≥ 4:1; only the G decorated |
| 32 | `ref-32-ornate-letter.jpg` | same query — "Ornate Medieval Letter" | Talent-tier headers and subclass badges | An initial inside a bordered square | Badge fits 32×32 with the letter filling ≥ 60% |
| 33 | `ref-33-letter-template.png` | same query — "Medieval Letter Template" | Blackletter alphabet reference for any in-game titling | Consistent x-height; angular bowls | — |
| 43 | `ref-43-ritual-dagger.jpg` | "bone dagger" / "ritual dagger antique" | Necromancer item icons | Slender blade, grip and guard proportions, carved material | — |
| 44 | `ref-44-antique-glass-vial.jpg` | "alchemist glass vials antique" | Potion silhouettes | Neck-to-body ratio, flared lip, glass highlights | — |
| 45 | `ref-45-red-wax-seal.jpg` | "wax seal red" | Sigil Brush and Runecraft picker | Raised wax rim, inset mark, red material shading | — |


---

## F. Global measurable targets (apply to every render before per-class scoring)

- Palette: every pixel within ΔE 12 of a §5.2 palette entry or its tint/shade (validator rule 1).
- Region ambient means (after lighting): Sewers 0.14, Prison 0.12, Caves 0.15, City 0.16, Halls 0.09 (±0.03).
- Per-tile luminance standard deviation ≥ 0.08 (validator rule 7; the "washed out" fix).
- Hue budget: each region has one dominant hue family (Sewers green-brown, Prison grey-yellow, Caves amber, City blue-grey, Halls near-neutral) covering ≥ 70% of pixels, and at most one accent.
- Readability (§15.3): any two mobs in the same region differ by ≥ 0.25 in 16-px hue histogram L1 distance.

## G. Don't-copy list (applies to the whole board)

- No Diablo, Path of Exile, Darkest Dungeon, or any other game's screenshots, concept art, or sprites, in this folder or as optimisation targets. Style vocabulary in words is fine; images are not.
- No reproduction of any specific artwork, statue, or photograph as an asset. The loop optimises toward *statistics and qualities*, never toward pixel similarity with a single image.
- Public-domain manuscript lettering may inspire the wordmark; the wordmark itself must be original.
