package org.srt.chess.game;

import java.util.UUID;

/**
 * Thrown when a move or another change is made to a game that has already ended. {@link GameExceptionHandler} turns it
 * into a 409.
 */
class GameOverException extends TitledException {

    GameOverException(UUID id, GameStatus status) {
        super("Game over", "Game " + id + " has already ended: " + status);
    }
}
