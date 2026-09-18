package com.donos.zebra.screens.gameplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameFlowControllerTest {

    @Test
    void escOpensAndClosesPauseWithoutDoubleTransition() {
        GameFlowController flow = new GameFlowController();
        assertTrue(flow.handlePauseEsc());
        assertEquals(GameFlowState.PAUSED, flow.getState());
        assertTrue(flow.isWorldFrozen());

        assertTrue(flow.handlePauseEsc());
        assertEquals(GameFlowState.PLAYING, flow.getState());
        assertFalse(flow.isWorldFrozen());
    }

    @Test
    void optionsEscReturnsToPauseNotPlaying() {
        GameFlowController flow = new GameFlowController();
        flow.openPause();
        flow.openOptions();
        assertEquals(GameFlowState.OPTIONS, flow.getState());
        assertTrue(flow.handlePauseEsc());
        assertEquals(GameFlowState.PAUSED, flow.getState());
    }

    @Test
    void deathBlocksPauseAndFreezesWorld() {
        GameFlowController flow = new GameFlowController();
        flow.onPlayerDied();
        assertEquals(GameFlowState.DYING, flow.getState());
        assertTrue(flow.isWorldFrozen());
        assertFalse(flow.isPlayerSimulationFrozen());
        assertTrue(flow.allowsDeathAnimationTick());
        assertFalse(flow.handlePauseEsc());

        flow.onDeathUiReady();
        assertEquals(GameFlowState.DEAD, flow.getState());
        assertTrue(flow.isPlayerSimulationFrozen());

        flow.onRevived();
        assertEquals(GameFlowState.PLAYING, flow.getState());
    }
}
