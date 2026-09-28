// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.LurkingHorrorSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.*;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

/** Shadowmeld hides presentation, never Actor occupancy or projectile collisions. */
public class LurkingHorror extends Mob {
    public enum Phase { STALKING, WARNING, FLEEING, RECOVERING }
    private Phase phase = Phase.STALKING;
    private int region;
    private float phaseAge, lastClock;
    private int healed;
    private boolean responseOffered, responseTaken, followUp;
    private boolean sensed, omen, predationUsed;
    private int preyId = -1;
    private boolean ambushAttack, controlled;
    private boolean strikePending, strikeFollowUp;
    private static final int[] HEALTH = {12,18,28,40,55};
    private static final int[] FLIGHT = {15,12,10,8,6};

    public LurkingHorror() {
        spriteClass = LurkingHorrorSprite.class;
        state = HUNTING;
        configure(Dungeon.depth);
    }
    public void configure(int depth) {
        region = Math.max(0, Math.min(4, (depth-1)/5));
        HP = HT = HEALTH[region]; EXP = 3 + region*3; maxLvl = 6 + region*5;
        defenseSkill = Math.round((8+2*region)*BalanceTuning.multiplier(HORROR_EVASION));
    }
    public Phase phase() { return phase; }
    public int healedTotal() { return healed; }
    public float phaseAge() { return phaseAge; }
    public boolean shadowmelded() { return phase == Phase.STALKING || (phase == Phase.WARNING && !followUp); }
    public boolean sensed() { return sensed; }
    public boolean visibleToHero() { return isAlive() && (!shadowmelded() && Dungeon.level.heroFOV[pos] || sensed); }
    public static boolean hidden(Char ch) { return ch instanceof LurkingHorror && !((LurkingHorror)ch).visibleToHero(); }
    public static boolean sensed(Char ch) { return ch instanceof LurkingHorror && ((LurkingHorror)ch).sensed; }
    public static boolean concealed(Char ch) { return ch instanceof LurkingHorror && ((LurkingHorror)ch).shadowmelded(); }
    @Override public boolean canSurpriseAttack() { return false; } // Only the explicit warned strike gets the bonus.
    @Override public float spawningWeight() { return 0; } // Regional allocation only, never respawns.
    @Override public boolean reset() { return true; }
    @Override public int attackSkill(Char target) { return ambushAttack ? INFINITE_ACCURACY : 10+region*5; }
    @Override public int damageRoll() { return Math.round(Random.NormalIntRange(2+region,4+2*region)*BalanceTuning.multiplier(HORROR_DAMAGE)); }
    @Override public int drRoll() { return super.drRoll()+region; }
    @Override public float speed() { return super.speed()*(phase==Phase.FLEEING ? 1.5f : phase==Phase.RECOVERING ? .5f : 1f); }
    public int flightTurns() { return Math.max(1, Math.round(FLIGHT[region]*BalanceTuning.multiplier(HORROR_FLIGHT))); }
    public int healingBudget() { return HT*BalanceTuning.get(HORROR_HEALING)/100; }

