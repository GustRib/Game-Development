package com.donos.zebra.items;

import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopCatalogTest {

    @Test
    void purchaseDeductsCurrencyAndAddsPotionToInventory() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.getWallet().addSilver(50);
        ShopCatalog.Offer offer = ShopCatalog.potionOffers().get(0); // small 10
        assertTrue(ShopCatalog.tryBuy(offer, player.getWallet(), player.getInventory()));
        assertEquals(40, player.getWallet().getTotalSilver());
        assertEquals(1, player.getInventory().getItemCount(ItemRegistry.POTION_SMALL));
        assertNullWeaponAndNoAutoEquip(player);
    }

    @Test
    void purchaseBlockedWhenBroke() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.getWallet().addSilver(10);
        ShopCatalog.Offer offer = ShopCatalog.potionOffers().get(1); // medium 25
        assertFalse(ShopCatalog.tryBuy(offer, player.getWallet(), player.getInventory()));
        assertEquals(10, player.getWallet().getTotalSilver());
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.POTION_MEDIUM));
    }

    @Test
    void purchaseStacksUntilNinetyNineThenNewStack() {
        Inventory inv = new Inventory(5);
        Wallet wallet = new Wallet(10_000);
        ShopCatalog.Offer offer = ShopCatalog.potionOffers().get(0);
        for (int i = 0; i < 99; i++) {
            assertTrue(ShopCatalog.tryBuy(offer, wallet, inv));
        }
        assertEquals(99, inv.getStackAt(0).getQuantity());
        assertTrue(ShopCatalog.tryBuy(offer, wallet, inv));
        assertEquals(99, inv.getStackAt(0).getQuantity());
        assertEquals(1, inv.getStackAt(1).getQuantity());
    }

    private static void assertNullWeaponAndNoAutoEquip(Player player) {
        // Shop must not auto-equip potions into the belt.
        assertNull(player.getPotionSlot());
    }
}
