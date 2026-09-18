package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldPersistenceTest extends HeadlessTestBase {

    @Test
    void defeatedAndDepletedWorldStateSurvivesSaveLoad() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setCharacterName("Gu");

        OreNode ore = new OreNode(200f, 300f, null, null);
        ore.restoreState(0, 40f);

        Orc orc = new Orc(50f, 60f, TestAnimationFactory.createDirectionalAnimations());
        orc.takeDamage(999f);
        float deathTimer = 12f;
        orc.restorePersistedState(55f, 66f, 0f, true, deathTimer, false, 5, new ArrayList<>());

        MentorNpc mentor = new MentorNpc(10f, 20f, null, TestAnimationFactory.createDirectionalAnimations());
        mentor.setGavePickaxe(true);

        List<Entity> entities = new ArrayList<>();
        entities.add(ore);
        entities.add(orc);
        entities.add(mentor);

        SaveData data = SaveStateMapper.capture(player, entities, false, 0f, 0f, null);
        assertEquals(1, data.world.oreNodes.size());
        assertEquals(0, data.world.oreNodes.get(0).hitsRemaining);
        assertEquals(40f, data.world.oreNodes.get(0).depletedTimer, 0.01f);
        assertTrue(data.world.orcs.get(0).dead);
        assertEquals(12f, data.world.orcs.get(0).deathTimer, 0.01f);
        assertTrue(data.quest.mentorGavePickaxe);

        OreNode ore2 = new OreNode(200f, 300f, null, null);
        Orc orc2 = new Orc(50f, 60f, TestAnimationFactory.createDirectionalAnimations());
        MentorNpc mentor2 = new MentorNpc(10f, 20f, null, TestAnimationFactory.createDirectionalAnimations());
        List<Entity> fresh = new ArrayList<>();
        fresh.add(ore2);
        fresh.add(orc2);
        fresh.add(mentor2);

        SaveStateMapper.applyWorld(data.world, data.quest, fresh);
        assertEquals(0, ore2.getHitsRemaining());
        assertEquals(40f, ore2.getDepletedTimer(), 0.01f);
        assertTrue(orc2.isDead());
        assertEquals(12f, orc2.getDeathTimer(), 0.01f);
        assertTrue(mentor2.hasGavePickaxe());
    }
}
