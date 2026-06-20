package com.donos.zebra.entities;

public final class AnimationConstants {

    public static final String ANIM_IDLE = "idle";
    public static final String ANIM_WALK = "walk";
    public static final String ANIM_RUN = "run";
    public static final String ANIM_ATTACK = "attack";

    public static final String IDLE_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Idle_with_shadow.png";
    public static final String WALK_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Walk_with_shadow.png";
    public static final String RUN_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Run_with_shadow.png";
    public static final String ATTACK_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Run_Attack_with_shadow.png";
    
    // ADICIONADO: Caminhos corretos baseados na estrutura do seu projeto
    public static final String HURT_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Hurt_with_shadow.png";
    public static final String DEATH_SHEET_PATH = "characters/Player1/Swordsman_lvl1_Death_with_shadow.png";

    public static final int DIRECTION_ROWS = 4;

    public static final float IDLE_FRAME_DURATION = 0.18f;
    public static final float WALK_FRAME_DURATION = 0.10f;
    public static final float RUN_FRAME_DURATION = 0.08f;
    public static final float ATTACK_FRAME_DURATION = 0.07f;

    private AnimationConstants() {
    }
}