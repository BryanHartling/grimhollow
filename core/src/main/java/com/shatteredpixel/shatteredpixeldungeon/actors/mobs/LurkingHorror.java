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
    private boolean ambushAttack, controlled, predatoryStrike;
    private boolean strikePending, strikeFollowUp;
    private int escapeGoal = -1;
    private boolean recoveryCue;
    private int aggressorId=-1;
    private static final int[] HEALTH = {24,50,120,150,180};
    private static final int[] DAMAGE_LOW = {4,6,11,14,18}, DAMAGE_HIGH = {8,12,22,28,36};
    private static final int[] FLIGHT = {15,12,10,8,6};

    public LurkingHorror() {
        spriteClass = LurkingHorrorSprite.class;
        state = HUNTING;
        configure(Dungeon.depth);
    }
    public void configure(int depth) {
        region = Math.max(0, Math.min(4, (depth-1)/5));
        HP = HT = HEALTH[region]; EXP = 3 + region*3; maxLvl = 6 + region*5;
        defenseSkill = Math.round((10+4*region)*BalanceTuning.multiplier(HORROR_EVASION));
    }
    public Phase phase() { return phase; }
    public int healedTotal() { return healed; }
    public float phaseAge() { return phaseAge; }
    public boolean shadowmelded() { return phase == Phase.STALKING || phase == Phase.RECOVERING || (phase == Phase.WARNING && !followUp); }
    public boolean sensed() { return sensed; }
    public boolean visibleToHero() { return isAlive() && (!shadowmelded() && Dungeon.level.heroFOV[pos] || sensed); }
    public static boolean hidden(Char ch) { return ch instanceof LurkingHorror && !((LurkingHorror)ch).visibleToHero(); }
    public static boolean sensed(Char ch) { return ch instanceof LurkingHorror && ((LurkingHorror)ch).sensed; }
    public static boolean concealed(Char ch) { return ch instanceof LurkingHorror && ((LurkingHorror)ch).shadowmelded(); }
    @Override public boolean canSurpriseAttack() { return false; } // Only the explicit warned strike gets the bonus.
    @Override public float spawningWeight() { return 0; } // Regional allocation only, never respawns.
    @Override public boolean reset() { return true; }
    @Override public int attackSkill(Char target) { return ambushAttack ? INFINITE_ACCURACY : 12+region*6; }
    @Override public int damageRoll() {
        // A sleeping-prey pounce is separate from the hero-facing combat range.
        int low=predatoryStrike?9+4*region:DAMAGE_LOW[region],high=predatoryStrike?13+6*region:DAMAGE_HIGH[region];
        return Math.round(Random.NormalIntRange(low,high)*BalanceTuning.multiplier(HORROR_DAMAGE));
    }
    @Override public int drRoll() { return super.drRoll()+region; }
    @Override public int defenseProc(Char attacker,int damage) {
        if(attacker instanceof Mob && attacker!=this)aggressorId=attacker.id();
        return super.defenseProc(attacker,damage);
    }
    @Override public float speed() { return super.speed()*(phase==Phase.FLEEING ? 1.5f : phase==Phase.RECOVERING ? .5f : 1f); }
    public int flightTurns() { return Math.max(1, Math.round(FLIGHT[region]*BalanceTuning.multiplier(HORROR_FLIGHT))); }
    public int healingBudget() { return HT*BalanceTuning.get(HORROR_HEALING)/100; }
    public boolean predatoryStrike() { return predatoryStrike; }
    @Override protected void onAdd() {
        if(firstAdded) lastClock=now()+cooldown();
        super.onAdd();
    }

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
    /** A concealed body still occupies its cell. A bump reveals it without a free strike. */
    public static boolean encounter(Char occupant) {
        if (!(occupant instanceof LurkingHorror) || !concealed(occupant)) return false;
        ((LurkingHorror)occupant).expose();
        Dungeon.hero.interrupt(); Dungeon.hero.lastAction=null;
        GLog.w(Messages.get(occupant,"shift"));
        return true;
    }
    private void enter(Phase next) {
        phase=next; phaseAge=0; responseOffered=responseTaken=false;
        escapeGoal=-1;
        if(next==Phase.RECOVERING) { recoveryHealed=0; recoveryCue=false; }
        lastClock=now()+cooldown();
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
    }
    public void announceArrival() {
        if (!omen && isAlive() && alignment==Alignment.ENEMY) {
            omen=true; GLog.w(Messages.get(this,"omen"));
            com.shatteredpixel.shatteredpixeldungeon.effects.HorrorSenseLayer.arrival();
            Sample.INSTANCE.play(Assets.Sounds.MISS,.75f,.65f);
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
            Boolean predation=tryPredation();
            if (predation!=null) return predation;
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
            if (phaseAge>=flightTurns() && !Dungeon.level.heroFOV[pos] && !sensed
                    && Dungeon.level.distance(pos,Dungeon.hero.pos)>=6) {
                enter(HP==HT || healed>=healingBudget()?Phase.STALKING:Phase.RECOVERING);
                if(sprite!=null) sprite.visible=visibleToHero();
                spend(TICK); return true;
            }
            int old=pos;
            if (retreat(false)) { spend(1/speed()); return moveSprite(old,pos); }
            // A cornered animal can fight, but only with ordinary damage and accuracy.
            Char aggressor=Actor.findById(aggressorId) instanceof Char?(Char)Actor.findById(aggressorId):null;
            if(aggressor!=null&&aggressor.isAlive()&&aggressor.invisible<=0&&Dungeon.level.adjacent(pos,aggressor.pos))attack(aggressor);
            else if (Dungeon.level.adjacent(pos,Dungeon.hero.pos) && Dungeon.hero.invisible<=0) attack(Dungeon.hero);
        } else {
            int available=healingBudget()-healed;
            int due=Math.min(available, (int)(phaseAge*healingBudget()/50)-recoveryHealed);
            int amount=Math.min(Math.max(0,due),HT-HP);
            HP+=amount; healed+=amount; recoveryHealed+=amount;
            if (HP==HT || healed>=healingBudget()) {
                enter(Phase.STALKING); followUp=false;
            } else {
                if(Dungeon.level.distance(pos,Dungeon.hero.pos)<=2 && !recoveryCue) {
                    recoveryCue=true; GLog.i(Messages.get(this,"shift"));
                }
                int old=pos;
                if(retreat(true)) { spend(1/speed()); return moveSprite(old,pos); }
                // Rooted creatures can still heal. Otherwise an unsafe blocked hiding place
                // resumes exposed flight instead of silently occupying a doorway.
                if(!rooted && (!hidingCell(pos) || Dungeon.level.adjacent(pos,Dungeon.hero.pos))) expose();
            }
        }
        spend(TICK); return true;
    }
    private int recoveryHealed;
    private boolean retreat(boolean recovering) {
        if (rooted) return false;
        Level level=Dungeon.level;
        boolean[] passable=level.passable.clone();
        if (region==4) for(int c=0;c<passable.length;c++)
            if(level.map[c]==Terrain.BARRICADE)passable[c]=true;
        modifyPassable(passable);
        for(Char c:Actor.chars()) if(c!=this && level.insideMap(c.pos)) passable[c.pos]=false;
        if(recovering) for(int off:PathFinder.NEIGHBOURS8) {
            int c=Dungeon.hero.pos+off;
            if(level.insideMap(c)) passable[c]=false;
        }
        // A reachable destination stays fixed while following its shortest route. Unlike a
        // greedy distance step, this can briefly approach the hero to escape a dead end.
        int[] distance=new int[level.length()], previous=new int[level.length()], queue=new int[level.length()];
        java.util.Arrays.fill(distance,-1);
        int head=0,tail=0; queue[tail++]=pos; distance[pos]=0;
        while(head<tail) {
            int from=queue[head++];
            for(int off:PathFinder.NEIGHBOURS8) {
                int c=from+off;
                if(level.insideMap(c) && passable[c] && distance[c]<0) {
                    distance[c]=distance[from]+1; previous[c]=from; queue[tail++]=c;
                }
            }
        }
        if(escapeGoal<0 || distance[escapeGoal]<0 || pos==escapeGoal
                || level.distance(escapeGoal,Dungeon.hero.pos)<3) {
            escapeGoal=-1; float best=-Float.MAX_VALUE;
            for(int i=0;i<tail;i++) {
                int c=queue[i], away=level.distance(c,Dungeon.hero.pos);
                if(recovering && (away<3 || !hidingCell(c))) continue;
                float score=10*Math.min(away,12)-distance[c]-(level.heroFOV[c]?35:0)-illumination(c)*4;
                if(score>best) { best=score; escapeGoal=c; }
            }
        }
        if(escapeGoal<0 || escapeGoal==pos) return false;
        int step=escapeGoal;
        while(previous[step]!=pos) step=previous[step];
        if(Actor.findChar(step)!=null) { escapeGoal=-1; return false; }
        move(step); return true;
    }
    private boolean hidingCell(int cell) {
        int terrain=Dungeon.level.map[cell];
        if(terrain==Terrain.DOOR || terrain==Terrain.OPEN_DOOR || terrain==Terrain.LOCKED_DOOR) return false;
        int open=0;
        for(int off:PathFinder.NEIGHBOURS8) if(Dungeon.level.insideMap(cell+off)
                && Dungeon.level.passable[cell+off]) open++;
        return open>=3;
    }
    private float illumination(int cell) {
        AshlightLantern lantern=AshlightLantern.open(Dungeon.hero);
        float result=lantern!=null && lantern.lights(cell)?4:0;
        int w=Dungeon.level.width();
        for(int y=-3;y<=3;y++)for(int x=-3;x<=3;x++) {
            int p=cell+x+y*w;
            if(Dungeon.level.insideMap(p) && Dungeon.level.distance(cell,p)<=3
                    && com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.torchAt(Dungeon.level,p))
                result+=1f/(1+Dungeon.level.distance(cell,p));
        }
        return result;
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
    public boolean predationUsed() { return predationUsed; }
    public static boolean ediblePrey(Mob prey) {
        return prey.isAlive() && prey.bountyContract<0 && prey.alignment==Alignment.ENEMY && prey.state==prey.SLEEPING
                && Bestiary.REGIONAL.entities().contains(prey.getClass())
                && !Char.hasProp(prey,Property.BOSS) && !Char.hasProp(prey,Property.MINIBOSS)
                && !Char.hasProp(prey,Property.IMMOVABLE) && !Char.hasProp(prey,Property.INORGANIC)
                && prey.buffs(ChampionEnemy.class).isEmpty() && prey.buff(CursedVariant.class)==null;
    }
    private Boolean tryPredation() {
        if(predationUsed || Dungeon.level.distance(pos,Dungeon.hero.pos)<=6)return null;
        Char old=Actor.findById(preyId) instanceof Char?(Char)Actor.findById(preyId):null;
        Mob prey=old instanceof Mob && ediblePrey((Mob)old)?(Mob)old:null;
        if(prey==null) {
            for(Mob candidate:Dungeon.level.mobs) if(ediblePrey(candidate) && fieldOfView[candidate.pos]
                    && (prey==null || Dungeon.level.distance(pos,candidate.pos)<Dungeon.level.distance(pos,prey.pos)))prey=candidate;
            preyId=prey==null?-1:prey.id();
        }
        if(prey==null)return null;
        if(Dungeon.level.adjacent(pos,prey.pos)) {
            predationUsed=true; expose(); ambushAttack=predatoryStrike=true;
            try { attack(prey,1f,0,1); }
            finally { ambushAttack=predatoryStrike=false; }
            if(prey.isAlive())prey.aggro(this);
            spend(attackDelay()); return true;
        }
        int from=pos;
        if(getCloser(prey.pos)) { spend(1/speed()); return moveSprite(from,pos); }
        return null;
    }

    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("horror_phase",phase); b.put("horror_region",region); b.put("horror_age",phaseAge);
        b.put("horror_clock",lastClock); b.put("horror_healed",healed); b.put("horror_recovery_healed",recoveryHealed);
        b.put("horror_offered",responseOffered); b.put("horror_answered",responseTaken); b.put("horror_followup",followUp);
        b.put("horror_omen",omen); b.put("horror_predation",predationUsed); b.put("horror_prey",preyId);
        b.put("horror_controlled",controlled);
        b.put("horror_evasion",defenseSkill);
        b.put("horror_goal",escapeGoal); b.put("horror_cue",recoveryCue);
        b.put("horror_aggressor",aggressorId);b.put("horror_stats_version",1);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        phase=b.getEnum("horror_phase",Phase.class); region=b.getInt("horror_region"); phaseAge=b.getFloat("horror_age");
        lastClock=b.getFloat("horror_clock"); healed=b.getInt("horror_healed"); recoveryHealed=b.getInt("horror_recovery_healed");
        responseOffered=b.getBoolean("horror_offered"); responseTaken=b.getBoolean("horror_answered"); followUp=b.getBoolean("horror_followup");
        omen=b.getBoolean("horror_omen"); predationUsed=b.getBoolean("horror_predation"); preyId=b.getInt("horror_prey");
        controlled=b.getBoolean("horror_controlled"); defenseSkill=b.getInt("horror_evasion");
        EXP=3+region*3;
        escapeGoal=b.contains("horror_goal")?b.getInt("horror_goal"):-1;
        recoveryCue=b.getBoolean("horror_cue");
        aggressorId=b.contains("horror_aggressor")?b.getInt("horror_aggressor"):-1;
        if(!b.contains("horror_stats_version")||b.getInt("horror_stats_version")<1){
            // Preserve injury and recovery-budget fractions, never resurrect a dead actor.
            int oldHealth=Math.max(1,HT);HT=HEALTH[region];
            HP=Math.min(HT,(int)Math.ceil(HP*(double)HT/oldHealth));
            healed=(int)Math.ceil(healed*(double)HT/oldHealth);
            recoveryHealed=(int)Math.ceil(recoveryHealed*(double)HT/oldHealth);
            defenseSkill=Math.round((10+4*region)*BalanceTuning.multiplier(HORROR_EVASION));
        }
    }
}
