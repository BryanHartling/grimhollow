// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.watabou.utils.RepositoryUris;
import com.watabou.utils.PlatformSupport;
import org.junit.Test;
import static org.junit.Assert.*;

public class RecoveryTest {
    @Test public void textureFilteringWaitsForRenderThreadAndSurvivesReload() throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        com.badlogic.gdx.graphics.GL20 previous=com.badlogic.gdx.Gdx.gl;
        Thread renderThread=Thread.currentThread();
        java.util.List<Integer> filters=new java.util.ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger ids=new java.util.concurrent.atomic.AtomicInteger();
        com.badlogic.gdx.Gdx.gl=(com.badlogic.gdx.graphics.GL20)java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),new Class<?>[]{com.badlogic.gdx.graphics.GL20.class},(proxy,method,args)->{
                    assertSame("OpenGL must only run on the rendering thread",renderThread,Thread.currentThread());
                    if(method.getName().equals("glGenTexture"))return ids.incrementAndGet();
                    if(method.getName().equals("glTexParameterf")
                            &&((Integer)args[1]==com.badlogic.gdx.graphics.GL20.GL_TEXTURE_MIN_FILTER
                            ||(Integer)args[1]==com.badlogic.gdx.graphics.GL20.GL_TEXTURE_MAG_FILTER))
                        filters.add(((Float)args[2]).intValue());
                    return null;
                });
        com.watabou.gltextures.SmartTexture texture=null;
        try {
            texture=new com.watabou.gltextures.SmartTexture(new com.badlogic.gdx.graphics.Pixmap(2,2,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888));
            texture.bind();
            assertEquals(java.util.Arrays.asList(texture.NEAREST,texture.NEAREST),filters);
            filters.clear();
            com.watabou.gltextures.SmartTexture shared=texture;
            java.util.concurrent.atomic.AtomicReference<Throwable> error=new java.util.concurrent.atomic.AtomicReference<>();
            Thread actor=new Thread(()->{try {shared.filter(shared.LINEAR,shared.LINEAR);}catch(Throwable t){error.set(t);}},"filter-regression-actor");
            actor.start();actor.join(5000);
            assertFalse("Actor completed without a rendering dependency",actor.isAlive());
            if(error.get()!=null)throw new AssertionError("Actor filter touched OpenGL",error.get());
            assertTrue("Requesting filtering performs no OpenGL calls",filters.isEmpty());
            texture.bind();
            assertEquals(java.util.Arrays.asList(texture.LINEAR,texture.LINEAR),filters);
            filters.clear();texture.bind();
            assertTrue("Unchanged filtering does not reapply GPU state",filters.isEmpty());
            texture.reload();
            assertEquals(java.util.Arrays.asList(texture.LINEAR,texture.LINEAR),filters);
        } finally {
            if(texture!=null)texture.delete();
            com.watabou.glwrap.Texture.clear();
            com.badlogic.gdx.Gdx.gl=previous;
        }
    }

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
