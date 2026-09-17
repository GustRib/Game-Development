package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.ui.CraftingUI;

/**
 * World anvil / forge. Blocks movement and opens {@link CraftingUI} on interact.
 */
public class CraftingStation implements Entity, Interactable {

    private static final float SIZE = 20f;
    private static final float RADIUS = 28f;

    private final float x;
    private final float y;
    private final Texture texture;
    private final CraftingUI craftingUI;
    private final Polygon hitbox;
    private final Polygon collisionPolygon;

    public CraftingStation(float x, float y, Texture texture, CraftingUI craftingUI) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.craftingUI = craftingUI;
        this.hitbox = new Polygon(new float[]{0, 0, SIZE, 0, SIZE, SIZE, 0, SIZE});
        this.hitbox.setPosition(x - SIZE / 2f, y - SIZE / 2f);

        float left = x - SIZE / 2f;
        float bottom = y - SIZE / 2f;
        this.collisionPolygon = new Polygon(new float[]{
            left, bottom,
            left + SIZE, bottom,
            left + SIZE, bottom + SIZE,
            left, bottom + SIZE
        });
    }

    public Polygon getCollisionPolygon() {
        return collisionPolygon;
    }

    @Override
    public void onInteract(Player player) {
        craftingUI.open(player);
        player.setInteracting(true);
    }

    @Override
    public float getInteractionRadius() {
        return RADIUS;
    }

    @Override
    public String getPromptText() {
        return "[E] Usar forja";
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture == null) {
            return;
        }
        batch.setColor(0.55f, 0.55f, 0.6f, 1f);
        batch.draw(texture, x - SIZE / 2f, y - SIZE / 2f, SIZE, SIZE);
        batch.setColor(Color.WHITE);
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
}
