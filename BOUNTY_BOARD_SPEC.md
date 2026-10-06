# Grimhollow Bounty Board Quest

Version 0.3 | 6 October 2026 | Implementation authorized

Cole, a bounty hunter operating from an abandoned Prison office, offers three dangerous contracts and a small premium shop. Completing two contracts unlocks a bounty on the actual Prison boss. Cole pays that bounty, then sells out the hero. Hunter crews pursue the hero through the Caves, City, and Halls, until the hero negotiates a settlement, confronts Cole, or outlasts the contract.

This specification records the agreed quest and item rules. Implementation was authorized on 6 October 2026; sections marked **Proposal** supply the initial tuning defaults. The earlier pasted Bounty Board proposal is superseded wherever it conflicts with this specification.

## 1 Scope

Add Cole, his office and shop, three Prison contracts, a Prison boss contract, wanted posters and physical Warrants, three later hunter crews, the resolution encounters, four cosmetic badges, Bloodmarked Brand, and Warden's Coat. Warrants are named quest claims, not a new spendable currency.

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

The weapon is useful Prison equipment, with better quality than an ordinary random find. Cole charges **exactly twice the equivalent standard shop price** for every sale slot. Shopping competes with purchases elsewhere and does not become a cheap upgrade source. Save the quoted prices when the stock is created; revisiting cannot change them.

### Agreed prices and proposed initial stock

- Food: a ration, priced at twice the equivalent ordinary Prison shop price.
- Consumables: choose two distinct useful existing potions, scrolls, or their established crafted variants; price each at twice its equivalent Prison shop price. Exclude guaranteed progression items such as Scrolls of Upgrade and Potions of Strength from this initial stock pool.
- Weapon: a tier-3 weapon at +1 or +2, uncursed, with a beneficial enchantment; price at twice its equivalent ordinary Prison shop price. Use existing weapon categories, including ranged options.
- Specialty: two Bloodmarked Brands, priced through the same double-price rule. **Valuation proposal:** each Brand has an ordinary item value of 15 gold, making the pair 300 gold at a standard Prison shop and 600 gold at Cole's.

Use the game's normal shop valuation as the baseline, including quantity, known upgrades, and other normal valuation modifiers. Round displayed prices consistently. The double-price rule is agreed; stock quality and the new Brand's intrinsic value remain proposals.

### Prison economy reference and proposed payouts

The current standard shop price is `item.value() * 5 * (depth / 5 + 1)`, using integer division. At global floor 7 this is ten times item value, so Cole's double price is twenty times value.

| Item | Standard Prison shop | Cole |
|---|---|---|
| One ration, ordinary value 10 | 100 gold | 200 gold |
| One known Potion of Healing, value 30 | 300 gold | 600 gold |
| One known Potion of Haste, value 40 | 400 gold | 800 gold |

The former 300/650-gold Common/Rare proposals cover one and roughly two ordinary Healing potions, but only half and just over one at Cole. They are useful payments, not a premium-equipment budget.

**Revised payout proposal:** Common 600 gold, Rare 1,200 gold, and the Prison boss 1,500 gold. The Common/Rare prompt bonuses would raise these to 720 and 1,440. Legendary still awards the coat instead of cash. Before the boss, the two cash contracts fund 1,800 gold, or 2,160 with both bonuses; the boss reward arrives after Cole's shop closes, making it useful in later regions.

Known, beneficially enchanted tier-3 weapons at +1/+2 cost substantially more than basic consumables under ordinary valuation. The revised payments support a few purchases or supplement the hero's savings, without buying Cole's entire stock.

### Failed theft

The Thieves' Armband retains its normal success probability, charge use, and successful item transfer. A failed theft attempt at Cole's stock permanently closes **his retail shop for this run**, including all remaining sale slots. Cole stays in the office, keeps the board available, honors earned payments, and does not attack or disappear before the betrayal.

Cole's response:

> "Keep those hands where I can see them. The next poster could be yours. Shop's closed."

