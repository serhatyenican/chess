package org.srt.chess.game;

/**
 * The errors of the game logic. They carry a message but no HTTP status, so callers other than the REST API, such as
 * the STOMP handlers of v2, can use them too.
 * <p>
 * The class is sealed: {@link GameExceptionHandler} maps every permitted exception to a status in a {@code switch}
 * without a default, so a new exception that isn't mapped there doesn't compile.
 */
abstract sealed class GameException extends RuntimeException
        permits GameNotFoundException, InvalidMoveException, IllegalMoveException, StatusChangeNotAllowedException,
        GameOverException, ConcurrentChangeException {

    GameException(String message) {
        super(message);
    }

    GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
