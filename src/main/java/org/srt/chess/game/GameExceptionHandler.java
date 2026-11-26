package org.srt.chess.game;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the game exceptions into problem details (ADR 0010), so that the service and its exceptions know nothing
 * about HTTP. All rules from an error to an HTTP status live here.
 * <p>
 * {@link GameException} is sealed, so the {@code switch} has no default: a new game exception without a status here
 * doesn't compile. The detail is the exception's message. Spring MVC's own errors, such as an id that is not a UUID
 * or a blank move (400), become problem details by themselves, because {@code spring.mvc.problemdetails.enabled} is
 * on. Any other exception stays a 500.
 */
@RestControllerAdvice
class GameExceptionHandler {

    @ExceptionHandler
    ProblemDetail gameError(GameException exception) {
        ProblemDetail problem = switch (exception) {
            case GameNotFoundException _ -> problem(HttpStatus.NOT_FOUND, "Game not found");
            case InvalidMoveException _ -> problem(HttpStatus.BAD_REQUEST, "Invalid move");
            case IllegalMoveException _ -> problem(HttpStatus.UNPROCESSABLE_CONTENT, "Illegal move");
            case StatusChangeNotAllowedException _ ->
                    problem(HttpStatus.UNPROCESSABLE_CONTENT, "Status change not allowed");
            case GameOverException _ -> problem(HttpStatus.CONFLICT, "Game over");
            case ConcurrentChangeException _ -> problem(HttpStatus.CONFLICT, "Conflict");
        };
        problem.setDetail(exception.getMessage());
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String title) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        return problem;
    }
}
