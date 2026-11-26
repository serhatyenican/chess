package org.srt.chess.game;

import java.util.UUID;

/**
 * Thrown when a move or another change is made to a game that has already ended. {@link GameExceptionHandler} turns it
 * into a 409.
 */
final class GameOverException extends GameException {

    GameOverException(UUID id, GameStatus status) {
        super("Game " + id + " has already ended: " + status);
    }
}
