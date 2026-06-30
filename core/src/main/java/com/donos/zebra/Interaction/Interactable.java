package com.donos.zebra.Interaction;

import com.donos.zebra.entities.Player;

public interface Interactable {
    void onInteract(Player player);
    float getX();
    float getY();
    float getInteractionRadius();
    String getPromptText(); // Ex: "Pressione E para falar" ou "Pressione E para minerar"
}
