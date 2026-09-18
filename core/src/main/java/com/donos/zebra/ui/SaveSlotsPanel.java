package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.save.SaveService;
import com.donos.zebra.save.SaveSlotInfo;

/**
 * Three save-slot picker used by Continue (load) and pause Save Game.
 */
public class SaveSlotsPanel extends Table {

    public enum Mode {
        LOAD,
        SAVE
    }

    public interface Listener {
        void onSlotChosen(int slotIndex);

        void onBack();
    }

    private final Skin skin;
    private final Mode mode;
    private final SaveService saveService;
    private Listener listener;
    private Label statusLabel;

    public SaveSlotsPanel(Skin skin, Mode mode, SaveService saveService) {
        this.skin = skin;
        this.mode = mode;
        this.saveService = saveService != null ? saveService : new SaveService();
        ensureStyles(skin);
        setBackground(solid(new Color(0.08f, 0.08f, 0.12f, 0.92f)));
        pad(20);
        align(Align.center);
        rebuild();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setStatusMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message != null ? message : "");
        }
    }

    public void refresh() {
        rebuild();
    }

    private void rebuild() {
        clearChildren();
        String titleText = mode == Mode.LOAD ? "CONTINUAR" : "SALVAR JOGO";
        Label title = new Label(titleText, skin);
        title.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        title.setAlignment(Align.center);
        add(title).padBottom(14).growX().row();

        SaveSlotInfo[] infos = saveService.getAllSlotInfos();
        for (SaveSlotInfo info : infos) {
            add(buildSlotRow(info)).width(360).height(56).padBottom(8).row();
        }

        statusLabel = new Label("", skin);
        statusLabel.setColor(Color.LIGHT_GRAY);
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(true);
        add(statusLabel).width(360).padTop(4).padBottom(8).row();

        TextButton back = new TextButton("Voltar", skin, "pause-menu");
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) {
                    listener.onBack();
                }
            }
        });
        add(back).width(220).height(36).row();
    }

    private Table buildSlotRow(SaveSlotInfo info) {
        Table row = new Table();
        boolean occupied = info.occupied;
        Color bg = occupied
            ? new Color(0.22f, 0.28f, 0.22f, 1f)
            : new Color(0.18f, 0.18f, 0.22f, 1f);
        row.setBackground(solid(bg));
        row.pad(8, 12, 8, 12);

        Label slotLabel = new Label("Slot " + info.slotIndex, skin);
        slotLabel.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        row.add(slotLabel).left().width(70);

        Label detail = new Label(info.summaryLine(), skin);
        detail.setColor(occupied ? Color.WHITE : Color.GRAY);
        detail.setWrap(true);
        row.add(detail).growX().padLeft(8);

        String action = mode == Mode.LOAD
            ? (occupied ? "Carregar" : "—")
            : (occupied ? "Sobrescrever" : "Salvar");
        TextButton btn = new TextButton(action, skin, "pause-menu");
        boolean enabled = mode == Mode.SAVE || occupied;
        btn.setDisabled(!enabled);
        btn.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        if (enabled) {
            final int slot = info.slotIndex;
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (listener != null) {
                        listener.onSlotChosen(slot);
                    }
                }
            });
        }
        row.add(btn).width(110).height(32).padLeft(8);
        return row;
    }

    private static void ensureStyles(Skin skin) {
        if (!skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.disabledFontColor = Color.GRAY;
            style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
            style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
            style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
            style.checked = solid(new Color(0.4f, 0.35f, 0.18f, 1f));
            style.disabled = solid(new Color(0.15f, 0.15f, 0.18f, 1f));
            skin.add("pause-menu", style);
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
