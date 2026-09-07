# Game Design Document & Build Specification (v0.2 — continuation revision)
## Working title: **Grimhollow** (a Shattered Pixel Dungeon derivative)

**Document purpose.** This is a complete, self-contained specification intended to be handed to an autonomous coding agent to produce a playable build in a single run. It defines the deliverable, the technical base, every new class and content item with concrete numbers, the art specification and pipeline, and the acceptance tests the build must pass. Where the spec is silent, follow existing Shattered Pixel Dungeon (SPD) conventions exactly.

**Reading order for the agent:** §1 (deliverable) → §2 (base and constraints) → §3 (architecture map) → §4 (resolution and rendering) → §5 (art) → §6–8 (classes) → §9 (content) → §10 (acceptance tests) → §11 (delivery).

---

## 1. Deliverable

A Git repository forked from `00-Evan/shattered-pixel-dungeon` at the latest tagged release, containing:

1. **Code**: three new hero classes (Necromancer, Enchanter, Psychic) fully integrated into hero selection, talents, subclasses, and armor abilities; all §9 content; 64px rendering with a lighting overlay.
2. **Art**: a 64px asset set for **all five regions, all existing mobs, all existing items, all three new classes, and all new content**, produced through a repeatable pipeline (§5.6) and committed as PNG sprite sheets in the layout defined in §5.4. First-pass quality is acceptable if it meets the §5.3 style constraints and §10.3 acceptance checks. Every asset must be a drop-in file so a human artist can replace it later without code changes.
3. **Build**: `./gradlew desktop:run` launches the game; `./gradlew android:assembleDebug` produces an installable APK. Both must succeed from a clean clone.
4. **Docs**: `README.md` (build steps), `ART_PIPELINE.md` (how to regenerate assets), `CHANGES.md` (everything added relative to upstream, per file).

**Definition of done:** all §10 acceptance tests pass; a new game can be started with each of the three new classes and played to floor 5 (past the first boss) without crashes; every talent, subclass, and armor ability is selectable and functional.

---

## 2. Base, constraints, and license

- **Base:** Shattered Pixel Dungeon **v3.3.8** (commit `7b8b845a`), Java, libGDX, Gradle. Modules: `core`, `desktop`, `android`, `services`, `SPD-classes`. Fork; do not rewrite.
- **License:** GPL-3.0. All new code and art are GPL-3.0. Do not import third-party assets under incompatible licenses. Do not reproduce any Blizzard/Diablo asset, name, or text; only the *style* described in §5 is the reference.
- **Scope rules (hard):**
  - Keep: top-down grid, all existing mechanics and systems, talent grid, subclasses, armor abilities, alchemy, identification, hunger, challenges, Amulet of Yendor, and all **six** existing classes (Warrior, Mage, Rogue, Huntress, Duelist, Cleric). Nine classes total after additions.
  - Enum key collisions: upstream already uses `HeroSubClass.WARDEN` (Huntress). New enum keys must not collide with any existing `HeroClass`, `HeroSubClass`, `Talent`, or `ArmorAbility` identifier; check before adding.
  - Add: only what is specified here. Every new ability is implemented on existing base classes (`Buff`, `Wand`, `Spell`, `Weapon.Enchantment`, `Armor.Glyph`, `Mob`, `Talent`, `ArmorAbility`, `Item`, `KindOfWeapon`, `Trap`).
  - Do not add: affixes/rares/uniques, skill trees, mana, isometric rendering, new resource systems beyond the three class items defined below, new game modes.
- **Compatibility:** save files from upstream do not need to load. Upstream's `Badges`, `Rankings`, and `Statistics` must continue to work and include the new classes.
- **Localization:** English only. All strings go through `Messages` and the `messages/*.properties` files per SPD convention; no hardcoded UI text.

---

## 3. Architecture map (where things go)

All paths under `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`.

| Concern | Location | Pattern to copy |
|---|---|---|
| Hero classes | `actors/hero/HeroClass.java` (enum), `actors/hero/HeroSubClass.java` | Duelist and Cleric entries |
| Talents | `actors/hero/Talent.java` | Existing tiered enums; hook methods `onLevelUp`, `onHit`, `onKill`, etc. |
| Armor abilities | `actors/hero/abilities/<class>/` | `actors/hero/abilities/duelist/*` |
| Class items | `items/<ClassName>Item.java` | `items/weapon/melee/MagesStaff.java` (charges), `items/BrokenSeal.java` (attaches to armor) |
| Class spells | `items/spells/` or new `items/classspells/` | `items/spells/*` (targeting via `CellSelector`) |
| Buffs / curses | `actors/buffs/` | `Vulnerable`, `Weakness`, `Hex`, `Amok`, `Terror`, `Vertigo`, `Corrosion`, `Barkskin` |
| Allies / minions | `actors/mobs/` | `actors/mobs/npcs/DirectableAlly`, `Huntress` spirit, `WandOfLivingEarth.EarthGuardian` |
| Temporary walls | `levels/Level.java` set/reset of `Terrain`; new `levels/features/` | `WandOfBlastWave` knockback; `Chasm`; `Terrain.EMPTY`/`WALL` toggling; must be reverted on timeout and on level exit |
| Wands | `items/wands/` | `WandOfCorrosion`, `WandOfBlastWave` |
| Weapons | `items/weapon/melee/`, `items/weapon/missiles/` | `Glaive` (reach), `Flail` (existing; reuse), `ThrowingKnife` |
| Enchantments | `items/weapon/enchantments/`, `items/weapon/curses/` | `Blazing`, `Sacrificial`, `Annoying` |
| Mobs | `actors/mobs/` | `Necromancer` (existing summoner), `DM100`, `Warlock` |
| Sprites | `sprites/*Sprite.java` | `HeroSprite`, `CharSprite`, `MobSprite` |
| Sprite sheets | `core/src/main/assets/sprites/`, `environment/`, `interfaces/` | see §5.4 |
| Rendering | `tiles/DungeonTilemap.java`, `tiles/FogOfWar.java`, `scenes/GameScene.java` | §4 |
| Strings | `core/src/main/assets/messages/**/*.properties` | keys `actors.hero.heroclass.necromancer`, etc. |

