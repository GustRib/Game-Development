package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.OpeningQuest;

public class MentorNpc implements Entity, Interactable {

    private final float x, y;
    private final float radius = 26f;
    private boolean gavePickaxe = false;
    private final DialogueUI dialogueUI;

    private final java.util.Map<String, Animation<TextureRegion>[]> animations;
    private float stateTime = 0f;

    private final Polygon dummyHitbox;

    public MentorNpc(float x, float y, DialogueUI dialogueUI,
                       java.util.Map<String, Animation<TextureRegion>[]> animations) {
        this.x = x;
        this.y = y;
        this.dialogueUI = dialogueUI;
        this.animations = animations;

        this.dummyHitbox = new Polygon(new float[]{0, 0, 16, 0, 16, 16, 0, 16});
        this.dummyHitbox.setPosition(x - 8f, y);
    }

    public boolean hasGavePickaxe() {
        return gavePickaxe;
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion>[] idle = animations.get(AnimationConstants.ANIM_IDLE);
        if (idle != null && idle.length > 0 && idle[0] != null) {
            TextureRegion currentFrame = idle[0].getKeyFrame(stateTime, true);

            float targetHeight = 48f;
            float targetWidth = targetHeight;

            batch.draw(
                currentFrame,
                x - targetWidth / 2f,
                y,
                targetWidth,
                targetHeight
            );
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
        return dummyHitbox;
    }

    @Override
    public float getCurrentHealth() {
        return 100f;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onInteract(Player player) {
        if (!gavePickaxe) {
            dialogueUI.showText(
                "Mentor: A corrupcao ja toma a vila...\n"
                    + "Voce nao aguenta esses monstros desarmado.\n"
                    + "Tome esta picareta — traga "
                    + OpeningQuest.COPPER_ORE_REQUIRED
                    + " minerios de cobre do norte.\n"
                    + "Eu forjo sua primeira espada."
            );
            player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);
            gavePickaxe = true;
            return;
        }

        if (player.hasFirstSword()) {
            dialogueUI.showText(
                "Mentor: A vila precisa de voce.\n"
                    + "Nao deixe a corrupcao se espalhar!"
            );
            return;
        }

        int oreCount = player.getInventory().getItemCount(ItemRegistry.COPPER_ORE);
        if (oreCount < OpeningQuest.COPPER_ORE_REQUIRED) {
            dialogueUI.showText(
                "Mentor: Ainda falta cobre. A veia fica ao norte da vila.\n"
                    + "Volte com "
                    + OpeningQuest.COPPER_ORE_REQUIRED
                    + " minerios e eu forjo sua lamina.\n"
                    + "(Voce tem " + oreCount + "/"
                    + OpeningQuest.COPPER_ORE_REQUIRED + ")"
            );
            return;
        }

        boolean removed = player.getInventory().removeItem(
            ItemRegistry.COPPER_ORE, OpeningQuest.COPPER_ORE_REQUIRED);
        if (!removed) {
            dialogueUI.showText("Mentor: Hmm... algo deu errado com o cobre. Tente de novo.");
            return;
        }

        player.getInventory().addItem(ItemRegistry.IRON_SWORD, 1);
        player.grantFirstSword();
        dialogueUI.showText(
            "Mentor: Bom trabalho. Com este cobre...\n"
                + "Eis sua primeira espada. Agora enfrente esses invasores!"
        );
    }

    @Override
    public float getInteractionRadius() {
        return radius;
    }

    @Override
    public String getPromptText() {
        return "[E] Falar com o Mentor";
    }
}
