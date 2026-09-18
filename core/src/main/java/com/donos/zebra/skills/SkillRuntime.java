package com.donos.zebra.skills;

/**
 * Per-skill runtime state (cooldown). Definition stays immutable in {@link SkillRegistry}.
 */
public final class SkillRuntime {

    private final SkillDefinition definition;
    private boolean unlocked;
    private float cooldownRemaining;

    public SkillRuntime(SkillDefinition definition, boolean unlocked) {
        this.definition = definition;
        this.unlocked = unlocked;
        this.cooldownRemaining = 0f;
    }

    public SkillDefinition getDefinition() {
        return definition;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public float getCooldownRemaining() {
        return cooldownRemaining;
    }

    public boolean isReady() {
        return unlocked && cooldownRemaining <= 0f;
    }

    public void tick(float delta) {
        if (cooldownRemaining > 0f && delta > 0f) {
            cooldownRemaining = Math.max(0f, cooldownRemaining - delta);
        }
    }

    public void startCooldown() {
        cooldownRemaining = definition.getCooldownSeconds();
    }

    /** Test helper. */
    public void setCooldownRemaining(float seconds) {
        this.cooldownRemaining = Math.max(0f, seconds);
    }
}
