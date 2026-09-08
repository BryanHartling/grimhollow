// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.*;
import java.util.*;
import com.watabou.utils.Random;
public class SigilBrush extends ClassSpellItem {
    {image=ItemSpriteSheet.SIGIL_BRUSH;}
    @Override public String[] spells(Hero h){return h.subClass==HeroSubClass.ARTIFICER?new String[]{"inscribe","hex","transmute","reinforce"}:h.subClass==HeroSubClass.SCRIVENER?new String[]{"inscribe","hex","sanctify","nullify","fracture"}:new String[]{"inscribe","hex"};}
    @Override protected void select(Hero h,String spell){
        if(spell.equals("inscribe")||spell.equals("transmute")||spell.equals("reinforce")){
            GameScene.show(new WndOptions(Messages.get(this,spell),Messages.get(this,"equipment"),Messages.get(this,"weapon"),Messages.get(this,"armor")){
                @Override protected void onSelect(int index){Item item=index==0?h.belongings.weapon:h.belongings.armor;if(item==null)return;
                    if(!spell.equals("inscribe")){cast(h,spell,h.pos,item,null);return;}
                    java.util.List<Class<?>> options=EnchanterMagic.state().choices(index==1);String[] labels=new String[options.size()];for(int i=0;i<labels.length;i++){Object o=Reflection.newInstance(options.get(i));labels[i]=o instanceof Weapon.Enchantment?((Weapon.Enchantment)o).name():((Armor.Glyph)o).name();}
                    GameScene.show(new WndOptions(Messages.get(SigilBrush.class,"inscribe"),Messages.get(SigilBrush.class,"known"),labels){@Override protected void onSelect(int i){cast(h,spell,h.pos,item,options.get(i));}});
                }});
        }else if(spell.equals("sanctify"))cast(h,spell,h.pos,null,null);
        else GameScene.selectCell(new CellSelector.Listener(){public void onSelect(Integer cell){cast(h,spell,cell,null,null);}public String prompt(){return Messages.get(SigilBrush.class,"target");}});
    }
    public boolean cast(Hero h,String spell,Integer cell,Item item,Class<?> choice){
        int cost=spell.equals("nullify")||spell.equals("transmute")?2:1;
        if(!Arrays.asList(spells(h)).contains(spell)||!ready(h,cost))return false;
        EnchanterMagic magic=EnchanterMagic.state();
        switch(spell){
            case "inscribe":
                if(item==null||!item.isEquipped(h)||!(item instanceof Weapon||item instanceof Armor)||!magic.choices(item instanceof Armor).contains(choice))return false;
                inscribe(item,choice,30+10*h.pointsInTalent(Talent.STEADY_HAND));
                if(Random.Float()<.25f*h.pointsInTalent(Talent.DUAL_INSCRIPTION)){
                    Item other=item instanceof Weapon?h.belongings.armor:h.belongings.weapon;
                    if(other!=null){java.util.List<Class<?>> choices=magic.choices(other instanceof Armor);Class<?> second=choices.isEmpty()?(other instanceof Armor?Armor.Glyph.common[0]:Weapon.Enchantment.common[0]):Random.element(choices);inscribe(other,second,30+10*h.pointsInTalent(Talent.STEADY_HAND));}
                }
                if(Random.Float()<.1f*h.pointsInTalent(Talent.EFFICIENT_SIGILS))cost=0;
                break;
            case "transmute":
                if(item==null||!item.isEquipped(h))return false;
                if(item instanceof Weapon){Weapon w=(Weapon)item;w.enchant((Weapon.Enchantment)reroll(w.enchantment,Weapon.Enchantment.common,Weapon.Enchantment.uncommon,Weapon.Enchantment.rare,Weapon.Enchantment.curses));}
                else if(item instanceof Armor){Armor a=(Armor)item;a.inscribe((Armor.Glyph)reroll(a.glyph,Armor.Glyph.common,Armor.Glyph.uncommon,Armor.Glyph.rare,Armor.Glyph.curses));}else return false;
                EnchanterMagic.learn(item);break;
            case "reinforce":
                if(item==null||!item.isEquipped(h)||!(item instanceof Weapon||item instanceof Armor))return false;
                item.reinforceTurns=50;item.reinforceFlat=h.pointsInTalent(Talent.MASTER_CRAFT);break;
            case "sanctify":
                int radius=1+Math.max(0,h.pointsInTalent(Talent.WIDE_FIELD)-1);
                for(Char ch:Actor.chars())if(ch.alignment==Char.Alignment.ALLY&&Dungeon.level.distance(h.pos,ch.pos)<=radius){Buff.prolong(ch,Bless.class,8);Buff.prolong(ch,Haste.class,8);}break;
            default:
                if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell])return false;
                Char target=Actor.findChar(cell);
                if(spell.equals("nullify")){
                    for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0]))if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.distance(cell,mob.pos)<=2+Math.max(0,h.pointsInTalent(Talent.WIDE_FIELD)-1)){EnchanterMagic.strip(mob);Buff.prolong(mob,Silenced.class,5);EnchanterMagic.counterweight();}
                }else{
                    if(target==null||target.alignment!=Char.Alignment.ENEMY)return false;
                    if(spell.equals("hex")){Buff.prolong(target,Hex.class,10);Buff.prolong(target,DegradedGear.class,10);int p=h.pointsInTalent(Talent.SHARPENED_SIGILS);if(p>0)Buff.prolong(target,Vulnerable.class,1+2*p);}
                    else if(spell.equals("fracture"))Buff.prolong(target,FracturedArmor.class,6);else return false;
                    Buff.prolong(target,EnchanterMagic.EnchanterDamage.class,20);EnchanterMagic.counterweight();
                }
        }
        finish(h,cost);return true;
    }
    private void inscribe(Item item,Class<?> type,int turns){if(item instanceof Weapon)((Weapon)item).inscribed=(Weapon.Enchantment)Reflection.newInstance(type);else ((Armor)item).inscribed=(Armor.Glyph)Reflection.newInstance(type);item.inscriptionTurns=turns;}
    private Object reroll(Object old,Class<?>[]...tiers){Class<?>[] pool=tiers[0];if(old!=null)for(Class<?>[] tier:tiers)if(Arrays.asList(tier).contains(old.getClass()))pool=tier;ArrayList<Class<?>> options=new ArrayList<>(Arrays.asList(pool));if(old!=null)options.remove(old.getClass());return Reflection.newInstance(Random.element(options));}
}
