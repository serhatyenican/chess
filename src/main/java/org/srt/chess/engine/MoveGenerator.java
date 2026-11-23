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
     * Returns the pseudo-legal moves of the side to move. Only knight moves are generated so far.
     */
    public static List<Move> generate(Position position) {
        Color mover = position.sideToMove();
        return Square.all().stream()
                .flatMap(from -> position.board().pieceAt(from).stream()
                        .filter(piece -> piece.color() == mover)
                        .flatMap(piece -> movesOf(position, from, piece.type()).stream()))
                .toList();
    }

    private static List<Move> movesOf(Position position, Square from, PieceType type) {
        return switch (type) {
            case KNIGHT -> knightMoves(position, from);
            case KING, QUEEN, ROOK, BISHOP, PAWN -> List.of();
        };
    }

    /**
     * Returns the moves of the knight on {@code from}, which belongs to the side to move: up to eight jumps, each to
     * an empty square or a capture.
     */
    static List<Move> knightMoves(Position position, Square from) {
        return stepMoves(position, from, KNIGHT_JUMPS);
    }

    /**
     * Returns the moves from {@code from} that apply each offset once, skipping squares off the board or taken by
     * a piece of the side to move.
     */
    private static List<Move> stepMoves(Position position, Square from, List<Offset> offsets) {
        Color mover = position.sideToMove();
        return offsets.stream()
                .flatMap(offset -> from.offset(offset.fileDelta(), offset.rankDelta()).stream())
                .filter(to -> position.board().pieceAt(to).map(Piece::color).filter(mover::equals).isEmpty())
                .map(to -> new Move(from, to))
                .toList();
    }

    private record Offset(int fileDelta, int rankDelta) {
    }
}
