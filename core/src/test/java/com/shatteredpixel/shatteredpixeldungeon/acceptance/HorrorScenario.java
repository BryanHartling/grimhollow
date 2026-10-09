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
        check(hp-Dungeon.hero.HP<=12,"Sewers ambush exceeds the 4-8 base range times 1.5");

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
        check(h.healedTotal()==6 && h.HP==7,"lifetime recovery budget must be 25 percent");
        h.damage(1,HorrorScenario.class); for(int i=0;i<67;i++)act(h);
        check(h.healedTotal()==6 && h.HP==6,"second recovery renewed lifetime healing budget");
        saved=new Bundle(); saved.put("h",h); copy=(LurkingHorror)saved.get("h");
        check(copy.healedTotal()==6,"save/load reset healing budget");
        for(int depth:new int[]{2,7,12,17,22}) {
            h=fresh(depth); int region=(depth-1)/5;
            check(h.HT==new int[]{24,50,120,150,180}[region],"regional health");
            for(int i=0;i<50;i++){int rolled=h.damageRoll();check(rolled>=new int[]{4,6,11,14,18}[region] && rolled<=new int[]{8,12,22,28,36}[region],"regional base damage");}
            check(!Char.hasProp(h,Char.Property.UNDEAD) && !Char.hasProp(h,Char.Property.DEMONIC),"Horror classified as undead/demon");
        }
        recoveryAndRouting(); counters(); predation(); denizenDefense(); generation();
        System.out.println("TEST 60 Horror PASS: five-region stats, real warning/response, retargeting, invisibility, solitary hunt, entity-only Mind Vision/Scry, collision occupancy, finite healing, concealed mobile recovery, tool/collision reveals, routed escape, predation/remains, regional generation, bundle/disk persistence");
    }
    private static void field(LurkingHorror h,String name,Object value) throws Exception {
        java.lang.reflect.Field f=LurkingHorror.class.getDeclaredField(name);f.setAccessible(true);f.set(h,value);
    }
    private static void recoveryAndRouting() throws Exception {
        LurkingHorror h=fresh(7);h.pos=Dungeon.hero.pos+8;h.HP=1;h.rooted=true;h.expose();
        field(h,"phaseAge",100f);Arrays.fill(Dungeon.level.heroFOV,true);act(h);
        check(h.phase()==LurkingHorror.Phase.FLEEING,"visible flight entered recovery");
        Arrays.fill(Dungeon.level.heroFOV,false);act(h);
        check(h.phase()==LurkingHorror.Phase.RECOVERING && h.shadowmelded(),"eligible flight did not conceal recovery");
        Dungeon.level.heroFOV[h.pos]=true;h.observe(0);
        check(h.phase()==LurkingHorror.Phase.RECOVERING && !h.visibleToHero(),"ordinary room entry exposes recovery");
        h.rooted=false;Dungeon.hero.pos=h.pos-1;int hp=Dungeon.hero.HP,from=h.pos;
        act(h);check(h.pos!=from && Dungeon.hero.HP==hp,"hidden recovery did not evade hero without attacking");
        Bundle b=new Bundle();b.put("h",h);LurkingHorror restored=(LurkingHorror)b.get("h");
        check(restored.shadowmelded() && restored.phase()==h.phase(),"concealed recovery save/load");
        TalismanOfForesight.CharAwareness aware=Buff.append(Dungeon.hero,TalismanOfForesight.CharAwareness.class,10);
        aware.charID=h.id();h.observe(0);check(h.phase()==LurkingHorror.Phase.FLEEING,"recovery ignored full reveal");

        h=fresh(2);hp=Dungeon.hero.HP;
        field(h,"phase",LurkingHorror.Phase.RECOVERING);h.HP=1;
        Dungeon.hero.curAction=new HeroAction.Move(h.pos);
        Method move=Hero.class.getDeclaredMethod("getCloser",int.class);move.setAccessible(true);
        check(!(boolean)move.invoke(Dungeon.hero,h.pos) && h.phase()==LurkingHorror.Phase.FLEEING
                && Dungeon.hero.HP==hp && Dungeon.hero.curAction==null && Actor.findChar(h.pos)==h,
                "physical encounter must stop, expose, preserve occupancy and not ambush");

        h=fresh(2);h.pos=Dungeon.hero.pos+8;h.rooted=true;h.expose();
        Arrays.fill(Dungeon.level.heroFOV,false);field(h,"phaseAge",100f);act(h);
        check(h.phase()==LurkingHorror.Phase.STALKING,"full-health Horror forced through idle recovery");
        h=fresh(2);h.pos=Dungeon.hero.pos+8;h.rooted=true;h.expose();h.HP=1;
        field(h,"healed",h.healingBudget());Arrays.fill(Dungeon.level.heroFOV,false);field(h,"phaseAge",100f);act(h);
        check(h.phase()==LurkingHorror.Phase.STALKING,"spent lifetime allowance forced idle recovery");

        h=fresh(7);int w=Dungeon.level.width();
        for(int c=0;c<Dungeon.level.length();c++)if(Dungeon.level.insideMap(c))Level.set(c,Terrain.WALL);
        // The first escape step approaches the hero. A connected corridor then doubles back
        // behind the obstacle and opens into a hiding room.
        int x=6,y=6;h.pos=y*w+x;Dungeon.hero.pos=y*w+x+4;
        Level.set(h.pos,Terrain.EMPTY);Level.set(h.pos+1,Terrain.EMPTY);
        for(int yy=y+1;yy<=y+9;yy++)Level.set(yy*w+x+1,Terrain.EMPTY);
        for(int xx=2;xx<=x+1;xx++)Level.set((y+9)*w+xx,Terrain.EMPTY);
        for(int yy=y+9;yy<=y+12;yy++)for(int xx=2;xx<=5;xx++)Level.set(yy*w+xx,Terrain.EMPTY);
        Level.set(Dungeon.hero.pos,Terrain.EMPTY);
        Arrays.fill(Dungeon.level.heroFOV,true);h.HP=1;h.expose();from=h.pos;act(h);
        check(h.pos%w==x+1 && Dungeon.level.distance(h.pos,Dungeon.hero.pos)<Dungeon.level.distance(from,Dungeon.hero.pos),"routed flight cannot approach briefly to escape dead end");
        java.util.Set<Integer> reached=new java.util.HashSet<>();reached.add(h.pos);
        for(int i=0;i<10;i++){act(h);reached.add(h.pos);}
        check(reached.size()>=9 && Dungeon.level.distance(h.pos,Dungeon.hero.pos)>=6,
                "routed escape oscillated instead of clearing the corridor");
        Arrays.fill(Dungeon.level.heroFOV,false);field(h,"phaseAge",100f);act(h);
        for(int i=0;i<3;i++)act(h);
        check(h.phase()==LurkingHorror.Phase.RECOVERING && h.shadowmelded(),"route failed to reach concealed room recovery");
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
        Rat victim=new Rat(); victim.pos=h.pos+1;
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
        int previousDamage=BalanceTuning.get(BalanceTuning.Key.HORROR_DAMAGE);
        try{
            for(int percent:new int[]{100,200}){
                BalanceTuning.setShared(BalanceTuning.Key.HORROR_DAMAGE,percent);
                h=fresh(2);h.pos=Dungeon.hero.pos+8;
                for(int seed=0;seed<100;seed++){
                    Random.pushGenerator(seed);
                    try{
                        Rat rat=new Rat();rat.pos=h.pos+1;rat.sprite=rat.sprite();rat.sprite.link(rat);
                        Dungeon.level.mobs.add(rat);Actor.add(rat);
                        field(h,"predationUsed",false);field(h,"preyId",-1);field(h,"phase",LurkingHorror.Phase.STALKING);
                        Arrays.fill(Dungeon.level.heroFOV,false);int before=h.HP;
                        act(h);check(!rat.isAlive() && h.HP==before && h.predationUsed(),"full-health floor-two rat survives sleeping-prey pounce at "+percent+" percent seed="+seed);
                    }finally{Random.popGenerator();}
                }
            }
        }finally{BalanceTuning.setShared(BalanceTuning.Key.HORROR_DAMAGE,previousDamage);}
        System.out.println("HORROR PREDATION PASS: 200 full-health floor-two rats, damage settings 100/200 percent; no retaliation, no hero kill credit; strengthened player damage remains separately bounded");
    }
    private static void denizenDefense() throws Exception {
        int wins=0;
        for(int seed=0;seed<64;seed++){
            LurkingHorror original=fresh(11);Dungeon.level.mobs.remove(original);Actor.remove(original);
            LurkingHorror horror=new LurkingHorror(){@Override public void rollToDropLoot(){}};
            horror.pos=original.pos;horror.sprite=horror.sprite();horror.sprite.link(horror);Dungeon.level.mobs.add(horror);Actor.add(horror);
            Dungeon.hero.pos=horror.pos-8;horror.rooted=true;horror.expose();
            Arrays.fill(Dungeon.level.heroFOV,false); // Offscreen denizen fight; no native particle scene in headless mode.
            Random.pushGenerator(seed);
            try {
                Spinner spider=new Spinner(){
                    @Override public void rollToDropLoot(){}
                    @Override public int attackProc(Char enemy,int damage){
                        com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite=enemy.sprite;
                        enemy.sprite=null; // Only suppress Poison's native splash, never death/combat rendering.
                        try{return super.attackProc(enemy,damage);}finally{enemy.sprite=sprite;}
                    }
                };spider.pos=horror.pos+1;
                spider.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.SpinnerSprite(){
                    @Override public com.watabou.noosa.particles.Emitter emitter(){return new com.watabou.noosa.particles.Emitter();}
                };spider.sprite.link(spider);
                Dungeon.level.mobs.add(spider);Actor.add(spider);
                int heroHP=Dungeon.hero.HP;
                for(int turn=0;turn<32&&horror.isAlive()&&spider.isAlive();turn++){
                    spider.attack(horror);
                    Poison poison=horror.buff(Poison.class);if(poison!=null&&horror.isAlive())poison.act();
                    if(horror.isAlive())act(horror);
                }
                if(horror.isAlive()&&!spider.isAlive())wins++;
                check(Dungeon.hero.HP==heroHP,"defensive denizen combat gained a hero ambush");
            } finally {Random.popGenerator();}
        }
        check(wins>=48,"caves Horror still loses most cornered encounters to ordinary cave spinners: "+wins+"/64");
        LurkingHorror horror=fresh(11);Bundle old=new Bundle();horror.storeInBundle(old);
        old.put("horror_stats_version",0);old.put("HT",28);old.put("HP",14);old.put("horror_healed",7);old.put("horror_recovery_healed",7);
        LurkingHorror restored=new LurkingHorror();restored.restoreFromBundle(old);
        check(restored.HT==120&&restored.HP==60&&restored.healedTotal()==30,"old Horror saves lost injury or renewed spent recovery");
        old.put("HP",0);restored.restoreFromBundle(old);check(!restored.isAlive(),"stat migration resurrected a killed Horror");
        Guard quarry=new Guard();quarry.bountyContract=1;check(!LurkingHorror.ediblePrey(quarry),"Horror can hunt a wanted target offscreen");
        System.out.println("HORROR DENIZEN DEFENSE PASS: floor-11 cave-spinner victories="+wins+"/64; five regional baselines, old-save injury/healing limits, no resurrection and wanted-target protection");
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
