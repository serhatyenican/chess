package org.srt.chess.engine;

import java.util.Objects;

/**
 * A move from one square to another.
 * <p>
 * {@code promotion} is the piece a pawn turns into when it reaches the last rank, and {@code null} for every other
 * move. A pawn can only promote to a queen, rook, bishop or knight.
 * <p>
 * A move is only a description. Whether it is legal in a position is decided by {@link Position#legalMoves()}, not
 * here.
 */
public record Move(Square from, Square to, PieceType promotion) {

    public Move {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (from.equals(to)) {
            throw new IllegalArgumentException("from and to must differ, both were " + from);
        }
        if (promotion != null && !promotion.canPromoteTo()) {
            throw new IllegalArgumentException("a pawn cannot promote to " + promotion);
        }
    }

    /**
     * A move without promotion.
     */
    public Move(Square from, Square to) {
        this(from, to, null);
    }

    /**
     * Parses a move in UCI notation, e.g. {@code "e2e4"} or {@code "e7e8q"}, the inverse of {@link #toString()}.
     * Says nothing about whether the move is legal.
     *
     * @throws IllegalArgumentException if the text is not a valid move
     */
    public static Move of(String text) {
        if (text == null || text.length() < 4 || text.length() > 5) {
            throw invalidText(text);
        }
        Square from = Square.of(text.substring(0, 2));
        Square to = Square.of(text.substring(2, 4));
        return text.length() == 4
                ? new Move(from, to)
                : new Move(from, to, promotionFrom(text.charAt(4)));
    }

    public boolean isPromotion() {
        return promotion != null;
    }

    private static PieceType promotionFrom(char letter) {
        return PieceType.fromLetter(letter)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid promotion letter '" + letter + "': expected q, r, b or n"));
    }

    private static IllegalArgumentException invalidText(String text) {
        return new IllegalArgumentException("Invalid move \"" + text
                + "\": expected two squares and an optional promotion letter, e.g. \"e2e4\" or \"e7e8q\"");
    }

    /**
     * Returns the move as origin and target square, plus the promotion piece if any, e.g. {@code "e2e4"} or
     * {@code "e7e8q"}. This is the format chess engines use to exchange moves (UCI).
     */
    @Override
    public String toString() {
        return "" + from + to + (isPromotion() ? promotion.letter() : "");
    }
}
