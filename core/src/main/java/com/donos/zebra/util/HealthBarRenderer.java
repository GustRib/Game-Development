package com.donos.zebra.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HealthBarRenderer {

    public static void draw(ShapeRenderer shape, float x, float y, float width, float height, float currentHealth, float maxHealth) {
        if (currentHealth <= 0) return;

        float percentage = currentHealth / maxHealth;
        if (percentage > 1f) percentage = 1f;

        // 1. Define a cor com base na porcentagem de vida
        Color healthColor;
        if (percentage > 0.6f) {
            healthColor = Color.GREEN;
        } else if (percentage > 0.4f) {
            healthColor = Color.YELLOW;
        } else if (percentage > 0.15f) {
            healthColor = Color.ORANGE;
        } else {
            healthColor = Color.RED;
        }

        // 2. Desenha o fundo/borda preta da barra
        shape.setColor(Color.BLACK);
        shape.rect(x - width / 2f, y, width, height);

        // 3. Desenha a barra de vida preenchida por dentro (com recuo de 1 pixel para parecer uma borda)
        shape.setColor(healthColor);
        float innerWidth = (width - 2f) * percentage;
        float innerHeight = height - 2f;
        
        if (innerWidth > 0) {
            shape.rect(x - width / 2f + 1f, y + 1f, innerWidth, innerHeight);
        }
    }
}