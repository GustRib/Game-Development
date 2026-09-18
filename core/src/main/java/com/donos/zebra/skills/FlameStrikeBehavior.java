package com.donos.zebra.skills;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Direction;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Player;
import com.donos.zebra.skills.vfx.FlameStrikeSlashEffect;

/**
 * Front-facing fire slash. Visuals are owned by {@link FlameStrikeSlashEffect}; this class applies gameplay.
 */
public final class FlameStrikeBehavior implements SkillBehavior {

    public static final float RANGE = 30f;
    public static final float HALF_ARC_DEGREES = 55f;
    public static final float DAMAGE_MULTIPLIER = 1.25f;

    private FlameStrikeSlashEffect activeVisual;

    @Override
    public void execute(SkillCastContext context) {
        beginCast(context, facingDegrees(context.player.getFacingDirection()));
        applyImpact(context, facingDegrees(context.player.getFacingDirection()));
    }

    public void beginCast(SkillCastContext context, float facingDegrees) {
        if (context == null || context.player == null) {
            return;
        }
        activeVisual = null;
        if (context.effects != null && context.vfxFactory != null) {
            activeVisual = context.vfxFactory.createFlameStrike(context.player, facingDegrees);
            context.effects.spawn(activeVisual);
        }
    }

    public void applyImpact(SkillCastContext context, float facingDegrees) {
        if (context == null || context.player == null) {
            return;
        }
        Player player = context.player;
        float damage = player.getAttackDamage() * DAMAGE_MULTIPLIER;

        if (activeVisual != null) {
            activeVisual.notifyImpact();
        }
        if (context.camera != null) {
            context.camera.shake(0.14f, 2.8f);
        }

        for (Entity ent : context.entities) {
            if (!(ent instanceof Enemy) || ent.isDead()) {
                continue;
            }
            Enemy enemy = (Enemy) ent;
            if (!isInFlameArc(player, enemy, facingDegrees)) {
                continue;
            }
            enemy.takeDamage(damage, null, SkillRegistry.FLAME_STRIKE_ID);
            enemy.getStatusEffects().apply(new BurningStatus(SkillRegistry.FLAME_STRIKE_ID));
            if (context.effects != null && context.vfxFactory != null) {
                context.effects.spawn(context.vfxFactory.createBurningAura(enemy));
            }
            if (context.damageTexts != null) {
                context.damageTexts.add(new DamageText(
                    enemy.getX(), enemy.getY() + 15f, "-" + (int) damage, new Color(1f, 0.4f, 0.1f, 1f)));
            }
        }
        activeVisual = null;
    }

    public static boolean isInFlameArc(Player player, Enemy enemy, float facingDegrees) {
        float dist = Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY());
        if (dist > RANGE) {
            return false;
        }
        if (dist <= 8f) {
            return true;
        }
        float toEnemy = MathUtils.atan2(enemy.getY() - player.getY(), enemy.getX() - player.getX())
            * MathUtils.radiansToDegrees;
        float deltaAng = Math.abs(MathUtils.atan2(
            MathUtils.sinDeg(toEnemy - facingDegrees),
            MathUtils.cosDeg(toEnemy - facingDegrees)) * MathUtils.radiansToDegrees);
        return deltaAng <= HALF_ARC_DEGREES;
    }

    public static float facingDegrees(Direction direction) {
        if (direction == null) {
            return 90f;
        }
        switch (direction) {
            case RIGHT:
                return 0f;
            case UP:
                return 90f;
            case LEFT:
                return 180f;
            case DOWN:
            default:
                return 270f;
        }
    }
}
