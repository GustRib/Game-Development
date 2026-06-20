package com.donos.zebra.entities;

public class DamageText {
    public float x, y;
    public String text;
    public float lifetime;
    public static final float MAX_LIFETIME = 1.0f; // Duração de 1 segundo na tela

    public DamageText(float x, float y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.lifetime = MAX_LIFETIME;
    }

    public void update(float delta) {
        lifetime -= delta;
        y += 20f * delta; // Faz o número subir lentamente (20 pixels por segundo)
    }
}