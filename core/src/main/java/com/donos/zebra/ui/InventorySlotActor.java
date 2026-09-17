package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.donos.zebra.items.ItemDefinition;

/**
 * Inventory grid slot with icon, quantity badge, and hover tooltip.
 */
public class InventorySlotActor extends Button {

    private final int slotIndex;
    private ItemDefinition item;
    private int quantity;
    private final AssetManager assetManager;
    private final BitmapFont qtyFont;
    private ItemTooltipPanel tooltipPanel;

    public InventorySlotActor(int slotIndex, Skin skin, AssetManager assetManager) {
        super(skin, "slot-style");
        this.slotIndex = slotIndex;
        this.assetManager = assetManager;
        this.item = null;
        this.quantity = 0;
        this.qtyFont = skin.getFont("default");
        installTooltipHover();
    }

    public InventorySlotActor(ItemDefinition item, AssetManager assetManager, Skin skin) {
        super(new ButtonStyle());
        this.slotIndex = -1;
        this.assetManager = assetManager;
        this.item = item;
        this.quantity = 0;
        this.qtyFont = skin != null && skin.has("default", BitmapFont.class)
            ? skin.getFont("default") : new BitmapFont();
        installTooltipHover();
    }

    /** Legacy drag-ghost ctor used by InventoryUI. */
    public InventorySlotActor(ItemDefinition item, AssetManager assetManager) {
        this(item, assetManager, null);
    }

    public void setTooltipPanel(ItemTooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
    }

    private void installTooltipHover() {
        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (tooltipPanel != null && item != null) {
                    tooltipPanel.showFor(item, InventorySlotActor.this);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (tooltipPanel != null) {
                    tooltipPanel.hide();
                }
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (item != null && item.getIconPath() != null && assetManager.isLoaded(item.getIconPath())) {
            Texture iconTexture = assetManager.get(item.getIconPath(), Texture.class);

            float slotWidth = getWidth();
            float slotHeight = getHeight();
            float iconSize = Math.min(slotWidth * 0.8f, slotHeight * 0.8f);
            float iconX = getX() + (slotWidth - iconSize) / 2f;
            float iconY = getY() + (slotHeight - iconSize) / 2f;
            batch.draw(iconTexture, iconX, iconY, iconSize, iconSize);

            if (quantity > 1 && qtyFont != null) {
                qtyFont.setColor(Color.WHITE);
                qtyFont.getData().setScale(0.7f);
                String text = String.valueOf(quantity);
                qtyFont.draw(batch, text, getX() + slotWidth - 14f, getY() + 12f);
                qtyFont.getData().setScale(1f);
            }
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
        if (item == null) {
            this.quantity = 0;
        }
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}
