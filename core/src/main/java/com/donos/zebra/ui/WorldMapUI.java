package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.donos.zebra.quests.QuestLocation;
import com.donos.zebra.quests.QuestMapMarker;
import com.donos.zebra.quests.QuestMarkerType;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared ARPG world map: renders the live overworld {@link TiledMap} (prototipo.tmx)
 * for both M (full map) and quest LOCALIZAR (focused objective).
 */
public class WorldMapUI extends Table {

    public enum Mode {
        FULL,
        QUEST_FOCUS
    }

    private final Label titleLabel;
    private final Label legendLabel;
    private final MapView mapView;
    private final TextButton closeButton;
    private Runnable onClosed;
    private Mode mode = Mode.FULL;

    public WorldMapUI(Skin skin) {
        ensureButtonStyles(skin);
        setFillParent(true);
        setBackground(solid(new Color(0f, 0f, 0f, 0.55f)));
        setVisible(false);
        setTouchable(Touchable.enabled);

        Table panel = new Table();
        panel.setBackground(solid(new Color(0.07f, 0.09f, 0.11f, 0.96f)));
        panel.pad(14);

        titleLabel = new Label("MAPA", skin);
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(new Color(0.95f, 0.9f, 0.55f, 1f));

        mapView = new MapView();
        mapView.setSize(640, 400);

        legendLabel = new Label("", skin);
        legendLabel.setWrap(true);
        legendLabel.setColor(new Color(0.8f, 0.85f, 0.9f, 1f));

        closeButton = new TextButton("Fechar (M / ESC)", skin, "pause-menu");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                close();
            }
        });

        panel.add(titleLabel).growX().padBottom(8).row();
        panel.add(mapView).size(640, 400).padBottom(8).row();
        panel.add(legendLabel).width(620).padBottom(8).row();
        panel.add(closeButton).center();

        add(panel).center();
    }

    public Mode getMode() {
        return mode;
    }

    public void setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
    }

    public boolean isOpen() {
        return isVisible();
    }

    /** Full overworld map (M key). */
    public void openFull(TiledMap map, List<QuestMapMarker> markers) {
        mode = Mode.FULL;
        titleLabel.setText("MAPA DO MUNDO");
        legendLabel.setText("Azul: voce  |  Marcadores: locais conhecidos\nM / ESC: Fechar");
        mapView.bind(map, markers, null);
        setVisible(true);
        toFront();
    }

    /** Quest locate mode — same map, focused on objective. */
    public void openQuestFocus(TiledMap map, List<QuestMapMarker> markers, QuestLocation focus) {
        mode = Mode.QUEST_FOCUS;
        String focusLabel = focus != null && focus.label != null ? focus.label : "Objetivo";
        titleLabel.setText("LOCALIZAR — " + focusLabel);
        legendLabel.setText("Azul: voce  |  Vermelho: objetivo (" + focusLabel + ")\nESC / M: Fechar");
        mapView.bind(map, markers, focus);
        setVisible(true);
        toFront();
    }

    public void updateMarkers(List<QuestMapMarker> markers) {
        mapView.setMarkers(markers);
    }

    public void close() {
        setVisible(false);
        mapView.releaseTransientResources();
        if (onClosed != null) {
            onClosed.run();
        }
    }

    public void dispose() {
        mapView.dispose();
    }

    private static void ensureButtonStyles(Skin skin) {
        if (skin == null) {
            return;
        }
        if (!skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
            style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
            style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
            style.checked = solid(new Color(0.4f, 0.35f, 0.18f, 1f));
            skin.add("pause-menu", style);
        }
    }

    private static Drawable solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    /**
     * Renders the real TMX via {@link OrthogonalTiledMapRenderer} into an FBO,
     * then composites markers in UI space.
     */
    private static final class MapView extends Actor {
        private TiledMap map;
        private OrthogonalTiledMapRenderer mapRenderer;
        private final OrthographicCamera mapCamera = new OrthographicCamera();
        private FrameBuffer fbo;
        private final List<QuestMapMarker> markers = new ArrayList<>();
        private QuestLocation focus;
        private float worldW;
        private float worldH;
        private float viewX;
        private float viewY;
        private float viewW;
        private float viewH;
        private final BitmapFont labelFont = new BitmapFont();
        private boolean needsRender = true;

        void bind(TiledMap map, List<QuestMapMarker> markers, QuestLocation focus) {
            this.map = map;
            this.focus = focus;
            setMarkers(markers);
            if (map == null) {
                return;
            }
            if (mapRenderer != null) {
                mapRenderer.dispose();
                mapRenderer = null;
            }
            mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
            MapProperties props = map.getProperties();
            int tilesW = props.get("width", Integer.class);
            int tilesH = props.get("height", Integer.class);
            int tileW = props.get("tilewidth", Integer.class);
            int tileH = props.get("tileheight", Integer.class);
            worldW = tilesW * tileW;
            worldH = tilesH * tileH;
            recomputeView();
            recreateFbo();
            needsRender = true;
        }

        void setMarkers(List<QuestMapMarker> next) {
            markers.clear();
            if (next != null) {
                markers.addAll(next);
            }
            if (map != null) {
                recomputeView();
                needsRender = true;
            }
        }

        private void recomputeView() {
            if (focus == null) {
                viewX = 0f;
                viewY = 0f;
                viewW = Math.max(1f, worldW);
                viewH = Math.max(1f, worldH);
                return;
            }
            float objX = worldW * 0.5f;
            float objY = worldH * 0.5f;
            float playerX = objX;
            float playerY = objY;
            for (QuestMapMarker m : markers) {
                if (m.isObjective) {
                    objX = m.x;
                    objY = m.y;
                }
                if (m.isPlayer || m.type == QuestMarkerType.PLAYER) {
                    playerX = m.x;
                    playerY = m.y;
                }
            }
            float pad = 140f;
            float minX = Math.min(playerX, objX) - pad;
            float maxX = Math.max(playerX, objX) + pad;
            float minY = Math.min(playerY, objY) - pad;
            float maxY = Math.max(playerY, objY) + pad;
            viewW = MathUtils.clamp(maxX - minX, 240f, worldW);
            viewH = MathUtils.clamp(maxY - minY, 180f, worldH);
            float panelAspect = getWidth() > 0 ? getWidth() / getHeight() : 1.6f;
            if (viewW / viewH < panelAspect) {
                viewW = viewH * panelAspect;
            } else {
                viewH = viewW / panelAspect;
            }
            viewW = Math.min(viewW, worldW);
            viewH = Math.min(viewH, worldH);
            viewX = MathUtils.clamp((playerX + objX) * 0.5f - viewW * 0.5f, 0f, Math.max(0f, worldW - viewW));
            viewY = MathUtils.clamp((playerY + objY) * 0.5f - viewH * 0.5f, 0f, Math.max(0f, worldH - viewH));
        }

        private void recreateFbo() {
            if (fbo != null) {
                fbo.dispose();
                fbo = null;
            }
            int w = Math.max(64, (int) getWidth());
            int h = Math.max(64, (int) getHeight());
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
            needsRender = true;
        }

        @Override
        protected void sizeChanged() {
            if (map != null && getWidth() > 1f && getHeight() > 1f) {
                recomputeView();
                recreateFbo();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (map == null || mapRenderer == null) {
                batch.setColor(0.1f, 0.12f, 0.14f, parentAlpha);
                batch.draw(pixel(), getX(), getY(), getWidth(), getHeight());
                batch.setColor(Color.WHITE);
                return;
            }
            if (fbo == null) {
                recreateFbo();
            }

            batch.end();

            if (needsRender) {
                fbo.begin();
                ScreenUtils.clear(0.05f, 0.07f, 0.06f, 1f);
                mapCamera.setToOrtho(false, viewW, viewH);
                mapCamera.position.set(viewX + viewW * 0.5f, viewY + viewH * 0.5f, 0f);
                mapCamera.update();
                mapRenderer.setView(mapCamera);
                mapRenderer.render();
                fbo.end();
                needsRender = false;
            }

            GdxGlRestore();

            batch.begin();
            batch.setColor(1f, 1f, 1f, parentAlpha);
            Texture tex = fbo.getColorBufferTexture();
            // FBO color buffer is upside-down relative to Stage batch
            batch.draw(tex,
                getX(), getY(), getWidth(), getHeight(),
                0, 0, tex.getWidth(), tex.getHeight(),
                false, true);

            for (QuestMapMarker m : markers) {
                float sx = worldToLocalX(m.x);
                float sy = worldToLocalY(m.y);
                float size = m.isObjective ? 12f : (m.isPlayer || m.type == QuestMarkerType.PLAYER ? 10f : 8f);
                Color c = markerColor(m);
                batch.setColor(c.r, c.g, c.b, parentAlpha);
                batch.draw(pixel(), getX() + sx - size / 2f, getY() + sy - size / 2f, size, size);
            }
            batch.setColor(Color.WHITE);

            labelFont.getData().setScale(0.85f);
            for (QuestMapMarker m : markers) {
                if (!(m.isObjective || m.isPlayer || m.type == QuestMarkerType.PLAYER)) {
                    continue;
                }
                float sx = worldToLocalX(m.x);
                float sy = worldToLocalY(m.y);
                String text = m.label != null ? m.label : m.id;
                labelFont.setColor(1f, 1f, 1f, parentAlpha);
                labelFont.draw(batch, text, getX() + sx + 8f, getY() + sy + 14f);
            }
        }

        private static void GdxGlRestore() {
            // Stage batch expects blending on after FBO work
            com.badlogic.gdx.Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            com.badlogic.gdx.Gdx.gl.glEnable(GL20.GL_BLEND);
            com.badlogic.gdx.Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        private float worldToLocalX(float worldX) {
            return ((worldX - viewX) / viewW) * getWidth();
        }

        private float worldToLocalY(float worldY) {
            return ((worldY - viewY) / viewH) * getHeight();
        }

        private static Color markerColor(QuestMapMarker m) {
            if (m.isPlayer || m.type == QuestMarkerType.PLAYER) {
                return new Color(0.3f, 0.75f, 1f, 1f);
            }
            if (m.isObjective || m.type == QuestMarkerType.QUEST_OBJECTIVE) {
                return new Color(1f, 0.35f, 0.25f, 1f);
            }
            if (m.type == QuestMarkerType.FORGE) {
                return new Color(1f, 0.55f, 0.2f, 1f);
            }
            if (m.type == QuestMarkerType.MINING_AREA) {
                return new Color(0.9f, 0.7f, 0.25f, 1f);
            }
            if (m.type == QuestMarkerType.TAVERN) {
                return new Color(0.7f, 0.55f, 0.95f, 1f);
            }
            if (m.type == QuestMarkerType.NPC) {
                return new Color(0.95f, 0.9f, 0.4f, 1f);
            }
            return new Color(0.85f, 0.85f, 0.9f, 1f);
        }

        void releaseTransientResources() {
            if (fbo != null) {
                fbo.dispose();
                fbo = null;
            }
            needsRender = true;
        }

        void dispose() {
            releaseTransientResources();
            if (mapRenderer != null) {
                mapRenderer.dispose();
                mapRenderer = null;
            }
            labelFont.dispose();
            map = null;
        }

        private static Texture pixelTex;

        private static Texture pixel() {
            if (pixelTex == null) {
                Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pm.setColor(Color.WHITE);
                pm.fill();
                pixelTex = new Texture(pm);
                pm.dispose();
            }
            return pixelTex;
        }
    }
}
