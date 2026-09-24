// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.utils.RectF;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/** Additive flame particles may share draws without changing their simulation. */
final class FireParticleBatch {
    private static final int CAPACITY = 512;
    private final FloatBuffer vertices = Quad.createSet(CAPACITY);
    private final float[] quad = new float[16];
    private final float[] identity = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
    private final ArrayList<Batch> batches = new ArrayList<>();
    private Vertexbuffer buffer;

    private static class Batch {
        SmartTexture texture;
        float alpha;
        final ArrayList<Image> images = new ArrayList<>();
    }

    boolean draw(Group owner, ArrayList<Gizmo> members) {
        // Fall back for an unusual particle or an explicit per-particle camera.
        // The ordinary FlameParticle/Ember pair has white tint and zero rotation.
        for (Gizmo child : members) if (child != null && child.isVisible()) {
            if (!(child instanceof Image)) return false;
            Image image = (Image)child;
            if (image.texture == null || image.angle != 0 || image.camera() != owner.camera()
                    || image.rm != 1 || image.gm != 1 || image.bm != 1
                    || image.ra != 0 || image.ga != 0 || image.ba != 0 || image.aa != 0) return false;
        }
        for (Batch batch : batches) batch.images.clear();
        int used = 0;
        for (Gizmo child : members) if (child != null && child.isVisible()) {
            Image image = (Image)child;
            int slot = 0;
            while (slot < used && (batches.get(slot).texture != image.texture
                    || batches.get(slot).alpha != image.am)) slot++;
            if (slot == used) {
                if (used == batches.size()) batches.add(new Batch());
                Batch batch = batches.get(used++);
                batch.texture = image.texture;
                batch.alpha = image.am;
            }
            batches.get(slot).images.add(image);
        }
        if (used == 0) return true;
        NoosaScript script = NoosaScript.get();
        script.camera(owner.camera());
        script.uModel.valueM4(identity);
        Blending.setLightMode();
        try {
            for (int slot = 0; slot < used; slot++) {
                Batch batch = batches.get(slot);
                batch.texture.bind();
                script.lighting(1,1,1,batch.alpha,0,0,0,0);
                ((Buffer)vertices).clear();
                int count = 0;
                for (Image image : batch.images) {
                    RectF uv = image.frame();
                    float x = (image.x + image.origin.x) - image.origin.x * image.scale.x;
                    float y = (image.y + image.origin.y) - image.origin.y * image.scale.y;
                    Quad.fill(quad, x, x+image.width(), y, y+image.height(),
                            image.flipHorizontal ? uv.right : uv.left,
                            image.flipHorizontal ? uv.left : uv.right,
                            image.flipVertical ? uv.bottom : uv.top,
                            image.flipVertical ? uv.top : uv.bottom);
                    vertices.put(quad);
                    if (++count == CAPACITY) {
                        flush(script, count);
                        ((Buffer)vertices).clear();
                        count = 0;
                    }
                }
                if (count > 0) flush(script, count);
            }
        } finally {
            Blending.setNormalMode();
        }
        return true;
    }

    private void flush(NoosaScript script, int count) {
        ((Buffer)vertices).flip();
        if (buffer == null) buffer = new Vertexbuffer(vertices);
        else buffer.updateVertices(vertices);
        script.drawQuadSet(buffer, count, 0);
    }

    void destroy() { if (buffer != null) buffer.delete(); }
}
