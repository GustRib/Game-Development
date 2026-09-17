package com.donos.zebra.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed recipe list for the village crafting station (not a generic recipe engine).
 */
public final class CraftingRecipes {

    public static final CraftingRecipe COPPER_LONGSWORD;
    public static final CraftingRecipe COPPER_HELMET;
    public static final CraftingRecipe COPPER_CHESTPLATE;
    public static final CraftingRecipe COPPER_BOOTS;

    private static final List<CraftingRecipe> ALL;

    static {
        Map<ItemDefinition, Integer> swordCosts = new LinkedHashMap<>();
        swordCosts.put(ItemRegistry.COPPER_ORE, 5);
        swordCosts.put(ItemRegistry.IRON_ORE, 2);
        COPPER_LONGSWORD = new CraftingRecipe(
            "recipe_copper_longsword",
            "Lamina de Cobre",
            ItemRegistry.COPPER_LONGSWORD,
            swordCosts
        );

        // Helmet: cheap entry piece into the copper set.
        Map<ItemDefinition, Integer> helmetCosts = new LinkedHashMap<>();
        helmetCosts.put(ItemRegistry.COPPER_ORE, 2);
        helmetCosts.put(ItemRegistry.IRON_ORE, 1);
        COPPER_HELMET = new CraftingRecipe(
            "recipe_copper_helmet",
            "Capacete de Cobre",
            ItemRegistry.COPPER_HELMET,
            helmetCosts
        );

        // Chest: heaviest piece — most defense, most iron (combat loop).
        Map<ItemDefinition, Integer> chestCosts = new LinkedHashMap<>();
        chestCosts.put(ItemRegistry.COPPER_ORE, 3);
        chestCosts.put(ItemRegistry.IRON_ORE, 3);
        COPPER_CHESTPLATE = new CraftingRecipe(
            "recipe_copper_chestplate",
            "Peitoral de Cobre",
            ItemRegistry.COPPER_CHESTPLATE,
            chestCosts
        );

        // Boots: mid cost to finish the set.
        Map<ItemDefinition, Integer> bootsCosts = new LinkedHashMap<>();
        bootsCosts.put(ItemRegistry.COPPER_ORE, 2);
        bootsCosts.put(ItemRegistry.IRON_ORE, 2);
        COPPER_BOOTS = new CraftingRecipe(
            "recipe_copper_boots",
            "Botas de Cobre",
            ItemRegistry.COPPER_BOOTS,
            bootsCosts
        );

        ALL = Collections.unmodifiableList(Arrays.asList(
            COPPER_LONGSWORD, COPPER_HELMET, COPPER_CHESTPLATE, COPPER_BOOTS));
    }

    private CraftingRecipes() {
    }

    public static List<CraftingRecipe> all() {
        return ALL;
    }
}
