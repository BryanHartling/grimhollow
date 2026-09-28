// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.BalanceTuning;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ExpeditionDragonSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.HashSet;

public class ExpeditionDragon extends Mob {
    public enum Attack { NONE, BREATH, WINGBEAT }
    public Attack pending = Attack.NONE;
    public int aim = -1, origin = -1, lowestHP;
    public float breathDelay, wingDelay;
    { spriteClass = ExpeditionDragonSprite.class; HP = HT = BalanceTuning.get(DRAGON_HEALTH); lowestHP = HT; defenseSkill = 22;
        EXP = 40; maxLvl = 30; flying = true; viewDistance = 6;
        properties.add(Property.BOSS);
        // Its wings overhang the tile, but it must approach heroes on one-cell bridges.
        immunities.add(Burning.class); immunities.add(Fire.class); HUNTING = new DragonHunting(); }
    @Override public String description() { return Messages.get(this, "desc", BalanceTuning.get(DRAGON_BREATH_COOLDOWN), BalanceTuning.get(DRAGON_KNOCKBACK)); }
    @Override public int attackSkill(Char enemy) { return 32; }
    @Override public int damageRoll() { return Math.round(Random.NormalIntRange(22, 32) * BalanceTuning.multiplier(DRAGON_DAMAGE)); }
    @Override public int drRoll() { return Random.NormalIntRange(4, 10); }
    @Override public int heal(int amount) { return 0; }
    @Override public void damage(int damage, Object source) { super.damage(damage, source); lowestHP = Math.min(lowestHP, HP); }
    @Override public void notice() { if (sprite != null) super.notice(); BossHealthBar.assignBoss(this); }
    @Override public void rollToDropLoot() {} // Its reward is the protected hoard.
    @Override protected boolean act() { HP = Math.min(HP, lowestHP); return super.act(); }
    @Override protected void spend(float time) {
        float before = cooldown(); super.spend(time);
        float elapsed = Math.max(0, cooldown() - before);
        breathDelay = Math.max(0, breathDelay - elapsed); wingDelay = Math.max(0, wingDelay - elapsed);
    }

    public HashSet<Integer> attackCells(Attack attack, int target) {
        if (attack == Attack.NONE || target == pos || !Dungeon.level.insideMap(target)) return new HashSet<>();
        int flags = Ballistica.STOP_SOLID | Ballistica.STOP_TARGET;
        // Aim gives a direction, not the end of the cone. Extend to its full reach.
        Ballistica direction = new Ballistica(pos, target, Ballistica.WONT_STOP);
        int end = direction.path.get(direction.path.size() - 1);
        ConeAOE cone = new ConeAOE(new Ballistica(pos, end, flags), attack == Attack.BREATH ? 6 : 2,
                attack == Attack.BREATH ? 50 : 90, flags);
        cone.cells.remove(pos);
        cone.cells.removeIf(c -> !Dungeon.level.insideMap(c) || Dungeon.level.solid[c]);
        return cone.cells;
    }
    public boolean prepare(Attack attack, int target) {
        if (pending != Attack.NONE || attack == Attack.NONE || !Dungeon.level.insideMap(target)
                || attack == Attack.BREATH && breathDelay > 0 || attack == Attack.WINGBEAT && wingDelay > 0) return false;
        HashSet<Integer> cells = attackCells(attack, target);
        if (cells.isEmpty()) return false;
        pending = attack; aim = target; origin = pos;
        for (int c : cells) if (Dungeon.level.heroFOV[c]) GameScene.targetedCell(c, 1f);
        if (Dungeon.level.heroFOV[pos]) {
            GLog.w(Messages.get(this, attack == Attack.BREATH ? "inhale" : "wings")); Dungeon.hero.interrupt();
        }
        return true;
    }
    /** Resolve against the warned direction; movement or displacement does not retarget the warning. */
    public void release() {
        Attack attack = pending; pending = Attack.NONE;
        if (attack == Attack.NONE || origin != pos) return;
        HashSet<Integer> cells = attackCells(attack, aim);
        if (attack == Attack.BREATH) breathDelay = BalanceTuning.get(DRAGON_BREATH_COOLDOWN);
        else wingDelay = 5;
        for (int cell : cells) {
            if (attack == Attack.BREATH) {
                GameScene.add(Blob.seed(cell, 3, Fire.class));
                Char ch = Actor.findChar(cell);
                if (ch != null && ch != this) {
                    ch.damage(Math.round(Random.NormalIntRange(14, 22) * BalanceTuning.multiplier(DRAGON_DAMAGE)), new Fire());
                    if (ch.isAlive() && !ch.isImmune(Fire.class)) {
                        Burning burn = Buff.affect(ch, Burning.class);
                        if (burn != null) burn.reignite(ch);
                    }
                }
            }
        }
        if (attack == Attack.WINGBEAT) {
            // Snapshot before moving anyone, so a pushed actor cannot be hit twice by the same cone.
            java.util.ArrayList<Char> hit = new java.util.ArrayList<>();
            for (int cell : cells) { Char ch = Actor.findChar(cell); if (ch != null && ch != this) hit.add(ch); }
            for (Char ch : hit) {
                ch.damage(Math.round(Random.NormalIntRange(8, 14) * BalanceTuning.multiplier(DRAGON_DAMAGE)), this);
                if (!ch.isAlive()) continue;
                Ballistica ray = new Ballistica(pos, ch.pos, Ballistica.WONT_STOP);
                int end = ray.path.get(ray.path.size() - 1);
                WandOfBlastWave.throwChar(ch, new Ballistica(ch.pos, end, Ballistica.STOP_SOLID), BalanceTuning.get(DRAGON_KNOCKBACK),
                        false, false, ExpeditionDragon.class, new WandOfBlastWave.LandingRules(true) {
                            @Override public void collide(Char target, int moved) {}
                        });
            }
        }
    }
    private class DragonHunting extends Mob.Hunting {
        @Override public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (pending != Attack.NONE) { release(); spend(TICK); return true; }
            if (enemyInFOV && !isCharmedBy(enemy)) {
                int distance = Dungeon.level.distance(pos, enemy.pos);
                if (distance <= 2 && wingDelay <= 0 && Random.Int(3) == 0 && prepare(Attack.WINGBEAT, enemy.pos)
                        || distance > 1 && distance <= 6 && breathDelay <= 0 && prepare(Attack.BREATH, enemy.pos)) {
                    spend(TICK); return true;
                }
            }
            return super.act(enemyInFOV, justAlerted);
        }
    }
    @Override public void die(Object cause) {
        if (DragonExpedition.dragonSlain) return;
        DragonExpedition.dragonSlain = true;
        DragonExpedition.victoryPending = true;
        pending = Attack.NONE;
        GLog.p(Messages.get(this, "defeated"));
        super.die(cause);
        DragonExpedition.queueVictory();
    }
    @Override public void storeInBundle(Bundle b) {
        HP = Math.min(HP, lowestHP);
        super.storeInBundle(b); b.put("pending_attack", pending); b.put("aim", aim); b.put("origin", origin);
        b.put("breath_delay", breathDelay); b.put("wing_delay", wingDelay); b.put("lowest_hp", lowestHP);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); pending = b.getEnum("pending_attack", Attack.class);
        aim = b.getInt("aim"); origin = b.getInt("origin");
        breathDelay = b.getFloat("breath_delay"); wingDelay = b.getFloat("wing_delay");
        lowestHP = b.contains("lowest_hp") ? b.getInt("lowest_hp") : HP; HP = Math.min(HP, lowestHP);
    }
}
