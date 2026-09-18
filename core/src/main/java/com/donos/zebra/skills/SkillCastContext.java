package com.donos.zebra.skills;

import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Player;
import com.donos.zebra.skills.vfx.SkillEffectWorld;
import com.donos.zebra.skills.vfx.SkillVfxFactory;
import com.donos.zebra.world.CameraController;

import java.util.List;

/**
 * Shared cast environment for skill behaviors (keeps UI/rendering out of combat rules).
 */
public final class SkillCastContext {

    public final Player player;
    public final List<Entity> entities;
    public final List<DamageText> damageTexts;
    public final SkillEffectWorld effects;
    public final SkillVfxFactory vfxFactory;
    public final CameraController camera;

    public SkillCastContext(Player player,
                            List<Entity> entities,
                            List<DamageText> damageTexts) {
        this(player, entities, damageTexts, null, null, null);
    }

    public SkillCastContext(Player player,
                            List<Entity> entities,
                            List<DamageText> damageTexts,
                            SkillEffectWorld effects,
                            SkillVfxFactory vfxFactory,
                            CameraController camera) {
        this.player = player;
        this.entities = entities;
        this.damageTexts = damageTexts;
        this.effects = effects;
        this.vfxFactory = vfxFactory;
        this.camera = camera;
    }
}
