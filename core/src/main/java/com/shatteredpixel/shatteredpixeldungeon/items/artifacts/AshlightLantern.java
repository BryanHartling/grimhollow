// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Soulfire;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.LightMap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
import java.util.HashSet;

/** Feeding grows the artifact; environmental darkness supplies its separate fuel. */
public class AshlightLantern extends Artifact {
    public static final String AC_FEED="FEED", AC_FLARE="FLARE", AC_SHUTTER="SHUTTER", AC_OPEN="OPEN";
    private boolean shuttered;
    private transient Level litLevel;
    private transient boolean[] lit;

    { image=ItemSpriteSheet.ASHLIGHT_OPEN; levelCap=10; chargeCap=3; defaultAction=AC_FLARE; }

    public int charges(){ return charge; }
    public int capacity(){ return 3+level()/3; }
    public int feedProgress(){ return exp; }
    public int feedCost(){ return level()<6 ? 1 : 2; }
    public int lightBonus(){ return 2+level()/5; }
    public boolean shuttered(){ return shuttered; }
    public float turnsPerCharge(){ return (40-2*level())/(shuttered ? 2f : 1f); }

    public static AshlightLantern equipped(Char ch){
        if (!(ch instanceof Hero)) return null;
        Hero h=(Hero)ch;
        if (h.belongings.artifact instanceof AshlightLantern) return (AshlightLantern)h.belongings.artifact;
        if (h.belongings.misc instanceof AshlightLantern) return (AshlightLantern)h.belongings.misc;
        return null;
    }
    public static AshlightLantern open(Char ch){
        AshlightLantern item=equipped(ch);
        return item!=null && !item.shuttered && !item.cursed && ch.buff(MagicImmune.class)==null ? item : null;
    }
    public static int sightRadius(Char ch, float ordinary){
        AshlightLantern item=open(ch);
        return Math.min(ShadowCaster.MAX_DISTANCE, Math.round(item==null ? ordinary : ordinary+item.lightBonus()));
    }
    public static int awarenessBonus(){
        AshlightLantern item=open(Dungeon.hero);
        return item==null || Dungeon.hero.invisible>0 ? 0 : item.lightBonus();
    }
    public static boolean deniesEntry(Mob mob,int cell){
        AshlightLantern item=open(Dungeon.hero);
        return item!=null && item.level()>=4 && mob.alignment==Char.Alignment.ENEMY
                && mob instanceof Wraith && item.lights(cell);
    }
    public boolean lights(int cell){
        return litLevel==Dungeon.level && lit!=null && cell>=0 && cell<lit.length && lit[cell];
    }
    /** Called before Mind Vision, ally sight, or other non-optical senses join the hero FOV. */
    public void rememberLight(Level level,boolean[] directSight){
        litLevel=level;
        if(lit==null || lit.length!=directSight.length)lit=new boolean[directSight.length];
        System.arraycopy(directSight,0,lit,0,lit.length);
        if(level()>=6)for(int cell=0;cell<lit.length;cell++)
            if(lit[cell] && (level.map[cell]==Terrain.SECRET_DOOR || level.map[cell]==Terrain.SECRET_TRAP))level.discover(cell);
    }

    public boolean toggle(Hero hero){
        if(!isEquipped(hero))return false;
        shuttered=!shuttered;
        updateImage();
        refresh(hero);
        return true;
    }
    private void updateImage(){ image=shuttered ? ItemSpriteSheet.ASHLIGHT_CLOSED : ItemSpriteSheet.ASHLIGHT_OPEN; updateQuickslot(); }
    @Override protected void onPlaytestLevelSet(){if(isEquipped(Dungeon.hero))refresh(Dungeon.hero);}
    private void refresh(Char ch){
        litLevel=null;
        if(open(ch)==this){
            Buff.detach(ch,Blindness.class);
            if(level()>=10)Buff.detach(ch,Burning.class);
        }
        if(ch==Dungeon.hero && Dungeon.level!=null)Dungeon.observe();
    }
    @Override public void activate(Char ch){ super.activate(ch); refresh(ch); }
    @Override public boolean doUnequip(Hero h,boolean collect,boolean single){
        if(!super.doUnequip(h,collect,single))return false;
        refresh(h); return true;
    }
    @Override public Item upgrade(){ return this; } // Only the feed path may call the parent upgrade.
    @Override public void transferUpgrade(int amount){} // No transmutation/infusion growth.
    @Override public void resetForTrinity(int visibleLevel){} // A spirit copy cannot feed this artifact.

