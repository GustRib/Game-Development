package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.items.ItemDefinition;

/**
 * Floating tooltip shown when hovering inventory / equipment slots.
 */
public class ItemTooltipPanel extends Table {

    private final Label nameLabel;
    private final Label statsLabel;

    public ItemTooltipPanel(Skin skin) {
        setBackground(createColorDrawable(new Color(0.05f, 0.05f, 0.08f, 0.95f)));
        pad(8);
        setVisible(false);

        nameLabel = new Label("", skin);
        nameLabel.setColor(new Color(1f, 0.9f, 0.55f, 1f));
        add(nameLabel).left().row();

        statsLabel = new Label("", skin);
        statsLabel.setWrap(true);
        statsLabel.setColor(Color.LIGHT_GRAY);
        add(statsLabel).width(220).left().padTop(4);
        pack();
    }

    public void showFor(ItemDefinition item, Actor anchor) {
        if (item == null || anchor == null) {
            hide();
            return;
        }
        nameLabel.setText(item.getName());
        statsLabel.setText(buildStats(item));
        pack();

        float stageX = anchor.getX();
        float stageY = anchor.getY() + anchor.getHeight() + 4f;
        Actor parent = anchor.getParent();
        while (parent != null && parent.getParent() != null) {
            stageX += parent.getX();
            stageY += parent.getY();
            parent = parent.getParent();
        }
        // Prefer local coordinates relative to stage root
        if (anchor.getStage() != null) {
            com.badlogic.gdx.math.Vector2 pos = anchor.localToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(0, anchor.getHeight() + 4f));
            setPosition(pos.x, pos.y);
        } else {
            setPosition(stageX, stageY);
        }
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
    }

    private static String buildStats(ItemDefinition item) {
        StringBuilder sb = new StringBuilder();
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            sb.append(item.getDescription());
        }
        if (item.getAttackDamage() > 0) {
            if (sb.length() > 0) sb.append('\n');
            sb.append("Dano: ").append(item.getAttackDamage());
        }
        if (item.getDefense() > 0) {
            if (sb.length() > 0) sb.append('\n');
            sb.append("Defesa: ").append(item.getDefense());
        }
        if (sb.length() == 0) {
            sb.append(item.getType().getDisplayName());
        }
        return sb.toString();
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
