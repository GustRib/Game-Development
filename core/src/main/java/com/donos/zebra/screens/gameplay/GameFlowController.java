package com.donos.zebra.screens.gameplay;

/**
 * Centralized GameScreen flow transitions (testable without LibGDX input).
 */
public final class GameFlowController {

    private GameFlowState state = GameFlowState.PLAYING;

    public GameFlowState getState() {
        return state;
    }

    public void setState(GameFlowState state) {
        this.state = state == null ? GameFlowState.PLAYING : state;
    }

    public boolean isPlaying() {
        return state == GameFlowState.PLAYING;
    }

    /** Pause / options: freeze all gameplay simulation. */
    public boolean isOverlayPause() {
        return state == GameFlowState.PAUSED || state == GameFlowState.OPTIONS;
    }

    /** World entities / craft / combat (not player death anim). */
    public boolean isWorldFrozen() {
        return isOverlayPause()
            || state == GameFlowState.DYING
            || state == GameFlowState.DEAD;
    }

    /** Player body simulation frozen (still render). Death anim uses {@link #allowsDeathAnimationTick()}. */
    public boolean isPlayerSimulationFrozen() {
        return isOverlayPause() || state == GameFlowState.DEAD;
    }

    public boolean allowsDeathAnimationTick() {
        return state == GameFlowState.DYING;
    }

    public boolean canOpenPause() {
        return state == GameFlowState.PLAYING;
    }

    /** ESC while playing with no inventory panels → pause. */
    public void openPause() {
        if (canOpenPause()) {
            state = GameFlowState.PAUSED;
        }
    }

    public void resumeFromPause() {
        if (state == GameFlowState.PAUSED || state == GameFlowState.OPTIONS) {
            state = GameFlowState.PLAYING;
        }
    }

    public void openOptions() {
        if (state == GameFlowState.PAUSED) {
            state = GameFlowState.OPTIONS;
        }
    }

    public void backFromOptions() {
        if (state == GameFlowState.OPTIONS) {
            state = GameFlowState.PAUSED;
        }
    }

    /**
     * ESC priority for pause stack (not inventory panels).
     * @return true if ESC was consumed
     */
    public boolean handlePauseEsc() {
        if (state == GameFlowState.OPTIONS) {
            backFromOptions();
            return true;
        }
        if (state == GameFlowState.PAUSED) {
            resumeFromPause();
            return true;
        }
        if (state == GameFlowState.PLAYING) {
            openPause();
            return true;
        }
        return false;
    }

    public void onPlayerDied() {
        if (state == GameFlowState.PLAYING
            || state == GameFlowState.PAUSED
            || state == GameFlowState.OPTIONS) {
            state = GameFlowState.DYING;
        }
    }

    public void onDeathUiReady() {
        if (state == GameFlowState.DYING) {
            state = GameFlowState.DEAD;
        }
    }

    public void onRevived() {
        state = GameFlowState.PLAYING;
    }
}
