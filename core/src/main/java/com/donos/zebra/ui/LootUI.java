package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.items.ItemStack;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.List;

public class LootUI extends Table {

    private final Skin skin;
    private final AssetManager assetManager;
    private final Drawable windowBackground;
    private final Drawable slotBackground;
    private final Label titleLabel;

    public LootUI(Skin skin, AssetManager assetManager) {
        this.skin = skin;
        this.assetManager = assetManager;

        // Estética: Amarelo/Dourado para diferenciar do Inventário
        this.windowBackground = createColorDrawable(new Color(0.15f, 0.15f, 0.05f, 0.9f));
        this.slotBackground = createColorDrawable(new Color(0.4f, 0.4f, 0.2f, 1f));

        this.setBackground(windowBackground);
        this.pad(10);
        this.align(Align.top);
        
        this.titleLabel = new Label("CORPO", skin);
        this.titleLabel.setColor(Color.YELLOW);
        
        this.setVisible(false);
    }

    /**
     * Preenche a janela com os itens de um inimigo específico
     */
    public void updateLoot(Enemy enemy) {
        this.clearChildren();
        this.add(titleLabel).padBottom(10).row();
        
        Table grid = new Table();
        List<ItemStack> loot = enemy.getLootTable();

        for (ItemStack stack : loot) {
            Stack slotStack = createSlot(stack);
            grid.add(slotStack).size(40, 40).pad(2);
        }
        
        this.add(grid).row();
        
        // Rodapé visual
        Label help = new Label("[ESPAÇO] PEGAR TUDO", skin);
        help.setFontScale(0.5f);
        this.add(help).padTop(10);
        
        this.pack();
    }

    private Stack createSlot(ItemStack stack) {
        Stack stackGroup = new Stack();
        stackGroup.add(new Image(slotBackground));

        String path = stack.getDefinition().getIconPath();
        if (assetManager.isLoaded(path)) {
            stackGroup.add(new Image(assetManager.get(path, Texture.class)));
        }

        if (stack.getQuantity() > 1) {
            Label qty = new Label(String.valueOf(stack.getQuantity()), skin);
            qty.setAlignment(Align.bottomRight);
            qty.setFontScale(0.8f);
            stackGroup.add(qty);
        }
        return stackGroup;
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