    /** Called from the hero's FOV computation. Never writes terrain knowledge. */
    public void observe(int mindRange) {
        Hero hero = Dungeon.hero;
        boolean reveal = mindRange>0 && Dungeon.level.distance(hero.pos,pos)<=mindRange;
        for (TalismanOfForesight.CharAwareness awareness : hero.buffs(TalismanOfForesight.CharAwareness.class))
            reveal |= awareness.charID==id();
        AshlightLantern lantern = AshlightLantern.open(hero);
        reveal |= lantern!=null && lantern.level()>=6 && lantern.lights(pos);
        sensed = reveal;
        if (reveal && (shadowmelded() || phase==Phase.WARNING)) expose();
    }
    public void expose() {
        if (phase!=Phase.FLEEING) enter(Phase.FLEEING);
        followUp=false; preyId=-1;
        if (sprite!=null) sprite.visible=visibleToHero();
    }
    private void enter(Phase next) {
        phase=next; phaseAge=0; responseOffered=responseTaken=false;
        state=next==Phase.STALKING || next==Phase.WARNING ? HUNTING : FLEEING;
    }
    public static void onHeroReady() {
        if (Dungeon.level==null) return;
        for (Mob mob:Dungeon.level.mobs) if (mob instanceof LurkingHorror) {
            LurkingHorror h=(LurkingHorror)mob;
            if (h.phase==Phase.WARNING) h.responseOffered=true;
        }
    }
    public static void onHeroSpent(float time) {
        if (time<=0 || Dungeon.level==null) return;
        for (Mob mob:Dungeon.level.mobs) if (mob instanceof LurkingHorror) {
            LurkingHorror h=(LurkingHorror)mob;
            if (h.phase==Phase.WARNING && h.responseOffered) h.responseTaken=true;
        }
    }
    private void warn(boolean secondStrike) {
        enter(Phase.WARNING); followUp=secondStrike;
        Dungeon.hero.interrupt(); Dungeon.hero.lastAction=null;
        GLog.w(Messages.get(this, secondStrike?"warning_again":"warning"));
        Sample.INSTANCE.play(Assets.Sounds.MISS, .55f, .65f);
        com.shatteredpixel.shatteredpixeldungeon.effects.HorrorSenseLayer.warn(pos);
    }
    public void announceArrival() {
        if (!omen && isAlive() && alignment==Alignment.ENEMY) {
            omen=true; GLog.i(Messages.get(this,"omen"));
        }
    }
    public static boolean sameHuntArea(Level level, int a, int b) {
        if (level instanceof RegularLevel) {
            Room ar=((RegularLevel)level).room(a), br=((RegularLevel)level).room(b);
            if (ar!=null && ar==br) return true;
        }
        // Doorways/corridors also protect a hero fighting a nearby enemy.
        return level.distance(a,b)<=5;
    }
    public boolean solitary() {
        for (Mob mob:Dungeon.level.mobs)
            if (mob!=this && mob.isAlive() && mob.alignment==Alignment.ENEMY
                    && sameHuntArea(Dungeon.level,Dungeon.hero.pos,mob.pos)) return false;
        return true;
    }
    private boolean canAmbush() {
        return Dungeon.hero.isAlive() && Dungeon.hero.invisible<=0 && !isCharmedBy(Dungeon.hero)
                && solitary() && Dungeon.level.adjacent(pos,Dungeon.hero.pos);
    }
    @Override protected boolean act() {
        float clock=now()+cooldown();
        phaseAge+=Math.max(0,clock-lastClock); lastClock=clock;
        if (fieldOfView==null || fieldOfView.length!=Dungeon.level.length()) fieldOfView=new boolean[Dungeon.level.length()];
        Dungeon.level.updateFieldOfView(this,fieldOfView);
        enemySeen=fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible<=0;
        if (alignment!=Alignment.ENEMY || buff(Amok.class)!=null || isCharmedBy(Dungeon.hero)) {
            if (!controlled) { expose(); state=HUNTING; controlled=true; }
            return super.act();
        }
        if (controlled) { controlled=false; enter(Phase.FLEEING); }
        if (paralysed>0 || buff(Sleep.class)!=null) { spend(TICK); return true; }
        if (buff(Terror.class)!=null || buff(Dread.class)!=null) expose();
        if (phase==Phase.STALKING) {
            if (tryPredation()) return true;
            if (Dungeon.hero.invisible>0) { spend(TICK); return true; }
            if (!solitary()) { spend(TICK); return true; }
            if (canAmbush()) { warn(false); spend(TICK); return true; }
            int old=pos;
            if (getCloser(Dungeon.hero.pos)) { spend(1/speed()); return moveSprite(old,pos); }
        } else if (phase==Phase.WARNING) {
            if (!canAmbush() || sensed) { expose(); spend(TICK); return true; }
            // Slow actions, auto-travel and reload must not eat the player's response.
            if (!responseTaken) { spend(TICK); return true; }
            strikeFollowUp=followUp;
            enter(Phase.FLEEING);
            if (sprite!=null && sprite.parent!=null && Dungeon.level.heroFOV[pos]) {
                strikePending=true; sprite.visible=true; sprite.attack(Dungeon.hero.pos); return false;
            }
            resolveStrike(); return true;
        } else if (phase==Phase.FLEEING) {
            if (phaseAge>=flightTurns() && Dungeon.level.distance(pos,Dungeon.hero.pos)>=6) {
                enter(Phase.RECOVERING); spend(TICK); return true;
            }
            int old=pos;
            if (retreat()) { spend(1/speed()); return moveSprite(old,pos); }
            // A cornered animal can fight, but only with ordinary damage and accuracy.
            if (Dungeon.level.adjacent(pos,Dungeon.hero.pos) && Dungeon.hero.invisible<=0) attack(Dungeon.hero);
        } else {
            int available=healingBudget()-healed;
            int due=Math.min(available, (int)(phaseAge*healingBudget()/50)-recoveryHealed);
            int amount=Math.min(Math.max(0,due),HT-HP);
            HP+=amount; healed+=amount; recoveryHealed+=amount;
            if (phaseAge>=50 && !Dungeon.level.heroFOV[pos] && !sensed && Dungeon.level.distance(pos,Dungeon.hero.pos)>=6) {
                enter(Phase.STALKING); followUp=false; recoveryHealed=0;
            }
        }
        spend(TICK); return true;
    }
    private int recoveryHealed;
    private boolean retreat() {
        if (rooted) return false;
        boolean[] passable=Dungeon.level.passable;
        if (region==4) {
            passable=passable.clone();
            for(int c=0;c<passable.length;c++) if(Dungeon.level.map[c]==Terrain.BARRICADE)passable[c]=true;
        }
        int step=Dungeon.flee(this,Dungeon.hero.pos,passable,fieldOfView,true);
        if (step<0 || Actor.findChar(step)!=null) return false;
        AshlightLantern lantern=AshlightLantern.open(Dungeon.hero);
        // Prefer an equally good escape step out of the lantern's light.
        if(lantern!=null && lantern.lights(step)) for(int offset:PathFinder.NEIGHBOURS8) {
            int candidate=pos+offset;
            if(Dungeon.level.insideMap(candidate) && passable[candidate] && Actor.findChar(candidate)==null
                    && !lantern.lights(candidate) && Dungeon.level.distance(candidate,Dungeon.hero.pos)>=Dungeon.level.distance(step,Dungeon.hero.pos)) {
                step=candidate; break;
            }
        }
        move(step); return true;
    }
    @Override public void damage(int damage,Object source) {
        expose(); recoveryHealed=0;
        super.damage(damage,source);
    }
    @Override public void move(int cell,boolean travelling) {
        super.move(cell,travelling);
        if(sprite!=null) sprite.visible=visibleToHero() && Dungeon.level.heroFOV[pos];
    }
    private void resolveStrike() {
        if (canAmbush() && !sensed) {
            ambushAttack=!strikeFollowUp;
            attack(Dungeon.hero,strikeFollowUp?1f:region<2?1.5f:region<4?1.75f:2f,0,1);
            ambushAttack=false;
            if (!strikeFollowUp && region>=3 && canAmbush()) warn(true);
        }
        spend(attackDelay());
    }
    @Override public void onAttackComplete() {
        if(strikePending) { strikePending=false; resolveStrike(); next(); }
        else super.onAttackComplete();
    }
    @Override public void clearTime() { lastClock-=Actor.now(); super.clearTime(); }
    @Override public void fixTime(float decrement) { lastClock-=decrement; super.fixTime(decrement); }
    @Override public void rollToDropLoot() {
        Item loot; int roll=Random.Int(9);
        loot=roll<4?new PotionOfMindVision():roll<7?new PotionOfInvisibility():new ScrollOfMagicMapping();
        Dungeon.level.drop(loot,pos).sprite.drop();
    }
    @Override public String description() {
        return Messages.get(this,"desc")+"\n\n"+Messages.get(this,"phase_"+phase.name().toLowerCase(java.util.Locale.ROOT))
                +(Bestiary.encounterCount(getClass())>0?"\n\n"+Messages.get(this,"bestiary"):"");
    }
    // Predation is implemented alongside the persistent remains feature.
    private boolean tryPredation() { return false; }

    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("horror_phase",phase); b.put("horror_region",region); b.put("horror_age",phaseAge);
        b.put("horror_clock",lastClock); b.put("horror_healed",healed); b.put("horror_recovery_healed",recoveryHealed);
        b.put("horror_offered",responseOffered); b.put("horror_answered",responseTaken); b.put("horror_followup",followUp);
        b.put("horror_omen",omen); b.put("horror_predation",predationUsed); b.put("horror_prey",preyId);
        b.put("horror_controlled",controlled);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        phase=b.getEnum("horror_phase",Phase.class); region=b.getInt("horror_region"); phaseAge=b.getFloat("horror_age");
        lastClock=b.getFloat("horror_clock"); healed=b.getInt("horror_healed"); recoveryHealed=b.getInt("horror_recovery_healed");
        responseOffered=b.getBoolean("horror_offered"); responseTaken=b.getBoolean("horror_answered"); followUp=b.getBoolean("horror_followup");
        omen=b.getBoolean("horror_omen"); predationUsed=b.getBoolean("horror_predation"); preyId=b.getInt("horror_prey");
        controlled=b.getBoolean("horror_controlled"); defenseSkill=Math.round((8+2*region)*BalanceTuning.multiplier(HORROR_EVASION));
        EXP=3+region*3;
    }
}
