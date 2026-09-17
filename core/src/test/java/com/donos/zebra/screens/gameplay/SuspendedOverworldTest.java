package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Part 4: entering/exiting tavern must park live overworld entities, not rebuild them.
 */
class SuspendedOverworldTest {

    @Test
    void suspendedOverworldKeepsSameDeadOrcAndOreNodeReferences() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createDirectionalAnimations());
        orc.takeDamage(9999f);
        assertTrue(orc.isDead());

        OreNode ore = new OreNode(50f, 50f, null, null);

        List<Entity> live = new ArrayList<>();
        live.add(orc);
        live.add(ore);
        List<Interactable> interactables = new ArrayList<>();
        interactables.add(ore);

        Array<Rectangle> rects = new Array<>();
        Array<Polygon> polys = new Array<>();
        TiledMap map = new TiledMap();

        SuspendedOverworld parked = new SuspendedOverworld(map, rects, polys, live, interactables);

        assertSame(orc, parked.entities.get(0));
        assertSame(ore, parked.entities.get(1));
        assertTrue(((Orc) parked.entities.get(0)).isDead());
        assertSame(map, parked.map);
        assertSame(rects, parked.collisionRects);
        assertSame(ore, parked.interactables.get(0));
    }

    @Test
    void restorePatternPreservesDeathStateOnSameInstance() {
        Orc living = new Orc(10f, 10f, TestAnimationFactory.createDirectionalAnimations());
        List<Entity> parkedList = new ArrayList<>();
        parkedList.add(living);

        SuspendedOverworld parked = new SuspendedOverworld(
            new TiledMap(), new Array<>(), new Array<>(), parkedList, new ArrayList<>());

        List<Entity> restored = new ArrayList<>();
        restored.addAll(parked.entities);

        living.takeDamage(9999f);
        assertTrue(living.isDead());
        assertTrue(((Orc) restored.get(0)).isDead());
        assertSame(living, restored.get(0));
    }
}
