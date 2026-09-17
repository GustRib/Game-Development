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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.CraftingRecipe;
import com.donos.zebra.items.CraftingRecipes;
import com.donos.zebra.items.Inventory;
import com.donos.zebra.items.ItemDefinition;

import java.util.Map;

/**
 * Scene2D crafting panel shown when interacting with the world forge.
 */
public class CraftingUI extends Table {

    private final Skin skin;
    private final AssetManager assetManager;
    private final Label statusLabel;
    private Player boundPlayer;

    public CraftingUI(Skin skin, AssetManager assetManager) {
        this.skin = skin;
        this.assetManager = assetManager;
        ensureButtonStyle(skin);

        setBackground(createColorDrawable(new Color(0.1f, 0.12f, 0.14f, 0.92f)));
        pad(12);
        align(Align.top);

        Label title = new Label("FORJA", skin);
        title.setColor(new Color(0.85f, 0.75f, 0.4f, 1f));
        add(title).padBottom(8).row();

        statusLabel = new Label("", skin);
        statusLabel.setFontScale(0.7f);
        statusLabel.setWrap(true);

        setVisible(false);
    }

    public void open(Player player) {
        this.boundPlayer = player;
        statusLabel.setText("Escolha uma receita.");
        rebuild();
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        boundPlayer = null;
    }

    public void rebuild() {
        clearChildren();

        Label title = new Label("FORJA", skin);
        title.setColor(new Color(0.85f, 0.75f, 0.4f, 1f));
        add(title).padBottom(8).row();

        if (boundPlayer == null) {
            pack();
            return;
        }

        Inventory inventory = boundPlayer.getInventory();
        for (CraftingRecipe recipe : CraftingRecipes.all()) {
            add(buildRecipeRow(recipe, inventory)).growX().padBottom(6).row();
        }

        add(statusLabel).growX().padTop(4).row();
        Label help = new Label("[E/ESPACO] Fechar", skin);
        help.setFontScale(0.55f);
        help.setColor(Color.LIGHT_GRAY);
        add(help).padTop(6);
        pack();
    }

    private Table buildRecipeRow(CraftingRecipe recipe, Inventory inventory) {
        Table row = new Table();
        row.setBackground(createColorDrawable(new Color(0.18f, 0.18f, 0.2f, 0.95f)));
        row.pad(6);

        boolean canAfford = recipe.canAfford(inventory);
        ItemDefinition result = recipe.getResult();

        Table header = new Table();
        Image icon = createResultIcon(result, canAfford);
        if (icon != null) {
            header.add(icon).size(24, 24).padRight(6);
        }

        Label name = new Label(recipe.getDisplayName(), skin);
        name.setFontScale(0.75f);
        name.setColor(canAfford ? Color.WHITE : Color.GRAY);
        header.add(name).left().growX();
        row.add(header).left().growX().row();

        String statLine;
        if (result.getAttackDamage() > 0) {
            statLine = "Dano " + result.getAttackDamage();
        } else {
            statLine = "Defesa " + result.getDefense();
        }
        Label stats = new Label(statLine + "  |  " + formatCosts(recipe, inventory), skin);
        stats.setFontScale(0.6f);
        stats.setColor(canAfford ? new Color(0.7f, 0.85f, 0.7f, 1f) : Color.DARK_GRAY);
        row.add(stats).left().growX().padTop(2).row();

        TextButton craftBtn = new TextButton(canAfford ? "CRIAR" : "FALTA MATERIAL", skin, "craft");
        craftBtn.setDisabled(!canAfford);
        if (!canAfford) {
            craftBtn.setColor(Color.DARK_GRAY);
        }
        craftBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tryCraft(recipe);
            }
        });
        row.add(craftBtn).padTop(4).height(22).growX();
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

    private void tryCraft(CraftingRecipe recipe) {
        if (boundPlayer == null) {
            return;
        }
        if (!recipe.canAfford(boundPlayer.getInventory())) {
            statusLabel.setText("Materiais insuficientes.");
            statusLabel.setColor(Color.ORANGE);
            rebuild();
            return;
        }
        boolean ok = recipe.craft(boundPlayer.getInventory());
        if (ok) {
            boundPlayer.equipCrafted(recipe.getResult());
            statusLabel.setText("Forjado: " + recipe.getResult().getName() + " (equipado).");
            statusLabel.setColor(Color.GREEN);
        } else {
            statusLabel.setText("Falha ao forjar.");
            statusLabel.setColor(Color.RED);
        }
        rebuild();
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

    private static Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
