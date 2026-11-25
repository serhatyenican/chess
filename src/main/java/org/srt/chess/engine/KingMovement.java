package org.srt.chess.engine;

import java.util.List;
import java.util.stream.Stream;

/**
 * The king: one step in each of the eight directions, plus castling where it is possible. Castling never captures,
 * so the king attacks only the squares around it; playing a castle moves the rook too.
 */
final class KingMovement implements Movement {

    private static final Stepping STEPS = new Stepping(Offset.KING_STEPS);

    @Override
    public List<Move> moves(Position position, Square from) {
        return Stream.concat(STEPS.moves(position, from).stream(), castlingMoves(position, from)).toList();
    }

    @Override
    public boolean attacks(Board board, Square target, Piece attacker) {
        return STEPS.attacks(board, target, attacker);
    }

    /**
     * Moves the rook too if the move is a castle: the king moving two squares to castle.
     */
    @Override
    public Board play(Position position, Piece king, Move move) {
        Board board = Movement.super.play(position, king, move);
        return CastlingRight.fromKingMove(move)
                .map(right -> board.without(right.rookFrom())
                        .with(right.rookTo(), new Piece(king.color(), PieceType.ROOK)))
                .orElse(board);
    }

    /**
     * Returns the castling moves of the king on {@code from}: for each castling right of the side to move, if its
     * rook is in place, the squares between king and rook are empty and none of the king's squares (start, crossed,
     * target) is attacked.
     */
    private static Stream<Move> castlingMoves(Position position, Square from) {
        Color mover = position.sideToMove();
        Board board = position.board();
        Piece rook = new Piece(mover, PieceType.ROOK);
        return position.castlingRights().stream()
                .filter(right -> right.color() == mover)
                .filter(right -> right.kingFrom().equals(from))
                .filter(right -> board.isOccupiedBy(right.rookFrom(), rook))
                .filter(right -> right.squaresBetween().stream().allMatch(board::isEmpty))
                .filter(right -> right.kingPath().stream()
                        .noneMatch(square -> board.isAttacked(square, mover.opposite())))
                .map(right -> new Move(right.kingFrom(), right.kingTo()));
    }
}
