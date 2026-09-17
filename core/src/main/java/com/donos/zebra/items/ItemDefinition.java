package com.donos.zebra.items;

/**
 * Represents the static template for an item.
 * Contains only immutable properties that define what the item is.
 */
public class ItemDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int maxStackSize;
    private final int value;
    private final boolean isConsumable;
    private final String iconPath;
    /** Weapon damage while equipped; 0 for non-weapons. */
    private final int attackDamage;
    /** Flat damage reduction while equipped as armor; 0 for non-armor. */
    private final int defense;
    private final ArmorSlot armorSlot;
    /** HP restored when consumed; 0 for non-potions. */
    private final int healAmount;

    public ItemDefinition(String id, String name, String description, ItemType type,
                          int maxStackSize, int value, boolean isConsumable, String iconPath) {
        this(id, name, description, type, maxStackSize, value, isConsumable, iconPath, 0, 0, ArmorSlot.NONE, 0);
    }

    public ItemDefinition(String id, String name, String description, ItemType type,
                          int maxStackSize, int value, boolean isConsumable, String iconPath,
                          int attackDamage, int defense) {
        this(id, name, description, type, maxStackSize, value, isConsumable, iconPath,
            attackDamage, defense, ArmorSlot.NONE, 0);
    }

    public ItemDefinition(String id, String name, String description, ItemType type,
                          int maxStackSize, int value, boolean isConsumable, String iconPath,
                          int attackDamage, int defense, ArmorSlot armorSlot) {
        this(id, name, description, type, maxStackSize, value, isConsumable, iconPath,
            attackDamage, defense, armorSlot, 0);
    }

    public ItemDefinition(String id, String name, String description, ItemType type,
                          int maxStackSize, int value, boolean isConsumable, String iconPath,
                          int attackDamage, int defense, ArmorSlot armorSlot, int healAmount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.maxStackSize = maxStackSize;
        this.value = value;
        this.isConsumable = isConsumable;
        this.iconPath = iconPath;
        this.attackDamage = attackDamage;
        this.defense = defense;
        this.armorSlot = armorSlot == null ? ArmorSlot.NONE : armorSlot;
        this.healAmount = Math.max(0, healAmount);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getValue() {
        return value;
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public String getIconPath() {
        return iconPath;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public int getDefense() {
        return defense;
    }

    public ArmorSlot getArmorSlot() {
        return armorSlot;
    }

    public int getHealAmount() {
        return healAmount;
    }

    @Override
    public String toString() {
        return "ItemDefinition{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", type=" + type +
            '}';
    }
}
