# Grimhollow Bounty Board Quest Draft

Version 0.1 | 6 October 2026 | Design for review

Cole, a bounty hunter operating from an abandoned Prison office, offers three dangerous contracts and a small premium shop. Completing two contracts unlocks a bounty on the actual Prison boss. Cole pays that bounty, then sells out the hero. Hunter crews pursue the hero through the Caves, City, and Halls, until the hero negotiates a settlement, confronts Cole, or outlasts the contract.

This draft records the agreed quest and item rules. Sections explicitly marked **Proposal** contain initial values or details still subject to review. Writing this specification does not authorize implementation. The earlier pasted Bounty Board proposal is superseded wherever it conflicts with this draft.

## 1 Scope

Add Cole, his office and shop, three Prison contracts, a Prison boss contract, wanted posters, three later hunter crews, the resolution encounters, four cosmetic badges, Bloodmarked Brand, and Warden's Coat.

Preserve existing classes, subclasses, armor abilities, ordinary quests, identification, alchemy, shops, and progression. Tengu's mask and the corresponding progression reward from another Prison boss retain their existing behavior.

Excluded from this quest:

- Trading the boss reward or replacing subclass selection with Bounty Hunter paths.
- A new helmet slot, restraint weapon, lockpicking system, or quest currency.
- The original Sharpened Bounty Knife, Lockpick Set, Tracker's Hood, Bounty Ledger, and Collector's Seal.
- Wardbreaker Salt, which Bloodmarked Brand replaces.
- A Prison hunter crew after the boss, recurring hunter respawns, and additional floors or quest branches.
- Permanent gameplay advantages in future runs. Badges and any lasting unlocks are cosmetic.

Cole's rewards are finite. This quest does not refresh ordinary shops or expand existing secret-room, elemental-cache, or expedition quotas.

## 2 Cole and the office

Cole appears on **Prison floor 2, global dungeon floor 7**, in a dedicated former warden's office. The office generates in every new ordinary run. It must coexist with existing Prison quest rooms and leave the main route accessible.

Cole is a scarred human hunter in worn leather, with a crossbow on his back. His board, papers, weapon rack, and expensive stock establish his profession. He remains non-hostile during the working relationship and cannot be killed before the betrayal. He does not roam out of the office during this phase.

**Proposal:** use an ordinary accessible doorway. Do not add the earlier proposal's Prison Key or make access depend on a particular class or artifact. The office entrance and board become journal landmarks when discovered, without revealing unseen rooms.

Buying from Cole does not accept a contract. Examining a poster does not accept it either. The board provides separate acceptance controls.

### First meeting dialogue

| Class | Greeting |
|---|---|
| Warrior | "Soldier. You know what a hard job looks like. Mine pay better than most." |
| Mage | "A scholar. My clients care very little how the work gets done." |
| Rogue | "Careful hands. Quiet feet. There's work here for someone like you." |
| Huntress | "You track. I can tell by how you entered. The things I need found don't stay still." |
| Duelist | "A duelist. You may appreciate having a name to put to your next opponent." |
| Cleric | "Holy work in an unholy place. Some of my clients would call that convenient." |
| Necromancer | "The dead follow you. Useful company, in my line of work." |
| Enchanter | "Everything about your gear is deliberate. Someone who pays attention. I can work with that." |
| Psychic | "You looked at the board before you looked at me. See anything interesting?" |

These are proposed final wording. Cole explains the arrangement briefly and leaves the choice with the player. His warmth should gradually give way to calculation as contracts are returned.

## 3 Cole's shop

The shop contains exactly **five sale slots**:

| Slot | Stock |
|---|---|
| 1 | One substantial food item |
| 2 | One quality consumable |
| 3 | One different quality consumable |
| 4 | One premium weapon |
| 5 | One bundle of Bloodmarked Brands |

Stock is seeded once, saved, and never refreshed. Sold goods stay sold. Cole's stock is separate from the three bounty prizes and later settlement choices. All five slots use familiar purchase controls and display their actual prices before purchase.

