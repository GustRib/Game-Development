package com.donos.zebra.skills;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Entity;

/**
 * 360° AoE around the player. Visuals owned by {@link com.donos.zebra.skills.vfx.WhirlwindRingEffect}.
 */
public final class WhirlwindBehavior implements SkillBehavior {

    public static final float RADIUS = 40f;

    @Override
    public void execute(SkillCastContext context) {
        float damage = context.player.getAttackDamage();
        if (context.effects != null && context.vfxFactory != null) {
            context.effects.spawn(context.vfxFactory.createWhirlwind(context.player, RADIUS));
        }
        if (context.camera != null) {
            context.camera.shake(0.12f, 2.2f);
        }
        for (Entity ent : context.entities) {
            if (!(ent instanceof Enemy) || ent.isDead()) {
                continue;
            }
            float dist = Vector2.dst(
                context.player.getX(), context.player.getY(), ent.getX(), ent.getY());
            if (dist <= RADIUS) {
                ent.takeDamage(damage);
                if (context.damageTexts != null) {
                    context.damageTexts.add(new DamageText(
                        ent.getX(), ent.getY() + 15f, "-" + (int) damage, Color.CYAN));
                }
            }
        }
    }
}
