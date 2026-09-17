package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

/**
 * Screen-fixed equip/unequip toast (not world floating damage text).
 * New messages replace the current one (no queue).
 */
public class StatusToast extends Table {

    public static final float DISPLAY_SECONDS = 2.2f;

    private final Label label;
    private float remaining;

    public StatusToast(Skin skin) {
        setBackground(createColorDrawable(new Color(0.08f, 0.1f, 0.12f, 0.88f)));
        pad(10, 16, 10, 16);
        label = new Label("", skin);
        label.setAlignment(Align.center);
        label.setColor(new Color(0.95f, 0.9f, 0.55f, 1f));
        add(label);
        setVisible(false);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    public void show(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        label.setText(message);
        remaining = DISPLAY_SECONDS;
        setVisible(true);
        pack();
        toFront();
    }

    public void update(float delta) {
        if (!isVisible()) {
            return;
        }
        remaining -= delta;
        if (remaining <= 0f) {
            setVisible(false);
        } else if (remaining < 0.4f) {
            float a = remaining / 0.4f;
            setColor(1f, 1f, 1f, a);
            label.getColor().a = a;
        } else {
            setColor(1f, 1f, 1f, 1f);
            label.getColor().a = 1f;
        }
    }

    private static Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