Bloodmarked Brand and Warden's Coat enter through this quest. Do not add them to ordinary random loot or alchemy pools. Other existing items continue to use their normal generation rules.

The weapon is useful Prison equipment, with better quality than an ordinary random find. Cole's prices are deliberately high. Shopping competes with purchases elsewhere and does not become a cheap upgrade source.

### Proposal for initial stock and prices

- Food: a ration, priced at three times the equivalent ordinary Prison shop price.
- Consumables: choose two distinct useful existing potions, scrolls, or their established crafted variants; price each at three times its equivalent Prison shop price. Exclude guaranteed progression items such as Scrolls of Upgrade and Potions of Strength from this initial stock pool.
- Weapon: a tier-3 weapon at +1 or +2, uncursed, with a beneficial enchantment; price at four times its equivalent ordinary Prison shop price. Use existing weapon categories, including ranged options.
- Specialty: two Bloodmarked Brands, sold together for 600 gold.

Use the game's normal shop valuation as the baseline, including the item's actual quantity and upgrades. Round displayed prices consistently. These prices and stock quality are tuning proposals, not established balance results.

## 4 The four contracts

Cole's board offers **one Common, one Rare, and one Legendary Prison contract**. The fourth contract targets the actual Prison boss and unlocks after **any two of the three Prison contracts have been completed and returned to Cole**. Legendary is optional for the normal Common-plus-Rare route.

All three Prison contracts may be accepted during the same visit. There is no limit of one active target at a time. Each target's floor is chosen independently from Prison floors 2, 3, and 4, corresponding to global floors 7, 8, and 9. Multiple contracts can name the same floor, so accepting all three can create overlapping encounters.

Before acceptance, each poster shows:

- A fixed alias and the correct existing creature painting.
- Its Common, Rare, or Legendary seal.
- A brief atmospheric description of the wanted creature's distinguishing strengths.
- **"Last seen on Prison floor X."**
- The offered payment or carried prize, plus any prompt-completion bonus terms.
- A distinct **Accept contract** control.

Targets, modifiers, rewards, and named floors are seeded before acceptance and cannot be rerolled by reopening the board, changing floors, or loading a save. Rarity reflects the difficulty of the base mob and its particular modifier combination, rather than simply counting buffs.

No wanted target exists before its contract is accepted. On acceptance, its spawn becomes pending. If the named floor is currently loaded, place it at a valid unoccupied location outside immediate hero sight and outside Cole's office. Otherwise, spawn it on the next entry to that floor. Already generated floors support this pending spawn without rebuilding the map.

Spawn placement must use reachable ordinary terrain, preserve entrances and exits, and respect mob size. If a valid location is temporarily unavailable, retain the pending spawn and retry when placement becomes possible. Never silently drop the contract, move its target to another floor, or put it on the hero's cell.

Wanted targets use their ordinary mob behavior and the same global tuning conventions as other mobs. They receive only their explicit wanted enhancements. Do not add a second random curse-bound or empowered-variant roll, and do not attach a self-weakening NecroCurse merely to identify the variant.

An accepted target's death completes its contract once, including deaths caused by allies, traps, falls, or other ordinary combat interactions. Completion does not depend on delivering the final blow personally. Summoned creatures and unrelated mobs of the same species do not count.

### Proposal for initial target pools

| Contract | Base mob candidates | Initial enhancements | Reward |
|---|---|---|---|
| Common | Skeleton or Thief | 1.25 times base health; 1.10 times damage | 300 gold |
| Rare | Guard, DM-100, or Necromancer | 1.60 times base health; 1.15 times damage; a compatible defense or speed trait | 650 gold |
| Legendary | Guard, presented as a former warden | 2.20 times base health; 1.25 times damage; two compatible wanted traits | Warden's Coat at +2 |

Suggested traits are **Ironhide**, adding 1 to both ends of the ordinary physical protection range, and **Quickstep**, multiplying ordinary movement speed by 1.10 without accelerating attack or spell cadence. These are proposed traits. Score each base-mob and trait combination for its actual difficulty before including it in a rarity pool. Retain normal status-effect vulnerabilities.

