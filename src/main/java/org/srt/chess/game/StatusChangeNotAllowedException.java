package org.srt.chess.game;

/**
 * Thrown when a request asks for a status other than {@link GameStatus#LEFT}. The other endings come from playing
 * moves, so a client can't set them. {@link GameExceptionHandler} turns it into a 422.
 */
final class StatusChangeNotAllowedException extends GameException {

    StatusChangeNotAllowedException(GameStatus requested) {
        super("The status can only be changed to LEFT, not to " + requested);
    }
}