---

## 4. Resolution and rendering

### 4.1 Geometry (validated approach)
**Logical world units stay at upstream's 16 per tile.** Camera, physics, pathing, UI, and all `Level` coordinates are untouched. Only *texture* resolution changes. `com.shatteredpixel.shatteredpixeldungeon.GameGeometry` holds the texture-side constants and the conversion between texture frames and logical placement:

```
LOGICAL_TILE = 16            (unchanged; do not modify)
TEX_TILE = 64
TEX_HERO_W = 48, TEX_HERO_H = 60      (rendered into a 12x15 logical footprint)
TEX_MOB default 48x60; large mobs 96x96 (bosses, DM-300, Yog)
TEX_ITEM = 32                          (rendered into an 8x8 logical footprint)
```
`Tilemap` (SPD-classes), `DungeonTilemap`, `DungeonTerrainTilemap`, `DungeonWallsTilemap`, and `FogOfWar` are already converted for terrain. Remaining: `CharSprite`, `HeroSprite`, `MobSprite`, `ItemSprite`, `MissileSprite`, and every `TextureFilm` constructor for character and item sheets must read `GameGeometry` and scale frames into the logical footprint. Visible tile count at default zoom is preserved automatically because logical units did not change.

### 4.2 Performance budget
- Android target: 60 fps on a 2020 mid-range device at default zoom.
- Texture atlases must not exceed 4096×4096. Split per-region tile sets and per-mob sprite sheets accordingly.
- Use `TextureFilm` frames as upstream does; do not switch to per-frame textures.

### 4.3 Lighting overlay (new)
Add `tiles/LightingOverlay.java`, drawn after terrain and before characters, additively blended:
- **Ambient:** each region has an ambient multiplier applied to visible terrain (see §5.2 table). Fog of war is unchanged.
- **Light sources:** a light source is any of: hero (radius = current view distance, warm amber), wall torches in the tile set (radius 3, amber), fire/burning tiles (radius 2, orange), electric effects (radius 2, cyan, 1-frame), necrotic/corrosion/curse effects (radius 1–2, sickly green), frost (radius 1, pale cyan).
- **Falloff:** quadratic; light color multiplied onto terrain color, clamped. Light must fall off radially from each source; the hero's own light is brightest at the hero and reaches ambient level at its radius edge. **Do not clamp or boost light at the visible-region boundary**: fog of war handles unexplored cells, and a room's edge must not render brighter than its interior.
- **Implementation:** one full-screen quad per light is unacceptable on mobile. Render lights into a low-resolution light map (1/4 tile resolution), then multiply once. Cache when nothing changed.
- **Setting:** add a toggle in Settings → Graphics: "Dynamic lighting" (default on). When off, render as upstream.

### 4.4 Effects
- Low-HP vignette: at ≤30% HP, a screen-edge crimson vignette scaling in opacity to 0.6 at 10% HP.
- Blood: upstream's `Splash` particles scaled to resolution; add a persistent blood decal (small, 3 variants, 20% chance on hit, fades on level exit).
- Curse/necrotic visuals: shared green emissive particle (`NecroticParticle`) used by all necromancer effects and curse buffs.

---

## 5. Art specification

### 5.1 Style reference (prose only; no external assets)
Dark gothic dungeon crawler. Painted-texture surfaces rather than flat color. Heavy shadows, low-key lighting, mid-contrast. Materials: wet stone, rusted iron, rotting wood, bone, mud, dried blood, mildew. Characters are weighty and grounded, with a slightly hunched, mid-weight silhouette rather than chibi. Readability at 64px is the priority: strong silhouette, one dominant color per character, one accent.

### 5.2 Palette (hex; use only these plus tints/shades of them)

| Role | Hex |
|---|---|
| Base stone dark / mid / light | `#1A1816` / `#3B3733` / `#6B645C` |
| Mud / earth | `#2E241A` / `#4A3B2A` |
| Wood | `#3C2E1F` / `#5A4630` |
| Iron | `#2C2F33` / `#565B62` |
| Bone | `#C9BFA8` / `#EFE7D2` |
| Blood / crimson accent | `#5E0D12` / `#9E1B24` |
| Necrotic / poison green | `#3F6A1F` / `#7BB33B` |
| Frost / arcane cyan | `#2E6F7A` / `#6FD3E0` |
| Fire / torch amber | `#8A4B12` / `#E0982F` |
| Holy / bless gold | `#8A7331` / `#E4C76A` |
| Psychic violet | `#4A2C6E` / `#9D6BD1` |

Region ambient multipliers (RGB): Sewers `0.55,0.60,0.50`; Prison `0.50,0.50,0.58`; Caves `0.60,0.45,0.35`; City `0.62,0.55,0.50`; Halls `0.40,0.40,0.52`.