Wanted mobs remain non-boss enemies. They do not acquire blanket immunity to charms, knockback, inscriptions, or other ordinary tools. Their explicit reward is finite; do not also grant the unrelated curse-bound bonus-loot roll.

### Proposal for poster storage

Store authoritative contracts and collected hunter fragments in the quest journal. Posters are individually examinable cards with unique contract identities, not stackable loose inventory. This avoids consuming several bag slots or losing quest progress when paper is dropped. The old proposal's requirement to physically carry a poster at the moment of death is removed under this proposal.

## 5 Prompt completion bonuses

Common and Rare contracts offer an optional bonus for prompt completion. Missing the deadline does not fail the contract, remove its target, or reduce the base payment. Legendary and the Prison boss contract have no timer.

**Initial tuning proposal:** 500 elapsed game turns, with a 20% additional gold payment for an eligible kill before the deadline. At the proposed payments, this is 60 extra gold for Common and 130 for Rare.

The clock starts once the contract is accepted and the hero first enters its named floor. Acceptance while on that floor starts it immediately. Once started, elapsed game time on other floors also counts. Haste and other timing effects follow the game's normal elapsed-turn model; this is not a count of mouse clicks or player actions.

Stop the clock when the actual target dies. Returning to Cole later cannot remove an earned bonus. Menus, examination, real-world time, saving, and time while the game is closed do not count. Persist elapsed time across floor transitions and reloads.

The contract view distinguishes an unstarted clock, an active bonus deadline, a bonus earned, and a bonus expired. Warnings cannot imply that the whole contract will fail.

## 6 The Prison boss and betrayal

The boss contract follows the **actual boss assigned to global floor 10**, including Tengu or Chainwarden. Its portrait, name, text, completion condition, and dialogue all match that boss. Use one saved boss choice shared by the poster and level generator; do not make a second independent roll or change the configured boss-selection probability.

**Proposal:** the boss payment is 700 gold. The contract must be explicitly accepted before the boss dies. A boss defeated without an accepted contract cannot be claimed retroactively and does not start the betrayal arc. Previously accepted ordinary contracts remain completable.

After the accepted boss contract completes, Cole meets the hero at the departure area. He pays the boss reward **once**, then reveals a wanted poster for the hero. The exchange stops automatic movement while its dialogue is open. Cole steps aside; the stairs remain usable, and this encounter does not initiate combat or demand the boss progression item.

The hero's poster uses their existing character painting, name, and class title. **Proposal:** its displayed bounty is a snapshot of held gold plus the ordinary value of owned items at betrayal, traversing equipped gear and all bags without counting any item twice. The amount is narrative information, not a new damage or loot multiplier. It remains fixed afterward.

Cole closes his shop and stops offering new contracts. Contracts accepted before betrayal stay valid, including a Legendary target fought after the boss. The coat remains the Legendary target's carried prize and does not depend on Cole surviving or reopening his business.

Cole's office is initially empty afterward. The board retains the hero's poster and a note:

> "Nothing personal. You were worth more than the job."

### Betrayal dialogue proposals

| Class | Line |
|---|---|
| Warrior | "I've seen soldiers. I've seen knights. Whatever you are, someone wants you stopped. Pays well, too." |
| Mage | "Funny thing about magic users. Everyone wants one. The orders I get rarely explain why." |
| Rogue | "Professional courtesy would've been nice. But you're too good at this for courtesy to apply." |
| Huntress | "The forest taught you to hunt. Someone has decided you're worth hunting in return." |
| Duelist | "People remember a fine duelist. Unfortunately for you, some of them have money." |
| Cleric | "Whoever's praying for you up there, they've been answered often enough. Someone wants to stop that." |
| Necromancer | "The dead follow you. The living fear you. My employer noticed both." |
| Enchanter | "Everything you touch gets better. That makes you dangerous in ways most people don't understand. My employer does." |
| Psychic | "You already know what I'm about to say, don't you? Then you know I mean it." |

