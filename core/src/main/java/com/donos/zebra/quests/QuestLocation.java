package com.donos.zebra.quests;

/**
 * Logical place for an objective (not UI coordinates).
 */
public final class QuestLocation {

    public final String areaId;
    public final QuestMarkerType markerType;
    public final String targetId;
    public final String label;

    public QuestLocation(String areaId, QuestMarkerType markerType, String targetId, String label) {
        this.areaId = areaId;
        this.markerType = markerType;
        this.targetId = targetId;
        this.label = label;
    }
}
