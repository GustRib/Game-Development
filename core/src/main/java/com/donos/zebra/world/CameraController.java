package com.donos.zebra.world;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class CameraController {

    private final OrthographicCamera camera;

    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void follow(float targetX, float targetY) {
        camera.position.set(targetX, targetY, 0);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