## 7 Bloodmarked Brand

**Type:** finite throwable consumable. Cole sells a pair in the specialty slot. It is an iron seal wrapped in crimson cord; its smouldering mark burns into the target's blood.

Throwing a Brand costs **one standard turn** and marks one enemy for **8 turns**. Your weapon attacks against that enemy gain **25% accuracy** and bypass **half its physical armor protection**. Both melee and ranged weapon attacks qualify. Bosses are valid targets.

The marked creature's position remains trackable outside normal sight. Reveal only that creature's position or silhouette. Do not reveal nearby terrain, items, other enemies, secret doors, or rooms. Tracking does not permit attacks through walls or bypass ordinary projectile collision.

Reapplication refreshes the duration. Accuracy and penetration do not stack with another Brand. Different enemies may each carry a mark, consuming a separate Brand. The effect does not replace enchantments, inscriptions, glyphs, or the enemy's special abilities.

Implementation details:

- The accuracy bonus multiplies the hero's applicable weapon attack accuracy by 1.25; it is not an extra 25 percentage points of hit probability.
- Bypass half the target's rolled physical armor reduction, rounded down, in the normal weapon damage calculation. Other kinds of damage reduction remain intact.
- The hero receives the attack benefits; allies and damaging wands do not inherit them.
- Target selection and delivery use existing throwable-consumable collision conventions. The Brand is consumed on use, with no automatic recovery.
- Use ordinary timed-buff behavior. The mark is temporary; it never permanently changes the target's stats.

**Inventory flavor:** "An iron seal wound with crimson cord. Its mark burns deeper than flesh. Once branded, the quarry is difficult to lose - and easier to wound."

The numerical rules belong in development and Playtest information, not in an exhaustive inventory explanation.

## 8 Warden's Coat

**Type:** unique quest armor, occupying the ordinary armor slot. It is carried by the Legendary target and drops once on its death. No cash reward or automatic recovery supplies accompany this prize.

The coat is dark leather beneath overlapping bronze plates, with the old warden's seal inside. It begins at **tier 3 and +2**, with the usual mail strength requirement. It accepts ordinary upgrades, glyphs, Enchanter inscriptions, and armor Rune Etching.

### Agreed upgrade progression

The first otherwise damaging hit from each enemy receives **10% damage reduction per permanent coat upgrade**, capped at **100% at +10**. The coat therefore starts with **20%** protection. Use the intrinsic saved upgrade level for this percentage, not `level()` or `buffedLvl()` bonuses. Rune Etching, Curse Infusion, and temporary upgrade bonuses do not increase it.

Beyond the starting +2, each upgrade adds **1 minimum and 4 maximum ordinary armor protection**. The ordinary unaugmented progression is:

| Permanent upgrade | Ordinary armor protection | First-hit reduction |
|---|---|---|
| +2 | 2 to 12 | 20% |
| +4 | 4 to 20 | 40% |
| +6 | 6 to 28 | 60% |
| +8 | 8 to 36 | 80% |
| +10 | 10 to 44 | 100% |

For nonnegative permanent upgrade level L, the baseline is minimum L and maximum `3 * (2 + L) + max(0, L - 2)`. Ordinary augmentation and other permitted armor modifiers apply through their existing paths. Existing challenges retain their armor restrictions. The coat has no artificial +10 upgrade ceiling; further upgrades improve ordinary protection while first-hit reduction stays at 100%.

At +10, the first qualifying hit from an enemy is absorbed completely. Reaching this from the found coat takes eight further upgrades. Subsequent hits from that enemy receive only the coat's ordinary armor and existing effects.

### Protection rules

