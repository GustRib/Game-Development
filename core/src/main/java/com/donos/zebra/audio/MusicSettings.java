package com.donos.zebra.audio;

import com.badlogic.gdx.audio.Music;

/**
 * Centralized music volume (0–1). Applied immediately to any bound {@link Music}.
 */
public final class MusicSettings {

    public static final float DEFAULT_VOLUME = 0.3f;

    private static float volume = DEFAULT_VOLUME;
    private static Music boundMusic;

    private MusicSettings() {
    }

    public static float getVolume() {
        return volume;
    }

    /** 0 = mute, 1 = full. Clamped. */
    public static void setVolume(float value) {
        volume = Math.max(0f, Math.min(1f, value));
        applyToBound();
    }

    public static int getVolumePercent() {
        return Math.round(volume * 100f);
    }

    public static void setVolumePercent(int percent) {
        setVolume(percent / 100f);
    }

    /** Bind the current gameplay (or menu) track so slider changes apply live. */
    public static void bind(Music music) {
        boundMusic = music;
        applyToBound();
    }

    public static void unbind(Music music) {
        if (boundMusic == music) {
            boundMusic = null;
        }
    }

    private static void applyToBound() {
        if (boundMusic != null) {
            boundMusic.setVolume(volume);
        }
    }

    /** Test helper — resets to defaults without disposing music. */
    public static void resetForTests() {
        volume = DEFAULT_VOLUME;
        boundMusic = null;
    }
}
