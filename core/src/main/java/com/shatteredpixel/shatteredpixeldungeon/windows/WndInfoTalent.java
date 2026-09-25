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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Callback;

public class WndInfoTalent extends WndTitledMessage {
    public WndInfoTalent(Talent talent,int points,TalentButtonCallback callback){
        super(new TalentIcon(talent),Messages.titleCase(talent.title()),description(talent,points,callback));
        if(callback!=null){
            RedButton upgrade=new RedButton(callback.prompt(),7){@Override protected void onClick(){hide();callback.call();}};
            upgrade.icon(Icons.get(Icons.TALENT));add(upgrade);upgrade.setRect(0,height+3,width,20);resize(width,height+24);
        }
    }
    public static String description(Talent talent,int points,TalentButtonCallback callback){
        boolean meta=(callback!=null&&callback.metamorphDesc()) || (Dungeon.hero!=null&&Dungeon.hero.metamorphedTalents.containsValue(talent));
        StringBuilder text=new StringBuilder(Messages.get(WndInfoTalent.class,"progression",Math.min(points,talent.maxPoints()),talent.maxPoints()));
        if(meta || Messages.NO_TEXT_FOUND.equals(Messages.get(Talent.class,talent.name()+".rank1")))
            return text+"\n\n"+talent.desc(meta);
        for(int rank=1;rank<=talent.maxPoints();rank++){
            String rankText=Messages.get(Talent.class,talent.name()+".rank"+rank);
            if(Messages.NO_TEXT_FOUND.equals(rankText))return text+"\n\n"+talent.desc(meta);
            text.append("\n\n").append(Messages.get(WndInfoTalent.class,"rank",rank)).append("\n").append(rankText);
        }
        return text.toString();
    }
    @Override protected float targetHeight(){return Math.min(220,PixelScene.uiCamera.height-40)-26;}
    public static abstract class TalentButtonCallback implements Callback {
        public abstract String prompt();
        public boolean metamorphDesc(){return false;}
    }
}