- Qualify the hit after ordinary armor reduction. Misses and hits already fully blocked by that armor do not consume protection.
- For remaining direct damage D and first-hit fraction R, apply `ceil(D * (1 - R))`. At R = 1, this is zero. Record consumption even when the coat absorbs all remaining damage.
- Protect against one hit, not an entire multi-hit action or turn. Immediate enemy melee, ranged, and spell hits qualify. Environmental hazards, falls, and later damage-over-time ticks do not.
- Damage reduction does not prevent an attached poison, curse, or other debuff from applying under its normal rules.
- The protection is available once per individual enemy, not once per species, room entry, or combat encounter. It is consumed only while the coat's property is active on the wearer.
- Save that enemy's consumed state. Retreating, floor revisits, healing, re-equipping, transferring the coat, or converting it to class armor cannot refresh protection against the same enemy.
- Preserve ordinary interactions with shields and existing defensive effects. Do not create a second damage event or bypass their normal trigger rules.

Class-armor conversion **must preserve the coat's enhanced protection growth and first-hit property**, along with normal glyph, upgrade, and Etching transfers. Keeping the coat should remain a deliberate equipment choice through the City and Halls. That lifespan is a playtesting target, not a verified outcome.

**Inventory flavor:** "The outer plates carry the marks of many first blows. Whoever wore it learned to survive the greeting."

The target's death must produce the coat in a recoverable location through normal loot handling. Handle falls and an occupied corpse cell without deleting the prize or generating a duplicate.

## 9 Hunter crews

After betrayal, **one crew in each of the Caves, City, and Halls** hunts the hero. There are three crews in total, with **2, 3, and 3 hunters** respectively. No crew appears in the Sewers or Prison, or inside an expedition or another off-floor branch.

Each crew occupies an ordinary room and pursues through ordinary rooms on its own floor. It does not travel between floors. It spawns once, only while the bounty is active. Resolving the bounty prevents any pending crews from appearing and ends the pursuit by surviving crew members.

### Proposal for crew placement and composition

| Region | Global floor | Composition |
|---|---|---|
| Caves | 13 | Armored melee hunter and a support hunter using existing debuffs |
| City | 18 | Melee hunter, ranged hunter, and a tactical hunter with finite consumables |
| Halls | 22 | Melee specialist, ranged or wand specialist, and a support specialist |

Initial strength proposal: approximately **1.5 times equivalent regional mob health and 1.2 times damage**, with role-specific defenses. A crew should be dangerous without approaching a full regional boss in combined endurance and unavoidable damage. Its consumables, healing, and ammunition are finite and saved; refreshing a floor cannot replenish them.

Use ordinary navigation and status-effect rules. Hunters investigate the hero's last known location, respect invisibility, and can be controlled through compatible existing effects. They do not know the hero's changing position through walls. They may open ordinary doors, but do not bypass sealed quest doors or create keys. Traps and hazards still affect them normally.

Hunters do not steal belongings. Ordinary monster and Lurking Horror interactions continue under their existing rules; neither coordinates specially with a crew. Defeating all members of a crew yields exactly one protected poster fragment. Controlled, displaced, or temporarily allied surviving hunters still count as alive.

Pending crews can appear on an already generated floor when the hero next visits it after betrayal. Spawn outside immediate sight, preserve safe arrival cells, and never duplicate a crew after reload or re-entry.

## 10 Cole's return and outstanding debts

The hero can arrange a later meeting in **Cole's old office** using the board, or meet him in a dedicated **Halls room** while the bounty remains unresolved. Both routes are available. **Proposal:** the Halls room appears on global floor 22, away from the required route and hunter arrival positions.

Summoning Cole at the office is a discrete meeting interaction. He does not appear merely because the hero passes the room. The Halls encounter is also optional; it cannot block stairs or the route to the final boss.

At either meeting, Cole first pays all **earned, unpaid** Common and Rare rewards, including earned timing bonuses. This happens before the player chooses settlement or combat:

> "Here. I owe you this. Now we're even."

Keep an explicit payment record. Do not put the debt in a payment chest or escrow box, refund earlier purchases, or drop previously paid bounty money again when Cole dies. Accepting, killing, returning, claiming, and paying are separate states.

