package org.srt.chess.engine;

import java.util.Objects;

/**
 * One of the 64 squares of the board.
 * <p>
 * {@code file} is the column a-h. {@code rank} is the row, 0-7 for 1-8. So a1 is (A, 0), e4 is (E, 3) and h8 is
 * (H, 7).
 */
public record Square(File file, int rank) {

    public Square {
        Objects.requireNonNull(file, "file must not be null");
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
        return new Square(File.fromLetter(fileChar), rankChar - '1');
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
        return "" + file.letter() + (char) ('1' + rank);
    }
}
