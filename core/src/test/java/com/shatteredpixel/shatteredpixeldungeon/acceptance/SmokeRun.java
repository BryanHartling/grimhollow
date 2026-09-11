// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.necromancer.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneRod;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.BoneWalls;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.util.Arrays;

/** Seeds the real generator and uses the upstream debug descent route, with saves each floor. */
public class SmokeRun {
    public static void main(String[] args) throws Exception {
        Path output=Path.of(System.getProperty("grimhollow.smokeOutput"));
        java.nio.file.Files.createDirectories(output);
        boolean upstream=Arrays.asList(args).contains("--upstream");
        String[] classes=upstream ? Arrays.stream(HeroClass.values()).map(Enum::name).toArray(String[]::new)
                : new String[]{"NECROMANCER","ENCHANTER","PSYCHIC"};
        for(String arg:args)if(arg.startsWith("--class="))classes=new String[]{arg.substring(8)};
        int failures=0;
        try (PrintWriter log=new PrintWriter(output.resolve("smoke.log").toFile())) {
            HeadlessApplicationConfiguration config=new HeadlessApplicationConfiguration();
            config.updatesPerSecond=-1;
            config.preferencesDirectory=output.resolve("prefs").toString();
            HeadlessApplication app=new HeadlessApplication(new ApplicationAdapter(){},config);
            new ShatteredPixelDungeon(null);
            Game.version=System.getProperty("grimhollow.version"); Game.versionCode=Integer.getInteger("grimhollow.versionCode");
            FileUtils.setDefaultFileProperties(Files.FileType.Absolute,output.resolve("saves").toString()+"/");
            for(String name:classes) for(int seed=0;seed<10;seed++) {
                try {
                    GamesInProgress.selectedClass=HeroClass.valueOf(name);
                    GamesInProgress.curSlot=99;
                    Dungeon.seed=seed;
                    Dungeon.init();
                    if(Dungeon.hero.heroClass!=GamesInProgress.selectedClass) throw new AssertionError("Wrong hero class");
                    for(int depth=1;depth<=6;depth++) {
                        Dungeon.depth=depth;
                        Level level=Dungeon.newLevel();
                        Dungeon.switchLevel(level,-1);
                        if(!Dungeon.level.insideMap(Dungeon.hero.pos)) throw new AssertionError("Invalid hero placement");
                        if(depth==1 && Dungeon.hero.heroClass==HeroClass.NECROMANCER)necromancerScenario();
                        if(depth==1 && Dungeon.hero.heroClass==HeroClass.ENCHANTER)enchanterScenario();
                        if(depth==1 && Dungeon.hero.heroClass==HeroClass.PSYCHIC)psychicScenario();
                        Dungeon.saveAll();
                        if(depth==6) {
                            Dungeon.loadGame(99);
                            Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
                            if(Dungeon.depth!=6) throw new AssertionError("Save/load depth mismatch");
                        }
                    }
                    if(seed==0){v4Scenario();contentScenario();}
                    String line="PASS "+name+" seed="+seed+" floor=6 save/load=ok";
                    System.out.println(line); log.println(line);
                } catch(Throwable error) {
                    failures++;
                    String line="FAIL "+name+" seed="+seed+": "+error;
                    System.out.println(line); log.println(line); error.printStackTrace(log);
                }
                log.flush();
            }
            app.exit();
            String result="Runs="+(classes.length*10)+" failures="+failures;
            System.out.println(result); log.println(result);
        }
        if(failures>0) System.exit(1);
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}

