package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerNameTest extends HeadlessTestBase {

    @Test
    void loadedCharacterNameIsPreserved() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setCharacterName("Gu");

        PlayerSaveData data = SaveStateMapper.capturePlayer(player, false, 0f, 0f);
        assertEquals("Gu", data.characterName);

        Player restored = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        SaveStateMapper.applyPlayer(data, restored);
        assertEquals("Gu", restored.getCharacterName());
    }

    @Test
    void playerNameIsAssociatedWithSaveSession() {
        SaveData save = new SaveData();
        save.player.characterName = "Gu";
        GameSession session = GameSession.continueFrom(save, 1);
        assertEquals("Gu", session.getCharacterName());
        assertEquals(1, session.getActiveSlot());
    }
}
