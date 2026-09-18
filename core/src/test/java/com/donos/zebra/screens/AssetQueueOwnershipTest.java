package com.donos.zebra.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.donos.zebra.entities.AnimationConstants;
import com.donos.zebra.entities.CraftingStation;
import com.donos.zebra.entities.OrcAnimationLoader;
import com.donos.zebra.world.LevelConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AssetQueueOwnershipTest {

    @Test
    void nonProceduralQueueLoadsEachPathAtMostOnce() {
        AssetManager assetManager = mock(AssetManager.class);
        LoadingScreen.queueGameplayAssets(assetManager, false);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(assetManager, atLeastOnce()).load(pathCaptor.capture(), any(Class.class));

        List<String> paths = pathCaptor.getAllValues();
        Set<String> unique = new HashSet<>(paths);
        assertEquals(unique.size(), paths.size(), "Duplicate asset paths queued: " + paths);

        assertTrue(unique.contains("items/copper_ore.png"));
        assertTrue(unique.contains("items/stone_pickaxe.png"));
        assertTrue(unique.contains("items/iron_ore.png"));
        assertTrue(unique.contains("items/copper_sword.png"));
        assertTrue(unique.contains("items/copper_helmet.png"));
        assertTrue(unique.contains("items/copper_chestplate.png"));
        assertTrue(unique.contains("items/copper_boots.png"));
        assertTrue(unique.contains("items/copper_gloves.png"));
        assertTrue(unique.contains("items/small_health_potion.png"));
        assertTrue(unique.contains("items/medium_health_potion.png"));
        assertTrue(unique.contains("items/big_health_potion.png"));
        assertTrue(unique.contains("items/silver_coin.png"));
        assertTrue(unique.contains("items/gold_coin.png"));
        assertTrue(unique.contains(CraftingStation.FORGE_TEXTURE_PATH));
        assertTrue(unique.contains(LevelConstants.TAVERN_BACKGROUND));
        assertFalse(unique.contains("items/iron_sword.png"));
        assertFalse(unique.contains("items/copper_longsword.png"));
        assertFalse(unique.contains("items/copper_mail.png"));
        assertTrue(unique.contains(AnimationConstants.IDLE_SHEET_PATH));
        assertTrue(unique.contains(OrcAnimationLoader.ORC_MASTER_PATH));
        assertTrue(unique.contains(LevelConstants.MAP_PATH));
        assertTrue(unique.contains(com.donos.zebra.skills.SkillRegistry.WHIRLWIND_ICON));
        assertTrue(unique.contains(com.donos.zebra.skills.SkillRegistry.FLAME_STRIKE_ICON));
        assertTrue(unique.contains(com.donos.zebra.skills.vfx.SkillVfxAssets.FLAME_SLASH_SHEET));

        verify(assetManager).load(eq(LevelConstants.MAP_PATH), eq(TiledMap.class));
        verify(assetManager).load(eq("items/copper_ore.png"), eq(Texture.class));
        verify(assetManager).load(eq("items/copper_sword.png"), eq(Texture.class));
        verify(assetManager).load(eq("items/iron_ore.png"), eq(Texture.class));
        verify(assetManager).load(eq(LevelConstants.TAVERN_BACKGROUND), eq(Texture.class));
        verify(assetManager).load(eq(com.donos.zebra.skills.SkillRegistry.WHIRLWIND_ICON), eq(Texture.class));
        verify(assetManager).load(eq(com.donos.zebra.skills.SkillRegistry.FLAME_STRIKE_ICON), eq(Texture.class));
        verify(assetManager).load(eq(com.donos.zebra.skills.vfx.SkillVfxAssets.FLAME_SLASH_SHEET), eq(Texture.class));
    }
}