This is a threat and narrative foreshadowing, not an early hunter-crew trigger. Ordinary contract acceptance and the boss-driven betrayal continue normally. The theft confirmation must describe Cole's actual consequence rather than promise that an ordinary shopkeeper will flee.

Route a failed attempt to the vendor owning that stock. Do not make another shopkeeper on the floor flee, remove unrelated merchandise, confiscate belongings, or silently reroll Cole's stock. Successful theft does not close the shop. Save the closed-shop flag through reloads and revisits.

## 4 The four contracts

Cole's board offers **one Common, one Rare, and one Legendary Prison contract**. The fourth contract targets the actual Prison boss and unlocks after **any two of the three Prison contracts have been completed and returned to Cole**. Legendary is optional for the normal Common-plus-Rare route.

All three Prison contracts may be accepted during the same visit. There is no limit of one active target at a time. Each target's floor is chosen independently from Prison floors 2, 3, and 4, corresponding to global floors 7, 8, and 9. Multiple contracts can name the same floor, so accepting all three can create overlapping encounters.

Before acceptance, each poster shows:

- A unique contract title, a fixed target alias, and the correct existing creature painting.
- Its Common, Rare, or Legendary seal.
- A brief atmospheric description of the wanted creature's distinguishing strengths.
- **"Last seen on Prison floor X."**
- The offered base payment or carried prize. Common and Rare carry an **URGENT BOUNTY** stamp and a brief hint that swift work earns a fuller purse; do not print a numerical bonus or deadline.
- A distinct **Accept contract** control.

Targets, modifiers, rewards, and named floors are seeded before acceptance and cannot be rerolled by reopening the board, changing floors, or loading a save. Rarity reflects the difficulty of the base mob and its particular modifier combination, rather than simply counting buffs.

No wanted target exists before its contract is accepted. On acceptance, its spawn becomes pending. If the named floor is currently loaded, place it at a valid unoccupied location outside immediate hero sight and outside Cole's office. Otherwise, spawn it on the next entry to that floor. Already generated floors support this pending spawn without rebuilding the map.

Spawn placement must use reachable ordinary terrain, preserve entrances and exits, and respect mob size. If a valid location is temporarily unavailable, retain the pending spawn and retry when placement becomes possible. Never silently drop the contract, move its target to another floor, or put it on the hero's cell.

Wanted targets use their ordinary mob behavior and the same global tuning conventions as other mobs. They receive only their explicit wanted enhancements. Do not add a second random curse-bound or empowered-variant roll, and do not attach a self-weakening NecroCurse merely to identify the variant.

An accepted target's death completes its contract once, including deaths caused by allies, traps, falls, or other ordinary combat interactions. Completion does not depend on delivering the final blow personally. Summoned creatures and unrelated mobs of the same species do not count.

### Proposal for initial target pools

| Contract | Base mob candidates | Initial enhancements | Reward |
|---|---|---|---|
| Common | Skeleton or Thief | 1.25 times base health; 1.10 times damage | 600 gold |
| Rare | Guard, DM-100, or Necromancer | 1.60 times base health; 1.15 times damage; a compatible defense or speed trait | 1,200 gold |
| Legendary | Guard, presented as a former warden | 2.20 times base health; 1.25 times damage; two compatible wanted traits | Warden's Coat at +2 |

Suggested traits are **Ironhide**, adding 1 to both ends of the ordinary physical protection range, and **Quickstep**, multiplying ordinary movement speed by 1.10 without accelerating attack or spell cadence. These are proposed traits. Score each base-mob and trait combination for its actual difficulty before including it in a rarity pool. Retain normal status-effect vulnerabilities.

Wanted mobs remain non-boss enemies. They do not acquire blanket immunity to charms, knockback, inscriptions, or other ordinary tools. Their explicit reward is finite; do not also grant the unrelated curse-bound bonus-loot roll.

### Target identity and flavor

Wanted creatures retain their existing species painting and silhouette. While normally visible, show a painted crimson wanted seal beside the health bar, with bronze, silver, or gold detail for Common, Rare, or Legendary, and a restrained bronze edge accent. The alias appears in targeting and examination, with the base species available in the description. These indicators confer no debuff and never reveal an unseen creature or room.

