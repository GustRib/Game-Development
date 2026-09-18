package com.donos.zebra.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.audio.MusicSettings;

/**
 * Scene2D pause + options overlay (UI time only; does not drive gameplay).
 */
public class PauseMenuUI extends Table implements UiPanelStack.Panel {

    public interface Listener {
        void onResume();

        void onSaveGame();

        void onRestart();

        void onExit();

        void onOpenOptions();

        void onBackFromOptions();
    }

    private final Skin skin;
    private final Table pauseRoot;
    private final Table optionsRoot;
    private final Label volumeValueLabel;
    private final Slider volumeSlider;
    private Listener listener;
    private boolean showingOptions;

    public PauseMenuUI(Skin skin) {
        this.skin = skin;
        ensureStyles(skin);
        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.enabled);
        align(Align.center);

        // Dim full-screen hit target (visual dim is also drawn in world space by GameScreen).
        setBackground(solid(new Color(0f, 0f, 0f, 0.35f)));

        pauseRoot = buildPausePanel();
        optionsRoot = buildOptionsPanel();
        volumeSlider = (Slider) optionsRoot.findActor("volume-slider");
        volumeValueLabel = (Label) optionsRoot.findActor("volume-label");

        add(pauseRoot).center();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isShowingOptions() {
        return showingOptions && isVisible();
    }

    public void openPause() {
        showingOptions = false;
        clearChildren();
        add(pauseRoot).center();
        setVisible(true);
        toFront();
    }

    public void openOptions() {
        showingOptions = true;
        clearChildren();
        syncVolumeWidgets();
        add(optionsRoot).center();
        setVisible(true);
        toFront();
    }

    public void closeAll() {
        showingOptions = false;
        setVisible(false);
    }

    @Override
    public boolean isPanelOpen() {
        return isVisible();
    }

    @Override
    public void closePanel() {
        if (showingOptions) {
            openPause();
            if (listener != null) {
                listener.onBackFromOptions();
            }
        } else {
            closeAll();
            if (listener != null) {
                listener.onResume();
            }
        }
    }

    private Table buildPausePanel() {
        Table panel = card();
        Label title = new Label("PAUSADO", skin);
        title.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        title.setAlignment(Align.center);
        panel.add(title).padBottom(16).growX().row();

        panel.add(menuButton("Continuar", () -> {
            if (listener != null) {
                listener.onResume();
            }
        })).width(220).height(36).padBottom(8).row();

        panel.add(menuButton("Salvar Jogo", () -> {
            if (listener != null) {
                listener.onSaveGame();
            }
        })).width(220).height(36).padBottom(8).row();

        panel.add(menuButton("Opcoes", () -> {
            if (listener != null) {
                listener.onOpenOptions();
            }
            openOptions();
        })).width(220).height(36).padBottom(8).row();

        panel.add(menuButton("Reiniciar", () -> {
            if (listener != null) {
                listener.onRestart();
            }
        })).width(220).height(36).padBottom(8).row();

        panel.add(menuButton("Sair", () -> {
            if (listener != null) {
                listener.onExit();
            }
        })).width(220).height(36).row();

        Label hint = new Label("[ESC] Continuar", skin);
        hint.setColor(Color.LIGHT_GRAY);
        panel.add(hint).padTop(12).row();
        return panel;
    }

    private Table buildOptionsPanel() {
        Table panel = card();
        Label title = new Label("OPCOES", skin);
        title.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        title.setAlignment(Align.center);
        panel.add(title).padBottom(12).growX().row();

        Label volTitle = new Label("Volume da Musica", skin);
        panel.add(volTitle).left().padBottom(4).row();

        Label value = new Label(MusicSettings.getVolumePercent() + "%", skin);
        value.setName("volume-label");
        value.setAlignment(Align.center);
        panel.add(value).growX().padBottom(6).row();

        Slider slider = new Slider(0f, 100f, 1f, false, skin, "music-volume");
        slider.setName("volume-slider");
        slider.setValue(MusicSettings.getVolumePercent());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int pct = Math.round(slider.getValue());
                MusicSettings.setVolumePercent(pct);
                value.setText(pct + "%");
            }
        });
        panel.add(slider).width(220).height(24).padBottom(14).row();

        panel.add(menuButton("Voltar", () -> {
            openPause();
            if (listener != null) {
                listener.onBackFromOptions();
            }
        })).width(220).height(36).row();

        Label hint = new Label("[ESC] Voltar", skin);
        hint.setColor(Color.LIGHT_GRAY);
        panel.add(hint).padTop(12).row();
        return panel;
    }

    private void syncVolumeWidgets() {
        if (volumeSlider != null) {
            volumeSlider.setValue(MusicSettings.getVolumePercent());
        }
        if (volumeValueLabel != null) {
            volumeValueLabel.setText(MusicSettings.getVolumePercent() + "%");
        }
    }

    private Table card() {
        Table panel = new Table();
        panel.setBackground(solid(new Color(0.1f, 0.1f, 0.14f, 0.94f)));
        panel.pad(22, 28, 18, 28);
        return panel;
    }

    private TextButton menuButton(String text, Runnable action) {
        TextButton btn = new TextButton(text, skin, "pause-menu");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return btn;
    }

    private static void ensureStyles(Skin skin) {
        if (!skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
            style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
            style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
            style.checked = solid(new Color(0.4f, 0.35f, 0.18f, 1f));
            skin.add("pause-menu", style);
        }
        if (!skin.has("music-volume", Slider.SliderStyle.class)) {
            Slider.SliderStyle style = new Slider.SliderStyle();
            style.background = solid(new Color(0.2f, 0.2f, 0.24f, 1f));
            style.knob = solid(new Color(0.9f, 0.8f, 0.35f, 1f));
            style.knobBefore = solid(new Color(0.55f, 0.45f, 0.2f, 1f));
            skin.add("music-volume", style);
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

    public void centerOnScreen() {
        // fillParent already covers stage; pack children
        invalidateHierarchy();
    }

    public void onResize(int width, int height) {
        setSize(width, height);
        centerOnScreen();
    }
}
