package com.donos.zebra.skills;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.AnimationConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillAttackTimingTest extends HeadlessTestBase {

    @Test
    void attackDurationMatchesWiredRunAttackSheet() {
        assertEquals(8, SkillAttackTiming.ATTACK_FRAME_COUNT);
        assertEquals(
            8 * AnimationConstants.ATTACK_FRAME_DURATION,
            SkillAttackTiming.attackDurationSeconds(),
            0.0001f);
        assertTrue(SkillAttackTiming.impactDelaySeconds() > 0f);
        assertTrue(SkillAttackTiming.impactDelaySeconds() < SkillAttackTiming.attackDurationSeconds());
        assertEquals(
            SkillAttackTiming.attackDurationSeconds() * SkillAttackTiming.IMPACT_NORMALIZED,
            SkillAttackTiming.impactDelaySeconds(),
            0.0001f);
    }
}
