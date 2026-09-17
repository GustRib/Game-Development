package com.donos.zebra.items;

import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellPricesTest {

    @Test
    void oreUnitSellPrices() {
        assertEquals(3, SellPrices.unitSellPrice(ItemRegistry.IRON_ORE));
        assertEquals(4, SellPrices.unitSellPrice(ItemRegistry.COPPER_ORE));
    }

    @Test
    void craftedResaleDerivedFromRecipeCosts() {
        // Boots: 2 copper + 2 iron + 2 flat = 8 + 6 + 2 = 16
        assertEquals(16, SellPrices.derivedCraftedResale(CraftingRecipes.COPPER_BOOTS));
        assertEquals(16, SellPrices.unitSellPrice(ItemRegistry.COPPER_BOOTS));

        // Helmet: 2 copper + 1 iron + 2 = 8 + 3 + 2 = 13
        assertEquals(13, SellPrices.derivedCraftedResale(CraftingRecipes.COPPER_HELMET));

        // Espada de Ferro: 20 iron + 10 copper + 2 = 60 + 40 + 2 = 102
        assertEquals(102, SellPrices.derivedCraftedResale(CraftingRecipes.COPPER_LONGSWORD));
    }

    @Test
    void potionsAreNotSellable() {
        assertFalse(SellPrices.isSellable(ItemRegistry.POTION_SMALL));
        assertEquals(-1, SellPrices.unitSellPrice(ItemRegistry.POTION_MEDIUM));
    }

    @Test
    void sellingOneIncreasesCurrencyAndDecreasesInventory() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.getInventory().addItem(ItemRegistry.IRON_ORE, 5);
        assertTrue(SellPrices.trySellOne(ItemRegistry.IRON_ORE, player.getInventory(), player.getWallet()));
        assertEquals(4, player.getInventory().getItemCount(ItemRegistry.IRON_ORE));
        assertEquals(3, player.getWallet().getTotalSilver());
    }
}
