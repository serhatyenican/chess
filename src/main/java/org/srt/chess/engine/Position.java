package org.srt.chess.engine;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Everything needed to continue a game: the pieces on the board plus the state FEN records alongside them.
 *
 * @param board          the placement of the pieces
 * @param sideToMove     the color that moves next
 * @param castlingRights the castling rights still available; an immutable copy is stored
 * @param enPassant      the square behind a pawn that just made a double step, or {@code null}
 * @param halfmoveClock  half-moves since the last capture or pawn move (for the fifty-move rule)
 * @param fullmoveNumber starts at 1 and increases after each black move
 */
public record Position(
        Board board,
        Color sideToMove,
        Set<CastlingRight> castlingRights,
        Square enPassant,
        int halfmoveClock,
        int fullmoveNumber) {

    public Position {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(sideToMove, "sideToMove must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        castlingRights = Set.copyOf(castlingRights);
        if (enPassant != null) {
            // White to move: black just double-stepped from rank 7 to 5, so the square behind it is on rank 6.
            int expectedRank = sideToMove == Color.WHITE ? 5 : 2;
            if (enPassant.rank() != expectedRank) {
                throw new IllegalArgumentException("en passant square must be on rank " + (expectedRank + 1)
                        + " when " + sideToMove.name().toLowerCase(Locale.ROOT) + " is to move, was " + enPassant);
            }
        }
        if (halfmoveClock < 0) {
            throw new IllegalArgumentException("halfmoveClock must not be negative, was " + halfmoveClock);
        }
        if (fullmoveNumber < 1) {
            throw new IllegalArgumentException("fullmoveNumber must be at least 1, was " + fullmoveNumber);
        }
    }

    /**
     * Returns the position at the start of a game.
     */
    public static Position starting() {
        return Fen.parse(Fen.STARTING_POSITION);
    }
}
