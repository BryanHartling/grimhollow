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
            Game.version="0.3.0"; Game.versionCode=899;
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
        replacement.doUnequip(h,true);replacement.detachAll(h.belongings.backpack);
        check(h.belongings.getItem(RuneEtching.class)==rune&&replacement.runeEtching==null&&replacement.level()==0,"37: lost carrier returns rune");
        starter.doEquip(h);check(RuneEtching.etch(h)&&starter.level()==1&&starter.runeEtching==rune,"37: reattach after carrier loss");
        check(!rune.actions(h).contains(Item.AC_DROP),"37: rune cannot be dropped");
        System.out.println("TEST 37 PASS: transfer, upgrade, replacement, carrier loss and reattachment");
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
        Class<?> old=w.enchantment.getClass();brush.gainCharge(10);check(brush.cast(h,"transmute",h.pos,w,null)&&w.enchantment.getClass()!=old&&Arrays.asList(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment.common).contains(w.enchantment.getClass()),"Transmute different same-rarity enchantment");
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
