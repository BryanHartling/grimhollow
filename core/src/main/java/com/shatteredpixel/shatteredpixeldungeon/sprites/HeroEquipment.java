// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;
import java.util.Locale;

/** Presentation only. Two reused atlas quads; no actors, timers or extra textures. */
final class HeroEquipment {
    private static JsonValue grips,itemGrips;
    private final Image weapon=new Image(Assets.Sprites.ITEMS),hand=new Image();
    private final HeroSprite sprite;
    private volatile Item actionWeapon;
    private int image=-1;
    private RectF weaponFrame;
    private boolean ready;

    HeroEquipment(HeroSprite sprite){this.sprite=sprite;weapon.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);}
    void capture(Item item){actionWeapon=item;}
    void rest(){actionWeapon=null;}
    Item displayedWeapon(Hero hero){return actionWeapon!=null?actionWeapon:hero.belongings.weapon();}

    boolean prepare(int pose,HeroClass appearance){
        ready=false;
        if(!(sprite.ch instanceof Hero)||!sprite.ch.isAlive()||!sprite.visible||pose>=8&&pose<=12||pose>=16&&pose<=17||pose>=19)return false;
        if(grips==null)grips=new JsonReader().parse(Gdx.files.internal("sprites/hero-grips.json"));
        if(itemGrips==null)itemGrips=new JsonReader().parse(Gdx.files.internal("sprites/equipment-grips.json"));
        JsonValue points=grips.get(appearance.name().toLowerCase(Locale.ROOT));
        if(points==null)return false;
        Item item=displayedWeapon((Hero)sprite.ch);
        if(!(item instanceof KindOfWeapon))return false;
        if(item.image()==ItemSpriteSheet.GLOVES||item.image()==ItemSpriteSheet.GAUNTLETS)return false;
        if(image!=item.image()){
            image=item.image();weaponFrame=ItemSpriteSheet.film.get(image);
            weapon.frame(weaponFrame);
        }
        String name=item.getClass().getSimpleName();
        boolean staff=item instanceof MagesStaff||name.contains("Spear")||name.contains("Glaive")||name.contains("Scythe");
        boolean bow=item instanceof SpiritBow||name.contains("Crossbow");
        boolean heavy=staff||name.contains("Great")||name.contains("Greataxe")||name.contains("BattleAxe")||name.contains("WarHammer")||name.contains("Flail");
        boolean crystal=item instanceof FocusCrystal;
        float length=crystal?4:item instanceof MissileWeapon&&!bow?6:heavy?13:bow?12:8;
        // Inventory silhouettes retain their individual art, fitted once on gear changes.
        float gx=points.get(pose).getFloat(0)/48f,gy=points.get(pose).getFloat(1)/60f;
        // An arm hanging at the hip carries a short blade downward. Polearms,
        // bows and hooked blades stay upright beside the body, not across it.
        boolean artFlip=sprite.flipHorizontal;
        weapon.flipHorizontal=artFlip;weapon.frame(weaponFrame);
        weapon.logicalSize(length,length);
        if(sprite.flipHorizontal)gx=1-gx;
        float hx=sprite.x+gx*sprite.width,hy=sprite.y+gy*sprite.height;
        JsonValue grip=itemGrips.get(Integer.toString(item.image()));
        float ox=grip==null?.28f:grip.getFloat(0),oy=grip==null?.76f:grip.getFloat(1);
        if(artFlip)ox=1-ox;
        weapon.origin.set(ox*weapon.width,oy*weapon.height);
        weapon.x=hx-weapon.origin.x;weapon.y=hy-weapon.origin.y;
        float tipX=grip==null?.78f:grip.getFloat(2),tipY=grip==null?.15f:grip.getFloat(3);
        if(artFlip)tipX=1-tipX;
        // Rotate the actual handle-to-tip axis, never a guessed inventory diagonal.
        float axis=(float)Math.toDegrees(Math.atan2(tipY-oy,tipX-ox));
        boolean leftHand=points.get(pose).getFloat(0)<24;
        boolean upright=heavy||bow||name.contains("Sickle")||name.contains("Whip")||name.contains("Rod")||name.contains("Baton");
        float desired=upright?(leftHand?-94:-86):(leftHand?115:65);
        if(pose>=13&&pose<=15)desired=pose==13?-105:pose==14?-5:-45;
        if(artFlip)desired=180-desired;
        weapon.angle=crystal?0:desired-axis;
        weapon.camera=sprite.camera();copyLight(weapon);
        // Repaint the fingers over the grip; use the current armor/action frame,
        // not a separate generic hand that would change anatomy or complexion.
        float px=points.get(pose).getFloat(0)*2,py=points.get(pose).getFloat(1)*2;
        RectF frame=sprite.frame();
        hand.texture(sprite.texture);
        hand.flipHorizontal=sprite.flipHorizontal;
        hand.frame(sprite.texture.uvRect(frame.left*sprite.texture.width+px-3,frame.top*sprite.texture.height+py-4,
                frame.left*sprite.texture.width+px+3,frame.top*sprite.texture.height+py+4));
        hand.logicalSize(sprite.width*6/96f,sprite.height*8/120f);
        hand.x=hx-hand.width/2;hand.y=hy-hand.height/2;hand.camera=sprite.camera();copyLight(hand);
        ready=true;return true;
    }
    private void copyLight(Image image){
        image.rm=sprite.rm;image.gm=sprite.gm;image.bm=sprite.bm;image.am=sprite.am;
        image.ra=sprite.ra;image.ga=sprite.ga;image.ba=sprite.ba;image.aa=sprite.aa;
    }
    void draw(){if(ready){weapon.draw();hand.draw();}}
    Image image(){return ready?weapon:null;}
    void destroy(){weapon.destroy();hand.destroy();}
}
