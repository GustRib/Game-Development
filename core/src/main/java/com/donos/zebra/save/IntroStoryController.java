package com.donos.zebra.save;

/**
 * Lightweight intro progression (no full cutscene framework).
 * Gameplay must not begin until {@link #isComplete()}.
 */
public final class IntroStoryController {

    private final IntroStoryData data;
    private final float secondsPerLine;
    private int lineIndex;
    private float lineElapsed;
    private boolean complete;
    private boolean skipped;

    public IntroStoryController(IntroStoryData data) {
        this(data, IntroStoryData.SECONDS_PER_LINE);
    }

    public IntroStoryController(IntroStoryData data, float secondsPerLine) {
        this.data = data != null ? data : new IntroStoryData();
        this.secondsPerLine = Math.max(0.1f, secondsPerLine);
        this.lineIndex = 0;
        this.lineElapsed = 0f;
    }

    public void update(float delta) {
        if (complete) {
            return;
        }
        lineElapsed += delta;
        if (lineElapsed >= secondsPerLine) {
            lineElapsed = 0f;
            lineIndex++;
            if (lineIndex >= data.lineCount()) {
                complete = true;
            }
        }
    }

    public void skip() {
        if (complete) {
            return;
        }
        skipped = true;
        complete = true;
        lineIndex = data.lineCount();
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean wasSkipped() {
        return skipped;
    }

    public int getLineIndex() {
        return Math.min(lineIndex, Math.max(0, data.lineCount() - 1));
    }

    public String getCurrentLine() {
        if (complete && !skipped) {
            return data.lineAt(data.lineCount() - 1);
        }
        return data.lineAt(getLineIndex());
    }

    public float getLineProgress() {
        return Math.min(1f, lineElapsed / secondsPerLine);
    }

    /** Alpha for subtle fade-in of the current line. */
    public float getLineAlpha() {
        float fade = IntroStoryData.FADE_SECONDS;
        if (lineElapsed < fade) {
            return Math.max(0f, lineElapsed / fade);
        }
        float remaining = secondsPerLine - lineElapsed;
        if (remaining < fade) {
            return Math.max(0f, remaining / fade);
        }
        return 1f;
    }
}
