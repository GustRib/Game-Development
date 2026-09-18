package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.donos.zebra.skills.SkillDefinition;
import com.donos.zebra.skills.SkillIcons;

/**
 * Tooltip for skills (Skill Menu + Skill Bar), parallel to {@link ItemTooltipPanel}.
 */
public class SkillTooltipPanel extends Table {

    private static final float BODY_WIDTH = 280f;

    private AssetManager assetManager;
    private final Image iconImage;
    private final Label nameLabel;
    private final Label bodyLabel;

    public SkillTooltipPanel(Skin skin) {
        setBackground(solid(new Color(0.05f, 0.05f, 0.08f, 0.95f)));
        pad(10);
        setVisible(false);

        iconImage = new Image();
        nameLabel = new Label("", skin);
        nameLabel.setColor(new Color(1f, 0.9f, 0.55f, 1f));

        Table header = new Table();
        header.add(iconImage).size(28).padRight(8);
        header.add(nameLabel).left();
        add(header).left().row();

        bodyLabel = new Label("", skin);
        bodyLabel.setWrap(true);
        bodyLabel.setColor(Color.LIGHT_GRAY);
        add(bodyLabel).width(BODY_WIDTH).left().padTop(6);
        pack();
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void showFor(SkillDefinition skill, Actor anchor) {
        if (skill == null || anchor == null) {
            hide();
            return;
        }
        iconImage.setDrawable(SkillIcons.drawable(assetManager, skill));
        nameLabel.setText(skill.getName());
        bodyLabel.setText(skill.buildTooltipBody());
        pack();
        if (anchor.getStage() != null) {
            com.badlogic.gdx.math.Vector2 pos = anchor.localToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(0, anchor.getHeight() + 4f));
            float stageW = anchor.getStage().getWidth();
            float stageH = anchor.getStage().getHeight();
            float x = Math.min(pos.x, stageW - getWidth() - 8f);
            float y = Math.min(pos.y, stageH - getHeight() - 8f);
            setPosition(Math.max(8f, x), Math.max(8f, y));
        }
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
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