### 5.3 Style constraints (acceptance-checked)
1. Every color in every asset is within ΔE 12 (CIE76) of a palette entry or a tint/shade of one.
2. Each character sprite has a ≥2px dark outline (`#0E0D0C`) on the outer silhouette.
3. Average luminance of any tile asset ≤ 0.45 (0–1 scale).
4. Hero and mob sprites occupy 60–85% of their frame height.
5. No text, logos, or watermarks in any asset.
6. Item icons are readable at 32px: single object, centered, dark outline, ≤ 3 dominant colors.

### 5.4 Sheet layout and naming
- `assets/sprites/<name>.png` for characters, one sheet per mob/hero, frames laid out left-to-right in rows: Row 0 idle, Row 1 run, Row 2 attack, Row 3 die, Row 4 special (cast/zap). Frame counts: idle 2, run 6, attack 5, die 5, special 4. Facing right; left is a horizontal flip (upstream convention).
- Hero sheets: `hero_<class>.png` at 48×60, with armor tier rows appended (tiers 1–5 plus class armor) exactly as upstream's `HeroSprite` expects.
- Tiles: `assets/environment/tiles_<region>.png` at 64×64, indices matching upstream's `DungeonTileSheet` constants (extend the sheet; do not renumber).
- Walls: `assets/environment/walls_<region>.png` following upstream's wall stitching layout.
- Items: `assets/sprites/items.png` at 32×32, indices matching upstream `ItemSpriteSheet` (extend; do not renumber).
- Effects: `assets/effects/*.png` at 64×64 frames.
- UI: `assets/interfaces/*` may remain upstream (UI scale is independent) except: talent icons (`talent_icons.png`), which need entries for every new talent; and the **title screen**. Replace `banners.png` (the "Shattered Pixel Dungeon" wordmark and any Pixel Dungeon branding) with a pipeline-generated Grimhollow wordmark in the §5.2 palette (bone-white lettering, crimson accent, dark outline), and replace the title background with a pipeline-generated dark stone/bone composition. No upstream title text or logo may remain visible anywhere in the running game.

### 5.5 Asset inventory (must be complete)
- **Tiles:** 5 regions × full `DungeonTileSheet` index set, plus new decor per region: Sewers (bone piles, rusted grate, mildew), Prison (chains, gibbet, bloodstain), Caves (crystal cluster, fungus, ash), City (broken statue, tapestry remnant, candle), Halls (obsidian, ritual circle, bone throne).
- **Characters:** all upstream heroes (6), all upstream mobs and bosses, plus new: Necromancer, Enchanter, Psychic heroes; Skeleton (ally recolor), Ghoul (ally), cursed mob variants (recolor pass + green emissive), Hexcaster elite, Chainwarden prison boss variant.
- **Items:** all upstream item indices plus new: Bone Rod, Phylactery, Sigil Brush, Runed Baton, Focus Ring, Focus Crystal, Scythe family (3 tiers), Bone armor, Leather variants, Hourglass of Ashes, Wand of Necrosis, Wand of Gravity, Wand of Bone, Spell of Soulfire.
- **Effects:** bone wall, force wall, necrotic particle, curse aura ring, sigil glyphs (weapon/armor inscription), psychic storm ring, corpse explosion burst.
- **Talent icons:** all §6–8 talents.
- **Title screen:** Grimhollow wordmark and title background (see §5.4). Also update the window title, About screen text, and any in-game reference to the upstream name so the build identifies as Grimhollow while preserving the required GPL attribution to Pixel Dungeon and Shattered Pixel Dungeon in the About/credits screen.

### 5.6 Art pipeline (required deliverable)
Create `tools/artgen/` containing a scripted, deterministic pipeline that regenerates every asset from source files:
- **Preferred approach:** procedural painting with layered noise textures, palette quantization to §5.2, silhouette masks per sprite defined in a small vector/JSON description, and outline/shading passes. Python + Pillow/NumPy is acceptable. Any generative model may be used *if* it runs locally, is seeded, and its output passes §5.3 automatically; otherwise use procedural.
- **Inputs:** `tools/artgen/specs/*.json` — one spec per asset (dimensions, palette roles, silhouette, animation keyframes described as transforms).
- **Outputs:** the sheets in §5.4. Running `python tools/artgen/build.py` must reproduce the committed assets byte-for-byte.
- **Validation:** `python tools/artgen/validate.py` runs §5.3 checks on every sheet and fails non-zero on any violation.
- **Human override:** any PNG in `assets/` with a sibling `.lock` file is skipped by the generator, so hand-painted replacements persist.

---

## 6. Class: Necromancer

Enum entry `NECROMANCER`; subclass entries `DEATHSPEAKER`, `HEXWEAVER`. Sprite `hero_necromancer.png`. Dominant color bone, accent necrotic green.

