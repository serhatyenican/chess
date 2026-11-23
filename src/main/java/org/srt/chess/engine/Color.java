package org.srt.chess.engine;

import java.util.Arrays;

/**
 * The two sides in a game of chess.
 */
public enum Color {
    WHITE('w'),
    BLACK('b');

    private final char letter;

    Color(char letter) {
        this.letter = letter;
    }

    /**
     * Returns the letter of this color in FEN: {@code 'w'} or {@code 'b'}.
     */
    public char letter() {
        return letter;
    }

    /**
     * Returns the color for its FEN letter.
     *
     * @throws IllegalArgumentException for any other character
     */
    public static Color fromLetter(char letter) {
        return Arrays.stream(values())
                .filter(color -> color.letter == letter)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown color letter '" + letter + "', expected 'w' or 'b'"));
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