Initial title and alias pairs:

| Base creature | Contract title | Target alias | Poster flavor |
|---|---|---|---|
| Skeleton | Jack's Second Sentence | Jack Twice-Hanged | "This used to be Jack. His first execution wasn't enough. Make the second stick." |
| Thief | The Missing Payroll | Nails the Cutpurse | "Took the payroll. Left the guards to take the blame." |
| Guard | The Keeper of Keys | Voss the Cellbreaker | "Sold keys to both sides. Neither got out alive." |
| DM-100 | A Machine Without Orders | The Widowmaker | "The last keeper disconnected it. It killed the next three." |
| Necromancer | An Unclosed Ledger | The Bone Clerk | "The dead on his ledger keep coming back to work." |
| Legendary Guard | The Last Warden | Warden Morcant | "Kept the keys. Kept the coat. Never opened the cells." |

These names and lines are agreed for the initial roster. Fix the chosen pair with its contract seed, avoid duplicate aliases within the run, and never rename ordinary members of the base species. The actual Prison boss retains its own identity, with an appropriate unique contract title. The separate target-pool and combat-strength proposals remain subject to review.

### Warrants and quest records

Accepting a bounty grants the hero a **Warrant**, a protected physical quest item naming the actual target and recording Cole's promised payment or prize. While Cole lives, it is an IOU eligible for payment only after that target dies. Completion remains tied to the accepted contract record, not to holding the paper at the moment of death. Returning the completed claim to Cole pays and consumes it once. Cole's death settles any outstanding accepted cash claims through the rule in section 10, including unfinished targets. The journal mirrors its poster, last-seen floor, urgency, and claim state.

Legendary's Warrant records the carried coat prize and the target's death. Returning it proves completion for boss-contract eligibility; it cannot create a second coat or an extra cash reward. The boss Warrant is settled once during the betrayal exchange.

Each defeated hunter crew drops **one Warrant naming the hero**, replacing poster fragments. Its face value is the hero bounty issued at betrayal. These captured Warrants prove how many separate crews were sent against the hero and provide leverage for settlement. The living hero cannot cash an IOU payable on their own death, and the printed bounty does not become a guaranteed gold drop from every crew.

**Stacking rules:** merge Warrants only when issuer, named target, reward terms, and claim state match. Different accepted bounties remain separate stacks; the three hunter Warrants share the hero's fixed bounty terms and stack together. Preserve individual contract or crew IDs within merged stacks, so quantity never invents extra claims or loses their identity. Splitting and merging cannot duplicate a receipt or count one crew twice.

Warrants cannot be sold, fed to the Hatchling, recycled, stolen, or destroyed as ordinary paper. If a backpack is full, place an issued Warrant in a safe recoverable heap while preserving the accepted quest record. They have no ordinary sale value. Keep the authoritative quest state independent of its presentation, including after a paid claim is removed.

Posters remain individually examinable board and journal cards. Warrants are their physical inventory claims. The journal mirrors accepted contracts, collected hunter Warrants, and payment history, while each claim's unique record remains authoritative. A target's death can complete the accepted contract while its Warrant is elsewhere; the physical claim remains recoverable and cannot be issued twice.

## 5 Prompt completion bonuses

Common and Rare contracts offer an optional bonus for prompt completion. Missing the deadline does not fail the contract, remove its target, or reduce the base payment. Legendary and the Prison boss contract have no timer.

**Initial tuning proposal:** 500 elapsed game turns, with a 20% additional gold payment for an eligible kill before the deadline. At the revised proposed payments, this is 120 extra gold for Common and 240 for Rare.

The clock starts once the contract is accepted and the hero first enters its named floor. Acceptance while on that floor starts it immediately. Once started, elapsed game time on other floors also counts. Haste and other timing effects follow the game's normal elapsed-turn model; this is not a count of mouse clicks or player actions.

Stop the clock when the actual target dies. Returning to Cole later cannot remove an earned bonus. Menus, examination, real-world time, saving, and time while the game is closed do not count. Persist elapsed time across floor transitions and reloads.

