package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;

public interface Entity {

    void update(float delta);

    void render(SpriteBatch batch);

    void dispose();

    // --- Métodos necessários para o Ciclo de Combate e IA ---

    /**
     * Aplica uma quantidade de dano à entidade.
     */
    void takeDamage(float amount);

    /**
     * Retorna se a entidade já foi derrotada (vida <= 0).
     */
    boolean isDead();

    /**
     * Retorna a coordenada X atual da entidade no mundo.
     */
    float getX();

    /**
     * Retorna a coordenada Y atual da entidade no mundo.
     */
    float getY();

    /**
     * Retorna a Hitbox física/polígono de colisão da entidade.
     */
    Polygon getHitbox();
    
    /**
     * Retorna a saúde atual da entidade (útil para logs e barras de vida).
     */
    float getCurrentHealth();
}