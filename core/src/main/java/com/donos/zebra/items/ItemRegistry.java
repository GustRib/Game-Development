package com.donos.zebra.items;

import java.util.HashMap;
import java.util.Map;

/**
 * Central catalog of all item definitions in the game.
 */
public class ItemRegistry {

    private static final Map<String, ItemDefinition> REGISTRY = new HashMap<>();

    public static final ItemDefinition COPPER_ORE;
    public static final ItemDefinition IRON_ORE;
    public static final ItemDefinition IRON_SWORD;
    public static final ItemDefinition COPPER_LONGSWORD;
    public static final ItemDefinition COPPER_HELMET;
    public static final ItemDefinition COPPER_CHESTPLATE;
    public static final ItemDefinition COPPER_BOOTS;
    public static final ItemDefinition HEALTH_POTION;
    public static final ItemDefinition STONE_PICKAXE;

    static {
        COPPER_ORE = new ItemDefinition(
            "copper_ore",
            "Minerio de Cobre",
            "Um minerio bruto, usado para forjar equipamentos basicos.",
            ItemType.RESOURCE,
            99,
            5,
            false,
            "items/copper_ore.png"
        );

        IRON_ORE = new ItemDefinition(
            "iron_ore",
            "Minerio de Ferro",
            "Fragmentos metalicos arrancados de invasores. Essencial para forja.",
            ItemType.RESOURCE,
            99,
            12,
            false,
            "items/iron_ore.png"
        );

        // Starter sword shares copper_sword art; stats alone distinguish it from the crafted upgrade.
        IRON_SWORD = new ItemDefinition(
            "iron_sword",
            "Espada de Ferro",
            "Sua primeira lamina. Dano 10.",
            ItemType.WEAPON,
            1,
            150,
            false,
            "items/copper_sword.png",
            10,
            0
        );

        COPPER_LONGSWORD = new ItemDefinition(
            "copper_longsword",
            "Lamina de Cobre",
            "Lamina reforcada na forja. Dano 16.",
            ItemType.WEAPON,
            1,
            200,
            false,
            "items/copper_sword.png",
            16,
            0
        );

        COPPER_HELMET = new ItemDefinition(
            "copper_helmet",
            "Capacete de Cobre",
            "Protege a cabeca. Defesa 2.",
            ItemType.ARMOR,
            1,
            120,
            false,
            "items/copper_helmet.png",
            0,
            2,
            ArmorSlot.HELMET
        );

        COPPER_CHESTPLATE = new ItemDefinition(
            "copper_chestplate",
            "Peitoral de Cobre",
            "Placas no torso. Defesa 4.",
            ItemType.ARMOR,
            1,
            180,
            false,
            "items/copper_chestplate.png",
            0,
            4,
            ArmorSlot.CHESTPLATE
        );

        COPPER_BOOTS = new ItemDefinition(
            "copper_boots",
            "Botas de Cobre",
            "Protecao nos pes. Defesa 2.",
            ItemType.ARMOR,
            1,
            120,
            false,
            "items/copper_boots.png",
            0,
            2,
            ArmorSlot.BOOTS
        );

        HEALTH_POTION = new ItemDefinition(
            "health_potion",
            "Pocao de Vida",
            "Recupera 50 pontos de vida instantaneamente.",
            ItemType.CONSUMABLE,
            10,
            25,
            true,
            "items/health_potion.png"
        );

        STONE_PICKAXE = new ItemDefinition(
            "stone_pickaxe",
            "Picareta de Pedra",
            "Utilizada para minerar cobre.",
            ItemType.RESOURCE,
            1,
            10,
            false,
            "items/stone_pickaxe.png"
        );

        register(COPPER_ORE);
        register(IRON_ORE);
        register(IRON_SWORD);
        register(COPPER_LONGSWORD);
        register(COPPER_HELMET);
        register(COPPER_CHESTPLATE);
        register(COPPER_BOOTS);
        register(HEALTH_POTION);
        register(STONE_PICKAXE);
    }

    private static void register(ItemDefinition item) {
        REGISTRY.put(item.getId(), item);
    }

    public static ItemDefinition getItem(String id) {
        return REGISTRY.get(id);
    }
}