The normal poster and Warrant carry an **URGENT BOUNTY** stamp and the line **"A fuller purse for a swift hand."** This hints at the reward without disclosing the percentage or countdown. Do not display turns remaining or a numerical deadline in ordinary journal views. Internal state still distinguishes an unstarted clock, active eligibility, earned bonus, and expired bonus for saving and Playtest inspection. An expired bonus never produces a mission-failed warning; Cole may acknowledge prompt work when paying it.

## 6 The Prison boss and betrayal

The boss contract follows the **actual boss assigned to global floor 10**, including Tengu or Chainwarden. Its portrait, name, text, completion condition, and dialogue all match that boss. Use one saved boss choice shared by the poster and level generator; do not make a second independent roll or change the configured boss-selection probability.

**Proposal:** the boss payment is 1,500 gold. The contract must be explicitly accepted before the boss dies. A boss defeated without an accepted contract cannot be claimed retroactively and does not start the betrayal arc. Previously accepted ordinary contracts remain completable.

After the accepted boss contract completes, Cole meets the hero at the departure area. He pays the boss Warrant **once**, then reveals a wanted poster for the hero. The exchange stops automatic movement while its dialogue is open. When the final dialogue closes, **Cole departs and is removed from the floor**; he does not stand around beside the exit. The stairs remain usable, and this encounter does not initiate combat or demand the boss progression item. His departure is not a death and grants no kill, loot, or badge.

The hero's poster uses their existing character painting, name, and class title. **Proposal:** its displayed bounty is a snapshot of held gold plus the ordinary value of owned items at betrayal, traversing equipped gear and all bags without counting any item twice. The amount is narrative information, not a new damage or loot multiplier. It remains fixed afterward.

Cole closes his shop and stops offering new contracts. Contracts accepted before betrayal stay valid, including a Legendary target fought after the boss. The coat remains the Legendary target's carried prize and does not depend on Cole surviving or reopening his business.

Cole's office is empty afterward. He appears again only for a later arranged meeting or the Halls encounter. The board retains the hero's poster and a note:

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

Hunters do not steal belongings. Ordinary monster and Lurking Horror interactions continue under their existing rules; neither coordinates specially with a crew. Defeating all members of a crew yields exactly one protected **hunter Warrant naming the hero**. Controlled, displaced, or temporarily allied surviving hunters still count as alive. A crew's Warrant is proof of that crew's defeat, not a second cash bounty on the crew.

Pending crews can appear on an already generated floor when the hero next visits it after betrayal. Spawn outside immediate sight, preserve safe arrival cells, and never duplicate a crew after reload or re-entry.

## 10 Cole's return and outstanding debts

The hero can arrange a later meeting in **Cole's old office** using the board, or meet him in a dedicated **Halls room** while the bounty remains unresolved. Both routes are available. **Proposal:** the Halls room appears on global floor 22, away from the required route and hunter arrival positions.

Summoning Cole at the office is a discrete meeting interaction. He does not appear merely because the hero passes the room. The Halls encounter is also optional; it cannot block stairs or the route to the final boss.

**Arriving in the Halls without hunter Warrants:** Cole has come to collect the hero's bounty personally. Zero Warrants provides no peaceful settlement option. After settling any earned outstanding Prison claims, he gives a clear hostile opening line and the encounter becomes combat. Closing the dialogue gives the hero control before Cole's first attack. The hero may retreat or avoid his room entirely; defeating crews is not mandatory for reaching the Amulet or escaping.

Cole's line:

> "Still breathing. Good. I prefer to collect my own work."

One captured hunter Warrant permits deliberate confrontation at the office but does not qualify for settlement. Two or three unlock their respective negotiated outcomes. Completed, unpaid Prison Warrants permit a debt meeting without pretending that the hero has defeated a hunter crew.

At either meeting, Cole first pays all **earned, unpaid** Common and Rare rewards, including earned timing bonuses. This happens before the player chooses settlement or combat:

> "Here. I owe you this. Now we're even."

