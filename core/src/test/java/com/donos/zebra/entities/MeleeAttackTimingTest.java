package com.donos.zebra.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeleeAttackTimingTest {

    @Test
    void standingTimingMatchesAttackSheet() {
        assertEquals(8, MeleeAttackTiming.STANDING_FRAME_COUNT);
        assertEquals(8 * AnimationConstants.ATTACK_FRAME_DURATION,
            MeleeAttackTiming.standingDurationSeconds(), 0.0001f);
        assertTrue(MeleeAttackTiming.standingImpactDelaySeconds()
            < MeleeAttackTiming.standingDurationSeconds());
    }

    @Test
    void walkTimingMatchesWalkAttackSheet() {
        assertEquals(6, MeleeAttackTiming.WALK_FRAME_COUNT);
        assertEquals(6 * AnimationConstants.ATTACK_FRAME_DURATION,
            MeleeAttackTiming.walkDurationSeconds(), 0.0001f);
    }
}
