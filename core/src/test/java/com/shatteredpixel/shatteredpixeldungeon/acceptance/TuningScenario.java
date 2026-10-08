// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.HashSet;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

/** Exercise the production generator and persisted menu values in the existing headless harness. */
final class TuningScenario {
    private static void check(boolean ok,String text){if(!ok)throw new AssertionError("58: "+text);}
    private static Rat rat(){Rat rat=new Rat();rat.sprite=rat.sprite();return rat;}
    private static String sequence(){
        Random.pushGenerator(58001);Generator.fullReset();StringBuilder out=new StringBuilder();
        for(int i=0;i<80;i++) {
            Item item=Generator.random();out.append(item.getClass().getName()).append(':').append(item.trueLevel()).append(':').append(item.cursed);
            out.append(MobSpawner.getMobRotation(12));
        }
        out.append(Random.Long());Random.popGenerator();return out.toString();
    }
    static void run() throws Exception {
        BalanceTuning.reset();Playtest.reset();Dungeon.hero=null;Dungeon.level=null;
        check(BalanceTuning.get(HORROR_WARNING_POPUP)==1&&HORROR_WARNING_POPUP.display(0).equals("OFF")&&HORROR_WARNING_POPUP.display(1).equals("ON"),"ambush popup defaults/display");
        BalanceTuning.setShared(DENSITY,125);BalanceTuning.setShared(HORROR_WARNING_POPUP,0);BalanceTuning.loadShared();
        check(BalanceTuning.get(HORROR_WARNING_POPUP)==0,"ambush popup shared persistence");
        check(BalanceTuning.configured(DENSITY)==125&&!Playtest.enabled()&&BalanceTuning.get(DENSITY)==125,"home settings must remain separate from tool access");
        boolean homeRejected=false;
        try{BalanceTuning.setShared(DENSITY,201);}catch(IllegalArgumentException expected){homeRejected=true;}
        check(homeRejected&&BalanceTuning.configured(DENSITY)==125,"home profile range enforcement");
        for(BalanceTuning.Key key:BalanceTuning.Key.values())if(key.group==3&&key!=GOLD)BalanceTuning.setShared(key,0);
        homeRejected=false;try{BalanceTuning.setShared(GOLD,0);}catch(IllegalArgumentException expected){homeRejected=true;}
        check(homeRejected&&BalanceTuning.configured(GOLD)==100,"home item weights accepted an empty loot pool");
        Playtest.reset();check(!Playtest.enabled()&&Playtest.unranked()&&BalanceTuning.get(DENSITY)==125&&!Playtest.god(),"new run adopts tuning without enabling tools or God mode");
        BalanceTuning.reset();check(BalanceTuning.get(HORROR_WARNING_POPUP)==1,"ambush popup reset default");Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);
        boolean rejected=false;try{BalanceTuning.set(CURSEBOUND,0);}catch(IllegalStateException expected){rejected=true;}
        check(rejected,"ordinary save accepted mutation");
        String standard=sequence();Playtest.enable();check(standard.equals(sequence()),"default tuning changes seeded generator/RNG");
        for(BalanceTuning.Key key:BalanceTuning.Key.values()){
            check(!Messages.get(BalanceTuning.class,key.id()).equals(Messages.NO_TEXT_FOUND),"missing label "+key);
            check(!Messages.get(BalanceTuning.class,key.id()+"_desc").equals(Messages.NO_TEXT_FOUND),"missing description "+key);
        }
        for(int seed=0;seed<100;seed++) {
            Random.pushGenerator(seed);boolean expected=Random.Int(10)==0;long next=Random.Long();Random.popGenerator();
            Random.pushGenerator(seed);check(BalanceTuning.roll(CURSEBOUND,10,1)==expected&&Random.Long()==next,"default spawn RNG changed");Random.popGenerator();
        }
        BalanceTuning.set(CURSEBOUND,0);for(int i=0;i<100;i++){Rat rat=rat();CursedVariant.roll(rat);check(rat.buff(CursedVariant.class)==null,"zero curse chance");}
        BalanceTuning.set(CURSEBOUND,100);BalanceTuning.set(CURSE_HEALTH,100);
        Rat rat=rat();int base=rat.HT;CursedVariant.roll(rat);check(rat.HT==2*base&&rat.buff(CursedVariant.class)!=null,"curse chance/health not applied");
        rat.pos=Dungeon.hero.pos;Dungeon.level.heaps.clear();BalanceTuning.set(CURSE_LOOT,0);rat.buff(CursedVariant.class).drop();check(Dungeon.level.heaps.size==0,"zero bonus loot");
        BalanceTuning.set(CURSE_LOOT,100);rat=rat();rat.pos=Dungeon.hero.pos;CursedVariant.roll(rat);rat.buff(CursedVariant.class).drop();
        int qty=Dungeon.level.heaps.get(rat.pos).items.size();rat.buff(CursedVariant.class).drop();check(qty==1&&Dungeon.level.heaps.get(rat.pos).items.size()==qty,"bonus loot duplicates");
        BalanceTuning.set(HEXCASTER,0);for(int i=0;i<30;i++)check(!MobSpawner.getMobRotation(12).contains(Hexcaster.class),"Hexcaster disabled");
        BalanceTuning.set(HEXCASTER,100);check(MobSpawner.getMobRotation(12).contains(Hexcaster.class)&&!MobSpawner.getMobRotation(6).contains(Hexcaster.class),"Hexcaster depth limits");
        BalanceTuning.set(CHAINWARDEN,0);check(!BalanceTuning.roll(CHAINWARDEN,10,3),"Chainwarden zero");BalanceTuning.set(CHAINWARDEN,100);check(BalanceTuning.roll(CHAINWARDEN,10,3),"Chainwarden full");
        BalanceTuning.reset();float cooldown=Dungeon.level.respawnCooldown();BalanceTuning.set(RESPAWN,200);check(Dungeon.level.respawnCooldown()==cooldown/2,"respawn interval");
        BalanceTuning.set(RESPAWN,0);int before=Dungeon.level.mobs.size();
        java.lang.reflect.Method act=MobSpawner.class.getDeclaredMethod("act");act.setAccessible(true);MobSpawner spawner=new MobSpawner();act.invoke(spawner);check(Dungeon.level.mobs.size()==before&&spawner.cooldown()>0,"disabled respawner added a mob or failed to yield");
        Snake snake=new Snake(); // Zero base loot must not acquire loot simply from a multiplier.
        BalanceTuning.set(MOB_LOOT,0);check(snake.lootChance()==0&&new Slime().lootChance()==0,"zero enemy loot");
        BalanceTuning.set(MOB_LOOT,100);float slime=new Slime().lootChance();BalanceTuning.set(MOB_LOOT,200);check(new Slime().lootChance()==2*slime,"enemy loot multiplier");
        BalanceTuning.reset();BalanceTuning.set(UPGRADES,0);BalanceTuning.set(CURSED_GEAR,0);BalanceTuning.set(ENCHANTED_GEAR,0);
        for(int i=0;i<100;i++)for(Item item:new Item[]{new Dagger(),new ClothArmor(),new WandOfMagicMissile(),new RingOfEvasion()}){
            item.random();check(item.trueLevel()==0&&!item.cursed,"zero gear quality/curse rates");
            if(item instanceof Weapon)check(((Weapon)item).enchantment==null,"zero enchantment chance");
            if(item instanceof Armor)check(((Armor)item).glyph==null,"zero glyph chance");
        }
        BalanceTuning.set(UPGRADES,400);BalanceTuning.set(ENCHANTED_GEAR,1000);
        for(int i=0;i<80;i++){
            Dagger weapon=(Dagger)new Dagger().random();ClothArmor armor=(ClothArmor)new ClothArmor().random();
            check(weapon.trueLevel()>=1&&weapon.enchantment!=null&&!weapon.enchantment.curse(),"boosted gear generation");
            check(armor.trueLevel()>=1&&armor.glyph!=null&&!armor.glyph.curse(),"boosted armor generation");
        }
        BalanceTuning.set(CURSED_GEAR,400);check(new Dagger().random().cursed&&new WandOfMagicMissile().random().cursed,"curse priority");
        BalanceTuning.reset();BalanceTuning.set(BONE_ARMOR,0);BalanceTuning.set(TIER_SHIFT,2);
        for(int i=0;i<30;i++)check(Generator.randomWeapon(0).tier>=3&&Generator.randomMissile(0).tier>=3,"tier shift lower clamp");
        BalanceTuning.set(TIER_SHIFT,-2);for(int i=0;i<30;i++)check(Generator.randomWeapon(4).tier<=3,"tier shift upper clamp");
        BalanceTuning.reset();BalanceTuning.set(RARE_ENCHANT,0);check(BalanceTuning.enchantRarity(new float[]{5,3,1})[2]==0,"rare enchant weight");
        BalanceTuning.reset();for(BalanceTuning.Key key:BalanceTuning.Key.values())if(key.group==3&&key!=GOLD)BalanceTuning.set(key,0);
        for(int i=0;i<70;i++)check(Generator.random() instanceof Gold&&Generator.randomUsingDefaults() instanceof Gold,"category exclusions not applied to both paths");
        rejected=false;try{BalanceTuning.set(GOLD,0);}catch(IllegalArgumentException expected){rejected=true;}check(rejected&&BalanceTuning.get(GOLD)==100,"all-zero weights accepted");
        BalanceTuning.set(ARTIFACT,100);BalanceTuning.set(GOLD,0);Generator.fullReset();HashSet<Class<?>> artifacts=new HashSet<>();
        for(int i=0;i<40;i++){Item item=Generator.random();if(item instanceof Artifact)check(artifacts.add(item.getClass()),"duplicate unique artifact");else check(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring,"exhausted artifact fallback");}
        check(artifacts.size()>5,"artifact pool not exercised");
        BalanceTuning.reset();BalanceTuning.set(CURSEBOUND,0);BalanceTuning.set(DENSITY,0);BalanceTuning.set(FLOOR_LOOT,0);
        Dungeon.saveAll();Dungeon.loadGame(GamesInProgress.curSlot);Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
        check(BalanceTuning.get(DENSITY)==0&&BalanceTuning.get(CURSEBOUND)==0,"actual disk save/load lost tuning");
        for(int depth:new int[]{1,6,11,16,21}){
            Dungeon.depth=depth;Level level=Dungeon.newLevel();Dungeon.switchLevel(level,-1);
            check(level.mobLimit()==0&&level.heaps.size>0,"population zero/guaranteed supplies in region "+depth);
        }
        BalanceTuning.set(DENSITY,200);BalanceTuning.set(FLOOR_LOOT,300);Dungeon.depth=2;
        Dungeon.switchLevel(Dungeon.newLevel(),-1);check(Dungeon.level.mobLimit()>=10,"higher population not applied");
        Bundle saved=new Bundle();Playtest.store(saved);Playtest.reset();Playtest.restore(saved);check(BalanceTuning.get(DENSITY)==200,"bundle persistence");
        com.badlogic.gdx.Preferences reloaded=new com.badlogic.gdx.backends.headless.HeadlessPreferences(
                com.badlogic.gdx.Gdx.files.absolute(System.getProperty("grimhollow.smokeOutput")+"/prefs/settings.xml"));
        SPDSettings.set(reloaded);Playtest.reset();check(BalanceTuning.get(DENSITY)==200,"preference file reload");
        Bundle corrupt=new Bundle(),tuning=new Bundle();corrupt.put("playtest",true);tuning.put("density",9999);
        for(BalanceTuning.Key key:BalanceTuning.Key.values())if(key.group==3)tuning.put(key.id(),0);
        corrupt.put("balance_tuning",tuning);Playtest.restore(corrupt);check(BalanceTuning.get(DENSITY)==200&&!BalanceTuning.customItemMix(),"corrupt save sanitization");
        Playtest.restore(new Bundle());check(!Playtest.enabled()&&Playtest.unranked()&&BalanceTuning.get(DENSITY)==200&&!Playtest.god(),"ordinary save adopts shared tuning without enabling tools");
        BalanceTuning.setShared(CURSEBOUND,0);Dungeon.init();
        check(!Playtest.enabled()&&Playtest.unranked()&&!Playtest.god()&&BalanceTuning.get(CURSEBOUND)==0,"new game keeps shared balance and requires explicit tools");
        BalanceTuning.reset();Playtest.restore(saved);
        check(Playtest.enabled()&&BalanceTuning.changedCount()==0&&BalanceTuning.get(DENSITY)==100,"old save resurrected reset settings");
        SPDSettings.put("balance_profile_v1","density=9999;removed_key=2;respawn=bad;weapon=0;armor=0;missile=0;wand=0;ring=0;artifact=0;potion=0;scroll=0;seed=0;stone=0;gold=0;");
        Playtest.reset();check(BalanceTuning.get(DENSITY)==200&&!BalanceTuning.customItemMix(),"damaged shared profile sanitization");
        BalanceTuning.reset();Dungeon.init();check(!Playtest.enabled()&&!Playtest.god(),"reset should permit ordinary future games");
        check(!Playtest.unranked(),"fresh defaults must permit a ranked game");
        BalanceTuning.setShared(DENSITY,125);Playtest.reset();
        Bundle ordinaryTuned=new Bundle();Playtest.store(ordinaryTuned);
        check(!ordinaryTuned.getBoolean("playtest")&&ordinaryTuned.getBoolean("custom_balance"),"save separates custom balance and explicit tools");
        BalanceTuning.reset();Playtest.restore(ordinaryTuned);
        check(!Playtest.enabled()&&!Playtest.god()&&Playtest.unranked(),"resetting balance must not rank an already customized run");
        Dungeon.saveAll();
        Bundle oldGame=com.watabou.utils.FileUtils.bundleFromFile(GamesInProgress.gameFile(GamesInProgress.curSlot));
        oldGame.remove("elemental_coin_plans");
        com.watabou.utils.FileUtils.bundleToFile(GamesInProgress.gameFile(GamesInProgress.curSlot),oldGame);
        Dungeon.loadGame(GamesInProgress.curSlot);Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
        check(Dungeon.hero.isAlive()&&Dungeon.level.insideMap(Dungeon.hero.pos),"legacy full save without elemental plans must load");
        reloaded.remove("balance_profile_v1");reloaded.flush();Playtest.restore(corrupt);
        check(Playtest.enabled()&&BalanceTuning.get(DENSITY)==200&&!BalanceTuning.customItemMix(),"legacy save migration/clamping");
        BalanceTuning.reset();Dungeon.init();
        GenerationToggles.reset();Generator.fullReset();
        GenerationToggles.toggle(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern.class);
        GenerationToggles.toggle(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.class);
        GenerationToggles.toggle(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision.class);
        GenerationToggles.toggle(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Hexcaster.class);
        GenerationToggles.toggle(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror.class);
        for(int draw=0;draw<100;draw++){
            check(!(Generator.random(Generator.Category.ARTIFACT) instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern),"disabled artifact returned after deck reset");
            check(!(Generator.random(Generator.Category.TRINKET) instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic),"disabled trinket returned");
            check(!(Generator.random(Generator.Category.POTION) instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision),"disabled potion returned across alternating decks");
            check(!(Generator.randomUsingDefaults(Generator.Category.POTION) instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision),"disabled default potion returned");
            check(!MobSpawner.getMobRotation(12).contains(Hexcaster.class),"disabled enemy rotation");
        }
        GenerationToggles.load();check(!GenerationToggles.allowed(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.class),"eligibility survives preference reload");
        for(Class<?> type:Generator.Category.ARMOR.classes)if(type!=PlateArmor.class && (type==ClothArmor.class || type==LeatherArmor.class || type==MailArmor.class || type==ScaleArmor.class))GenerationToggles.toggle(type);
        for(int region=0;region<5;region++)check(Generator.randomArmor(region) instanceof PlateArmor,"depth-weighted armor exclusion "+region);
        boolean protectedPool=false;try{GenerationToggles.toggle(PlateArmor.class);}catch(IllegalArgumentException expected){protectedPool=true;}
        check(protectedPool&&GenerationToggles.allowed(PlateArmor.class),"empty armor pool rejected atomically");
        GenerationToggles.reset();check(GenerationToggles.changedCount()==0,"reset restores eligibility");
        com.watabou.utils.Random.pushGenerator(77);long rosterSeed=com.watabou.utils.Random.Long();com.watabou.utils.Random.popGenerator();
        com.watabou.utils.Random.pushGenerator(77);GenerationToggles.enemyTypes();
        check(com.watabou.utils.Random.Long()==rosterSeed,"opening the enemy catalog must not consume gameplay RNG");com.watabou.utils.Random.popGenerator();
        check(!GenerationToggles.enemyTypes().contains(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo.class)
                &&!GenerationToggles.itemTypes().contains(Phylactery.class),"fixed bosses and class essentials are not misleading toggles");
        for(Class<?> type:new Class<?>[]{com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Hexcaster.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.RedShaman.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.BlueShaman.class})GenerationToggles.toggle(type);
        for(int draw=0;draw<200;draw++){
            java.util.ArrayList<Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob>> roster=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner.getMobRotation(11);
            check(!roster.isEmpty()&&roster.stream().allMatch(t->t==com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.PurpleShaman.class),"eligible shaman variant fallback "+draw);
        }
        GenerationToggles.reset();
        Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
        Waterskin skin=Dungeon.hero.belongings.getItem(Waterskin.class);if(skin==null){skin=new Waterskin();skin.collect();}
        skin.empty();Playtest.recharge(skin);check(skin.isFull(),"individual recharge fills Waterskin");
        skin.empty();Playtest.recharge();check(skin.isFull(),"bulk recharge fills Waterskin");
        check(com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.itemGroup(new FocusCrystal()).equals("Class items"),"Crystal class-item category");
        System.out.println("TEST 58 PASS: default seeded RNG; spawn/loot/quality/category boundaries; unique artifacts; generated five regions; disk persistence, shared new/old games, reset precedence and profile sanitization");
    }
}