Keep an explicit payment record. Do not put the debt in a payment chest or escrow box, refund earlier purchases, or drop previously paid bounty money again when Cole dies. Accepting, killing, returning, claiming, and paying are separate states.

### Cash claims on Cole's death

When Cole dies, he drops **all outstanding cash owed for accepted contracts**, including the base payments of Common/Rare bounties whose targets have not yet died. Include an urgency bonus only if the target's death already earned it. Already paid claims contribute nothing. Unaccepted offers, the Legendary's coat prize, and hunter Warrants naming the hero contribute no cash to this settlement.

This is an explicit exception to the usual target-death requirement for payment: Cole's death releases the remaining accepted cash claims. It does not kill their targets or mark unfinished contracts complete. The targets remain available to fight, and the Legendary's carried coat remains recoverable under its existing rule.

Resolve each eligible claim once and place its gold in a safe recoverable heap with Cole's death loot. Record that claim's payment as settled through Cole's death, and retire its cash Warrant so it cannot be redeemed again. Preserve the underlying contract and target identity for any unfinished encounter. A later target death may complete that encounter but grants no second payment or new urgency bonus.

This claim gold is separate from Cole's ordinary finite personal loot. Never calculate it by returning previously paid bounty money or prior shop purchases. Saving before or after the death, re-entering the room, and splitting/merging Warrants cannot create another payment. There is no forfeiture warning or posthumous payment NPC.

## 11 Resolution and rewards

Presenting captured hunter Warrants starts a conversation, not an automatic attack at the office. The player deliberately chooses confrontation or an available peaceful settlement there; the zero-Warrant Halls encounter follows its hostile rule above. Settling with two hunter Warrants ends the pursuit and prevents obtaining a third crew Warrant afterward.

| Outcome | Requirement | Current-run result | Cosmetic badge |
|---|---|---|---|
| Confrontation | At least one hunter Warrant at the office, or the optional Halls confrontation even with none | Defeat Cole; bounty ends | Cole's Folly |
| Negotiated settlement | Two distinct captured hunter Warrants | Bounty ends; choose one discounted quality reward | The Negotiated Settlement |
| Professional settlement | All three captured hunter Warrants | Bounty ends; choose one superior reward without charge | The Professional |
| Outlast the contract | Return out of the dungeon with the Amulet and the bounty unresolved, regardless of crews defeated or Warrants collected | No settlement payout or extra mechanical perk | Wanted |

The professional outcome improves **choice and quality**, not the quantity of items handed out. No outcome adds permanent rarity, gold, room-generation, or character bonuses to later runs.

### Proposal for settlement choices

Seed the choices once and save them. Show the specific item and price before confirming a settlement. Never reopen the choice to reroll its contents.

- **Two hunter Warrants:** choose one of two offers at half Cole's double-standard price: a tier-4 weapon at +2 with a beneficial enchantment, or a ring at +2.
- **Three hunter Warrants:** choose one of three free offers: a tier-5 weapon at +3 with a beneficial enchantment, a ring at +3, or scale/plate armor at +3 with a beneficial glyph.
- **Cole defeated:** one seeded quality equipment reward, his finite remaining personal gold and unused consumables, and the outstanding accepted cash claims defined in section 10. No full exclusive-item set, second Warden's Coat, or repayment of previously paid rewards.

These are initial reward proposals. Respect existing eligibility, item generation, and identification conventions. Do not apply unrequested extra Ring of Wealth or Doubloon multipliers to guaranteed quest payments or prizes; ordinary hunter drops retain their normal interactions.

**Settlement pricing proposal:** apply the agreed double-standard price, then the existing half-price settlement offer. The two-Warrant outcome therefore costs the equivalent standard shop price. Determine that baseline from the deepest main-dungeon region reached when the offer is first made, then save the quoted price. Later visits cannot raise the price or reroll the item.

### Proposal for Cole's combat

Cole is a mobile mini-boss, using a crossbow, close-range weapon, and finite equipment appropriate to the encounter's depth. The office meeting scales to the deepest main-dungeon region reached, so returning to floor 7 does not trivialize him. The Halls encounter uses the same encounter record and cannot grant a second kill reward.