**Proposal for unfinished contracts if Cole is killed:** Cole's death drops a finite reserve covering the base payments of accepted, unfinished Common and Rare contracts. Record those payments as made, leave their targets completable, and close their prompt-bonus eligibility. This preserves the remaining base rewards without a posthumous payment system. This edge-case policy requires review; it is not an agreed reward rule.

## 11 Resolution and rewards

Returning a fragment starts a conversation, not an automatic attack. The player deliberately chooses confrontation or an available peaceful settlement. Settling with two fragments ends the pursuit and prevents collecting a third crew fragment afterward.

| Outcome | Requirement | Current-run result | Cosmetic badge |
|---|---|---|---|
| Confrontation | At least one fragment, or the optional Halls confrontation | Defeat Cole; bounty ends | Cole's Folly |
| Negotiated settlement | Two crew fragments | Bounty ends; choose one discounted quality reward | The Negotiated Settlement |
| Professional settlement | All three crew fragments | Bounty ends; choose one superior reward without charge | The Professional |
| Outlast the contract | Obtain the Amulet with all three crews defeated and the bounty unresolved | No settlement payout or extra mechanical perk | Wanted |

The professional outcome improves **choice and quality**, not the quantity of items handed out. No outcome adds permanent rarity, gold, room-generation, or character bonuses to later runs.

### Proposal for settlement choices

Seed the choices once and save them. Show the specific item and price before confirming a settlement. Never reopen the choice to reroll its contents.

- **Two fragments:** choose one of two offers at half its Cole premium price: a tier-4 weapon at +2 with a beneficial enchantment, or a ring at +2.
- **Three fragments:** choose one of three free offers: a tier-5 weapon at +3 with a beneficial enchantment, a ring at +3, or scale/plate armor at +3 with a beneficial glyph.
- **Cole defeated:** one seeded quality equipment reward and his finite remaining personal gold and unused consumables. No full exclusive-item set, second Warden's Coat, or repayment of old rewards.

These are initial reward proposals. Respect existing eligibility, item generation, and identification conventions. Do not apply unrequested extra Ring of Wealth or Doubloon multipliers to guaranteed quest payments or prizes; ordinary hunter drops retain their normal interactions.

**Pricing proposal:** use four times the equivalent ordinary shop price for settlement equipment, with the two-fragment discount reducing it to twice that baseline. Determine the baseline from the deepest main-dungeon region reached when the offer is first made, then save the quoted price. Later visits cannot raise the price or reroll the item.

### Proposal for Cole's combat

Cole is a mobile mini-boss, using a crossbow, close-range weapon, and finite equipment appropriate to the encounter's depth. The office meeting scales to the deepest main-dungeon region reached, so returning to floor 7 does not trivialize him. The Halls encounter uses the same encounter record and cannot grant a second kill reward.

Initial budget: health equal to roughly three ordinary regional melee mobs, regional elite damage, moderate additional evasion, and normal movement speed. He carries one healing potion, one smoke bomb, and one repositioning consumable, each usable once. He may call one ordinary melee hunter once per fight; this hunter does not grant another crew fragment. Escape or interrupted combat preserves damage, spent consumables, and the reinforcement flag.

The original proposal's 1.5 movement speed, multiple healing loops, extreme evasion, full exclusive-pool drop, and very large Halls health formula are not initial defaults.

### Badge behavior

Only one quest outcome badge is awarded per run. A completed settlement or Cole victory locks its outcome. Wanted applies at Amulet acquisition only while no other quest outcome has been awarded; later conversation cannot grant a second badge. Respect the game's existing exclusion of Playtest/custom-balance runs from eligible achievements.

Suggested badge flavor:

- Cole's Folly: "He came to collect. He miscalculated."
- The Negotiated Settlement: "The contract was called off. Professionally."
- The Professional: "Cole called it off himself. He said mostly."
- Wanted: "You outlasted the contract. Cole hasn't filed the paperwork yet."

## 12 Journal and visual presentation

Wanted posters use the existing high-resolution painting of their base creature with a unique alias, paper composition, rarity seal, and completion stamp. Each poster must match its actual target. The boss poster follows the actual boss, and the hero's wanted poster uses their own existing painting.

