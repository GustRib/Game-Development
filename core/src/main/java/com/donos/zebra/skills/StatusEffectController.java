package com.donos.zebra.skills;

import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Enemy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Owns active status effects on a single enemy.
 */
public final class StatusEffectController {

    private final List<StatusEffect> effects = new ArrayList<>();

    public void apply(StatusEffect effect) {
        if (effect == null) {
            return;
        }
        for (StatusEffect existing : effects) {
            if (existing.getId().equals(effect.getId())) {
                existing.refreshFrom(effect);
                return;
            }
        }
        effects.add(effect);
    }

    public void update(Enemy target, float delta, List<DamageText> damageTexts) {
        Iterator<StatusEffect> it = effects.iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next();
            effect.update(target, delta, damageTexts);
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }

    public boolean has(String statusId) {
        for (StatusEffect effect : effects) {
            if (effect.getId().equals(statusId)) {
                return true;
            }
        }
        return false;
    }

    public StatusEffect get(String statusId) {
        for (StatusEffect effect : effects) {
            if (effect.getId().equals(statusId)) {
                return effect;
            }
        }
        return null;
    }

    public boolean hasBurning() {
        return has(BurningStatus.ID);
    }

    public void clear() {
        effects.clear();
    }

    public int size() {
        return effects.size();
    }
}
