// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;
import com.badlogic.gdx.graphics.GL20;
import com.watabou.noosa.Image;
/** Distinct authored silhouettes, packed from botany-skills.json. IDs never alias upstream icons. */
public class SkillIcon extends Image {
    public static final String TEXTURE="interfaces/painted_skills.png";
    public static final int DEATHSPEAKER=1000+25;
    public static final int HEXWEAVER=1000+26;
    public static final int CORPSE_EXPLOSION=1000+27;
    public static final int DEATH_PACT=1000+28;
    public static final int BONE_PRISON=1000+29;
    public static final int RAISE_SKELETON=1000+30;
    public static final int WITHER=1000+31;
    public static final int RAISE_WRAITH=1000+32;
    public static final int RAISE_GHOUL=1000+33;
    public static final int RAISE_REVENANT=1000+34;
    public static final int AMPLIFY=1000+35;
    public static final int DECREPIFY=1000+36;
    public static final int IRON_MAIDEN=1000+37;
    public static final int LOWER_RESISTANCE=1000+38;
    public static final int ARTIFICER=1000+67;
    public static final int SCRIVENER=1000+68;
    public static final int OVERCHARGE=1000+69;
    public static final int SANCTUARY=1000+70;
    public static final int UNMAKING=1000+71;
    public static final int INSCRIBE=1000+72;
    public static final int HEX=1000+73;
    public static final int TRANSMUTE=1000+74;
    public static final int REINFORCE=1000+75;
    public static final int SANCTIFY=1000+76;
    public static final int NULLIFY=1000+77;
    public static final int FRACTURE=1000+78;
    public static final int ETCH=1000+79;
    public static final int PUPPETEER=1000+109;
    public static final int SEER=1000+110;
    public static final int PSYCHIC_STORM=1000+111;
    public static final int MIND_MELD=1000+112;
    public static final int FORCE_WALL=1000+113;
    public static final int GRASP=1000+114;
    public static final int GLIMPSE=1000+115;
    public static final int PUSH=1000+116;
    public static final int DOMINATE=1000+117;
    public static final int SUGGESTION=1000+118;
    public static final int HURL=1000+119;
    public SkillIcon(int index){super(TEXTURE);apply(this,index);}
    public static void apply(Image image,int index){
        if(index<0||index>=126)throw new IllegalArgumentException("Unknown painted skill "+index);
        image.texture(TEXTURE);
        image.frame(index%16*64,index/16*64,64,64);
        image.logicalSize(16,16);
        image.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
    }
    public static int talentIndex(int icon){
        if(icon>=224&&icon<296)return (icon-224)/24*42+(icon-224)%24;
        if(icon>=296&&icon<=298)return (icon-296)*42+24;
        return -1;
    }
    public static SkillIcon spell(String spell){
        switch(spell.toLowerCase(java.util.Locale.ROOT)){
            case "raise_skeleton":return new SkillIcon(30);
            case "wither":return new SkillIcon(31);
            case "raise_wraith":return new SkillIcon(32);
            case "raise_ghoul":return new SkillIcon(33);
            case "raise_revenant":return new SkillIcon(34);
            case "amplify":return new SkillIcon(35);
            case "decrepify":return new SkillIcon(36);
            case "iron_maiden":return new SkillIcon(37);
            case "lower_resistance":return new SkillIcon(38);
            case "inscribe":return new SkillIcon(72);
            case "hex":return new SkillIcon(73);
            case "transmute":return new SkillIcon(74);
            case "reinforce":return new SkillIcon(75);
            case "sanctify":return new SkillIcon(76);
            case "nullify":return new SkillIcon(77);
            case "fracture":return new SkillIcon(78);
            case "etch":return new SkillIcon(79);
            case "grasp":return new SkillIcon(114);
            case "glimpse":return new SkillIcon(115);
            case "push":return new SkillIcon(116);
            case "dominate":return new SkillIcon(117);
            case "suggestion":return new SkillIcon(118);
            case "hurl":return new SkillIcon(119);
            default:throw new IllegalArgumentException("Missing spell icon "+spell);
        }
    }
}
