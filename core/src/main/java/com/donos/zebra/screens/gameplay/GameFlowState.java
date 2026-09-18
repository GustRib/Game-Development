package com.donos.zebra.screens.gameplay;

/**
 * High-level GameScreen flow. Only one overlay/mode is active at a time.
 */
public enum GameFlowState {
    /** Normal gameplay; inventory/craft panels may still be open. */
    PLAYING,
    /** Pause menu open — gameplay simulation frozen. */
    PAUSED,
    /** Options submenu under pause — gameplay still frozen. */
    OPTIONS,
    /** Quest journal / contextual map — gameplay simulation frozen. */
    QUESTS,
    /** Player HP hit zero; death animation playing; no revive UI yet. */
    DYING,
    /** Death animation done; revive/exit UI interactive. */
    DEAD
}
