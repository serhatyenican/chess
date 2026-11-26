package org.srt.chess.game;

import java.util.UUID;

/**
 * Thrown when no game has the requested id. It carries no HTTP status, so callers other than the REST API, such as
 * the STOMP handlers of v2, can use it too. {@link GameExceptionHandler} turns it into a 404.
 */
final class GameNotFoundException extends GameException {

    GameNotFoundException(UUID id) {
        super("No game with id " + id);
    }
}
