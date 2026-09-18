package com.donos.zebra.skills.vfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns active skill visuals. Skills spawn effects here; GameScreen only updates/renders.
 */
public final class SkillEffectWorld {

    private final List<SkillEffect> effects = new ArrayList<>();

    public void spawn(SkillEffect effect) {
        if (effect != null) {
            effects.add(effect);
        }
    }

    public void update(float delta) {
        if (delta <= 0f) {
            return;
        }
        for (int i = effects.size() - 1; i >= 0; i--) {
            SkillEffect effect = effects.get(i);
            effect.update(delta);
            if (!effect.isAlive()) {
                effects.remove(i);
            }
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes) {
        if (effects.isEmpty()) {
            return;
        }
        // Sprite / additive pass
        batch.begin();
        for (SkillEffect effect : effects) {
            effect.renderSprites(batch);
        }
        // Reset blend in case an effect left additive mode
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (SkillEffect effect : effects) {
            effect.renderShapes(shapes);
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public int size() {
        return effects.size();
    }

    public void clear() {
        effects.clear();
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }
}
