package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.ArmorSlot;
import com.donos.zebra.items.ItemDefinition;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.items.ItemType;
import com.donos.zebra.items.PotionRules;

/**
 * Equipment panel (P): weapon + armor + potion belt.
 */
public class EquipmentUI extends GameWindow {

    private final Skin skin;
    private final AssetManager assetManager;
    private final ItemTooltipPanel tooltipPanel;
    private final EquipSlotActor weaponSlot;
    private final EquipSlotActor helmetSlot;
    private final EquipSlotActor chestSlot;
    private final EquipSlotActor glovesSlot;
    private final EquipSlotActor bootsSlot;
    private final PotionSlotActor potionSlot;
    private final Label defenseLabel;
    private final Table content;
    private Player boundPlayer;
    private Runnable onInventoryChanged;
    private java.util.function.Consumer<String> equipToast;

    public EquipmentUI(Skin skin, AssetManager assetManager, ItemTooltipPanel tooltipPanel) {
        super("Personagem", skin);
        this.skin = skin;
        this.assetManager = assetManager;
        this.tooltipPanel = tooltipPanel;

        setResizable(true);

        content = new Table();
        content.top().left().pad(4);
        content.align(Align.top);

        weaponSlot = addSlot(content, "Arma", null);
        helmetSlot = addSlot(content, "Capacete", ArmorSlot.HELMET);
        chestSlot = addSlot(content, "Peitoral", ArmorSlot.CHESTPLATE);
        glovesSlot = addSlot(content, "Luvas", ArmorSlot.GLOVES);
        bootsSlot = addSlot(content, "Botas", ArmorSlot.BOOTS);
        potionSlot = addPotionSlot(content);

        defenseLabel = new Label("Defesa total: 0", skin);
        content.add(defenseLabel).padTop(8).left().growX().row();

        Label help = new Label("Botao direito: desequipar  |  H: pocao", skin);
        help.setColor(Color.LIGHT_GRAY);
        content.add(help).padTop(6).left().growX().row();

        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        add(scroll).grow();
        setSize(280, 420);
    }

    public void setOnInventoryChanged(Runnable onInventoryChanged) {
        this.onInventoryChanged = onInventoryChanged;
    }

    public void setEquipToast(java.util.function.Consumer<String> equipToast) {
        this.equipToast = equipToast;
    }

    private EquipSlotActor addSlot(Table parent, String label, ArmorSlot armorSlot) {
        Table row = new Table();
        Label name = new Label(label, skin);
        EquipSlotActor slot = new EquipSlotActor(skin, assetManager, armorSlot, tooltipPanel);
        slot.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 1 && boundPlayer != null && slot.getItem() != null) {
                    ItemDefinition removed = slot.getItem();
                    if (boundPlayer.unequipToInventory(removed)) {
                        refresh();
                        if (onInventoryChanged != null) {
                            onInventoryChanged.run();
                        }
                        if (equipToast != null) {
                            equipToast.accept(EquipFeedback.forUnequip(removed));
                        }
                    }
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        row.add(name).width(90).left();
        row.add(slot).size(48, 48).padLeft(6).expandX().right();
        parent.add(row).growX().left().padBottom(4).row();
        return slot;
    }

    private PotionSlotActor addPotionSlot(Table parent) {
        Table row = new Table();
        Label name = new Label("Pocao", skin);
        PotionSlotActor slot = new PotionSlotActor(skin, assetManager, tooltipPanel);
        slot.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 1 && boundPlayer != null && boundPlayer.getPotionSlot() != null) {
                    if (boundPlayer.unequipPotionSlotToInventory()) {
                        refresh();
                        if (onInventoryChanged != null) {
                            onInventoryChanged.run();
                        }
                    }
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        row.add(name).width(90).left();
        row.add(slot).size(48, 48).padLeft(6).expandX().right();
        parent.add(row).growX().left().padBottom(4).row();
        return slot;
    }

    public void bind(Player player) {
        this.boundPlayer = player;
        refresh();
    }

    public void refresh() {
        if (boundPlayer == null) {
            return;
        }
        weaponSlot.setItem(boundPlayer.getEquippedWeapon());
        helmetSlot.setItem(boundPlayer.getEquippedHelmet());
        chestSlot.setItem(boundPlayer.getEquippedChestplate());
        glovesSlot.setItem(boundPlayer.getEquippedGloves());
        bootsSlot.setItem(boundPlayer.getEquippedBoots());
        potionSlot.setStack(boundPlayer.getPotionSlot());
        defenseLabel.setText("Defesa total: " + (int) boundPlayer.getDefense()
            + "  |  Dano: " + (int) boundPlayer.getAttackDamage());
    }

