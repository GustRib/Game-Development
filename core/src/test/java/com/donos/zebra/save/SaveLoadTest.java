package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.items.ItemRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveLoadTest extends HeadlessTestBase {

    private final SaveService saveService = new SaveService();

    @BeforeEach
    @AfterEach
    void clean() {
        saveService.clearAllSlots();
    }

    @Test
    void saveAndRestorePositionHpInventoryEquipmentCurrency() {
        Player player = newPlayer("Gu");
        player.setPosition(123f, 456f);
        player.setCurrentHealth(55f);
        player.getWallet().addSilver(250);
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 3);
        player.getInventory().addItem(ItemRegistry.IRON_ORE, 2);
        player.equipWeapon(ItemRegistry.IRON_SWORD);
        player.equipArmor(ItemRegistry.COPPER_HELMET);

        SaveData data = new SaveData();
        data.player = SaveStateMapper.capturePlayer(player, false, 0f, 0f);
        data.quest = new QuestSaveData();
        data.world = new WorldSaveData();
        assertTrue(saveService.writeSlot(1, data));

        SaveData loaded = saveService.readSlot(1);
        assertNotNull(loaded);

        Player restored = newPlayer("Temp");
        SaveStateMapper.applyPlayer(loaded.player, restored);

        assertEquals("Gu", restored.getCharacterName());
        assertEquals(123f, restored.getX(), 0.01f);
        assertEquals(456f, restored.getY(), 0.01f);
        assertEquals(55f, restored.getCurrentHealth(), 0.01f);
        assertEquals(250, restored.getWallet().getTotalSilver());
        assertEquals(3, restored.getInventory().getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(2, restored.getInventory().getItemCount(ItemRegistry.IRON_ORE));
        assertEquals(ItemRegistry.IRON_SWORD.getId(), restored.getEquippedWeapon().getId());
        assertEquals(ItemRegistry.COPPER_HELMET.getId(), restored.getEquippedHelmet().getId());
    }

    private static Player newPlayer(String name) {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setCharacterName(name);
        return player;
    }
}
