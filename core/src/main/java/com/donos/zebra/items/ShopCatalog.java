package com.donos.zebra.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mentor shop offers (instant purchase, potions only).
 */
public final class ShopCatalog {

    public static final class Offer {
        public final ItemDefinition item;
        public final int priceSilver;

        public Offer(ItemDefinition item, int priceSilver) {
            this.item = item;
            this.priceSilver = priceSilver;
        }
    }

    private static final List<Offer> POTION_OFFERS;

    static {
        List<Offer> offers = new ArrayList<>();
        offers.add(new Offer(ItemRegistry.POTION_SMALL, 10));
        offers.add(new Offer(ItemRegistry.POTION_MEDIUM, 25));
        offers.add(new Offer(ItemRegistry.POTION_LARGE, 40));
        POTION_OFFERS = Collections.unmodifiableList(offers);
    }

    private ShopCatalog() {
    }

    public static List<Offer> potionOffers() {
        return POTION_OFFERS;
    }

    /**
     * Instant buy: deduct currency, add one potion to inventory (stacking rules apply).
     * @return false if cannot afford or inventory full
     */
    public static boolean tryBuy(Offer offer, Wallet wallet, Inventory inventory) {
        if (offer == null || wallet == null || inventory == null) {
            return false;
        }
        if (!wallet.canAfford(offer.priceSilver)) {
            return false;
        }
        if (!wallet.trySpend(offer.priceSilver)) {
            return false;
        }
        if (!inventory.addItem(offer.item, 1)) {
            wallet.addSilver(offer.priceSilver); // refund if inventory rejected
            return false;
        }
        return true;
    }
}
