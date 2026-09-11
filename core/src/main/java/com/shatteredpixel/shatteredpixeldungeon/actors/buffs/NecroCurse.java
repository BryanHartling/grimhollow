// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NecroticParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
/** One owned curse per target; replacement ends its owned debuffs. */
public class NecroCurse extends Buff {
    public enum Kind { WITHER, AMPLIFY, DECREPIFY, IRON_MAIDEN, LOWER_RESISTANCE }
    public Kind kind=Kind.WITHER;
    public int remaining;
    private boolean ownWeakness,ownVulnerable,ownSlow,ownCripple;
    { type=buffType.NEGATIVE; }
    public static NecroCurse find(Char target) { for (NecroCurse b:target.buffs(NecroCurse.class)) return b; return null; }
    public static NecroCurse apply(Char target,Kind kind,int turns) {
        NecroCurse old=find(target); if(old!=null)old.detach();
        NecroCurse curse=kind==Kind.AMPLIFY?new AmplifySuffering():kind==Kind.LOWER_RESISTANCE?new LowerResistance():new NecroCurse();
        curse.kind=kind;curse.remaining=Math.max(1,turns);
        if (!curse.attachTo(target)) return null;
        if(kind==Kind.WITHER || kind==Kind.DECREPIFY) {
            curse.ownWeakness=target.buff(Weakness.class)==null; Buff.prolong(target,Weakness.class,turns);
        }
        if(kind==Kind.WITHER) {
            curse.ownVulnerable=target.buff(Vulnerable.class)==null; Buff.prolong(target,Vulnerable.class,turns);
            int cripple=2*Math.max(0,Necromancy.points(Talent.GRAVE_WISDOM)-1);
            if(cripple>0) {curse.ownCripple=target.buff(Cripple.class)==null;Buff.prolong(target,Cripple.class,cripple);}
        }
        if(kind==Kind.DECREPIFY) {
            curse.ownSlow=target.buff(Slow.class)==null;curse.ownCripple=target.buff(Cripple.class)==null;
            Buff.prolong(target,Slow.class,turns);Buff.prolong(target,Cripple.class,turns);
        }
        curse.spark(); return curse;
    }
    public void spark() { if(com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.enabled()){com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.burst(target.pos,com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.Style.CURSE,16,.6f);return;} if(target.sprite!=null && target.sprite.parent!=null && target.sprite.visible) target.sprite.centerEmitter().burst(NecroticParticle.FACTORY,3); }
    @Override public boolean act() {
        if(remaining<=0 || !target.isAlive()) {detach();return true;}
        Necromancy passive=Dungeon.hero.buff(Necromancy.class);
        if(passive!=null && target.alignment==Char.Alignment.ENEMY)passive.siphon(target);
        spark();remaining--;spend(TICK);return true;
    }
    @Override public void detach() {
        if(target!=null) { if(ownWeakness)Buff.detach(target,Weakness.class);if(ownVulnerable)Buff.detach(target,Vulnerable.class);if(ownSlow)Buff.detach(target,Slow.class);if(ownCripple)Buff.detach(target,Cripple.class); }
        super.detach();
    }
    @Override public int icon() {return BuffIndicator.HEX;}
    @Override public String name() {return Messages.get(NecroCurse.class,kind.name());}
    @Override public String desc() {return Messages.get(NecroCurse.class,kind.name()+"_desc",remaining);}
    @Override public void storeInBundle(Bundle b) {super.storeInBundle(b);b.put("kind",kind);b.put("remaining",remaining);b.put("owned",new boolean[]{ownWeakness,ownVulnerable,ownSlow,ownCripple});}
    @Override public void restoreFromBundle(Bundle b) {super.restoreFromBundle(b);kind=b.getEnum("kind",Kind.class);remaining=b.getInt("remaining");boolean[] owned=b.getBooleanArray("owned");if(owned.length==4){ownWeakness=owned[0];ownVulnerable=owned[1];ownSlow=owned[2];ownCripple=owned[3];}}
}
