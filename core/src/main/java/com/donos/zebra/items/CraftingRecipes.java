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
    public static final CraftingRecipe COPPER_GLOVES;
    public static final CraftingRecipe COPPER_BOOTS;

    private static final List<CraftingRecipe> ALL;

    static {
        Map<ItemDefinition, Integer> swordCosts = new LinkedHashMap<>();
        swordCosts.put(ItemRegistry.IRON_ORE, 20);
        swordCosts.put(ItemRegistry.COPPER_ORE, 10);
        COPPER_LONGSWORD = new CraftingRecipe(
            "recipe_iron_sword",
            "Espada de Ferro",
            ItemRegistry.COPPER_LONGSWORD,
            swordCosts
        );

        Map<ItemDefinition, Integer> helmetCosts = new LinkedHashMap<>();
        helmetCosts.put(ItemRegistry.COPPER_ORE, 2);
        helmetCosts.put(ItemRegistry.IRON_ORE, 1);
        COPPER_HELMET = new CraftingRecipe(
            "recipe_copper_helmet",
            "Capacete de Cobre",
            ItemRegistry.COPPER_HELMET,
            helmetCosts
        );

        Map<ItemDefinition, Integer> chestCosts = new LinkedHashMap<>();
        chestCosts.put(ItemRegistry.COPPER_ORE, 3);
        chestCosts.put(ItemRegistry.IRON_ORE, 3);
        COPPER_CHESTPLATE = new CraftingRecipe(
            "recipe_copper_chestplate",
            "Peitoral de Cobre",
            ItemRegistry.COPPER_CHESTPLATE,
            chestCosts
        );

        Map<ItemDefinition, Integer> glovesCosts = new LinkedHashMap<>();
        glovesCosts.put(ItemRegistry.COPPER_ORE, 2);
        glovesCosts.put(ItemRegistry.IRON_ORE, 1);
        COPPER_GLOVES = new CraftingRecipe(
            "recipe_copper_gloves",
            "Luvas de Cobre",
            ItemRegistry.COPPER_GLOVES,
            glovesCosts
        );

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
            COPPER_LONGSWORD, COPPER_HELMET, COPPER_CHESTPLATE, COPPER_GLOVES, COPPER_BOOTS));
    }

    private CraftingRecipes() {
    }

    public static List<CraftingRecipe> all() {
        return ALL;
    }

    public static CraftingRecipe findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (CraftingRecipe recipe : ALL) {
            if (id.equals(recipe.getId())) {
                return recipe;
            }
        }
        return null;
    }
}
