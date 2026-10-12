// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.utils.Reflection;
import com.watabou.utils.Random;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Shared selection for deliberate enchantment sources, with explicit payment timing. */
public final class Runecraft {
    private Runecraft(){}
    public static boolean enabled(Hero hero){return hero.heroClass==HeroClass.ENCHANTER;}
    public static Object current(Item item){return item instanceof Weapon?((Weapon)item).enchantment:item instanceof Armor?((Armor)item).glyph:null;}
    public static boolean eligible(Item item){
        Object old=current(item);
        return (item instanceof Weapon||item instanceof Armor)&&!item.cursed
            &&!(old instanceof Weapon.Enchantment&&((Weapon.Enchantment)old).curse())
            &&!(old instanceof Armor.Glyph&&((Armor.Glyph)old).curse());
    }
    public static Offer offer(Hero hero,Item item,boolean sameRarity,BooleanSupplier payment){
        if(!enabled(hero)||!eligible(item))return null;
        return new Offer(hero,item,sameRarity,payment,false);
    }
    public static Offer committedOffer(Hero hero,Item item,boolean sameRarity,BooleanSupplier payment){
        // Validate before consuming the source or revealing randomized choices.
        if(!enabled(hero)||!eligible(item)||!payment.getAsBoolean())return null;
        return new Offer(hero,item,sameRarity,()->true,true);
    }
    public static final class Offer {
        private final Item item;
        private final Object previous;
        private final BooleanSupplier payment;
        private final boolean committed;
        private final ArrayList<Object> options=new ArrayList<>();
        private boolean open=true;
        @SuppressWarnings("unchecked")
        private Offer(Hero hero,Item item,boolean sameRarity,BooleanSupplier payment,boolean committed){
            this.item=item;previous=current(item);this.payment=payment;this.committed=committed;
            boolean armor=item instanceof Armor;
            ArrayList<Class<?>> excluded=new ArrayList<>();if(previous!=null)excluded.add(previous.getClass());
            ArrayList<Class<?>> pool=new ArrayList<>();
            if(sameRarity){
                Class<?>[][] tiers=armor?new Class<?>[][]{Armor.Glyph.common,Armor.Glyph.uncommon,Armor.Glyph.rare}:new Class<?>[][]{Weapon.Enchantment.common,Weapon.Enchantment.uncommon,Weapon.Enchantment.rare};
                Class<?>[] tier=tiers[0];if(previous!=null)for(Class<?>[] candidate:tiers)if(Arrays.asList(candidate).contains(previous.getClass()))tier=candidate;
                pool.addAll(Arrays.asList(tier));pool.removeAll(excluded);
            }
            int count=hero.subClass==HeroSubClass.ARTIFICER?3:2;
            if(sameRarity)count=Math.min(count,pool.size());
            if(hero.subClass==HeroSubClass.SCRIVENER&&hero.buff(EnchanterMagic.class)!=null){
                Class<?> favorite=hero.buff(EnchanterMagic.class).favorite(armor,previous==null?null:previous.getClass());
                if(favorite!=null&&(!sameRarity||pool.contains(favorite))){options.add(Reflection.newInstance(favorite));excluded.add(favorite);pool.remove(favorite);}
            }
            while(options.size()<count){
                Object next=sameRarity?Reflection.newInstance(Random.element(pool)):armor?Armor.Glyph.random(excluded.toArray(new Class[0])):Weapon.Enchantment.random(excluded.toArray(new Class[0]));
                if(excluded.contains(next.getClass()))continue;
                options.add(next);excluded.add(next.getClass());pool.remove(next.getClass());
            }
        }
        public List<Object> options(){return Collections.unmodifiableList(options);}
        public boolean apply(int index){
            if(!open||index<0||index>=options.size()||!eligible(item)||current(item)!=previous||!payment.getAsBoolean())return false;
            open=false;
            if(item instanceof Weapon)((Weapon)item).enchant((Weapon.Enchantment)options.get(index));
            else ((Armor)item).inscribe((Armor.Glyph)options.get(index));
            EnchanterMagic.learn(item);return true;
        }
        public boolean cancel(){if(!open)return false;open=false;return true;}
        public String name(int i){Object e=options.get(i);return e instanceof Weapon.Enchantment?((Weapon.Enchantment)e).name():((Armor.Glyph)e).name();}
        public String description(int i){Object e=options.get(i);return e instanceof Weapon.Enchantment?((Weapon.Enchantment)e).desc():((Armor.Glyph)e).desc();}
    }
    public static void show(Offer offer,Runnable after){
        show(offer,after,()->{});
    }
    public static void show(Offer offer,Runnable after,Runnable abandoned){
        if(offer==null){com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(Messages.get(Runecraft.class,"cursed"));return;}
        if(offer.committed){GameScene.show(new CommittedWindow(offer,after,abandoned));return;}
        GameScene.show(new WndOptions(new ItemSprite(offer.item),Messages.get(Runecraft.class,"name"),Messages.get(Runecraft.class,"choose"),labels(offer)){
            private void abandon(){if(offer.cancel())abandoned.run();}
            @Override protected void onSelect(int index){if(index==offer.options.size())abandon();else if(offer.apply(index))after.run();else if(offer.committed)abandon();}
            @Override protected boolean hasInfo(int index){return index<offer.options.size();}
            @Override protected void onInfo(int index){GameScene.show(new WndTitledMessage(Icons.get(Icons.INFO),offer.name(index),offer.description(index)));}
            @Override public void onBackPressed(){abandon();super.onBackPressed();}
        });
    }
    private static String[] labels(Offer offer){
        String[] labels=new String[offer.options.size()+1];for(int i=0;i<offer.options.size();i++)labels[i]=offer.name(i);
        labels[labels.length-1]=Messages.get(Runecraft.class,offer.committed?"abandon":"cancel");return labels;
    }
    /** Public no-arg restoration preserves the paid offer when the tablet changes orientation. */
    public static class CommittedWindow extends WndOptions {
        private static CommittedWindow pending;
        private final Offer offer;
        private final Runnable after,abandoned;
        public CommittedWindow(){this(restorePending());}
        private CommittedWindow(CommittedWindow old){this(old.offer,old.after,old.abandoned);}
        private CommittedWindow(Offer offer,Runnable after,Runnable abandoned){
            super();
            this.offer=offer;this.after=after;this.abandoned=abandoned;
            initOptions(new ItemSprite(offer.item),Messages.get(Runecraft.class,"name"),Messages.get(Runecraft.class,"choose_committed"),labels(offer));
            pending=this;
        }
        private static CommittedWindow restorePending(){
            if(pending==null||!pending.offer.open)throw new IllegalStateException("No pending glyph choice");return pending;
        }
        private void clearPending(){if(pending==this)pending=null;}
        private void abandon(){clearPending();if(offer.cancel())abandoned.run();}
        @Override protected void onSelect(int index){
            if(index==offer.options.size())abandon();
            else if(offer.apply(index)){clearPending();after.run();}
            else abandon();
        }
        @Override protected boolean hasInfo(int index){return index<offer.options.size();}
        @Override protected void onInfo(int index){GameScene.show(new WndTitledMessage(Icons.get(Icons.INFO),offer.name(index),offer.description(index)));}
        @Override public void onBackPressed(){abandon();super.onBackPressed();}
    }
}
