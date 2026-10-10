// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.*;

/** A dropped pet, not a loot-bearing combat mimic. Deadlines use the run clock. */
public class AbandonedHatchling extends NPC {
    private HatchlingMimic pet;
    private float leaveAt, escapeAt;
    private boolean scurrying;
    { spriteClass=PetSprite.class; maxLvl=0; properties.add(Property.IMMOVABLE); }

    public static float clock(){return Statistics.duration+Actor.now();}
    public static AbandonedHatchling leave(HatchlingMimic pet,int origin){
        int cell=HatchlingMimic.closestSpawn(origin);
        if(cell<0){GLog.w(Messages.get(AbandonedHatchling.class,"no_space"));return null;}
        AbandonedHatchling stray=new AbandonedHatchling();
        stray.pet=pet;stray.pos=cell;
        int level=Math.max(0,Math.min(3,pet.level()));
        stray.leaveAt=clock()+Random.IntRange(120-20*level,180-30*level);
        stray.escapeAt=stray.leaveAt+20;
        pet.detachAll(Dungeon.hero.belongings.backpack);
        if(com.watabou.noosa.Game.scene() instanceof GameScene)GameScene.add(stray);
        else{Dungeon.level.mobs.add(stray);Actor.add(stray);}
        GLog.w(Messages.get(stray,"put_down"));
        return stray;
    }
    public HatchlingMimic pet(){return pet;}
    @Override public String name(){return Messages.get(HatchlingMimic.class,"name");}
    @Override public String description(){return Messages.get(this,scurrying?"scurrying_desc":"waiting_desc");}
    @Override public int defenseSkill(Char enemy){return INFINITE_EVASION;}
    @Override public boolean isInvulnerable(Class effect){return true;}
    @Override public void damage(int damage,Object source){}
    @Override public boolean add(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff){return false;}
    @Override public boolean interact(Char ch){
        if(ch!=Dungeon.hero || !Dungeon.level.adjacent(pos,ch.pos) || pet==null)return false;
        HatchlingMimic recovered=pet;
        if(!recovered.doPickUp((Hero)ch)){
            GLog.w(Messages.get(this,"full_pack"));return true;
        }
        pet=null;remove();
        GLog.w(Messages.get(this,"recovered"));
        return true;
    }
    private void remove(){destroy();if(sprite!=null)sprite.killAndErase();}
    private void escape(){
        if(pet!=null)GLog.w(Messages.get(this,"escaped"));
        pet=null;remove();
    }
    @Override public boolean act(){
        if(pet==null){remove();return true;}
        // The clock includes time on other floors and survives Actor.fixTime().
        float now=clock();
        if(now>=escapeAt){escape();return true;}
        if(now>=leaveAt){
            if(!scurrying){
                scurrying=true;
                Dungeon.hero.interrupt();Dungeon.hero.lastAction=null;Dungeon.hero.resting=false;
                GLog.w(Messages.get(this,"leaving"));
                spend(TICK);return true; // Give a fresh action before it starts moving.
            }
            int exit=Dungeon.level.exit();
            if(exit<0)exit=Dungeon.level.entrance();
            if(pos==exit && now>=leaveAt+3){escape();return true;}
            if(fieldOfView==null || fieldOfView.length!=Dungeon.level.length())fieldOfView=new boolean[Dungeon.level.length()];
            Dungeon.level.updateFieldOfView(this,fieldOfView);
            int old=pos;
            boolean moved=exit>=0 && getCloser(exit);
            spend(TICK);
            if(moved && sprite!=null)return moveSprite(old,pos);
            return true;
        }
        spend(TICK);return true;
    }
    @Override public void storeInBundle(Bundle b){
        super.storeInBundle(b);b.put("pet",pet);b.put("leave_at",leaveAt);b.put("escape_at",escapeAt);b.put("scurrying",scurrying);
    }
    @Override public void restoreFromBundle(Bundle b){
        super.restoreFromBundle(b);pet=(HatchlingMimic)b.get("pet");leaveAt=b.getFloat("leave_at");escapeAt=b.getFloat("escape_at");scurrying=b.getBoolean("scurrying");
    }
    /** Reuse the pet's existing painted inventory art with an eight-unit body. */
    public static class PetSprite extends CharSprite {
        public PetSprite(){
            texture(Assets.Sprites.ITEMS);
            TextureFilm film=new TextureFilm(texture);
            film.add(0,ItemSpriteSheet.film.get(ItemSpriteSheet.HATCHLING_MIMIC));
            idle=new Animation(1,true);idle.frames(film,0);
            run=idle.clone();attack=idle.clone();die=idle.clone();
            play(idle);
        }
        @Override public float visualFootprint(){return 8f;}
    }
}
