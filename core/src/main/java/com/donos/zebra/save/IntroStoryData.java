package com.donos.zebra.save;

/**
 * Placeholder intro narrative (edit lines here; presentation stays separate).
 */
public final class IntroStoryData {

    public static final String[] DEFAULT_LINES = {
        "Voce acorda na beira de uma terra desconhecida...",
        "O antigo assentamento de mineracao fica a frente...",
        "Algo na floresta esta observando."
    };

    public static final float SECONDS_PER_LINE = 2.4f;
    public static final float FADE_SECONDS = 0.45f;

    private final String[] lines;

    public IntroStoryData() {
        this(DEFAULT_LINES);
    }

    public IntroStoryData(String[] lines) {
        this.lines = lines != null && lines.length > 0 ? lines.clone() : DEFAULT_LINES.clone();
    }

    public String[] getLines() {
        return lines.clone();
    }

    public int lineCount() {
        return lines.length;
    }

    public String lineAt(int index) {
        if (index < 0 || index >= lines.length) {
            return "";
        }
        return lines[index];
    }
}
