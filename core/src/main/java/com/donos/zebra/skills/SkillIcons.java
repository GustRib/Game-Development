package com.donos.zebra.skills;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Resolves {@link SkillDefinition#getIconPath()} through the shared AssetManager.
 */
public final class SkillIcons {

    private SkillIcons() {
    }

    public static Drawable drawable(AssetManager assetManager, SkillDefinition def) {
        if (def == null) {
            return fallback(Color.GRAY);
        }
        return drawable(assetManager, def.getIconPath(), iconTint(def.getId()));
    }

    public static Drawable drawable(AssetManager assetManager, String iconPath, Color fallbackTint) {
        if (assetManager != null && iconPath != null && !iconPath.isEmpty()
            && assetManager.isLoaded(iconPath, Texture.class)) {
            Texture texture = assetManager.get(iconPath, Texture.class);
            TextureRegionDrawable d = new TextureRegionDrawable(texture);
            d.setMinWidth(48f);
            d.setMinHeight(48f);
            return d;
        }
        return fallback(fallbackTint != null ? fallbackTint : Color.WHITE);
    }

    public static Image image(AssetManager assetManager, SkillDefinition def, float size) {
        Image image = new Image(drawable(assetManager, def));
        image.setSize(size, size);
        return image;
    }

    public static Color iconTint(String skillId) {
        if (SkillRegistry.WHIRLWIND_ID.equals(skillId)) {
            return new Color(0.65f, 0.85f, 1f, 1f);
        }
        if (SkillRegistry.FLAME_STRIKE_ID.equals(skillId)) {
            return new Color(1f, 0.55f, 0.2f, 1f);
        }
        return Color.WHITE;
    }

    private static Drawable fallback(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable d = new TextureRegionDrawable(texture);
        d.setMinWidth(48f);
        d.setMinHeight(48f);
        return d;
    }
}
