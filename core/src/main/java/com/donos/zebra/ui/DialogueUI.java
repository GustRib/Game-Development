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

public class DialogueUI extends Table {

    private final Label textLabel;

    public DialogueUI(Skin skin) {
        Drawable background = createColorDrawable(new Color(0.08f, 0.08f, 0.08f, 0.85f));
        this.setBackground(background);
        this.pad(15);
        this.align(Align.topLeft);

        this.textLabel = new Label("", skin);
        this.textLabel.setWrap(true);
        this.add(textLabel).expandX().fillX().align(Align.topLeft).row();

        Label nextLabel = new Label("[ESPAÇO] FECHAR", skin);
        nextLabel.setColor(Color.LIGHT_GRAY);
        nextLabel.setFontScale(0.6f);
        this.add(nextLabel).expandX().align(Align.bottomRight).padTop(10);

        this.setVisible(false);
    }

    public void showText(String text) {
        this.textLabel.setText(text);
        this.setVisible(true);
    }

    public void hideDialogue() {
        this.setVisible(false);
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