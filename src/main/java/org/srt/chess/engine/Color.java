package org.srt.chess.engine;

/**
 * The two sides in a game of chess.
 */
public enum Color {
    WHITE,
    BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
