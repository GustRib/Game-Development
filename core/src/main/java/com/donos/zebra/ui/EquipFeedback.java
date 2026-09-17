package com.donos.zebra.ui;

/**
 * Builds short equip/unequip confirmation strings (logic only; rendering is separate).
 */
public final class EquipFeedback {

    private EquipFeedback() {
    }

    public static String forEquip(com.donos.zebra.items.ItemDefinition item) {
        if (item == null) {
            return "";
        }
        if (item.getAttackDamage() > 0) {
            return item.getName() + " equipado (+" + (int) item.getAttackDamage() + " Dano)";
        }
        if (item.getDefense() > 0) {
            return item.getName() + " equipado (+" + (int) item.getDefense() + " Defesa)";
        }
        return item.getName() + " equipado";
    }

    public static String forUnequip(com.donos.zebra.items.ItemDefinition item) {
        if (item == null) {
            return "";
        }
        if (item.getAttackDamage() > 0) {
            return item.getName() + " removido (-" + (int) item.getAttackDamage() + " Dano)";
        }
        if (item.getDefense() > 0) {
            return item.getName() + " removido (-" + (int) item.getDefense() + " Defesa)";
        }
        return item.getName() + " removido";
    }
}
