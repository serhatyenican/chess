package org.srt.chess.engine;

import java.util.List;

/**
 * Generates pseudo-legal moves: moves that follow how each piece moves, without checking whether they leave the
 * mover's own king in check.
 */
public final class MoveGenerator {

    private static final List<Offset> KNIGHT_JUMPS = List.of(
            new Offset(1, 2), new Offset(2, 1), new Offset(2, -1), new Offset(1, -2),
            new Offset(-1, -2), new Offset(-2, -1), new Offset(-2, 1), new Offset(-1, 2));

    private MoveGenerator() {
    }

    /**
     * Returns the moves of the knight on {@code from}, which belongs to the side to move: up to eight jumps, each to
     * an empty square or a capture.
     */
    static List<Move> knightMoves(Position position, Square from) {
        Color mover = position.sideToMove();
        return KNIGHT_JUMPS.stream()
                .flatMap(jump -> from.offset(jump.fileDelta(), jump.rankDelta()).stream())
                .filter(to -> position.board().pieceAt(to).map(Piece::color).filter(mover::equals).isEmpty())
                .map(to -> new Move(from, to))
                .toList();
    }

    private record Offset(int fileDelta, int rankDelta) {
    }
}
