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
    public static final ItemDefinition COPPER_GLOVES;
    public static final ItemDefinition COPPER_BOOTS;
    public static final ItemDefinition POTION_SMALL;
    public static final ItemDefinition POTION_MEDIUM;
    public static final ItemDefinition POTION_LARGE;
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

        // Mentor starter sword — Espada de Cobre (copper_sword art).
        IRON_SWORD = new ItemDefinition(
            "copper_sword",
            "Espada de Cobre",
            "Sua primeira lamina. Dano 10.",
            ItemType.WEAPON,
            1,
            150,
            false,
            "items/copper_sword.png",
            10,
            0
        );

        // Station-crafted upgrade — Espada de Ferro (no distinct iron_sword.png yet; reuse copper art).
        // Damage 24: meaningful upgrade over starter (10) and old craft (16) for ~4× material cost.
        COPPER_LONGSWORD = new ItemDefinition(
            "iron_sword",
            "Espada de Ferro",
            "Lamina de ferro reforcada na forja. Dano 24.",
            ItemType.WEAPON,
            1,
            200,
            false,
            "items/copper_sword.png",
            24,
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

        COPPER_GLOVES = new ItemDefinition(
            "copper_gloves",
            "Luvas de Cobre",
            "Protecao nas maos. Defesa 1.",
            ItemType.ARMOR,
            1,
            100,
            false,
            "items/copper_gloves.png",
            0,
            1,
            ArmorSlot.GLOVES
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

        // Asset names on disk: small/medium/big_health_potion.png (owner-provided).
        POTION_SMALL = new ItemDefinition(
            "potion_small",
            "Pocao Pequena",
            "Recupera 10 pontos de vida.",
            ItemType.CONSUMABLE,
            99,
            15,
            true,
            "items/small_health_potion.png",
            0,
            0,
            ArmorSlot.NONE,
            10
        );

        POTION_MEDIUM = new ItemDefinition(
            "potion_medium",
            "Pocao Media",
            "Recupera 20 pontos de vida.",
            ItemType.CONSUMABLE,
            99,
            30,
            true,
            "items/medium_health_potion.png",
            0,
            0,
            ArmorSlot.NONE,
            20
        );

        POTION_LARGE = new ItemDefinition(
            "potion_large",
            "Pocao Grande",
            "Recupera 30 pontos de vida.",
            ItemType.CONSUMABLE,
            99,
            50,
            true,
            "items/big_health_potion.png",
            0,
            0,
            ArmorSlot.NONE,
            30
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
        register(COPPER_GLOVES);
        register(COPPER_BOOTS);
        register(POTION_SMALL);
        register(POTION_MEDIUM);
        register(POTION_LARGE);
        register(STONE_PICKAXE);
    }

    private static void register(ItemDefinition item) {
        REGISTRY.put(item.getId(), item);
    }

    public static ItemDefinition getItem(String id) {
        return REGISTRY.get(id);
    }
}
