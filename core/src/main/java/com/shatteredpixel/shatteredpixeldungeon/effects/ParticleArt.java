// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

/** Shared authored particle motifs, independent of emitter timing and gameplay RNG. */
public final class ParticleArt {
    public static final String ATLAS="effects/painted_particles.png";
    public static final int GRASS=0, LEAF=1, STONE=2, SPLINTER=3, CURSE=4, MOTE=5,
            SPARK=6, EMBER=7, MIST=8, DROP=9, GLINT=10, FLAME=11, BONE=12, FROST=13, RIPPLE=14, WEB=15;
    public static final String SPECKS="effects/painted_specks.png";
    public static final String RAYS="effects/painted_rays.png";
    private static final java.util.WeakHashMap<SmartTexture,Boolean> filtered=new java.util.WeakHashMap<>();
    private static final RectF[] FRAMES=new RectF[16];
    static { for(int i=0;i<16;i++) FRAMES[i]=new RectF((i%4*64+.5f)/256f,(i/4*64+.5f)/256f,
            (i%4*64+63.5f)/256f,(i/4*64+63.5f)/256f); }
    public static void frame(Image image,int motif,float width,float height){
        frame(image,ATLAS,motif,width,height);
    }
    public static void speck(Image image,int motif,float width,float height){
        frame(image,SPECKS,motif,width,height);
    }
    public static void ray(Image image,int kind){
        image.texture(RAYS);image.texture.filter(Texture.LINEAR,Texture.LINEAR);
        image.frame(image.texture.uvRect(.5f,kind*64+.5f,255.5f,kind*64+63.5f));
        image.logicalSize(16,8);
    }
    private static void frame(Image image,String atlas,int motif,float width,float height){
        SmartTexture texture=TextureCache.get(atlas);
        if(!filtered.containsKey(texture)){texture.filter(Texture.LINEAR,Texture.LINEAR);filtered.put(texture,true);}
        if(image.texture!=texture)image.texture(texture);
        image.frame(FRAMES[motif]);image.logicalSize(width,height);
    }
    static void appearance(Image from,Image to){
        to.camera=from.camera();to.x=from.x;to.y=from.y;
        to.origin.set(from.origin);to.scale.set(from.scale);to.angle=from.angle;
        to.rm=from.rm;to.gm=from.gm;to.bm=from.bm;to.am=from.am;
        to.ra=from.ra;to.ga=from.ga;to.ba=from.ba;to.aa=from.aa;
    }
    private ParticleArt(){}
}
