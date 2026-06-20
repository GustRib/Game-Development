package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Color;

public class DamageText {
    public float x, y;
    public String text;
    public float lifetime;
    public Color color; // Nova propriedade de cor
    public static final float MAX_LIFETIME = 1.0f;

    public DamageText(float x, float y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.lifetime = MAX_LIFETIME;
        this.color = color;
    }

    public void update(float delta) {
        lifetime -= delta;
        y += 20f * delta;
    }
}