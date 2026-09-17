package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.OpeningQuest;

/**
 * Interactable copper vein. Depletes after {@link OpeningQuest#ORE_NODE_HITS} hits,
 * then respawns after {@link #RESPAWN_SECONDS} of overworld time.
 */
public class OreNode implements Entity, Interactable {

    public static final float RESPAWN_SECONDS = 90f;

    private static final float SIZE = 16f;
    private static final float RADIUS = 22f;

    private final float x;
    private final float y;
    private final Texture texture;
    private final DialogueUI dialogueUI;
    private final Polygon hitbox;

    private int hitsRemaining = OpeningQuest.ORE_NODE_HITS;
    private float depletedTimer;
    private boolean interactionTargeted;
    private float pulseTime;
    private BitmapFont progressFont;

    public OreNode(float x, float y, Texture texture, DialogueUI dialogueUI) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.dialogueUI = dialogueUI;
        this.hitbox = new Polygon(new float[]{0, 0, SIZE, 0, SIZE, SIZE, 0, SIZE});
        this.hitbox.setPosition(x - SIZE / 2f, y - SIZE / 2f);
    }

    public void setProgressFont(BitmapFont progressFont) {
        this.progressFont = progressFont;
    }

    public void setInteractionTargeted(boolean targeted) {
        this.interactionTargeted = targeted;
    }

    public boolean isInteractionTargeted() {
        return interactionTargeted;
    }

    public int getHitsRemaining() {
        return hitsRemaining;
    }

    public int getHitsCompleted() {
        return OpeningQuest.ORE_NODE_HITS - hitsRemaining;
    }

    public String getProgressLabel() {
        return getHitsCompleted() + "/" + OpeningQuest.ORE_NODE_HITS;
    }

    public boolean isDepleted() {
        return hitsRemaining <= 0;
    }

    public float getDepletedTimer() {
        return depletedTimer;
    }

    @Override
    public void onInteract(Player player) {
        if (isDepleted()) {
            if (dialogueUI != null) {
                dialogueUI.showText("Este veio de cobre ja foi esgotado.");
            }
            return;
        }

        if (!player.getInventory().hasItemQuantity(ItemRegistry.STONE_PICKAXE, 1)) {
            if (dialogueUI != null) {
                dialogueUI.showText("Voce precisa de uma picareta para minerar isto.");
            }
            return;
        }

        hitsRemaining--;
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 1);
        if (isDepleted()) {
            depletedTimer = 0f;
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
        if (isDepleted()) {
            depletedTimer += delta;
            if (depletedTimer >= RESPAWN_SECONDS) {
                respawn();
            }
        }
        if (interactionTargeted && !isDepleted()) {
            pulseTime += delta;
        } else if (!interactionTargeted) {
            pulseTime = 0f;
        }
    }

    void respawn() {
        hitsRemaining = OpeningQuest.ORE_NODE_HITS;
        depletedTimer = 0f;
        pulseTime = 0f;
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
        } else if (interactionTargeted) {
            float pulse = 0.75f + 0.25f * (float) Math.sin(pulseTime * 8f);
            batch.setColor(1f, 0.95f * pulse, 0.35f, 1f);
        } else if (hitsRemaining == 1) {
            batch.setColor(0.95f, 0.6f, 0.35f, 1f);
        } else if (hitsRemaining == 2) {
            batch.setColor(1f, 0.72f, 0.4f, 1f);
        } else {
            batch.setColor(1f, 0.75f, 0.45f, 1f);
        }

        batch.draw(texture, drawX, drawY, SIZE, SIZE);
        batch.setColor(Color.WHITE);

        if (interactionTargeted && !isDepleted() && progressFont != null) {
            progressFont.setColor(Color.WHITE);
            progressFont.draw(batch, getProgressLabel(), x - 8f, y + SIZE / 2f + 14f);
        }
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
