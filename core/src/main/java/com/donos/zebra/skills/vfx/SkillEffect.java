package com.donos.zebra.skills.vfx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Modular skill visual effect. Implementations may use sprites, particles, shapes, or mixes.
 */
public interface SkillEffect {

    void update(float delta);

    boolean isAlive();

    /** SpriteBatch / additive layered draw. Default no-op. */
    default void renderSprites(SpriteBatch batch) {
    }

    /** ShapeRenderer filled/line pass. Default no-op. */
    default void renderShapes(ShapeRenderer shapes) {
    }
}
