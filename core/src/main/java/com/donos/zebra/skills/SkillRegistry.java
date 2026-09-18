package com.donos.zebra.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Static catalog of skills. Both starter skills are unlocked by default.
 */
public final class SkillRegistry {

    public static final String WHIRLWIND_ID = "whirlwind";
    public static final String FLAME_STRIKE_ID = "flame_strike";

    public static final String WHIRLWIND_ICON = "skills/whirlwind_skill.png";
    public static final String FLAME_STRIKE_ICON = "skills/flamestrike_skill.png";

    public static final SkillDefinition WHIRLWIND = new SkillDefinition(
        WHIRLWIND_ID,
        "Redemoinho",
        "Gire rapidamente, atingindo inimigos ao seu redor.",
        5f,
        true,
        "AoE circular · Dano = ataque equipado",
        WHIRLWIND_ICON
    );

    public static final SkillDefinition FLAME_STRIKE = new SkillDefinition(
        FLAME_STRIKE_ID,
        "Golpe Flamejante",
        "Golpe frontal de fogo que causa dano e aplica Queimadura.",
        6f,
        true,
        "Dano inicial + Queimadura 2s",
        FLAME_STRIKE_ICON
    );

    private static final Map<String, SkillDefinition> BY_ID = new LinkedHashMap<>();

    static {
        register(WHIRLWIND);
        register(FLAME_STRIKE);
    }

    private SkillRegistry() {
    }

    private static void register(SkillDefinition def) {
        BY_ID.put(def.getId(), def);
    }

    public static SkillDefinition get(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return BY_ID.get(id);
    }

    public static List<SkillDefinition> all() {
        return Collections.unmodifiableList(new ArrayList<>(BY_ID.values()));
    }

    public static boolean isKnown(String id) {
        return get(id) != null;
    }

    /** Queue skill icon textures on the shared gameplay AssetManager. */
    public static void queueIconAssets(com.badlogic.gdx.assets.AssetManager assetManager) {
        if (assetManager == null) {
            return;
        }
        for (SkillDefinition def : BY_ID.values()) {
            if (def.getIconPath() != null && !def.getIconPath().isEmpty()) {
                assetManager.load(def.getIconPath(), com.badlogic.gdx.graphics.Texture.class);
            }
        }
    }
}
