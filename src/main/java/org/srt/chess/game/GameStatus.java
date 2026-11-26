package org.srt.chess.game;

import org.srt.chess.engine.GameState;

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
    LEFT;

    /**
     * Returns the status for the engine's judgement of a position. A position alone never means {@link #LEFT}.
     */
    static GameStatus from(GameState state) {
        return switch (state) {
            case IN_PROGRESS -> IN_PROGRESS;
            case CHECKMATE -> CHECKMATE;
            case DRAW -> STALEMATE;
        };
    }
}
