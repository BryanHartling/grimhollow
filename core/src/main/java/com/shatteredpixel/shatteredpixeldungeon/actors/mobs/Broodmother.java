// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

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
    public int hatched, hatchCooldown = 5, hatchCell = -1;
    { spriteClass = SpinnerSprite.class; HP = HT = 240; defenseSkill = 18; EXP = 25; maxLvl = 30;
        properties.add(Property.BOSS); properties.add(Property.LARGE);
        resistances.add(Poison.class); HUNTING = new BroodHunting(); }
    @Override public int attackSkill(Char enemy) { return 26; }
    @Override public int damageRoll() { return Random.NormalIntRange(16, 24); }
    @Override public int drRoll() { return Random.NormalIntRange(2, 7); }
    @Override public int attackProc(Char enemy, int damage) {
        Poison poison = Buff.affect(enemy, Poison.class);
        if (poison != null) poison.set(6);
        return super.attackProc(enemy, damage);
    }
    @Override public void notice() { if (sprite != null) super.notice(); BossHealthBar.assignBoss(this); }
    public void alertArrival() { target = Dungeon.hero.pos; aggro(Dungeon.hero); }

    public boolean canHatch() {
        int alive = 0, brood = 0;
        for (Mob mob : Dungeon.level.mobs) if (mob instanceof CavernSpinner && mob.isAlive()) {
            alive++; if (((CavernSpinner) mob).hatchling) brood++;
        }
        return !DragonExpedition.spiderSlain && hatched < 6 && brood < 3 && alive < 9;
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
            if (hatchCell >= 0) { hatch(); hatchCooldown = 5; spend(TICK); return true; }
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
        if (Dungeon.level instanceof DragonCavernLevel) ((DragonCavernLevel) Dungeon.level).clearBrood();
        GLog.p(Messages.get(this, "defeated"));
        super.die(cause);
    }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b); b.put("hatched", hatched); b.put("hatch_cooldown", hatchCooldown); b.put("hatch_cell", hatchCell);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); hatched = b.getInt("hatched"); hatchCooldown = b.getInt("hatch_cooldown"); hatchCell = b.getInt("hatch_cell");
    }
}
