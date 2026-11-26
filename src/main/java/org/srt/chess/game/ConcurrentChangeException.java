package org.srt.chess.game;

/**
 * Thrown when another request changed the game first, for example the same game open in two tabs (ADR 0011).
 * {@link GameService} throws it in place of the persistence error that reports the conflict, which is its cause.
 * {@link GameExceptionHandler} turns it into a 409.
 */
final class ConcurrentChangeException extends GameException {

    ConcurrentChangeException(Throwable cause) {
        super("The game was changed by another request. Load it again and retry.", cause);
    }
}