    public static int feedValue(Item item){
        if(item instanceof PotionOfDragonsBreath || item instanceof Soulfire)return 2;
        return item instanceof PotionOfLiquidFlame ? 1 : 0;
    }
    public boolean canFeed(Item item){ return item!=null && item.isIdentified() && feedValue(item)>0 && level()<levelCap; }
    public boolean feed(Hero hero,Item item){
        if(!isEquipped(hero) || cursed || hero.buff(MagicImmune.class)!=null || !canFeed(item)
                || !hero.belongings.contains(item))return false;
        int value=feedValue(item), previousLevel=level();
        if(item.detach(hero.belongings.backpack)==null)return false;
        exp+=value;
        while(level()<levelCap && exp>=feedCost()){
            exp-=feedCost(); super.upgrade();
        }
        if(level()==levelCap)exp=0;
        chargeCap=capacity();
        if(level()>previousLevel){
            com.shatteredpixel.shatteredpixeldungeon.journal.Catalog.countUse(AshlightLantern.class);
            GLog.p(Messages.get(this,"levelup",level()));
        }
        refresh(hero); updateQuickslot();
        hero.spendAndNext(1f);
        return true;
    }
    public WndBag.ItemSelector feedSelector(final Hero hero){
        return new WndBag.ItemSelector(){
            @Override public String textPrompt(){return Messages.get(AshlightLantern.this,"feed_prompt");}
            @Override public boolean itemSelectable(Item item){return canFeed(item);}
            @Override public void onSelect(Item item){if(item!=null)feed(hero,item);}
        };
    }
    public boolean flare(Hero hero){
        if(open(hero)!=this || charge<1)return false;
        Level level=Dungeon.level;
        boolean[] burst=new boolean[level.length()];
        ShadowCaster.castShadow(hero.pos%level.width(),hero.pos/level.width(),level.width(),burst,level.losBlocking,3);
        charge--;
        for(Mob mob:level.mobs.toArray(new Mob[0]))if(mob.alignment==Char.Alignment.ENEMY && burst[mob.pos]){
            Buff.prolong(mob,Blindness.class,4f);
            if(level()>=8){ Burning burn=Buff.affect(mob,Burning.class); if(burn!=null)burn.reignite(mob); }
        }
        if(level()>=2)for(int cell=0;cell<burst.length;cell++)if(burst[cell]
                && level.map[cell]!=Terrain.BONE_WALL && level.map[cell]!=Terrain.FORCE_WALL
                && (level.flamable[cell] || Blob.volumeAt(cell,Web.class)>0)){
            GameScene.add(Blob.seed(cell,4,Fire.class));
        }
        if(hero.sprite!=null && hero.sprite.parent!=null)hero.sprite.emitter().burst(com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle.FACTORY,16);
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.onArtifactUsed(hero);
        updateQuickslot(); hero.spendAndNext(1f); return true;
    }
    @Override public ArrayList<String> actions(Hero hero){
        ArrayList<String> result=super.actions(hero);
        if(isEquipped(hero)){
            result.add(shuttered ? AC_OPEN : AC_SHUTTER);
            if(!cursed && hero.buff(MagicImmune.class)==null){
                if(level()<levelCap)result.add(AC_FEED);
                if(!shuttered && charge>0)result.add(AC_FLARE);
            }
        }
        return result;
    }
    @Override public void execute(Hero hero,String action){
        super.execute(hero,action);
        if(AC_SHUTTER.equals(action) || AC_OPEN.equals(action))toggle(hero);
        else if(AC_FEED.equals(action)){
            if(isEquipped(hero) && !cursed && hero.buff(MagicImmune.class)==null && level()<levelCap)GameScene.selectItem(feedSelector(hero));
            else GLog.w(Messages.get(this,"unavailable"));
        }else if(AC_FLARE.equals(action) && !flare(hero))GLog.w(Messages.get(this,"unavailable"));
    }
    @Override public String desc(){
        return Messages.get(this,"desc")+"\n\n"+Messages.get(this,"lore")+"\n\n"
                +Messages.get(this,"rules",lightBonus(),capacity(),Math.round(turnsPerCharge()),level(),exp,feedCost())
                +"\n\n"+Messages.get(this,"riders")+"\n\n"+Messages.get(this,shuttered ? "closed" : "open");
    }
    @Override protected ArtifactBuff passiveBuff(){return new EmberKeeper();}
    public class EmberKeeper extends ArtifactBuff {
        @Override public boolean act(){
            chargeCap=capacity();
            if(!cursed && target.buff(MagicImmune.class)==null && charge<chargeCap && lowAmbient(Dungeon.level,target.pos)){
                partialCharge+=1f/turnsPerCharge();
                if(partialCharge>=.99999f){charge++;partialCharge=Math.max(0,partialCharge-1);updateQuickslot();}
                if(charge==chargeCap)partialCharge=0;
            }
            if(open(target)==AshlightLantern.this){
                Buff.detach(target,Blindness.class);
                if(level()>=10)Buff.detach(target,Burning.class);
            }
            spend(TICK); return true;
        }
        @Override public HashSet<Class> resistances(){
            HashSet<Class> result=new HashSet<>();
            if(open(target)==AshlightLantern.this && level()>=6)addFire(result);
            return result;
        }
        @Override public HashSet<Class> immunities(){
            HashSet<Class> result=new HashSet<>();
            if(open(target)==AshlightLantern.this){result.add(Blindness.class);if(level()>=10)addFire(result);}
            return result;
        }
    }
    private static void addFire(HashSet<Class> result){
        result.add(Burning.class); result.add(Fire.class); result.add(WandOfFireblast.class);
    }

