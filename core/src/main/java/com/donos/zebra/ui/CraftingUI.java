package com.donos.zebra.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.CraftingController;
import com.donos.zebra.items.CraftingJob;
import com.donos.zebra.items.CraftingRecipe;
import com.donos.zebra.items.CraftingRecipes;
import com.donos.zebra.items.Inventory;
import com.donos.zebra.items.ItemDefinition;

import java.util.Map;

/**
 * Crafting forge window — starts delayed crafts via {@link CraftingController}.
 */
public class CraftingUI extends GameWindow {

    private final Skin skin;
    private final AssetManager assetManager;
    private final CraftingController craftingController;
    private final Label statusLabel;
    private final Label countdownLabel;
    private final ProgressBar progressBar;
    private final Table recipeTable;
    private final ScrollPane scrollPane;
    private Player boundPlayer;
    private Runnable onOpened;
    private Runnable onInventoryChanged;

    public CraftingUI(Skin skin, AssetManager assetManager, CraftingController craftingController) {
        super("Forja", skin);
        this.skin = skin;
        this.assetManager = assetManager;
        this.craftingController = craftingController;
        ensureButtonStyle(skin);
        ensureProgressStyle(skin);

        setResizable(false);

        recipeTable = new Table();
        recipeTable.top().left().pad(4);

        statusLabel = new Label("", skin);
        statusLabel.setWrap(true);

        countdownLabel = new Label("", skin);
        countdownLabel.setColor(new Color(0.9f, 0.8f, 0.4f, 1f));
        progressBar = new ProgressBar(0f, 1f, 0.01f, false, skin, "craft-progress");
        progressBar.setAnimateDuration(0.05f);

        scrollPane = new ScrollPane(recipeTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setForceScroll(false, true);

        Table body = new Table();
        body.add(scrollPane).grow().row();
        body.add(countdownLabel).growX().padTop(4).row();
        body.add(progressBar).growX().height(14).pad(4).row();
        body.add(statusLabel).growX().pad(6).row();
        Label help = new Label("[E/ESPACO] Fechar", skin);
        help.setColor(Color.LIGHT_GRAY);
        body.add(help).padBottom(4).left();

        add(body).grow();
        float maxH = Math.min(420f, Gdx.graphics.getHeight() * 0.75f);
        float maxW = Math.min(360f, Gdx.graphics.getWidth() * 0.55f);
        setSize(maxW, maxH);
    }

    public void setOnOpened(Runnable onOpened) {
        this.onOpened = onOpened;
    }

    public void setOnInventoryChanged(Runnable onInventoryChanged) {
        this.onInventoryChanged = onInventoryChanged;
    }

    public void open(Player player) {
        this.boundPlayer = player;
        statusLabel.setText("Escolha uma receita.");
        statusLabel.setColor(Color.WHITE);
        float maxH = Math.min(420f, Gdx.graphics.getHeight() * 0.75f);
        float maxW = Math.min(360f, Gdx.graphics.getWidth() * 0.55f);
        setSize(maxW, maxH);
        setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);
        rebuild();
        refreshProgressUi();
        openPanel();
        if (getStage() != null) {
            clampToStage(getStage().getWidth(), getStage().getHeight());
        }
        if (onOpened != null) {
            onOpened.run();
        }
    }

    public void close() {
        closePanel();
    }

    @Override
    public void closePanel() {
        boundPlayer = null;
        super.closePanel();
    }

    /** Call each frame so bar/countdown stay live while the window is open. */
    public void refreshProgressUi() {
        CraftingJob job = craftingController.getActiveJob();
        if (job == null) {
            countdownLabel.setText("");
            progressBar.setValue(0f);
            progressBar.setVisible(false);
            return;
        }
        progressBar.setVisible(true);
        progressBar.setValue(job.getProgress());
        if (job.isComplete()) {
            countdownLabel.setText("Concluido: " + job.getRecipe().getResult().getName());
            statusLabel.setText("Forjado: " + job.getRecipe().getResult().getName() + " (inventario).");
            statusLabel.setColor(Color.GREEN);
        } else {
            int secs = (int) Math.ceil(job.getSecondsRemaining());
            countdownLabel.setText("Forjando... " + secs);
        }
    }

    public void rebuild() {
        recipeTable.clearChildren();
        if (boundPlayer == null) {
            return;
        }

        float contentWidth = Math.max(180f, getWidth() - getPadLeft() - getPadRight() - 28f);
        Inventory inventory = boundPlayer.getInventory();
        boolean busy = craftingController.isBusy();
        for (CraftingRecipe recipe : CraftingRecipes.all()) {
            recipeTable.add(buildRecipeRow(recipe, inventory, contentWidth, busy))
                .width(contentWidth).growX().padBottom(6).row();
        }
        recipeTable.pack();
        scrollPane.layout();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (boundPlayer != null && recipeTable != null) {
            rebuild();
        }
    }

