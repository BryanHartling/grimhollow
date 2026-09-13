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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.Camera;
import com.watabou.utils.RepositoryUris;

/** Minimal recovery credits; upstream project links are reached through repository attribution. */
public class AboutScene extends PixelScene {
    @Override public void create() {
        super.create();
        add(new TitleBackground(Camera.main.width,Camera.main.height));
        float width=Math.min(220,Camera.main.width-24),left=(Camera.main.width-width)/2f,y=12;
        RenderedTextBlock title=PixelScene.renderTextBlock("Credits",12);title.hardlight(Window.TITLE_COLOR);
        title.setPos(left,y);add(title);y=title.bottom()+12;
        String[] labels={"Oleg Dolya — Pixel Dungeon", "Evan Debenham and contributors — Shattered Pixel Dungeon",
                "GPL-3.0-or-later license", "Grimhollow repository"};
        String[] paths={"#pixel-dungeon","#shattered-pixel-dungeon","/blob/grimhollow/LICENSE.txt",""};
        for(int i=0;i<labels.length;i++) {
            final String url=RepositoryUris.ROOT+paths[i];
            RedButton link=new RedButton(labels[i],8){@Override protected void onClick(){ShatteredPixelDungeon.platform.openURI(url);}};
            link.multiline=true;link.setRect(left,y,width,28);add(link);y=link.bottom()+6;
        }
        ExitButton exit=new ExitButton();exit.setPos(Camera.main.width-exit.width(),0);add(exit);
        fadeIn();
    }
    @Override protected void onBackPressed(){ShatteredPixelDungeon.switchNoFade(TitleScene.class);}
}
