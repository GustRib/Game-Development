package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.skills.SkillBar;
import com.donos.zebra.skills.SkillBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Always-visible four-slot skill hotbar HUD (not part of {@link UiPanelStack}).
 */
public class SkillBarUI extends Table {

    private final SkillBook skillBook;
    private final List<SkillBarSlotActor> slots = new ArrayList<>();
    private SkillTooltipPanel tooltipPanel;

    public SkillBarUI(SkillBook skillBook, Skin skin, AssetManager assetManager) {
        this.skillBook = skillBook;
        setBackground(solid(new Color(0.08f, 0.08f, 0.12f, 0.75f)));
        pad(6);
        defaults().pad(3);

        for (int i = 0; i < SkillBar.SLOT_COUNT; i++) {
            SkillBarSlotActor slot = new SkillBarSlotActor(i, skillBook, skin, assetManager);
            slots.add(slot);
            add(slot).size(52, 52);
        }
        pack();
    }

    public void setTooltipPanel(SkillTooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
        for (SkillBarSlotActor slot : slots) {
            slot.setTooltipPanel(tooltipPanel);
        }
    }

    public List<SkillBarSlotActor> getSlots() {
        return slots;
    }

    public SkillBook getSkillBook() {
        return skillBook;
    }

    public void refresh() {
        for (SkillBarSlotActor slot : slots) {
            slot.refresh();
        }
    }

    public void placeBottomCenter(float stageWidth, float stageHeight) {
        pack();
        setPosition((stageWidth - getWidth()) / 2f, 18f, Align.bottomLeft);
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
