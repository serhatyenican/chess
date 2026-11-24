package org.srt.chess.engine;

import java.util.Optional;

/**
 * Whether a game goes on or how it ended, judged from the position alone. In v1 a game ends only by checkmate or by
 * stalemate, which is a draw. Endings that depend on what happened in the game rather than on the position, such as
 * resigning, are not decided here.
 */
public enum GameState {
    IN_PROGRESS,
    CHECKMATE,
    DRAW;

    /**
     * Returns the state of the game in this position: in progress while the side to move has a legal move, otherwise
     * checkmate if its king is in check and a draw (stalemate) if not.
     */
    public static GameState of(Position position) {
        if (!MoveGenerator.legalMoves(position).isEmpty()) {
            return IN_PROGRESS;
        }
        return MoveGenerator.isInCheck(position) ? CHECKMATE : DRAW;
    }

    /**
     * Returns the winner if the position is checkmate: the side that is not to move, since the mated side is always
     * the one to move. Empty while the game is in progress or drawn.
     */
    public static Optional<Color> winner(Position position) {
        return of(position) == CHECKMATE ? Optional.of(position.sideToMove().opposite()) : Optional.empty();
    }
}
