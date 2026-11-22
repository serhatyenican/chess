package org.srt.chess.exception;

public class HttpStatusException extends RuntimeException  {
    protected final int code;

    public HttpStatusException(int code, String message) {
        super(message);
        this.code = code;
    }

    public HttpStatusException(int code) {
        this.code = code;
    }
}
