package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.ui.CraftingUI;

/**
 * World anvil / forge. Blocks movement and opens {@link CraftingUI} on interact.
 * Draws a simple procedural anvil (no final forge art in project assets yet).
 */
public class CraftingStation implements Entity, Interactable {

    private static final float WIDTH = 28f;
    private static final float HEIGHT = 22f;
    private static final float RADIUS = 30f;

    private final float x;
    private final float y;
    private final CraftingUI craftingUI;
    private final Polygon hitbox;
    private final Polygon collisionPolygon;
    private final Texture anvilTexture;
    private final boolean ownsTexture;

    public CraftingStation(float x, float y, Texture ignoredLegacyIcon, CraftingUI craftingUI) {
        this(x, y, craftingUI);
    }

    public CraftingStation(float x, float y, CraftingUI craftingUI) {
        this.x = x;
        this.y = y;
        this.craftingUI = craftingUI;
        this.anvilTexture = createAnvilTexture();
        this.ownsTexture = true;
        this.hitbox = new Polygon(new float[]{0, 0, WIDTH, 0, WIDTH, HEIGHT, 0, HEIGHT});
        this.hitbox.setPosition(x - WIDTH / 2f, y - HEIGHT / 2f);

        float left = x - WIDTH / 2f;
        float bottom = y - HEIGHT / 2f;
        this.collisionPolygon = new Polygon(new float[]{
            left, bottom,
            left + WIDTH, bottom,
            left + WIDTH, bottom + HEIGHT,
            left, bottom + HEIGHT
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
        batch.setColor(Color.WHITE);
        batch.draw(anvilTexture, x - WIDTH / 2f, y - HEIGHT / 2f, WIDTH, HEIGHT);
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

    /** Simple dark anvil silhouette — placeholder until dedicated forge art exists. */
    static Texture createAnvilTexture() {
        int w = 28;
        int h = 22;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        // Base
        pixmap.setColor(0.25f, 0.25f, 0.28f, 1f);
        pixmap.fillRectangle(8, 0, 12, 6);
        // Stem
        pixmap.fillRectangle(11, 6, 6, 6);
        // Horn / top
        pixmap.setColor(0.4f, 0.4f, 0.45f, 1f);
        pixmap.fillRectangle(2, 12, 24, 8);
        // Highlight edge
        pixmap.setColor(0.55f, 0.55f, 0.6f, 1f);
        pixmap.fillRectangle(2, 18, 24, 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
