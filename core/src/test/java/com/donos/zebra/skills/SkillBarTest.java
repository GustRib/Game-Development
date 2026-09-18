package com.donos.zebra.skills;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkillBarTest extends HeadlessTestBase {

    @Test
    void emptySlotsDoNothingAndAssignedSlotsActivate() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.getSkillBook().unlock(SkillRegistry.WHIRLWIND_ID);
        player.getSkillBook().unlock(SkillRegistry.FLAME_STRIKE_ID);
        SkillBook book = player.getSkillBook();
        book.getBar().clearSlot(2);
        book.getBar().clearSlot(3);

        SkillCaster caster = new SkillCaster();
        SkillCastContext ctx = new SkillCastContext(
            player, new ArrayList<Entity>(), new ArrayList<>());

        assertEquals(SkillCaster.CastResult.EMPTY_SLOT, caster.tryActivateSlot(2, book, ctx));
        assertEquals(SkillCaster.CastResult.OK, caster.tryActivateSlot(0, book, ctx));
    }

    @Test
    void skillCanBeAssignedAndReplacedOnAnySlot() {
        SkillBar bar = new SkillBar();
        bar.setSlot(3, SkillRegistry.WHIRLWIND_ID);
        assertEquals(SkillRegistry.WHIRLWIND_ID, bar.getSlotId(3));

        bar.setSlot(3, SkillRegistry.FLAME_STRIKE_ID);
        assertEquals(SkillRegistry.FLAME_STRIKE_ID, bar.getSlotId(3));

        bar.clearSlot(3);
        assertNull(bar.getSlotId(3));
    }

    @Test
    void defaultLoadoutUsesFirstTwoSlots() {
        SkillBar bar = new SkillBar();
        assertEquals(SkillRegistry.WHIRLWIND_ID, bar.getSlotId(0));
        assertEquals(SkillRegistry.FLAME_STRIKE_ID, bar.getSlotId(1));
        assertNull(bar.getSlotId(2));
        assertNull(bar.getSlotId(3));
    }
}
