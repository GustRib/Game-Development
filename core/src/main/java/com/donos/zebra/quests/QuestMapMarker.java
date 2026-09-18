package com.donos.zebra.quests;

/**
 * Resolved marker for contextual map rendering (world units relative to area).
 */
public final class QuestMapMarker {

    public final String id;
    public final QuestMarkerType type;
    public final String label;
    public final float x;
    public final float y;
    public final boolean isPlayer;
    public final boolean isObjective;

    public QuestMapMarker(String id,
                          QuestMarkerType type,
                          String label,
                          float x,
                          float y,
                          boolean isPlayer,
                          boolean isObjective) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
        this.isObjective = isObjective;
    }
}
