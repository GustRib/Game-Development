package com.donos.zebra.save;

/**
 * Character name rules for creation and save metadata.
 */
public final class CharacterNameValidator {

    public static final int MAX_LENGTH = 16;

    private CharacterNameValidator() {
    }

    /**
     * @return normalized name, or null if invalid
     */
    public static String validateAndNormalize(String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            return null;
        }
        if (name.length() > MAX_LENGTH) {
            return null;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '\'') {
                continue;
            }
            return null;
        }
        return name;
    }

    public static boolean isValid(String raw) {
        return validateAndNormalize(raw) != null;
    }
}
