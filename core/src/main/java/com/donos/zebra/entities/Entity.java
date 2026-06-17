package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Entity {

    void update(float delta);

    void render(SpriteBatch batch);

    void dispose();
}
