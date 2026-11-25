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

    public boolean isPromotion() {
        return promotion != null;
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
