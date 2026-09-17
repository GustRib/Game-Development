package com.donos.zebra.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletTest {

    @Test
    void addingSilverConvertsToGoldAtHundredAndKeepsRemainder() {
        Wallet wallet = new Wallet();
        wallet.addSilver(100);
        assertEquals(1, wallet.getGold());
        assertEquals(0, wallet.getSilverRemainder());
        assertEquals(100, wallet.getTotalSilver());

        wallet.addSilver(250);
        assertEquals(3, wallet.getGold());
        assertEquals(50, wallet.getSilverRemainder());
        assertEquals(350, wallet.getTotalSilver());
    }

    @Test
    void spendingBreaksGoldIntoSilverCorrectly() {
        Wallet wallet = new Wallet(105); // 1 gold + 5 silver
        assertTrue(wallet.trySpend(30));
        assertEquals(0, wallet.getGold());
        assertEquals(75, wallet.getSilverRemainder());
        assertEquals(75, wallet.getTotalSilver());
    }

    @Test
    void insufficientFundsLeavesWalletUnchanged() {
        Wallet wallet = new Wallet(20);
        assertFalse(wallet.trySpend(30));
        assertEquals(20, wallet.getTotalSilver());
    }
}
