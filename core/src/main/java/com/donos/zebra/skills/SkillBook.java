package com.donos.zebra.skills;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Player-owned skill state: unlocked runtimes + hotbar assignments.
 */
public final class SkillBook {

    private final Map<String, SkillRuntime> runtimes = new LinkedHashMap<>();
    private final SkillBar bar = new SkillBar();

    public SkillBook() {
        for (SkillDefinition def : SkillRegistry.all()) {
            runtimes.put(def.getId(), new SkillRuntime(def, true));
        }
    }

    public SkillBar getBar() {
        return bar;
    }

    public SkillRuntime getRuntime(String skillId) {
        return runtimes.get(skillId);
    }

    public SkillRuntime getRuntimeForSlot(int slotIndex) {
        String id = bar.getSlotId(slotIndex);
        return id != null ? runtimes.get(id) : null;
    }

    public Collection<SkillRuntime> allRuntimes() {
        return runtimes.values();
    }

    public List<SkillDefinition> unlockedDefinitions() {
        List<SkillDefinition> list = new ArrayList<>();
        for (SkillRuntime runtime : runtimes.values()) {
            if (runtime.isUnlocked()) {
                list.add(runtime.getDefinition());
            }
        }
        return list;
    }

    /**
     * Gameplay-time tick. Pass {@code worldDelta} so cooldowns freeze on pause/death.
     */
    public void tick(float delta) {
        for (SkillRuntime runtime : runtimes.values()) {
            runtime.tick(delta);
        }
    }
}
