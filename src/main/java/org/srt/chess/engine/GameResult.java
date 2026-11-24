package org.srt.chess.engine;

import java.util.Optional;

/**
 * The state of a game together with its winner, judged from the position alone. Only a checkmate has a winner.
 * Endings that depend on what happened in the game rather than on the position, such as resigning, are not decided
 * here.
 */
public record GameResult(GameState state, Optional<Color> winner) {

    /**
     * @throws IllegalArgumentException if the state is checkmate without a winner, or any other state with one
     */
    public GameResult {
        if ((state == GameState.CHECKMATE) != winner.isPresent()) {
            throw new IllegalArgumentException("only a checkmate has a winner, got " + state + " with " + winner);
        }
    }

    /**
     * Returns the result in this position: in progress while the side to move has a legal move, otherwise checkmate
     * if its king is in check and a draw (stalemate) if not. The winner of a checkmate is the side that is not to
     * move, since the mated side is always the one to move. The legal moves are generated once.
     */
    public static GameResult of(Position position) {
        if (!MoveGenerator.legalMoves(position).isEmpty()) {
            return new GameResult(GameState.IN_PROGRESS, Optional.empty());
        }
        return MoveGenerator.isInCheck(position)
                ? new GameResult(GameState.CHECKMATE, Optional.of(position.sideToMove().opposite()))
                : new GameResult(GameState.DRAW, Optional.empty());
    }
}
