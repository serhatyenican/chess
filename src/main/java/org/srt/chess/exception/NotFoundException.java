package org.srt.chess.exception;

public class NotFoundException extends HttpStatusException {
    public NotFoundException(String message) {
        super(404, message);
    }

    public NotFoundException() {
        super(404);
    }
}
