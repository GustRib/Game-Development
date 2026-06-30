package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.donos.zebra.items.ItemDefinition;

public class InventorySlotActor extends Button {

    private final int slotIndex;
    private ItemDefinition item;
    private final AssetManager assetManager;

    // Construtor principal para a grade do inventário
    public InventorySlotActor(int slotIndex, Skin skin, AssetManager assetManager) {
        super(skin, "slot-style"); 
        this.slotIndex = slotIndex;
        this.assetManager = assetManager;
        this.item = null;
    }

    // Construtor auxiliar para o item flutuante (não precisa de slotIndex ou background de Skin)
    public InventorySlotActor(ItemDefinition item, AssetManager assetManager) {
        super(new ButtonStyle()); 
        this.slotIndex = -1;
        this.assetManager = assetManager;
        this.item = item;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Desenha o quadrado de fundo do slot baseado no style da Skin
        super.draw(batch, parentAlpha);

        // Se houver um item e seu ícone estiver devidamente carregado no AssetManager
        if (item != null && item.getIconPath() != null && assetManager.isLoaded(item.getIconPath())) {
            Texture iconTexture = assetManager.get(item.getIconPath(), Texture.class);
            
            float slotWidth = getWidth();
            float slotHeight = getHeight();
            
            float iconSize = Math.min(slotWidth * 0.8f, slotHeight * 0.8f); 
            
            float iconX = getX() + (slotWidth - iconSize) / 2f;
            float iconY = getY() + (slotHeight - iconSize) / 2f;

            batch.draw(iconTexture, iconX, iconY, iconSize, iconSize);
        }
    }

    public boolean isEmpty() {
        return item == null;
    }

    public ItemDefinition getItem() {
        return item;
    }

    public void setItem(ItemDefinition item) {
        this.item = item;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}