package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.items.Inventory;
import com.donos.zebra.items.ItemStack;
import com.badlogic.gdx.assets.AssetManager;

/**
 * Componente visual que representa o inventário do jogador em uma grade.
 */
public class InventoryUI extends Table {

    private final Inventory inventory;
    private final Skin skin;
    private final AssetManager assetManager;
    private final int slotsPerRow = 5;
    
    // Armazenamos os drawables diretamente para não depender do mapa interno da Skin
    private final Drawable windowBackground;
    private final Drawable slotBackground;

    public InventoryUI(Inventory inventory, Skin skin, AssetManager assetManager) {
        this.inventory = inventory;
        this.skin = skin;
        this.assetManager = assetManager;

        // Geramos texturas de fallback seguras e incolores caso a skin falhe
        this.windowBackground = createColorDrawable(new Color(0.1f, 0.1f, 0.1f, 0.8f));
        this.slotBackground = createColorDrawable(new Color(0.3f, 0.3f, 0.3f, 1f));

        // Configurações da tabela
        this.align(Align.center);
        this.setBackground(windowBackground);
        this.pad(10);
        this.setVisible(false); // Começa oculta
        
        refresh();
    }

    /**
     * Sincroniza a interface visual com os dados reais do Inventory.
     */
    public void refresh() {
        this.clearChildren(); // Limpa a grade atual
        
        ItemStack[] slots = inventory.getSlots();

        for (int i = 0; i < slots.length; i++) {
            Stack slotStack = createSlot(slots[i]);
            this.add(slotStack).size(40, 40).pad(2); // Tamanho fixo do slot

            if ((i + 1) % slotsPerRow == 0) {
                this.row(); // Pula para a próxima linha da grade
            }
        }
        this.pack(); // Ajusta o tamanho da janela ao conteúdo
    }

    /**
     * Cria um slot individual (Fundo + Ícone + Quantidade).
     */
    private Stack createSlot(ItemStack stack) {
        Stack stackGroup = new Stack();
        
        // 1. Fundo do Slot usando o drawable local seguro
        Image bg = new Image(slotBackground);
        stackGroup.add(bg);

        // 2. Se houver item no slot, adiciona o ícone e a quantidade
        if (stack != null) {
            String path = stack.getDefinition().getIconPath();
            if (assetManager.isLoaded(path)) {
                Image icon = new Image(assetManager.get(path, Texture.class));
                stackGroup.add(icon);
            }

            if (stack.getQuantity() > 1) {
                Label qtyLabel = new Label(String.valueOf(stack.getQuantity()), skin);
                qtyLabel.setAlignment(Align.bottomRight);
                stackGroup.add(qtyLabel);
            }
        }

        return stackGroup;
    }

    /**
     * Método auxiliar para criar cores sólidas sem depender da busca da Skin
     */
    private static Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    /**
     * Cria uma skin básica apenas para gerenciar fontes e estilos de texto.
     */
    public static Skin createDefaultSkin(BitmapFont font) {
        Skin skin = new Skin();
        skin.add("default", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        return skin;
    }
}