Initial budget: health equal to roughly three ordinary regional melee mobs, regional elite damage, moderate additional evasion, and normal movement speed. He carries one healing potion, one smoke bomb, and one repositioning consumable, each usable once. He also carries **one finite stack of the existing Bolas weapon** to cripple the hero, create distance for crossbow attacks, or let his reinforcement close in. **Initial ammunition proposal:** two unupgraded Bolas. Use normal hit, projectile collision, and Cripple rules; do not make them unavoidable or replace their existing behavior. Each throw consumes one projectile from his carried stack, and any unused ammunition follows normal remaining-loot handling.

Cole may call one ordinary melee hunter once per fight; this hunter does not grant another hunter Warrant. Escape or interrupted combat preserves damage, spent consumables, remaining Bolas, and the reinforcement flag. Reloading cannot replenish his ammunition.

The original proposal's 1.5 movement speed, multiple healing loops, extreme evasion, full exclusive-pool drop, and very large Halls health formula are not initial defaults.

### Badge behavior

Only one quest outcome badge is awarded per run. A completed settlement or Cole victory locks its outcome. **Wanted applies on successful return from the dungeon with the Amulet while Cole's bounty is still unresolved.** It does not require defeating a crew, collecting a hunter Warrant, or confronting Cole. Avoiding all three crews and Cole qualifies. Merely picking up the Amulet does not award it under this revised condition. Respect the game's existing exclusion of Playtest/custom-balance runs from eligible achievements.

Suggested badge flavor:

- Cole's Folly: "He came to collect. He miscalculated."
- The Negotiated Settlement: "The contract was called off. Professionally."
- The Professional: "Cole called it off himself. He said mostly."
- Wanted: "You outlasted the contract. Cole hasn't filed the paperwork yet."

## 12 Journal and visual presentation

Wanted posters use the existing high-resolution painting of their base creature with a unique contract title, alias, paper composition, rarity seal, and completion stamp. Each poster must match its actual target. Common/Rare have the painted urgency stamp and implicit bonus hint. The boss poster follows the actual boss, and the hero's wanted poster uses their own existing painting. The inventory Warrant has its own painted paper-and-seal icon and opens the relevant full contract artwork.

Cole needs distinct painted character art. Bloodmarked Brand and Warden's Coat each need a unique painted inventory icon and large examination image. The office, board, journal landmark, badge seals, and mark effect use the current painted visual language. Reuse existing Prison materials and frames where appropriate.

All new artwork must follow the established reproducible painted-asset workflow, with committed source paintings, prompts where applicable, offline packing, and validator coverage. No old pixel effect should be introduced for the brand or wanted indicators. Do not redesign unrelated creatures, terrain, or hero poses.

The existing examination artwork viewer must work on Cole, posters, the new items, and relevant journal entries. Descriptions and contract lists scroll correctly in portrait and landscape, with readable acceptance and reward controls on a tablet.

Keep prose thematic. Base payments and named floors provide information needed to accept the job. Urgency hints at a bonus without exposing its percentage or deadline; item flavor and NPC dialogue should avoid exhaustive combat formulas. Numerical bonus detail remains in this specification and the Playtest controls. Localize all UI strings through the existing message system.

## 13 Persistence and compatibility

Keep one authoritative quest record per save. Persist the office and shop plan, fixed prices, failed-theft closure, each contract's identity and state, title and alias, target identity and floor, chosen traits, spawn state, death completion, elapsed bonus time, Warrant issuance and per-claim or per-crew IDs in stacks, reward ownership, payment receipts including settlement through Cole's death, the created claim-gold heap, boss identity, betrayal and departure state, hunter crew members and captured Warrants, Cole's location and encounter state including ammunition, seeded settlement choices, and final outcome.

Persist Bloodmarked Brand through the ordinary buff mechanism. Persist the coat's intrinsic property through upgrading and class-armor conversion, and preserve first-hit consumption for individual enemies across their floor saves.

