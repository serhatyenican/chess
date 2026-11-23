package org.srt.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates pseudo-legal moves: moves that follow how each piece moves, without checking whether they leave the
 * mover's own king in check.
 */
public final class MoveGenerator {

    private static final List<Offset> KNIGHT_JUMPS = List.of(
            new Offset(1, 2), new Offset(2, 1), new Offset(2, -1), new Offset(1, -2),
            new Offset(-1, -2), new Offset(-2, -1), new Offset(-2, 1), new Offset(-1, 2));

    private static final List<Offset> KING_STEPS = List.of(
            new Offset(0, 1), new Offset(1, 1), new Offset(1, 0), new Offset(1, -1),
            new Offset(0, -1), new Offset(-1, -1), new Offset(-1, 0), new Offset(-1, 1));

    private static final List<Offset> ROOK_DIRECTIONS = List.of(
            new Offset(0, 1), new Offset(1, 0), new Offset(0, -1), new Offset(-1, 0));

    private MoveGenerator() {
    }

    /**
     * Returns the pseudo-legal moves of the side to move. Only king, rook and knight moves are generated so far,
     * without castling.
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
            case KING -> kingMoves(position, from);
            case ROOK -> rookMoves(position, from);
            case KNIGHT -> knightMoves(position, from);
            case QUEEN, BISHOP, PAWN -> List.of();
        };
    }

    /**
     * Returns the moves of the king on {@code from}, which belongs to the side to move: one step in each of the eight
     * directions, each to an empty square or a capture. Castling is not included.
     */
    static List<Move> kingMoves(Position position, Square from) {
        return stepMoves(position, from, KING_STEPS);
    }

    /**
     * Returns the moves of the rook on {@code from}, which belongs to the side to move: along its rank and file up to
     * the edge of the board or the first piece, which it captures if it is an enemy piece.
     */
    static List<Move> rookMoves(Position position, Square from) {
        return slidingMoves(position, from, ROOK_DIRECTIONS);
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
        return offsets.stream()
                .flatMap(offset -> from.offset(offset.fileDelta(), offset.rankDelta()).stream())
                .filter(to -> position.board().pieceAt(to).isEmpty() || isEnemy(position, to))
                .map(to -> new Move(from, to))
                .toList();
    }

    /**
     * Returns the moves from {@code from} that repeat each direction up to the edge of the board or the first piece,
     * which is included as a capture if it belongs to the opponent.
     */
    private static List<Move> slidingMoves(Position position, Square from, List<Offset> directions) {
        return directions.stream()
                .flatMap(direction -> slide(position, from, direction).stream())
                .toList();
    }

    /**
     * Returns the moves from {@code from} in one direction: every empty square up to the first piece, plus that
     * piece if it is an enemy piece.
     */
    private static List<Move> slide(Position position, Square from, Offset direction) {
        List<Move> moves = new ArrayList<>();
        Optional<Square> next = from.offset(direction.fileDelta(), direction.rankDelta());
        while (next.isPresent()) {
            Square to = next.get();
            if (position.board().pieceAt(to).isPresent()) {
                if (isEnemy(position, to)) {
                    moves.add(new Move(from, to));    // capture, then stop
                }
                break;                                // own piece: stop before it
            }
            moves.add(new Move(from, to));            // empty square: keep going
            next = to.offset(direction.fileDelta(), direction.rankDelta());
        }
        return moves;
    }

    private static boolean isEnemy(Position position, Square square) {
        return position.board().pieceAt(square)
                .map(Piece::color)
                .filter(position.sideToMove().opposite()::equals)
                .isPresent();
    }

    private record Offset(int fileDelta, int rankDelta) {
    }
}
