package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.Inventory; 
import com.donos.zebra.items.ItemDefinition;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.items.PotionRules;

import java.util.ArrayList;
import java.util.List;

public class InventoryUI extends GameWindow {

    private final DragAndDrop dragAndDrop;
    private final AssetManager assetManager;
    private final Inventory backendInventory;
    private final Skin skin;
    private final Player player;
    private final Table slotGrid;
    private final ScrollPane scrollPane;
    private final CurrencyReadout currencyReadout;
    private final List<InventorySlotActor> slots = new ArrayList<>();
    private ItemTooltipPanel tooltipPanel;
    private ItemContextMenu contextMenu;
    private Runnable onEquipmentChanged;
    private java.util.function.Consumer<String> equipToast;
    private java.util.function.Consumer<String> statusToast;
    private int currentColumns = 4;

    public InventoryUI(Inventory backendInventory, Skin skin, AssetManager assetManager) {
        this(backendInventory, skin, assetManager, null);
    }

    public InventoryUI(Inventory backendInventory, Skin skin, AssetManager assetManager, Player player) {
        super("Inventario", skin);
        this.backendInventory = backendInventory;
        this.skin = skin;
        this.assetManager = assetManager;
        this.player = player;
        this.dragAndDrop = new DragAndDrop();
        
        setResizable(true);

        currencyReadout = new CurrencyReadout(skin, assetManager);

        slotGrid = new Table();
        slotGrid.top().left().pad(4);
        for (int i = 0; i < 16; i++) {
            InventorySlotActor slotActor = new InventorySlotActor(i, skin, assetManager);
            configurarDragAndDrop(slotActor);
            configurarRightClick(slotActor);
            slots.add(slotActor);
        }
        layoutSlotGrid(4);

        scrollPane = new ScrollPane(slotGrid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);

        Table body = new Table();
        body.add(currencyReadout).growX().pad(4).row();
        body.add(scrollPane).grow().row();
        add(body).grow();
        setSize(280, 340);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (slotGrid == null) {
            return;
        }
        float inner = Math.max(80f, getWidth() - getPadLeft() - getPadRight() - 20f);
        int cols = Math.max(2, Math.min(8, (int) (inner / 54f)));
        if (cols != currentColumns) {
            layoutSlotGrid(cols);
        }
        invalidateHierarchy();
    }

    private void layoutSlotGrid(int cols) {
        currentColumns = cols;
        slotGrid.clearChildren();
        for (int i = 0; i < slots.size(); i++) {
            slotGrid.add(slots.get(i)).size(48, 48).pad(3);
            if ((i + 1) % cols == 0) {
                slotGrid.row();
            }
        }
        slotGrid.invalidateHierarchy();
    }

    public void setOnEquipmentChanged(Runnable onEquipmentChanged) {
        this.onEquipmentChanged = onEquipmentChanged;
    }

    public void setEquipToast(java.util.function.Consumer<String> equipToast) {
        this.equipToast = equipToast;
    }

    public void setStatusToast(java.util.function.Consumer<String> statusToast) {
        this.statusToast = statusToast;
    }

    public void setTooltipPanel(ItemTooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
        for (InventorySlotActor slot : slots) {
            slot.setTooltipPanel(tooltipPanel);
        }
    }

    public void refresh() {
        if (player != null) {
            currencyReadout.refresh(player.getWallet());
        }
        for (InventorySlotActor slotActor : slots) {
                int index = slotActor.getSlotIndex();
            ItemStack stack = backendInventory.getStackAt(index);
                if (stack != null) {
                    slotActor.setItem(stack.getDefinition());
                slotActor.setQuantity(stack.getQuantity());
                } else {
                    slotActor.setItem(null);
                slotActor.setQuantity(0);
            }
        }
    }

    @Override
    public void openPanel() {
        super.openPanel();
        refresh();
    }

