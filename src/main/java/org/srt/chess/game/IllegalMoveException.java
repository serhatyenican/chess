package org.srt.chess.game;

import org.srt.chess.engine.Move;

/**
 * Thrown when a well-formed move is not legal in the game's position, for example a pawn moving three squares or a
 * promotion without the piece. {@link GameExceptionHandler} turns it into a 422.
 */
final class IllegalMoveException extends GameException {

    IllegalMoveException(Move move) {
        super("The move " + move + " is not legal in this position");
    }
}
