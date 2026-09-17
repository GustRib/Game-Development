package com.donos.zebra.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

/**
 * Shared Window chrome: padded title bar (required for LibGDX drag hit-testing),
 * working close button, resize-border cursor hints, viewport clamping.
 */
public abstract class GameWindow extends Window implements UiPanelStack.Panel {

    public static final float TITLE_PAD = 32f;
    public static final float SIDE_PAD = 10f;
    public static final float BOTTOM_PAD = 10f;
    public static final int RESIZE_BORDER = 14;

    private Runnable onClosed;
    private boolean notifyingClose;

    protected GameWindow(String title, Skin skin) {
        super(title, skin);
        setMovable(true);
        setKeepWithinStage(true);
        setVisible(false);
        setResizeBorder(RESIZE_BORDER);

        // Explicit pads — LibGDX uses padTop as the only title-bar drag hit region.
        padTop(TITLE_PAD);
        padLeft(SIDE_PAD);
        padRight(SIDE_PAD);
        padBottom(BOTTOM_PAD);

        TextButton closeBtn = new TextButton("X", skin, "window-close");
        closeBtn.setTouchable(Touchable.enabled);
        getTitleTable().add(closeBtn).size(24, 24).padRight(2);
        // Stop propagation so Window's MOVE drag listener does not steal the click.
        closeBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                closePanel();
            }
        });

        addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                updateResizeCursor(x, y);
                return false;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && (toActor == null || !toActor.isDescendantOf(GameWindow.this))) {
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                }
            }
        });
    }

    public void setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
    }

    @Override
    public boolean isPanelOpen() {
        return isVisible();
    }

    @Override
    public void closePanel() {
        if (!isVisible()) {
            return;
        }
        setVisible(false);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        if (!notifyingClose && onClosed != null) {
            notifyingClose = true;
            try {
                onClosed.run();
            } finally {
                notifyingClose = false;
            }
        }
    }

    public void openPanel() {
        setVisible(true);
        toFront();
    }

    /** Keep this window inside the stage viewport after game-window resize. */
    public void clampToStage(float stageWidth, float stageHeight) {
        float maxW = Math.max(120f, stageWidth * 0.92f);
        float maxH = Math.max(120f, stageHeight * 0.92f);
        if (getWidth() > maxW) {
            setWidth(maxW);
        }
        if (getHeight() > maxH) {
            setHeight(maxH);
        }
        float x = Math.max(0f, Math.min(getX(), stageWidth - getWidth()));
        float y = Math.max(0f, Math.min(getY(), stageHeight - getHeight()));
        setPosition(x, y);
        invalidateHierarchy();
    }

    private void updateResizeCursor(float x, float y) {
        if (!isResizable() || !isVisible()) {
            return;
        }
        float border = RESIZE_BORDER;
        float w = getWidth();
        float h = getHeight();
        boolean left = x < border;
        boolean right = x > w - border;
        boolean bottom = y < border;
        boolean top = y > h - getPadTop() && y < h && (left || right);

        if ((left || right) && (bottom || top)) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Crosshair);
        } else if (left || right) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize);
        } else if (bottom) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }

    /** NinePatch-style padded solid used as Window background so pad* are non-zero. */
    public static Drawable createPaddedWindowBackground(Color fill, float top, float left, float bottom, float right) {
        int tw = Math.max(3, (int) (left + right + 1));
        int th = Math.max(3, (int) (top + bottom + 1));
        Pixmap pixmap = new Pixmap(tw, th, Pixmap.Format.RGBA8888);
        pixmap.setColor(fill);
        pixmap.fill();
        // Slightly lighter title strip
        pixmap.setColor(fill.r + 0.06f, fill.g + 0.06f, fill.b + 0.08f, fill.a);
        pixmap.fillRectangle(0, th - Math.max(1, (int) top), tw, Math.max(1, (int) top));
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinWidth(tw);
        drawable.setMinHeight(th);
        drawable.setTopHeight(top);
        drawable.setLeftWidth(left);
        drawable.setBottomHeight(bottom);
        drawable.setRightWidth(right);
        return drawable;
    }

    public static void positionDefault(GameWindow window, float stageW, float stageH, int align) {
        float w = Math.min(window.getWidth(), stageW * 0.45f);
        float h = Math.min(window.getHeight(), stageH * 0.7f);
        window.setSize(w, h);
        if ((align & Align.left) != 0) {
            window.setPosition(16f, (stageH + h) / 2f, Align.topLeft);
        } else if ((align & Align.right) != 0) {
            window.setPosition(stageW - 16f, (stageH + h) / 2f, Align.topRight);
        } else {
            window.setPosition(stageW / 2f, stageH / 2f, Align.center);
        }
        window.clampToStage(stageW, stageH);
    }
}
