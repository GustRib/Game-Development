package com.donos.zebra.Interaction;

import com.donos.zebra.entities.Player;

public interface Interactable {
    void onInteract(Player player);
    float getX();
    float getY();
    float getInteractionRadius();
    String getPromptText(); // Ex: "Pressione E para falar" ou "Pressione E para minerar"

    /**
     * Optional second interaction (e.g. mentor shop via R).
     * Default: no secondary action.
     */
    default boolean hasSecondaryInteract() {
        return false;
    }

    default String getSecondaryPromptText() {
        return null;
    }

    default void onSecondaryInteract(Player player) {
    }
}
