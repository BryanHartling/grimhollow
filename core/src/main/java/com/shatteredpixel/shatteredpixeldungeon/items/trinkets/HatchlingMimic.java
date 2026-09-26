// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunedBaton;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.*;
import java.util.function.Predicate;

/** Hunger belongs to the item, not its carrier or floor; cauldron duplication preserves it. */
public class HatchlingMimic extends Trinket {
    public enum Tier { MINOR, STANDARD, MAJOR, EXCEPTIONAL }
    private int remaining = 300;
    private boolean warned;
    private long goldDemand = 50;
    private final HashSet<Integer> charmedFloors = new HashSet<>();
    { image = ItemSpriteSheet.HATCHLING_MIMIC; bones = false; }

    public static HatchlingMimic carried() {
        return Dungeon.hero == null ? null : Dungeon.hero.belongings.getItem(HatchlingMimic.class);
    }
    public int interval() { return 300 - 50 * Math.max(0, Math.min(3, level())); }
    public int remaining() { return remaining; }
    public long goldDemand() { return goldDemand; }
    public boolean warned() { return warned; }
    @Override public int upgradeEnergyCost() { return 10 + 5 * level(); }
    @Override public Item upgrade() {
        int oldInterval = interval();
        super.upgrade();
        if (!warned) remaining = Math.max(2, (int)Math.ceil(remaining * interval() / (float)oldInterval));
        return this;
    }
    @Override public boolean collect(Bag bag) {
        boolean result = super.collect(bag);
        if (result && Dungeon.hero != null) Buff.affect(Dungeon.hero, Feeding.class);
        return result;
    }
    @Override public String statsDesc() {
        float fraction = remaining / (float)interval();
        String hunger = warned ? "hunger_now" : fraction > .5f ? "hunger_full"
                : fraction > .25f ? "hunger_restless" : fraction > .1f ? "hunger_hungry" : "hunger_soon";
        return Messages.get(this, "stats", interval(), goldDemand)
                + "\n\n" + Messages.get(this, hunger) + "\n\n" + Messages.get(this, "benefits_" + level());
    }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("hunger_left", remaining); b.put("hunger_warned", warned); b.put("gold_demand", goldDemand);
        b.put("charmed_floors", charmedFloors.stream().mapToInt(Integer::intValue).toArray());
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        remaining = b.contains("hunger_left") ? Math.max(0, b.getInt("hunger_left")) : interval();
        warned = b.getBoolean("hunger_warned");
        goldDemand = b.contains("gold_demand") ? Math.max(50, b.getLong("gold_demand")) : 50;
        charmedFloors.clear(); for (int floor : b.getIntArray("charmed_floors")) charmedFloors.add(floor);
    }

    public static boolean protectedItem(Item item, Hero hero) {
        return item == null || item.isEquipped(hero) || item instanceof Bag
                || item instanceof Phylactery || item instanceof FocusCrystal || item instanceof SigilBrush
                || item.unique && !(item instanceof Artifact) && !(item instanceof RunedBaton);
    }
    /** Only explicitly listed families can be eaten. Nested bags are never traversed. */
    public static int foodPriority(Item item, Hero hero) {
        if (protectedItem(item, hero)) return -1;
        if (item instanceof MissileWeapon) return ((MissileWeapon)item).tier <= 2 ? 0 : 6;
        if (item instanceof MeleeWeapon && ((MeleeWeapon)item).tier <= 2) return 1;
        if (item instanceof Plant.Seed) return 2;
        if (item instanceof Runestone) return 3;
        if (item instanceof Potion) return 4;
        if (item instanceof Scroll) return 5;
        if (item instanceof Wand) return 7;
        if (item instanceof Ring) return 8;
        if (item instanceof Armor) return 9;
        if (item instanceof Weapon) return 10;
        if (item instanceof Artifact) return 11;
        return -1;
    }
    private static int unitValue(Item item) { return item.value() / Math.max(1, item.quantity()); }
    private static int foodBand(Item item) {
        if (item instanceof Wand) return item.level() <= 1 ? 0 : item.level() <= 4 ? 1 : 2;
        if (item instanceof MeleeWeapon) return ((MeleeWeapon)item).tier;
        if (item instanceof MissileWeapon) return ((MissileWeapon)item).tier;
        return 0;
    }
    public Item nextFood(Hero hero) {
        ArrayList<Item> candidates = new ArrayList<>();
        for (Item item : hero.belongings.backpack.items) if (foodPriority(item, hero) >= 0) candidates.add(item);
        Comparator<Item> order = Comparator.comparingInt((Item i) -> foodPriority(i, hero))
                .thenComparingInt(i -> i.isIdentified() ? 0 : 1).thenComparingInt(HatchlingMimic::foodBand)
                .thenComparingInt(HatchlingMimic::unitValue);
        return minimum(candidates, order);
    }
    private static Item minimum(ArrayList<Item> candidates, Comparator<Item> order) {
        if (candidates.isEmpty()) return null;
        candidates.sort(order);
        Item first = candidates.get(0);
        candidates.removeIf(i -> order.compare(i, first) != 0);
        return Random.element(candidates);
    }
    public static Tier foodTier(Item item) {
        if (item instanceof Ring) return Tier.EXCEPTIONAL;
        if (item instanceof Wand) return item.level() <= 1 ? Tier.STANDARD : item.level() <= 4 ? Tier.MAJOR : Tier.EXCEPTIONAL;
        int tier = item instanceof MeleeWeapon ? ((MeleeWeapon)item).tier
                : item instanceof MissileWeapon ? ((MissileWeapon)item).tier : item instanceof Armor ? ((Armor)item).tier : 0;
        if (tier > 0) return item instanceof MissileWeapon && tier == 1 ? Tier.MINOR
                : tier <= 3 ? Tier.STANDARD : tier <= 5 ? Tier.MAJOR : Tier.EXCEPTIONAL;
        if (item instanceof ExoticPotion || item instanceof ExoticScroll
                || item instanceof PotionOfStrength || item instanceof ScrollOfUpgrade) return Tier.MAJOR;
        if (item instanceof Potion || item instanceof Scroll) return Tier.STANDARD;
        return Tier.MINOR;
    }
    public static Tier goldTier(long amount) {
        return amount < 200 ? Tier.MINOR : amount < 800 ? Tier.STANDARD : amount < 3200 ? Tier.MAJOR : Tier.EXCEPTIONAL;
    }

    /** One world turn; the warning and consumption are separate serialized states. */
    public void tick(Hero hero) {
        if (remaining > 1) remaining--;
        if (remaining == 1 && !warned) {
            warned = true;
            hero.interrupt(); hero.resting = false;
            GLog.w(Messages.get(this, "warning"));
        } else if (warned) {
            if (feed(hero)) { remaining = interval(); warned = false; }
        }
    }
    private boolean feed(Hero hero) {
        Item meal = nextFood(hero);
        if (meal instanceof Artifact) {
            int cell = closestSpawn(hero.pos);
            if (cell < 0) return false; // An entirely full level postpones consumption, without losing the warning.
            int artifactLevel=Math.round(10f*meal.level()/Math.max(1,((Artifact)meal).playtestLevelCap()));
            Class<? extends Mimic> type = Random.Float() < crystalChance(artifactLevel) ? CrystalMimic.class : mimicType(Dungeon.depth);
            Class<?> consumed = meal.getClass();
            meal.detachAll(hero.belongings.backpack);
            detachAll(hero.belongings.backpack);
            Mimic grown = Mimic.spawnAt(cell, type);
            grown.items.removeIf(i -> i.getClass() == consumed);
            grown.items.add(wealthReward(consumed));
            awaken(grown);
            GameScene.add(grown);
            Item.updateQuickslot();
            return true; // Deliberately no consumption/confirmation message.
        }
        if (meal != null) {
            Tier tier = foodTier(meal);
            Item eaten = meal instanceof MissileWeapon ? meal.detachAll(hero.belongings.backpack) : meal.detach(hero.belongings.backpack);
            String effects = benefit(hero, tier);
            GLog.w(Messages.get(this, "ate", eaten.toString(), effects));
        } else {
            long demand = goldDemand;
            if (Dungeon.gold < demand) {
                int taken = Dungeon.gold; Dungeon.gold = 0;
                GLog.w(Messages.get(this, "escape_gold", taken));
                detachAll(hero.belongings.backpack);
                scheduleEscape(Mimic.spawnAt(hero.pos, mimicType(nextEscapeDepth(Dungeon.depth))));
                Item.updateQuickslot();
                return true;
            }
            Dungeon.gold -= (int)demand;
            goldDemand = demand > Long.MAX_VALUE / 2 ? Long.MAX_VALUE : demand * 2;
            String effects = benefit(hero, goldTier(demand));
            GLog.w(Messages.get(this, "ate_gold", demand));
            if (!effects.isEmpty()) GLog.w(effects);
        }
        Item.updateQuickslot();
        return true;
    }
    private Item benefitTarget(Hero hero, Predicate<Item> eligible) {
        ArrayList<Item> candidates = new ArrayList<>();
        for (Item item : hero.belongings.backpack.items)
            if (foodPriority(item, hero) >= 0 && eligible.test(item)) candidates.add(item);
        return minimum(candidates, Comparator.comparingInt(HatchlingMimic::unitValue));
    }
    public static boolean canUpgrade(Item item) {
        return !(item instanceof Artifact) && (item instanceof Weapon || item instanceof Armor || item instanceof Wand || item instanceof Ring)
                && item.isUpgradable();
    }
    public static float upgradeChance(int level, Tier tier) {
        return tier == Tier.MINOR ? 0 : tier == Tier.STANDARD ? (level == 0 ? .3f : .5f) : 1;
    }
    public static float curseChance(int level, Tier tier) {
        return level == 1 ? .2f : level == 2 ? (tier == Tier.EXCEPTIONAL ? .1f : .15f)
                : tier == Tier.EXCEPTIONAL ? .05f : .1f;
    }
    public String benefit(Hero hero, Tier tier) {
        ArrayList<String> descriptions = new ArrayList<>();
        boolean eligibleEffect = false;
        Item identified = benefitTarget(hero, i -> !i.isIdentified());
        if (identified != null) {
            identified.identify(); eligibleEffect = true;
            descriptions.add(Messages.get(this, "identified", identified.name()));
        }
        if (tier == Tier.EXCEPTIONAL) {
            boolean any = false;
            for (Item item : hero.belongings.backpack.items)
                if(foodPriority(item,hero)>=0&&!item.isIdentified()){item.identify();any=true;}
            for (Heap heap : Dungeon.level.heaps.valueList()) for (Item item : heap.items)
                if (!item.isIdentified()) { item.identify(); any = true; }
            if (any) { eligibleEffect = true; descriptions.add(Messages.get(this, "identified_floor")); }
        }
        float chance = upgradeChance(level(), tier);
        Item upgrade = chance == 0 ? null : benefitTarget(hero, HatchlingMimic::canUpgrade);
        if (upgrade != null) {
            eligibleEffect = true;
            if (Random.Float() < chance) {
                int amount = tier == Tier.EXCEPTIONAL ? 2 : 1;
                upgrade.upgrade(amount);
                descriptions.add(Messages.get(this, "upgraded", upgrade.toString(), amount));
            }
        }
        if (level() >= 1 && tier.ordinal() >= Tier.MAJOR.ordinal()) {
            Item enchanted = benefitTarget(hero, i -> i instanceof Weapon || i instanceof Armor);
            if (enchanted != null) {
                eligibleEffect = true;
                boolean curse = Random.Float() < curseChance(level(), tier);
                // Exceptional meals raise the rare branch to 50% of non-curse rolls.
                boolean rare = tier == Tier.EXCEPTIONAL && Random.Int(2) == 0;
                String name;
                if (enchanted instanceof Weapon) {
                    Weapon weapon = (Weapon)enchanted;
                    Weapon.Enchantment enchantment;
                    do { enchantment = curse ? Weapon.Enchantment.randomCurse() : rare ? Weapon.Enchantment.randomRare() : Weapon.Enchantment.random(); }
                    while (SigilBrush.ranged(weapon) && enchantment.meleeContactOnly());
                    weapon.enchant(enchantment); name = enchantment.name();
                } else {
                    Armor.Glyph glyph = curse ? Armor.Glyph.randomCurse() : rare ? Armor.Glyph.randomRare() : Armor.Glyph.random();
                    ((Armor)enchanted).inscribe(glyph); name = glyph.name();
                }
                if(curse)enchanted.cursed=true;
                enchanted.cursedKnown = true;
                descriptions.add(Messages.get(this, "enchanted", enchanted.toString(), name));
            }
        }
        if (level() >= 2 || !eligibleEffect) {
            Buff.affect(hero, ItemSense.class).refresh(level(), tier);
            descriptions.add(Messages.get(this, "sense"));
        }
        if (descriptions.isEmpty()) descriptions.add(Messages.get(this, "satisfied"));
        return String.join(" ", descriptions);
    }
    public boolean charm(Mimic mimic) {
        int key = Dungeon.depth + Dungeon.branch * 100;
        if (mimic.getClass() != Mimic.class || charmedFloors.contains(key)) return false;
        AllyBuff.affectAndLoot(mimic, Dungeon.hero, Kinship.class);
        if (mimic.buff(Kinship.class) == null) return false;
        charmedFloors.add(key);
        mimic.kinship(true);
        GLog.w(Messages.get(this, "charmed"));
        return true;
    }
    public static float crystalChance(int level) {
        return level <= 0 ? .05f : level <= 2 ? .15f : level <= 4 ? .25f : level <= 6 ? .4f : level <= 8 ? .6f : .85f;
    }
    public static Class<? extends Mimic> mimicType(int depth) {
        return depth <= 5 ? Mimic.class : depth <= 15 ? GoldenMimic.class : EbonyMimic.class;
    }
    public static Item wealthReward(Class<?> excluded) {
        Item item;
        do { item = RingOfWealth.genEquipmentDrop(10); }
        while (item.getClass() == excluded || Challenges.isItemBlocked(item));
        return item;
    }
    public static int closestSpawn(int origin) {
        int result = -1, distance = Integer.MAX_VALUE;
        for (int cell = 0; cell < Dungeon.level.length(); cell++)
            if (Dungeon.level.insideMap(cell) && Dungeon.level.passable[cell] && !Dungeon.level.pit[cell]
                    && cell != Dungeon.hero.pos && Dungeon.level.findMob(cell) == null) {
                int d = Dungeon.level.distance(origin, cell);
                if (d < distance) { distance = d; result = cell; }
            }
        return result;
    }
    public static void awaken(Mimic mimic) {
        mimic.wakeHatchling();
    }
    public static int nextEscapeDepth(int depth) {
        int next = depth + 1;
        if (next % 5 == 0) next++;
        return next <= 26 ? next : 24; // At the dungeon bottom, intercept the return journey.
    }
    public static void scheduleEscape(Mimic mimic) {
        Escape escape = new Escape();
        escape.mimic = mimic; escape.destination = nextEscapeDepth(Dungeon.depth);
        escape.originDepth = Dungeon.depth; escape.originBranch = Dungeon.branch;
        escape.attachTo(Dungeon.hero);
    }
    public static class Feeding extends Buff {
        { actPriority = HERO_PRIO + 1; revivePersists = true; }
        @Override public boolean act() {
            HatchlingMimic item = carried();
            if (item != null && target.isAlive()) item.tick((Hero)target);
            spend(TICK); return true;
        }
    }
    public static class ItemSense extends Buff {
        private int[] clocks = new int[16];
        private int depth, branch;
        private static final int[][] DURATIONS = {{20,40,80,150},{30,60,100,200},{50,80,150,300},{80,120,200,400}};
        private static final int[][] RADII = {{5,8,-1,-1},{8,-1,-1,-1},{-1,-1,-1,-1},{-1,-1,-1,-1}};
        { type = buffType.POSITIVE; }
        public static int duration(int level, Tier tier) { return DURATIONS[level][tier.ordinal()]; }
        public static int radius(int level, Tier tier) { return RADII[level][tier.ordinal()]; }
        public int turnsLeft() { return Arrays.stream(clocks).max().orElse(0); }
        public int radius() {
            int radius=0;
            for(int i=0;i<clocks.length;i++) if(clocks[i]>0) {
                int r=RADII[i/4][i%4]; if(r<0)return -1; radius=Math.max(radius,r);
            }
            return radius;
        }
        public boolean revealsMimics() { return depth==Dungeon.depth&&branch==Dungeon.branch&&clocks[15]>0; }
        public void refresh(int level, Tier tier) {
            if(depth!=Dungeon.depth||branch!=Dungeon.branch)Arrays.fill(clocks,0);
            depth=Dungeon.depth; branch=Dungeon.branch;
            clocks[level*4+tier.ordinal()]=duration(level,tier);
        }
        public boolean senses(int cell) { return depth==Dungeon.depth&&branch==Dungeon.branch&&turnsLeft()>0
                && (radius()<0||Dungeon.level.distance(target.pos,cell)<=radius()); }
        @Override public int icon() { return BuffIndicator.FORESIGHT; }
        @Override public String iconTextDisplay() { return Integer.toString(turnsLeft()); }
        @Override public boolean act() {
            for(int i=0;i<clocks.length;i++)clocks[i]=Math.max(0,clocks[i]-1);
            if(turnsLeft()==0||depth!=Dungeon.depth||branch!=Dungeon.branch)detach();
            spend(TICK); return true;
        }
        @Override public String desc() { return Messages.get(this,"desc",turnsLeft(),radius()<0?Messages.get(this,"floor"):radius()); }
        @Override public void storeInBundle(Bundle b) { super.storeInBundle(b); b.put("clocks",clocks); b.put("depth",depth); b.put("branch",branch); }
        @Override public void restoreFromBundle(Bundle b) {
            super.restoreFromBundle(b); int[] saved=b.getIntArray("clocks");
            clocks=Arrays.copyOf(saved,16); depth=b.getInt("depth"); branch=b.getInt("branch");
        }
    }
    public static class Kinship extends ScrollOfSirensSong.Enthralled {
        @Override public void detach() {
            if (target != null) {
                target.alignment = Char.Alignment.ENEMY;
                if (target instanceof Mimic) ((Mimic)target).kinship(false);
            }
            super.detach();
        }
    }
    public static class Escape extends Buff {
        public Mimic mimic;
        private int destination, originDepth, originBranch;
        private boolean leftOrigin;
        { revivePersists = true; }
        @Override public boolean act() {
            leftOrigin |= Dungeon.depth != originDepth || Dungeon.branch != originBranch;
            if (leftOrigin && Dungeon.branch == 0 && Dungeon.depth == destination) {
                int cell = closestSpawn(Dungeon.hero.pos);
                if (cell >= 0) {
                    mimic.pos=cell; mimic.setLevel(Dungeon.scalingDepth());
                    if (mimic.items == null) mimic.items=new ArrayList<>();
                    mimic.items.add(wealthReward(null)); awaken(mimic); GameScene.add(mimic); detach();
                }
            }
            spend(TICK); return true;
        }
        @Override public void storeInBundle(Bundle b) {
            super.storeInBundle(b); b.put("mimic",mimic); b.put("destination",destination);
            b.put("origin_depth",originDepth); b.put("origin_branch",originBranch); b.put("left_origin",leftOrigin);
        }
        @Override public void restoreFromBundle(Bundle b) {
            super.restoreFromBundle(b); mimic=(Mimic)b.get("mimic"); destination=b.getInt("destination");
            originDepth=b.getInt("origin_depth"); originBranch=b.getInt("origin_branch"); leftOrigin=b.getBoolean("left_origin");
        }
    }
}