    /** Match environmental light falloff, independent of graphics settings and personal light. */
    public static boolean lowAmbient(Level level,int cell){
        if(level==null || cell<0 || cell>=level.length())return false;
        float added=0;
        int x=cell%level.width(),y=cell/level.width();
        boolean lava=Assets.Environment.WATER_HALLS.equals(level.waterTex());
        for(int sy=Math.max(0,y-4);sy<=Math.min(level.height()-1,y+4);sy++)
            for(int sx=Math.max(0,x-4);sx<=Math.min(level.width()-1,x+4);sx++){
                int source=sx+sy*level.width();
                float distance=(float)Math.hypot(sx-x,sy-y);
                if(level.map[source]==Terrain.WALL_DECO)added+=.258f*LightMap.falloff(distance,Dungeon.depth>=16 && Dungeon.depth<=20 ? 4 : 3);
                if(lava && level.water[source])added+=.263f*LightMap.falloff(distance,1.25f);
                for(Blob blob:level.blobs.values())if(blob.cur!=null && blob.volume>0 && blob.cur[source]>0){
                    String type=blob.getClass().getSimpleName();
                    float radius=2, strength=0;
                    if(blob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Soulfire
                            || blob instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom.EternalFire)strength=.204f;
                    else if(type.contains("Fire"))strength=.248f;
                    else if(type.contains("Electric"))strength=.292f;
                    else if(type.contains("Freez") || type.contains("Frost")){strength=.27f;radius=1;}
                    else if(type.contains("Corro") || type.contains("Toxic"))strength=.257f;
                    added+=strength*LightMap.falloff(distance,radius);
                }
            }
        return added<.06f;
    }
    @Override public void storeInBundle(Bundle bundle){super.storeInBundle(bundle);bundle.put("shuttered",shuttered);}
    @Override public void restoreFromBundle(Bundle bundle){
        super.restoreFromBundle(bundle);
        level(Math.max(0,Math.min(levelCap,bundle.getInt("level"))));
        chargeCap=capacity(); charge=Math.max(0,Math.min(chargeCap,bundle.getInt("charge")));
        shuttered=bundle.getBoolean("shuttered"); updateImage(); litLevel=null;
    }
}
