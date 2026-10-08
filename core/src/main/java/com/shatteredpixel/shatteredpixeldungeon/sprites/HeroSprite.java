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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeroDisguise;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;

public class HeroSprite extends CharSprite {
	
	private static final int FRAME_WIDTH = GameGeometry.HERO_FRAME_W;
	private static final int FRAME_HEIGHT = GameGeometry.HERO_FRAME_H;
	
	private static final int RUN_FRAMERATE	= 20;
	
	private static TextureFilm tiers;
	
	private Animation fly;
	private Animation read;
    private volatile com.shatteredpixel.shatteredpixeldungeon.items.Item actionWeapon;
    private com.shatteredpixel.shatteredpixeldungeon.effects.HasteTrail hasteTrail;
    private volatile com.shatteredpixel.shatteredpixeldungeon.items.Item drinkingItem;
    private com.shatteredpixel.shatteredpixeldungeon.items.Item renderedDrink;
    private ItemSprite drinkBottle;
    private float drinkTime;

	public HeroSprite() {
		super();
		
		texture( Dungeon.hero.heroClass.spritesheet() );
		texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR, com.badlogic.gdx.graphics.GL20.GL_LINEAR);
		updateArmor();
		
		link( Dungeon.hero );

		if (ch.isAlive())
			idle();
		else
			die();
        // Cache draw resources on the scene/render thread, never in actor callbacks.
        hasteTrail=new com.shatteredpixel.shatteredpixeldungeon.effects.HasteTrail(this);
        drinkBottle=new ItemSprite();
	}

	public void disguise(HeroClass cls){
		texture( cls.spritesheet() );
		texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR, com.badlogic.gdx.graphics.GL20.GL_LINEAR);
		updateArmor();
	}
	
	public void updateArmor() {

		TextureFilm film = new TextureFilm( tiers(), Dungeon.hero.tier(), FRAME_WIDTH, FRAME_HEIGHT );
		
		idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( com.watabou.utils.DeviceCompat.isDesktop()?RUN_FRAMERATE:12, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );
		
		die = new Animation( 20, false );
		die.frames( film, 8, 9, 10, 11, 12, 11 );
		
		attack = new Animation( 15, false );
		attack.frames( film, 13, 14, 15, 0 );
		
		zap = attack.clone();
		
		operate = new Animation( 8, false );
		operate.frames( film, 16, 17, 16, 17 );
		
		fly = new Animation( 1, true );
		fly.frames( film, 18 );

		read = new Animation( 20, false );
		read.frames( film, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19 );
		
		if (Dungeon.hero.isAlive())
			idle();
		else
			die();
	}
	
	@Override
	public void place( int p ) {
		super.place( p );
		if (Game.scene() instanceof GameScene) Camera.main.panFollow(this, 5f);
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		if (ch != null && ch.flying) {
			play( fly );
		}
		Camera.main.panFollow(this, com.watabou.utils.DeviceCompat.isDesktop()?20f:8f);
	}

	@Override
	public void idle() {
        actionWeapon=null;
        drinkingItem=null;
		super.idle();
		if (ch != null && ch.flying) {
			play( fly );
		}
	}

	@Override
	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		super.jump( from, to, height, duration, callback );
		play( fly );
		Camera.main.panFollow(this, 20f);
	}

	public synchronized void read() {
		animCallback = new Callback() {
			@Override
			public void call() {
				idle();
				ch.onOperateComplete();
			}
		};
		play( read );
	}

    public void presentProjectile(com.shatteredpixel.shatteredpixeldungeon.items.Item item){
        actionWeapon=item instanceof com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon?item:null;
    }
    // Generic painted poses replace inventory icons used as anatomical overlays.
    public Image displayedEquipment(){return null;}
    public com.shatteredpixel.shatteredpixeldungeon.items.Item displayedWeapon(){return actionWeapon!=null?actionWeapon:((Hero)ch).belongings.weapon();}
    /** Keep the normal operation callback and one-turn potion timing. */
    public synchronized void drink(com.shatteredpixel.shatteredpixeldungeon.items.Item potion){
        operate(ch.pos);
        drinkingItem=potion;
    }
    @Override public synchronized void attack(int cell,Callback callback){
        actionWeapon=((Hero)ch).belongings.attackingWeapon();
        super.attack(cell,callback);
    }
    @Override public void draw(){
        super.draw();
        if(hasteTrail!=null)hasteTrail.draw();
        if(drinkingItem!=null && drinkingItem==renderedDrink && visible){
            RectF body=visibleBounds();
            float lift=Math.min(1,drinkTime/.18f);
            if(drinkTime>.36f)lift=Math.max(0,(.5f-drinkTime)/.14f);
            float side=flipHorizontal?1:-1;
            drinkBottle.camera=camera();
            drinkBottle.x=(body.left+body.right)/2+side*(1.2f+2*(1-lift))-drinkBottle.origin.x;
            drinkBottle.y=body.top+body.height()*.13f+6*(1-lift)-drinkBottle.origin.y;
            drinkBottle.angle=side*55*lift;
            drinkBottle.alpha(am);
            drinkBottle.draw();
        }
    }
    @Override public void destroy(){if(hasteTrail!=null)hasteTrail.destroy();if(drinkBottle!=null)drinkBottle.destroy();super.destroy();}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		//Does nothing.

		/*
		 * This is both for visual clarity, and also for content ratings regarding violence
		 * towards human characters. The heroes are the only human or human-like characters which
		 * participate in combat, so removing all blood associated with them is a simple way to
		 * reduce the violence rating of the game.
		 */
	}

	@Override
	public void update() {
		sleeping = ch.isAlive() && ((Hero)ch).resting;
		
		super.update();
        if(hasteTrail!=null)hasteTrail.update();
        com.shatteredpixel.shatteredpixeldungeon.items.Item potion=drinkingItem;
        if(potion!=renderedDrink){
            renderedDrink=potion;drinkTime=0;
            if(potion!=null){
                drinkBottle.view(potion.image,null);
                GameGeometry.fitBox(drinkBottle,3.8f,5.5f);
                drinkBottle.origin.set(drinkBottle.width()/2,drinkBottle.height()*.2f);
            }
        }
        if(potion!=null)drinkTime+=Game.elapsed;
	}
	
	public void sprint( float speed ) {
		run.delay = 1f / speed / (com.watabou.utils.DeviceCompat.isDesktop()?RUN_FRAMERATE:12);
	}
	
	public static TextureFilm tiers() {
		if (tiers == null) {
			SmartTexture texture = TextureCache.get( Assets.Sprites.ROGUE );
			tiers = new TextureFilm( texture, texture.width, FRAME_HEIGHT );
		}
		
		return tiers;
	}

	public static Image avatar( Hero hero ){
		if (hero.buff(HeroDisguise.class) != null){
			return avatar(hero.buff(HeroDisguise.class).getDisguise(), hero.tier());
		} else {
			return avatar(hero.heroClass, hero.tier());
		}
	}
	
	public static Image avatar( HeroClass cl, int armorTier ) {
		
        return new com.shatteredpixel.shatteredpixeldungeon.ui.HeroPortrait(cl, 26);
	}
}
