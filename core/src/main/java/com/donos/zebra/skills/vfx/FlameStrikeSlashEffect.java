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
 * Flame Strike visual: fire gathering → animated slash trail → impact flash.
 * Gameplay timing stays in {@link com.donos.zebra.skills.SkillCaster}; this only renders.
 */
public final class FlameStrikeSlashEffect implements SkillEffect {

    private static final class Ember {
        float x, y, vx, vy, life, maxLife, size;
        float heat;
    }

    private final Player player;
    private final float facingDegrees;
    private final float duration;
    private final Animation<TextureRegion> slashAnim;
    private final Texture softGlow;
    private final Texture flameBlob;
    private final List<Ember> embers = new ArrayList<>();
    private float elapsed;
    private float impactFlashLife;
    private float originX;
    private float originY;

    public FlameStrikeSlashEffect(Player player,
                                  float facingDegrees,
                                  Animation<TextureRegion> slashAnim,
                                  Texture softGlow,
                                  Texture flameBlob) {
        this.player = player;
        this.facingDegrees = facingDegrees;
        this.duration = SkillAttackTiming.attackDurationSeconds();
        this.slashAnim = slashAnim;
        this.softGlow = softGlow;
        this.flameBlob = flameBlob;
        this.originX = player.getX();
        this.originY = player.getY();
    }

    public void notifyImpact() {
        impactFlashLife = 0.22f;
        spawnImpactEmbers();
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        if (player != null) {
            originX = player.getX();
            originY = player.getY();
        }
        if (impactFlashLife > 0f) {
            impactFlashLife -= delta;
        }

        // Continuous embers along the sword arc during the swing
        if (elapsed < duration * 0.9f && MathUtils.randomBoolean(0.65f)) {
            spawnArcEmber(elapsed / duration);
        }

        for (int i = embers.size() - 1; i >= 0; i--) {
            Ember e = embers.get(i);
            e.life -= delta;
            e.x += e.vx * delta;
            e.y += e.vy * delta;
            e.vy += 18f * delta;
            if (e.life <= 0f) {
                embers.remove(i);
            }
        }
    }

    @Override
    public boolean isAlive() {
        return elapsed < duration || impactFlashLife > 0f || !embers.isEmpty();
    }

