package com.donos.zebra.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class CameraController {

    private final OrthographicCamera camera;
    private float shakeTime;
    private float shakeMagnitude;

    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void follow(float targetX, float targetY) {
        float x = targetX;
        float y = targetY;
        if (shakeTime > 0f && shakeMagnitude > 0f) {
            x += MathUtils.random(-shakeMagnitude, shakeMagnitude);
            y += MathUtils.random(-shakeMagnitude, shakeMagnitude);
        }
        camera.position.set(x, y, 0);
        camera.update();
    }

    /** Lightweight screen shake (duration seconds, magnitude in world units). */
    public void shake(float duration, float magnitude) {
        this.shakeTime = Math.max(this.shakeTime, duration);
        this.shakeMagnitude = Math.max(this.shakeMagnitude, magnitude);
    }

    public void update(float delta) {
        if (shakeTime > 0f) {
            shakeTime -= delta;
            if (shakeTime <= 0f) {
                shakeTime = 0f;
                shakeMagnitude = 0f;
            } else {
                shakeMagnitude *= 0.92f;
            }
        }
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
