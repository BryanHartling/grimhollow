// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
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
        LurkingHorror h=new LurkingHorror(); h.pos=Dungeon.hero.pos+1; h.sprite=h.sprite(); h.sprite.link(h);
        Dungeon.level.mobs.add(h); Actor.add(h); return h;
    }
    static void run() throws Exception {
        LurkingHorror h=fresh(2); int hp=Dungeon.hero.HP;
        Dungeon.hero.curAction=new HeroAction.Move(Dungeon.hero.pos+8); Dungeon.hero.resting=true;
        act(h);
        check(h.phase()==LurkingHorror.Phase.WARNING && Dungeon.hero.HP==hp,"warning must precede attack");
        check(Dungeon.hero.curAction==null && !Dungeon.hero.resting && Dungeon.hero.lastAction==null,"warning fails to stop automation");
        for(int i=0;i<4;i++)act(h);
        check(Dungeon.hero.HP==hp,"slow action consumed warning response");
        LurkingHorror.onHeroReady(); act(h);
        check(Dungeon.hero.HP==hp,"ready without an action was treated as a response");
        LurkingHorror.onHeroSpent(0); act(h); check(Dungeon.hero.HP==hp,"free action consumed response");
        Bundle saved=new Bundle(); saved.put("h",h); LurkingHorror copy=(LurkingHorror)saved.get("h");
        check(copy.phase()==h.phase() && copy.healedTotal()==h.healedTotal(),"phase save round trip");
        LurkingHorror.onHeroSpent(1); act(h);
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
        System.out.println("TEST 60 Horror PASS: five-region stats, real warning/response, retargeting, invisibility, solitary hunt, entity-only Mind Vision/Scry, collision occupancy, finite healing, bundle persistence");
    }
}
