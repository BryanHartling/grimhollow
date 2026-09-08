// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;
public class WndSupportPrompt extends WndOptions {
    public WndSupportPrompt(){super("Grimhollow","Report an issue or share feedback on the project repository.","GitHub","Close");}
    @Override protected void onSelect(int index){if(index==0)com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.platform.openURI("https://github.com/bryanhartling/grimhollow/issues");}
}
