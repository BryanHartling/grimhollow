// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;

/** Pure CPU light accumulation; testable without OpenGL. Values multiply terrain once. */
public final class LightMap {
    public static final float[][] AMBIENT = {
        {.50f, .55f, .45f}, {.50f, .50f, .58f}, {.60f, .45f, .35f},
        {.50f, .55f, .62f}, {.40f, .40f, .52f}
    };
    public final int width, height, samples;
    private final float[] rgb;

    public LightMap(int cellsWide, int cellsHigh, int samples) {
        this.samples = samples;
        width = cellsWide * samples;
        height = cellsHigh * samples;
        rgb = new float[width * height * 3];
    }
    public void clear(int region) {
        float[] ambient = AMBIENT[Math.max(0, Math.min(4, region))];
        for (int i = 0; i < rgb.length; i += 3) {
            System.arraycopy(ambient, 0, rgb, i, 3);
        }
    }
    public static float falloff(float distance, float radius) {
        if (radius <= 0 || distance >= radius) return 0;
        float remaining = 1 - Math.max(0, distance) / radius;
        return remaining * remaining;
    }
    public void add(float x, float y, float radius, float r, float g, float b) {
        int left = Math.max(0, (int)((x-radius)*samples));
        int right = Math.min(width, (int)Math.ceil((x+radius)*samples));
        int top = Math.max(0, (int)((y-radius)*samples));
        int bottom = Math.min(height, (int)Math.ceil((y+radius)*samples));
        for (int py = top; py < bottom; py++) for (int px = left; px < right; px++) {
            float dx = (px + .5f) / samples - x;
            float dy = (py + .5f) / samples - y;
            float strength = falloff((float)Math.sqrt(dx*dx+dy*dy), radius);
            int offset = (px + py*width)*3;
            rgb[offset] = Math.min(1, rgb[offset] + strength*r);
            rgb[offset+1] = Math.min(1, rgb[offset+1] + strength*g);
            rgb[offset+2] = Math.min(1, rgb[offset+2] + strength*b);
        }
    }
    public int rgba(int x, int y) {
        int offset = (x+y*width)*3;
        return ((int)(255*rgb[offset])<<24) | ((int)(255*rgb[offset+1])<<16)
                | ((int)(255*rgb[offset+2])<<8) | 255;
    }
}
