package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.OpeningQuest;

/**
 * Interactable copper vein. Three strikes with a pickaxe yield three ore, then the node depletes.
 * Rock-sized (one tile) with a mild copper tint so it stays readable without dominating the scene.
 */
public class OreNode implements Entity, Interactable {

    /** Matches a single 16px map tile — rock/decoration scale, not tree/bush scale. */
    private static final float SIZE = 16f;
    private static final float RADIUS = 22f;

    private final float x;
    private final float y;
    private final Texture texture;
    private final DialogueUI dialogueUI;
    private final Polygon hitbox;

    private int hitsRemaining = OpeningQuest.ORE_NODE_HITS;

    public OreNode(float x, float y, Texture texture, DialogueUI dialogueUI) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.dialogueUI = dialogueUI;
        this.hitbox = new Polygon(new float[]{0, 0, SIZE, 0, SIZE, SIZE, 0, SIZE});
        this.hitbox.setPosition(x - SIZE / 2f, y - SIZE / 2f);
    }

    public int getHitsRemaining() {
        return hitsRemaining;
    }

    public boolean isDepleted() {
        return hitsRemaining <= 0;
    }

    @Override
    public void onInteract(Player player) {
        if (isDepleted()) {
            dialogueUI.showText("Este veio de cobre ja foi esgotado.");
            return;
        }

        if (!player.getInventory().hasItemQuantity(ItemRegistry.STONE_PICKAXE, 1)) {
            dialogueUI.showText("Voce precisa de uma picareta para minerar isto.");
            return;
        }

        hitsRemaining--;
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 1);

        if (isDepleted()) {
            dialogueUI.showText("O veio se esgota. Voce arranca o ultimo pedaco de cobre.");
        } else {
            dialogueUI.showText("Clang! Voce extrai cobre. ("
                + (OpeningQuest.ORE_NODE_HITS - hitsRemaining) + "/"
                + OpeningQuest.ORE_NODE_HITS + ")");
        }
    }

    @Override
    public float getInteractionRadius() {
        return RADIUS;
    }

    @Override
    public String getPromptText() {
        return isDepleted() ? "[Esgotado]" : "[E] Minerar cobre";
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture == null) {
            return;
        }

        float drawX = x - SIZE / 2f;
        float drawY = y - SIZE / 2f;

        if (isDepleted()) {
            batch.setColor(0.4f, 0.4f, 0.4f, 0.9f);
        } else if (hitsRemaining == 1) {
            batch.setColor(0.95f, 0.6f, 0.35f, 1f);
        } else if (hitsRemaining == 2) {
            batch.setColor(1f, 0.72f, 0.4f, 1f);
        } else {
            // Mild copper tint — distinct from grey rocks, not a blown-out orange orb.
            batch.setColor(1f, 0.75f, 0.45f, 1f);
        }

        batch.draw(texture, drawX, drawY, SIZE, SIZE);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        // Texture owned by AssetManager
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
