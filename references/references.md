# Grimhollow reference board (`references/`)

**Purpose.** This folder is the target for the reference-guided render iteration in spec §15.8. Each image is a *source of qualities* (surface, value range, light behaviour, silhouette), never a thing to copy. No image here is a game asset, a game screenshot, or a copyrighted character. The original board uses real places, materials, museum objects and manuscript lettering. The user-supplied additions below also include licensed AI illustrations and local artist/stock previews, explicitly identified by medium and verification status.

**Licensing.** The v0.6 continuation requires CC0 or public domain only, including photographs and material-map companions. StockCake, Unsplash, Pexels, royalty-free and attribution-only licenses are not accepted unless the specific file has a verified CC0/public-domain grant. Actual source URLs, licenses and retrieval dates are in `SOURCES.md`. StockCake now has an explicitly verified CC0 grant for its 15 supplied AI images. Eighteen other supplied files are catalogued locally with unresolved or incompatible licenses; they are not cleared for redistribution or optimization. Supplying an image is not recorded as a copyright/license grant. Original search titles below are retained as target descriptions, not source attributions.

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

## H. User-supplied additions (ingested 2026-09-08)

These 33 files remain under their original filenames in `new additions/`. U01–U33 are stable intake IDs, not new numeric scoring targets. Each maps to existing rows; the original targets and weights are unchanged. The 15 verified StockCake CC0 images are additional usable visual references. The other 18 are visible locally for review with license/source limits and are excluded from Git. No unresolved image is silently substituted for a cleared reference. U08 and U09 are crops of one source photograph and count as one observation.

All files decode; no image pixels or watermarks were edited. StockCake images are AI illustrations, not scans or factual photographs. U17 additionally needs its original source checked against section G before use. The strict test 38 license requirement is not met by the entire local intake while these 18 entries remain unresolved/incompatible; the earlier 45-row checkpoint pass does not certify them.

