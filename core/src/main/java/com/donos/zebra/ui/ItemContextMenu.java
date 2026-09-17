package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Lightweight 2-option right-click menu for inventory potions.
 */
public final class ItemContextMenu extends Table {

    public interface Listener {
        void onUse();

        void onMoveToCharacter();
    }

    private final Skin skin;

    public ItemContextMenu(Skin skin) {
        this.skin = skin;
        setBackground(solid(new Color(0.12f, 0.12f, 0.16f, 0.96f)));
        pad(4);
        setVisible(false);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    public void show(Stage stage, float stageX, float stageY, Listener listener) {
        clearChildren();
        add(option("Usar", () -> {
            hide();
            if (listener != null) {
                listener.onUse();
            }
        })).growX().height(28).pad(2).row();
        add(option("Mover para Personagem", () -> {
            hide();
            if (listener != null) {
                listener.onMoveToCharacter();
            }
        })).growX().height(28).pad(2).row();
        pack();
        setPosition(stageX, stageY - getHeight());
        setVisible(true);
        toFront();
        if (getStage() == null && stage != null) {
            stage.addActor(this);
        }
    }

    public void hide() {
        setVisible(false);
    }

    private TextButton option(String text, Runnable action) {
        ensureStyle(skin);
        TextButton btn = new TextButton(text, skin, "context-option");
        btn.getLabel().setFontScale(0.85f);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return btn;
    }

    private static void ensureStyle(Skin skin) {
        if (skin.has("context-option", TextButton.TextButtonStyle.class)) {
            return;
        }
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = skin.getFont("default");
        style.fontColor = Color.WHITE;
        style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
        style.over = solid(new Color(0.32f, 0.32f, 0.4f, 1f));
        style.down = solid(new Color(0.16f, 0.16f, 0.2f, 1f));
        skin.add("context-option", style);
    }

    private static Drawable solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    /** Hide when clicking outside (call from stage click / parent). */
    public static void hideIfOutside(ItemContextMenu menu, Actor hit) {
        if (menu == null || !menu.isVisible()) {
            return;
        }
        if (hit == null || (hit != menu && !hit.isDescendantOf(menu))) {
            menu.hide();
        }
    }
}
