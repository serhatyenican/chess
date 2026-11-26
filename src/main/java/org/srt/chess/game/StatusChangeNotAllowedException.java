package org.srt.chess.game;

/**
 * Thrown when a request asks for a status other than {@link GameStatus#LEFT}. The other endings come from playing
 * moves, so a client can't set them. {@link GameExceptionHandler} turns it into a 422.
 */
class StatusChangeNotAllowedException extends TitledException {

    StatusChangeNotAllowedException(GameStatus requested) {
        super("Status change not allowed", "The status can only be changed to LEFT, not to " + requested);
    }
}