    @Override
    public void openPanel() {
        super.openPanel();
        refresh();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (content != null) {
            content.invalidateHierarchy();
        }
    }

    public static final class EquipSlotActor extends Button {
        private final AssetManager assetManager;
        private final ArmorSlot armorSlot;
        private ItemDefinition item;
        private final ItemTooltipPanel tooltipPanel;
        private final BitmapFont font;

        public EquipSlotActor(Skin skin, AssetManager assetManager, ArmorSlot armorSlot,
                              ItemTooltipPanel tooltipPanel) {
            super(skin, "slot-style");
            this.assetManager = assetManager;
            this.armorSlot = armorSlot;
            this.tooltipPanel = tooltipPanel;
            this.font = skin.getFont("default");
            addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (tooltipPanel != null && item != null) {
                        tooltipPanel.showFor(item, EquipSlotActor.this);
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

        public ArmorSlot getArmorSlot() {
            return armorSlot;
        }

        public ItemDefinition getItem() {
            return item;
        }

        public void setItem(ItemDefinition item) {
            this.item = item;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if (item != null && item.getIconPath() != null && assetManager.isLoaded(item.getIconPath())) {
                Texture icon = assetManager.get(item.getIconPath(), Texture.class);
                float size = Math.min(getWidth(), getHeight()) * 0.8f;
                float ix = getX() + (getWidth() - size) / 2f;
                float iy = getY() + (getHeight() - size) / 2f;
                batch.draw(icon, ix, iy, size, size);
            } else if (font != null) {
                font.setColor(0.4f, 0.4f, 0.45f, 1f);
                font.getData().setScale(0.55f);
                String mark = "W";
                if (armorSlot == ArmorSlot.HELMET) {
                    mark = "C";
                } else if (armorSlot == ArmorSlot.CHESTPLATE) {
                    mark = "P";
                } else if (armorSlot == ArmorSlot.GLOVES) {
                    mark = "L";
                } else if (armorSlot == ArmorSlot.BOOTS) {
                    mark = "B";
                }
                font.draw(batch, mark, getX() + getWidth() / 2f - 4f, getY() + getHeight() / 2f + 4f);
                font.getData().setScale(1f);
                font.setColor(Color.WHITE);
            }
        }

        public boolean isWeaponSlot() {
            return armorSlot == null;
        }

        public boolean accepts(ItemDefinition def) {
            if (def == null) return false;
            if (armorSlot == null) return def.getType() == ItemType.WEAPON;
            return def.getType() == ItemType.ARMOR && def.getArmorSlot() == armorSlot;
        }
    }

    /** Fixed Poção slot — shows stack quantity. */
    public static final class PotionSlotActor extends Button {
        private final AssetManager assetManager;
        private final ItemTooltipPanel tooltipPanel;
        private final BitmapFont font;
        private ItemStack stack;

        public PotionSlotActor(Skin skin, AssetManager assetManager, ItemTooltipPanel tooltipPanel) {
            super(skin, "slot-style");
            this.assetManager = assetManager;
            this.tooltipPanel = tooltipPanel;
            this.font = skin.getFont("default");
            addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (tooltipPanel != null && stack != null) {
                        tooltipPanel.showFor(stack.getDefinition(), PotionSlotActor.this);
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

        public void setStack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if (stack != null && PotionRules.isPotion(stack.getDefinition())
                && stack.getDefinition().getIconPath() != null
                && assetManager.isLoaded(stack.getDefinition().getIconPath())) {
                Texture icon = assetManager.get(stack.getDefinition().getIconPath(), Texture.class);
                float size = Math.min(getWidth(), getHeight()) * 0.8f;
                float ix = getX() + (getWidth() - size) / 2f;
                float iy = getY() + (getHeight() - size) / 2f;
                batch.draw(icon, ix, iy, size, size);
                if (font != null && stack.getQuantity() > 1) {
                    font.setColor(Color.WHITE);
                    font.getData().setScale(0.7f);
                    font.draw(batch, String.valueOf(stack.getQuantity()),
                        getX() + getWidth() - 14f, getY() + 12f);
                    font.getData().setScale(1f);
                }
            } else if (font != null) {
                font.setColor(0.4f, 0.4f, 0.45f, 1f);
                font.getData().setScale(0.55f);
                font.draw(batch, "Po", getX() + getWidth() / 2f - 6f, getY() + getHeight() / 2f + 4f);
                font.getData().setScale(1f);
                font.setColor(Color.WHITE);
            }
        }
    }
}
