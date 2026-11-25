package org.srt.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A piece that repeats each of its directions up to the edge of the board or the first piece, which it captures if it
 * is an enemy piece: the rook along ranks and files, the bishop along diagonals, the queen along both.
 */
record Sliding(List<Offset> directions) implements Movement {

    @Override
    public List<Move> moves(Position position, Square from) {
        return directions.stream()
                .flatMap(direction -> slide(position, from, direction).stream())
                .toList();
    }

    /**
     * The directions are symmetric, so an attacker is the first piece seen from the target along one of them.
     */
    @Override
    public boolean attacks(Board board, Square target, Piece attacker) {
        return directions.stream()
                .flatMap(direction -> firstPiece(board, target, direction).stream())
                .anyMatch(attacker::equals);
    }

    /**
     * Returns the moves from {@code from} in one direction: every empty square up to the first piece, plus that
     * piece if it is an enemy piece.
     */
    private static List<Move> slide(Position position, Square from, Offset direction) {
        Board board = position.board();
        Color enemy = position.sideToMove().opposite();
        List<Move> moves = new ArrayList<>();
        Optional<Square> next = direction.applyTo(from);
        while (next.isPresent()) {
            Square to = next.get();
            if (!board.isEmpty(to)) {
                if (board.isOccupiedBy(to, enemy)) {
                    moves.add(new Move(from, to));    // capture, then stop
                }
                break;                                // own piece: stop before it
            }
            moves.add(new Move(from, to));            // empty square: keep going
            next = direction.applyTo(to);
        }
        return moves;
    }

    /**
     * Returns the first piece seen from {@code square} in one direction, or an empty Optional if the line is empty up
     * to the edge of the board.
     */
    private static Optional<Piece> firstPiece(Board board, Square square, Offset direction) {
        Optional<Square> next = direction.applyTo(square);
        while (next.isPresent()) {
            Optional<Piece> piece = board.pieceAt(next.get());
            if (piece.isPresent()) {
                return piece;
            }
            next = direction.applyTo(next.get());
        }
        return Optional.empty();
    }
}
