// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.BalanceTuning;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.levels.DragonCavernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpinnerSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class Broodmother extends Mob {
    public boolean replenished;
    public int hatched, hatchCooldown = BalanceTuning.get(BROOD_INTERVAL), hatchCell = -1;
    { spriteClass = com.shatteredpixel.shatteredpixeldungeon.sprites.BroodmotherSprite.class; HP = HT = BalanceTuning.get(BROOD_HEALTH); defenseSkill = 18; EXP = 25; maxLvl = 30;
        properties.add(Property.BOSS); properties.add(Property.LARGE);
        resistances.add(Poison.class); HUNTING = new BroodHunting(); }
    @Override public String description() { return Messages.get(this, "desc", BalanceTuning.get(BROOD_TOTAL), BalanceTuning.get(BROOD_LIVE)); }
    @Override public int attackSkill(Char enemy) { return 26; }
    @Override public int damageRoll() { return Math.round(Random.NormalIntRange(16, 24) * BalanceTuning.multiplier(BROOD_DAMAGE)); }
    @Override public int drRoll() { return Random.NormalIntRange(2, 7); }
    @Override public int attackProc(Char enemy, int damage) {
        if (BalanceTuning.get(BROOD_POISON) > 0) {
            Poison poison = Buff.affect(enemy, Poison.class);
            if (poison != null) poison.set(BalanceTuning.get(BROOD_POISON));
        }
        return super.attackProc(enemy, damage);
    }
    @Override protected boolean act() {
        if (isAlive() && Dungeon.level.heroFOV[pos] && !BossHealthBar.isAssigned()) BossHealthBar.assignBoss(this);
        return super.act();
    }
    @Override public void damage(int damage, Object source) {
        if (isAlive() && Dungeon.level.mobs.contains(this)) BossHealthBar.assignBoss(this);
        super.damage(damage, source);
    }
    @Override public void notice() { if (sprite != null) super.notice(); BossHealthBar.assignBoss(this); }
    public void alertArrival() { target = Dungeon.hero.pos; aggro(Dungeon.hero); }

    public boolean canHatch() {
        int alive = 0, brood = 0;
        for (Mob mob : Dungeon.level.mobs) if (mob instanceof CavernSpinner && mob.isAlive()) {
            alive++; if (((CavernSpinner) mob).hatchling) brood++;
        }
        return isAlive() && hatched < BalanceTuning.get(BROOD_TOTAL) && brood < BalanceTuning.get(BROOD_LIVE) && alive < BalanceTuning.get(SPIDERS_CAP);
    }
    private int chooseHatchCell() {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int d : PathFinder.NEIGHBOURS8) {
            int c = pos + d;
            if (Dungeon.level.insideMap(c) && Dungeon.level.passable[c] && Actor.findChar(c) == null) cells.add(c);
        }
        return cells.isEmpty() ? -1 : Random.element(cells);
    }
    public boolean hatch() {
        int c = hatchCell; hatchCell = -1;
        if (!canHatch() || !Dungeon.level.insideMap(c) || !Dungeon.level.passable[c] || Actor.findChar(c) != null) return false;
        CavernSpinner spider = new CavernSpinner(); spider.setHatchling(); spider.pos = c;
        spider.aggro(Dungeon.hero); GameScene.add(spider, 1f); hatched++;
        return true;
    }
    private class BroodHunting extends Mob.Hunting {
        @Override public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (hatchCell >= 0) { hatch(); hatchCooldown = BalanceTuning.get(BROOD_INTERVAL); spend(TICK); return true; }
            if (enemyInFOV && --hatchCooldown <= 0 && canHatch()) {
                hatchCell = chooseHatchCell();
                if (hatchCell >= 0) {
                    GameScene.targetedCell(hatchCell, 1f);
                    if (Dungeon.level.heroFOV[pos]) {
                        GLog.w(Messages.get(Broodmother.class, "hatching")); Dungeon.hero.interrupt();
                    }
                    spend(TICK); return true;
                }
            }
            return super.act(enemyInFOV, justAlerted);
        }
    }
    @Override public void die(Object cause) {
        DragonExpedition.spiderSlain = true;
        GLog.p(Messages.get(this, "defeated"));
        super.die(cause);
    }
    public void setReplenished() { replenished = true; generatedRespawn = true; EXP = 0; lootChance = 0; }
    public void patrolToward(int cell) { state = WANDERING; target = cell; }
    @Override public com.shatteredpixel.shatteredpixeldungeon.items.Item createLoot() {
        return replenished ? null : super.createLoot();
    }
    @Override public void rollToDropLoot() { if (!replenished) super.rollToDropLoot(); }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b); b.put("hatched", hatched); b.put("hatch_cooldown", hatchCooldown); b.put("hatch_cell", hatchCell); b.put("replenished", replenished);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); hatched = b.getInt("hatched"); hatchCooldown = b.getInt("hatch_cooldown"); hatchCell = b.getInt("hatch_cell");
        if (b.getBoolean("replenished")) setReplenished();
    }
}
