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

    public static final String FORGE_TEXTURE_PATH = "maps/forja.png";

    private static final float SIZE = 16f;
    private static final float RADIUS = 30f;

    private final float x;
    private final float y;
    private final CraftingUI craftingUI;
    private final Polygon hitbox;
    private final Polygon collisionPolygon;
    private final Texture anvilTexture;
    private final boolean ownsTexture;

    private boolean interactionTargeted;
    private float pulseTime;

    public CraftingStation(float x, float y, Texture forgeTexture, CraftingUI craftingUI) {
        this.x = x;
        this.y = y;
        this.craftingUI = craftingUI;
        this.anvilTexture = forgeTexture;
        this.ownsTexture = false;
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

    /** Fallback when texture not loaded (tests). */
    public CraftingStation(float x, float y, CraftingUI craftingUI) {
        this(x, y, (Texture) null, craftingUI);
    }

    public Polygon getCollisionPolygon() {
        return collisionPolygon;
    }

    public void setInteractionTargeted(boolean targeted) {
        this.interactionTargeted = targeted;
    }

    public boolean isInteractionTargeted() {
        return interactionTargeted;
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
        if (interactionTargeted) {
            pulseTime += delta;
        } else {
            pulseTime = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (interactionTargeted) {
            float pulse = 0.75f + 0.25f * (float) Math.sin(pulseTime * 8f);
            batch.setColor(1f, 0.95f * pulse, 0.35f, 1f);
        } else {
            batch.setColor(Color.WHITE);
        }
        if (anvilTexture != null) {
            batch.draw(anvilTexture, x - SIZE / 2f, y - SIZE / 2f, SIZE, SIZE);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        if (ownsTexture && anvilTexture != null) {
            anvilTexture.dispose();
        }
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
