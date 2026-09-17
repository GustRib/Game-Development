package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.AnimationConstants;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.util.HealthBarRenderer;

import java.util.List;

/**
 * Player melee attacks, enemy AI ticks, floating damage text, and health bars.
 */
public final class CombatController {

    private static final float ATTACK_RANGE = 24f;
    /** Fallback / starter damage (matches equipped {@code IRON_SWORD}). Prefer {@link Player#getAttackDamage()}. */
    private static final float ATTACK_DAMAGE = 10f;

    private CombatController() {
    }

    public static float getAttackRange() {
        return ATTACK_RANGE;
    }

    /** Starter sword damage constant; combat resolution uses the player's equipped weapon. */
    public static float getAttackDamage() {
        return ATTACK_DAMAGE;
    }

    public static void resolvePlayerMelee(Player player, List<Entity> entities, List<DamageText> damageTexts) {
        resolvePlayerMelee(player, entities, damageTexts, Gdx.input.justTouched());
    }

    /**
     * Testable overload: {@code attackJustPressed} replaces {@code Gdx.input.justTouched()}.
     */
    public static void resolvePlayerMelee(Player player,
                                          List<Entity> entities,
                                          List<DamageText> damageTexts,
                                          boolean attackJustPressed) {
        if (player.isInteracting()) {
            return;
        }

        // Shared gate: unarmed click never starts the attack anim (Player.update);
        // surface the same warning here so feedback stays in one combat path.
        if (player.consumeUnarmedAttackFeedback()) {
            damageTexts.add(new DamageText(
                player.getX(), player.getY() + 18f, "Precisa de uma arma!", Color.YELLOW));
            return;
        }

        if (!player.getCurrentAnimationKey().equals(AnimationConstants.ANIM_ATTACK)) {
            return;
        }
        if (!attackJustPressed) {
            return;
        }

        if (!player.hasWeaponEquipped()) {
            damageTexts.add(new DamageText(
                player.getX(), player.getY() + 18f, "Precisa de uma arma!", Color.YELLOW));
            return;
        }

        float damage = player.getAttackDamage();
        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity ent = entities.get(i);
            if (ent instanceof Enemy && !ent.isDead()) {
                float distance = Vector2.dst(player.getX(), player.getY(), ent.getX(), ent.getY());
                if (distance <= ATTACK_RANGE) {
                    ent.takeDamage(damage);
                    damageTexts.add(new DamageText(
                        ent.getX(), ent.getY() + 15f, "-" + (int) damage, Color.RED));
                }
            }
        }
    }

    public static void updateEntities(Player player,
                                      List<Entity> entities,
                                      List<DamageText> damageTexts,
                                      float delta,
                                      Array<Polygon> collisionPolygons) {
        float playerOldHealth = player.getCurrentHealth();

        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity ent = entities.get(i);

            if (ent.isDead()) {
                if (ent instanceof Orc) {
                    ((Orc) ent).updateEnemy(player, delta, collisionPolygons);
                }
                continue;
            }

            if (ent instanceof Orc) {
                ((Orc) ent).updateEnemy(player, delta, collisionPolygons);

                if (player.getCurrentHealth() < playerOldHealth) {
                    float damageTaken = playerOldHealth - player.getCurrentHealth();
                    damageTexts.add(new DamageText(
                        player.getX(), player.getY() + 15f, "-" + (int) damageTaken, Color.ORANGE));
                    playerOldHealth = player.getCurrentHealth();
                }
            } else if (!(ent instanceof Player)) {
                ent.update(delta);
            }
        }
    }

    public static void updateDamageTexts(List<DamageText> damageTexts, float delta) {
        for (int i = damageTexts.size() - 1; i >= 0; i--) {
            DamageText dt = damageTexts.get(i);
            dt.update(delta);
            if (dt.lifetime <= 0) {
                damageTexts.remove(i);
            }
        }
    }

    public static void renderDamageTexts(SpriteBatch batch, BitmapFont font, List<DamageText> damageTexts) {
        for (DamageText dt : damageTexts) {
            font.setColor(dt.color);
            font.draw(batch, dt.text, dt.x - 5f, dt.y);
        }
    }

    public static void renderHealthBars(ShapeRenderer shapeRenderer, List<Entity> entities) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Entity ent : entities) {
            if (ent instanceof Orc && !ent.isDead()) {
                Orc orc = (Orc) ent;
                HealthBarRenderer.draw(
                    shapeRenderer, orc.getX(), orc.getY() + 14f, 20f, 4f,
                    orc.getCurrentHealth(), orc.getMaxHealth());
            } else if (ent instanceof Player) {
                Player p = (Player) ent;
                HealthBarRenderer.draw(
                    shapeRenderer, p.getX(), p.getY() + 16f, 20f, 4f,
                    p.getCurrentHealth(), p.getMaxHealth());
            }
        }

        shapeRenderer.end();
    }
}