    private void configurarRightClick(InventorySlotActor slot) {
        slot.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 1 && player != null && !slot.isEmpty()) {
                    ItemDefinition item = slot.getItem();
                    if (PotionRules.isPotion(item)) {
                        showPotionMenu(slot, event.getStageX(), event.getStageY());
                    } else {
                        hideContextMenu();
                        equipFromInventory(item);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void showPotionMenu(InventorySlotActor slot, float stageX, float stageY) {
        if (getStage() == null) {
            return;
        }
        if (contextMenu == null) {
            contextMenu = new ItemContextMenu(skin);
            getStage().addActor(contextMenu);
        }
        final int index = slot.getSlotIndex();
        contextMenu.show(getStage(), stageX, stageY, new ItemContextMenu.Listener() {
            @Override
            public void onUse() {
                ItemStack before = backendInventory.getStackAt(index);
                int heal = before != null ? before.getDefinition().getHealAmount() : 0;
                PotionRules.Result result = player.tryUsePotionFromInventory(index);
                refresh();
                if (onEquipmentChanged != null) {
                    onEquipmentChanged.run();
                }
                if (statusToast != null) {
                    if (result == PotionRules.Result.OK) {
                        statusToast.accept("+" + heal + " HP");
                    } else {
                        statusToast.accept(PotionRules.feedback(result, player.getPotionCooldownRemaining()));
                    }
                }
            }

            @Override
            public void onMoveToCharacter() {
                if (player.movePotionStackToSlot(index)) {
                    refresh();
                    if (onEquipmentChanged != null) {
                        onEquipmentChanged.run();
                    }
                }
            }
        });
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    private void equipFromInventory(ItemDefinition item) {
        if (item == null || player == null) {
            return;
        }
        if (!player.equipFromInventory(item)) {
            return;
        }
        refresh();
        if (onEquipmentChanged != null) {
            onEquipmentChanged.run();
        }
        if (equipToast != null) {
            equipToast.accept(EquipFeedback.forEquip(item));
        }
    }

    private void configurarDragAndDrop(InventorySlotActor slot) {
        dragAndDrop.addSource(new Source(slot) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                InventorySlotActor actor = (InventorySlotActor) getActor();
                if (actor.isEmpty()) return null; 

                Payload payload = new Payload();
                payload.setObject(actor); 

                InventorySlotActor dragActor = new InventorySlotActor(actor.getSlotIndex(), skin, assetManager);
                dragActor.setItem(actor.getItem());
                dragActor.setQuantity(actor.getQuantity());
                dragActor.setSize(actor.getWidth(), actor.getHeight());
                payload.setDragActor(dragActor);
                return payload;
            }
        });

        dragAndDrop.addTarget(new Target(slot) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                return true; 
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
                InventorySlotActor slotOrigem = (InventorySlotActor) payload.getObject();
                InventorySlotActor slotDestino = (InventorySlotActor) getActor();
                if (slotOrigem.getSlotIndex() == slotDestino.getSlotIndex()) return;
                backendInventory.swapSlots(slotOrigem.getSlotIndex(), slotDestino.getSlotIndex());
                refresh();
            }
        });
    }

    public static Skin createDefaultSkin(BitmapFont font) {
        Skin skin = new Skin();
        skin.add("default", font);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = 
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        Drawable panelBg = GameWindow.createPaddedWindowBackground(
            new Color(0.1f, 0.1f, 0.14f, 0.94f),
            GameWindow.TITLE_PAD, GameWindow.SIDE_PAD, GameWindow.BOTTOM_PAD, GameWindow.SIDE_PAD);

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = panelBg;
        windowStyle.titleFont = font;
        windowStyle.titleFontColor = new Color(0.9f, 0.8f, 0.45f, 1f);
        windowStyle.stageBackground = null;
        skin.add("default", windowStyle);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = null;
        scrollStyle.vScroll = solidDrawable(new Color(0.25f, 0.25f, 0.28f, 0.8f));
        scrollStyle.vScrollKnob = solidDrawable(new Color(0.45f, 0.45f, 0.5f, 1f));
        scrollStyle.hScroll = scrollStyle.vScroll;
        scrollStyle.hScrollKnob = scrollStyle.vScrollKnob;
        skin.add("default", scrollStyle);

        TextButton.TextButtonStyle closeStyle = new TextButton.TextButtonStyle();
        closeStyle.font = font;
        closeStyle.fontColor = Color.WHITE;
        closeStyle.up = solidDrawable(new Color(0.45f, 0.15f, 0.15f, 1f));
        closeStyle.down = solidDrawable(new Color(0.3f, 0.1f, 0.1f, 1f));
        closeStyle.over = solidDrawable(new Color(0.55f, 0.2f, 0.2f, 1f));
        skin.add("window-close", closeStyle);

        Pixmap pixmapNormal = new Pixmap(48, 48, Pixmap.Format.RGBA8888);
        pixmapNormal.setColor(new Color(0.2f, 0.2f, 0.2f, 0.85f));
        pixmapNormal.fill();
        pixmapNormal.setColor(Color.GRAY);
        pixmapNormal.drawRectangle(0, 0, 48, 48);
        Texture textureNormal = new Texture(pixmapNormal);
        pixmapNormal.dispose();

        Pixmap pixmapHover = new Pixmap(48, 48, Pixmap.Format.RGBA8888);
        pixmapHover.setColor(new Color(0.32f, 0.32f, 0.35f, 0.95f));
        pixmapHover.fill();
        pixmapHover.setColor(Color.WHITE);
        pixmapHover.drawRectangle(0, 0, 48, 48);
        Texture textureHover = new Texture(pixmapHover);
        pixmapHover.dispose();

        com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle slotStyle =
            new com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle();
        slotStyle.up = new TextureRegionDrawable(textureNormal);
        slotStyle.over = new TextureRegionDrawable(textureHover);
        skin.add("slot-style", slotStyle);
        return skin;
    }

    private static Drawable solidDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
