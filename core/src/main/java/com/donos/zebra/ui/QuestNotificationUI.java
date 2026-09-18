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
 * Short-lived quest event banner (does not block gameplay).
 */
public class QuestNotificationUI extends Table {

    public static final float DISPLAY_SECONDS = 2.8f;

    private final Label titleLabel;
    private final Label bodyLabel;
    private float remaining;

    public QuestNotificationUI(Skin skin) {
        setBackground(solid(new Color(0.07f, 0.1f, 0.14f, 0.9f)));
        pad(12, 18, 12, 18);
        titleLabel = new Label("", skin);
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(new Color(0.95f, 0.88f, 0.45f, 1f));
        bodyLabel = new Label("", skin);
        bodyLabel.setAlignment(Align.center);
        bodyLabel.setColor(Color.WHITE);
        add(titleLabel).growX().row();
        add(bodyLabel).padTop(4).growX();
        setVisible(false);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    public void show(String title, String body) {
        titleLabel.setText(title == null ? "" : title);
        bodyLabel.setText(body == null ? "" : body);
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
        } else if (remaining < 0.45f) {
            float a = remaining / 0.45f;
            setColor(1f, 1f, 1f, a);
        } else {
            setColor(1f, 1f, 1f, 1f);
        }
    }

    private static Drawable solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
