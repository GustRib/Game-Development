package com.donos.zebra.items;

/**
 * Owns at most one in-progress craft. Survives CraftingUI open/close.
 */
public final class CraftingController {

    private CraftingJob activeJob;

    public boolean isBusy() {
        return activeJob != null && !activeJob.isComplete();
    }

    public CraftingJob getActiveJob() {
        return activeJob;
    }

    /**
     * Consumes materials immediately and starts the forge timer.
     * @return false if already busy or materials insufficient
     */
    public boolean tryStart(CraftingRecipe recipe, Inventory inventory) {
        if (activeJob != null && activeJob.isComplete()) {
            activeJob = null;
        }
        if (isBusy()) {
            return false;
        }
        if (!recipe.consumeMaterials(inventory)) {
            return false;
        }
        activeJob = new CraftingJob(recipe, inventory);
        return true;
    }

    public void update(float delta) {
        if (activeJob == null || activeJob.isComplete()) {
            return;
        }
        activeJob.update(delta);
    }

    public boolean hasCompletedJob() {
        return activeJob != null && activeJob.isComplete();
    }

    public void clearCompleted() {
        if (activeJob != null && activeJob.isComplete()) {
            activeJob = null;
        }
    }

    /** Rebuilds an in-progress craft from save data (does not re-consume materials). */
    public void restoreJob(CraftingRecipe recipe, Inventory inventory, float elapsedSeconds) {
        if (recipe == null || inventory == null) {
            return;
        }
        activeJob = new CraftingJob(recipe, inventory, elapsedSeconds);
        if (activeJob.isComplete()) {
            activeJob = null;
        }
    }
}
