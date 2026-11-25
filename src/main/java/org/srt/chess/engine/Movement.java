package org.srt.chess.engine;

import java.util.List;

/**
 * How a kind of piece moves and attacks. Each {@link PieceType} has one, so move generation and attack detection ask
 * the piece's movement instead of switching on its type.
 */
sealed interface Movement permits Stepping, Sliding, KingMovement, PawnMovement {

    /**
     * Returns the pseudo-legal moves of the piece on {@code from}, which belongs to the side to move: moves that follow
     * how the piece moves, without checking whether they leave the mover's own king in check.
     */
    List<Move> moves(Position position, Square from);

    /**
     * Returns whether {@code attacker}, a piece that moves like this, attacks {@code target} from anywhere on the
     * board, i.e. could capture a piece standing there. Looks outward from the target for the attacker.
     */
    boolean attacks(Board board, Square target, Piece attacker);

    /**
     * Returns the board after {@code piece} plays the move in this position. By default the piece leaves its square
     * and lands on the target, replacing any piece captured there; the king and the pawn add their special moves.
     */
    default Board play(Position position, Piece piece, Move move) {
        return position.board().without(move.from()).with(move.to(), piece);
    }
}
