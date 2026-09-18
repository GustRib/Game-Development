package com.donos.zebra.screens.gameplay;

/**
 * Pure state machine for death overlay timing (no rendering).
 * Allows death animation to finish before interactive UI.
 */
public final class DeathScreenController {

    public static final float POST_ANIM_DELAY_SECONDS = 0.45f;

    public enum Phase {
        /** Not in a death sequence. */
        INACTIVE,
        /** Waiting for death animation (+ optional post delay). */
        WAITING_ANIM,
        /** Overlay interactive (R / ESC). */
        INTERACTIVE
    }

    private Phase phase = Phase.INACTIVE;
    private float postAnimTimer;
    private float overlayAlpha;
    private float titleAlpha;
    private float instructionsAlpha;

    public Phase getPhase() {
        return phase;
    }

    public boolean isInteractive() {
        return phase == Phase.INTERACTIVE;
    }

    public boolean isActive() {
        return phase != Phase.INACTIVE;
    }

    public float getOverlayAlpha() {
        return overlayAlpha;
    }

    public float getTitleAlpha() {
        return titleAlpha;
    }

    public float getInstructionsAlpha() {
        return instructionsAlpha;
    }

    /** Call when player first becomes dead. */
    public void begin() {
        phase = Phase.WAITING_ANIM;
        postAnimTimer = 0f;
        overlayAlpha = 0f;
        titleAlpha = 0f;
        instructionsAlpha = 0f;
    }

    public void reset() {
        phase = Phase.INACTIVE;
        postAnimTimer = 0f;
        overlayAlpha = 0f;
        titleAlpha = 0f;
        instructionsAlpha = 0f;
    }

    /**
     * @param deathAnimFinished from {@code Player.isDeathAnimationFinished()}
     * @param uiDelta screen/UI time (not frozen while gameplay is otherwise paused)
     */
    public void update(boolean deathAnimFinished, float uiDelta) {
        if (phase == Phase.INACTIVE) {
            return;
        }

        if (phase == Phase.WAITING_ANIM) {
            // Darken gradually during the fall.
            overlayAlpha = Math.min(0.82f, overlayAlpha + uiDelta * 0.9f);
            titleAlpha = Math.min(0.35f, titleAlpha + uiDelta * 0.4f);

            if (deathAnimFinished) {
                postAnimTimer += uiDelta;
                if (postAnimTimer >= POST_ANIM_DELAY_SECONDS) {
                    phase = Phase.INTERACTIVE;
                }
            }
            return;
        }

        // INTERACTIVE — finish fade-ins
        overlayAlpha = Math.min(0.88f, overlayAlpha + uiDelta * 1.2f);
        titleAlpha = Math.min(1f, titleAlpha + uiDelta * 2f);
        instructionsAlpha = Math.min(1f, instructionsAlpha + uiDelta * 1.6f);
    }
}
