package com.donos.zebra.skills;

/**
 * Immutable skill template (shared across all players/sessions).
 */
public final class SkillDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final float cooldownSeconds;
    private final boolean requiresWeapon;
    private final String effectSummary;
    private final String iconPath;

    public SkillDefinition(String id,
                           String name,
                           String description,
                           float cooldownSeconds,
                           boolean requiresWeapon,
                           String effectSummary,
                           String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cooldownSeconds = cooldownSeconds;
        this.requiresWeapon = requiresWeapon;
        this.effectSummary = effectSummary != null ? effectSummary : "";
        this.iconPath = iconPath != null ? iconPath : "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getCooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean requiresWeapon() {
        return requiresWeapon;
    }

    public String getEffectSummary() {
        return effectSummary;
    }

    /** Internal asset path under {@code assets/}, e.g. {@code skills/whirlwind_skill.png}. */
    public String getIconPath() {
        return iconPath;
    }

    public String buildTooltipBody() {
        StringBuilder sb = new StringBuilder();
        if (description != null && !description.isEmpty()) {
            sb.append(description);
        }
        if (effectSummary != null && !effectSummary.isEmpty()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(effectSummary);
        }
        if (sb.length() > 0) {
            sb.append('\n');
        }
        sb.append("Recarga: ").append(formatCooldown(cooldownSeconds));
        return sb.toString();
    }

    public static String formatCooldown(float seconds) {
        if (seconds == (int) seconds) {
            return (int) seconds + "s";
        }
        return String.format(java.util.Locale.ROOT, "%.1fs", seconds);
    }
}
