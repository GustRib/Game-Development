package com.donos.zebra.skills;

import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Enemy;

import java.util.List;

/**
 * Reusable timed status applied to enemies (Burning, future Poison/Bleed/etc.).
 */
public interface StatusEffect {

    String getId();

    void update(Enemy target, float delta, List<DamageText> damageTexts);

    boolean isExpired();

    /** Refresh/replace semantics when the same status id is re-applied. */
    void refreshFrom(StatusEffect other);
}
