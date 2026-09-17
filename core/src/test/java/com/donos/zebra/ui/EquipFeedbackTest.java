package com.donos.zebra.ui;

import com.donos.zebra.items.ItemRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipFeedbackTest {

    @Test
    void equipAndUnequipMessagesIncludeNameAndStatDelta() {
        String equipWeapon = EquipFeedback.forEquip(ItemRegistry.COPPER_LONGSWORD);
        assertTrue(equipWeapon.contains(ItemRegistry.COPPER_LONGSWORD.getName()));
        assertTrue(equipWeapon.contains("+" + (int) ItemRegistry.COPPER_LONGSWORD.getAttackDamage()));
        assertTrue(equipWeapon.contains("Dano"));

        String unequipArmor = EquipFeedback.forUnequip(ItemRegistry.COPPER_CHESTPLATE);
        assertTrue(unequipArmor.contains(ItemRegistry.COPPER_CHESTPLATE.getName()));
        assertTrue(unequipArmor.contains("-" + (int) ItemRegistry.COPPER_CHESTPLATE.getDefense()));
        assertTrue(unequipArmor.contains("Defesa"));

        assertEquals("", EquipFeedback.forEquip(null));
        assertEquals("", EquipFeedback.forUnequip(null));
    }
}
