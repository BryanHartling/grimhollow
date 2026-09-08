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
            Game.version="0.2.0"; Game.versionCode=896;
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
        Dungeon.level.mobs.clear();BoneWalls.clear(Dungeon.level);
        int width=Dungeon.level.width(),center=width*(Dungeon.level.height()/2)+width/2;
        for(int y=-4;y<=4;y++)for(int x=-4;x<=4;x++){int cell=center+y*width+x;Level.set(cell,Terrain.EMPTY);Dungeon.level.traps.remove(cell);Dungeon.level.heaps.remove(cell);}
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
}
