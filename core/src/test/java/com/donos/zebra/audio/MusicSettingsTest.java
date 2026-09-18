package com.donos.zebra.audio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MusicSettingsTest {

    @AfterEach
    void tearDown() {
        MusicSettings.resetForTests();
    }

    @Test
    void volumePercentClampsAndConverts() {
        MusicSettings.setVolumePercent(0);
        assertEquals(0f, MusicSettings.getVolume(), 0.001f);
        assertEquals(0, MusicSettings.getVolumePercent());

        MusicSettings.setVolumePercent(100);
        assertEquals(1f, MusicSettings.getVolume(), 0.001f);

        MusicSettings.setVolumePercent(150);
        assertEquals(1f, MusicSettings.getVolume(), 0.001f);

        MusicSettings.setVolume(-1f);
        assertEquals(0f, MusicSettings.getVolume(), 0.001f);

        MusicSettings.setVolumePercent(42);
        assertEquals(42, MusicSettings.getVolumePercent());
    }
}
