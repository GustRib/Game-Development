package com.donos.zebra.skills.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Procedural soft textures for glow/sparks (no ParticleEffect asset required).
 */
public final class SoftTextureFactory {

    private SoftTextureFactory() {
    }

    public static Texture softCircle(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float cx = (size - 1) * 0.5f;
        float cy = (size - 1) * 0.5f;
        float maxR = size * 0.5f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy) / maxR;
                float a = d >= 1f ? 0f : (1f - d) * (1f - d);
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    public static Texture softFlameBlob(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float cx = (size - 1) * 0.5f;
        float cy = (size - 1) * 0.55f;
        float maxR = size * 0.48f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - cx) / maxR;
                float dy = (y - cy) / (maxR * 1.15f);
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = d >= 1f ? 0f : (1f - d);
                a = a * a;
                pixmap.setColor(1f, 0.85f, 0.35f, a);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /** Elongated soft streak for wind / slash trails. */
    public static Texture softStreak(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        float cx = (width - 1) * 0.5f;
        float cy = (height - 1) * 0.5f;
        float invW = 1f / Math.max(1f, cx);
        float invH = 1f / Math.max(1f, cy);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = (x - cx) * invW;
                float dy = (y - cy) * invH;
                float d = (float) Math.sqrt(dx * dx + dy * dy * 4f);
                float a = d >= 1f ? 0f : (1f - d);
                a = a * a;
                // brighter along the long axis tip
                float tip = 1f - Math.abs(dx);
                a *= 0.55f + 0.45f * tip;
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Tapered wind ribbon: bright head, fading tail, soft edges — reads as air, not a laser.
     */
    public static Texture softWindRibbon(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        float cy = (height - 1) * 0.5f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float along = x / (float) Math.max(1, width - 1); // 0=tail, 1=head
                float taper = along * along; // thinner toward tail via alpha
                float ny = Math.abs(y - cy) / Math.max(1f, cy);
                float edge = ny >= 1f ? 0f : (1f - ny);
                edge = edge * edge;
                // slight vertical curve bias so the ribbon feels bent
                float curve = (float) Math.sin(along * Math.PI) * 0.35f;
                float nyCurve = Math.abs((y - cy) / Math.max(1f, cy) - curve * (1f - along));
                float edgeCurve = nyCurve >= 1f ? 0f : (1f - nyCurve);
                edgeCurve = edgeCurve * edgeCurve;
                float a = Math.max(edge, edgeCurve) * (0.25f + 0.75f * taper) * (0.4f + 0.6f * along);
                if (a < 0.02f) {
                    continue;
                }
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Partial soft arc (ring segment) for irregular tornado bands — not a full neon circle.
     */
    public static Texture softWindArc(int size, float startDeg, float sweepDeg) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float cx = (size - 1) * 0.5f;
        float cy = (size - 1) * 0.5f;
        float outer = size * 0.48f;
        float inner = size * 0.32f;
        float start = startDeg * (float) (Math.PI / 180.0);
        float sweep = sweepDeg * (float) (Math.PI / 180.0);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float r = (float) Math.sqrt(dx * dx + dy * dy);
                if (r < inner || r > outer) {
                    continue;
                }
                float ang = (float) Math.atan2(dy, dx);
                // Normalize relative to start into [0, 2PI)
                float rel = ang - start;
                while (rel < 0f) {
                    rel += MathUtilsTwoPi();
                }
                while (rel >= MathUtilsTwoPi()) {
                    rel -= MathUtilsTwoPi();
                }
                if (rel > sweep) {
                    continue;
                }
                float radial = (r - inner) / (outer - inner);
                float radialSoft = 1f - Math.abs(radial * 2f - 1f);
                radialSoft = radialSoft * radialSoft;
                float along = rel / sweep;
                // taper both ends of the arc
                float endFade = Math.min(along, 1f - along) * 3f;
                if (endFade > 1f) {
                    endFade = 1f;
                }
                float a = radialSoft * endFade * 0.95f;
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static float MathUtilsTwoPi() {
        return (float) (Math.PI * 2.0);
    }

    public static Color fireTint(float heat) {
        // heat 0 = deep red/orange, 1 = bright yellow
        float h = Math.max(0f, Math.min(1f, heat));
        return new Color(1f, 0.25f + 0.55f * h, 0.05f + 0.25f * h, 1f);
    }
}
