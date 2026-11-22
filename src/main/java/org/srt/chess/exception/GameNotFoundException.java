package org.srt.chess.exception;

public class GameNotFoundException extends NotFoundException {
    public GameNotFoundException(String id) {
        super("Game not found: " + id);
    }
}
