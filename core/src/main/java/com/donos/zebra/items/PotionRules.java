package com.donos.zebra.items;

/**
 * Shared potion consume rules (heal clamp, full-HP block, cooldown).
 */
public final class PotionRules {

    public static final float COOLDOWN_SECONDS = 10f;

    public enum Result {
        OK,
        FULL_HP,
        ON_COOLDOWN,
        NO_POTION
    }

    private PotionRules() {
    }

    public static boolean isPotion(ItemDefinition item) {
        return item != null
            && item.getType() == ItemType.CONSUMABLE
            && item.getHealAmount() > 0;
    }

    public static String feedback(Result result, float cooldownRemaining) {
        switch (result) {
            case FULL_HP:
                return "Vida cheia";
            case ON_COOLDOWN:
                int secs = Math.max(1, (int) Math.ceil(cooldownRemaining));
                return "Pocao em recarga (" + secs + "s)";
            case NO_POTION:
                return "Nenhuma pocao equipada";
            case OK:
            default:
                return "";
        }
    }
}
