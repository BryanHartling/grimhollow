// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.*;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.*;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Actual AI, perception and save contracts in the existing headless acceptance gate. */
final class HorrorScenario {
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError("Horror: "+message); }
    private static void act(LurkingHorror h) throws Exception {
        Method act=LurkingHorror.class.getDeclaredMethod("act"); act.setAccessible(true); act.invoke(h);
    }
    private static LurkingHorror fresh(int depth) {
        Dungeon.init(); Dungeon.switchLevel(Dungeon.newLevel(),-1); SmokeRun.clearArena();
        for(int cell=0;cell<Dungeon.level.length();cell++) if(Dungeon.level.insideMap(cell)) Level.set(cell,Terrain.EMPTY);
        Dungeon.level.cleanWalls();
        Dungeon.depth=depth; Dungeon.hero.HP=Dungeon.hero.HT=200; Dungeon.hero.belongings.armor=null;
        Dungeon.hero.fieldOfView=Dungeon.level.heroFOV;
        LurkingHorror h=new LurkingHorror(); h.pos=Dungeon.hero.pos+1; h.sprite=h.sprite(); h.sprite.link(h);
        Dungeon.level.mobs.add(h); Actor.add(h); return h;
    }
    // The native renderer verifies Hero.ready's actual UI/input hook; the headless app has no AttackIndicator.
    private static void ready() { LurkingHorror.onHeroReady(); }
    static void run() throws Exception {
        LurkingHorror h=fresh(2); int hp=Dungeon.hero.HP;
        Dungeon.hero.curAction=new HeroAction.Move(Dungeon.hero.pos+8); Dungeon.hero.resting=true;
        act(h);
        check(h.phase()==LurkingHorror.Phase.WARNING && Dungeon.hero.HP==hp,"warning must precede attack");
        check(Dungeon.hero.curAction==null && !Dungeon.hero.resting && Dungeon.hero.lastAction==null,"warning fails to stop automation");
        for(int i=0;i<4;i++)act(h);
        check(Dungeon.hero.HP==hp,"slow action consumed warning response");
        ready(); act(h);
        check(Dungeon.hero.HP==hp,"ready without an action was treated as a response");
        Dungeon.hero.spend(0); act(h); check(Dungeon.hero.HP==hp,"free action consumed response");
        Bundle saved=new Bundle(); saved.put("h",h); LurkingHorror copy=(LurkingHorror)saved.get("h");
        check(copy.phase()==h.phase() && copy.healedTotal()==h.healedTotal(),"phase save round trip");
        Dungeon.hero.spend(1); act(h);
        check(Dungeon.hero.HP<hp && h.phase()==LurkingHorror.Phase.FLEEING,"responded warning did not resolve");
        check(hp-Dungeon.hero.HP<=6,"Sewers burst exceeds reduced damage range");

        h=fresh(2); act(h); LurkingHorror.onHeroReady(); LurkingHorror.onHeroSpent(1);
        Dungeon.hero.pos+=Dungeon.level.width(); // Still adjacent: attack follows the hero, not a committed cell.
        hp=Dungeon.hero.HP; act(h); check(Dungeon.hero.HP<hp,"ambush incorrectly committed to old cell");
        h=fresh(2); act(h); Dungeon.hero.invisible=1; hp=Dungeon.hero.HP; act(h);
        check(Dungeon.hero.HP==hp && h.phase()==LurkingHorror.Phase.FLEEING,"invisibility must cancel ambush");
        h=fresh(2); act(h); Rat rat=new Rat(); rat.pos=Dungeon.hero.pos+2; Dungeon.level.mobs.add(rat);
        hp=Dungeon.hero.HP; act(h); check(Dungeon.hero.HP==hp && h.phase()==LurkingHorror.Phase.FLEEING,"another sleeping hostile failed to cancel");
        Dungeon.level.mobs.remove(rat);

        h=fresh(2); h.pos=Dungeon.hero.pos+8;
        Arrays.fill(Dungeon.level.visited,false); Arrays.fill(Dungeon.level.mapped,false);
        Dungeon.hero.viewDistance=2; Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        boolean[] ordinary=Dungeon.level.heroFOV.clone();
        Buff.affect(Dungeon.hero,MindVision.class,10); Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        check(h.sensed() && h.phase()==LurkingHorror.Phase.FLEEING,"Mind Vision did not expose and cancel stealth");
        check(Arrays.equals(ordinary,Dungeon.level.heroFOV),"Horror Mind Vision exposed terrain");
        for(boolean mapped:Dungeon.level.mapped)check(!mapped,"Horror mapped terrain");
        check(Actor.findChar(h.pos)==h,"concealment removed collision occupancy");
        Buff.detach(Dungeon.hero,MindVision.class); Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        check(!h.sensed() && !h.shadowmelded(),"ending detection immediately restored shadowmeld");
        TalismanOfForesight.CharAwareness aware=Buff.append(Dungeon.hero,TalismanOfForesight.CharAwareness.class,10);
        aware.charID=h.id(); Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        check(h.sensed() && Arrays.equals(ordinary,Dungeon.level.heroFOV),"Scry must reveal only entity");
        Buff.detach(Dungeon.hero,TalismanOfForesight.CharAwareness.class);

        h=fresh(2); h.pos=Dungeon.hero.pos+8; h.HP=1; h.expose();
        Arrays.fill(Dungeon.level.heroFOV,false);
        h.rooted=true; // A stationary isolated fixture makes elapsed recovery measurable.
        for(int i=0;i<67;i++)act(h);
        check(h.healedTotal()==3 && h.HP==4,"lifetime recovery budget must be 25 percent");
        h.damage(1,HorrorScenario.class); for(int i=0;i<67;i++)act(h);
        check(h.healedTotal()==3 && h.HP==3,"second recovery renewed lifetime healing budget");
        saved=new Bundle(); saved.put("h",h); copy=(LurkingHorror)saved.get("h");
        check(copy.healedTotal()==3,"save/load reset healing budget");
        for(int depth:new int[]{2,7,12,17,22}) {
            h=fresh(depth); int region=(depth-1)/5;
            check(h.HT==new int[]{12,18,28,40,55}[region],"regional health");
            for(int i=0;i<50;i++)check(h.damageRoll()<=4+2*region,"regional base damage");
            check(!Char.hasProp(h,Char.Property.UNDEAD) && !Char.hasProp(h,Char.Property.DEMONIC),"Horror classified as undead/demon");
        }
        counters(); predation(); generation();
        System.out.println("TEST 60 Horror PASS: five-region stats, real warning/response, retargeting, invisibility, solitary hunt, entity-only Mind Vision/Scry, collision occupancy, finite healing, predation/remains, regional generation, bundle/disk persistence");
    }
    private static void counters() throws Exception {
        LurkingHorror h=fresh(7); h.pos=Dungeon.hero.pos+4;
        AshlightLantern lamp=new AshlightLantern();lamp.identify();lamp.level(5);
        Dungeon.hero.belongings.artifact=lamp;lamp.activate(Dungeon.hero);Dungeon.observe();
        check(!h.sensed() && h.shadowmelded(),"Lantern revealed before level six");
        lamp.level(6);Dungeon.observe();check(h.sensed() && !h.shadowmelded(),"open level-six light did not reveal");
        lamp.toggle(Dungeon.hero);Dungeon.observe();check(!h.sensed() && !h.shadowmelded(),"shuttering restored stealth too soon");
        h=fresh(2);Dungeon.observe();Dungeon.hero.search(true);
        check(!h.shadowmelded(),"intentional search failed to expose");
        h=fresh(12);h.damage(0,new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight());
        check(!h.shadowmelded(),"zero-damage illumination failed to expose");
        h=fresh(17);act(h);ready();Dungeon.hero.spend(1);int hp=Dungeon.hero.HP;act(h);
        check(h.phase()==LurkingHorror.Phase.WARNING && Dungeon.hero.HP<hp,"City follow-up not separately warned");
        hp=Dungeon.hero.HP;for(int i=0;i<4;i++)act(h);
        check(hp==Dungeon.hero.HP,"follow-up attacked without another fresh action");
        ready();Dungeon.hero.spend(1);Dungeon.hero.pos-=2;act(h);
        check(hp==Dungeon.hero.HP && h.phase()==LurkingHorror.Phase.FLEEING,"follow-up could not be evaded");
        for(int depth:new int[]{17,22}) {
            h=fresh(depth);int from=h.pos;
            for(int off:PathFinder.NEIGHBOURS8)if(from+off!=Dungeon.hero.pos)Level.set(from+off,Terrain.WALL);
            Level.set(from+1,Terrain.BARRICADE);h.expose();act(h);
            check(h.pos==(depth==22?from+1:from),"wood-only Halls phasing tier "+depth);
        }
        h=fresh(2);h.alignment=Char.Alignment.ALLY;
        Rat rat=new Rat();rat.pos=h.pos+1;rat.sprite=rat.sprite();rat.sprite.link(rat);
        Dungeon.level.mobs.add(rat);Actor.add(rat);int kills=Statistics.enemiesSlain;
        rat.damage(100,h);
        check(Statistics.enemiesSlain==kills+1 && Dungeon.level.freshRemains.get(rat.pos)==null,"controlled Horror kills misclassified as predation");
    }
    private static void predation() throws Exception {
        LurkingHorror h=fresh(2); h.pos=Dungeon.hero.pos+8;
        Rat victim=new Rat(); victim.HP=1; victim.pos=h.pos+1;
        victim.sprite=victim.sprite(); victim.sprite.link(victim); victim.sprite.visible=false;
        Dungeon.level.mobs.add(victim); Actor.add(victim);
        Arrays.fill(Dungeon.level.heroFOV,false);
        int xp=Dungeon.hero.exp, killed=Statistics.enemiesSlain;
        java.util.List<String> messages=new java.util.ArrayList<>();
        com.watabou.utils.Signal.Listener<String> listener=text->{messages.add(text);return false;};
        GLog.update.add(listener);
        try {
            h.announceArrival(); h.announceArrival();
            act(h);
        } finally { GLog.update.remove(listener); }
        check(!victim.isAlive() && h.predationUsed() && h.phase()==LurkingHorror.Phase.FLEEING,"actual predation kill/flight");
        check(Dungeon.hero.exp==xp && Statistics.enemiesSlain==killed,"predation awarded hero kill credit");
        check(messages.stream().filter(s->s.contains("notice your arrival")).count()==1,"entry omen repeats");
        check(messages.stream().filter(s->s.contains("distance")||s.contains("struggle")||s.contains("death cry")).count()==1,"missing/duplicate distant death message");
        FreshRemains remains=Dungeon.level.freshRemains.get(victim.pos);
        check(remains!=null && !remains.seen && remains.name().contains("rat"),"victim identity/unknown remains");
        check(Dungeon.level.corpses.get(victim.pos)!=null,"Corpse Explosion cannot use predator victim");
        check(!remains.description().contains("recognize"),"remains identified predator before first kill");
        Dungeon.hero.pos=victim.pos-1; Dungeon.observe(); check(remains.seen,"normal exploration fails to discover remains");
        com.watabou.noosa.Image image=remains.image(); check(image.width()>0 && image.height()<=10,"corpse silhouette bounds");
        Bundle b=new Bundle(); b.put("h",h); b.put("remains",remains);
        LurkingHorror copy=(LurkingHorror)b.get("h"); FreshRemains corpse=(FreshRemains)b.get("remains");
        check(copy.predationUsed() && corpse.name().equals(remains.name()) && corpse.seen,"predation/remains bundle persistence");
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot); Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
        check(Dungeon.level.freshRemains.get(victim.pos)!=null,"remains lost on disk reload");
        h=(LurkingHorror)Dungeon.level.mobs.stream().filter(m->m instanceof LurkingHorror).findFirst().get();
        check(h.predationUsed(),"reload renewed predation allowance");
        check(!LurkingHorror.ediblePrey(new Albino()) && !LurkingHorror.ediblePrey(new Mimic())
                && !LurkingHorror.ediblePrey(new FetidRat()) && !LurkingHorror.ediblePrey(new Tengu()),"special/rare/quest prey accepted");
        h=fresh(2);h.pos=Dungeon.hero.pos+8;
        Rat survivor=new Rat();survivor.HP=survivor.HT=100;survivor.pos=h.pos+1;
        survivor.sprite=survivor.sprite();survivor.sprite.link(survivor);Dungeon.level.mobs.add(survivor);Actor.add(survivor);
        act(h);check(survivor.isAlive() && survivor.HP<100 && survivor.isTargeting(h) && h.predationUsed(),"survivor did not wake/retaliate, or failed hunt did not count");
    }
    private static void generation() throws Exception {
        Dungeon.init(); int opportunities=0;
        for(int seed=0;seed<400;seed++)for(int r=0;r<5;r++) {
            int floor=HorrorHunts.selectedFloor(seed,r);
            check(floor==HorrorHunts.selectedFloor(seed,r),"regional allocation not deterministic");
            if(floor!=-1) { opportunities++;check(floor>1 && floor%5!=0 && (floor-1)/5==r,"invalid spawn floor"); }
        }
        check(opportunities>850 && opportunities<1150,"regional frequency deviates from 50 percent");
        int spawned=0;java.util.Set<Integer> regions=new java.util.HashSet<>();
        for(int seed=0;seed<16;seed++) {
            Dungeon.seed=seed; Dungeon.init();
            for(int r=0;r<5;r++) {
                int floor=HorrorHunts.selectedFloor(Dungeon.seed,r);if(floor<0)continue;
                Dungeon.depth=floor;Level level=Dungeon.newLevel();
                for(Mob m:level.mobs)if(m instanceof LurkingHorror) {
                    spawned++;regions.add(r);
                    for(Mob other:level.mobs)if(other!=m)check(((RegularLevel)level).room(m.pos)!=((RegularLevel)level).room(other.pos),"spawn room contains another creature");
                    int before=level.mobs.size(); HorrorHunts.populate(level);check(before==level.mobs.size(),"regional cap permits duplicate");
                }
            }
        }
        check(spawned>=12 && regions.size()==5,"generated coverage missing regions/empty rooms: "+spawned+" "+regions);
        Dungeon.depth=2;Dungeon.branch=DragonExpedition.BRANCH;Level level=new DragonCavernLevel();
        int cap=Dungeon.LimitedDrops.HORROR_REGIONS.count;HorrorHunts.populate(level);
        check(Dungeon.LimitedDrops.HORROR_REGIONS.count==cap,"side quest consumed regional allocation");
        Dungeon.branch=0;Dungeon.depth=1;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        check(Dungeon.level.mobs.stream().noneMatch(m->m instanceof LurkingHorror),"floor one spawn");
        Dungeon.LimitedDrops.HORROR_REGIONS.count=31;Dungeon.saveAll();Dungeon.loadGame(GamesInProgress.curSlot);
        check(Dungeon.LimitedDrops.HORROR_REGIONS.count==31,"regional cap not saved");
        System.out.println("Horror generation: "+opportunities+"/2000 regional opportunities, "+spawned+" real spawned floors across all five regions");
    }
}
