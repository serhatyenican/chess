package org.srt.chess.engine;

/**
 * Whether a game goes on or how it ended. In v1 a game ends only by checkmate or by stalemate, which is a draw.
 * {@link GameResult#of(Position)} decides it from a position.
 */
public enum GameState {
    IN_PROGRESS,
    CHECKMATE,
    DRAW
}
