package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.util.SpriteSheetLoader;

public class AnimatedObject implements Entity {

    private Animation<TextureRegion> animation;
    private Texture texture;
    private float x, y;
    private float stateTime = 0f;

    public AnimatedObject(String spriteSheetPath, int frameCols, int frameRows, float x, float y, float frameDuration) {
        this.texture = new Texture(spriteSheetPath);
        TextureRegion[][] tmp = SpriteSheetLoader.split(texture, frameCols, frameRows);

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

    @Override
    public void update(float delta) {
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime);
        batch.draw(currentFrame, x, y);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