### 6.1 Stats and kit
- Base stats identical to Mage (HP 20, +5/level; str 10).
- **Bone Rod** (`items/weapon/melee/BoneRod.java`): tier 1, dmg 1–6, str req 10, delay 1. Unique class weapon (like Mage's Staff): cannot be dropped, can be upgraded. On killing blow: `Phylactery.gainCharge(1)`.
- **Phylactery** (`items/Phylactery.java`): equipped in a dedicated slot like the Cleric's Holy Tome; charges start 0, cap 3 (5 for Deathspeaker). No time regen. `gainCharge` on any kill by hero or hero-owned minion. Opens a radial spell menu.
- **Raise Skeleton** (1 charge): spawns `NecroSkeleton` (ally, based on upstream `Skeleton` with `DirectableAlly` behavior) on an adjacent empty cell. HP = 15 + 4 × heroLevel; dmg = 2 + heroLevel/2 to 5 + heroLevel; armor = heroLevel/3. Lifetime 40 turns (dies, no explosion). Max concurrent minions 2 (3 with Bone Legion).
- **Wither** (1 charge, targets any cell in sight): `Weakness` 10 turns + `Vulnerable` 10 turns.
- Starting bag: Bone Rod, Phylactery, cloth armor, 2 food rations, 1 Potion of Toxic Gas (identified), 1 Scroll of Identify.
- Identifies at start: Wand of Corrosion, Potion of Toxic Gas.

### 6.2 Talents
**T1** (levels 2–6, 5 points, 2 max each)
| Talent | Effect |
|---|---|
| Grave Harvest | Kills grant an extra charge 25/50% |
| Bone Meal | Eating heals all minions 25/50% max HP |
| Corpse Sense | Minions +2/+4 view distance; hero sees what they see |
| Necrotic Touch | Melee hits apply Corrosion 1/2 turns (dmg 1) |

**T2** (levels 7–12, 5 points, 2 max each)
| Talent | Effect |
|---|---|
| Sturdy Bones | Minions +10/20% max HP, +1/+2 armor |
| Dark Pact | If a cursed (Wither/any curse) target dies, hero heals 1/2 HP × remaining curse turns, capped at 15 |
| Second Grave | Minion death → 15/30% chance a new skeleton spawns in place (does not count against cap for 5 turns) |
| Ward of Bone | Once per floor at <30% HP: Barkskin (lvl/2, lvl) for 20 turns |
| Soul Siphon | Cursed enemies drain 1/2 HP/turn to hero (max 1 target contributes per turn) |

**T3** (level 13+, subclass)
- Common: *Deathspeaker's Command* (minions gain +1/+2/+3 damage), *Grave Wisdom* (charges cap +0/+1/+1; Wither also Cripples 0/2/4 turns).
- Deathspeaker: *Bone Legion* (+1 concurrent minion), *Grave Speech* (minions +5/10/15% accuracy and evasion per other minion alive).
- Hexweaver: *Lingering Hex* (curse duration +25/50/75%), *Cursed Ground* (cursed enemy death spreads its curse to adjacent enemies at 50/75/100% duration).

### 6.3 Subclasses
- **Deathspeaker:** Phylactery cap 5. New spell **Raise Ghoul** (2 charges): HP 25 + 6×lvl, dmg 4+lvl/2 to 8+lvl, 30% lifesteal to hero, lifetime 40. Minions inherit hero's Bless, Haste, Barkskin while active.
- **Hexweaver:** four new curses, 1 charge each, one curse per target (new replaces old), duration 12 turns base:
  - **Amplify Suffering**: target takes +50% physical damage (`AmplifySuffering extends Buff`; hook in `Char.defenseProc` or damage pipeline as upstream does for `Vulnerable`; do not stack with Vulnerable, take max).
  - **Decrepify**: Slow + Weakness + Cripple, 6 turns.
  - **Iron Maiden**: attacker takes 50% of melee damage dealt to target back as physical damage.
  - **Lower Resistance**: target loses `immunities` and `resistances` (implement as a buff checked in `Char.isImmune`/`Char.isInvulnerable`), and elemental damage +25%.

### 6.4 Armor abilities (T4)
| Ability | Charge cost | Effect | Talents (1–3 pts) |
|---|---|---|---|
| Corpse Explosion | 35% | Target a "corpse cell" (any cell where an enemy died this floor; track in `Level` as a `SparseArray<Integer>` of maxHP). 3×3 physical dmg = 20% of corpse maxHP + 2×lvl, min 10. Consumes corpse. | Wider Blast (+1 radius); Rot (adds Corrosion 3/5/7 turns, dmg 2); Soul Refund (kill refunds 1 charge, 33/66/100%) |
| Death Pact | 50% | Kill all minions; heal hero their summed remaining HP (cap 50% max HP); Adrenaline 10 turns. | Blessed Pact (Bless 5/10/15 turns); Bone Shell (Barkskin lvl/2 for 10/15/20 turns); Reclaimed (refund 1/2/3 charges) |
| Bone Prison | 60% | Ring the target cell's 8 neighbors with `BONE_WALL` terrain (new terrain: impassable, blocks LOS like walls, not diggable) for 10 turns. Occupied cells are skipped. Reverts on timeout, level change, and load. | Lasting Cage (+3/6/9 turns); Jagged (adjacent enemies take 2/4/6 dmg per turn); Necromancer's Key (hero can pass through 1/2/3 times) |

Armor abilities use upstream's charge system (`HeroClass` armor ability charge, "Heroic Energy" talent included).

---

## 7. Class: Enchanter

Enum `ENCHANTER`; subclasses `ARTIFICER`, `SCRIVENER` (display name "Scrivener"; not `WARDEN`, which upstream uses for the Huntress). Sprite `hero_enchanter.png`. Dominant color iron/blue-grey, accent holy gold.

### 7.1 Stats and kit
- Base stats identical to Rogue (HP 20, +5/level; str 10).
- **Runed Baton** (`RunedBaton.java`): tier 1, dmg 2–6, str 10. Unique class weapon. On floor entry, rolls a random *common* enchantment (Blazing, Shocking, Chilling, Kinetic, Lucky, Blooming) at 50% proc strength for that floor; shown in item description.
- **Sigil Brush** (`SigilBrush.java`): charges start 2; +1 max at levels 7, 13, 20. Regen: 1 charge per (40 − 2×lvl) turns, min 20. Radial spell menu.
- **Inscribe** (1 charge): choose from all enchantments/glyphs *identified this run* (tracked via upstream's `Catalog`/`Notes` knowledge of enchantments; if none, only the Runed Baton's current one). Applies as a temporary enchantment/glyph on the equipped weapon or armor for 30 turns. Does not replace a permanent enchantment; both proc (temporary at 100%, permanent normally). One temporary per item.
- **Hex Sigil** (1 charge, target in sight): Hex 10 turns + new buff **Degraded Gear** 10 turns (enemy accuracy −20%, armor −30%).
- Starting bag: Runed Baton, Sigil Brush, cloth armor, 2 food, 1 Scroll of Enchantment (identified), 1 Potion of Healing.
- Identifies at start: Scroll of Enchantment.

### 7.2 Talents
**T1**
| Talent | Effect |
|---|---|
| Keen Study | Reading a scroll / drinking a potion advances Brush regen by 5/10 turns |
| Steady Hand | Inscriptions +10/+20 turns |
| Sharpened Sigils | Hex Sigil also Vulnerable 3/5 turns |
| Field Repair | Descending restores 1/2 charges |

**T2**
| Talent | Effect |
|---|---|
| Dual Inscription | 25/50% inscription applies to both weapon and armor |
| Resonance | While inscribed, permanent enchant proc chance ×1.1/1.2 |
| Warding Sigils | After 3 stationary turns: Barkskin 1×/2× lvl for 5 turns (refreshes while stationary) |
| Attunement | Enchant/glyph on picked-up gear revealed 50/100% |
| Overload | Inscription expiry triggers the enchantment's proc once on all adjacent enemies at 50/100% |

**T3**
- Common: *Efficient Sigils* (10/20/30% chance Inscribe costs no charge), *Deep Knowledge* (start floors with 0/1/2 random enchantments temporarily "known" for Inscribe).
- Artificer: *Master Craft* (Reinforce also +1/+2/+3 flat dmg or armor), *Lasting Work* (on descent, 25/50/75% chance the current inscription or Reinforce becomes permanent; once per floor; permanent enchant replaces existing).
- Scrivener: *Wide Field* (aura radius +0/+1/+2), *Counterweight* (each debuff applied grants 1/2/3 turns Barkskin at lvl/2).

### 7.3 Subclasses
- **Artificer:** **Transmute Sigil** (2 charges): reroll the permanent enchantment/glyph on equipped weapon/armor to a random different one of the same rarity tier. **Reinforce** (1 charge): +1 temporary upgrade level on weapon or armor for 50 turns (uses upstream's temporary level system as in the Mage's Staff/Magical Infusion).
- **Scrivener:** **Sanctify** (1 charge): 3×3 around hero: allies/hero gain Bless 8 + Haste 8. **Nullify** (2 charges): 5×5 around target: remove all buffs from enemies (`Buff.detach` on all non-permanent), and apply **Silenced** 5 turns (enemy `Mob` subclasses that cast — Warlock, Shaman, DM-100, Necromancer, Hexcaster — skip ranged/cast actions). **Fracture** (1 charge): target's `drRoll` returns 0 for 6 turns.

### 7.4 Armor abilities
| Ability | Cost | Effect | Talents |
|---|---|---|---|
| Overcharge | 35% | 10 turns: every enchantment/glyph (permanent + inscribed) procs on every hit/hit-taken. | Sustained (+3/6/9 turns); Amplified (procs at ×1.25/1.5/1.75 strength); Feedback (kill during Overcharge refunds 1 Brush charge 33/66/100%) |
| Sanctuary | 50% | 5×5 zone for 15 turns: hero/allies regen 1 HP per 2 turns; enemies inside Slowed and their attacks are Hexed (−30% accuracy). Zone is a `Blob`. | Wide Sanctuary (+1/2/3 radius); Consecrated (enemies inside Corrosion dmg 1/2/3); Mobile (zone recenters on hero 25/50/100% of moves) |
| Unmaking | 60% | Target: armor 0, weapon enchant negated, all buffs removed, Vulnerable + Cripple 8 turns (bosses 4). | Cascade (also adjacent enemies at 33/66/100%); Salvage (kill refunds 1/2/3 charges); Reclamation (heal 1/2/3 HP per buff removed) |

---

## 8. Class: Psychic

Enum `PSYCHIC`; subclasses `PUPPETEER`, `SEER`. Sprite `hero_psychic.png`. Dominant color slate, accent psychic violet.

### 8.1 Stats and kit
- Base stats identical to Huntress (HP 20, +5/level; str 10).
- **Focus Ring** (`FocusRing.java`): tier 1, dmg 1–5, str 10. Unique class weapon.
- **Focus Crystal** (`FocusCrystal.java`): charges start 2; +1 max at 7/13/20. Regen 1 per (40 − 2×lvl) turns, min 20.
- **Grasp** (1 charge, any cell in sight): if the cell holds an item heap, move the heap to the hero's cell (pick up as if walked over). If the cell holds a trap (visible or hidden but known), trigger it as if a `Char` stood there, then remove the trap. If both, prefer item. If neither, no effect and no charge cost.
- **Glimpse** (1 charge): Mind Vision 5 turns.
- **Class passive — Telekinetic Force:** all `MissileWeapon` damage from the hero: `+ floor(heroLevel / 5)` added after base roll, before enchant and `Fracture Point`. Displayed in thrown weapon descriptions.
- Starting bag: Focus Ring, Focus Crystal, cloth armor, 2 food, 3 Throwing Knives, 1 Potion of Mind Vision (identified).
- Identifies at start: Potion of Mind Vision, Scroll of Magic Mapping.

### 8.2 Talents
**T1**
| Talent | Effect |
|---|---|
| Recall | Thrown weapons return to inventory after landing 50/100% (durability still consumed) |
| Wrench | Grasp on a heap under an enemy: Vertigo 2/4 turns |
| Lingering Sight | Kills extend active Mind Vision 1/2 turns |
| Trap Sense | Traps in view auto-search; remotely triggered trap damage ×1.25/1.5 |

**T2**
| Talent | Effect |
|---|---|
| Guided Throw | Thrown accuracy +10/20%; ignore 1/2 tiles of range penalty |
| Deep Focus | Crystal regen ×1.15/1.3 while no enemy in view |
| Precognition | Once per floor, a hit that would drop hero below 25% HP is dodged |
| Kinetic Reserve | On floor entry gain 1/2 charges |
| Fracture Point | Thrown hits on Vertigo'd or Hexed targets +2/+4 dmg |

**T3**
- Common: *Focused Mind* (charge cap +0/1/1), *Far Reach* (Grasp range +0/2/4 beyond view via Mind Vision-revealed cells).
- Puppeteer: *Shared Will* (dominated enemies gain hero's Bless/Haste/Barkskin), *Harvest Thought* (dominated enemy kills refund 1 charge 33/66/100%).
- Seer: *Heavy Hand* (Hurl works on bosses at 1 tile; +1 tile on others), *Treasure Sense* (reveal all items on floor entry; 3 pts also reveals traps).

### 8.3 Subclasses
- **Puppeteer:** **Dominate** (2 charges): Amok 15 turns; extend `Amok` with a `dominated` flag so the target's `chooseEnemy` prefers other enemies and never the hero while another enemy is in view. Bosses: 5 turns. **Suggestion** (1 charge): Terror 10 turns.
- **Seer:** passive Mind Vision radius 3 (implemented as a permanent `MindVision`-like buff with limited radius); traps and hidden doors revealed within radius 3 regardless of LOS. **Hurl** (1 charge): move non-boss target 3 cells in chosen direction using `WandOfBlastWave.throwChar`; wall impact → Paralysis 2 turns; landing on a trap → trigger on the enemy.

### 8.4 Armor abilities
| Ability | Cost | Effect | Talents |
|---|---|---|---|
| Psychic Storm | 35% | Radius 4: Vertigo 5 + Amok 5 on all enemies. | Wider Storm (+1/2/3 radius); Dread (adds Terror 3/6/9); Backlash (refund 1 charge per 3/2/1 enemies affected) |
| Mind Meld | 50% | Full-floor Mind Vision 20 turns; reveal all items and traps. | Long Meld (+10/20/30 turns); Total Sight (reveal secrets/hidden doors at 1+, whole map at 3); Kinetic Surge (thrown dmg ×1.5 during effect at 3; ×1.25 at 1–2) |
| Force Wall | 60% | 3-cell line of `FORCE_WALL` terrain perpendicular to hero→target direction, at the target cell, 8 turns. Impassable, transparent (does not block LOS). | Held Firm (+3/6/9 turns); Repulse (enemy walking into it: Paralysis 1/2/3); Permeable (hero's thrown weapons pass through at 1+; hero may walk through at 3) |

---

## 9. Content additions

### 9.1 New item curses (malevolent enchantments, `items/weapon/curses/` and `items/armor/curses/`)
| Name | Slot | Effect |
|---|---|---|
| Leech | Weapon | 30% on hit: enemy heals 20% of damage dealt |
| Withering | Armor | −1 max HP per floor descended while worn (restored on removal) |
| Echo | Weapon | 25% of damage dealt is also dealt to hero |
| Dark Blessing (rare, 5% of curse rolls) | Either | Weapon: +30% dmg, hero takes 10% of dealt as recoil. Armor: +3 armor, −20% healing received. Reads as cursed until identified. |

### 9.2 New wands and spells
| Item | Type | Effect |
|---|---|---|
| Wand of Necrosis | Wand, tier as Corrosion | Bolt: Corrosion (dmg 2+lvl) that chains to 1+lvl/2 adjacent enemies at −1 dmg per hop |
| Wand of Gravity | Wand | Pull target enemy 2+lvl/3 cells toward hero; if it collides with another char, both Vertigo 3 |
| Wand of Bone | Wand | Raise a 1-cell BONE_WALL at target for 5+lvl turns (same terrain as Bone Prison) |
| Spell of Soulfire | Alchemy spell (Potion of Liquid Flame + Scroll of Terror + energy 6) | Thrown: 3×3 fire that ignores fire immunity and applies Terror 4 |

### 9.3 New weapons (reuse existing `MeleeWeapon` behaviors)
| Weapon | Tier | Dmg | Behavior |
|---|---|---|---|
| Bone Scythe | 2 | 4–14 | Reach 2 (as Glaive), sweep: 50% of damage to the cell adjacent-and-perpendicular on either side of the target |
| Reaper's Scythe | 4 | 8–28 | Reach 2, sweep at 75% |
| Grave Scythe | 5 | 10–36 | Reach 2, sweep at 100%, +10% dmg vs cursed targets |

### 9.4 New armor
- Bone Armor: tier 3 equivalent, +1 armor, −1 evasion vs. Scale; immune to Withering curse.
- Leather variants for tiers 2–3 are recolors only (no stat change).

### 9.5 New artifact — Hourglass of Ashes
Charges 0–10 (regen 1 per 30 turns, faster with upgrades). Activate: enemies in view lose their last N turns of buffs (N = level), and the hero regains the last action's turn cost once (cannot chain). Upgrades by absorbing Corrosion damage taken.

### 9.6 New mobs
- **Cursed variants:** 10% of eligible spawns (rat, gnoll, crab, skeleton, thief, bat, brute, shaman, monk, warlock, golem, succubus, eye, scorpio) spawn with a random Hexweaver curse *on themselves* (they apply it to the hero on hit, 5 turns). +30% HP, drop an extra item from the floor loot table. Green emissive tint.
- **Hexcaster** (elite, floors 11–20): HP 60, ranged caster; alternates Amplify Suffering and Decrepify on the hero at range; flees at melee range. Drops a random wand 25%.
- **Chainwarden** (Tengu-level variant boss, 30% chance replaces Tengu on floor 10): Tengu stats; instead of traps, summons Chain Traps (Prison chain visuals) that root the hero 2 turns; every 4 turns pulls the hero 2 cells toward himself (Gravity effect).

### 9.7 Existing content adjustments
- Add all new enchantments/curses to `Weapon.Enchantment`/`Armor.Glyph` pools with upstream's rarity weights (curses weight 3 each, Dark Blessing weight 1).
- Add new wands to the wand generator table at weight 3.
- Do not change existing drop rates otherwise.

---

## 10. Acceptance tests

Implement as JUnit tests in `core/src/test/` where feasible; otherwise as a headless smoke script.

### 10.1 Build
1. Clean clone → `./gradlew desktop:run` launches to title screen.
2. `./gradlew android:assembleDebug` succeeds.
3. `python tools/artgen/build.py` then `git status` shows no changes (assets reproducible).
4. `python tools/artgen/validate.py` exits 0.

### 10.2 Gameplay (headless where possible)
5. Each of 9 classes starts a run with its kit; inventory contents match §6–8.
6. Each new talent can be selected at its tier and does not throw when its hook fires (drive via scripted hero level-ups and simulated combat).
7. Each subclass is selectable at level 13 via the Tengu reward flow.
8. Each armor ability is selectable at level 21 and executes without exception in a scripted scenario (target present, minions present, corpse cell present, etc.).
9. Bone Prison / Wand of Bone / Force Wall terrain reverts on timeout, on level change, and after save/load.
10. Raise Skeleton respects the concurrent cap; Second Grave does not exceed cap +1.
11. Grasp moves a heap, triggers a trap, and costs no charge on empty cell.
12. Telekinetic Force adds exactly `floor(lvl/5)` to a thrown weapon roll at levels 1, 5, 10, 30.
13. Dominate causes the target to attack a different enemy in a two-enemy scenario for the duration.
14. Save/load round-trips a run mid-floor with active inscriptions, curses, minions, and dominated enemies.
15. Full automated run: a bot that descends by taking stairs when found (upstream has a debug "descend" path) reaches floor 6 with each new class without exception, 10 seeds each.

### 10.3 Art
16. Every sprite index referenced in code has a non-empty frame in the sheet (no magenta/empty frames).
17. Every sheet passes §5.3 constraints 1–6 programmatically.
18. Screenshot of each region at floor 1 of that region has mean luminance within 0.15–0.35.

### 10.4 Performance
19. Desktop headless timing: 1,000 simulated turns on floor 15 with 20 mobs and lighting on completes in < 3 s.

---

## 11. Delivery format

- Single Git repo; upstream history preserved; all work on branch `grimhollow`.
- Tag `v0.1.0-oneshot` at the passing state.
- `CHANGES.md` lists every new and modified file with a one-line reason.
- `KNOWN_ISSUES.md` lists any acceptance test that could not be made to pass, with the failing output. An honest failure list is preferred over a silent skip.

---

## 12. Priority order and current checkpoint

**Checkpoint (commit `a4015d1`, branch `grimhollow`):** build, packaging, CI, desktop-only flag, geometry for terrain, Sewers tiles/walls at 64px, lighting overlay (ambient, hero, wall torches, persistent blobs), vignette, blood decals, art pipeline for the assets listed in ART_PIPELINE.md, headless smoke harness. Verified by the logs under `verification/`. Tests 1, 2, 3, 20, 22, 23 pass.

**Remaining work, in priority order.** Finish each stage to a clean, building, committed, pushed state before starting the next:

1. **Stage 1 remainder:** (a) lighting falloff fix per §4.3 (radial, no boundary halo); (b) character and item geometry (`CharSprite`, `HeroSprite`, `MobSprite`, `ItemSprite`, `MissileSprite`) reading `GameGeometry`; (c) pipeline generation of the six upstream hero sheets and `items.png` at texture resolution, using upstream silhouettes as vector sources exactly as the Sewers tiles do. (d) Grimhollow title screen wordmark and background per §5.4, and the About/credits text update. Transient light sources and remaining regions are deferred to stage 3.
2. **Necromancer complete** (§6). Green headless gate for this class alone (`Runs=10 failures=0`).
3. **Art pipeline coverage:** remaining four regions, all upstream mobs, new-hero sheets, transient light sources. Test 17 passes.
4. **Enchanter complete** (§7).
5. **Psychic complete** (§8).
6. **§9 content**, in listed order.

---

## 13. Build environment, packaging, and CI

### 13.1 Package identity
- Change the Android `applicationId` (in `android/build.gradle`) to `com.grimhollow.dungeon`. Keep Java package names as upstream to avoid a mass refactor; only the application ID and app label change.
- App label: "Grimhollow". Replace the launcher icon set (`android/src/main/res/mipmap-*`) and desktop icons with new assets from the art pipeline (§5.6), in the §5.2 palette.
- Rationale: the debug-signed APK must install side-by-side with the Play Store SPD, which is impossible with the same application ID.
- Desktop: change the window title and the LWJGL3 launcher's preferences folder name so settings and saves do not collide with an installed upstream desktop build.

### 13.2 Desktop-only builds
- Add a Gradle project property `desktopOnly`. When `-PdesktopOnly=true` is passed (or `desktopOnly=true` is set in `gradle.properties`), `settings.gradle` must exclude the `android` module so configuration succeeds with no Android SDK present.
- Verify: on a machine with only a JDK 17 installed and no `ANDROID_HOME`, `gradlew.bat desktop:run -PdesktopOnly=true` launches the game on Windows, and `./gradlew desktop:run -PdesktopOnly=true` does the same on Linux/macOS.
- Provide `gradlew.bat` and `gradlew` in the repo root with executable bit set.
- Document in `README.md`: JDK version, the desktop-only command, and the full-build command.

### 13.3 Continuous integration (required)
Add `.github/workflows/build.yml` that runs on every push and pull request and on manual dispatch:

**Job `desktop`** (ubuntu-latest and windows-latest matrix):
1. Checkout, set up JDK 17 (Temurin).
2. `gradlew desktop:dist -PdesktopOnly=true` (upstream's desktop module produces a runnable jar via the `dist` task; if the task name differs in the forked version, use the equivalent and note it in README).
3. Run `python tools/artgen/validate.py` (Python 3.11).
4. Run JUnit tests: `gradlew core:test -PdesktopOnly=true`.
5. Upload the jar as artifact `grimhollow-desktop-<os>`.

**Job `android`** (ubuntu-latest):
1. Checkout, set up JDK 17, set up Android SDK (use `android-actions/setup-android`), install the `compileSdk` platform and build-tools the project requires.
2. `./gradlew android:assembleDebug`.
3. Upload `android/build/outputs/apk/debug/*.apk` as artifact `grimhollow-android-debug`.

**Job `headless-smoke`** (ubuntu-latest, depends on `desktop`):
1. Run the §10.2 test 15 bot for each new class, 10 seeds, via a Gradle task `core:smokeRun`.
2. Fail the job on any exception; upload logs.

Constraints:
- Workflow must be green on the first push of the `grimhollow` branch. A red CI run at delivery counts as a failed acceptance test unless the cause is documented in `KNOWN_ISSUES.md`.
- Artifact retention 14 days.
- No secrets required; no release signing. Debug signing only.
- Cache Gradle dependencies using `actions/cache` or the Gradle setup action to keep runs under 20 minutes.

### 13.4 Sideload instructions (for `README.md`)
- Download `grimhollow-android-debug.zip` from the latest Actions run, extract the APK.
- On the device: enable Developer Options; allow "Install unknown apps" for the app used to open the APK; open the APK and install. Or `adb install -r grimhollow-debug.apk` over USB with USB debugging enabled.
- The app installs alongside Shattered Pixel Dungeon without conflict (§13.1).

### 13.5 Additional acceptance tests (extend §10)
20. `gradlew.bat desktop:run -PdesktopOnly=true` succeeds in an environment with no `ANDROID_HOME` set.
21. The CI workflow file exists and all three jobs pass on the delivery commit.
22. The debug APK's `applicationId` is `com.grimhollow.dungeon` and its label is "Grimhollow" (check via `aapt dump badging`).
23. Desktop build writes saves and settings to a directory distinct from upstream's.

---

## 14. Process guidance for continuation runs

These rules exist because the first run spent most of its time on verification scaffolding and produced no gameplay content.

- **The verification harness is sufficient.** `verification/`, `SmokeRun`, `DesktopSmokeProbe`, `validate.py`, `update-change-ledger.py`, and the CI workflow exist and work. Do not build new probes, ledgers, evidence formats, or acceptance documents. Extend `SmokeRun` only where a new class requires it. Append to existing logs; do not restructure them.
- **Verify at stage boundaries, not continuously.** Within a stage, compile and run the directly relevant test. Run the full acceptance set (§10, §13.5) once per stage, at the end, then commit and push.
- **Content over evidence.** A stage that ships a working feature with a two-line KNOWN_ISSUES entry is worth more than a stage that ships a perfect report and no feature. Spend at least 80% of effort on the numbered stage deliverable.
- **Do not re-verify what the checkpoint already proved.** Tests 1, 2, 3, 20, 22, 23 are passing; re-run them only in the final CI run.
- **Environment friction.** If a command fails for sandbox or permission reasons, retry once with the documented escalation route from KNOWN_ISSUES.md, then record it and move on. Do not spend more than a few minutes on any single environment issue.
- **Deviations.** When the spec is wrong or a better engineering approach exists (as with the 16-unit logical geometry), take the better approach, note it in one line in CHANGES.md, and continue. Do not stop to ask.
- **Report format is unchanged** (see the prompt's Finishing section), but keep the KNOWN_ISSUES entries to one line each.

---
