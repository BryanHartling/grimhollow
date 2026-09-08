// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.watabou.noosa.Game;

/** Opt-in launch diagnostic: renders real OpenGL frames, writes evidence, exits. */
final class DesktopSmokeProbe extends ShatteredPixelDungeon {
    private final boolean sewers;
    private int frames;
    private boolean originalLighting;
    DesktopSmokeProbe(boolean sewers) {
        super(new DesktopPlatformSupport());
        this.sewers=sewers;
        sceneClass=TitleScene.class;
    }
    @Override public void create() {
        super.create();
        originalLighting=SPDSettings.dynamicLighting();
    }
    private void capture(String name) {
        Pixmap screenshot=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        String output=System.getProperty("grimhollow.smokeDir", ".local/acceptance")+"/"+name+".png";
        PixmapIO.writePNG(Gdx.files.absolute(output),screenshot,-1,true);
        screenshot.dispose();
        System.out.println("RENDERED="+Game.scene().getClass().getSimpleName()+" SCREENSHOT="+output);
    }
    @Override public void render() {
        super.render();
        frames++;
        if (frames==180) {
            if (!(Game.scene() instanceof TitleScene)) throw new AssertionError("Title scene did not launch");
            capture("title");
            if (!sewers) { Gdx.app.exit(); return; }
            GamesInProgress.selectedClass=HeroClass.WARRIOR;
            GamesInProgress.curSlot=99;
            Dungeon.seed=417;
            Dungeon.init();
            Dungeon.switchLevel(Dungeon.newLevel(),-1);
            InterlevelScene.mode=InterlevelScene.Mode.DESCEND;
            SPDSettings.dynamicLighting(true);
            switchNoFade(GameScene.class);
        } else if (sewers && frames==360) {
            if (!(Game.scene() instanceof GameScene)) throw new AssertionError("Sewer scene did not launch");
            capture("sewers-lighting-on");
            SPDSettings.dynamicLighting(false);
        } else if (sewers && frames==420) {
            capture("sewers-lighting-off");
            SPDSettings.dynamicLighting(originalLighting);
            if (Boolean.getBoolean("grimhollow.geometryTests")) geometryTests();
            System.out.println("PASS: Sewer scene renders with dynamic lighting on and off.");
            Gdx.app.exit();
        }
    }