| ID | File | Existing rows | Intended visual use | Medium | License / ingestion status |
|---|---|---|---|---|---|
| U01 | [`0058-rounded-cobblestone-with-moss-texture-seamless.jpg`](new%20additions/0058-rounded-cobblestone-with-moss-texture-seamless.jpg) | 21, 20 | Large irregular cobbles, moss in seams and joint widths; visible provider watermark. | Material preview | Local only; SketchUp Texture Club preview; CC0/public-domain grant unverified. |
| U02 | [`1000_F_263342851_SQTmoHqXoZurBtKgzImI0zOfY301pJj7.jpg`](new%20additions/1000_F_263342851_SQTmoHqXoZurBtKgzImI0zOfY301pJj7.jpg) | 04, 06 | Arched brick courses, dark mortar, wet waterline and shallow channel; visible Adobe Stock watermark. | Stock photograph | Local only; Adobe Stock preview; CC0/public-domain grant unverified. |
| U03 | [`1000_F_564286652_fJoxacIKvQHuzw75Sz9VvDHoZj4TL5m4.jpg`](new%20additions/1000_F_564286652_fJoxacIKvQHuzw75Sz9VvDHoZj4TL5m4.jpg) | 18, 16 | Tall nave, ribbed arches, small cold light source and deep negative space; visible Adobe Stock watermark. | Stock illustration | Local only; Adobe Stock preview; CC0/public-domain grant unverified. |
| U04 | [`362010dd1c35130e19274faf4591ea90.jpg`](new%20additions/362010dd1c35130e19274faf4591ea90.jpg) | 33, 31 | Ornamented capitals, stroke weight and angular letter construction; original publication and reproduction rights unverified. | Lettering reproduction | Local only; Pinterest-hosted reproduction; original rights/source unverified. |
| U05 | [`485462c83466ff7ac9796580fae8b715.jpg`](new%20additions/485462c83466ff7ac9796580fae8b715.jpg) | 06 | Brick arch section, channel edge and masonry joints; monochrome, so not a water-color reference. | Historical photograph | Local only; Pinterest-hosted photograph; archival source and public-domain status unverified. |
| U06 | [`657543f5ca8cfb6e66ecf3332d6a3fce4de3360e_2_768x1024.jpg`](new%20additions/657543f5ca8cfb6e66ecf3332d6a3fce4de3360e_2_768x1024.jpg) | 29, 28 | Readable hood opening and broad cloak-fold bands; shape and shading study only, not a mesh source. | Artist render | Local only; Artist portfolio render; no CC0/public-domain grant verified. |
| U07 | [`75ce06_9ea94975106143dbbeee8a90cb084568~mv2.webp`](new%20additions/75ce06_9ea94975106143dbbeee8a90cb084568~mv2.webp) | 03, 01, 02 | Warm light on coarse stone arches and gradual corridor falloff; photographic/generated origin unverified. | Architectural image | Local only; Wix-hosted image; original author and license unverified. |
| U08 | [`765045eb2fb7edd27ce597063b07816d-10307-paris-catacombs-small-group-guided-tour-with-special-access-01 (1).avif`](new%20additions/765045eb2fb7edd27ce597063b07816d-10307-paris-catacombs-small-group-guided-tour-with-special-access-01%20%281%29.avif) | 02, 01 | Portrait crop of U09, useful for arch framing; same source photograph, not independent evidence or additional scoring weight. | Tour photograph | Local only; Headout tour photograph; CC0/public-domain grant unverified. |
| U09 | [`765045eb2fb7edd27ce597063b07816d-10307-paris-catacombs-small-group-guided-tour-with-special-access-01.avif`](new%20additions/765045eb2fb7edd27ce597063b07816d-10307-paris-catacombs-small-group-guided-tour-with-special-access-01.avif) | 01, 02 | Primary view of the U08/U09 pair: irregular limestone, repeated arches and warm corridor light. | Tour photograph | Local only; Headout tour photograph; CC0/public-domain grant unverified. |
| U10 | [`albedo_preview_512.png`](new%20additions/albedo_preview_512.png) | 20, 21 | Moss distribution and stone-to-mortar size; 512 px watermarked albedo preview with no companion maps. | Material preview | Local only; AITextured FCL v1.1; standalone raw-file redistribution is not permitted. |
| U11 | [`ancient-chains-decay-stockcake.jpg`](new%20additions/ancient-chains-decay-stockcake.jpg) | 10 | Large near-black links, rust flecks and warm highlights against blue-grey fractured stone. | AI illustration | CC0-1.0; committed supplement |
| U12 | [`ancient-mine-interior-stockcake.jpg`](new%20additions/ancient-mine-interior-stockcake.jpg) | 13 | Low timber ceiling, rough rock and near-lantern illumination; tracks are composition context only. | AI illustration | CC0-1.0; committed supplement |
| U13 | [`ancient-stone-chain-stockcake.jpg`](new%20additions/ancient-stone-chain-stockcake.jpg) | 12 | Chain silhouette, stone/iron value separation, contact shadows and restrained warm accent. | AI illustration | CC0-1.0; committed supplement |
| U14 | [`ancient-torch-burns-stockcake.jpg`](new%20additions/ancient-torch-burns-stockcake.jpg) | 24, 22 | Holder bowl, flame tongue and ember scatter; colored smoke is a mood cue, not physical evidence. | AI illustration | CC0-1.0; committed supplement |
| U15 | [`Arms-and-Armor-Galleries-front-gallery-Worcester-Art-Museum-3-scaled.jpg`](new%20additions/Arms-and-Armor-Galleries-front-gallery-Worcester-Art-Museum-3-scaled.jpg) | 26 | Variation in helm and shoulder silhouettes across actual armor displays; gallery displays are not UI designs. | Museum photograph | Local only; Worcester Business Journal-hosted photo; CC0/public-domain grant unverified. |
| U16 | [`be67ad86-cc68-4ca6-b4e4-6674b06dcd9b.jpg`](new%20additions/be67ad86-cc68-4ca6-b4e4-6674b06dcd9b.jpg) | 28 | Deep face aperture, hood outline, stone weathering and broad robe folds; do not reproduce the sculpture. | Auction photograph | Local only; David Duggleby auction photograph; CC0/public-domain grant unverified. |
| U17 | [`d834974b19084c288fa5a0cd4088eb92.jpg`](new%20additions/d834974b19084c288fa5a0cd4088eb92.jpg) | 05, 04 | Curved brick vault, raised walkway and warm floor reflection; original creator and possible game origin remain unresolved, so not an optimization target. | Unverified rendered/photographic source | Local only; Pinterest-hosted image; license and section G source check unresolved. |
| U18 | [`dancing-fire-torch-stockcake.jpg`](new%20additions/dancing-fire-torch-stockcake.jpg) | 22 | Thin licking flame, isolated hot core, wispy smoke and scattered sparks. | AI illustration | CC0-1.0; committed supplement |
| U19 | [`dancing-flame-light-stockcake.jpg`](new%20additions/dancing-flame-light-stockcake.jpg) | 23 | Alternate flame silhouette and tongue position above a narrow dark holder. | AI illustration | CC0-1.0; committed supplement |
| U20 | [`dd54f22c884315ae3e9643d205444931--medieval-manuscript-initials.jpg`](new%20additions/dd54f22c884315ae3e9643d205444931--medieval-manuscript-initials.jpg) | 32 | Decorated capital in a framed manuscript field; original folio and reproduction provenance unverified. | Lettering reproduction | Local only; Pinterest-hosted reproduction; original rights/source unverified. |
| U21 | [`e5fdc5a15053ef3e59a455ab8a8920ec.jpg`](new%20additions/e5fdc5a15053ef3e59a455ab8a8920ec.jpg) | 19 | Pooled specular in joints, irregular slab relief, wet/dry contrast and mossy seams; original source/license unverified. | Material/render preview | Local only; Pinterest-hosted image; original rights/source unverified. |
| U22 | [`gothic-cathedral-depths-stockcake.jpg`](new%20additions/gothic-cathedral-depths-stockcake.jpg) | 18, 17 | Very dark vault mass, column rhythm and small readable highlights; illustrative mood rather than architectural evidence. | AI illustration | CC0-1.0; committed supplement |
| U23 | [`gothic-crypt-ambiance-stockcake.jpg`](new%20additions/gothic-crypt-ambiance-stockcake.jpg) | 08, 09 | Fine web strands, dusty top light, stone slabs and sparse candle accents. | AI illustration | CC0-1.0; committed supplement |
| U24 | [`gothic-ruin-interior-stockcake.jpg`](new%20additions/gothic-ruin-interior-stockcake.jpg) | 16, 17 | Tall ruined columns, floor rubble, cool ambient and overhead openings. | AI illustration | CC0-1.0; committed supplement |
| U25 | [`illuminated-medieval-manuscript-stockcake.jpg`](new%20additions/illuminated-medieval-manuscript-stockcake.jpg) | 31, 32 | Single ornate capital, strong border and contrasting stroke weight; lettering/text are synthetic, not historical exemplars. | AI illustration | CC0-1.0; committed supplement |
| U26 | [`medieval-armor-on-display-at-metropolitan-museum-of-art-in-new-york-C1A5DW.jpg`](new%20additions/medieval-armor-on-display-at-metropolitan-museum-of-art-in-new-york-C1A5DW.jpg) | 25 | Three plate-armor silhouettes and large highlight planes; visible Alamy watermarks retained. | Stock museum photograph | Local only; Alamy preview C1A5DW; a museum subject does not establish a public-domain photograph. |
| U27 | [`medieval-treasury-chamber-stockcake.jpg`](new%20additions/medieval-treasury-chamber-stockcake.jpg) | 09 | Dark stone room with small warm accents and a cool distant opening; table props are illustrative. | AI illustration | CC0-1.0; committed supplement |
| U28 | [`pexels-photo-29927910.avif`](new%20additions/pexels-photo-29927910.avif) | 27, 25 | Plate plane changes, recessed joints and restrained metal highlights; decorative pattern is not to be copied. | Museum photograph | Local only; Pexels-hosted photo 29927910; CC0/public-domain grant unverified. |
| U29 | [`sacred-underground-vault-stockcake.jpg`](new%20additions/sacred-underground-vault-stockcake.jpg) | 07 | Dressed stone, large slabs and small warm candle pools against cooler ambient. | AI illustration | CC0-1.0; committed supplement |
| U30 | [`seated-hooded-statue-with-draped-robes-on-stone-pedestal-weathered-st.webp`](new%20additions/seated-hooded-statue-with-draped-robes-on-stone-pedestal-weathered-st.webp) | 30 | Cloth over a seated base, hood cue and broad folds; no mesh or exact sculpture reproduction. | Generated/model preview | Local only; Tripo-hosted preview; project license/redistribution rights unverified. |
| U31 | [`shadowed-mining-history-stockcake.jpg`](new%20additions/shadowed-mining-history-stockcake.jpg) | 14 | Warm near timber, hard beam shadows and cool recession into the tunnel. | AI illustration | CC0-1.0; committed supplement |
| U32 | [`underground-mining-mystery-stockcake.jpg`](new%20additions/underground-mining-mystery-stockcake.jpg) | 15 | Deep dark tunnel, rubble ledges and narrow overhead light shaft; depth falloff study. | AI illustration | CC0-1.0; committed supplement |
| U33 | [`weathered-dungeon-chains-stockcake.jpg`](new%20additions/weathered-dungeon-chains-stockcake.jpg) | 11 | Heavy suspended links, link shadows and a dark/warm metal silhouette. | AI illustration | CC0-1.0; committed supplement |
