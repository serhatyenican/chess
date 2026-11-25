package org.srt.chess.engine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A step of some files and ranks across the board, used for knight jumps, king steps and the directions of the
 * sliding pieces. Positive {@code fileDelta} goes towards the h-file, positive {@code rankDelta} towards rank 8.
 */
record Offset(int fileDelta, int rankDelta) {

    static final List<Offset> KNIGHT_JUMPS = List.of(
            new Offset(1, 2), new Offset(2, 1), new Offset(2, -1), new Offset(1, -2),
            new Offset(-1, -2), new Offset(-2, -1), new Offset(-2, 1), new Offset(-1, 2));

    static final List<Offset> KING_STEPS = List.of(
            new Offset(0, 1), new Offset(1, 1), new Offset(1, 0), new Offset(1, -1),
            new Offset(0, -1), new Offset(-1, -1), new Offset(-1, 0), new Offset(-1, 1));

    static final List<Offset> ROOK_DIRECTIONS = List.of(
            new Offset(0, 1), new Offset(1, 0), new Offset(0, -1), new Offset(-1, 0));

    static final List<Offset> BISHOP_DIRECTIONS = List.of(
            new Offset(1, 1), new Offset(1, -1), new Offset(-1, -1), new Offset(-1, 1));

    // Declared after the rook and bishop directions, which must be initialized first.
    static final List<Offset> QUEEN_DIRECTIONS =
            Stream.concat(ROOK_DIRECTIONS.stream(), BISHOP_DIRECTIONS.stream()).toList();

    /**
     * Returns the square this offset away from {@code square}, or an empty Optional if that is off the board.
     */
    Optional<Square> applyTo(Square square) {
        return square.offset(fileDelta, rankDelta);
    }
}
