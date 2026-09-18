package com.donos.zebra.skills.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.donos.zebra.entities.Player;
import com.donos.zebra.skills.SkillAttackTiming;

import java.util.ArrayList;
import java.util.List;

/**
 * Whirlwind visual: a small tornado / violent wind vortex centered on the player.
 * Wind silhouette and rotation dominate; glow and slash sprites are secondary.
 */
public final class WhirlwindRingEffect implements SkillEffect {

    private static final class Debris {
        float angle;
        float radius;
        float height;
        float angVel;
        float radVel;
        float life;
        float maxLife;
        float size;
        boolean flung;
    }

    private static final class AirStreak {
        float angle;
        float radius;
        float length;
        float thickness;
        float life;
        float maxLife;
        float angVel;
        float alpha;
    }

    private final Player player;
    private final float radius;
    private final float duration;
    private final Texture softGlow;
    private final Texture windRibbon;
    private final Texture windArc;
    private final Texture windStreak;
    private final Texture sparkBlob;
    private final Animation<TextureRegion> slashAnim;
    private final List<Debris> debris = new ArrayList<>();
    private final List<AirStreak> streaks = new ArrayList<>();
    private final float[] bandPhase = new float[6];
    private final float[] bandSpeed = {520f, 380f, 290f, 610f, 240f, 450f};
    private float elapsed;
    private float originX;
    private float originY;
    private boolean endBurstSpawned;

    public WhirlwindRingEffect(Player player,
                               float radius,
                               Texture softGlow,
                               Texture windRibbon,
                               Texture windArc,
                               Texture windStreak,
                               Texture sparkBlob,
                               Animation<TextureRegion> slashAnim) {
        this.player = player;
        this.radius = radius;
        this.duration = Math.max(0.45f, SkillAttackTiming.attackDurationSeconds() * 0.95f);
        this.softGlow = softGlow;
        this.windRibbon = windRibbon;
        this.windArc = windArc;
        this.windStreak = windStreak;
        this.sparkBlob = sparkBlob;
        this.slashAnim = slashAnim;
        this.originX = player.getX();
        this.originY = player.getY();
        for (int i = 0; i < bandPhase.length; i++) {
            bandPhase[i] = MathUtils.random(360f);
        }
        spawnInitialOrbitDebris();
    }

    /** Headless / simple test constructor (no GPU textures). */
    public WhirlwindRingEffect(Player player, float radius,
                               Texture softGlow, Texture streak, Texture sparkBlob,
                               Animation<TextureRegion> slashAnim) {
        this(player, radius, softGlow, streak, null, streak, sparkBlob, slashAnim);
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        if (player != null) {
            originX = player.getX();
            originY = player.getY();
        }

        float progress = MathUtils.clamp(elapsed / duration, 0f, 1f);
        float intensity = vortexIntensity(progress);

        for (int i = 0; i < bandPhase.length; i++) {
            bandPhase[i] += bandSpeed[i] * delta * (0.55f + 0.7f * intensity);
        }

        if (progress < 0.72f && MathUtils.randomBoolean(0.55f + 0.35f * intensity)) {
            spawnAirStreak(progress, intensity);
        }
        if (progress < 0.65f && MathUtils.randomBoolean(0.25f)) {
            spawnOrbitDebris(progress);
        }
        if (!endBurstSpawned && progress >= 0.62f) {
            endBurstSpawned = true;
            spawnOutwardBurst();
        }

        for (int i = debris.size() - 1; i >= 0; i--) {
            Debris d = debris.get(i);
            d.life -= delta;
            d.angle += d.angVel * delta;
            d.radius += d.radVel * delta;
            if (!d.flung) {
                // Pull toward a mid orbit then keep circling
                float target = radius * (0.35f + 0.25f * MathUtils.sin(elapsed * 3f + d.angle * 0.02f));
                d.radVel += (target - d.radius) * 2.5f * delta;
                d.radVel *= 0.92f;
                d.angVel = MathUtils.clamp(d.angVel + MathUtils.random(-40f, 40f) * delta, 180f, 720f);
            }
            d.height += MathUtils.sin(elapsed * 8f + d.angle) * 6f * delta;
            if (d.life <= 0f || d.radius < 2f || d.radius > radius * 1.6f) {
                debris.remove(i);
            }
        }

        for (int i = streaks.size() - 1; i >= 0; i--) {
            AirStreak s = streaks.get(i);
            s.life -= delta;
            s.angle += s.angVel * delta;
            s.radius += MathUtils.sin(s.angle * 0.05f) * 8f * delta;
            if (s.life <= 0f) {
                streaks.remove(i);
            }
        }
    }

