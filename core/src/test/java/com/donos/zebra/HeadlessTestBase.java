package com.donos.zebra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.BeforeAll;

public abstract class HeadlessTestBase {

    @BeforeAll
    static void initGdx() {
        if (com.badlogic.gdx.Gdx.files == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            new HeadlessApplication(new ApplicationAdapter() {}, config);
        }
    }
}
