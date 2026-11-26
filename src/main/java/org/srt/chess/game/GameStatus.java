package org.srt.chess.game;

/**
 * Whether a stored game goes on, and if not, how it ended.
 * <p>
 * Unlike the engine's {@link org.srt.chess.engine.GameState}, which judges a position alone, this also knows endings
 * that come from what the players did, such as leaving. The engine's {@code DRAW} is stored as {@link #STALEMATE},
 * so the reason is kept when more kinds of draws arrive.
 */
public enum GameStatus {
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    LEFT
}
