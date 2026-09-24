// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Reflection;
import java.io.IOException;
import java.util.*;
import java.util.function.IntConsumer;

/** Paged, touch-sized controls. No nested scroll ownership or off-screen action buttons. */
public class WndPlaytest extends Window {
    private static class Entry {
        final String label; final Runnable action; final Item item;
        Entry(String label,Runnable action){this(label,action,null);}
        Entry(String label,Runnable action,Item item){this.label=label;this.action=action;this.item=item;}
    }
    public WndPlaytest(){this("Playtest",Playtest.enabled()
            ?"Level "+Dungeon.hero.lvl+" | Floor "+Dungeon.depth+" | God mode "+(Playtest.god()?"ON":"OFF")
            :"Enable testing for this save. This permanently marks it as a playtest: no rankings, badges, item-catalog credit or bones. Other saves stay normal. Powers can be toggled off.",rootEntries(),0,null);}

    private WndPlaytest(String title,String body,List<Entry> entries,int page,Runnable back){
        int width=(int)Math.min(PixelScene.landscape()?220:170,PixelScene.uiCamera.width-24);
        int maxHeight=(int)Math.min(230,PixelScene.uiCamera.height-30);
        RenderedTextBlock heading=PixelScene.renderTextBlock(title,9);
        heading.maxWidth(width-4);heading.hardlight(TITLE_COLOR);heading.setPos(2,2);add(heading);
        RenderedTextBlock hint=PixelScene.renderTextBlock(body,6);
        hint.maxWidth(width-4);hint.setPos(2,heading.bottom()+4);add(hint);
        float y=hint.bottom()+5;
        int count=Math.max(1,(int)((maxHeight-y-25)/24));
        int pages=Math.max(1,(entries.size()+count-1)/count);
        page=Math.max(0,Math.min(page,pages-1));
        final int current=page;
        for(int i=page*count;i<Math.min(entries.size(),(page+1)*count);i++){
            Entry entry=entries.get(i);
            RedButton button=new RedButton(entry.label,6){
                @Override protected void onClick(){hide();run(entry.action);}
            };
            button.multiline=true;
            if(entry.item!=null)button.icon(new ItemSprite(entry.item));
            add(button);button.setRect(0,y,width,22);y+=24;
        }
        if(entries.isEmpty()){
            RenderedTextBlock empty=PixelScene.renderTextBlock("No matching entries.",6);
            empty.setPos(2,y);add(empty);y+=18;
        }
        if(pages>1){
            RedButton previous=new RedButton("<",7){@Override protected void onClick(){hide();show(title,body,entries,current-1,back);}};
            add(previous);previous.setRect(0,y,28,20);previous.enable(page>0);
            RedButton next=new RedButton(">",7){@Override protected void onClick(){hide();show(title,body,entries,current+1,back);}};
            add(next);next.setRect(width-28,y,28,20);next.enable(page+1<pages);
            RedButton close=new RedButton((page+1)+"/"+pages+"  "+(back==null?"Close":"Back"),6){
                @Override protected void onClick(){hide();if(back!=null)run(back);}
            };
            add(close);close.setRect(30,y,width-60,20);
        }else{
            RedButton close=new RedButton(back==null?"Close":"Back",7){@Override protected void onClick(){hide();if(back!=null)run(back);}};
            add(close);close.setRect(0,y,width,20);
        }
        resize(width,(int)y+20);
    }
    private static void run(Runnable action){
        try{action.run();}catch(RuntimeException e){GameScene.show(new WndError(e.getMessage()==null?"Unable to apply this playtest action.":e.getMessage()));}
    }
    private static void show(String title,String body,List<Entry> rows,int page,Runnable back){GameScene.show(new WndPlaytest(title,body,rows,page,back));}
    private static void show(String title,String body,List<Entry> rows,Runnable back){show(title,body,rows,0,back);}
    private static void root(){GameScene.show(new WndPlaytest());}
    private static void save(){try{Dungeon.saveAll();}catch(IOException e){throw new IllegalStateException("Could not save playtest: "+e.getMessage(),e);}}
    private static void changed(Runnable action,Runnable after){action.run();save();after.run();}
    private static void reload(){save();InterlevelScene.mode=InterlevelScene.Mode.CONTINUE;Game.switchScene(InterlevelScene.class);}
    private static List<Entry> rootEntries(){
        List<Entry> rows=new ArrayList<>();
        if(!Playtest.enabled()){
            rows.add(new Entry("Enable Playtest for this save",()->changed(Playtest::enable,WndPlaytest::root)));
            return rows;
        }
        rows.add(new Entry("God mode: "+(Playtest.god()?"ON":"OFF"),()->changed(()->Playtest.god(!Playtest.god()),WndPlaytest::root)));
        rows.add(new Entry("Create items",WndPlaytest::categories));
        rows.add(new Entry("Edit carried equipment / recharge",WndPlaytest::inventory));
        rows.add(new Entry("Hero, class and progression",WndPlaytest::hero));
        rows.add(new Entry("Travel to any floor / quest branch",WndPlaytest::floors));
        rows.add(new Entry("Heal, feed and clear harmful effects",()->changed(Playtest::restoreHero,WndPlaytest::root)));
        rows.add(new Entry("Refill all item charges",()->changed(Playtest::recharge,WndPlaytest::root)));
        rows.add(new Entry("Gold and alchemy energy",WndPlaytest::resources));
        rows.add(new Entry("Reveal map, doors and traps",()->changed(Playtest::reveal,WndPlaytest::root)));
        rows.add(new Entry("Teleport within this floor",()->GameScene.selectCell(new CellSelector.Listener(){
            @Override public void onSelect(Integer cell){if(cell!=null)run(()->changed(()->Playtest.teleport(cell),()->{}));}
            @Override public String prompt(){return "Playtest: choose an empty walkable cell";}
        })));
        rows.add(new Entry("Spawn a creature",WndPlaytest::creatures));
        rows.add(new Entry("Identify all carried items",()->changed(()->Dungeon.hero.belongings.identify(),WndPlaytest::root)));
        return rows;
    }
    private static void number(String title,String body,int value,int min,int max,IntConsumer action,Runnable back){
        number(title,body,value,min,max,action,back,back);
    }
    private static void number(String title,String body,int value,int min,int max,IntConsumer action,Runnable back,Runnable cancel){
        GameScene.show(new WndTextInput(title,body+" ("+min+"-"+max+")",Integer.toString(value),6,false,"Apply","Cancel"){
            @Override public void onSelect(boolean positive,String text){
                if(!positive){cancel.run();return;}
                run(()->{
                    int parsed;
                    try{parsed=Integer.parseInt(text.trim());}catch(NumberFormatException e){throw new IllegalArgumentException("Enter a whole number from "+min+" to "+max+".");}
                    if(parsed<min || parsed>max)throw new IllegalArgumentException("Enter a value from "+min+" to "+max+".");
                    action.accept(parsed);save();back.run();
                });
            }
        });
    }
    private static void hero(){
        Hero h=Dungeon.hero;
        show("Hero setup",h.className()+" | Level "+h.lvl+" | Strength "+h.STR,Arrays.asList(
                new Entry("Change class and starter kit",WndPlaytest::classes),
                new Entry("Set hero level",()->number("Hero level","Resets allocated talents; choose a subclass and armor ability separately.",h.lvl,1,30,Playtest::heroLevel,WndPlaytest::hero)),
                new Entry("Set base strength",()->number("Base strength","Equipment and buff bonuses remain separate.",h.STR,1,50,Playtest::strength,WndPlaytest::hero)),
                new Entry("Choose subclass",WndPlaytest::subclasses),
                new Entry("Choose armor ability / grant class armor",WndPlaytest::abilities),
                new Entry("Max all talents in chosen paths",()->changed(Playtest::maximizeTalents,WndPlaytest::hero)),
                new Entry("Reset allocated talents",()->{Playtest.resetTalents();reload();}),
                new Entry("Learn every Enchanter inscription",()->changed(Playtest::learnInscriptions,WndPlaytest::hero))),WndPlaytest::root);
    }
    private static void classes(){
        List<Entry> rows=new ArrayList<>();
        for(HeroClass cls:HeroClass.values())rows.add(new Entry(Messages.titleCase(cls.title()),()->{Playtest.heroClass(cls);reload();}));
        show("Choose class","Keeps floor, level, strength and backpack. Gives the class starter kit; displaced gear is kept or dropped at your feet. Clears talents, temporary effects and summoned allies.",rows,WndPlaytest::hero);
    }
    private static void subclasses(){
        List<Entry> rows=new ArrayList<>();
        rows.add(new Entry("No subclass",()->{Playtest.subclass(HeroSubClass.NONE);reload();}));
        for(HeroSubClass sub:Dungeon.hero.heroClass.subClasses())rows.add(new Entry(Messages.titleCase(sub.title()),()->{Playtest.subclass(sub);reload();}));
        show("Subclass","Resets allocated talents; your class and items stay.",rows,WndPlaytest::hero);
    }
    private static void abilities(){
        List<Entry> rows=new ArrayList<>();
        for(ArmorAbility ability:Dungeon.hero.heroClass.armorAbilities())rows.add(new Entry(ability.name(),()->{Playtest.armorAbility(ability);reload();}));
        show("Armor ability","Converts worn armor to class armor, fills its charge and resets talents. Uses a plate base if no armor is worn.",rows,WndPlaytest::hero);
    }
    private static void resources(){
        show("Resources","Set totals directly; no turn passes.",Arrays.asList(
                new Entry("Gold: "+Dungeon.gold,()->number("Gold","New total",Dungeon.gold,0,999999,n->Dungeon.gold=n,WndPlaytest::resources)),
                new Entry("Energy: "+Dungeon.energy,()->number("Alchemy energy","New total",Dungeon.energy,0,999999,n->Dungeon.energy=n,WndPlaytest::resources))),WndPlaytest::root);
    }
    private static void floors(){
        List<Entry> rows=new ArrayList<>();
        rows.add(new Entry("Rebuild current floor",()->show("Rebuild floor "+Dungeon.depth,
                "Replace this floor's terrain, creatures and loot so you can repeat its encounters. Keep your hero and inventory. Other visited floors stay.",
                Arrays.asList(new Entry("Rebuild this floor",()->{Playtest.require();InterlevelScene.mode=InterlevelScene.Mode.PLAYTEST_RESET;Game.switchScene(InterlevelScene.class);})),WndPlaytest::floors)));
        for(int d=1;d<=26;d++){
            final int depth=d;
            String region=d<=5?"Sewers":d<=10?"Prison":d<=15?"Caves":d<=20?"City":d<=25?"Halls":"Amulet";
            rows.add(new Entry("Floor "+d+" - "+region+(Dungeon.bossLevel(d)?" boss":""),()->travel(depth,0)));
        }
        for(int d=11;d<=14;d++){final int depth=d;rows.add(new Entry("Mine branch at floor "+d,()->travel(depth,1)));}
        for(int d=16;d<=19;d++){final int depth=d;rows.add(new Entry("Imp Vault at floor "+d,()->travel(depth,1)));}
        show("Floor travel","Keep this hero and inventory. Skipped floors are generated in order; visited floors retain their state. Arrival is at the entrance, even in sealed arenas.",rows,WndPlaytest::root);
    }
    private static void travel(int depth,int branch){
        Playtest.require();
        InterlevelScene.returnDepth=depth;InterlevelScene.returnBranch=branch;
        InterlevelScene.mode=InterlevelScene.Mode.PLAYTEST;Game.switchScene(InterlevelScene.class);
    }
    private static String group(Item item){
        if(item instanceof Artifact)return "Artifacts and class focuses";
        if(item instanceof Trinket)return "Trinkets";
        if(item instanceof Weapon)return "Weapons and ammunition";
        if(item instanceof Armor)return "Armor";
        if(item instanceof Wand)return "Wands";
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)return "Rings";
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell)return "Spells";
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion)return "Potions, brews and elixirs";
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll)return "Scrolls";
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed)return "Seeds";
        return "Food, keys, stones, quest and other items";
    }
    private static String name(Item item){String name=item.trueName();return Messages.NO_TEXT_FOUND.equals(name)?item.getClass().getSimpleName():Messages.titleCase(name);}
    private static void categories(){
        List<Entry> rows=new ArrayList<>();
        rows.add(new Entry("Search all items",()->search(null)));
        Set<String> groups=new TreeSet<>();
        for(Class<? extends Item> cls:PlaytestCatalog.items())groups.add(group(Reflection.newInstance(cls)));
        for(String category:groups)rows.add(new Entry(category,()->items(category,"")));
        show("Create items",PlaytestCatalog.items().size()+" item types. Includes crafted spells, class focuses, artifacts, trinkets and quest objects.",rows,WndPlaytest::root);
    }
    private static void search(String category){
        GameScene.show(new WndTextInput("Find item","Search names, for example Ashlight, Soulfire or Mind Vision.","",48,false,"Search","Cancel"){
            @Override public void onSelect(boolean positive,String text){if(positive)items(category,text.trim());else categories();}
        });
    }
    private static void items(String category,String query){
        List<Entry> rows=new ArrayList<>();
        List<Item> matches=new ArrayList<>();
        for(Class<? extends Item> cls:PlaytestCatalog.items()){
            Item item=Reflection.newInstance(cls);
            if((category==null || group(item).equals(category)) && (name(item)+" "+cls.getSimpleName()).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))matches.add(item);
        }
        matches.sort(Comparator.comparing(WndPlaytest::name));
        rows.add(new Entry("Search / filter",()->search(category)));
        for(Item item:matches)rows.add(new Entry(name(item),()->spawnOptions(item,1,0,true,false,category,query),item));
        show(category==null?"All items":category,matches.size()+" matches"+(query.isEmpty()?"":" for \""+query+"\""),rows,WndPlaytest::categories);
    }
    private static void spawnOptions(Item template,int qty,int level,boolean identified,boolean cursed,String category,String query){
        Runnable back=()->items(category,query);
        List<Entry> rows=new ArrayList<>();
        rows.add(new Entry("Create "+qty+(template.stackable?"":" (single item)"),()->{
            Item item=Playtest.create(template.getClass(),qty,level,identified,cursed);
            boolean packed=Playtest.give(item);save();GLog.p(name(item)+(packed?" created.":" placed at your feet (inventory full)."));
            spawnOptions(template,qty,level,identified,cursed,category,query);
        },template));
        if(template.stackable)rows.add(new Entry("Quantity: "+qty,()->number("Quantity","Stack size",qty,1,100,
                n->spawnOptions(template,n,level,identified,cursed,category,query),()->{},()->spawnOptions(template,qty,level,identified,cursed,category,query))));
        int cap=Playtest.maxItemLevel(template);
        if(cap>0)rows.add(new Entry("Upgrade level: "+level,()->number("Item level","Displayed artifact levels are scaled to its native cap.",level,0,cap,
                n->spawnOptions(template,qty,n,identified,cursed,category,query),()->{},()->spawnOptions(template,qty,level,identified,cursed,category,query))));
        rows.add(new Entry("Identified: "+(identified?"YES":"NO"),()->spawnOptions(template,qty,level,!identified,cursed,category,query)));
        rows.add(new Entry("Cursed: "+(cursed?"YES":"NO"),()->spawnOptions(template,qty,level,identified,!cursed,category,query)));
        rows.add(new Entry("Description",()->{
            spawnOptions(template,qty,level,identified,cursed,category,query);
            GameScene.show(new WndTitledMessage(new ItemSprite(template),name(template),template.desc()));
        }));
        show(name(template),"Creates a new item; non-stackable items are created one at a time. Keys belong to the current floor.",rows,back);
    }
    private static void inventory(){
        List<Entry> rows=new ArrayList<>();
        for(Item item:Dungeon.hero.belongings)rows.add(new Entry(item.title(),()->edit(item),item));
        show("Carried items","Select an item to identify, recharge, change upgrades, curse or enchant.",rows,WndPlaytest::root);
    }
    private static void edit(Item item){
        List<Entry> rows=new ArrayList<>();int cap=Playtest.maxItemLevel(item);
        if(cap>0)rows.add(new Entry("Set upgrade level",()->number("Item level","Allowed upgrade range",item instanceof Artifact?item.visiblyUpgraded():item.trueLevel(),0,cap,n->Playtest.itemLevel(item,n),()->edit(item))));
        rows.add(new Entry("Identify",()->changed(item::identify,()->edit(item))));
        rows.add(new Entry("Recharge",()->changed(()->Playtest.recharge(item),()->edit(item))));
        rows.add(new Entry(item.cursed?"Remove binding curse":"Apply binding curse",()->changed(()->{item.cursed=!item.cursed;item.cursedKnown=true;Item.updateQuickslot();},()->edit(item))));
        if(item instanceof Weapon || item instanceof Armor)rows.add(new Entry("Choose enchantment / glyph / curse",()->enchantments(item)));
        show(item.title(),"Changes this existing item. Enchantment curses and the binding curse are separate controls.",rows,WndPlaytest::inventory);
    }
    private static void enchantments(Item item){
        List<Entry> rows=new ArrayList<>();
        rows.add(new Entry("Remove enchantment / glyph",()->changed(()->Playtest.enchant(item,null),()->edit(item))));
        for(Class<?> type:PlaytestCatalog.enchantments(item instanceof Armor))rows.add(new Entry(WndInscribe.name(type),()->changed(()->Playtest.enchant(item,type),()->edit(item))));
        show("Enchantment or glyph","Includes curses. Applies a permanent effect to this item.",rows,()->edit(item));
    }
    private static void creatures(){
        List<Entry> rows=new ArrayList<>();
        for(Class<? extends Mob> type:PlaytestCatalog.mobs()){
            Mob sample=Reflection.newInstance(type);
            rows.add(new Entry(Messages.titleCase(sample.name()),()->GameScene.selectCell(new CellSelector.Listener(){
                @Override public String prompt(){return "Place "+sample.name()+" on an empty walkable cell";}
                @Override public void onSelect(Integer cell){if(cell!=null)run(()->changed(()->Playtest.spawnMob(type,cell),()->{}));}
            })));
        }
        show("Spawn creatures","These use normal AI. Test arena bosses through Floor travel, so their arenas and encounter scripts exist.",rows,WndPlaytest::root);
    }
}
