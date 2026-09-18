package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.Player;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.LootUI;

/**
 * Death overlay: waits for death animation, then shows interactive revive/exit UI.
 */
public final class PlayerDeathHandler {

    private PlayerDeathHandler() {
    }

    public enum Result {
        NONE,
        REVIVED,
        EXIT
    }

    /**
     * Advances death controller and draws overlay. Input only when interactive.
     */
    public static Result updateDrawAndHandle(
            DeathScreenController controller,
            Player player,
            float spawnX,
            float spawnY,
            Array<Polygon> collisionPolygons,
            OrthographicCamera camera,
            ShapeRenderer shapeRenderer,
            SpriteBatch batch,
            BitmapFont font,
            LootUI lootWindow,
            InventoryUI inventoryWindow,
            float uiDelta
    ) {
        if (!player.isDead()) {
            controller.reset();
            return Result.NONE;
        }
        if (!controller.isActive()) {
            controller.begin();
        }

        controller.update(player.isDeathAnimationFinished(), uiDelta);
        draw(controller, camera, shapeRenderer, batch, font);

        if (!controller.isInteractive()) {
            return Result.NONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            applyRevive(player, spawnX, spawnY, collisionPolygons, lootWindow, inventoryWindow);
            controller.reset();
            return Result.REVIVED;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return Result.EXIT;
        }
        return Result.NONE;
    }

    private static void draw(DeathScreenController controller,
                             OrthographicCamera camera,
                             ShapeRenderer shapeRenderer,
                             SpriteBatch batch,
                             BitmapFont font) {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, controller.getOverlayAlpha());
        shapeRenderer.rect(camX - width / 2, camY - height / 2, width, height);
        // Soft vignette rings
        float vig = controller.getOverlayAlpha() * 0.35f;
        shapeRenderer.setColor(0f, 0f, 0f, vig);
        shapeRenderer.rect(camX - width / 2, camY - height / 2, width, height * 0.18f);
        shapeRenderer.rect(camX - width / 2, camY + height / 2 - height * 0.18f, width, height * 0.18f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        GlyphLayout layout = new GlyphLayout();
        float titleA = controller.getTitleAlpha();
        font.getData().setScale(1.6f);
        font.setColor(0.85f, 0.15f, 0.12f, titleA);
        String title = "VOCE MORREU";
        layout.setText(font, title);
        font.draw(batch, title, camX - layout.width / 2f, camY + 36f);

        float instA = controller.getInstructionsAlpha();
        if (instA > 0.05f) {
            font.getData().setScale(0.65f);
            font.setColor(0.85f, 0.85f, 0.85f, instA * 0.9f);
            String sub = "Sua jornada nao termina aqui.";
            layout.setText(font, sub);
            font.draw(batch, sub, camX - layout.width / 2f, camY + 8f);

            font.setColor(1f, 1f, 1f, instA);
            String r = "[ R ]  Renascer";
            layout.setText(font, r);
            font.draw(batch, r, camX - layout.width / 2f, camY - 22f);

            String esc = "[ ESC ]  Sair";
            layout.setText(font, esc);
            font.draw(batch, esc, camX - layout.width / 2f, camY - 42f);
        }

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        batch.end();
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
        if (lootWindow != null) {
            lootWindow.setVisible(false);
        }
        if (inventoryWindow != null) {
            inventoryWindow.setVisible(false);
        }
    }
}
