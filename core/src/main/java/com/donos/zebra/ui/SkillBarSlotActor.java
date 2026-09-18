package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.skills.SkillBook;
import com.donos.zebra.skills.SkillDefinition;
import com.donos.zebra.skills.SkillIcons;
import com.donos.zebra.skills.SkillRuntime;

/**
 * One Skill Bar hotkey slot (1–4).
 */
public class SkillBarSlotActor extends Button {

    private final int slotIndex;
    private final SkillBook skillBook;
    private final AssetManager assetManager;
    private final Label keyLabel;
    private final Image iconImage;
    private final Label cooldownLabel;
    private SkillTooltipPanel tooltipPanel;
    private final Drawable emptyBg;
    private final Drawable filledBg;
    private final Drawable cooldownOverlay;

    public SkillBarSlotActor(int slotIndex, SkillBook skillBook, Skin skin, AssetManager assetManager) {
        super(new Button.ButtonStyle(skin.get("slot-style", Button.ButtonStyle.class)));
        this.slotIndex = slotIndex;
        this.skillBook = skillBook;
        this.assetManager = assetManager;
        setSize(52, 52);

        emptyBg = solid(new Color(0.12f, 0.12f, 0.16f, 0.9f));
        filledBg = solid(new Color(0.18f, 0.2f, 0.28f, 0.95f));
        cooldownOverlay = solid(new Color(0f, 0f, 0f, 0.55f));

        keyLabel = new Label(String.valueOf(slotIndex + 1), skin);
        keyLabel.setFontScale(0.75f);
        keyLabel.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        keyLabel.setAlignment(Align.topLeft);
        keyLabel.setTouchable(Touchable.disabled);

        iconImage = new Image();
        iconImage.setTouchable(Touchable.disabled);

        cooldownLabel = new Label("", skin);
        cooldownLabel.setFontScale(0.7f);
        cooldownLabel.setAlignment(Align.center);
        cooldownLabel.setColor(Color.WHITE);
        cooldownLabel.setTouchable(Touchable.disabled);

        addActor(iconImage);
        addActor(keyLabel);
        addActor(cooldownLabel);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (tooltipPanel != null) {
                    SkillDefinition def = skillBook.getBar().getSlotDefinition(slotIndex);
                    if (def != null) {
                        tooltipPanel.showFor(def, SkillBarSlotActor.this);
                    }
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (tooltipPanel != null) {
                    tooltipPanel.hide();
                }
            }
        });
        refresh();
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setTooltipPanel(SkillTooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
    }

    public void refresh() {
        SkillDefinition def = skillBook.getBar().getSlotDefinition(slotIndex);
        if (def == null) {
            iconImage.setDrawable(null);
            iconImage.setVisible(false);
            getStyle().up = emptyBg;
        } else {
            iconImage.setDrawable(SkillIcons.drawable(assetManager, def));
            iconImage.setVisible(true);
            getStyle().up = filledBg;
        }
        layoutLabels();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        SkillRuntime runtime = skillBook.getRuntimeForSlot(slotIndex);
        if (runtime != null && runtime.getCooldownRemaining() > 0f) {
            float cd = runtime.getCooldownRemaining();
            cooldownLabel.setText(cd >= 1f
                ? String.valueOf((int) Math.ceil(cd))
                : String.format(java.util.Locale.ROOT, "%.1f", cd));
            cooldownLabel.setVisible(true);
        } else {
            cooldownLabel.setText("");
            cooldownLabel.setVisible(false);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        SkillRuntime runtime = skillBook.getRuntimeForSlot(slotIndex);
        if (runtime != null && runtime.getCooldownRemaining() > 0f && cooldownOverlay != null) {
            Color c = batch.getColor();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            cooldownOverlay.draw(batch, getX(), getY(), getWidth(), getHeight());
            batch.setColor(c);
        }
    }

    @Override
    public void layout() {
        super.layout();
        layoutLabels();
    }

    private void layoutLabels() {
        keyLabel.setBounds(3, getHeight() - 16, 16, 14);
        float pad = 6f;
        iconImage.setBounds(pad, pad, getWidth() - pad * 2f, getHeight() - pad * 2f);
        cooldownLabel.setBounds(0, 0, getWidth(), getHeight());
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