Repeated dialogue, stairs, room entry, reloads, Warrant splitting/merging, and returning after a delayed spawn must not create another mob, payment, coat, Warrant claim, shop restock, or settlement reward. Clearing one room must not mark a different contract complete. A full backpack leaves physical Warrants and rewards in a safe recoverable heap under ordinary pickup conventions.

Older saves load with a safe absent-quest default. Do not insert an office into an already generated floor 7 or rebuild existing maps to force the quest into an old run. New runs are the reliable way to receive the complete quest. Missing saved fields must never cause a null-array or missing-plan crash.

Playtest travel and floor rebuilding do not reset normal quest receipts or recreate collected rewards. Fresh quest testing uses a new test save or a clearly isolated fixture.

## 14 Playtest controls and implementation components

### Proposal for Playtest organization

Add **Quests > Cole and Bounty Board** to the existing Playtest structure, preserving page position and the route back to the main menu. Group its tuning into shop/rewards, wanted creatures, hunter crews, Cole combat, Bloodmarked Brand, and Warden's Coat.

Expose prices, bounty payments, bonus duration and percentage, wanted health/damage/traits, crew health/damage, Cole's combat budget including Bolas count, Brand duration/accuracy/penetration, and coat armor-growth/first-hit scaling. Show defaults, ranges, and whether a control affects future generation or an existing encounter. Tuning persists across games on the device and marks affected runs according to existing custom-balance conventions.

Do not expose reward duplication, multiple Legendary contracts, or additional normal crews as ordinary tuning settings. Keep one-per-run rewards, payment receipts, maximum first-hit reduction of 100%, and map-reveal restrictions intact. Test tools may navigate to the office, inspect quest state, and open a seeded encounter without silently resetting progression.

### Proposed build order after design review

1. Quest state, safe save migration, office placement, and fixed five-slot shop with double-standard pricing and the agreed failed-theft response.
2. Board, explicit acceptance, wanted variants, seeded named posters, physical Warrants and stacking, implicit urgency, timing bonuses, and reward receipts.
3. Bloodmarked Brand and Warden's Coat, including class-armor conversion and painted item art.
4. Actual-boss contract, boss-choice consistency, payment, betrayal, and Cole's departure.
5. Hunter crews, captured Warrants, both Cole meeting routes including a zero-Warrant Halls encounter, debt settlement, and encounter persistence.
6. Settlement rewards, Cole combat, mutually exclusive cosmetic badges, journal/artwork presentation, and Playtest organization.
7. Integrated desktop/Android checks, human review evidence, and release packaging.

Each component finishes in a building, committed state before the next starts. Check remaining usage before beginning each component and stop at a clean boundary if there is insufficient allowance. Report the boundary and unfinished work. No component begins as part of writing this draft.

## 15 Verification requirements

Extend the relevant existing gameplay, save, and native-interface checks during implementation. Do not create a parallel verification harness or reinterpret retired checks. Terrain contrast test 45 remains **Abandoned: test failed** and is not reopened by this quest.

Required coverage:

