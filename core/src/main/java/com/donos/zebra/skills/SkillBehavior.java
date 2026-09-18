package com.donos.zebra.skills;

/**
 * Pluggable cast logic per skill id.
 */
public interface SkillBehavior {

    void execute(SkillCastContext context);
}
