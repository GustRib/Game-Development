package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterCreationTest extends HeadlessTestBase {

    @Test
    void emptyNameIsRejected() {
        assertNull(CharacterNameValidator.validateAndNormalize(""));
        assertNull(CharacterNameValidator.validateAndNormalize("   "));
        assertNull(CharacterNameValidator.validateAndNormalize(null));
        assertFalse(CharacterNameValidator.isValid(""));
    }

    @Test
    void validNameCreatesNormalizedCharacterName() {
        String name = CharacterNameValidator.validateAndNormalize("  Gu  ");
        assertEquals("Gu", name);
        assertTrue(CharacterNameValidator.isValid("Aventureiro"));
    }

    @Test
    void characterNameIsStoredOnSession() {
        String name = CharacterNameValidator.validateAndNormalize("Gu");
        assertNotNull(name);
        GameSession session = GameSession.newGame(name);
        assertEquals("Gu", session.getCharacterName());
        assertFalse(session.shouldApplySave());
    }

    @Test
    void rejectsTooLongAndInvalidChars() {
        assertNull(CharacterNameValidator.validateAndNormalize("ABCDEFGHIJKLMNOPQ"));
        assertNull(CharacterNameValidator.validateAndNormalize("Bad@Name"));
    }
}
