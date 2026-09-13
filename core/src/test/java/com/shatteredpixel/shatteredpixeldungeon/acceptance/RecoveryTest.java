// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.watabou.utils.RepositoryUris;
import com.watabou.utils.PlatformSupport;
import org.junit.Test;
import static org.junit.Assert.*;

public class RecoveryTest {
    @Test public void test46RepositoryNavigationCannotEscapeItsScope() throws Exception {
        for(String suffix:new String[]{"","/","/issues","/releases","#pixel-dungeon","#shattered-pixel-dungeon","/blob/grimhollow/LICENSE.txt"})
            assertTrue(suffix,RepositoryUris.allowed(RepositoryUris.ROOT+suffix));
        for(String uri:new String[]{null,"","https://github.com","http://github.com/BryanHartling/grimhollow",
                "https://github.com/00-Evan/shattered-pixel-dungeon","https://github.com/BryanHartling/grimhollow-evil",
                "https://github.com.evil.test/BryanHartling/grimhollow","https://evil.test/BryanHartling/grimhollow",
                "https://github.com@evil.test/BryanHartling/grimhollow","https://user@github.com/BryanHartling/grimhollow",
                "https://github.com:81/BryanHartling/grimhollow","https://github.com/BryanHartling/grimhollow/../other",
                "https://github.com/BryanHartling/grimhollow/%2e%2e/other","https://github.com/BryanHartling/grimhollow%2fother",
                "file:///tmp/test","mailto:user@example.test","javascript:alert(1)","https://github.com\\@evil.test/"})
            assertFalse(String.valueOf(uri),RepositoryUris.allowed(uri));
        assertTrue(java.lang.reflect.Modifier.isFinal(PlatformSupport.class.getMethod("openURI",String.class).getModifiers()));
    }
}
