// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.*;
import com.shatteredpixel.shatteredpixeldungeon.journal.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.*;
import java.util.ArrayList;
import java.util.Arrays;

/** Runs inside the existing real-generator/headless gate, without another harness. */
final class HatchlingScenario {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError("Hatchling: "+message);}
    private static Hero hero(){return Dungeon.hero;}
    private static HatchlingMimic fresh(){
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);SmokeRun.clearArena();
        hero().belongings.backpack.items.clear();
        HatchlingMimic hatchling=new HatchlingMimic();hatchling.collect();
        hero().belongings.identify(); // The test kit is not an unknown target in meal fixtures.
        return hatchling;
    }
    private static void carry(Item...items){hero().belongings.backpack.items.addAll(Arrays.asList(items));}
    private static void due(HatchlingMimic item){while(!item.warned())item.tick(hero());item.onHeroReady();}
    private static Mimic mimic(Class<? extends Mimic> type,int cell){
        Mimic mimic=Mimic.spawnAt(cell,type);mimic.sprite=mimic.sprite();mimic.sprite.link(mimic);
        new com.watabou.noosa.Group().add(mimic.sprite);
        Dungeon.level.heroFOV[cell]=false; // Room AI is independent of renderer visibility.
        Dungeon.level.mobs.add(mimic);Actor.add(mimic);return mimic;
    }
    private static void lootSprite(int cell){
        Heap heap=new Heap();heap.pos=cell;heap.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite();
        heap.sprite.link(heap);Dungeon.level.heaps.put(cell,heap);
    }
    static void run() throws Exception {
        manualContainers();
        abandonment();
        HatchlingMimic hatchling=fresh();
        Item[] food={new ThrowingKnife().quantity(3),new Dagger(),new Firebloom.Seed(),new StoneOfIntuition(),
                new PotionOfHealing(),new BlankParchment(),new ScrollOfIdentify(),new Javelin(),new WandOfMagicMissile(),
                new RingOfEvasion(),new LeatherArmor(),new Greatsword(),new AshlightLantern()};
        carry(food);
        for(Item expected:food){check(hatchling.nextFood(hero())==expected,"feed order "+expected.getClass());expected.detachAll(hero().belongings.backpack);}
        carry(new Food().quantity(10),new Waterskin(),new TrinketCatalyst(),new com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers(),new FocusCrystal(),new Phylactery());
        VelvetPouch bag=new VelvetPouch();bag.items.add(new Firebloom.Seed());carry(bag);
        check(hatchling.nextFood(hero())==null,"protected and unlisted items, nested bag");
        check(HatchlingMimic.foodPriority(hero().belongings.weapon(),hero())<0,"equipped weapon protected");
        Item known=new Dagger().identify(),unknown=new Dagger();carry(unknown,known);
        check(hatchling.nextFood(hero())==known,"identified first");
        check(HatchlingMimic.foodPriority(new Shuriken(),hero())==0,"tier-two thrown gap closed");
        check(!HatchlingMimic.canUpgrade(new AshlightLantern())&&!HatchlingMimic.canUpgrade(new FocusCrystal()),"artifact/focus upgrade restrictions");
        check(new BlankParchment().quantity(3).value()==15 && HatchlingMimic.foodTier(new BlankParchment())==Tier.MINOR,"sellable parchment is a minor meal");
        hatchling=fresh();Item paper=new BlankParchment().quantity(3);carry(paper);
        due(hatchling);hatchling.tick(hero());
        check(hero().belongings.contains(paper)&&paper.quantity()==2,"a parchment meal consumes one sheet");

        hatchling=fresh();Item knives=new ThrowingKnife().quantity(3);carry(knives);
        ArrayList<String> events=new ArrayList<>();Signal.Listener<String> listener=s->{events.add(s);return false;};GLog.update.add(listener);
        try {
            hero().resting=true;
            hero().curAction=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction.Move(hero().pos+3);
            for(int t=0;t<298;t++)hatchling.tick(hero());
            check(events.isEmpty()&&hero().belongings.contains(knives),"no early warning or consumption");
            hatchling.tick(hero());
            check(events.size()==1&&events.get(0).startsWith(GLog.WARNING)&&!hero().resting&&hatchling.warned(),"warning interrupts exactly one turn before meal");
            check(hero().curAction==null,"warning cancels queued travel");
            HatchlingMimic copy=(HatchlingMimic)hatchling.duplicate();check(copy.warned()&&copy.remaining()==1,"pending warning survives item serialization");
            for(int t=0;t<6;t++)hatchling.tick(hero());
            copy.tick(hero());
            check(hero().belongings.contains(knives)&&events.size()==1,"slow or paralysed turns and reload cannot consume before player control returns");
            hatchling.onHeroReady();
            hatchling.tick(hero());check(!hero().belongings.contains(knives)&&events.size()==2,"whole throwing stack consumed, single confirmation");
            check(events.get(1).contains(knives.title())&&!events.get(1).contains("com.shatteredpixel")&&!events.get(1).contains("@"),"meal reports localized title and stack count, not Java object identity");
            check(hero().buff(ItemSense.class)!=null,"no eligible target falls back to Sense");
        } finally {GLog.update.remove(listener);}

        hatchling=fresh();hatchling.level(3);
        carry(new Shortsword().identify());
        String benefit=hatchling.benefit(hero(),Tier.EXCEPTIONAL);
        check(!benefit.contains("com.shatteredpixel")&&!benefit.contains("@")&&benefit.contains("shortsword"),"upgrade and enchantment messages use item titles");
        System.out.println("TEST 55 WARNING PASS: queued travel/rest interrupted; slow-turn and reload barrier; localized meal/upgrade/enchantment titles");

        hatchling=fresh();Dungeon.gold=100000;
        for(long amount:new long[]{50,100,200,400,800,1600,3200}){
            int before=Dungeon.gold;due(hatchling);hatchling.tick(hero());
            check(before-Dungeon.gold==amount&&hatchling.goldDemand()==amount*2,"gold sequence "+amount);
            check(HatchlingMimic.goldTier(amount)==(amount<=100?Tier.MINOR:amount<=400?Tier.STANDARD:amount<=1600?Tier.MAJOR:Tier.EXCEPTIONAL),"gold benefit tier");
        }
        long demand=hatchling.goldDemand();carry(new Firebloom.Seed());due(hatchling);hatchling.tick(hero());
        hatchling=(HatchlingMimic)hatchling.duplicate();check(hatchling.goldDemand()==demand,"item meal and serialization never reset gold debt");
        int beforeUpgrade=hatchling.remaining();hatchling.upgrade();
        check(hatchling.goldDemand()==demand&&hatchling.remaining()<beforeUpgrade,"upgrading preserves elapsed fraction and debt");
        for(int level=0;level<3;level++){hatchling.level(level);check(hatchling.upgradeEnergyCost()==10+5*level,"large upgrade cost");}
        hatchling.level(0);due(hatchling);
        ArrayList<Item> ingredients=new ArrayList<>();ingredients.add(hatchling);
        Trinket.UpgradeTrinket recipe=new Trinket.UpgradeTrinket();
        check(recipe.testIngredients(ingredients)&&recipe.cost(ingredients)==10,"ordinary cauldron recipe");
        HatchlingMimic brewed=(HatchlingMimic)recipe.brew(ingredients);
        check(brewed.level()==1&&brewed.warned()&&brewed.remaining()==1&&brewed.goldDemand()==demand,"cauldron cannot erase pending hunger or gold debt");
        brewed.level(3);ingredients.clear();ingredients.add(brewed);check(!recipe.testIngredients(ingredients),"cauldron caps at +3");

        hatchling=fresh();Dungeon.gold=17;due(hatchling);hatchling.tick(hero());
        check(Dungeon.gold==0&&HatchlingMimic.carried()==null&&hero().buff(Escape.class)!=null,"partial gold payment escapes immediately");
        Escape pending=hero().buff(Escape.class);Bundle state=new Bundle();state.put("pending",pending);
        Escape restored=(Escape)state.get("pending");check(restored.mimic!=null,"pending encounter serializes");
        pending.detach();restored.attachTo(hero());Dungeon.depth=2;restored.act();
        check(hero().buff(Escape.class)==null&&Dungeon.level.mobs.stream().anyMatch(m->m instanceof Mimic&&((Mimic)m).hatchlingBorn),"escape encounter arrives once on next floor");
        check(HatchlingMimic.nextEscapeDepth(4)==6&&HatchlingMimic.nextEscapeDepth(24)==26&&HatchlingMimic.nextEscapeDepth(26)==24,"boss/final-floor boundaries");

        hatchling=fresh();Dungeon.level.heaps.clear();
        int near=hero().pos+2,far=hero().pos+3;
        lootSprite(near);lootSprite(far);
        Dungeon.level.heaps.get(near).items.add(new Dagger());Dungeon.level.heaps.get(far).items.add(new Dagger());
        Dungeon.level.heroFOV[near]=Dungeon.level.heroFOV[far]=false;
        Dungeon.level.heaps.get(near).seen=Dungeon.level.heaps.get(far).seen=false;
        boolean[] fov=Dungeon.level.heroFOV.clone(),visited=Dungeon.level.visited.clone(),mapped=Dungeon.level.mapped.clone();
        for(int level=0;level<4;level++)for(Tier tier:Tier.values()){
            ItemSense sense=Buff.affect(hero(),ItemSense.class);sense.refresh(level,tier);
            check(sense.turnsLeft()==20&&sense.radius()==new int[]{5,8,12,16}[level],"nearest scent duration/range");
            check(sense.senses(near)&&!sense.senses(far)&&!sense.revealsMimics(),"exactly nearest pile, no mimic scan");
            Bundle saved=new Bundle();sense.storeInBundle(saved);ItemSense copy=new ItemSense();copy.restoreFromBundle(saved);
            check(copy.senses(near)&&!copy.senses(far),"selected scent survives reload");
            sense.act();sense.refresh(level,tier);check(sense.turnsLeft()==20,"refresh replaces duration");
        }
        ItemSense sense=hero().buff(ItemSense.class);Dungeon.level.heaps.remove(near);
        check(!sense.senses(near)&&!sense.senses(far),"collection never chains to another pile");
        sense.refresh(3,Tier.MINOR);check(sense.senses(far),"next meal may choose next pile");
        check(Arrays.equals(fov,Dungeon.level.heroFOV)&&Arrays.equals(visited,Dungeon.level.visited)&&Arrays.equals(mapped,Dungeon.level.mapped),"Sense never reveals terrain");
        for(int i=0;i<20;i++)sense.act();check(!sense.senses(far),"scent expires in twenty turns");
        sense.refresh(3,Tier.EXCEPTIONAL);Dungeon.depth++;check(!sense.senses(far),"no scent across floors");

        for(int level=0;level<4;level++)for(Tier tier:Tier.values()){
            hatchling=fresh();hatchling.level(level);Ring ring=new RingOfEvasion();carry(ring);
            hatchling.benefit(hero(),tier);
            check(ring.isIdentified(),"remaining item identified");
            if(tier==Tier.MAJOR)check(ring.level()==1,"major permanent +1 ring");
            if(tier==Tier.EXCEPTIONAL)check(ring.level()==2,"exceptional permanent +2 ring");
            if(tier==Tier.MINOR)check(ring.level()==0,"minor never upgrades");
            Bundle saved=new Bundle();saved.put("ring",ring);check(((Ring)saved.get("ring")).level()==ring.level(),"benefit persists in saves");
            check(HatchlingMimic.upgradeChance(level,Tier.STANDARD)==(level==0?.3f:.5f),"standard probability");
            if(level>=2)check(hero().buff(ItemSense.class)!=null,"level-two-plus meal always senses");
        }
        hatchling=fresh();Potion potion=new PotionOfHealing();carry(potion);hatchling.benefit(hero(),Tier.MINOR);
        check(potion.isIdentified(),"consumables can be identified");
        hatchling=fresh();
        MagicalHolster holster=new MagicalHolster(); ScrollHolder holder=new ScrollHolder();
        PotionBandolier bandolier=new PotionBandolier(); VelvetPouch pouch=new VelvetPouch();
        carry(holster,holder,bandolier,pouch);
        Item[] protectedTargets={new WandOfMagicMissile(),new ScrollOfIdentify(),new PotionOfHealing(),
                new Dagger(),new LeatherArmor(),new RingOfEvasion(),new AshlightLantern()};
        holster.items.add(protectedTargets[0]);holder.items.add(protectedTargets[1]);bandolier.items.add(protectedTargets[2]);
        hero().belongings.weapon=(Weapon)protectedTargets[3];hero().belongings.armor=(Armor)protectedTargets[4];
        hero().belongings.ring=(Ring)protectedTargets[5];hero().belongings.artifact=(Artifact)protectedTargets[6];
        for(int i=0;i<protectedTargets.length;i++)hatchling.benefit(hero(),Tier.MINOR);
        for(Item target:protectedTargets)check(target.isIdentified(),"identification reaches bagged/equipped "+target.getClass());
        check(hatchling.nextFood(hero())==null,"wider identification must not widen feeding");
        WandOfLightning nested=new WandOfLightning();pouch.items.add(new VelvetPouch());
        ((VelvetPouch)pouch.items.get(0)).items.add(nested);
        hatchling.level(3);lootSprite(hero().pos+1);Wand floorWand=new WandOfLightning();Dungeon.level.heaps.get(hero().pos+1).items.add(floorWand);
        hatchling.benefit(hero(),Tier.EXCEPTIONAL);check(!floorWand.isIdentified(),"unowned floor gear not identified");
        check(nested.isIdentified()&&nested.trueLevel()==0,"nested bag identification without upgrade");
        for(Item target:protectedTargets)check(target.trueLevel()==0,"protected targets cannot be upgraded");
        System.out.println("TEST 55 IDENTIFY PASS: bagged consumables/wands, equipped gear/artifact and nested bags; feeding and upgrades remain loose-only");
        for(int level=1;level<4;level++)for(Tier tier:new Tier[]{Tier.MAJOR,Tier.EXCEPTIONAL}){
            int curses=0,rare=0,enchants=0,upgrades=0;
            hatchling=fresh();hatchling.level(level);
            Random.pushGenerator(5500+4*level+tier.ordinal());
            for(int trial=0;trial<640;trial++){
                Greatsword sword=new Greatsword();sword.identify();carry(sword);
                hatchling.benefit(hero(),tier);
                check((sword.enchantment!=null)!=(sword.level()>0),"exactly one equipment reward");
                check(sword.inscriptionTurns==0,"permanent reward");
                if(sword.enchantment==null){upgrades++;check(sword.level()==(tier==Tier.EXCEPTIONAL?2:1),"major +1 and exceptional +2 unchanged");sword.detachAll(hero().belongings.backpack);continue;}
                enchants++;
                if(sword.hasCurseEnchant())curses++;
                if(Arrays.asList(Weapon.Enchantment.rare).contains(sword.enchantment.getClass()))rare++;
                sword.detachAll(hero().belongings.backpack);
            }
            Random.popGenerator();
            check(curses>0&&curses<65,"curse outcomes present without dominating");
            check(upgrades>enchants&&enchants>10,"upgrade-biased exclusive reward choice");
            if(tier==Tier.EXCEPTIONAL)check(rare>30,"elevated rare enchantment chance");
        }

        hatchling=fresh();CloakOfShadows artifact=new CloakOfShadows();artifact.level(10);carry(artifact);events.clear();GLog.update.add(listener);
        try{due(hatchling);check(events.size()==1,"artifact warning");hatchling.tick(hero());check(events.size()==1,"artifact transformation has no confirmation");}
        finally{GLog.update.remove(listener);}
        check(HatchlingMimic.carried()==null&&!hero().belongings.contains(artifact),"artifact and trinket gone");
        Mimic grown=(Mimic)Dungeon.level.mobs.iterator().next();
        check(grown.hatchlingBorn&&grown.alignment==Char.Alignment.ENEMY,"transformation hostile immediately");
        check(grown.items.stream().noneMatch(i->i instanceof CloakOfShadows||i instanceof HatchlingMimic||i instanceof TrinketCatalyst),"forbidden recovery loot excluded");
        for(int level=0;level<=10;level++)check(Math.abs(HatchlingMimic.crystalChance(level)-new float[]{.05f,.15f,.15f,.25f,.25f,.4f,.4f,.6f,.6f,.85f,.85f}[level])<.0001f,"artifact probability table");
        for(int i=0;i<80;i++){Item reward=HatchlingMimic.wealthReward(CloakOfShadows.class);check(!reward.cursed&&(!reward.isUpgradable()||reward.level()>=5),"+10 Wealth reward");}

        hatchling=fresh();int origin=hero().pos;
        Mimic ordinary=mimic(Mimic.class,origin+1);ordinary.actForHatchling();check(ordinary.alignment==Char.Alignment.NEUTRAL&&Dungeon.level.distance(origin,ordinary.pos)>1,"ordinary mimic steps away at +0");
        Dungeon.level.heroFOV[ordinary.pos]=false;lootSprite(ordinary.pos);hero().lvl=30;
        check(hatchling.charm(ordinary)&&ordinary.alignment==Char.Alignment.ALLY,"standard ally conversion");
        Mimic other=mimic(Mimic.class,origin+2);check(!hatchling.charm(other),"one charm per floor");
        Mimic golden=mimic(GoldenMimic.class,origin-1);golden.actForHatchling();check(golden.alignment==Char.Alignment.NEUTRAL&&!hatchling.charm(golden),"Golden retreats and cannot be charmed");
        Mob.holdAllies(Dungeon.level);check(ordinary.alignment==Char.Alignment.ENEMY&&Dungeon.level.mobs.contains(ordinary),"charmed mimic stays behind and releases");
        Mimic crystal=mimic(CrystalMimic.class,origin+3);crystal.takeHatchling();
        check(HatchlingMimic.carried()==null&&crystal.stolenHatchling==hatchling,"theft removes original item");
        Bundle stolen=new Bundle();stolen.put("m",crystal);check(((Mimic)stolen.get("m")).stolenHatchling!=null,"stolen state survives save");
        int normalLoot=crystal.items.size();lootSprite(crystal.pos);crystal.rollToDropLoot();
        check(crystal.stolenHatchling==null&&Dungeon.level.heaps.get(crystal.pos).items.stream().anyMatch(i->i instanceof HatchlingMimic),"interrupted theft recovers Hatchling");
        check(Dungeon.level.heaps.get(crystal.pos).size()==normalLoot+1,"interrupted theft has no extra reward");
        hatchling=fresh();crystal=mimic(CrystalMimic.class,hero().pos+2);crystal.takeHatchling();crystal.escapeWithHatchling();
        check(!Dungeon.level.mobs.contains(crystal)&&hero().buff(Escape.class)!=null&&crystal.stolenHatchling==null,"completed theft schedules Crystal encounter without recoverable Hatchling");

        hatchling=fresh();
        com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel rooms=(com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)Dungeon.level;
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room room=rooms.rooms().stream().filter(r->r.width()>=6&&r.height()>=6).findFirst().get();
        int width=Dungeon.level.width(),center=(room.left+2)+(room.top+2)*width;
        for(int dy=-1;dy<=2;dy++)for(int dx=-1;dx<=2;dx++)Level.set(center+dx+dy*width,Terrain.EMPTY);
        hero().pos=center;
        Mimic ebony=mimic(EbonyMimic.class,center+2);ebony.actForHatchling();
        check(ebony.alignment==Char.Alignment.ENEMY&&ebony.state==ebony.HUNTING,"Ebony enrages on room entry at level zero");ebony.destroy();
        crystal=mimic(CrystalMimic.class,center+2);crystal.actForHatchling();
        check(crystal.alignment==Char.Alignment.ENEMY&&Dungeon.level.adjacent(crystal.pos,hero().pos),"Crystal approaches on room entry");
        hero().invisible=1;crystal.actForHatchling();check(HatchlingMimic.carried()==hatchling,"invisibility prevents a theft attempt");hero().invisible=0;
        for(int attempt=0;attempt<200&&crystal.stolenHatchling==null;attempt++)crystal.actForHatchling();
        check(crystal.stolenHatchling==hatchling,"actual adjacent hit/evasion theft contest");
        crystal.pos=Dungeon.level.exit();hero().pos=Dungeon.level.entrance();Dungeon.level.heroFOV[crystal.pos]=true;
        crystal.actForHatchling();check(crystal.stolenHatchling!=null,"visible fleeing mimic remains recoverable");
        Dungeon.level.heroFOV[crystal.pos]=false;crystal.actForHatchling();
        check(hero().buff(Escape.class)!=null&&!Dungeon.level.mobs.contains(crystal),"unseen mimic escapes only through deeper exit");

        hatchling=fresh();int remote=hero().pos+3;
        boolean[] passable=Dungeon.level.passable.clone();Arrays.fill(Dungeon.level.passable,false);Dungeon.level.passable[remote]=true;
        check(HatchlingMimic.closestSpawn(hero().pos)==remote,"transformation searches beyond blocked adjacent cells");
        Dungeon.level.passable[remote]=false;check(HatchlingMimic.closestSpawn(hero().pos)<0,"full map defers transformation");Dungeon.level.passable=passable;
        due(hatchling);long rememberedGold=hatchling.goldDemand();Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
        check(HatchlingMimic.carried().warned()&&HatchlingMimic.carried().remaining()==1&&HatchlingMimic.carried().goldDemand()==rememberedGold,"full save/load retains pending warning and debt");

        // Grasp opens only its tier's containers and never bypasses locks.
        FocusCrystal focus=new FocusCrystal();focus.level(2);check(!focus.canOpenWithGrasp(Heap.Type.SKELETON),"Grasp bones tier boundary");
        focus.level(3);check(focus.canOpenWithGrasp(Heap.Type.SKELETON)&&!focus.canOpenWithGrasp(Heap.Type.CHEST),"Grasp bones tier");
        focus.level(7);check(focus.canOpenWithGrasp(Heap.Type.CHEST)&&!focus.canOpenWithGrasp(Heap.Type.LOCKED_CHEST),"Grasp chest tier and locked exclusion");
        Catalog.setSeen(HatchlingMimic.class);Journal.saveGlobal();Dungeon.init();
        check(Catalog.isSeen(HatchlingMimic.class),"new game retains reference knowledge");
        check(Arrays.asList(Generator.Category.TRINKET.classes).contains(HatchlingMimic.class),"natural trinket pool includes Hatchling");
        for(float weight:Generator.Category.TRINKET.probs)check(weight==1,"existing trinket generation weights preserved");
        Dungeon.LimitedDrops.TRINKET_CATA.count=0;Dungeon.depth=3;check(Dungeon.trinketCataNeeded(),"one catalyst guaranteed by floor three");
        Dungeon.LimitedDrops.TRINKET_CATA.drop();
        for(int depth=1;depth<=26;depth++){Dungeon.depth=depth;check(!Dungeon.trinketCataNeeded(),"no second natural catalyst at depth "+depth);}
        System.out.println("TEST 55 PASS: Hatchling hierarchy, hunger, gold, benefits, detection, transformation, Wealth rewards, kinship, theft and persistence; Grasp and journal PASS");
    }
    private static void manualContainers(){
        HatchlingMimic hatchling=fresh();
        PotionBandolier bandolier=new PotionBandolier();VelvetPouch pouch=new VelvetPouch();
        MagicalHolster holster=new MagicalHolster();ScrollHolder scrolls=new ScrollHolder();
        carry(bandolier,pouch,holster,scrolls);
        Potion potion=new PotionOfHealing();potion.quantity(2);bandolier.items.add(potion);
        Item seed=new Firebloom.Seed();pouch.items.add(seed);
        Item wand=new WandOfMagicMissile();holster.items.add(wand);
        Item scroll=new ScrollOfIdentify();scrolls.items.add(scroll);
        Honeypot honey=new Honeypot();honey.quantity(2);carry(honey);
        while(!hatchling.hungry())hatchling.tick(hero());
        for(Item meal:new Item[]{potion,seed,wand,scroll,honey})check(hatchling.canFeed(hero(),meal),"manual container/value target "+meal.getClass());
        check(hatchling.nextFood(hero())==null,"manual selection broadened passive grazing");
        Item quest=new com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers();bandolier.items.add(quest);
        Item key=new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(1);carry(key);
        check(!hatchling.canFeed(hero(),quest)&&!hatchling.canFeed(hero(),key)
                && !hatchling.canFeed(hero(),hatchling)&&!hatchling.canFeed(hero(),bandolier)
                && !hatchling.canFeed(hero(),hero().belongings.weapon()),"manual quest/key/container/equipped protections");
        check(hatchling.feedChosen(hero(),potion)&&potion.quantity()==1&&bandolier.items.contains(potion),"one nested potion, container preserved");
        while(!hatchling.hungry())hatchling.tick(hero());
        check(hatchling.feedChosen(hero(),honey)&&honey.quantity()==1&&hero().belongings.contains(honey),"one valuable honey pot, not its stack");
        while(!hatchling.hungry())hatchling.tick(hero());
        check(hatchling.feedChosen(hero(),seed)&&!pouch.items.contains(seed)&&hero().belongings.contains(pouch),"nested seed detaches from correct container");
        System.out.println("HATCHLING MANUAL PASS: all four specialized containers, valuable honey pots, one-unit meals, protected quest/key/gear/bags and unchanged loose-only grazing");
    }
    private static void abandonment() throws Exception {
        for(int level=0;level<4;level++){
            HatchlingMimic hatchling=fresh();hatchling.level(level);
            for(int turn=0;turn<11;turn++)hatchling.tick(hero());
            if(level==3)due(hatchling);
            int hunger=hatchling.remaining();long gold=hatchling.goldDemand();
            float now=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.clock();
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling stray=
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.leave(hatchling,hero().pos);
            check(stray!=null&&!hero().belongings.contains(hatchling)&&Dungeon.level.mobs.contains(stray),"dropped pet is one neutral floor actor");
            Bundle b=new Bundle();b.put("stray",stray);Bundle fields=new Bundle();stray.storeInBundle(fields);
            float leave=fields.getFloat("leave_at"),escape=fields.getFloat("escape_at");
            check(leave-now>=120-20*level && leave-now<=180-30*level && escape==leave+20,"level-scaled saved grace and escape deadlines");
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling restored=
                    (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling)b.get("stray");
            check(restored.pet().remaining()==hunger&&restored.pet().goldDemand()==gold&&restored.pet().level()==level,"pet hunger/gold/level survive ground serialization");
            Bundle after=new Bundle();restored.storeInBundle(after);
            check(after.getFloat("leave_at")==leave&&after.getFloat("escape_at")==escape,"reload cannot reroll deadlines");
            Actor.remove(stray);Dungeon.level.mobs.remove(stray);Dungeon.level.mobs.add(restored);Actor.add(restored);
            Statistics.duration+=leave-com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.clock();
            int position=restored.pos;restored.act();
            check(restored.pos==position&&restored.description().contains("stairs"),"warning grants a fresh action before flight");
            if(!Dungeon.level.adjacent(hero().pos,restored.pos)){
                for(int offset:PathFinder.NEIGHBOURS8){int cell=restored.pos+offset;if(Dungeon.level.insideMap(cell)&&Dungeon.level.passable[cell]&&Actor.findChar(cell)==null){hero().pos=cell;break;}}
            }
            HatchlingMimic recovered=restored.pet();
            check(restored.interact(hero())&&hero().belongings.contains(recovered)&&!Dungeon.level.mobs.contains(restored),"recapture before permanent escape");
            check(recovered.remaining()==hunger&&recovered.goldDemand()==gold,"recapture must not reset hunger or gold demand");
            if(recovered.warned()){
                recovered.tick(hero());
                check(hero().belongings.contains(recovered)&&recovered.remaining()==1&&recovered.goldDemand()==gold,"pending hunger must wait for player control after recapture");
            }
        }
        HatchlingMimic hatchling=fresh();
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling stray=
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.leave(hatchling,hero().pos);
        Bundle fields=new Bundle();stray.storeInBundle(fields);
        Statistics.duration+=fields.getFloat("leave_at")-com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.clock();
        stray.sprite=stray.sprite();stray.sprite.link(stray);new com.watabou.noosa.Group().add(stray.sprite);
        stray.act(); // Warning.
        int old=stray.pos;stray.act();
        check(stray.pos!=old || stray.pos==Dungeon.level.exit(),"scurrying pet must path toward stairs");
        Statistics.duration+=25;stray.act();
        check(!Dungeon.level.mobs.contains(stray)&&stray.pet()==null&&!hero().belongings.contains(hatchling)
                &&hero().buff(Escape.class)==null,"abandonment removes pet permanently, without farming loot or a transformed mimic");
        hatchling=fresh();stray=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.leave(hatchling,hero().pos);
        fields=new Bundle();stray.storeInBundle(fields);
        float before=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.clock();
        Actor.fixTime();check(Math.abs(before-com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.clock())<.001f,"run-clock rebasing changed deadline");
        Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
        stray=null;for(Mob mob:Dungeon.level.mobs)if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling)stray=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling)mob;
        check(stray!=null&&stray.pet()!=null,"dropped pet disk reload");
        // Time spent away counts even when its floor's actor is not simulated.
        Statistics.duration+=fields.getFloat("escape_at")-com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.AbandonedHatchling.clock()+1;
        stray.act();check(!Dungeon.level.mobs.contains(stray),"time away allowed indefinite pet storage");
        System.out.println("HATCHLING ABANDONMENT PASS: four grace ranges, warning/action, stair path, recapture, unchanged hunger/gold, fixed saved deadlines, disk reload, rebasing and elapsed-away expiry; no loot/XP transformation");
    }
}
