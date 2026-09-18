package com.donos.zebra.skills.vfx;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Player;

/**
 * Factory that builds skill visuals from loaded assets + procedural soft textures.
 * Separates rendering construction from skill gameplay behaviors.
 */
public final class SkillVfxFactory {

    private final AssetManager assetManager;
    private Texture softGlow;
    private Texture flameBlob;
    private Texture windStreak;
    private Texture windRibbon;
    private Texture windArc;
    private Animation<TextureRegion> flameSlashAnim;
    private boolean initialized;

    public SkillVfxFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void ensureLoaded() {
        if (initialized) {
            return;
        }
        softGlow = SoftTextureFactory.softCircle(64);
        flameBlob = SoftTextureFactory.softFlameBlob(32);
        windStreak = SoftTextureFactory.softStreak(96, 16);
        windRibbon = SoftTextureFactory.softWindRibbon(128, 28);
        windArc = SoftTextureFactory.softWindArc(96, -40f, 150f);
        if (assetManager != null && assetManager.isLoaded(SkillVfxAssets.FLAME_SLASH_SHEET, Texture.class)) {
            Texture sheet = assetManager.get(SkillVfxAssets.FLAME_SLASH_SHEET, Texture.class);
            flameSlashAnim = FlameStrikeSlashEffect.buildSlashAnimation(sheet);
        }
        initialized = true;
    }

    public WhirlwindRingEffect createWhirlwind(Player player, float radius) {
        ensureLoaded();
        return new WhirlwindRingEffect(
            player, radius, softGlow, windRibbon, windArc, windStreak, softGlow, flameSlashAnim);
    }

    public FlameStrikeSlashEffect createFlameStrike(Player player, float facingDegrees) {
        ensureLoaded();
        return new FlameStrikeSlashEffect(player, facingDegrees, flameSlashAnim, softGlow, flameBlob);
    }

    public BurningAuraEffect createBurningAura(Enemy enemy) {
        ensureLoaded();
        return new BurningAuraEffect(enemy, softGlow, flameBlob);
    }

    public void dispose() {
        if (softGlow != null) {
            softGlow.dispose();
            softGlow = null;
        }
        if (flameBlob != null) {
            flameBlob.dispose();
            flameBlob = null;
        }
        if (windStreak != null) {
            windStreak.dispose();
            windStreak = null;
        }
        if (windRibbon != null) {
            windRibbon.dispose();
            windRibbon = null;
        }
        if (windArc != null) {
            windArc.dispose();
            windArc = null;
        }
        initialized = false;
    }
}
