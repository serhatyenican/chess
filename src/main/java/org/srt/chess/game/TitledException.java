package org.srt.chess.game;

/**
 * An exception with a short title for its kind of error, such as "Illegal move". {@link GameExceptionHandler} uses
 * the title for the problem detail, so one handler can answer several exceptions that share an HTTP status.
 */
abstract class TitledException extends RuntimeException {

    private final String title;

    TitledException(String title, String message) {
        super(message);
        this.title = title;
    }

    String title() {
        return title;
    }
}
