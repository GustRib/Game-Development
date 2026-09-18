package com.donos.zebra.skills.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.skills.BurningStatus;

/**
 * Persistent burning feedback while {@link BurningStatus} is active on an enemy.
 */
public final class BurningAuraEffect implements SkillEffect {

    private final Enemy target;
    private final Texture softGlow;
    private final Texture flameBlob;
    private float time;

    public BurningAuraEffect(Enemy target, Texture softGlow, Texture flameBlob) {
        this.target = target;
        this.softGlow = softGlow;
        this.flameBlob = flameBlob;
    }

    @Override
    public void update(float delta) {
        time += delta;
    }

    @Override
    public boolean isAlive() {
        return target != null && !target.isDead() && target.getStatusEffects().hasBurning();
    }

    @Override
    public void renderSprites(SpriteBatch batch) {
        if (!isAlive()) {
            return;
        }
        float x = target.getX();
        float y = target.getY();
        float pulse = 0.7f + 0.3f * MathUtils.sin(time * 11f);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (softGlow != null) {
            batch.setColor(1f, 0.35f, 0.05f, 0.35f * pulse);
            float s = 18f * pulse;
            batch.draw(softGlow, x - s / 2f, y - 2f, s, s);
        }
        if (flameBlob != null) {
            for (int i = 0; i < 3; i++) {
                float ox = MathUtils.sin(time * (9f + i) + i) * (3f + i);
                float oy = 4f + MathUtils.cos(time * (7f + i)) * 2f + i * 3f;
                float a = 0.45f + 0.2f * MathUtils.sin(time * 13f + i);
                Color c = SoftTextureFactory.fireTint(0.4f + i * 0.2f);
                batch.setColor(c.r, c.g, c.b, a);
                float size = 6f + i;
                batch.draw(flameBlob, x + ox - size / 2f, y + oy - size / 2f, size, size);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