    /** Acceptance 24-26 use actual sprite draws into an RGBA framebuffer. No scene screenshots are measured. */
    private void geometryTests() {
        try {
            float zoom=com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.defaultZoom;
            com.watabou.noosa.Camera camera=new com.watabou.noosa.Camera(0,0,256,256,zoom);
            camera.fullScreen=true;
            camera.matrix[0]=2*zoom/256;camera.matrix[5]=-2*zoom/256;
            camera.matrix[12]=-1;camera.matrix[13]=1;
            com.badlogic.gdx.graphics.glutils.FrameBuffer buffer=new com.badlogic.gdx.graphics.glutils.FrameBuffer(Pixmap.Format.RGBA8888,256,256,false);
            java.util.List<String> failures=new java.util.ArrayList<>();
            int heroes=0,mobs=0,items=0,icons=0;
            HeroClass original=Dungeon.hero.heroClass;
            com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite originalSprite=Dungeon.hero.sprite;
            for(HeroClass hero:HeroClass.values()) {
                Dungeon.hero.heroClass=hero;
                com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
                band(sprite,16,.85f,.95f,buffer,camera,zoom,failures,"24 "+hero);heroes++;sprite.destroy();
            }
            Dungeon.hero.heroClass=original;Dungeon.hero.sprite=originalSprite;
            java.io.File jar=new java.io.File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            try(java.util.jar.JarFile classes=new java.util.jar.JarFile(jar)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries=classes.entries();
                while(entries.hasMoreElements()) {
                    String name=entries.nextElement().getName();
                    if(!name.startsWith("com/shatteredpixel/shatteredpixeldungeon/sprites/")||!name.endsWith(".class"))continue;
                    Class<?> type=Class.forName(name.substring(0,name.length()-6).replace('/','.'));
                    if(!com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class.isAssignableFrom(type)
                            || type==com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.class
                            || java.lang.reflect.Modifier.isAbstract(type.getModifiers()))continue;
                    com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite=(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite)type.getDeclaredConstructor().newInstance();
                    if(sprite.texture==null)continue; // Base sprite classes have no art or frame.
                    float footprint=sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.DM300Sprite||sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.YogSprite?32:16;
                    band(sprite,footprint,.85f,.95f,buffer,camera,zoom,failures,"24 "+type.getSimpleName());mobs++;sprite.destroy();
                }
            }
            System.out.println("TEST 24: heroes="+heroes+" mob sprites="+mobs+" failures="+failures.size());
            int before=failures.size();
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(index);
                if(GameGeometry.opaqueHeight(sprite.texture,sprite.frame())==0){failures.add("25 empty "+field.getName());continue;}
                band(sprite,16,.45f,.55f,buffer,camera,zoom,failures,"25 "+field.getName());
                exactRectangle(sprite,index,32,buffer,camera,zoom,failures,field.getName());items++;sprite.destroy();
            }
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.watabou.noosa.Image icon=new com.watabou.noosa.Image(Assets.Sprites.ITEM_ICONS);
                icon.frame(com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.film.get(index));
                exactRectangle(icon,index,8,buffer,camera,zoom,failures,"icon "+field.getName());icons++;icon.destroy();
            }
            System.out.println("TEST 25: items="+items+" identification icons="+icons+" failures="+(failures.size()-before));before=failures.size();
            int cell=Dungeon.hero.pos,terrain=Dungeon.level.map[cell];
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
            for(int i=0;i<3;i++) {
                com.watabou.noosa.Image decal=GameScene.createBloodDecal(cell,i);
                if(decal==null)failures.add("26 no floor decal");
                else {band(decal,16,.30f,.60f,buffer,camera,zoom,failures,"26 blood "+i);decal.destroy();}
            }
            for(int invalid:new int[]{com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WATER,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.TRAP}){
                com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,invalid);
                if(GameScene.createBloodDecal(cell,0)!=null)failures.add("26 decal on invalid terrain "+invalid);
            }
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,terrain);
            System.out.println("TEST 26: three stains and floor/chasm/water/trap placement failures="+(failures.size()-before));
            buffer.dispose();for(String failure:failures)System.out.println("FAIL "+failure);
            if(!failures.isEmpty())throw new AssertionError("Rendering acceptance failures="+failures.size());
            System.out.println("TESTS 24-26 PASS");
        }catch(Exception e){throw new RuntimeException(e);}
    }
    private Pixmap renderSprite(com.watabou.noosa.Image image,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,com.watabou.noosa.Camera camera)throws Exception {
        // Generated ground shadows are separate from the sprite's alpha silhouette.
        if(image instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite){java.lang.reflect.Field shadow=com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class.getDeclaredField("renderShadow");shadow.setAccessible(true);shadow.setBoolean(image,false);}
        image.x=16;image.y=8;image.camera=camera;
        buffer.begin();Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_SCISSOR_TEST);Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);Gdx.gl.glClearColor(0,0,0,0);Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
        com.watabou.noosa.NoosaScript.get().resetCamera();image.draw();
        Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,256,256);buffer.end();com.watabou.glwrap.Blending.useDefault();return pixels;
    }
    private void band(com.watabou.noosa.Image sprite,float footprint,float low,float high,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,com.watabou.noosa.Camera camera,float zoom,java.util.List<String> failures,String name)throws Exception {
        Pixmap p=renderSprite(sprite,buffer,camera);int top=256,bottom=-1;
        for(int y=0;y<256;y++)for(int x=0;x<256;x++)if((p.getPixel(x,y)&255)!=0){top=Math.min(top,y);bottom=Math.max(bottom,y);}
        float ratio=(bottom-top+1)/(footprint*zoom);p.dispose();
        if(ratio<low||ratio>high)failures.add(name+" height/tile="+ratio);
    }
    private void exactRectangle(com.watabou.noosa.Image sprite,int index,int cellSize,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,com.watabou.noosa.Camera camera,float zoom,java.util.List<String> failures,String name)throws Exception {
        com.watabou.utils.RectF uv=sprite.frame();int w=Math.round(uv.width()*sprite.texture.width),h=Math.round(uv.height()*sprite.texture.height);
        int x=index%16*cellSize,y=index/16*cellSize;
        if(Math.round(uv.left*sprite.texture.width)!=x||Math.round(uv.top*sprite.texture.height)!=y||w>cellSize||h>cellSize){failures.add("25 atlas boundary "+name);return;}
        sprite.logicalSize(w/zoom,h/zoom);Pixmap p=renderSprite(sprite,buffer,camera);
        for(int j=0;j<h;j++)for(int i=0;i<w;i++) {
            int expected=sprite.texture.bitmap.getPixel(x+i,y+j),actual=p.getPixel(Math.round(16*zoom)+i,255-Math.round(8*zoom)-j);
            if(actual!=expected){failures.add("25 pixel mismatch "+name+" at "+i+","+j);p.dispose();return;}
        }
        p.dispose();
    }
}
