package org.srt.chess.game;

/**
 * Thrown when the text of a move can't be read, e.g. {@code "xyz"}. Its message is the one from
 * {@link org.srt.chess.engine.Move#of(String)}, which names the text and what is wrong. {@link GameExceptionHandler}
 * turns it into a 400.
 */
final class InvalidMoveException extends GameException {

    InvalidMoveException(IllegalArgumentException cause) {
        super(cause.getMessage(), cause);
    }
}
