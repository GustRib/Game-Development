package com.donos.zebra.items;

/**
 * Active delayed craft: materials already consumed; result granted after {@link #DURATION_SECONDS}.
 */
public final class CraftingJob {

    public static final float DURATION_SECONDS = 5f;

    private final CraftingRecipe recipe;
    private final Inventory inventory;
    private float elapsed;
    private boolean complete;
    private boolean justCompleted;

    public CraftingJob(CraftingRecipe recipe, Inventory inventory) {
        this(recipe, inventory, 0f);
    }

    /** Restore an in-progress job (materials already consumed when originally started). */
    public CraftingJob(CraftingRecipe recipe, Inventory inventory, float elapsedSeconds) {
        this.recipe = recipe;
        this.inventory = inventory;
        this.elapsed = Math.max(0f, elapsedSeconds);
        if (this.elapsed >= DURATION_SECONDS) {
            this.elapsed = DURATION_SECONDS;
            inventory.addItem(recipe.getResult(), 1);
            this.complete = true;
            this.justCompleted = true;
        }
    }

    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public float getElapsed() {
        return elapsed;
    }

    public float getProgress() {
        return Math.min(1f, elapsed / DURATION_SECONDS);
    }

    public float getSecondsRemaining() {
        return Math.max(0f, DURATION_SECONDS - elapsed);
    }

    public boolean isComplete() {
        return complete;
    }

    /** True for one update cycle after completion. */
    public boolean consumeJustCompleted() {
        if (justCompleted) {
            justCompleted = false;
            return true;
        }
        return false;
    }

    public void update(float delta) {
        if (complete) {
            justCompleted = false;
            return;
        }
        elapsed += delta;
        if (elapsed >= DURATION_SECONDS) {
            elapsed = DURATION_SECONDS;
            inventory.addItem(recipe.getResult(), 1);
            complete = true;
            justCompleted = true;
        }
    }
}
