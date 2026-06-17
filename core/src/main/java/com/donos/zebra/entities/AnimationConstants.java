package com.donos.zebra.entities;

public final class AnimationConstants {

    public static final String ANIM_IDLE = "idle";
    public static final String ANIM_WALK = "walk";
    public static final String ANIM_ATTACK = "attack";

    public static final String IDLE_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Idle_with_shadow.png";
    public static final String WALK_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Walk_with_shadow.png";
    public static final String ATTACK_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Attack_with_shadow.png";

    public static final int IDLE_COLS = 12;
    public static final int WALK_COLS = 6;
    public static final int ATTACK_COLS = 8;
    public static final int DIRECTION_ROWS = 4;

    public static final float IDLE_FRAME_DURATION = 0.18f;
    public static final float WALK_FRAME_DURATION = 0.10f;
    public static final float ATTACK_FRAME_DURATION = 0.07f;

    private AnimationConstants() {
    }
}
