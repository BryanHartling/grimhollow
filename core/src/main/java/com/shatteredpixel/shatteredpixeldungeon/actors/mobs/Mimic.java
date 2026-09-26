/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Mimic extends Mob {
    public boolean hatchlingBorn;
    private boolean pursuingHatchling;
    public HatchlingMimic stolenHatchling;
	
	private int level;
	
	{
		spriteClass = MimicSprite.class;

		properties.add(Property.DEMONIC);

		EXP = 0;
		
		//mimics are neutral when hidden
		alignment = Alignment.NEUTRAL;
		state = PASSIVE;
	}
	
	public ArrayList<Item> items;

	private boolean stealthy = false;
	
	private static final String LEVEL	= "level";
	private static final String ITEMS	= "items";
	private static final String STEALTHY= "stealthy";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (items != null) bundle.put( ITEMS, items );
		bundle.put( LEVEL, level );
		bundle.put( STEALTHY, stealthy );
        bundle.put("hatchling_born", hatchlingBorn);
        bundle.put("pursuing_hatchling", pursuingHatchling);
        if (stolenHatchling != null) bundle.put("stolen_hatchling", stolenHatchling);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		if (bundle.contains( ITEMS )) {
			items = new ArrayList<>((Collection<Item>) ((Collection<?>) bundle.getCollection(ITEMS)));
		}
		level = bundle.getInt( LEVEL );
		adjustStats(level);
		stealthy = bundle.getBoolean(STEALTHY);
        hatchlingBorn=bundle.getBoolean("hatchling_born");
        pursuingHatchling=bundle.getBoolean("pursuing_hatchling");
        if(bundle.contains("stolen_hatchling")) stolenHatchling=(HatchlingMimic)bundle.get("stolen_hatchling");
		super.restoreFromBundle(bundle);
		if (state != PASSIVE && alignment == Alignment.NEUTRAL){
			alignment = Alignment.ENEMY;
		}
	}

	@Override
	public boolean add(Buff buff) {
		if (super.add(buff)) {
			if (buff.type == Buff.buffType.NEGATIVE && alignment == Alignment.NEUTRAL) {
				alignment = Alignment.ENEMY;
				stopHiding();
				if (sprite != null) sprite.idle();
			}
			return true;
		}
		return false;
	}

	@Override
	public String name() {
		if (alignment == Alignment.NEUTRAL){
			return Messages.get(Heap.class, "chest");
		} else {
			return super.name();
		}
	}

	@Override
	public String description() {
		if (alignment == Alignment.NEUTRAL){
			if (MimicTooth.stealthyMimics()){
				return Messages.get(Heap.class, "chest_desc");
			} else {
				return Messages.get(Heap.class, "chest_desc") + "\n\n" + Messages.get(this, "hidden_hint");
			}
		} else {
			return super.description();
		}
	}

	@Override
	protected boolean act() {
        if (actForHatchling()) return true;
		if (alignment == Alignment.NEUTRAL && state != PASSIVE){
			alignment = Alignment.ENEMY;
			if (sprite != null) sprite.idle();
			if (Dungeon.level.heroFOV[pos]) {
				GLog.w(Messages.get(this, "reveal") );
				CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
				Sample.INSTANCE.play(Assets.Sounds.MIMIC);
			}
		}
		return super.act();
	}

	@Override
	public CharSprite sprite() {
		MimicSprite sprite = (MimicSprite) super.sprite();
		if (alignment == Alignment.NEUTRAL) sprite.hideMimic(this);
		return sprite;
	}

    public void kinship(boolean friendly) {
        intelligentAlly = friendly;
        state = friendly ? WANDERING : HUNTING;
    }
    /** Set combat intent before GameScene creates the new creature's sprite. */
    public void wakeHatchling() {
        hatchlingBorn = true; alignment = Alignment.ENEMY; state = HUNTING;
        enemy = Dungeon.hero; target = Dungeon.hero.pos; enemySeen = true;
    }

    private boolean sharesRoomWithHero() {
        if (Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel) {
            com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel level =
                    (com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)Dungeon.level;
            return level.room(pos) != null && level.room(pos) == level.room(Dungeon.hero.pos);
        }
        return Dungeon.level.heroFOV[pos];
    }

    private boolean hatchlingStep(int destination, boolean away) {
        if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) fieldOfView=new boolean[Dungeon.level.length()];
        Dungeon.level.updateFieldOfView(this, fieldOfView);
        int old=pos;
        boolean moved=away ? getFurther(destination) : getCloser(destination);
        if (moved && sprite != null) sprite.move(old,pos);
        return moved;
    }

    /** Returns true when kin recognition, theft or flight consumed this creature's turn. */
    public boolean actForHatchling() {
        if (paralysed > 0 || !isAlive()) return false;
        if (stolenHatchling != null) {
            int exit= Dungeon.level.exit();
            if (pos == exit && !Dungeon.level.heroFOV[pos] && Dungeon.level.distance(pos,Dungeon.hero.pos)>=6) {
                escapeWithHatchling();
            } else {
                if (exit >= 0) hatchlingStep(exit,false);
                spend(1/speed());
            }
            return true;
        }
        HatchlingMimic hatchling=HatchlingMimic.carried();
        if (hatchling == null || hatchlingBorn || alignment == Alignment.ALLY) return false;
        if (this instanceof EbonyMimic) {
            if (alignment == Alignment.NEUTRAL && sharesRoomWithHero()) {
                stopHiding(); alignment=Alignment.ENEMY;
                // Wake on room entry; normal turn scheduling and attack speed still apply.
            }
            return false;
        }
        if (this instanceof CrystalMimic) {
            if (!pursuingHatchling && sharesRoomWithHero()) {
                pursuingHatchling=true; alignment=Alignment.ENEMY; state=HUNTING;
                if (sprite!=null) sprite.idle();
                GLog.w(Messages.get(HatchlingMimic.class,"theft_notice"));
            }
            if (pursuingHatchling) {
                if (Dungeon.hero.invisible<=0 && Dungeon.level.adjacent(pos,Dungeon.hero.pos)) {
                    GLog.w(Messages.get(HatchlingMimic.class,"theft_attempt"));
                    // Thief accuracy is 12; reuse the actual hit/evasion contest and its modifiers.
                    if (Char.hit(this,Dungeon.hero,12f/Math.max(1,attackSkill(Dungeon.hero)),false)) takeHatchling();
                } else if (Dungeon.hero.invisible<=0) hatchlingStep(Dungeon.hero.pos,false);
                spend(TICK);
                return true;
            }
            return false;
        }
        if (alignment == Alignment.NEUTRAL && Dungeon.level.adjacent(pos,Dungeon.hero.pos)) {
            hatchlingStep(Dungeon.hero.pos,true); spend(TICK); return true;
        }
        return false;
    }

    public void takeHatchling() {
        HatchlingMimic hatchling=HatchlingMimic.carried();
        if (!(this instanceof CrystalMimic) || hatchling == null || stolenHatchling != null) return;
        stolenHatchling=(HatchlingMimic)hatchling.detachAll(Dungeon.hero.belongings.backpack);
        pursuingHatchling=false; alignment=Alignment.ENEMY; state=FLEEING;
        Dungeon.hero.interrupt();
        Item.updateQuickslot();
        GLog.w(Messages.get(HatchlingMimic.class,"stolen"));
    }

    public void escapeWithHatchling() {
        if (stolenHatchling == null) return;
        stolenHatchling=null;
        HatchlingMimic.scheduleEscape(this);
        destroy();
        if (sprite!=null) sprite.killAndErase();
    }

	@Override
	public boolean interact(Char c) {
        HatchlingMimic hatchling = HatchlingMimic.carried();
        if (c == Dungeon.hero && hatchling != null && !hatchlingBorn && alignment == Alignment.NEUTRAL
                && this instanceof CrystalMimic) {
            pursuingHatchling = true;
            stopHiding(); alignment = Alignment.ENEMY; state = HUNTING;
            GLog.w(Messages.get(HatchlingMimic.class, "theft_notice"));
            Dungeon.hero.spendAndNext(1f);
            return true;
        }
        if (c == Dungeon.hero && hatchling != null && !hatchlingBorn && alignment == Alignment.NEUTRAL
                && !(this instanceof CrystalMimic) && !(this instanceof EbonyMimic)) {
            if (!hatchling.charm(this)) hatchlingStep(Dungeon.hero.pos, true);
            Dungeon.hero.spendAndNext(1f);
            return true;
        }
		if (alignment != Alignment.NEUTRAL || c != Dungeon.hero){
			return super.interact(c);
		}
		stopHiding();

		Dungeon.hero.busy();
		Dungeon.hero.sprite.operate(pos);
		if (Dungeon.hero.invisible <= 0
				&& Dungeon.hero.buff(Swiftthistle.TimeBubble.class) == null
				&& Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) == null){
			return doAttack(Dungeon.hero);
		} else {
			sprite.idle();
			alignment = Alignment.ENEMY;
			Dungeon.hero.spendAndNext(1f);
			return true;
		}
	}

	@Override
	public void onAttackComplete() {
		super.onAttackComplete();
		if (alignment == Alignment.NEUTRAL){
			alignment = Alignment.ENEMY;
			Dungeon.hero.spendAndNext(1f);
			enemySeen = true;
		}
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (state == PASSIVE){
			alignment = Alignment.ENEMY;
			stopHiding();
		}
		return super.defenseProc(enemy, damage);
	}

	@Override
	public void damage(int dmg, Object src) {
        if (dmg > 0) Buff.detach(this, HatchlingMimic.Kinship.class);
		if (state == PASSIVE){
			alignment = Alignment.ENEMY;
			stopHiding();
		}
		super.damage(dmg, src);
	}

	@Override
	public void die(Object cause) {
		if (state == PASSIVE){
			alignment = Alignment.ENEMY;
			stopHiding();
		}
		super.die(cause);
	}

	public void stopHiding(){
		state = HUNTING;
		if (sprite != null) sprite.idle();
		if (Actor.chars().contains(this) && Dungeon.level.heroFOV[pos]) {
			enemy = Dungeon.hero;
			target = Dungeon.hero.pos;
			GLog.w(Messages.get(this, "reveal") );
			CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.Sounds.MIMIC);
		}
	}

	//stealthy mimics have changes to visual behaviour that make them much harder to detect
	public boolean stealthy(){
		return stealthy;
	}

	@Override
	public int damageRoll() {
		if (alignment == Alignment.NEUTRAL){
			return Random.NormalIntRange( 2 + 2*level, 2 + 2*level);
		} else {
			return Random.NormalIntRange( 1 + level, 2 + 2*level);
		}
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 1 + level/2);
	}

	@Override
	public void beckon( int cell ) {
		if (alignment != Alignment.NEUTRAL) {
			super.beckon(cell);
		}
	}

	@Override
	public int attackSkill( Char target ) {
		if (target != null && alignment == Alignment.NEUTRAL && target.invisible <= 0){
			return INFINITE_ACCURACY;
		} else {
			return 6 + level;
		}
	}

	public void setLevel( int level ){
		this.level = level;
		adjustStats(level);
	}
	
	public void adjustStats( int level ) {
		HP = HT = (1 + level) * 6;
		defenseSkill = 2 + level/2;
		
		enemySeen = true;
	}
	
	@Override
	public void rollToDropLoot(){
        if (stolenHatchling != null) {
            if (items == null) items = new ArrayList<>();
            items.add(stolenHatchling);
            GLog.w(Messages.get(HatchlingMimic.class,"recovered"));
            stolenHatchling=null;
        }
		
		if (items != null) {
			for (Item item : items) {
				Dungeon.level.drop( item, pos ).sprite.drop();
			}
			items = null;
		}
		super.rollToDropLoot();
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		if (state != PASSIVE) state = WANDERING;
		return true;
	}

	public static Mimic spawnAt( int pos, Item... items){
		return spawnAt(pos, Mimic.class, items);
	}

	public static Mimic spawnAt( int pos, Class mimicType, Item... items){
		return spawnAt(pos, mimicType, true, items);
	}

	public static Mimic spawnAt( int pos, boolean useDecks, Item... items){
		return spawnAt(pos, Mimic.class, useDecks, items);
	}

	public static Mimic spawnAt( int pos, Class mimicType, boolean useDecks, Item... items){
		Mimic m;
		if (mimicType == GoldenMimic.class){
			m = new GoldenMimic();
		} else if (mimicType == CrystalMimic.class) {
			m = new CrystalMimic();
		} else if (mimicType == EbonyMimic.class) {
			m = new EbonyMimic();
		} else {
			m = new Mimic();
		}

		m.items = new ArrayList<>( Arrays.asList(items) );
		m.setLevel( Dungeon.scalingDepth() );
		m.pos = pos;

		//generate an extra reward for killing the mimic
		m.generatePrize(useDecks);

		if (MimicTooth.stealthyMimics()){
			m.stealthy = true;
		}

		return m;
	}

	protected void generatePrize( boolean useDecks ){
		Item reward = null;
		do {
			switch (Random.Int(5)) {
				case 0:
					reward = new Gold().random();
					break;
				case 1:
					reward = Generator.randomMissile(!useDecks);
					break;
				case 2:
					reward = Generator.randomArmor();
					break;
				case 3:
					reward = Generator.randomWeapon(!useDecks);
					break;
				case 4:
					reward = useDecks ? Generator.random(Generator.Category.RING) : Generator.randomUsingDefaults(Generator.Category.RING);
					break;
			}
		} while (reward == null || Challenges.isItemBlocked(reward));
		items.add(reward);

		if (MimicTooth.stealthyMimics()){
			//add an extra random item if player has a mimic tooth
			items.add(Generator.randomUsingDefaults());
		}
	}

}