    @Override
    public boolean isAlive() {
        return elapsed < duration || !debris.isEmpty() || !streaks.isEmpty();
    }

    @Override
    public void renderSprites(SpriteBatch batch) {
        float progress = MathUtils.clamp(elapsed / duration, 0f, 1f);
        float fade = envelope(progress);
        float intensity = vortexIntensity(progress);
        float form = MathUtils.clamp(progress / 0.18f, 0f, 1f);
        float funnel = radius * (0.42f + 0.58f * form) * (0.85f + 0.2f * intensity);

        // Mostly normal alpha so wind reads as air, not neon energy
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawTornadoBands(batch, funnel, fade, intensity, form);
        drawWindRibbons(batch, funnel, fade, intensity);
        drawAirStreaks(batch, fade);
        drawSecondarySlashHints(batch, funnel, fade, intensity);
        drawDebris(batch, fade);

        // Subtle supportive glow only (additive, low alpha)
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (softGlow != null) {
            batch.setColor(0.78f, 0.9f, 1f, 0.07f * fade * intensity);
            float g = funnel * 1.15f;
            batch.draw(softGlow, originX - g / 2f, originY - g * 0.35f, g, g * 0.85f);
        }
        if (progress >= 0.58f && progress < 0.78f && softGlow != null) {
            float burst = 1f - Math.abs(progress - 0.68f) / 0.1f;
            burst = MathUtils.clamp(burst, 0f, 1f);
            batch.setColor(0.9f, 0.96f, 1f, 0.16f * burst * fade);
            float s = radius * (0.7f + 0.5f * burst);
            batch.draw(softGlow, originX - s / 2f, originY - s / 2f, s, s);
        }

        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Stacked partial arcs at different heights/radii → funnel / tornado silhouette.
     */
    private void drawTornadoBands(SpriteBatch batch, float funnel, float fade, float intensity, float form) {
        if (windArc == null && windRibbon == null) {
            return;
        }
        // Base wide → top narrow (top-down volumetric hint via Y offset + scale)
        float[] radiusFrac = {1.00f, 0.88f, 0.74f, 0.60f, 0.48f, 0.36f};
        float[] heightOff = {-2f, 2f, 6f, 10f, 14f, 18f};
        float[] scaleY = {0.55f, 0.5f, 0.45f, 0.4f, 0.35f, 0.3f};
        for (int i = 0; i < radiusFrac.length; i++) {
            float ang = bandPhase[i];
            float r = funnel * radiusFrac[i] * (0.9f + 0.08f * MathUtils.sin(elapsed * 5f + i));
            float cx = originX;
            float cy = originY + heightOff[i] * form;
            float size = r * 2.05f;
            float a = (0.22f + 0.08f * (i % 2)) * fade * (0.55f + 0.45f * intensity);
            // Pale white / soft cyan, not saturated glow
            if (i % 2 == 0) {
                batch.setColor(0.92f, 0.96f, 1f, a);
            } else {
                batch.setColor(0.72f, 0.86f, 0.95f, a * 0.9f);
            }
            Texture tex = windArc != null ? windArc : windRibbon;
            batch.draw(tex,
                cx - size / 2f, cy - size * scaleY[i] / 2f,
                size / 2f, size * scaleY[i] / 2f,
                size, size * scaleY[i],
                1f, 1f,
                ang,
                0, 0, tex.getWidth(), tex.getHeight(),
                false, false);
            // Second opposite arc for irregular silhouette
            batch.setColor(0.88f, 0.94f, 1f, a * 0.55f);
            batch.draw(tex,
                cx - size / 2f, cy - size * scaleY[i] / 2f,
                size / 2f, size * scaleY[i] / 2f,
                size, size * scaleY[i],
                1.05f, 0.9f,
                ang + 150f + i * 12f,
                0, 0, tex.getWidth(), tex.getHeight(),
                true, false);
        }
    }

    /** Curved tapered ribbons that spiral with the vortex. */
    private void drawWindRibbons(SpriteBatch batch, float funnel, float fade, float intensity) {
        if (windRibbon == null) {
            return;
        }
        int count = 7;
        for (int i = 0; i < count; i++) {
            float ang = bandPhase[i % bandPhase.length] * (0.7f + i * 0.05f) + i * 51f;
            float angRad = ang * MathUtils.degreesToRadians;
            float reach = funnel * (0.25f + 0.55f * ((i % 5) / 5f));
            float hx = originX + MathUtils.cos(angRad) * reach;
            float hy = originY + MathUtils.sin(angRad) * reach * 0.72f + (i % 3) * 3f;
            float w = funnel * (0.55f + 0.2f * (i % 3) / 3f);
            float h = 5f + (i % 4) * 1.5f;
            float a = (0.18f + 0.1f * ((i + 1) % 3) / 3f) * fade * intensity;
            batch.setColor(0.9f, 0.95f, 1f, a);
            batch.draw(windRibbon,
                hx - w / 2f, hy - h / 2f,
                w * 0.15f, h / 2f,
                w, h,
                1f, 1f,
                ang + 18f,
                0, 0, windRibbon.getWidth(), windRibbon.getHeight(),
                false, false);
            if (i % 2 == 0) {
                float inner = reach * 0.55f;
                float ix = originX + MathUtils.cos(angRad + 0.4f) * inner;
                float iy = originY + MathUtils.sin(angRad + 0.4f) * inner * 0.7f + 8f;
                batch.setColor(0.75f, 0.88f, 0.96f, a * 0.7f);
                batch.draw(windRibbon,
                    ix - w * 0.35f, iy - h * 0.4f,
                    w * 0.1f, h * 0.4f,
                    w * 0.7f, h * 0.8f,
                    1f, 1f,
                    ang + 40f,
                    0, 0, windRibbon.getWidth(), windRibbon.getHeight(),
                    true, false);
            }
        }
    }

    private void drawAirStreaks(SpriteBatch batch, float fade) {
        Texture tex = windStreak != null ? windStreak : windRibbon;
        if (tex == null) {
            return;
        }
        for (AirStreak s : streaks) {
            float lifeA = MathUtils.clamp(s.life / s.maxLife, 0f, 1f);
            float angRad = s.angle * MathUtils.degreesToRadians;
            float x = originX + MathUtils.cos(angRad) * s.radius;
            float y = originY + MathUtils.sin(angRad) * s.radius * 0.75f;
            batch.setColor(0.85f, 0.92f, 0.98f, s.alpha * lifeA * fade);
            batch.draw(tex,
                x - s.length / 2f, y - s.thickness / 2f,
                s.length * 0.2f, s.thickness / 2f,
                s.length, s.thickness,
                1f, 1f,
                s.angle + 90f,
                0, 0, tex.getWidth(), tex.getHeight(),
                false, false);
        }
    }

    private void drawSecondarySlashHints(SpriteBatch batch, float funnel, float fade, float intensity) {
        if (slashAnim == null) {
            return;
        }
        TextureRegion frame = slashAnim.getKeyFrame(elapsed * 1.2f, true);
        // Only two faint hints — sword motion, not the main read
        for (int i = 0; i < 2; i++) {
            float angDeg = bandPhase[i] * 0.85f + i * 180f;
            float ang = angDeg * MathUtils.degreesToRadians;
            float reach = funnel * 0.62f;
            float sx = originX + MathUtils.cos(ang) * reach;
            float sy = originY + MathUtils.sin(ang) * reach * 0.7f + 4f;
            float size = 22f;
            batch.setColor(0.8f, 0.9f, 1f, 0.12f * fade * intensity);
            batch.draw(frame,
                sx - size / 2f, sy - size / 2f,
                size / 2f, size / 2f,
                size, size,
                1f, 1f,
                angDeg + 90f);
        }
    }

    private void drawDebris(SpriteBatch batch, float fade) {
        if (sparkBlob == null) {
            return;
        }
        for (Debris d : debris) {
            float lifeA = MathUtils.clamp(d.life / d.maxLife, 0f, 1f);
            float angRad = d.angle * MathUtils.degreesToRadians;
            float x = originX + MathUtils.cos(angRad) * d.radius;
            float y = originY + MathUtils.sin(angRad) * d.radius * 0.72f + d.height;
            // Dusty gray-white, not bright cyan energy
            batch.setColor(0.82f, 0.86f, 0.9f, 0.75f * lifeA * fade);
            float s = d.size;
            batch.draw(sparkBlob, x - s / 2f, y - s / 2f, s, s);
        }
    }

    private static float envelope(float progress) {
        if (progress < 0.12f) {
            return MathUtils.clamp(progress / 0.12f, 0f, 1f);
        }
        if (progress > 0.7f) {
            return MathUtils.clamp(1f - (progress - 0.7f) / 0.3f, 0f, 1f);
        }
        return 1f;
    }

    private static float vortexIntensity(float progress) {
        // Peak around mid-attack / impact window
        float peak = 1f - Math.abs(progress - 0.45f) / 0.45f;
        return MathUtils.clamp(peak, 0.25f, 1f);
    }

    private void spawnInitialOrbitDebris() {
        for (int i = 0; i < 10; i++) {
            Debris d = new Debris();
            d.angle = i * 36f + MathUtils.random(-8f, 8f);
            d.radius = radius * MathUtils.random(0.2f, 0.75f);
            d.height = MathUtils.random(-1f, 10f);
            d.angVel = MathUtils.random(260f, 640f);
            d.radVel = MathUtils.random(-15f, 10f);
            d.maxLife = MathUtils.random(0.35f, duration * 0.85f);
            d.life = d.maxLife;
            d.size = MathUtils.random(2.5f, 5f);
            d.flung = false;
            debris.add(d);
        }
    }

    private void spawnOrbitDebris(float progress) {
        Debris d = new Debris();
        d.angle = MathUtils.random(360f);
        d.radius = radius * MathUtils.random(0.55f, 1.05f);
        d.height = MathUtils.random(0f, 12f);
        d.angVel = MathUtils.random(300f, 700f);
        d.radVel = MathUtils.random(-40f, -10f); // get pulled in
        d.maxLife = MathUtils.random(0.2f, 0.45f);
        d.life = d.maxLife;
        d.size = MathUtils.random(2f, 4.5f);
        d.flung = false;
        debris.add(d);
    }

    private void spawnOutwardBurst() {
        for (int i = 0; i < 12; i++) {
            Debris d = new Debris();
            d.angle = i * 30f + MathUtils.random(-6f, 6f);
            d.radius = radius * MathUtils.random(0.25f, 0.45f);
            d.height = MathUtils.random(0f, 8f);
            d.angVel = MathUtils.random(120f, 280f);
            d.radVel = MathUtils.random(55f, 110f);
            d.maxLife = MathUtils.random(0.18f, 0.35f);
            d.life = d.maxLife;
            d.size = MathUtils.random(2.5f, 5.5f);
            d.flung = true;
            debris.add(d);
        }
    }

    private void spawnAirStreak(float progress, float intensity) {
        AirStreak s = new AirStreak();
        s.angle = MathUtils.random(360f);
        s.radius = radius * MathUtils.random(0.3f, 0.95f) * (0.6f + 0.4f * progress);
        s.length = MathUtils.random(14f, 28f) * (0.7f + 0.5f * intensity);
        s.thickness = MathUtils.random(2.5f, 5f);
        s.maxLife = MathUtils.random(0.08f, 0.18f);
        s.life = s.maxLife;
        s.angVel = MathUtils.random(400f, 900f);
        s.alpha = MathUtils.random(0.25f, 0.5f);
        streaks.add(s);
    }
}