    private Table buildRecipeRow(CraftingRecipe recipe, Inventory inventory, float width, boolean busy) {
        Table row = new Table();
        row.setBackground(createColorDrawable(new Color(0.18f, 0.18f, 0.2f, 0.95f)));
        row.pad(6);
        row.left();

        boolean canAfford = recipe.canAfford(inventory);
        ItemDefinition result = recipe.getResult();

        Table header = new Table();
        Image icon = createResultIcon(result, canAfford && !busy);
        if (icon != null) {
            header.add(icon).size(24, 24).padRight(6);
        }

        Label name = new Label(recipe.getDisplayName(), skin);
        name.setColor(canAfford && !busy ? Color.WHITE : Color.GRAY);
        name.setWrap(true);
        header.add(name).growX().left();
        row.add(header).width(width - 12f).growX().row();

        String statLine;
        if (result.getAttackDamage() > 0) {
            statLine = "Dano " + result.getAttackDamage();
        } else {
            statLine = "Defesa " + result.getDefense();
        }
        Label stats = new Label(statLine + "  |  " + formatCosts(recipe, inventory), skin);
        stats.setWrap(true);
        stats.setColor(canAfford && !busy ? new Color(0.7f, 0.85f, 0.7f, 1f) : Color.DARK_GRAY);
        row.add(stats).width(width - 12f).growX().padTop(2).left().row();

        String btnText;
        if (busy) {
            btnText = "FORJANDO...";
        } else if (canAfford) {
            btnText = "CRIAR";
        } else {
            btnText = "FALTA MATERIAL";
        }
        TextButton craftBtn = new TextButton(btnText, skin, "craft");
        craftBtn.setDisabled(busy || !canAfford);
        if (busy || !canAfford) {
            craftBtn.setColor(Color.DARK_GRAY);
        }
        craftBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tryStartCraft(recipe);
            }
        });
        row.add(craftBtn).padTop(4).height(28).growX();
        return row;
    }

    private Image createResultIcon(ItemDefinition result, boolean canAfford) {
        String path = result.getIconPath();
        if (path == null || !assetManager.isLoaded(path)) {
            return null;
        }
        Image image = new Image(assetManager.get(path, Texture.class));
        if (!canAfford) {
            image.setColor(0.45f, 0.45f, 0.45f, 1f);
        }
        return image;
    }

    private void tryStartCraft(CraftingRecipe recipe) {
        if (boundPlayer == null) {
            return;
        }
        if (craftingController.isBusy()) {
            statusLabel.setText("Ja ha uma forja em andamento.");
            statusLabel.setColor(Color.ORANGE);
            rebuild();
            return;
        }
        if (!recipe.canAfford(boundPlayer.getInventory())) {
            statusLabel.setText("Materiais insuficientes.");
            statusLabel.setColor(Color.ORANGE);
            rebuild();
            return;
        }
        boolean ok = craftingController.tryStart(recipe, boundPlayer.getInventory());
        if (ok) {
            statusLabel.setText("Forjando " + recipe.getResult().getName() + "...");
            statusLabel.setColor(Color.LIGHT_GRAY);
            if (onInventoryChanged != null) {
                onInventoryChanged.run();
            }
        } else {
            statusLabel.setText("Nao foi possivel iniciar a forja.");
            statusLabel.setColor(Color.RED);
        }
        rebuild();
        refreshProgressUi();
    }

    private static String formatCosts(CraftingRecipe recipe, Inventory inventory) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ItemDefinition, Integer> entry : recipe.getCosts().entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            int have = inventory.getItemCount(entry.getKey());
            sb.append(entry.getValue()).append("x ").append(entry.getKey().getName())
                .append(" (").append(have).append(")");
        }
        return sb.toString();
    }

    private static void ensureButtonStyle(Skin skin) {
        if (skin.has("craft", TextButton.TextButtonStyle.class)) {
            return;
        }
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = skin.getFont("default");
        style.fontColor = Color.WHITE;
        style.disabledFontColor = Color.DARK_GRAY;
        style.up = createColorDrawable(new Color(0.35f, 0.28f, 0.15f, 1f));
        style.down = createColorDrawable(new Color(0.25f, 0.2f, 0.1f, 1f));
        style.disabled = createColorDrawable(new Color(0.2f, 0.2f, 0.2f, 1f));
        skin.add("craft", style);
    }

    private static void ensureProgressStyle(Skin skin) {
        if (skin.has("craft-progress", ProgressBar.ProgressBarStyle.class)) {
            return;
        }
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = createColorDrawable(new Color(0.15f, 0.15f, 0.18f, 1f));
        style.knobBefore = createColorDrawable(new Color(0.75f, 0.55f, 0.2f, 1f));
        skin.add("craft-progress", style);
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