Cole needs distinct painted character art. Bloodmarked Brand and Warden's Coat each need a unique painted inventory icon and large examination image. The office, board, journal landmark, badge seals, and mark effect use the current painted visual language. Reuse existing Prison materials and frames where appropriate.

All new artwork must follow the established reproducible painted-asset workflow, with committed source paintings, prompts where applicable, offline packing, and validator coverage. No old pixel effect should be introduced for the brand or wanted indicators. Do not redesign unrelated creatures, terrain, or hero poses.

The existing examination artwork viewer must work on Cole, posters, the new items, and relevant journal entries. Descriptions and contract lists scroll correctly in portrait and landscape, with readable acceptance and reward controls on a tablet.

Keep prose thematic. Contract prices, named floors, and bonus deadlines provide information needed to accept the job; item flavor and NPC dialogue should avoid exhaustive combat formulas. Numerical detail remains in this specification and the Playtest controls. Localize all UI strings through the existing message system.

## 13 Persistence and compatibility

Keep one authoritative quest record per save. Persist the office and shop plan, each contract's identity and state, target identity and floor, chosen traits, spawn state, death completion, elapsed bonus time, reward ownership, payment receipts, boss identity, betrayal state, hunter crew members and fragments, Cole's location and encounter state, seeded settlement choices, and final outcome.

Persist Bloodmarked Brand through the ordinary buff mechanism. Persist the coat's intrinsic property through upgrading and class-armor conversion, and preserve first-hit consumption for individual enemies across their floor saves.

Repeated dialogue, stairs, room entry, reloads, and returning after a delayed spawn must not create another mob, payment, coat, fragment, shop restock, or settlement reward. Clearing one room must not mark a different contract complete. A full backpack leaves physical rewards in a safe recoverable heap under ordinary pickup conventions.

Older saves load with a safe absent-quest default. Do not insert an office into an already generated floor 7 or rebuild existing maps to force the quest into an old run. New runs are the reliable way to receive the complete quest. Missing saved fields must never cause a null-array or missing-plan crash.

Playtest travel and floor rebuilding do not reset normal quest receipts or recreate collected rewards. Fresh quest testing uses a new test save or a clearly isolated fixture.

## 14 Playtest controls and implementation components

### Proposal for Playtest organization

Add **Quests > Cole and Bounty Board** to the existing Playtest structure, preserving page position and the route back to the main menu. Group its tuning into shop/rewards, wanted creatures, hunter crews, Cole combat, Bloodmarked Brand, and Warden's Coat.

Expose prices, bounty payments, bonus duration and percentage, wanted health/damage/traits, crew health/damage, Cole's combat budget, Brand duration/accuracy/penetration, and coat armor-growth/first-hit scaling. Show defaults, ranges, and whether a control affects future generation or an existing encounter. Tuning persists across games on the device and marks affected runs according to existing custom-balance conventions.

Do not expose reward duplication, multiple Legendary contracts, or additional normal crews as ordinary tuning settings. Keep one-per-run rewards, payment receipts, maximum first-hit reduction of 100%, and map-reveal restrictions intact. Test tools may navigate to the office, inspect quest state, and open a seeded encounter without silently resetting progression.

### Proposed build order after design review

1. Quest state, safe save migration, office placement, and fixed five-slot shop.
2. Board, explicit acceptance, wanted variants, seeded posters, timing bonuses, and reward receipts.
3. Bloodmarked Brand and Warden's Coat, including class-armor conversion and painted item art.
4. Actual-boss contract, boss-choice consistency, payment, and betrayal.
5. Hunter crews, fragments, both Cole meeting routes, debt settlement, and encounter persistence.
6. Settlement rewards, Cole combat, mutually exclusive cosmetic badges, journal/artwork presentation, and Playtest organization.
7. Integrated desktop/Android checks, human review evidence, and release packaging.

