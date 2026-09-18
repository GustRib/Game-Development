package com.donos.zebra.quests;

import com.donos.zebra.entities.CraftingStation;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.MapDoor;
import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.entities.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds map markers from live entities for the shared world map / quest locate UI.
 */
public final class QuestMapModel {

    public final String areaId;
    public final String areaTitle;
    public final float worldMinX;
    public final float worldMinY;
    public final float worldMaxX;
    public final float worldMaxY;
    public final List<QuestMapMarker> markers;

    public QuestMapModel(String areaId,
                         String areaTitle,
                         float worldMinX,
                         float worldMinY,
                         float worldMaxX,
                         float worldMaxY,
                         List<QuestMapMarker> markers) {
        this.areaId = areaId;
        this.areaTitle = areaTitle;
        this.worldMinX = worldMinX;
        this.worldMinY = worldMinY;
        this.worldMaxX = worldMaxX;
        this.worldMaxY = worldMaxY;
        this.markers = markers;
    }

    /**
     * Full overworld marker set for M / LOCALIZAR. Player world position may be overridden
     * (e.g. outdoor return position while inside the tavern).
     */
    public static List<QuestMapMarker> worldMarkers(Player player,
                                                    float playerWorldX,
                                                    float playerWorldY,
                                                    List<Entity> entities,
                                                    QuestLocation focus) {
        List<QuestMapMarker> markers = new ArrayList<>();
        if (player != null) {
            markers.add(new QuestMapMarker(
                "player", QuestMarkerType.PLAYER, "Voce",
                playerWorldX, playerWorldY, true, false));
        }

        MentorNpc mentor = findMentor(entities);
        if (mentor != null) {
            boolean objective = isFocus(focus, QuestIds.TARGET_MENTOR)
                || (focus != null && focus.markerType == QuestMarkerType.NPC
                && (focus.targetId == null || QuestIds.TARGET_MENTOR.equals(focus.targetId)));
            markers.add(new QuestMapMarker(
                QuestIds.TARGET_MENTOR, QuestMarkerType.NPC, "Mentor",
                mentor.getX(), mentor.getY(), false, objective));
        }

        float oreCx = 0f;
        float oreCy = 0f;
        int oreCount = 0;
        for (Entity e : safe(entities)) {
            if (e instanceof OreNode) {
                oreCx += e.getX();
                oreCy += e.getY();
                oreCount++;
            }
        }
        if (oreCount > 0) {
            oreCx /= oreCount;
            oreCy /= oreCount;
            boolean objective = isFocus(focus, QuestIds.TARGET_COPPER_VEINS)
                || (focus != null && focus.markerType == QuestMarkerType.MINING_AREA);
            markers.add(new QuestMapMarker(
                QuestIds.TARGET_COPPER_VEINS, QuestMarkerType.MINING_AREA, "Mineracao",
                oreCx, oreCy, false, objective));
        }

        CraftingStation forge = findForge(entities);
        if (forge != null) {
            boolean objective = isFocus(focus, QuestIds.TARGET_FORGE)
                || (focus != null && focus.markerType == QuestMarkerType.FORGE);
            markers.add(new QuestMapMarker(
                QuestIds.TARGET_FORGE, QuestMarkerType.FORGE, "Forja",
                forge.getX(), forge.getY(), false, objective));
        }

        for (Entity e : safe(entities)) {
            if (e instanceof MapDoor) {
                boolean tavernFocus = isFocus(focus, QuestIds.TARGET_TAVERN)
                    || (focus != null && focus.markerType == QuestMarkerType.TAVERN);
                // Forge objectives outdoors point at the tavern entrance when forge isn't loaded.
                boolean forgeViaDoor = forge == null
                    && (isFocus(focus, QuestIds.TARGET_FORGE)
                    || (focus != null && focus.markerType == QuestMarkerType.FORGE));
                markers.add(new QuestMapMarker(
                    QuestIds.TARGET_TAVERN, QuestMarkerType.TAVERN, "Taverna",
                    e.getX(), e.getY(), false, tavernFocus || forgeViaDoor));
            }
        }

        return markers;
    }

    /**
     * Resolves a contextual marker model (bounds + markers) for tests / legacy callers.
     */
    public static QuestMapModel forLocation(QuestLocation location,
                                            Player player,
                                            List<Entity> entities,
                                            boolean inTavern) {
        float px = player != null ? player.getX() : 0f;
        float py = player != null ? player.getY() : 0f;
        List<QuestMapMarker> markers = worldMarkers(player, px, py, entities, location);
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (QuestMapMarker m : markers) {
            minX = Math.min(minX, m.x);
            minY = Math.min(minY, m.y);
            maxX = Math.max(maxX, m.x);
            maxY = Math.max(maxY, m.y);
        }
        if (minX == Float.POSITIVE_INFINITY) {
            minX = 0;
            minY = 0;
            maxX = 320;
            maxY = 240;
        } else {
            minX -= 50f;
            minY -= 50f;
            maxX += 50f;
            maxY += 50f;
        }
        String area = location != null ? location.areaId : QuestIds.AREA_VILLAGE;
        String title = "Mapa";
        if (QuestIds.AREA_MINING.equals(area)) {
            title = "Area de Mineracao";
        } else if (QuestIds.AREA_TAVERN.equals(area)) {
            title = "Taverna";
        } else {
            title = "Vila";
        }
        return new QuestMapModel(area, title, minX, minY, maxX, maxY, markers);
    }

    private static boolean isFocus(QuestLocation focus, String targetId) {
        return focus != null && targetId != null && targetId.equals(focus.targetId);
    }

    private static List<Entity> safe(List<Entity> entities) {
        return entities != null ? entities : List.of();
    }

    private static MentorNpc findMentor(List<Entity> entities) {
        for (Entity e : safe(entities)) {
            if (e instanceof MentorNpc) {
                return (MentorNpc) e;
            }
        }
        return null;
    }

    private static CraftingStation findForge(List<Entity> entities) {
        for (Entity e : safe(entities)) {
            if (e instanceof CraftingStation) {
                return (CraftingStation) e;
            }
        }
        return null;
    }

    /** Location for the currently tracked objective, or null. */
    public static QuestLocation currentObjectiveLocation(QuestLog log) {
        if (log == null) {
            return null;
        }
        QuestObjectiveDefinition obj = log.getCurrentObjective();
        return obj == null ? null : obj.location;
    }
}
