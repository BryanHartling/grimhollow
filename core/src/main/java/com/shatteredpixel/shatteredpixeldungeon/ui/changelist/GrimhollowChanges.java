// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import java.util.ArrayList;

/** Player-facing release history, curated from CHANGES.md, README.md and verified deliveries.
 * Keep newest releases first; describe shipped behavior, not superseded plans or build stages. */
public final class GrimhollowChanges {
    private GrimhollowChanges() {}

    private static ChangeInfo release(ArrayList<ChangeInfo> list, String version, String summary) {
        ChangeInfo info=new ChangeInfo(version,true,summary);
        info.hardlight(Window.TITLE_COLOR);
        list.add(info);
        return info;
    }
    private static void note(ChangeInfo release, Icons icon, String title, String text) {
        release.addButton(new ChangeButton(icon.get(),title,text));
    }
    public static void addAllChanges(ArrayList<ChangeInfo> list) {
        ChangeInfo r=release(list,"v1.29.15","A stronger legion. A hunter's payment, then a new face on the board.");
        note(r,Icons.TALENT,"Bone Legion","Deathspeakers can invest three ranks. Newly raised servants remain bound for 5/10/15 additional turns; rank three also permits one more servant. Existing ranks and servants' remaining time are preserved.");
        note(r,Icons.JOURNAL,"Cole's handover","Speak to Cole at the Prison stairs and accept payment before his separate betrayal conversation. He hands over your own Wanted Poster, kept in inventory and in your run's Deeds. Rereading it cannot pay a bounty or alter the quest.");
        note(r,Icons.INFO,"Wanted quarries and the Horror","Unfinished bounties recover missing target references, preserving living quarries and starting urgency only after placement. Horrors have stronger regional combat baselines and can defend themselves against dungeon denizens when cornered. Their warning and escape rules remain.");
        r=release(list,"v1.29.14","Whispers against hunger. Clearer knowledge, room for discovery.");
        note(r,Icons.TALENT,"Soul Sustenance","The Necromancer's former Soul Siphon becomes Soul Sustenance. Kills made while the Phylactery is already full can quiet hunger, including kills by raised servants. Two ranks restore 10/20 hunger turns, up to 60/120 per floor. Summoned and rewardless victims do not qualify. This replaces health drain without healing or triggering eating talents; invested ranks are retained.");
        note(r,Icons.JOURNAL,"Knowledge and mystery","Added talents, spells and equipment again describe their useful effects, costs and progression alongside their stories. Quest clues and dangers remain readable; hidden bargains and exceptional interactions remain yours to discover.");
        r=release(list,"v1.29.13","Danger earns its treasure.");
        note(r,Icons.WARNING,"Dragon expedition","The dragon's blows are heavier, and its wings threaten nearby prey more reliably. The warning remains your chance to move. Playtest's expedition supplies include clearly labeled guaranteed cavern rations; existing caverns keep their finite supplies.");
        note(r,Icons.INFO,"The Imp's bargain","Entering and leaving the Vault no longer restores your health or fills your stomach. Damage inflicted on its guardian earns partial credit; merely awakening it does not.");
        r=release(list,"v1.29.12","Vault travel and room furnishings.");
        note(r,Icons.INFO,"The Imp's Vault","Playtest now enters the single Vault attached to this run's Imp entrance. Revisiting a completed Vault no longer ejects you on your first step. The Escape Crystal returns to the correct entrance, including older saves.");
        note(r,Icons.JOURNAL,"Carpets and wall hangings","Cole's office rug remains whole beneath the furnishings. City and Vault wall hangings stay on plain wall faces and leave torches and floor paving clear.");
        r=release(list,"v1.29.11","The cavern does not sleep.");
        note(r,Icons.WARNING,"Cavern scavengers","Spiders survive the broodmother's death. More scavengers can arrive from the darkness, including a new broodmother when none remains. The first victory keeps the climb open; returning creatures carry no renewed rewards. Playtest balance settings include the cavern's replenishment interval.");
        r=release(list,"v1.29.10","Dark veins in the rock.");
        note(r,Icons.INFO,"Mine ore","Dark ore is visible on side-facing rock and wall rims, using the mine's existing painted mineral. Crystal treasure pockets place their deposits on separate cells. Existing mines keep their terrain and collected ore.");
        r=release(list,"v1.29.9","Clearer motion and dependable telekinesis.");
        note(r,Icons.TALENT,"Hurl and stealth","A practiced Seer's Hurl springs traps along its path as well as on landing, while retaining its ledge and impact effects. Chainwarden cannot pull a hero concealed by the Cloak or another invisibility effect. Rogue's Foresight has its own eye motif.");
        note(r,Icons.PREFS,"Quiet speed","Drinking raises and tips the painted bottle. Haste leaves restrained blue-silver brush wisps at your boots while moving, for as long as its effect lasts. Ambush warnings have no directional marker, from either side; the popup follows your Playtest setting.");
        r=release(list,"v1.29.8","The Horror's warning stands on its own.");
        note(r,Icons.WARNING,"Ambush warning","The bronze directional marker is gone. Playtest balance settings can turn the ambush popup on or off; the warning text remains in the log and you still have an opportunity to respond.");
        r=release(list,"v1.29.7","Painted boss-room landmarks and bounty crews.");
        note(r,Icons.DEPTH,"Dungeon landmarks","The Caves gate and trap plates, City stairway and statues, Imp shop skulls and dais, and blacksmith's quenching trough have painted artwork. The Vault arrival stairwell no longer borrows fragments of the Imp's pit.");
        note(r,Icons.WARNING,"Bounty crews","Hunters patrol together and rally their crew when they spot their quarry. Armored hunters, bandits, and spellcasters have distinct appearances.");
        note(r,Icons.GOLD,"The Amulet","Claiming the Amulet presents its full painted artwork, with the familiar choice to leave or return.");
        r=release(list,"v1.29.6","Cole waits at the Prison stairs.");
        note(r,Icons.INFO,"Speak to Cole","After completing his Prison boss bounty, find Cole beside the stairs and speak to him to collect your payment and hear what comes next. His Wanted notice remains in your ranked run's Deeds.");
        r=release(list,"v1.29.5","The deeds and debts of a run, remembered.");
        note(r,Icons.JOURNAL,"Run history","Each ranked character has a Deeds tab: quest deliveries, the troll commission, the Imp's contract, dragon expedition outcomes and Cole's fate. Old runs without a recorded history say so.");
        note(r,Icons.RANKINGS,"Archived Wanted posters","Review your Wanted notice from Cole's betrayal and the contracts you accepted. Completed notices retain their crimson stamp. The highest paid bounty records the actual gold collected; archived notices cannot accept contracts or claim rewards.");
        r=release(list,"v1.29.4","A name crossed off the board. A purse plainly paid.");
        note(r,Icons.INFO,"Bounty receipts and urgency","Returning a paid Warrant shows a gold receipt, coin effect and sound. Warrants and journal entries show the urgent bonus clock; the small HUD clocks open their matching posters. An expired urgency bonus leaves the base bounty payable.");
        note(r,Icons.JOURNAL,"The Legendary Warrant","The coat is still carried by its quarry. Bring the Legendary Warrant back to Cole for a cash payment equal to the Rare bounty, without an urgency bonus.");
        note(r,Icons.JOURNAL,"Claimed bounties","Completed posters bear a large diagonal crimson CLAIMED stamp. The quarry's death marks the notice; uncollected payment remains due.");
        r=release(list,"v1.29.3","A clearer trail through the Prison.");
        note(r,Icons.JOURNAL,"Bounty floor references","The Last seen line on Warrants, posters and bounty journal entries now includes the absolute dungeon floor beside the Prison floor number.");
        r=release(list,"v1.29.2","Six faces worth remembering.");
        note(r,Icons.JOURNAL,"Named wanted creatures","Jack Twice-Hanged carries his noose and shackles; Nails guards a stolen payroll; Voss wears battered blue prison livery. The Widowmaker's broken machinery, the Bone Clerk's grim ledger and Morcant's red-and-gold armor each have their own paintings. Their posters and enlarged inspection images match the creatures you encounter.");
        r=release(list,"v1.29.1","A proper wanted notice.");
        note(r,Icons.JOURNAL,"Wanted posters","Bold WANTED lettering and the quarry's name sit above a larger centered portrait. Centered ink lists the contract's story, last sighting, bounty and urgency before its signature line and ornate worn rarity seal. Contract buttons remain accessible while scrolling.");
        r=release(list,"v1.29.0","Painted sanctums, a stately office and clearer discoveries.");
        note(r,Icons.JOURNAL,"The Warden's Office","Cole's office now has a broad carpet, a ledger desk, carved furniture and wall shackles. The board hangs by the wall and the five sale displays are spaced apart. Contracts read as parchment notices, with a large wanted portrait above their story and seal.");
        note(r,Icons.TALENT,"Painted talents and discoveries","The retained heroes' talents now have painted, effect-specific emblems. Journal lists show larger pictures, and identified consumables retain their identity emblem in examination and enlarged artwork. Bloodmarked Brands bear the same mark shown on their victims.");
        note(r,Icons.INFO,"The final sanctums","Yog's platform and the Amulet sanctum now use painted basalt, carved masonry, stained glass and candles. Blank Parchment can be sold or offered to a hungry Hatchling, one sheet at a time.");
        r=release(list,"v1.28.0","A name on a poster. A hunter who keeps his promises.");
        note(r,Icons.JOURNAL,"Cole's Bounty Board","Find Cole in his old Prison office. Read the posters and choose which contracts to sign; an unsigned contract never summons its quarry. His finite shop asks a steep price for good equipment. Some names on the board are best approached carefully.");
        note(r,Icons.INFO,"Promises and Warrants","Named wanted creatures, painted posters, physical Warrants and journal notes follow your accepted jobs. A swift hand may earn a fuller purse. The boss poster matches the prison's actual jailer, and Cole has one last contract to discuss afterward.");
        note(r,Icons.TALENT,"Hunter's equipment","Bloodmarked Brands and the Warden's Coat join the dungeon's equipment. Later hunters carry their own Warrants; what you bring back to Cole determines the terms he will offer. Each ending has a cosmetic badge.");
        note(r,Icons.PREFS,"Quest testing","Quests > Cole and Bounty Board groups status, travel and tuning. Prices, contract terms, wanted creatures, hunter crews, Cole's finite supplies and the new equipment can be tuned without resetting progress or duplicating prizes. New games receive the complete quest; older saves retain their existing maps.");
        r=release(list,"v1.27.0","See the dungeon's paintings up close.");
        note(r,Icons.MAGNIFY,"Artwork inspection","Tap an image or its small magnifying lens in an examination window to see a larger painting. Close it to return to the same description. Items, creatures, plants, traps, terrain, skills and journal notes support inspection, with sharper exports from the original paintings where available.");
        r=release(list,"v1.26.0","More mystery, clearer companions and organized testing tools.");
        note(r,Icons.INFO,"Rediscover the dungeon","Descriptions of Grimhollow's additions now favor atmosphere and useful hints over formulas and hidden reward tables. Actions and current conditions remain readable.");
        note(r,Icons.TALENT,"Rebuff","Psychics can learn a brief protective ward that answers a successful Push. Golden Mimic companions accept directions from their harness or inspection window.");
        note(r,Icons.PREFS,"Testing tools","Travel, expedition settings, class items and companion controls are grouped into submenus. Pages are remembered, with shortcuts to the main menu and the last submenu. Generation controls choose which types can appear in future random finds and roaming spawns.");
        note(r,Icons.JOURNAL,"Warnings and artwork","The Hatchling's hunger warning opens a popup and stops queued actions. Waterskin refill works in the testing tools. The Sewers boss exit, royal cushion, Rebuff and Living Earth fragments have renewed painted presentation.");
        r=release(list,"v1.25.1","Safer old saves and quieter hero presentation.");
        note(r,Icons.INFO,"Saved games","Older games missing an elemental-room planning field now load safely. Their previously encountered treasuries remain recorded.");
        note(r,Icons.PREFS,"Ordinary new games","Shared balance tuning still applies across games, but no longer enables Playtest tools automatically. Explicit tools and God mode remain separate for each save; customized games remain outside rankings and badges.");
        note(r,Icons.TALENT,"Enchanter knowledge","New Enchanters begin with three weapon enchantments and three armor glyphs. A random Rune Etching effect no longer becomes permanent trade knowledge just by crossing floors; actual discoveries and knowledge talents still work.");
        note(r,Icons.TALENT,"Hero presentation","Floating inventory weapon overlays are replaced by the heroes' generic painted poses. Accelerated movement has two short soft wisps at the boots, fading when you stop, instead of the overhead Haste flash. Combat, speed and timing are unchanged.");
        r=release(list,"v1.25.0","A chart that trades old paths for hidden treasure.");
        note(r,Icons.JOURNAL,"Wayward Chart","A new trinket marks painted treasure mounds without revealing their surroundings. Its promises grow with the chart, and it remembers which regions have already rewarded you.");
        note(r,Icons.INFO,"Wandering ink","The Chart can leave old paths faint after keeping a promise. Familiar mapping magic may help its wandering lines settle.");
        note(r,Icons.PREFS,"Chart controls","Playtest balance includes discovery odds, reward counts, gold, quality comparisons and the size of faded memories. Terrain, visible cells, doors and known hazards remain reliable.");
        r=release(list,"v1.24.0","A pirate's wager, and a golden guardian.");
        note(r,Icons.GOLD,"Fickle Doubloon","A rare coin grows through wagers, draws fortune from a hoard, and invites unwanted attention from thieves. Its Black Spot turns every flip against its bearer. It will not share its place with another artifact.");
        note(r,Icons.TALENT,"A costly transformation","A golden guardian has joined Grimhollow’s possibilities. Some dungeon bargains ask for more than gold; their outcomes are yours to discover.");
        note(r,Icons.PREFS,"Playtest controls","The balance menu now has separate Fickle Doubloon and Golden Mimic sections for odds, recharge, fortune, theft, treasure generation and companion strength and recovery. Changes persist across games on this device.");
        r=release(list,"v1.23.3","Readable keys, description controls and faster servant decay.");
        note(r,Icons.JOURNAL,"Collected keys","Collected key types and counts have a separate framed HUD row below the menu. Larger painted keys are no longer squeezed into the version label; the enemy counter moves down when keys are present.");
        note(r,Icons.MAGNIFY,"Complete descriptions","Long item, talent and creature descriptions have visible up/down controls and a bronze scrollbar, alongside dragging and mouse-wheel scrolling. Action buttons stay outside the text viewport. The last line has extra padding. Ashlight's feeding costs use readable hyphens.");
        note(r,Icons.TALENT,"Raised servants","Raised undead now deteriorate after their binding fades instead of disappearing instantly. Their remaining binding can still be examined.");
        r=release(list,"v1.23.2","Painted bone walls and a more threatening, clearly warned hunter.");
        note(r,Icons.TALENT,"Bone walls","Wand of Bone and Bone Prison share four stable painted bone barricades. Examine shows their movement/sight/projectile blocking, fire immunity and remaining binding turns. Their duration and terrain rules are unchanged.");
        note(r,Icons.WARNING,"Lurking Horror","Increase ordinary damage modestly and give sleeping-prey pounces their own stronger damage range. A full-health normal rat can no longer defeat the opening pounce at normal or doubled damage. The once-per-floor omen has a framed notice and audio cue; the larger ambush warning stays visible until you respond. Your fresh-action window, detection counters, solitary hunt and lifetime healing limit remain.");
        r=release(list,"v1.23.1","Bronze talent markers, natural weapon grips and clearer Haste rules.");
        note(r,Icons.TALENT,"Bronze talent markers","Rounded bronze studs show the ranks you have purchased. Tier headers distinguish ivory available points, bronze spent points and hollow future points, with an explicit available/spent count. A brass shuffle medallion replaces the old Random allocation icon. Talent costs and allocation rules are unchanged.");
        note(r,Icons.TALENT,"Weapon grips","Correct the palm anchors for all nine heroes and the handles of curved and narrow weapons. Short blades hang naturally at rest; hooked blades and polearms stay upright beside the body. Attacks still point toward the target. Combat and movement timings are unchanged.");
        note(r,Icons.INFO,"Haste and lingering cold","Haste triples movement speed but does not clear Chill, Slow or Cripple. Thawing in water leaves Chill, so a hasted step can still take long enough for a red sentry to interrupt travel. The potion and status descriptions now explain this interaction.");
        r=release(list,"v1.23.0","Clearer controls, aligned equipment and renewed necromancy.");
        note(r,Icons.TALENT,"Necromancy","Necrotic Touch leaves a stronger refreshed wound. Raised servants deteriorate as their binding fades, and spell information offers clearer guidance about the Phylactery’s curses.");
        note(r,Icons.JOURNAL,"Readable controls","Spent talent sockets glow gold; unused sockets remain dark. Keys remain visible above the journal button. Identification emblems keep their individual shapes in a restrained ivory-and-gold palette. The mouse pointer, stairs direction arrow, Distant Well and Rat King statue have new painted artwork, and the update screen has a scrollable framed layout. Smooth text is the default; your selected font preference is retained.");
        note(r,Icons.MAGNIFY,"Equipment and flames","Weapon layers align their actual handles and blade tips with both facing directions, including Bone Rod, spear and shortsword. Torch flames draw behind characters. Broken dash characters in English item descriptions are repaired. Haste remains movement speed rather than sentry immunity; other actions still give sentries time to charge.");
        r=release(list,"v1.22.6","Nine individual painted heroes complete the sprint.");
        note(r,Icons.TALENT,"Psychic presentation","Psychic has short silver hair, a circlet and angular lavender mantle over fitted armor, with precise restrained poses and the equipped Crystal or weapon. All nine heroes now have individual painted figures, portrait identities, fitted armor and weapon layers. These character paintings preserve movement and combat timing.");
        r=release(list,"v1.22.5","A gaunt painted Necromancer.");
        note(r,Icons.TALENT,"Necromancer presentation","Necromancer retains the elderly portrait face, silver hair and bone charms in a gaunt olive-black silhouette. Fitted armor preserves the stoop and asymmetric cowl, with subtle robe motion and equipped weapons. Gameplay and timing remain unchanged.");
        r=release(list,"v1.22.4","A steadfast painted Cleric and clearer grass overlap.");
        note(r,Icons.TALENT,"Cleric presentation","Cleric gains ivory cape and stole panels over fitted armor, with his portrait face and grounded natural proportions. Grass in the row behind upper bodies now draws behind them while blades remain around feet. Gameplay and timing remain unchanged.");
        r=release(list,"v1.22.3","A poised painted Duelist.");
        note(r,Icons.TALENT,"Duelist presentation","Duelist gains a fencing stance, fitted teal doublet, ivory collar and asymmetric shoulder armor, with her portrait identity and natural proportions. Equipped and ability-selected weapons follow her grip. Gameplay and timing remain unchanged.");
        r=release(list,"v1.22.2","A long-legged painted ranger.");
        note(r,Icons.TALENT,"Huntress presentation","Huntress has a moss-green split coat, short fur mantle, long legs and fitted ranger armor. Her held bow or other weapon follows the equipped loadout, with restrained coat movement. Gameplay and timing remain unchanged.");
        r=release(list,"v1.22.1","A nimble painted Rogue.");
        note(r,Icons.TALENT,"Rogue presentation","Rogue has a lean hooded silhouette, short asymmetric cloak, fitted charcoal armor and a muted teal sash. His empty hands display the equipped weapon, with quick restrained action poses. Gameplay and timing remain unchanged.");
        r=release(list,"v1.22.0","A leaner painted Mage.");
        note(r,Icons.TALENT,"Mage presentation","Mage now has a lean purple scholarly silhouette, long astronomical stole, armor fitted to his robe and restrained class-specific movement. His equipped staff or weapon uses the painted grip layer. Gameplay and timing remain unchanged.");
        r=release(list,"v1.21.5","Your equipment becomes part of your silhouette.");
        note(r,Icons.TALENT,"Held weapon prototype","Warrior and Enchanter now display the painted weapon they wield. Grip and swing follow the class, facing and weapon category; thrown and ability-selected weapons appear during their action. Gear uses the existing paintings, with gameplay and combat timing unchanged.");
        r=release(list,"v1.21.4","Every identity has its own emblem.");
        note(r,Icons.CATALOG,"Distinct painted icons","Rings, scrolls and potions now have sixty distinct painted identity emblems. Identify uses a book and lens, Mind Vision a third-eye profile, and Magical Sight a prism. The guide's surprise-attack serpent and Unbound Focus also have dedicated paintings. Unidentified appearances remain randomized.");
        r=release(list,"v1.21.3","A dependable weapon and more focused senses.");
        note(r,Icons.TALENT,"Psychic Crystal and senses","The Focus Crystal is a modest, growing weapon. Unbound Focus permits carried casting and recovery. Treasure Sense supplies a fleeting impression of nearby items, while Trap Sense improves searching and the Seer keeps its close awareness.");
        r=release(list,"v1.21.2","Two runes, working together.");
        note(r,Icons.TALENT,"Independent Rune Etchings","Enchanters can etch both weapon and armor at the same time. Each rune keeps its own floor-changing effect and paid upgrade, and transfers within its equipment category. Existing saves receive an unupgraded counterpart without losing their current rune.");
        r=release(list,"v1.21.1","A patient hunter finds its way back into shadow.");
        note(r,Icons.MAGNIFY,"Lurking Horror","The Horror follows reachable escape routes, continues fleeing while visible, and recovers hidden without attacking. It avoids nearby heroes and doorways. Tools and physical encounters expose it, and its lifetime healing remains limited to a quarter of its health.");
        r=release(list,"v1.21.0","Clearer characters and signals.");
        note(r,Icons.JOURNAL,"Visibility and interface fixes","Tall grass now stands behind upper bodies, with foreground blades around feet. The journal stays visible beside its keys and uses a small unread badge. Talent ranks distinguish filled and empty sockets and show available points. Hexcaster retreat stays aligned with its map position.");
        r=release(list,"v1.20.2","Reopen testing tools without leaving the dungeon.");
        note(r,Icons.TALENT,"Playtest in your run",
                "Enable Playtest for a save through the home-screen Playtest menu. That run's in-game menu then includes Playtest, with all its existing tools. The shortcut also appears for previously enabled saves and remains available after reloading or changing floors.");
        r=release(list,"v1.20.1","Warrior and Enchanter character-art proof of concept.");
        note(r,Icons.TALENT,"Two distinct silhouettes",
                "Warrior and Enchanter have new painted bodies with natural proportions and armor fitted to their individual costumes. Warrior retains his red gambeson and grounded stance; Enchanter wears a slender blue artisan coat with split tails and a tool belt. Their faces match their existing portraits.");
        note(r,Icons.MAGNIFY,"Calmer motion",
                "Both characters hold still while idle, with restrained walking and action poses. This two-character trial changes appearance only: gameplay, equipment, movement speed and combat timing are unchanged. The other seven characters keep their current artwork while this direction is reviewed.");
        r=release(list,"v1.19.2","Painted adventuring notes and reminders for sealed treasuries.");
        note(r,Icons.STAIRS_WATER,"Adventuring Notes",
                "Floor conditions, gardens, wells and other journal landmarks now have painted icons. NPC portraits fit their note tiles and description windows. Healing wells use a red heart.");
        note(r,Icons.STAIRS_SECRETS,"Treasury reminders",
                "Discovering an elemental treasury's clue or revealing its door adds a fire, water or lightning reminder to that floor's Adventuring Notes. The note explains how to return prepared, persists between visits and saved games, and clears once you open the seal.");
        r=release(list,"v1.19.1","Playtest tools move to the home screen.");
        note(r,Icons.TALENT,"Home-screen Playtest",
                "Open Playtest from the home screen to tune balance without loading a game, or select a saved run or start a new one for God mode, item creation and travel. Run-specific tools open once the selected dungeon is loaded. The active-run menu no longer contains Playtest. Ordinary saves keep their normal status until testing is enabled or custom balance is applied.");
        r=release(list,"v1.19.0","Written power, elemental treasuries, and a painted journal.");
        note(r,Icons.TALENT,"Spellguard","Spellguard replaces Overload, sheltering the Enchanter from hostile magic while worn armor carries a temporary inscription. Existing talent investments carry over.");
        note(r,Icons.SCROLL_COLOR,"Scribe","The Sigil Brush can write familiar scrolls on blank parchment using crystallized energy. Careful work takes time. Recycling scrolls can leave useful parchment behind.");
        note(r,Icons.STAIRS_SECRETS,"Elemental treasuries","Unfamiliar braziers, dry fountains and brass conductors may lead to hidden riches. These new treasuries grow rarer and richer with the dungeon’s depths, alongside ordinary secret rooms. Their seals reward experimentation.");
        note(r,Icons.JOURNAL,"Painted navigation",
                "Journal and menu buttons, journal tabs, alchemy categories and catalog icons have new painted artwork. Bestiary portraits fit their frames without cropping.");

        r=release(list,"v1.18.3","A rune for your armor, and light behind the right walls.");
        note(r,Icons.TALENT,"Armor Rune Etching","Rune Etching can now take a home on armor as well as weapons. Its changing pattern travels through the Sigil Brush without losing the strength entrusted to it.");
        note(r,Icons.MAGNIFY,"Hidden torch flames",
                "Wall torches and their light now require sight of the wall's facing side. Seeing the back of a wall no longer exposes a flame or glow in an unexplored room.");

        r=release(list,"v1.18.2","Read the materials, keep the atmosphere.");
        note(r,Icons.MAGNIFY,"Clearer terrain",
                "Regional ambient light and the hero's visibility light preserve more of the painted stone and foliage colors. "
                +"Torches and fire still cast warm local light. Flattened grass has a fuller spread of low leaves, making it easier to distinguish from bare paving. "
                +"Sight, fog, traps, movement and artifact charging are unchanged.");

        r=release(list,"v1.18.1","A measured appetite and clearer signals.");
        note(r,Icons.BACKPACK,"Hatchling balance","The Hatchling’s gifts are more restrained. It scents nearby treasure rather than revealing a whole floor, and a meal favors a single improvement. Identification concerns your own belongings.");
        note(r,Icons.INFO,"Clearer portraits and notifications",
                "Target-box creatures fill the available space without counting transparent sprite padding. The home screen opens this complete update log directly. "
                +"Healing wells and healing bursts show red hearts, and floating combat, pickup and spell notifications use painted symbols.");

        r=release(list,"v1.18.0","Prepared explorers and a better-fed companion.");
        note(r,Icons.JOURNAL,"Feed deliberately, revisit the log","The Hatchling accepts deliberate meals when hungry. Neglect still invites its appetite. Message History provides a saved, scrollable record, and every hero begins knowing their own equipment.");
        note(r,Icons.TALENT,"Enchanter refinements","The Enchanter enters with steel-tipped darts and stronger etchings. Defensive Sigil lasts longer; Wandering Brush permits carried casting. Resonance and Appraisal offer stronger magic and a trained eye for fresh finds.");
        note(r,Icons.STAIRS,"Clearer dungeon details",
                "Ascending and descending stairs and the eating symbol have new painted artwork. Garden shadowmeld motes now use soft painted light. "
                +"The Rat King's statue crown stays hidden until its statue cell has been discovered.");

        r=release(list,"v1.17.1","Grimhollow's update log.");
        note(r,Icons.JOURNAL,"Our journey so far",
                "The update log now follows Grimhollow's own releases, from its three new heroes to the Lurking Horror. "
                +"Browse the entries below for new adventures, equipment, painted artwork, balance changes and playtest fixes. "
                +"The upstream integration is summarized in one entry at the end.");

        r=release(list,"v1.17.0","The Lurking Horror.");
        note(r,Icons.SKULL,"A solitary predator",
                "A rare living predator can stalk one ordinary floor in each region. An arrival omen hints at its presence. "
                +"Its ambush warning interrupts running and resting, leaving you a fresh action to move out of reach, reveal it or turn invisible. "
                +"It follows your current position rather than striking an abandoned cell. Another nearby hostile prevents its ambush.\n\n"
                +"Revealing it forces an exposed retreat and recovery before it can hide again. It can heal only a quarter of its maximum health over its lifetime. "
                +"Later regions bring stronger attacks and separately warned follow-up strikes; in the Halls it can flee through wooden barricades.");
        note(r,Icons.MAGNIFY,"Detection and fresh remains","Detection tools can expose the Horror without mapping its surroundings. Its hunts may leave a distant death cry and fresh remains. Examination can reveal the marks of a patient predator.");

        r=release(list,"v1.16.1","Cavern salvage and shared settings.");
        note(r,Icons.STAIRS,"Expedition refinements",
                "The lower cavern has rough natural edges, safer fall arrivals and a broodmother farther from the landing area. "
                +"Both return routes use upward stairs. Mixed bone piles and adventurer remains favor common weapons, armor and ranged equipment, "
                +"with occasional rings and finite guaranteed food and torches. There is more salvage than one backpack can carry. "
                +"Playtest can tune the remains count, loot mix, equipment tier and upgrades.");
        note(r,Icons.PREFS,"Lasting discoveries and clearer plants",
                "Balance settings now apply across every game on this device and survive restarts; god mode stays local to its save. "
                +"The Hatchling can identify equipped items and belongings in every bag, while eating and equipment improvements retain their loose-inventory rules. "
                +"Dewcatchers and seedpods now have distinct painted forms.");

        r=release(list,"v1.16.0","The dragon expedition.");
        note(r,Icons.STAIRS_CHASM,"A map and a warning","A wounded City explorer offers an expedition into a dragon’s territory. Wooden bridges cross a vast chasm above a dark cavern. The way home lies beyond the hoard; heed the hunter’s warning.");
        note(r,Icons.GOLD,"The cavern and the hoard","Fallen adventurers lie beneath the bridges, watched by a poisonous broodmother and hungry scavengers. Above them, a dragon guards its treasure. New branches, bosses and rewards are available through Playtest for testing.");

        r=release(list,"v1.15.0","Tune the dungeon.");
        note(r,Icons.PREFS,"Balance tuning",
                "Playtest's balance menu controls enemy density and respawns, rare and curse-bound creatures, Hexcasters, Chainwarden, "
                +"floor loot, ordinary enemy drops, equipment tiers, upgrades, curses, enchantment rarity and item-category weights. "
                +"Each setting explains its limits and scope; reset restores Grimhollow defaults. "
                +"Generation changes affect future rolls and new or rebuilt floors. Guaranteed quest supplies remain protected. "
                +"These settings became shared across games in v1.16.1.");

        r=release(list,"v1.14.0-1.14.2","Effects, class balance and quest repairs.");
        note(r,Icons.BUFFS,"Painted effects and sharper creatures",
                "Grass rustling, curses, electricity, debris, droplets, mist, coins, frost and fire use softer painted particles. "
                +"Lightning and death, light and healing beams have painted cores. Gas is more visible while known loot remains readable through it. "
                +"Sharper exports cover creatures, merchants, NPCs, summons, bosses and sentries. "
                +"Raised-undead inspection shows remaining binding time; the Phylactery's summon menu is called Raise Dead.");
        note(r,Icons.TALENT,"Defensive Sigil and Ghouls","Defensive Sigil replaces Field Repair, giving the Enchanter a deliberate protective ward. Ghouls answer more experienced Necromancers and return less stolen vitality. Keepsakes now suit the current hero.");
        note(r,Icons.CHANGES,"Tablet and quest fixes",
                "Hatchling meals show readable item names, and feeding warnings stop queued or held movement before giving the player a fresh action. "
                +"Status icons wrap within the panel; the enemy counter and movement-resume arrow use painted art. "
                +"Interrupted movement and knockback no longer leave a Shaman's image away from its occupied cell.\n\n"
                +"Repaired the wandmaker's summoning circle and candle instructions, workshop and mine atlas layouts, and overflowing terrain previews. "
                +"Rails and shelves join across cells; cauldrons match laboratory floors. Mining ore no longer emits torch flames or leaves floating fire. "
                +"Boss health bars and floor indicators have painted frames.");

        r=release(list,"v1.13.0","Hatchling Mimic and a new dungeon story.");
        note(r,Icons.BACKPACK_LRG,"Hatchling Mimic",
                "A tiny mimic adopts your hoard and grazes on loose belongings. A well-fed hatchling can be surprisingly agreeable. "
                +"Examine it for hunger clues, and heed its warning before the next meal. "
                +"Its kin recognize the creature in your harness; not all welcome the sight.");
        note(r,Icons.JOURNAL,"Grimhollow's story and discoveries",
                "New regional introductions and journal lore tell Grimhollow's own story, accompanied by five painted loading screens. "
                +"Journal reference discoveries persist between games; potion, scroll and ring identities still reset for each run. "
                +"A practiced Grasp can disturb remains and open ordinary unlocked chests. "
                +"Known loot remains visible beneath gas, raised skeletons are sharper, and long descriptions scroll to their last line. "
                +"The Windows app has a custom icon and bundled runtime, and fullscreen remains open when focus changes.");

        r=release(list,"v1.12.0","Ranged inscriptions and smoother movement.");
        note(r,Icons.TALENT,"Inscribe and Lucky",
                "Inscribe supports carried thrown weapons and the Spirit Bow, filtering out any enchantment marked as melee-contact-only. "
                +"Thrown stacks preserve their inscriptions when split or merged. "
                +"Lucky now keeps a successful roll when multiple permanent, inscribed or etched effects are checked, including Overcharge. "
                +"Its description explains killing-hit and loot-eligibility requirements. "
                +"Skill descriptions show all ranks together instead of separate rank-toggle buttons.");
        note(r,Icons.DISPLAY,"Movement, travel and interface repairs",
                "Movement lighting updates use less work, with steadier creature poses and sharper Brutes and Shamans. "
                +"Tengu's smoke and electricity use painted effects; wall torches emit fire instead of inherited pipe drips. "
                +"Inventory numbers are larger and identification icons match their items. "
                +"Known walls remain at fog boundaries without revealing unknown overhangs.\n\n"
                +"Issue dialogs offer copyable diagnostic text. Class switching reuses the starter pouch and merges old duplicates without losing contents. "
                +"Travel into an unvisited floor chooses a valid arrival cell instead of placing the hero outside the map.");

        r=release(list,"v1.11.1-1.11.2","Class progression and tablet fixes.");
        note(r,Icons.TALENT,"Enchanter and meaningful talent ranks","Enchanter workings answer more readily. Talent purchases enforce limits and repair excess ranks from older saves. New-class armor paths and previously ineffective ranks now offer distinct progression.");
        note(r,Icons.CONTROLLER,"Touch, targeting and equipment","Android movement and camera follow are smoother. Barricades span their passages, descriptions scroll and Hurl retains its enemy-then-direction targeting. Class focuses preserve growth when replaced. Upgrade effects use painted rising light.");

        r=release(list,"v1.11.0","In-game Playtest menu.");
        note(r,Icons.PREFS,"Build a test run",
                "Enable Playtest from the game menu to use optional god mode, create and edit items, recharge artifacts, "
                +"set hero level and strength, switch classes, choose subclasses and armor paths, and allocate talents. "
                +"Travel to main floors and quest branches, rebuild a floor, reveal the map, place creatures, teleport or set resources. "
                +"These saves are labeled Playtests and excluded from rankings, badges and bones. "
                +"Rebuilding a floor does not reset completed quests or collected one-time rewards.");

        r=release(list,"v1.10.0-1.10.1","Nine individual hero silhouettes.");
        note(r,Icons.DISPLAY,"Painted heroes in motion",
                "All nine heroes have individual proportions, garments, stances, strides and attack or casting gestures, with sharper painted frames. "
                +"The Warrior is broad and planted; the Rogue crouches; the Huntress draws her bow; the Duelist lunges. "
                +"Mage, Cleric, Necromancer, Enchanter and Psychic have distinct clothing and casting poses. "
                +"Each face and class palette remains recognizable as armor improves. World size and action timing stay consistent.");

        r=release(list,"v1.9.0-1.9.1","Ashlight Lantern and inscription input.");
        note(r,Icons.ALCHEMY,"Ashlight Lantern","A soot-stained lantern feeds on alchemical fire. Its open light holds back darkness and announces its bearer. Experiment with its shutter and gathered ember; the dungeon’s shadows do not welcome them.");

        r=release(list,"v1.8.0-1.8.1","Readability, inspection and scouting.");
        note(r,Icons.MAGNIFY,"Read the battlefield",
                "Locked doors have clear lock markers, and ground items, especially keys, have stronger outlines. "
                +"Gas uses soft overlapping clouds; altar motes and environmental particles are larger. "
                +"Awards have distinct painted medals. Repeated inspection can be locked by pressing Examine twice, including for shops and inventory.\n\n"
                +"The Focus Crystal's reach and scouting grow with practice. "
                +"Gas overdraw was reduced and flame particles batched to lower rendering cost without changing hazard rules.");

        r=release(list,"v1.7.0","Plants, skill icons and lasting knowledge.");
        note(r,Icons.GRASS,"Painted growth and spellcraft",
                "Sprouted plants have distinct painted forms. New-class talents, subclasses, armor abilities and spells have custom icons. "
                +"Enchanter starts knowing Obfuscation, Swiftness and Viscosity for armor inscription alongside its starter weapon enchantments. "
                +"Learned inscriptions persist across floors and saves; only Rune Etching's active effect rerolls. "
                +"The alphabetized inscription library scrolls, includes descriptions and explains unavailable casts.");

        r=release(list,"v1.6.0","Portraits, handbook, traps and icons.");
        note(r,Icons.TALENT,"Choose your hero",
                "Nine matching painted portraits and class splashes replace shared character art. "
                +"Character selection presents each hero's theme; selecting again or opening information shows Profile, Growth, Paths and Armor. "
                +"Duelist is selectable without the old badge lock. "
                +"Enemy idle poses are steady, traps have painted mechanisms, and a shared iron-and-amber emblem supplies Windows and Android launcher icons.");

        r=release(list,"v1.5.0","Psychic growth and a painted interface.");
        note(r,Icons.TALENT,"Psychic and Enchanter openings","Psychic thrown weapons endure better, and the Focus Crystal learns through use. Push, Hurl and domination gain richer progression. Enchanters begin with familiar inscriptions and a steadier Brush; finding equipment remains an act of discovery.");
        note(r,Icons.BACKPACK_LRG,"Inventory and menus",
                "Painted bronze and leather panels, enamel buttons, equipment borders, glass status bars and navigation symbols refresh the interface. "
                +"Item artwork covers weapons, armor, artifacts and their states, potions, scrolls, seeds, stones, spells and quest objects. "
                +"Item descriptions scroll while their actions remain accessible, and class spell menus fit portrait and landscape.");

        r=release(list,"v1.1.0-1.4.0","The painted world and complete bestiary.");
        note(r,Icons.DISPLAY,"A darker painted dungeon",
                "Painted terrain, animated water, walls, doors, grass states, props and lighting replace the earlier visual style across all five regions. "
                +"The title screen gains painted art and animation. Creatures, bosses, NPCs, summons, mimics and sentries receive matching painted artwork, "
                +"with species-specific scale, larger heroes, cleaner texture edges and better health-bar placement. "
                +"Status emblems and alert cues are painted, and wall torches match the regional masonry.");
        note(r,Icons.CHANGES,"Crashes, text and item identity",
                "Fixed the render-thread crash encountered during Necromancer play, including item drops and death effects. "
                +"Repaired missing names and formatting in descriptions. Gravity and Blast Wave now display their separate names and correct descriptions, "
                +"so a pulling wand is no longer mislabeled as a knockback wand.");

        r=release(list,"v1.0.1-1.0.2","Rendering recovery.");
        note(r,Icons.MAGNIFY,"Fog, terrain and item repairs",
                "Corrected remembered terrain, fog scale, camera alignment and lighting during movement and door transitions. "
                +"Fixed item-sheet mismatches, including Waterskin and Mind Vision icons. "
                +"Restored the earlier world art and upstream characters as a recovery baseline; the later painted releases superseded those temporary visuals. "
                +"Menus and project links were cleaned up while preserving attribution.");

        r=release(list,"Foundation-v1.0.0","Three new heroes and Grimhollow content.");
        note(r,Icons.TALENT,"Necromancer, Enchanter and Psychic",
                "Grimhollow adds three heroes to the original six, each with its own equipment, talents, two subclasses and three armor paths. "
                +"Necromancer raises servants and casts curses through the Phylactery, with Deathspeaker and Hexweaver paths. "
                +"Enchanter uses the Sigil Brush, Runecraft and Rune Etching, with Artificer and Scrivener paths. "
                +"Psychic uses the Focus Crystal for telekinesis and perception, with Puppeteer and Seer paths. "
                +"Class progression, minion behavior, charges and saved knowledge received subsequent playtest revisions recorded above.");
        note(r,Icons.ALCHEMY,"Equipment, enemies and curses",
                "New content includes Wands of Necrosis, Gravity and Bone; the Soulfire spell; Bone, Reaper's and Grave Scythes; "
                +"Bone Armor; and the Hourglass of Ashes. Added item curses include Leech, Echo, Withering and Dark Blessing. "
                +"Rare Hexcasters, curse-bound creature variants and the alternate Prison boss Chainwarden bring new threats. "
                +"Painted effects, blood and scorch marks, dynamic lighting and the Grimhollow title and identity grew through the foundation releases.");

        release(list,"Upstream foundation","Incorporated Shattered Pixel Dungeon 4.0's expanded Imp/Vault quest, content additions, enchantments, curses and bug fixes.");
    }
}
