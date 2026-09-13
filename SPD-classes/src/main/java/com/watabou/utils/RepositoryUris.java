// SPDX-License-Identifier: GPL-3.0-or-later
package com.watabou.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** The game can navigate only to its own repository; attribution links live there. */
public final class RepositoryUris {
    public static final String ROOT="https://github.com/BryanHartling/grimhollow";
    private RepositoryUris() {}

    public static boolean allowed(String value) {
        if(value==null)return false;
        try {
            URI uri=new URI(value);
            if(!"https".equalsIgnoreCase(uri.getScheme())||!"github.com".equalsIgnoreCase(uri.getHost())
                    ||uri.getUserInfo()!=null||(uri.getPort()!=-1&&uri.getPort()!=443))return false;
            String path=uri.getPath();
            if(path==null||!path.equals(uri.getRawPath())||!uri.normalize().getPath().equals(path))return false;
            path=path.toLowerCase(Locale.ROOT);
            return path.equals("/bryanhartling/grimhollow")||path.startsWith("/bryanhartling/grimhollow/");
        } catch(URISyntaxException ignored) {return false;}
    }
}
