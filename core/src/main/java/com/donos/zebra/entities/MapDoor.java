package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;

/**
 * Door that triggers a map transition (enter house / exit tavern).
 */
public class MapDoor implements Entity, Interactable {

    public interface Transition {
        void onUse(Player player);
    }

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final String prompt;
    private final Transition transition;
    private final Polygon hitbox;
    private final float radius;

    public MapDoor(float x, float y, float width, float height, String prompt, Transition transition) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.prompt = prompt;
        this.transition = transition;
        this.radius = Math.max(width, height) * 0.75f + 10f;
        this.hitbox = new Polygon(new float[]{0, 0, width, 0, width, height, 0, height});
        this.hitbox.setPosition(x - width / 2f, y - height / 2f);
    }

    @Override
    public void onInteract(Player player) {
        if (transition != null) {
            transition.onUse(player);
        }
    }

    @Override
    public float getInteractionRadius() {
        return radius;
    }

    @Override
    public String getPromptText() {
        return prompt;
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(SpriteBatch batch) {
        // Subtle marker so the door is findable without custom art.
    }

    /** Debug / light visual: draw a translucent rectangle via ShapeRenderer from GameScreen if needed. */
    public void renderMarker(ShapeRenderer shapes) {
        // Dark blue door plate matching village tileset door color
        shapes.setColor(0.15f, 0.2f, 0.45f, 0.7f);
        shapes.rect(x - width / 2f, y - height / 2f, width, height);
        shapes.setColor(0.35f, 0.4f, 0.55f, 0.85f);
        shapes.rect(x - 1.5f, y - 1.5f, 3f, 3f); // handle
    }

    @Override
    public void dispose() {
    }

    @Override
    public void takeDamage(float amount) {
    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public Polygon getHitbox() {
        return hitbox;
    }

    @Override
    public float getCurrentHealth() {
        return 1f;
    }

    public Color getMarkerColor() {
        return new Color(0.55f, 0.35f, 0.15f, 0.55f);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