    private static void contentScenario() throws Exception {
        // Required content is exercised inside the established real-game smoke harness.
        Dungeon.init();Dungeon.depth=1;Dungeon.branch=0;Dungeon.switchLevel(Dungeon.newLevel(),-1);clearArena();
        Hero h=Dungeon.hero;for(Buff b:h.buffs())b.detach();h.belongings.weapon=null;h.belongings.armor=null;
        h.HT=h.HP=200;h.STR=30;
        int center=h.pos,w=Dungeon.level.width();
        Rat primary=target(center+2),side=target(center+2+w),other=target(center+2-w);
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneScythe bone=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneScythe();
        h.belongings.weapon=bone;check(bone.tier==2&&bone.min(0)==4&&bone.max(0)==14&&bone.reachFactor(h)==2,"Bone Scythe stats/reach");
        bone.proc(h,primary,20);check(side.HP==190&&other.HP==190,"Bone Scythe perpendicular sweep");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ReapersScythe reaper=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ReapersScythe();
        reaper.proc(h,primary,20);check(side.HP==175&&other.HP==175&&reaper.min(0)==8&&reaper.max(0)==28,"Reaper sweep/stats");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GraveScythe grave=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GraveScythe();
        NecroCurse.apply(primary,NecroCurse.Kind.AMPLIFY,5);int boosted=grave.proc(h,primary,20);
        check(boosted==22&&side.HP==153&&other.HP==153&&grave.min(0)==10&&grave.max(0)==36,"Grave curse bonus/sweep");
        for(Buff b:primary.buffs())b.detach();
        Weapon testWeapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger();
        h.belongings.weapon=testWeapon;testWeapon.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Echo());h.HP=200;primary.HP=200;
        check(h.attack(primary,1f,0f,Float.POSITIVE_INFINITY),"Echo attack hits");check(h.HP==200-Math.round((200-primary.HP)*.25f),"Echo reflects actual damage after mitigation");
        h.HP=200;primary.HP=200;testWeapon.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.DarkBlessing());NecroCurse.apply(primary,NecroCurse.Kind.AMPLIFY,5);
        check(h.attack(primary,1f,0f,Float.POSITIVE_INFINITY),"Dark Blessing attack hits");check(h.HP==200-Math.round((200-primary.HP)*.1f),"Dark Blessing recoil includes amplified damage");
        for(Buff buff:primary.buffs())buff.detach();h.belongings.weapon=bone;testWeapon.enchant(null);
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Leech leech=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Leech();
        int triggered=0;
        for(int i=0;i<100;i++){
            leech.proc(testWeapon,h,primary,20);
            com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Leech.Recovery recovery=primary.buff(com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Leech.Recovery.class);
            if(recovery!=null){primary.HP=100;recovery.recover(h,20);check(primary.HP==104,"Leech restores actual damage fraction");triggered++;}
        }
        check(triggered>10&&triggered<50,"Leech probabilistic proc");
        BoneArmor boneArmor=new BoneArmor();ScaleArmor scale=new ScaleArmor();
        check(boneArmor.tier==3&&boneArmor.DRMax(0)==scale.DRMax(0)+1&&boneArmor.DRMin(0)==scale.DRMin(0)+1,"Bone armor relative protection");
        check(boneArmor.evasionFactor(h,10)==scale.evasionFactor(h,10)-1,"Bone armor evasion penalty");
        com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering withering=new com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering();
        h.belongings.armor=new LeatherArmor().inscribe(withering);h.updateHT(false);int max=h.HT;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering.arrive(h);Dungeon.depth++;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering.arrive(h);check(h.HT==max-1,"Withering descent");
        Bundle armorSave=new Bundle();armorSave.put("armor",h.belongings.armor);h.belongings.armor=(Armor)armorSave.get("armor");h.updateHT(false);check(h.HT==max-1,"Withering survives serialization");
        h.belongings.armor.inscribe(null);check(h.HT==max,"Withering removal restores maximum health");
        boneArmor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering());h.belongings.armor=boneArmor;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering.arrive(h);Dungeon.depth++;com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering.arrive(h);check(h.HT==max,"Bone armor Withering immunity");
        armorSave=new Bundle();armorSave.put("armor",ClassArmor.upgrade(h,boneArmor));check(((Armor)armorSave.get("armor")).boneConstruction,"Bone construction survives crown and serialization");
        Armor dark=new LeatherArmor().inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.DarkBlessing());h.belongings.armor=dark;
        check(dark.DRMax()==new LeatherArmor().DRMax()+3&&dark.DRMin()==new LeatherArmor().DRMin()+3,"Dark armor +3 protection");
        h.HP=1;h.heal(10);check(h.HP==9,"Dark armor reduces burst healing");
        for(int i=0;i<5;i++)h.heal(1);check(h.HP==13,"Dark armor reduces one-point regeneration without rounding it away");
        h.belongings.armor=null;h.HT=h.HP=200;
        clearArena();primary=target(center+2);side=target(center+2+w);other=target(center+2+2*w);
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfNecrosis necrosis=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfNecrosis();necrosis.upgrade(2);
        necrosis.onZap(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(center,primary.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT));
        check(primary.buff(Corrosion.class)!=null&&side.buff(Corrosion.class)!=null&&other.buff(Corrosion.class)!=null,"Necrosis chains to two additional adjacent enemies at level 2");
        check(primary.buff(Corrosion.class).iconTextDisplay().equals("4")&&side.buff(Corrosion.class).iconTextDisplay().equals("3")&&other.buff(Corrosion.class).iconTextDisplay().equals("2"),"Necrosis minus one damage per hop");
        clearArena();primary=target(center+4);
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfGravity gravity=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfGravity();
        gravity.onZap(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(center,primary.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT));check(primary.pos==center+2,"Gravity pull two cells");
        side=target(center+1);com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfGravity.pull(primary,center,2,gravity);
        check(primary.pos==center+2&&primary.buff(Vertigo.class)!=null&&side.buff(Vertigo.class)!=null,"Gravity collision stops at blocker and gives both Vertigo");
        clearArena();Dungeon.depth=1;int wallCell=center+2;
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBone wandBone=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBone();
        wandBone.onZap(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(center,wallCell,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.STOP_TARGET));
        check(Dungeon.level.map[wallCell]==Terrain.BONE_WALL&&Dungeon.level.solid[wallCell]&&!Dungeon.level.passable[wallCell],"9: Wand of Bone blocks movement and LOS");
        check(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(center,center+3,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos==wallCell-1,"9: Wand of Bone stops bolts before the solid cell (upstream Ballistica)");
        for(int i=0;i<5;i++)h.buff(BoneWalls.class).act();check(Dungeon.level.map[wallCell]==Terrain.EMPTY,"9: Wand wall expires after five turns");
        BoneWalls.raise(wallCell,5);Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);h=Dungeon.hero;
        check(Dungeon.level.map[wallCell]==Terrain.EMPTY&&Dungeon.level.boneOriginal.keyArray().length==0,"9: Wand wall cleanup on real save/reload");
        BoneWalls.raise(wallCell,5);Level previousWallLevel=Dungeon.level;Dungeon.newLevel();check(previousWallLevel.map[wallCell]==Terrain.EMPTY&&previousWallLevel.boneOriginal.keyArray().length==0,"9: Wand of Bone reverts on actual level exit");Dungeon.switchLevel(previousWallLevel,h.pos);
        System.out.println("TEST 9 PASS: Wand of Bone and Bone Prison terrain blocks movement/bolts, expires and clears on level exit/save/load");
        clearArena();center=h.pos;w=Dungeon.level.width();h.HT=h.HP=200;
        Elemental.FireElemental fire=new Elemental.FireElemental();fire.pos=center+2;fire.sprite=new ElementalSprite.Fire(){@Override protected com.watabou.noosa.particles.Emitter createEmitter(){return new com.watabou.noosa.particles.Emitter();}};fire.sprite.link(fire);Dungeon.level.mobs.add(fire);Actor.add(fire);int before=fire.HP;
        com.shatteredpixel.shatteredpixeldungeon.items.spells.Soulfire soulfire=new com.shatteredpixel.shatteredpixeldungeon.items.spells.Soulfire();soulfire.ignite(fire.pos,h);
        com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Soulfire flame=(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Soulfire)Dungeon.level.blobs.get(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Soulfire.class);
        check(flame!=null&&flame.volume==45&&fire.buff(Terror.class)!=null,"Soulfire 3x3 and terror");flame.act();check(fire.HP<before,"Soulfire bypasses fire elemental immunity");
        java.util.ArrayList<Item> ingredients=new java.util.ArrayList<>(Arrays.asList(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame(),new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror()));
        for(Item ingredient:ingredients)ingredient.identify();
        com.shatteredpixel.shatteredpixeldungeon.items.spells.Soulfire.Recipe recipe=new com.shatteredpixel.shatteredpixeldungeon.items.spells.Soulfire.Recipe();check(recipe.testIngredients(ingredients)&&recipe.cost(ingredients)==6&&recipe.brew(ingredients) instanceof com.shatteredpixel.shatteredpixeldungeon.items.spells.Soulfire,"Soulfire alchemy recipe");
        clearArena();for(Buff b:h.buffs())b.detach();
        com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HourglassOfAshes ashes=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HourglassOfAshes();h.belongings.artifact=ashes;h.belongings.misc=null;ashes.activate(h);
        com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HourglassOfAshes.AshKeeper keeper=h.buff(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HourglassOfAshes.AshKeeper.class);
        for(int i=0;i<29;i++)keeper.act();check(ashes.charges()==0,"Ashes no premature charge");keeper.act();check(ashes.charges()==1,"Ashes charge at 30 turns");
        for(int i=0;i<300;i++)keeper.act();check(ashes.charges()==10,"Ashes charge cap");
        h.HT=h.HP=200;h.damage(20,new Corrosion());check(ashes.level()==1&&ashes.turnsPerCharge()==28,"Ashes upgrades from Corrosion actually taken");
        primary=target(center+2);side=target(center+3);Buff recent=Buff.prolong(primary,Bless.class,10),old=Buff.prolong(side,Bless.class,10);old.fixTime(5);Buff.prolong(primary,Weakness.class,10);
        h.spendConstant(1);float time=h.cooldown();check(ashes.rewind(h)&&h.cooldown()==time-1,"Ashes refunds last action");
        check(primary.buff(Bless.class)==null&&primary.buff(Weakness.class)!=null&&side.buff(Bless.class)!=null,"Ashes erases only recently gained positive buffs");
        check(!ashes.rewind(h),"Ashes cannot immediately chain");h.spendConstant(1);check(!ashes.rewind(h),"Refunded time cannot charge another rewind");h.spendConstant(1);check(ashes.rewind(h),"Next paid action can be refunded");
        Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);h=Dungeon.hero;
        ashes=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HourglassOfAshes)h.belongings.artifact;
        check(ashes.level()==1&&ashes.charges()==8&&!ashes.rewind(h),"Ashes charges/upgrade/no-chain state persist");
        clearArena();h.HT=h.HP=200;primary=target(h.pos+1);CursedVariant.apply(primary,NecroCurse.Kind.DECREPIFY);
        check(primary.HT==260&&primary.buff(CursedVariant.class)!=null&&NecroCurse.find(primary).kind==NecroCurse.Kind.DECREPIFY,"Cursed variant health and own curse");
        primary.attackProc(h,1);check(NecroCurse.find(h).kind==NecroCurse.Kind.DECREPIFY&&NecroCurse.find(h).remaining==5,"Cursed creature passes its curse on hit");
        Bundle saved=new Bundle();saved.put("mob",primary);Rat restored=(Rat)saved.get("mob");check(restored.HT==260&&restored.buff(CursedVariant.class).kind==NecroCurse.Kind.DECREPIFY,"Cursed variant serialization does not multiply stats");
        primary.buff(CursedVariant.class).drop();int loot=Dungeon.level.heaps.get(primary.pos).items.size();primary.buff(CursedVariant.class).drop();check(loot==1&&Dungeon.level.heaps.get(primary.pos).items.size()==1,"Cursed variant drops exactly one bonus item");
        clearArena();Hexcaster hex=new Hexcaster();check(hex.HT==60&&hex.lootChance()==.25f&&hex.createLoot() instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand,"Hexcaster stats/loot");hex.curse(h);check(NecroCurse.find(h).kind==NecroCurse.Kind.AMPLIFY,"Hexcaster first curse");saved=new Bundle();saved.put("mob",hex);hex=(Hexcaster)saved.get("mob");hex.curse(h);check(NecroCurse.find(h).kind==NecroCurse.Kind.DECREPIFY,"Hexcaster alternation persists");
        hex.pos=h.pos+1;hex.sprite=new HexcasterSprite();hex.sprite.link(hex);hex.sprite.visible=false;hex.state=hex.HUNTING;Dungeon.level.mobs.add(hex);Actor.add(hex);
        java.lang.reflect.Method mobAct=Mob.class.getDeclaredMethod("act");mobAct.setAccessible(true);int distance=Dungeon.level.distance(hex.pos,h.pos);mobAct.invoke(hex);check(Dungeon.level.distance(hex.pos,h.pos)>distance,"Hexcaster flees adjacent hero using actual AI");
        Dungeon.level.mobs.remove(hex);Actor.remove(hex);
        for(Buff b:h.buffs())if(b instanceof NecroCurse||b instanceof Slow||b instanceof Cripple)b.detach();
        Chainwarden boss=new Chainwarden();boss.pos=h.pos+4;boss.sprite=new ChainwardenSprite();boss.sprite.link(boss);Dungeon.level.mobs.add(boss);Actor.add(boss);
        check(boss.HT==new Tengu().HT&&boss.attackSkill(h)==new Tengu().attackSkill(h),"Chainwarden retains Tengu stats");int initial=h.pos;boss.advanceChains(3);check(h.pos==initial,"Chainwarden waits four turns");boss.advanceChains(1);check(h.pos==initial+2,"Chainwarden pulls two cells at fourth turn");
        com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChainTrap trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChainTrap();trap.set(h.pos);trap.activate();check(h.buff(Roots.class)!=null&&h.buff(Roots.class).cooldown()==2,"Chain Trap roots two turns");
        for(Class<?> kind:new Class<?>[]{com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfNecrosis.class,com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfGravity.class,com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBone.class}){int index=Arrays.asList(Generator.Category.WAND.classes).indexOf(kind);check(index>=0&&Generator.Category.WAND.defaultProbs[index]==3,"New wand generator weight");}
        check(Arrays.asList(Weapon.Enchantment.curses).contains(com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Echo.class)&&Arrays.asList(Armor.Glyph.curses).contains(com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Withering.class),"New curse pools");
        System.out.println("PASS CONTENT "+h.heroClass+": curses, healing, three scythes, armor/crown persistence, three wands, Soulfire recipe/immunity, Hourglass charge/upgrade/refund/save, cursed mob/drop, Hexcaster and Chainwarden");
    }
    private static void v4Scenario() throws Exception {
        // Exercise the merge boundary: actual City/Vault generation, serialized quest and
        // equipment exchange. Dialog navigation and a played boss fight are separate checks.
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.reset();
        Dungeon.depth=19; Dungeon.branch=0;
        Level city=Dungeon.newLevel(); Dungeon.switchLevel(city,-1);
        check(city.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp),"v4 Imp missing");
        check(!com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.isOld(),"v4 generated old Imp quest");
        check(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.rewardOptions.size()==6,"v4 quest rewards missing");
        check(city.transitions.stream().anyMatch(t->t.destBranch==1),"v4 Vault entrance missing");
        Dungeon.saveAll();
        Item originalArtifact=Dungeon.hero.belongings.artifact;
        int originalArtifactLevel=originalArtifact==null?-1:originalArtifact.level();
        int originalClassCharges=originalArtifact instanceof Phylactery?((Phylactery)originalArtifact).charges():originalArtifact instanceof ClassSpellItem?((ClassSpellItem)originalArtifact).charges():-1;
        int gold=Dungeon.gold,energy=Dungeon.energy;
        Dungeon.hero.live();
        switch(Dungeon.hero.heroClass){
            case NECROMANCER:check(Dungeon.hero.buff(Necromancy.class)!=null,"Vault cleared Necromancer state");break;
            case ENCHANTER:check(Dungeon.hero.buff(EnchanterMagic.class)!=null,"Vault cleared Enchanter state");break;
            case PSYCHIC:check(Dungeon.hero.buff(PsychicMind.class)!=null,"Vault cleared Psychic state");break;
        }
        com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal escape=new com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal();
        escape.storeHeroBelongings(Dungeon.hero);escape.collect();
        Dungeon.hero.belongings.armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();
        Dungeon.branch=1;
        Level vault=Dungeon.newLevel();Dungeon.switchLevel(vault,-1);
        check(vault instanceof com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel,"v4 wrong branch level");
        check(((com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel)vault).room(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom.class)!=null,"v4 final arena missing");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultMirror mirror=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultMirror)vault.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultMirror).findFirst().orElseThrow();
        check(mirror.reward!=null,"v4 mirror reward missing for "+Dungeon.hero.heroClass);
        Class<?> mirrorReward=mirror.reward.getClass();
        check(vault.heaps.valueList().stream().anyMatch(h->h.items.stream().anyMatch(i->i instanceof com.shatteredpixel.shatteredpixeldungeon.items.quest.ImpStatue)),"v4 final reward missing");
        Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
        check(Dungeon.branch==1&&Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel,"v4 Vault save/load failed");
        mirror=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultMirror)Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultMirror).findFirst().orElseThrow();
        check(mirror.reward!=null&&mirror.reward.getClass()==mirrorReward,"v4 mirror reward lost on reload");
        escape=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal.class);
        check(escape!=null&&escape.storedItems!=null,"v4 stored equipment lost");
        Dungeon.hero.live();escape.restoreHeroBelongings(Dungeon.hero,null);
        check(Dungeon.gold==gold&&Dungeon.energy==energy,"v4 currency restore failed");
        if(originalArtifact!=null)check(Dungeon.hero.belongings.artifact!=null&&Dungeon.hero.belongings.artifact.getClass()==originalArtifact.getClass()&&Dungeon.hero.belongings.artifact.level()==originalArtifactLevel,"v4 class item restore failed");
        Item restoredArtifact=Dungeon.hero.belongings.artifact;
        if(originalClassCharges>=0)check(originalClassCharges==(restoredArtifact instanceof Phylactery?((Phylactery)restoredArtifact).charges():((ClassSpellItem)restoredArtifact).charges()),"v4 class charges changed while stored");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.complete(4000);
        Bundle quest=new Bundle();com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.storeInBundle(quest);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.reset();
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.restoreFromBundle(quest);
        check(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.isCompleted()&&com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp.Quest.earnedShop(),"v4 completion/shop state lost");
        for(Weapon.Enchantment enchant:new Weapon.Enchantment[]{new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vorpal(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Venomous(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Crystal(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Eldritch(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Pressurized(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wondrous()}){
            Weapon weapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger();weapon.enchant(enchant);
            Bundle bundle=new Bundle();bundle.put("weapon",weapon);Weapon restored=(Weapon)bundle.get("weapon");
            check(restored.enchantment.getClass()==enchant.getClass(),"v4 enchantment serialization failed");
            if(Dungeon.hero.heroClass==HeroClass.ENCHANTER&&!enchant.curse()){
                weapon.identify();EnchanterMagic.learn(weapon);
                check(EnchanterMagic.state().choices(false).contains(enchant.getClass()),"v4 enchantment cannot be learned for inscription");
            }
        }
        System.out.println("PASS V4 "+Dungeon.hero.heroClass+" Vault generation/save/load, mirror="+mirrorReward.getSimpleName()+", equipment restore, quest completion/shop and six enchantment/curses serialized");
    }
    private static void clearArena(){
        if(com.watabou.noosa.Camera.main==null)com.watabou.noosa.Camera.main=new com.watabou.noosa.Camera(0,0,320,240,1);
        for(Mob m:Dungeon.level.mobs.toArray(new Mob[0])){Actor.remove(m);for(Buff buff:m.buffs())Actor.remove(buff);}
        Dungeon.level.mobs.clear();BoneWalls.clear(Dungeon.level);com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls.clear(Dungeon.level);
        int width=Dungeon.level.width(),center=width*(Dungeon.level.height()/2)+width/2;
        for(int y=-4;y<=4;y++)for(int x=-4;x<=4;x++){int cell=center+y*width+x;Level.set(cell,Terrain.EMPTY);Dungeon.level.traps.remove(cell);Dungeon.level.heaps.remove(cell);}
        Dungeon.level.cleanWalls();
        Dungeon.hero.pos=center;Dungeon.hero.sprite=new HeroSprite();Arrays.fill(Dungeon.level.heroFOV,true);
    }
    private static Rat target(int cell){
        Rat rat=new Rat();rat.HT=rat.HP=200;rat.EXP=0;rat.pos=cell;rat.sprite=new RatSprite();rat.sprite.link(rat);Dungeon.level.mobs.add(rat);Actor.add(rat);return rat;
    }
    private static void maxTalents(){for(java.util.Map<Talent,Integer> tier:Dungeon.hero.talents)for(Talent t:tier.keySet())tier.put(t,t.maxPoints());}
    private static void necromancerScenario() throws Exception {
        Hero h=Dungeon.hero;Phylactery item=h.belongings.getItem(Phylactery.class);
        check(h.HT==20&&h.HP==20&&h.STR==10,"Necromancer base stats");
        check(h.belongings.weapon instanceof BoneRod&&item!=null&&item.charges()==1&&item.cap()==3,"Necromancer equipment/charge kit");
        check(h.belongings.getItem(Food.class).quantity()==2&&h.belongings.getItem(PotionOfToxicGas.class).isIdentified()&&h.belongings.getItem(ScrollOfIdentify.class)!=null,"Necromancer bag kit");
        check(h.talents.get(0).size()==4&&h.talents.get(1).size()==5,"Necromancer talent tiers");
        clearArena();item.gainCharge(3);
        check(item.cast(h,Phylactery.Spell.RAISE_SKELETON,h.pos),"First skeleton");

        check(!item.cast(h,Phylactery.Spell.RAISE_SKELETON,h.pos)&&item.charges()==2,"Concurrent cap and no charge on failure");
        NecroSkeleton skeleton=NecroSkeleton.minions().get(0);check(skeleton.HT==19&&skeleton.remaining==30,"Skeleton starting stats/lifetime");
        skeleton.sprite=new NecroSkeletonSprite();skeleton.sprite.link(skeleton);
        for(int i=0;i<29;i++)skeleton.buff(NecroSkeleton.Lifetime.class).act();
        check(skeleton.isAlive(),"Minion lives through turn 29");skeleton.buff(NecroSkeleton.Lifetime.class).act();
        check(!NecroSkeleton.minions().contains(skeleton)&&h.HP==20,"Minion expires at turn 30 without explosion");
        clearArena();Rat enemy=target(h.pos+1);enemy.HP=1;enemy.damage(1,h);
        check(item.charges()==3&&Dungeon.level.corpses.get(enemy.pos)==200,"Hero kill charge and corpse tracking");
        // Acceptance 27: same equipped resource and scheduled keeper after a real kill, without further kills.
        int stable=item.charges();
        for(int turn=0;turn<300;turn++){h.buff(Phylactery.Keeper.class).act();item.charge(h,1);}
        check(item.charges()==stable,"27: no time or external artifact regeneration after 300 turns");
        enemy=target(h.pos+1);
        // One charge was already spent raising the first skeleton; spend eleven more.
        for(int i=0;i<11;i++){item.gainCharge(1);check(item.cast(h,Phylactery.Spell.WITHER,enemy.pos),"27: spell spending");}
        check(item.level()==1&&item.spells(h).contains(Phylactery.Spell.RAISE_WRAITH),"27: twelve charges unlock Wraith at level one");
        item.upgrade();item.transferUpgrade(10);check(item.level()==1,"Usage-only artifact growth");
        clearArena();NecroSkeleton hunter=NecroSkeleton.raise(h.pos+1,false,false);
        hunter.sprite=new NecroSkeletonSprite(){@Override public void showAlert(){} };hunter.sprite.link(hunter);hunter.sprite.visible=false;
        Rat victim=target(h.pos+2);victim.sprite.visible=false;
        Buff.prolong(victim,Paralysis.class,5);
        int victimHP=victim.HP;
        hunter.act();hunter.act();
        check(victim.HP<victimHP,"27: uncommanded minion attacks a visible sleeping hostile within two turns");
        hunter.defendPos(h.pos+1);hunter.act();check(hunter.pos==h.pos+1,"Hold command preserved");
        System.out.println("TEST 27 PASS: 300 turns unchanged; 12 charges level=1 Wraith offered; hostile attacked within 2 turns");
        clearArena();enemy=target(h.pos+1);
        while(item.level()<3){item.gainCharge(1);check(item.cast(h,Phylactery.Spell.WITHER,enemy.pos),"Growth to Ghoul");}
        // Exercise the two subclass sets, all new talent hooks and all class spells.
        h.lvl=21;h.HT=h.HP=120;h.subClass=HeroSubClass.DEATHSPEAKER;Talent.initSubclassTalents(h);maxTalents();
        check(item.cap()==5&&h.heroClass.subClasses().length==2,"Deathspeaker subclass and Grave Wisdom");
        item.gainCharge(20);check(item.cast(h,Phylactery.Spell.RAISE_GHOUL,h.pos),"Raise Ghoul");
        NecroSkeleton ghoul=NecroSkeleton.minions().get(0);check(ghoul instanceof NecroGhoul&&ghoul.HT==Math.round(156*1.15f*1.2f),"Ghoul and Sturdy Bones");
        ghoul.sprite=new GhoulSprite();ghoul.sprite.link(ghoul);
        ghoul.HP=1;Talent.onFoodEaten(h,100,new Food());check(ghoul.HP>1,"Bone Meal");
        enemy=target(h.pos+2);h.HP=50;ghoul.attackProc(enemy,10);check(h.HP==53,"Ghoul lifesteal");
        Talent.onAttackProc(h,enemy,5);check(enemy.buff(Corrosion.class)!=null,"Necrotic Touch");
        Buff.detach(enemy,Corrosion.class);h.belongings.thrownWeapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife();
        Talent.onAttackProc(h,enemy,5);check(enemy.buff(Corrosion.class)==null,"Necrotic Touch excludes thrown weapons");h.belongings.thrownWeapon=null;
        Buff.prolong(h,Bless.class,10);Buff.prolong(h,Haste.class,10);Buff.affect(h,Barkskin.class).setForDuration(10,10);
        // Second Grave never permits more than cap+1, and the exemption ends after 5 turns.
        for(NecroSkeleton m:NecroSkeleton.minions()){m.sacrificed=true;if(m.sprite==null){m.sprite=new NecroSkeletonSprite();m.sprite.link(m);}m.die(h);}
        NecroSkeleton first=NecroSkeleton.raise(h.pos-1,false,false);
        NecroSkeleton second=NecroSkeleton.raise(h.pos+Dungeon.level.width(),false,false);
        NecroSkeleton third=NecroSkeleton.raise(h.pos-Dungeon.level.width(),false,false);
        check(first!=null&&second!=null&&third!=null,"Level 21 minion cap");
        check(NecroSkeleton.raise(h.pos-2,false,false)!=null&&NecroSkeleton.raise(h.pos+Dungeon.level.width()-1,false,false)!=null,"Deathspeaker and Bone Legion each add one slot");
        NecroSkeleton extra=NecroSkeleton.raise(h.pos+Dungeon.level.width()+1,false,true);
        check(extra!=null&&NecroSkeleton.raise(h.pos-Dungeon.level.width()-1,false,true)==null,"Second Grave cap+1");
        extra.sprite=new NecroSkeletonSprite();extra.sprite.link(extra);
        for(int i=0;i<5;i++)extra.buff(NecroSkeleton.Lifetime.class).act();
        check(NecroSkeleton.minions().size()==5,"Second Grave exemption lasts five turns");
        for(NecroSkeleton m:NecroSkeleton.minions()){m.sprite=new NecroSkeletonSprite();m.sprite.link(m);}
        // Death Pact must suppress Second Grave while consuming minions.
        ClassArmor armor=ClassArmor.upgrade(h,new ClothArmor());h.armorAbility=new DeathPact();Talent.initArmorTalents(h);maxTalents();armor.charge=100;
        ((DeathPact)h.armorAbility).activate(armor,h,h.pos);check(NecroSkeleton.minions().isEmpty()&&h.buff(Adrenaline.class)!=null,"Death Pact sacrifice and buff");
        clearArena();enemy=target(h.pos+2);
        while(item.level()<6){item.gainCharge(1);check(item.cast(h,Phylactery.Spell.WITHER,enemy.pos),"Growth to Revenant");}
        item.gainCharge(20);check(item.cast(h,Phylactery.Spell.RAISE_REVENANT,h.pos),"Deathspeaker Revenant unlock");
        NecroSkeleton revenant=NecroSkeleton.minions().get(0);
        check(revenant.slots()==2&&revenant.remaining==50&&revenant.isImmune(Terror.class)&&revenant.isImmune(Amok.class),"Revenant slots lifetime immunities");
        item.gainCharge(20);check(!item.cast(h,Phylactery.Spell.RAISE_REVENANT,h.pos),"Only one Revenant");
        NecroGhoul rising=(NecroGhoul)NecroSkeleton.raise(h.pos-1,true,false);rising.HP=0;rising.die(h);check(rising.HP==rising.HT/2,"Ally-scoped Ghoul rises once");
        rising.sacrificed=true;rising.sprite=new GhoulSprite();rising.sprite.link(rising);rising.HP=0;rising.die(h);check(!NecroSkeleton.minions().contains(rising),"Sacrifice suppresses Ghoul rise");
        clearArena();h.subClass=HeroSubClass.HEXWEAVER;h.talents.clear();Talent.initClassTalents(h);Talent.initSubclassTalents(h);maxTalents();
        enemy=target(h.pos+1);
        for(Phylactery.Spell spell:new Phylactery.Spell[]{Phylactery.Spell.WITHER,Phylactery.Spell.AMPLIFY,Phylactery.Spell.DECREPIFY,Phylactery.Spell.IRON_MAIDEN,Phylactery.Spell.LOWER_RESISTANCE}){
            item.gainCharge(20);check(item.cast(h,spell,enemy.pos),"Cast "+spell);check(enemy.buffs(NecroCurse.class).size()==1,"One curse per enemy");
        }
        Buff.prolong(enemy,Invulnerability.class,10);check(!enemy.isInvulnerable(BoneRod.class),"Lower Resistance bypasses immunity");Buff.detach(enemy,Invulnerability.class);
        h.HP=10;h.buff(Necromancy.class).ward();check(h.buff(Barkskin.class)!=null,"Ward of Bone");
        Buff.detach(h,Barkskin.class);Dungeon.depth=2;h.buff(Necromancy.class).ward();Buff.detach(h,Barkskin.class);Dungeon.depth=1;h.buff(Necromancy.class).ward();
        check(h.buff(Barkskin.class)==null,"Ward of Bone cannot recharge by revisiting a floor");
        h.buff(Necromancy.class).siphon(enemy);int hp=h.HP;h.buff(Necromancy.class).siphon(enemy);check(h.HP==hp,"One Soul Siphon target per turn");
        // All armor talent sets and concrete target scenarios.
        h.armorAbility=new CorpseExplosion();h.talents.get(3).clear();Talent.initArmorTalents(h);maxTalents();armor.charge=100;Dungeon.level.corpses.put(h.pos+1,100);
        int oldHP=enemy.HP;((CorpseExplosion)h.armorAbility).activate(armor,h,h.pos+1);check(enemy.HP<oldHP&&Dungeon.level.corpses.get(h.pos+1)==null,"Corpse Explosion damage and consumption");
        h.armorAbility=new BonePrison();h.talents.get(3).clear();Talent.initArmorTalents(h);maxTalents();armor.charge=100;
        ((BonePrison)h.armorAbility).activate(armor,h,h.pos+2);check(Dungeon.level.boneOriginal.keyArray().length>0,"Bone Prison creates terrain");
        for(int cell:Dungeon.level.boneOriginal.keyArray())check(!Dungeon.level.passable[cell]&&Dungeon.level.losBlocking[cell],"Bone walls block movement and sight");
        for(int i=0;i<19;i++){BoneWalls walls=h.buff(BoneWalls.class);if(walls!=null)walls.act();}
        check(Dungeon.level.boneOriginal.keyArray().length==0,"Bone Prison timeout");
        clearArena();item.gainCharge(20);check(item.cast(h,Phylactery.Spell.RAISE_SKELETON,h.pos),"Saved minion");
        enemy=target(h.pos+2);NecroCurse.apply(enemy,NecroCurse.Kind.AMPLIFY,12);
        check(BoneWalls.prison(h.pos+3,10,1),"Saved prison");int[] walls=Dungeon.level.boneOriginal.keyArray();
        int charges=item.charges(),artifactLevel=item.level();Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
        h=Dungeon.hero;h.sprite=new HeroSprite();item=h.belongings.getItem(Phylactery.class);
        check(item.charges()==charges&&NecroSkeleton.minions().size()==1,"Minion and Phylactery save/load");
        check(item.level()==artifactLevel,"Usage-grown artifact level survives save/load");
        check(NecroSkeleton.minions().get(0).remaining==30,"Minion lifetime save/load");
        check(Dungeon.level.mobs.stream().anyMatch(m->NecroCurse.find(m)!=null),"Curse save/load");
        for(int cell:walls)check(Dungeon.level.map[cell]!=Terrain.BONE_WALL,"Bone Prison reverts on load");
        check(BoneWalls.prison(h.pos+3,10,1),"Exit prison");Level previous=Dungeon.level;Dungeon.newLevel();check(previous.boneOriginal.keyArray().length==0,"Bone Prison reverts on level exit");Dungeon.switchLevel(previous,h.pos);
        System.out.println("NECROMANCER kit, talents, subclasses, spells, armor, caps, and persistence: PASS");
    }

    private static void psychicScenario() throws Exception {
        Hero h=Dungeon.hero;FocusCrystal crystal=h.belongings.getItem(FocusCrystal.class);
        check(h.HP==20&&h.HT==20&&h.STR==10&&crystal!=null&&crystal.charges()==2&&crystal.cap()==3,"Psychic base kit");
        check(h.belongings.weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FocusRing&&h.belongings.armor instanceof ClothArmor&&h.belongings.getItem(Food.class).quantity()==2,"Psychic equipment and food");
        check(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife.class).quantity()==3&&h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision.class).isIdentified()&&new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping().isKnown(),"Psychic consumables and identification");
        check(h.talents.get(0).size()==4&&h.talents.get(1).size()==5,"Psychic talent tiers");
        clearArena();int center=h.pos,width=Dungeon.level.width();
        check(crystal.cast(h,"glimpse",h.pos,null)&&h.buff(MindVision.class)!=null,"Glimpse");
        for(int i=0;i<37;i++)h.buff(ClassSpellItem.Charger.class).act();check(crystal.charges()==1,"Crystal no early charge");h.buff(ClassSpellItem.Charger.class).act();check(crystal.charges()==2,"Crystal level-one cadence");
        for(int level:new int[]{1,5,10,30}){
            h.lvl=level;com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife knife=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife();
            com.watabou.utils.Random.pushGenerator(12345);int psychic=knife.damageRoll(h);com.watabou.utils.Random.popGenerator();
            h.heroClass=HeroClass.HUNTRESS;com.watabou.utils.Random.pushGenerator(12345);int base=knife.damageRoll(h);com.watabou.utils.Random.popGenerator();h.heroClass=HeroClass.PSYCHIC;
            check(psychic-base==level/5,"12: exact Telekinetic Force at level "+level);
        }
        h.lvl=21;h.HT=h.HP=120;h.subClass=HeroSubClass.PUPPETEER;Talent.initSubclassTalents(h);maxTalents();
        check(crystal.cap()==7,"Focused Mind capacity");
        int heapCell=center+2;Dungeon.level.drop(new Food(),heapCell);Rat enemy=target(heapCell);
        com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap().reveal();Dungeon.level.setTrap(trap,heapCell);Level.set(heapCell,Terrain.TRAP);
        int food=h.belongings.getItem(Food.class).quantity();crystal.gainCharge(10);
        check(crystal.cast(h,"grasp",heapCell,null),"11: cast Grasp on visible heap");
        check(h.belongings.getItem(Food.class).quantity()==food+1&&Dungeon.level.heaps.get(heapCell)==null,"11: Grasp collects heap");
        check(trap.active&&enemy.buff(Vertigo.class)!=null,"11: Grasp heap priority and Wrench");
        Buff.detach(enemy,Vertigo.class);int oldHP=enemy.HP;
        check(crystal.cast(h,"grasp",heapCell,null)&&!trap.active&&Dungeon.level.traps.get(heapCell)==null,"11: Grasp triggers and removes trap");
        com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas gas=(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas)Dungeon.level.blobs.get(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas.class);gas.act();
        check(oldHP-enemy.HP==Math.round(1.5f*(1+Dungeon.scalingDepth()/5)),"Trap Sense scales remote hazard damage");
        int charge=crystal.charges();check(!crystal.cast(h,"grasp",center-2,null)&&crystal.charges()==charge,"11: empty Grasp is free");
        clearArena();enemy=target(center+1);Rat rival=target(center+2);
        Buff.prolong(h,Bless.class,10);Buff.prolong(h,Haste.class,10);Buff.affect(h,Barkskin.class).setForDuration(8,10);
        crystal.gainCharge(10);check(crystal.cast(h,"dominate",enemy.pos,null)&&enemy.buff(Amok.class).dominated&&enemy.buff(Bless.class)!=null&&enemy.buff(Haste.class)!=null&&Barkskin.currentLevel(enemy)==8,"Dominate and Shared Will");
        enemy.sprite=new RatSprite(){@Override public void showAlert(){}};enemy.sprite.link(enemy);enemy.sprite.visible=false;enemy.state=enemy.HUNTING;rival.sprite.visible=false;Buff.prolong(rival,Paralysis.class,5);
        // Isolate the AI assertion from a rat's legal 1-damage minus 1-armor roll.
        Buff.prolong(rival,FracturedArmor.class,5);
        java.lang.reflect.Method act=Mob.class.getDeclaredMethod("act");act.setAccessible(true);int rivalHP=rival.HP;act.invoke(enemy);
        check(enemy.isTargeting(rival)&&rival.HP<rivalHP,"13: dominated enemy attacks another hostile instead of adjacent hero");
        check(enemy.buff(Amok.class).cooldown()>=15,"Dominate fifteen-turn duration");
        for(int turn=1;turn<15;turn++){act.invoke(enemy);check(enemy.isTargeting(rival),"13: domination continues to prefer the hostile");}
        crystal.gainCharge(-crystal.charges());rival.HP=1;rival.damage(1,enemy);check(crystal.charges()==1,"Harvest Thought kill charge");
        crystal.gainCharge(10);check(crystal.cast(h,"suggestion",enemy.pos,null)&&enemy.buff(Terror.class)!=null,"Suggestion");Buff.detach(enemy,Terror.class);
        Buff.prolong(h,MindVision.class,5);float vision=h.buff(MindVision.class).cooldown();Rat killed=target(center+3);killed.HP=1;killed.damage(1,h);check(h.buff(MindVision.class).cooldown()==vision+2,"Lingering Sight");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon knives=h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife.class);
        Buff.prolong(enemy,Hex.class,5);check(knives.proc(h,enemy,10)==14,"Fracture Point");
        float acc=knives.accuracyFactor(h,enemy);h.talents.get(1).put(Talent.GUIDED_THROW,0);float baseAcc=knives.accuracyFactor(h,enemy);h.talents.get(1).put(Talent.GUIDED_THROW,2);check(Math.abs(acc/baseAcc-1.2f)<.001f,"Guided Throw");
        java.lang.reflect.Field curUser=Item.class.getDeclaredField("curUser");curUser.setAccessible(true);curUser.set(null,h);
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon thrown=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)knives.detach(h.belongings.backpack);
        float durability=knives.durabilityLeft();java.lang.reflect.Method land=com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon.class.getDeclaredMethod("rangedHit",Char.class,int.class);land.setAccessible(true);land.invoke(thrown,enemy,enemy.pos);
        check(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife.class).quantity()==3&&h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife.class).durabilityLeft()<durability,"Recall returns the thrown knife and consumes durability");
        Buff.detach(h,Barkskin.class);h.HP=35;h.damage(10,enemy);check(h.HP==35,"Precognition dodges threshold hit");h.damage(10,enemy);check(h.HP==25,"Precognition only once on this floor");h.HP=120;
        int dominatedCell=enemy.pos;Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);h=Dungeon.hero;h.sprite=new HeroSprite();crystal=h.belongings.getItem(FocusCrystal.class);
        enemy=(Rat)Dungeon.level.findMob(dominatedCell);check(enemy!=null&&enemy.buff(Amok.class)!=null&&enemy.buff(Amok.class).dominated,"14: domination survives save/load");check(!PsychicMind.state().dodge(110,enemy),"Precognition expenditure survives save/load");
        clearArena();Buff.detach(h,MindVision.class);Buff.detach(h,Bless.class);Buff.detach(h,Haste.class);
        crystal.gainCharge(-crystal.charges());for(int i=0;i<16;i++)h.buff(ClassSpellItem.Charger.class).act();check(crystal.charges()==1,"Deep Focus accelerates calm regeneration");
        h.subClass=HeroSubClass.SEER;h.talents.clear();Talent.initClassTalents(h);Talent.initSubclassTalents(h);maxTalents();
        for(int y=-4;y<=4;y++)Level.set(center+1+y*width,Terrain.WALL);
        enemy=target(center+2);Rat distant=target(center+4);int secret=center+2+width;Level.set(secret,Terrain.SECRET_DOOR);
        trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap().hide();Dungeon.level.setTrap(trap,center+2-width);Level.set(trap.pos,Terrain.SECRET_TRAP);
        PsychicMind.state().act();Dungeon.observe();check(h.buff(SeerSight.class)!=null&&Dungeon.level.heroFOV[enemy.pos]&&!Dungeon.level.heroFOV[distant.pos]&&Dungeon.level.map[secret]==Terrain.DOOR&&trap.visible,"Seer radius-three sight and secrets through walls");
        clearArena();enemy=target(center-2+width);crystal.gainCharge(10);check(crystal.cast(h,"hurl",enemy.pos,center-1+width)&&enemy.pos==center+2+width,"Hurl plus Heavy Hand distance");
        Level.set(center+4+width,Terrain.WALL);crystal.gainCharge(10);check(crystal.cast(h,"hurl",enemy.pos,center+3+width)&&enemy.pos==center+3+width&&enemy.buff(Paralysis.class)!=null,"Hurl wall impact");
        clearArena();enemy=target(center-2+width);trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap().reveal();Dungeon.level.setTrap(trap,center+2+width);Level.set(trap.pos,Terrain.TRAP);crystal.gainCharge(10);check(crystal.cast(h,"hurl",enemy.pos,center-1+width)&&enemy.pos==trap.pos&&!trap.active,"Hurl landing triggers trap");
        clearArena();int revealCell=center+3;Dungeon.level.drop(new Food(),revealCell);trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap().hide();Dungeon.level.setTrap(trap,revealCell+width);Level.set(trap.pos,Terrain.SECRET_TRAP);
        crystal.gainCharge(-crystal.charges());Dungeon.depth=2;PsychicMind.state().arrive();check(crystal.charges()==2&&Dungeon.level.heaps.get(revealCell).seen&&trap.visible,"Kinetic Reserve and Treasure Sense");crystal.gainCharge(-2);PsychicMind.state().arrive();check(crystal.charges()==0,"No floor-entry recharge exploit");Dungeon.depth=1;
        clearArena();enemy=target(center+2);Rat other=target(center+2+width);ClassArmor armor=ClassArmor.upgrade(h,new ClothArmor());
        h.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic.PsychicStorm();Talent.initArmorTalents(h);maxTalents();armor.charge=100;((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic.PsychicStorm)h.armorAbility).activate(armor,h,h.pos);
        check(enemy.buff(Amok.class)!=null&&enemy.buff(Vertigo.class)!=null&&enemy.buff(Terror.class)!=null&&crystal.charges()==2,"Psychic Storm, Dread and Backlash");
        h.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic.MindMeld();h.talents.get(3).clear();Talent.initArmorTalents(h);maxTalents();armor.charge=100;((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic.MindMeld)h.armorAbility).activate(armor,h,h.pos);
        check(h.buff(MindVision.class).cooldown()>=50&&h.buff(MeldedMind.class)!=null&&PsychicMind.thrownDamage(h,10)==21,"Mind Meld duration and Kinetic Surge");for(boolean mapped:Dungeon.level.mapped)check(mapped,"Total Sight");
        clearArena();h.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic.ForceWall();h.talents.get(3).clear();Talent.initArmorTalents(h);armor.charge=100;
        ((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.psychic.ForceWall)h.armorAbility).activate(armor,h,center+2);
        check(Dungeon.level.forceOriginal.keyArray().length==3,"Force Wall creates three cells");for(int cell:Dungeon.level.forceOriginal.keyArray())check(!Dungeon.level.passable[cell]&&!Dungeon.level.losBlocking[cell],"Force Wall blocks movement but not LOS");
        knives=h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife.class);check(knives.throwPos(h,center+4)==center+1&&!com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls.heroPasses(center+2),"Force Wall blocks ordinary throws and hero movement");
        maxTalents();check(knives.throwPos(h,center+4)==center+4&&com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls.heroPasses(center+2),"Permeable throws and walking");
        enemy=target(center+3);enemy.move(center+2);check(enemy.pos==center+3&&enemy.buff(Paralysis.class)!=null,"Repulse blocks and paralyzes");
        for(int i=0;i<8;i++)h.buff(com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls.class).act();check(Dungeon.level.forceOriginal.keyArray().length==0,"Force Wall eight-turn timeout");
        check(com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls.line(center+2,17),"Held Firm wall");int[] walls=Dungeon.level.forceOriginal.keyArray();Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);h=Dungeon.hero;h.sprite=new HeroSprite();for(int cell:walls)check(Dungeon.level.map[cell]!=Terrain.FORCE_WALL,"Force Wall restores on load");
        check(com.shatteredpixel.shatteredpixeldungeon.levels.features.ForceWalls.line(center+2,17),"Exit wall");Level previous=Dungeon.level;Dungeon.newLevel();check(previous.forceOriginal.keyArray().length==0,"Force Wall restores on floor exit");Dungeon.switchLevel(previous,h.pos);
        System.out.println("PSYCHIC kit, talents, subclasses, Grasp, thrown damage, domination, armor and persistence: PASS; TESTS 11-13 PASS");
    }

    private static void runecraftScenario(Hero h,SigilBrush brush,com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon w)throws Exception {
        clearArena();
        for(HeroSubClass subclass:new HeroSubClass[]{HeroSubClass.NONE,HeroSubClass.ARTIFICER}){
            h.subClass=subclass;w.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic());
            com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment scroll=new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment();scroll.collect();
            Runecraft.Offer canceled=scroll.runecraftOffer(h,w);int count=subclass==HeroSubClass.ARTIFICER?3:2;
            check(canceled.options().size()==count,"33: exact option count "+subclass);
            java.util.Set<Class<?>> types=new java.util.HashSet<>();for(Object option:canceled.options()){check(option.getClass()!=w.enchantment.getClass(),"33: excludes current enchantment");types.add(option.getClass());}
            check(types.size()==count,"33: distinct choices");canceled.cancel();check(h.belongings.contains(scroll)&&!canceled.apply(0),"33: cancel preserves scroll");
            Runecraft.Offer chosen=scroll.runecraftOffer(h,w);Class<?> selected=chosen.options().get(0).getClass();check(chosen.apply(0)&&w.enchantment.getClass()==selected&&!h.belongings.contains(scroll),"33: choice applies and consumes scroll");
        }
        h.subClass=HeroSubClass.SCRIVENER;
        w.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing());EnchanterMagic.learn(w);
        w.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic());
        for(int i=0;i<3;i++){brush.gainCharge(10);check(brush.cast(h,"inscribe",h.pos,w,com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing.class),"33: inscribe Blazing");}
        com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment scroll=new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment();scroll.collect();
        Runecraft.Offer favorite=scroll.runecraftOffer(h,w);check(favorite.options().size()==2&&favorite.options().stream().anyMatch(e->e instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing),"33: Scrivener favors three Blazing inscriptions");favorite.cancel();
        com.watabou.utils.Bundle stored=new com.watabou.utils.Bundle();EnchanterMagic.state().storeInBundle(stored);EnchanterMagic restored=new EnchanterMagic();restored.restoreFromBundle(stored);check(restored.favorite(false,w.enchantment.getClass())==com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing.class,"33: inscription history persists");
        com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment stone=new com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment();stone.collect();
        Runecraft.Offer glyphs=stone.runecraftOffer(h,h.belongings.armor);check(glyphs.options().size()==2&&glyphs.options().get(0) instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph,"33: crafted stone offers armor glyphs");glyphs.cancel();check(h.belongings.contains(stone),"33: cancel preserves stone");
        w.cursed=true;check(scroll.runecraftOffer(h,w)==null&&w.cursed&&h.belongings.contains(scroll),"33: Runecraft does not cleanse curses");w.cursed=false;
        scroll.detachAll(h.belongings.backpack);stone.detachAll(h.belongings.backpack);w.enchant(null);w.inscribed=null;w.inscriptionTurns=0;h.subClass=HeroSubClass.NONE;brush.gainCharge(10);
        System.out.println("TEST 33 PASS: scroll and stone offers, distinct exclusions, cancel/apply, Artificer, Scrivener and history persistence");
    }
    private static void enchanterScenario() throws Exception {
        Hero h=Dungeon.hero;SigilBrush brush=h.belongings.getItem(SigilBrush.class);
        check(h.HT==20&&h.STR==10&&brush!=null&&brush.charges()==2,"Enchanter base kit");
        check(h.belongings.weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunedBaton&&h.belongings.getItem(Food.class).quantity()==2,"Baton and rations");
        check(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment.class)==null&&new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment().isKnown()&&h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify.class)!=null&&h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class)!=null,"Enchanter consumables");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon starter=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)h.belongings.weapon;
        RuneEtching rune=starter.runeEtching;starter.upgrade();
        check(starter.actions(h).contains(Item.AC_DROP)&&starter.value()>0,"37: ordinary sellable starter");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword replacement=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword();
        replacement.identify();replacement.collect();check(replacement.doEquip(h),"37: replace starting weapon");
        int chargesBefore=brush.charges();check(RuneEtching.etch(h)&&brush.charges()==chargesBefore&&replacement.runeEtching==rune&&starter.runeEtching==null&&starter.level()==0&&replacement.level()==1,"37: transfer exactly one upgrade without charge");
        replacement.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing());
        Dungeon.depth=2;EnchanterMagic.state().arrive();check(Arrays.asList(RuneEtching.FLOOR_ENCHANTS).contains(rune.floorEnchant.getClass())&&replacement.enchantment instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing,"37: replacement rolls floor rune beside permanent enchant");Dungeon.depth=1;EnchanterMagic.state().arrive();
        replacement.doUnequip(h,true);replacement.detachAll(h.belongings.backpack);
        check(h.belongings.getItem(RuneEtching.class)==rune&&replacement.runeEtching==null&&replacement.level()==0,"37: lost carrier returns rune");
        starter.doEquip(h);check(RuneEtching.etch(h)&&starter.level()==1&&starter.runeEtching==rune,"37: reattach after carrier loss");
        check(!rune.actions(h).contains(Item.AC_DROP),"37: rune cannot be dropped");
        System.out.println("TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment");
        runecraftScenario(h,brush,starter);
        clearArena();Rat enemy=target(h.pos+1);enemy.sprite.visible=false;
        check(brush.cast(h,"hex",enemy.pos,null,null)&&brush.charges()==1&&enemy.buff(DegradedGear.class)!=null&&enemy.buff(Hex.class)!=null,"Hex Sigil");
        for(int i=0;i<37;i++)h.buff(ClassSpellItem.Charger.class).act();check(brush.charges()==1,"No early Brush charge");h.buff(ClassSpellItem.Charger.class).act();check(brush.charges()==2,"Brush level-one cadence");
        h.lvl=21;h.HT=h.HP=120;h.subClass=HeroSubClass.ARTIFICER;Talent.initSubclassTalents(h);maxTalents();
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon w=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)h.belongings.weapon;
        w.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic());w.identify();
        EnchanterMagic.learn(w);check(EnchanterMagic.state().choices(false).contains(com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic.class),"Identified-run inscription knowledge");
        brush.gainCharge(10);check(brush.cast(h,"inscribe",h.pos,w,com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic.class),"Inscribe selected known enchantment");
        check(w.inscribed!=null&&w.enchantment!=null&&w.inscriptionTurns==50,"Temporary and permanent sigils coexist; Steady Hand");
        brush.gainCharge(10);check(brush.cast(h,"reinforce",h.pos,w,null)&&w.buffedLvl()==w.level()+1&&w.reinforceFlat==3,"Reinforce and Master Craft");
        Class<?> old=w.enchantment.getClass();brush.gainCharge(10);Runecraft.Offer transmute=brush.transmuteOffer(h,w);check(transmute!=null&&transmute.apply(0)&&w.enchantment.getClass()!=old&&Arrays.asList(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment.common).contains(w.enchantment.getClass()),"Transmute different same-rarity enchantment");
        // Proc both sigils through the real weapon path, using Kinetic to avoid GL-only visual effects in headless mode.
        w.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic());w.runeEtching.floorEnchant=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic();
        w.proc(h,enemy,5);check(h.buff(com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic.KineticTracker.class)!=null,"Inscription proc hook");
        EnchanterMagic magic=EnchanterMagic.state();for(int i=0;i<4;i++)magic.act();check(h.buff(Barkskin.class)!=null,"Warding Sigils");
        int turns=w.inscriptionTurns;Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);h=Dungeon.hero;h.sprite=new HeroSprite();brush=h.belongings.getItem(SigilBrush.class);w=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)h.belongings.weapon;
        check(w.inscribed!=null&&w.inscriptionTurns==turns&&w.reinforceTurns>0&&w.reinforceFlat==3,"Inscription and Reinforce save/load");
        clearArena();enemy=target(h.pos+1);
        for(int i=0;i<50;i++)EnchanterMagic.state().act();check(w.inscribed==null&&w.reinforceTurns==0&&w.buffedLvl()==w.level(),"Temporary effects expire");
        h.subClass=HeroSubClass.SCRIVENER;h.talents.clear();Talent.initClassTalents(h);Talent.initSubclassTalents(h);maxTalents();
        brush.gainCharge(10);check(brush.cast(h,"sanctify",h.pos,null,null)&&h.buff(Bless.class)!=null&&h.buff(Haste.class)!=null,"Sanctify");
        Buff.prolong(enemy,Bless.class,10);brush.gainCharge(10);check(brush.cast(h,"nullify",enemy.pos,null,null)&&enemy.buff(Bless.class)==null&&enemy.buff(Silenced.class)!=null,"Nullify and Silence");
        brush.gainCharge(10);check(brush.cast(h,"fracture",enemy.pos,null,null)&&EnchanterMagic.armorRoll(enemy)==0,"Fracture");
        ClassArmor armor=ClassArmor.upgrade(h,new ClothArmor());
        h.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter.Overcharge();Talent.initArmorTalents(h);maxTalents();armor.charge=100;((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter.Overcharge)h.armorAbility).activate(armor,h,h.pos);
        check(h.buff(Overcharged.class)!=null&&EnchanterMagic.procChance(h,.2f)==1.75f,"Overcharge forces procs with Amplified strength");
        h.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter.Sanctuary();h.talents.get(3).clear();Talent.initArmorTalents(h);maxTalents();armor.charge=100;h.HP=60;
        ((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter.Sanctuary)h.armorAbility).activate(armor,h,h.pos);
        com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SanctuaryZone zone=(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SanctuaryZone)Dungeon.level.blobs.get(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SanctuaryZone.class);zone.act();zone.act();check(h.HP==61&&enemy.buff(Slow.class)!=null&&enemy.buff(Corrosion.class)!=null,"Sanctuary healing and enemy effects");
        h.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter.Unmaking();h.talents.get(3).clear();Talent.initArmorTalents(h);maxTalents();armor.charge=100;
        ((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.enchanter.Unmaking)h.armorAbility).activate(armor,h,enemy.pos);
        check(enemy.buff(Unmade.class)!=null&&enemy.buff(Vulnerable.class)!=null&&enemy.buff(Cripple.class)!=null&&h.HP>61,"Unmaking and Reclamation");
        enemy.damage(enemy.HP,h);check(brush.charges()>0,"Salvage hook");
        System.out.println("ENCHANTER kit, talents, subclasses, inscriptions, armor, and persistence: PASS");
    }
}
