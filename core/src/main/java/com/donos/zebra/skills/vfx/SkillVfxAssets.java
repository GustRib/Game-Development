package com.donos.zebra.skills.vfx;

/**
 * Shared VFX asset paths for skill effects.
 */
public final class SkillVfxAssets {

    /** Horizontal slash strip (copied from Berserker Attack03 effect, 9×100px frames). */
    public static final String FLAME_SLASH_SHEET = "vfx/flame_slash_sheet.png";
    public static final int FLAME_SLASH_FRAME_SIZE = 100;
    public static final int FLAME_SLASH_FRAME_COUNT = 9;

    private SkillVfxAssets() {
    }

    public static void queue(com.badlogic.gdx.assets.AssetManager assetManager) {
        if (assetManager == null) {
            return;
        }
        assetManager.load(FLAME_SLASH_SHEET, com.badlogic.gdx.graphics.Texture.class);
    }
}
