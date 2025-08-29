package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class AnimatedObject {

    private Animation<TextureRegion> animation;
    private Texture texture; // para dar dispose
    private float x, y;
    private float stateTime = 0f;

    public AnimatedObject(String spriteSheetPath, int frameCols, int frameRows, float x, float y, float frameDuration) {
        this.texture = new Texture(spriteSheetPath);
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / frameCols, texture.getHeight() / frameRows);

        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                frames.add(tmp[i][j]);
            }
        }

        animation = new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime);
        batch.draw(currentFrame, x, y);
    }

    public void dispose() {
        texture.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