1. Every generated test run has a reachable floor-7 office and exactly five finite shop slots, each quoted at twice the standard price; existing Prison quests and exits remain usable. Verify the failed-theft response, successful theft, correct vendor isolation, closed-stock persistence, and continued board/payment access.
2. Examining or buying does not accept a contract. Unaccepted targets never spawn. Accepted targets appear only on their saved named floor, including an already generated floor.
3. All three contracts can be accepted together and can share a floor. Target identity, traits, floor, contract title, alias, and poster stay stable through reloads. Compatible wanted modifiers apply once. Visible wanted seals distinguish targets without exposing them through fog.
4. Actual target death completes one contract through hero, ally, and environmental paths. Ordinary same-species mobs and summoned servants cannot impersonate the target.
5. Prompt bonuses start at the correct acceptance/entry boundary, count elapsed turns across floors, stop on death, survive reloads, and expire without failing the contract. Later payment preserves an earned bonus. Normal posters and Warrants show implicit urgency without numerical bonus amounts or countdowns; Playtest retains the actual values.
6. Returning any two Prison contracts unlocks the boss contract. Common plus Rare works without Legendary. Tengu and Chainwarden each get the correct poster and completion trigger, with their original progression rewards intact.
7. Betrayal occurs once after an accepted boss kill, pays the boss Warrant once, leaves stairs usable, closes new business, and preserves previously accepted targets and their prizes. Cole departs when the dialogue closes, with no death rewards or lingering actor at the exit.
8. Bloodmarked Brand lasts eight turns, modifies hero melee/ranged accuracy and physical penetration correctly, works on bosses, refreshes without stacking, and reveals only its target. It cannot permit attacks through walls or amplify allied attacks.
9. Warden's Coat matches every row of its progression table. Test an actual +10 upgrade, temporary-level boosts, misses, fully armor-blocked hits, multi-hit attacks, direct spells, attached debuffs, environmental damage, and rounding. The first qualifying hit at +10 is zero damage and consumes that enemy's protection.
10. Coat consumption survives retreat, re-equipping, revisits, reloads, and class-armor conversion. Glyphs, inscriptions, Etching, upgrades, and normal armor restrictions remain functional. The Legendary prize stays recoverable after a fall and never duplicates.
11. Exactly three crews appear after betrayal, with the configured 2/3/3 composition, one hunter Warrant per defeated crew, persistent members/consumables, and no branch-floor or duplicate spawns. Resolution ends pending and active pursuit. Acceptance issues one correct Warrant; target death makes it payable while Cole lives; payment consumes its claim once. Compatible stacks preserve IDs, incompatible claims remain separate, and split/merge cannot manufacture gold or crew progress. Captured hero Warrants are leverage, not a cash payout to the living target.
12. Both Cole meeting routes pay earned outstanding debts before any fight or settlement, with no repeated payout or recovery of previously spent shop gold. Cole's death drops all accepted unpaid cash claims, including unfinished targets' base payments and only earned urgency bonuses. Unaccepted offers and hunter Warrants add no cash. Claims settle once, unfinished targets remain incomplete and available, and a later target death or reload grants no additional money or bonus. The cash stays recoverable if inventory is full or the corpse cell is unsuitable.
13. One hunter Warrant does not automatically attack at the office. Two- and three-Warrant settlements offer their saved choices and end the bounty. A zero-Warrant Halls encounter follows the agreed hostile-opening behavior, with debt settlement and a player response before combat. Cole's damage, consumables, reinforcement, and finite Bolas persist across interruptions, with normal dodge/collision/Cripple behavior and one finite kill reward.
14. Each of the four outcomes awards at most one cosmetic badge, with no future-run combat or generation bonus and normal Playtest eligibility rules. Wanted succeeds on escaping with the Amulet and an unresolved bounty, including zero crews defeated and zero hunter Warrants; it does not trigger on pickup alone or after a settlement/Cole kill.
15. Older saves lacking quest data load safely. Floor travel, dialogue re-entry, full inventories, and deliberate reloads cannot lose mandatory progress or recreate paid prizes.
16. All nine greetings and betrayals display correctly. Posters, art enlargement, journal entries, long text, and shop/contract controls fit native tablet portrait and desktop landscape layouts.
17. Reconstruct and validate committed new art offline; build Windows desktop and Android through the existing workflow. Keep all applicable CI checks intact and report actual results before releasing an implemented quest.

## 16 Remaining review items

The agreed item names, coat progression, Brand effects, quest structure, explicit acceptance, optional Legendary fight, double-standard shop prices, failed-theft response, implicit urgency, target identities and wanted seals, physical Warrants and stacking, Cole's departure and zero-Warrant Halls response, expanded Wanted eligibility, debt settlement including unfinished cash claims on Cole's death, finite Bolas in Cole's loadout, and cosmetic-only permanent rewards are fixed by the discussion.

Review the revised proposed payouts, intrinsic Brand value, stock quality, 500-turn bonus values, exact wanted pools and combat traits, crew floors and combat budgets, Cole's Bolas quantity and other combat budget, settlement rewards, and accessible office doorway. These proposals make the draft concrete without treating untested numbers as approved decisions. Greeting and betrayal wording remain draft dialogue; the approved urgency, theft, target-flavor, and Halls lines are established above.
