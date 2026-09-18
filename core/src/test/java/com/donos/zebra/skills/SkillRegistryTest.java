package com.donos.zebra.skills;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillRegistryTest extends HeadlessTestBase {

    @Test
    void bothSkillsExistWithUniqueIdsAndDescriptions() {
        assertNotNull(SkillRegistry.get(SkillRegistry.WHIRLWIND_ID));
        assertNotNull(SkillRegistry.get(SkillRegistry.FLAME_STRIKE_ID));
        assertEquals(2, SkillRegistry.all().size());

        Set<String> ids = new HashSet<>();
        for (SkillDefinition def : SkillRegistry.all()) {
            assertTrue(ids.add(def.getId()));
            assertNotNull(def.getName());
            assertFalse(def.getName().isEmpty());
            assertNotNull(def.getDescription());
            assertFalse(def.getDescription().isEmpty());
            assertTrue(def.getCooldownSeconds() > 0f);
            assertNotNull(def.getIconPath());
            assertFalse(def.getIconPath().isEmpty());
            assertTrue(def.getIconPath().startsWith("skills/"));
        }
    }

    @Test
    void skillDefinitionsExposeCanonicalIconPaths() {
        assertEquals(SkillRegistry.WHIRLWIND_ICON, SkillRegistry.WHIRLWIND.getIconPath());
        assertEquals(SkillRegistry.FLAME_STRIKE_ICON, SkillRegistry.FLAME_STRIKE.getIconPath());
        assertEquals("skills/whirlwind_skill.png", SkillRegistry.WHIRLWIND.getIconPath());
        assertEquals("skills/flamestrike_skill.png", SkillRegistry.FLAME_STRIKE.getIconPath());
    }

    @Test
    void bothSkillsLockedInitiallyInSkillBook() {
        SkillBook book = new SkillBook();
        assertFalse(book.getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertFalse(book.getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());
        assertEquals(0, book.unlockedDefinitions().size());
        book.unlock(SkillRegistry.WHIRLWIND_ID);
        assertTrue(book.getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertEquals(1, book.unlockedDefinitions().size());
    }
}