    @Override
    public void renderSprites(SpriteBatch batch) {
        float progress = MathUtils.clamp(elapsed / duration, 0f, 1f);
        float facingRad = facingDegrees * MathUtils.degreesToRadians;

        // Soft sword glow gathering (additive)
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        float gather = MathUtils.clamp(progress / SkillAttackTiming.IMPACT_NORMALIZED, 0f, 1f);
        float glowAlpha = 0.35f + 0.4f * gather;
        if (progress > SkillAttackTiming.IMPACT_NORMALIZED) {
            glowAlpha *= MathUtils.clamp(1.2f - progress, 0.25f, 1f);
        }
        Color glow = SoftTextureFactory.fireTint(0.35f + 0.5f * gather);
        batch.setColor(glow.r, glow.g, glow.b, glowAlpha);
        float glowSize = 22f + 10f * gather;
        float gx = originX + MathUtils.cos(facingRad) * 8f;
        float gy = originY + MathUtils.sin(facingRad) * 8f;
        if (softGlow != null) {
            batch.draw(softGlow, gx - glowSize / 2f, gy - glowSize / 2f, glowSize, glowSize);
        }

        // Animated slash sprite following swing progress
        if (slashAnim != null) {
            TextureRegion frame = slashAnim.getKeyFrame(elapsed, false);
            float slashW = 48f;
            float slashH = 48f;
            float reach = 10f + 16f * progress;
            float sx = originX + MathUtils.cos(facingRad) * reach;
            float sy = originY + MathUtils.sin(facingRad) * reach;
            float rot = facingDegrees - 90f + (-40f + 75f * progress);
            batch.setColor(1f, 0.45f, 0.12f, 0.55f + 0.25f * gather);
            batch.draw(frame,
                sx - slashW / 2f, sy - slashH / 2f,
                slashW / 2f, slashH / 2f,
                slashW, slashH,
                1.15f, 1.15f,
                rot);
            // Hotter inner pass
            batch.setColor(1f, 0.85f, 0.35f, 0.35f * gather);
            batch.draw(frame,
                sx - slashW / 2f, sy - slashH / 2f,
                slashW / 2f, slashH / 2f,
                slashW, slashH,
                0.85f, 0.85f,
                rot);
        }

        // Embers
        if (flameBlob != null) {
            for (Ember e : embers) {
                float a = MathUtils.clamp(e.life / e.maxLife, 0f, 1f);
                Color c = SoftTextureFactory.fireTint(e.heat);
                batch.setColor(c.r, c.g, c.b, a * 0.85f);
                batch.draw(flameBlob, e.x - e.size / 2f, e.y - e.size / 2f, e.size, e.size);
            }
        }

        // Impact flash
        if (impactFlashLife > 0f && softGlow != null) {
            float a = MathUtils.clamp(impactFlashLife / 0.22f, 0f, 1f);
            float tipX = originX + MathUtils.cos(facingRad) * 26f;
            float tipY = originY + MathUtils.sin(facingRad) * 26f;
            batch.setColor(1f, 0.75f, 0.25f, 0.7f * a);
            float s = 36f + 20f * (1f - a);
            batch.draw(softGlow, tipX - s / 2f, tipY - s / 2f, s, s);
            batch.setColor(1f, 0.95f, 0.7f, 0.45f * a);
            batch.draw(softGlow, tipX - s * 0.35f, tipY - s * 0.35f, s * 0.7f, s * 0.7f);
        }

        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void spawnArcEmber(float progress) {
        float sweepStart = facingDegrees - 75f;
        float sweepEnd = facingDegrees + 35f;
        float ang = (sweepStart + (sweepEnd - sweepStart) * progress) * MathUtils.degreesToRadians;
        float reach = 14f + 18f * progress;
        Ember e = new Ember();
        e.x = originX + MathUtils.cos(ang) * reach + MathUtils.random(-2f, 2f);
        e.y = originY + MathUtils.sin(ang) * reach + MathUtils.random(-2f, 2f);
        e.vx = MathUtils.cos(ang) * MathUtils.random(8f, 22f);
        e.vy = MathUtils.sin(ang) * MathUtils.random(8f, 22f) + MathUtils.random(10f, 25f);
        e.maxLife = MathUtils.random(0.18f, 0.35f);
        e.life = e.maxLife;
        e.size = MathUtils.random(4f, 8f);
        e.heat = MathUtils.random(0.3f, 1f);
        embers.add(e);
    }

    private void spawnImpactEmbers() {
        float ang = facingDegrees * MathUtils.degreesToRadians;
        float tipX = originX + MathUtils.cos(ang) * 26f;
        float tipY = originY + MathUtils.sin(ang) * 26f;
        for (int i = 0; i < 14; i++) {
            float a = (facingDegrees - 50f + i * 8f) * MathUtils.degreesToRadians;
            Ember e = new Ember();
            e.x = tipX;
            e.y = tipY;
            e.vx = MathUtils.cos(a) * MathUtils.random(30f, 70f);
            e.vy = MathUtils.sin(a) * MathUtils.random(30f, 70f);
            e.maxLife = MathUtils.random(0.2f, 0.4f);
            e.life = e.maxLife;
            e.size = MathUtils.random(5f, 10f);
            e.heat = MathUtils.random(0.5f, 1f);
            embers.add(e);
        }
    }

    /** Builds slash animation from the loaded sheet, or null if unavailable. */
    public static Animation<TextureRegion> buildSlashAnimation(Texture sheet) {
        if (sheet == null) {
            return null;
        }
        int frameW = SkillVfxAssets.FLAME_SLASH_FRAME_SIZE;
        int count = Math.max(1, sheet.getWidth() / frameW);
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = new TextureRegion(sheet, i * frameW, 0, frameW, frameW);
        }
        float frameDuration = SkillAttackTiming.attackDurationSeconds() / count;
        return new Animation<>(frameDuration, frames);
    }
}
