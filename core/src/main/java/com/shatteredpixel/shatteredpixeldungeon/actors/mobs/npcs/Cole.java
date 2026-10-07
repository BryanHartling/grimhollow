// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract;
import com.watabou.noosa.Game;

public class Cole extends NPC {
    public boolean combatConfigured, healed, smoked, repositioned, reinforcementCalled, lootDropped;
    public int region, personalGold, bolts=12;
    public com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Bolas bolas;
    public com.shatteredpixel.shatteredpixeldungeon.items.Item prize;
    private boolean throwingBolas;
    { spriteClass = com.shatteredpixel.shatteredpixeldungeon.sprites.ColeSprite.class; properties.add(Property.IMMOVABLE); }
    public void startCombat(){
        if(combatConfigured)return;
        combatConfigured=true;region=Math.max(0,Math.min(4,(BountyBoard.deepestMain-1)/5));
        HT=HP=Math.round(new int[]{12,25,40,70,80}[region]*com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.multiplier(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.COLE_HEALTH));
        defenseSkill=Math.round(new int[]{5,10,15,25,25}[region]*com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.multiplier(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.COLE_EVASION));
        wantedDamage=com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.multiplier(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.COLE_DAMAGE);
        EXP=5+region*3;maxLvl=30;alignment=Alignment.ENEMY;state=HUNTING;properties.remove(Property.IMMOVABLE);properties.add(Property.MINIBOSS);
        bolas=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Bolas();bolas.quantity(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.get(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.COLE_BOLAS));
        personalGold=200+100*region;
        com.watabou.utils.Random.pushGenerator(Dungeon.seed ^ 0x434F4C454C4F4F54L);
        try{prize=BountyBoard.qualityPrize(4,2);}finally{com.watabou.utils.Random.popGenerator();}
    }
    @Override public int defenseSkill(Char enemy) { return combatConfigured?super.defenseSkill(enemy):INFINITE_EVASION; }
    @Override public int attackSkill(Char enemy){return new int[]{10,20,20,30,35}[region];}
    @Override public int damageRoll(){return throwingBolas?com.watabou.utils.Random.NormalIntRange(bolas.min(0),bolas.max(0)):
        com.watabou.utils.Random.NormalIntRange(new int[]{2,4,5,12,25}[region],new int[]{8,12,25,25,30}[region]);}
    @Override public boolean attack(Char enemy,float multiplier,float bonus,float accuracy){
        float eliteDamage=wantedDamage;
        try{if(throwingBolas)wantedDamage=1f;return super.attack(enemy,multiplier,bonus,accuracy);}
        finally{wantedDamage=eliteDamage;}
    }
    @Override public void damage(int damage, Object source) {if(combatConfigured)super.damage(damage,source);}
    @Override public boolean add(Buff buff) { return combatConfigured&&super.add(buff); }
    @Override protected boolean canAttack(Char enemy){
        return super.canAttack(enemy)||(bolts>0||bolas!=null&&bolas.quantity()>0)&&Dungeon.level.distance(pos,enemy.pos)<=6
            &&new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(pos,enemy.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos==enemy.pos;
    }
    @Override protected boolean act(){
        if(combatConfigured&&BountyBoard.responsePending){spend(TICK);return true;}
        if(combatConfigured&&HP>0&&paralysed==0&&buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep.class)==null){
            if(!healed&&HP<HT/3){healed=true;com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.heal(this);spend(TICK);return true;}
            if(enemySeen&&!reinforcementCalled&&HP<HT*2/3){
                for(int offset:com.watabou.utils.PathFinder.NEIGHBOURS8){int cell=pos+offset;
                    if(Dungeon.level.insideMap(cell)&&Dungeon.level.passable[cell]&&!Dungeon.level.pit[cell]&&com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(cell)==null){
                        reinforcementCalled=true;com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter();
                        hunter.configure(Math.max(11,BountyBoard.deepestMain),0);hunter.coleReinforcement=true;hunter.pos=cell;Dungeon.level.mobs.add(hunter);GameScene.add(hunter);spend(TICK);return true;
                    }
                }
            }
            if(enemySeen&&!smoked&&HP<HT/2){smoked=true;new com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShroudingFog().shatter(pos);spend(TICK);return true;}
            if(enemySeen&&!repositioned&&HP<HT/4&&com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation.teleportChar(this)){repositioned=true;spend(TICK);return true;}
        }
        return super.act();
    }
    @Override protected boolean doAttack(Char enemy){
        if(Dungeon.level.adjacent(pos,enemy.pos))return super.doAttack(enemy);
        throwingBolas=bolas!=null&&bolas.quantity()>0&&enemy.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class)==null;
        if(throwingBolas)bolas.quantity(bolas.quantity()-1);else if(bolts>0)bolts--;else {throwingBolas=true;bolas.quantity(bolas.quantity()-1);}
        if(sprite!=null&&(sprite.visible||enemy.sprite!=null&&enemy.sprite.visible)){sprite.zap(enemy.pos);return false;}
        attack(enemy);throwingBolas=false;com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility.dispel(this);spend(attackDelay());return true;
    }
    public com.shatteredpixel.shatteredpixeldungeon.items.Item projectile(){return throwingBolas?bolas:new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart();}
    @Override public int attackProc(Char enemy,int damage){damage=super.attackProc(enemy,damage);return throwingBolas?bolas.proc(this,enemy,damage):damage;}
    public void onZapComplete(){attack(enemy);throwingBolas=false;com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility.dispel(this);spend(attackDelay());next();}
    @Override public void rollToDropLoot(){
        if(!combatConfigured||lootDropped)return;lootDropped=true;
        int gold=BountyBoard.coleDefeated(this)+personalGold;
        if(gold>0)Dungeon.level.drop(BountyBoard.deathGold(gold),pos);
        if(prize!=null)Dungeon.level.drop(prize,pos);
        if(!healed)Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing(),pos);
        if(!smoked)Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShroudingFog(),pos);
        if(!repositioned)Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift(),pos);
        if(bolas!=null&&bolas.quantity()>0)Dungeon.level.drop(bolas,pos);
    }
    @Override public void die(Object source){rollToDropLoot();super.die(source);}
    @Override public void storeInBundle(com.watabou.utils.Bundle b){
        super.storeInBundle(b);b.put("combat",combatConfigured);b.put("region",region);b.put("personal_gold",personalGold);b.put("bolts",bolts);
        b.put("healed",healed);b.put("smoked",smoked);b.put("repositioned",repositioned);b.put("reinforcement",reinforcementCalled);b.put("loot_dropped",lootDropped);b.put("bolas",bolas);b.put("prize",prize);b.put("evasion",defenseSkill);
    }
    @Override public void restoreFromBundle(com.watabou.utils.Bundle b){
        combatConfigured=b.getBoolean("combat");super.restoreFromBundle(b);region=b.getInt("region");personalGold=b.getInt("personal_gold");bolts=b.contains("bolts")?b.getInt("bolts"):12;
        healed=b.getBoolean("healed");smoked=b.getBoolean("smoked");repositioned=b.getBoolean("repositioned");reinforcementCalled=b.getBoolean("reinforcement");lootDropped=b.getBoolean("loot_dropped");
        bolas=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Bolas)b.get("bolas");prize=(com.shatteredpixel.shatteredpixeldungeon.items.Item)b.get("prize");
        if(combatConfigured){properties.remove(Property.IMMOVABLE);properties.add(Property.MINIBOSS);defenseSkill=b.getInt("evasion");EXP=5+region*3;}
    }
    @Override public boolean reset() { return true; }
    @Override public boolean interact(Char ch) {
        if (ch == Dungeon.hero) Game.runOnRenderThread(() -> {
            if(BountyBoard.betrayed){BountyBoard.showMeeting(Cole.this,Dungeon.depth==22);return;}
            BountyBoard.planContracts();
            java.util.ArrayList<String> options=new java.util.ArrayList<>();
            for(int i=0;i<3;i++)options.add(BountyBoard.contracts[i].title());
            if(BountyBoard.bossUnlocked()&&!BountyBoard.bossDefeated){BountyBoard.planBoss();options.add(BountyBoard.contracts[3].title());}
            GameScene.show(new WndOptions(sprite(), name(),
                    Messages.get(this, "greet_" + Dungeon.hero.heroClass.name())
                            + (BountyBoard.shopClosed ? "\n\n" + Messages.get(this, "closed") : ""),
                    options.toArray(new String[0])) {
                @Override protected void onSelect(int index) { GameScene.show(new WndBountyContract(BountyBoard.contracts[index])); }
            });
        });
        return true;
    }
}
