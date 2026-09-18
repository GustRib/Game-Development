package com.donos.zebra.items;

/**
 * Player currency as a single silver total. Gold/silver display is a decomposition of that total
 * (100 Prata = 1 Ouro). Not an inventory item.
 */
public final class Wallet {

    public static final int SILVER_PER_GOLD = 100;

    private int totalSilver;

    public Wallet() {
        this(0);
    }

    public Wallet(int totalSilver) {
        this.totalSilver = Math.max(0, totalSilver);
    }

    public int getTotalSilver() {
        return totalSilver;
    }

    public int getGold() {
        return totalSilver / SILVER_PER_GOLD;
    }

    public int getSilverRemainder() {
        return totalSilver % SILVER_PER_GOLD;
    }

    public void addSilver(int amount) {
        if (amount <= 0) {
            return;
        }
        totalSilver += amount;
    }

    /** Absolute restore for save/load (clamped to &gt;= 0). */
    public void setTotalSilver(int totalSilver) {
        this.totalSilver = Math.max(0, totalSilver);
    }

    public boolean canAfford(int silverCost) {
        return silverCost >= 0 && totalSilver >= silverCost;
    }

    /**
     * Spends {@code silverCost} from the total (breaking gold as needed).
     * @return false if insufficient funds (unchanged)
     */
    public boolean trySpend(int silverCost) {
        if (!canAfford(silverCost)) {
            return false;
        }
        totalSilver -= silverCost;
        return true;
    }

    public String formatDisplay() {
        return "Ouro " + getGold() + "  |  Prata " + getSilverRemainder();
    }
}
