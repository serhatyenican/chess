package org.srt.chess.engine;

import java.util.Objects;

/**
 * One of the 64 squares of the board.
 * <p>
 * {@code file} is the column, 0-7 for a-h. {@code rank} is the row, 0-7 for 1-8. So a1 is (0, 0), e4 is (4, 3)
 * and h8 is (7, 7).
 */
public record Square(int file, int rank) {

    public Square {
        if (file < 0 || file > 7) {
            throw new IllegalArgumentException("file must be 0-7, was " + file);
        }
        if (rank < 0 || rank > 7) {
            throw new IllegalArgumentException("rank must be 0-7, was " + rank);
        }
    }

    /**
     * Parses a square in algebraic notation, e.g. {@code "e4"}: a lowercase file a-h, then a rank 1-8.
     *
     * @throws IllegalArgumentException if the text is not a valid square
     */
    public static Square of(String name) {
        Objects.requireNonNull(name, "square name must not be null");
        if (name.length() != 2) {
            throw invalidName(name);
        }
        char fileChar = name.charAt(0);
        char rankChar = name.charAt(1);
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw invalidName(name);
        }
        return new Square(fileChar - 'a', rankChar - '1');
    }

    private static IllegalArgumentException invalidName(String name) {
        return new IllegalArgumentException(
                "Invalid square \"" + name + "\": expected a file a-h and a rank 1-8, e.g. \"e4\"");
    }

    /**
     * Returns the square in algebraic notation, e.g. {@code "e4"}.
     */
    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }
}
