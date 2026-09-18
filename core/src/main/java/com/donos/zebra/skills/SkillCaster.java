package com.donos.zebra.skills;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves hotbar activation into skill behaviors. Keeps cooldown/weapon gates in one place.
 * Flame Strike uses a delayed impact synchronized with {@link SkillAttackTiming}.
 */
public final class SkillCaster {

    public enum CastResult {
        OK,
        EMPTY_SLOT,
        UNKNOWN_SKILL,
        LOCKED,
        ON_COOLDOWN,
        NEEDS_WEAPON,
        BLOCKED
    }

    private final Map<String, SkillBehavior> behaviors = new HashMap<>();
    private final FlameStrikeBehavior flameStrike = new FlameStrikeBehavior();
    private PendingSkillCast pending;

    public SkillCaster() {
        behaviors.put(SkillRegistry.WHIRLWIND_ID, new WhirlwindBehavior());
        behaviors.put(SkillRegistry.FLAME_STRIKE_ID, flameStrike);
    }

    public CastResult tryActivateSlot(int slotIndex, SkillBook book, SkillCastContext context) {
        if (book == null || context == null || context.player == null) {
            return CastResult.BLOCKED;
        }
        if (context.player.isDead() || context.player.isInteracting()) {
            return CastResult.BLOCKED;
        }
        String skillId = book.getBar().getSlotId(slotIndex);
        if (skillId == null || skillId.isEmpty()) {
            return CastResult.EMPTY_SLOT;
        }
        return tryCast(skillId, book, context);
    }

    public CastResult tryCast(String skillId, SkillBook book, SkillCastContext context) {
        if (book == null || context == null || context.player == null) {
            return CastResult.BLOCKED;
        }
        if (context.player.isDead() || context.player.isInteracting()) {
            return CastResult.BLOCKED;
        }
        if (pending != null && pending.isActive()) {
            return CastResult.BLOCKED;
        }
        SkillRuntime runtime = book.getRuntime(skillId);
        if (runtime == null) {
            return CastResult.UNKNOWN_SKILL;
        }
        if (!runtime.isUnlocked()) {
            return CastResult.LOCKED;
        }
        if (!runtime.isReady()) {
            return CastResult.ON_COOLDOWN;
        }
        SkillDefinition def = runtime.getDefinition();
        if (def.requiresWeapon() && !context.player.hasWeaponEquipped()) {
            return CastResult.NEEDS_WEAPON;
        }
        SkillBehavior behavior = behaviors.get(skillId);
        if (behavior == null) {
            return CastResult.UNKNOWN_SKILL;
        }

        context.player.triggerSkillAttackAnimation();
        runtime.startCooldown();

        if (SkillRegistry.FLAME_STRIKE_ID.equals(skillId)) {
            float facing = FlameStrikeBehavior.facingDegrees(context.player.getFacingDirection());
            flameStrike.beginCast(context, facing);
            pending = new PendingSkillCast(
                skillId,
                SkillAttackTiming.impactDelaySeconds(),
                SkillAttackTiming.attackDurationSeconds(),
                facing);
            return CastResult.OK;
        }

        behavior.execute(context);
        return CastResult.OK;
    }

    /**
     * Advances delayed casts using gameplay ({@code worldDelta}) time.
     */
    public void update(float delta, SkillCastContext context) {
        if (pending == null || delta <= 0f) {
            return;
        }
        pending.elapsed += delta;
        if (!pending.impactApplied && pending.elapsed >= pending.impactDelay) {
            pending.impactApplied = true;
            if (SkillRegistry.FLAME_STRIKE_ID.equals(pending.skillId)) {
                flameStrike.applyImpact(context, pending.facingDegrees);
            }
        }
        if (!pending.isActive()) {
            pending = null;
        }
    }

    public PendingSkillCast getPendingCast() {
        return pending;
    }

    public boolean hasActiveCast() {
        return pending != null && pending.isActive();
    }

    public boolean hasAppliedImpact() {
        return pending != null && pending.impactApplied;
    }

    /** Test helper: clear any in-flight cast. */
    public void clearPending() {
        pending = null;
    }
}
