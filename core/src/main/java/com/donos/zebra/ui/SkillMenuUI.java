package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.skills.SkillBook;
import com.donos.zebra.skills.SkillDefinition;
import com.donos.zebra.skills.SkillIcons;
import com.donos.zebra.skills.SkillRuntime;

/**
 * Spacious Skill Menu (K). Drag entries onto the Skill Bar to assign.
 */
public class SkillMenuUI extends GameWindow {

    private static final float ICON_SIZE = 56f;
    private static final float DESC_WIDTH = 380f;

    private final SkillBook skillBook;
    private final Skin skin;
    private final AssetManager assetManager;
    private final Table listTable;
    private final DragAndDrop dragAndDrop = new DragAndDrop();
    private SkillTooltipPanel tooltipPanel;
    private SkillBarUI skillBarUI;

    public SkillMenuUI(SkillBook skillBook, Skin skin, AssetManager assetManager) {
        super("Habilidades", skin);
        this.skillBook = skillBook;
        this.skin = skin;
        this.assetManager = assetManager;

        setSize(520, 420);
        setMovable(true);

        listTable = new Table();
        listTable.top().left().pad(8);

        ScrollPane scroll = new ScrollPane(listTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        add(scroll).grow().pad(10, 14, 6, 14).row();

        Label hint = new Label("Arraste para a barra [1-4]   ·   [K] fechar", skin);
        hint.setColor(Color.LIGHT_GRAY);
        hint.setAlignment(Align.center);
        add(hint).padBottom(12).padTop(4).growX();

        rebuild();
    }

    public void setTooltipPanel(SkillTooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
        if (tooltipPanel != null) {
            tooltipPanel.setAssetManager(assetManager);
        }
    }

    public void bindSkillBar(SkillBarUI skillBarUI) {
        this.skillBarUI = skillBarUI;
        rebuild();
    }

    public void rebuild() {
        listTable.clearChildren();
        dragAndDrop.clear();
        boolean first = true;
        for (SkillRuntime runtime : skillBook.allRuntimes()) {
            if (!runtime.isUnlocked()) {
                continue;
            }
            if (!first) {
                Table separator = new Table();
                separator.setBackground(solid(new Color(0.3f, 0.3f, 0.36f, 0.7f)));
                listTable.add(separator).height(1f).growX().padTop(10).padBottom(10).row();
            }
            first = false;
            SkillDefinition def = runtime.getDefinition();
            Table row = buildSkillRow(def);
            listTable.add(row).growX().padBottom(8).row();
            wireMenuSource(row, def);
        }
        if (skillBarUI != null) {
            wireBarTargets();
            wireBarSources();
        }
    }

    private Table buildSkillRow(SkillDefinition def) {
        Table row = new Table();
        row.setBackground(solid(new Color(0.14f, 0.14f, 0.2f, 0.95f)));
        row.pad(16, 18, 16, 18);

        Image icon = SkillIcons.image(assetManager, def, ICON_SIZE);
        row.add(icon).size(ICON_SIZE).padRight(18).top();

        Table text = new Table();
        text.left();

        Label name = new Label(def.getName(), skin);
        name.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        text.add(name).left().padBottom(8).row();

        Label desc = new Label(def.getDescription(), skin);
        desc.setWrap(true);
        desc.setColor(Color.LIGHT_GRAY);
        text.add(desc).width(DESC_WIDTH).left().padBottom(10).row();

        if (def.getEffectSummary() != null && !def.getEffectSummary().isEmpty()) {
            Label effect = new Label(def.getEffectSummary(), skin);
            effect.setWrap(true);
            effect.setColor(new Color(0.75f, 0.8f, 0.9f, 1f));
            text.add(effect).width(DESC_WIDTH).left().padBottom(8).row();
        }

        Label cd = new Label("Recarga: " + SkillDefinition.formatCooldown(def.getCooldownSeconds()), skin);
        cd.setColor(new Color(0.85f, 0.78f, 0.45f, 1f));
        text.add(cd).left();

        row.add(text).growX().top().left();

        row.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (tooltipPanel != null) {
                    tooltipPanel.showFor(def, row);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (tooltipPanel != null) {
                    tooltipPanel.hide();
                }
            }
        });
        return row;
    }

    private void wireMenuSource(final Table row, final SkillDefinition def) {
        dragAndDrop.addSource(new Source(row) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                Payload payload = new Payload();
                payload.setObject(def.getId());
                Image drag = SkillIcons.image(assetManager, def, 40f);
                payload.setDragActor(drag);
                return payload;
            }
        });
    }

    private void wireBarTargets() {
        for (final SkillBarSlotActor slot : skillBarUI.getSlots()) {
            dragAndDrop.addTarget(new Target(slot) {
                @Override
                public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                    return payload != null && payload.getObject() instanceof String;
                }

                @Override
                public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    if (payload == null || !(payload.getObject() instanceof String)) {
                        return;
                    }
                    String skillId = (String) payload.getObject();
                    if (source.getActor() instanceof SkillBarSlotActor) {
                        SkillBarSlotActor from = (SkillBarSlotActor) source.getActor();
                        if (from.getSlotIndex() != slot.getSlotIndex()) {
                            skillBook.getBar().swapSlots(from.getSlotIndex(), slot.getSlotIndex());
                        }
                    } else {
                        skillBook.getBar().setSlot(slot.getSlotIndex(), skillId);
                    }
                    skillBarUI.refresh();
                    if (tooltipPanel != null) {
                        tooltipPanel.hide();
                    }
                }
            });
        }
    }

    private void wireBarSources() {
        for (final SkillBarSlotActor slot : skillBarUI.getSlots()) {
            dragAndDrop.addSource(new Source(slot) {
                @Override
                public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    String id = skillBook.getBar().getSlotId(slot.getSlotIndex());
                    if (id == null) {
                        return null;
                    }
                    SkillDefinition def = skillBook.getBar().getSlotDefinition(slot.getSlotIndex());
                    Payload payload = new Payload();
                    payload.setObject(id);
                    Image drag = SkillIcons.image(assetManager, def, 40f);
                    payload.setDragActor(drag);
                    return payload;
                }
            });
        }
    }

    public void open() {
        rebuild();
        openPanel();
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
