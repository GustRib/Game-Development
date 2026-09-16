package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.Player;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.LootUI;

/**
 * Game-over overlay and revive / quit inputs.
 */
public final class PlayerDeathHandler {

    private PlayerDeathHandler() {
    }

    /**
     * @return true if the player revived this frame (caller should clear loot target state)
     */
    public static boolean drawAndHandle(
            Player player,
            float spawnX,
            float spawnY,
            Array<Polygon> collisionPolygons,
            OrthographicCamera camera,
            ShapeRenderer shapeRenderer,
            SpriteBatch batch,
            BitmapFont font,
            LootUI lootWindow,
            InventoryUI inventoryWindow
    ) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.75f);

        float camX = camera.position.x;
        float camY = camera.position.y;
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;

        shapeRenderer.rect(camX - width / 2, camY - height / 2, width, height);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", camX - 50f, camY + 30f);

        font.setColor(Color.WHITE);
        font.getData().setScale(0.7f);
        font.draw(batch, "Pressione [ R ] para Renascer", camX - 70f, camY - 10f);
        font.draw(batch, "Pressione [ ESC ] para Sair", camX - 62f, camY - 30f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            applyRevive(player, spawnX, spawnY, collisionPolygons, lootWindow, inventoryWindow);
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        return false;
    }

    /**
     * Revive side effects without rendering or reading input (testable).
     */
    public static void applyRevive(Player player,
                                   float spawnX,
                                   float spawnY,
                                   Array<Polygon> collisionPolygons,
                                   LootUI lootWindow,
                                   InventoryUI inventoryWindow) {
        player.revive(spawnX, spawnY);
        player.update(0, collisionPolygons);
        lootWindow.setVisible(false);
        inventoryWindow.setVisible(false);
    }
}