Each component finishes in a building, committed state before the next starts. Check remaining usage before beginning each component and stop at a clean boundary if there is insufficient allowance. Report the boundary and unfinished work. No component begins as part of writing this draft.

## 15 Verification requirements

Extend the relevant existing gameplay, save, and native-interface checks during implementation. Do not create a parallel verification harness or reinterpret retired checks. Terrain contrast test 45 remains **Abandoned: test failed** and is not reopened by this quest.

Required coverage:

1. Every generated test run has a reachable floor-7 office and exactly five finite shop slots; existing Prison quests and exits remain usable.
2. Examining or buying does not accept a contract. Unaccepted targets never spawn. Accepted targets appear only on their saved named floor, including an already generated floor.
3. All three contracts can be accepted together and can share a floor. Target identity, traits, floor, and poster stay stable through reloads. Compatible wanted modifiers apply once.
4. Actual target death completes one contract through hero, ally, and environmental paths. Ordinary same-species mobs and summoned servants cannot impersonate the target.
5. Prompt bonuses start at the correct acceptance/entry boundary, count elapsed turns across floors, stop on death, survive reloads, and expire without failing the contract. Later payment preserves an earned bonus.
6. Returning any two Prison contracts unlocks the boss contract. Common plus Rare works without Legendary. Tengu and Chainwarden each get the correct poster and completion trigger, with their original progression rewards intact.
7. Betrayal occurs once after an accepted boss kill, pays the boss reward once, leaves stairs usable, closes new business, and preserves previously accepted targets and their prizes.
8. Bloodmarked Brand lasts eight turns, modifies hero melee/ranged accuracy and physical penetration correctly, works on bosses, refreshes without stacking, and reveals only its target. It cannot permit attacks through walls or amplify allied attacks.
9. Warden's Coat matches every row of its progression table. Test an actual +10 upgrade, temporary-level boosts, misses, fully armor-blocked hits, multi-hit attacks, direct spells, attached debuffs, environmental damage, and rounding. The first qualifying hit at +10 is zero damage and consumes that enemy's protection.
10. Coat consumption survives retreat, re-equipping, revisits, reloads, and class-armor conversion. Glyphs, inscriptions, Etching, upgrades, and normal armor restrictions remain functional. The Legendary prize stays recoverable after a fall and never duplicates.
11. Exactly three crews appear after betrayal, with the configured 2/3/3 composition, one fragment per defeated crew, persistent members/consumables, and no branch-floor or duplicate spawns. Resolution ends pending and active pursuit.
12. Both Cole meeting routes pay earned outstanding debts before any fight or settlement, with no repeated payout or recovery of previously spent shop gold. Verify the reviewed policy for unfinished contracts if Cole dies.
13. One fragment does not automatically attack. Two- and three-fragment settlements offer their saved choices and end the bounty; Cole combat persists across interruptions and grants one finite reward.
14. Each of the four outcomes awards at most one cosmetic badge, with no future-run combat or generation bonus and normal Playtest eligibility rules.
15. Older saves lacking quest data load safely. Floor travel, dialogue re-entry, full inventories, and deliberate reloads cannot lose mandatory progress or recreate paid prizes.
16. All nine greetings and betrayals display correctly. Posters, art enlargement, journal entries, long text, and shop/contract controls fit native tablet portrait and desktop landscape layouts.
17. Reconstruct and validate committed new art offline; build Windows desktop and Android through the existing workflow. Keep all applicable CI checks intact and report actual results before releasing an implemented quest.

## 16 Remaining review items

The agreed item names, coat progression, Brand effects, quest structure, explicit acceptance, optional Legendary fight, betrayal, debt settlement, and cosmetic-only permanent rewards are fixed by the discussion.

Review the proposed stock prices and payouts, 500-turn bonus values, exact wanted pools and traits, crew floors and combat budgets, Cole's combat and settlement rewards, journal-only poster storage, accessible office doorway, and unfinished-contract payment on Cole's death. These proposals make the draft concrete without treating untested numbers or newly resolved edge cases as approved decisions.
