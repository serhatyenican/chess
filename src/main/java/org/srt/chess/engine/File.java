package org.srt.chess.engine;

import java.util.Arrays;

/**
 * One of the eight files (columns) of the board, a to h.
 * <p>
 * The constants are declared from a to h, so {@link #ordinal()} is the file's index 0-7. A test pins that order.
 */
public enum File {
    A, B, C, D, E, F, G, H;

    private static final File[] VALUES = values();

    /**
     * Returns the letter of this file in algebraic notation, e.g. {@code 'e'}: the constant's name in lowercase.
     */
    public char letter() {
        return Character.toLowerCase(name().charAt(0));
    }

    /**
     * Returns the file for its index 0-7, the inverse of {@link #ordinal()}: 0 is a, 7 is h.
     */
    public static File fromIndex(int index) {
        return VALUES[index];
    }

    /**
     * Returns the file for its lowercase letter.
     *
     * @throws IllegalArgumentException for any other character
     */
    public static File fromLetter(char letter) {
        return Arrays.stream(VALUES)
                .filter(file -> file.letter() == letter)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown file letter '" + letter + "', expected one of a-h"));
    }
}
