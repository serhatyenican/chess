package org.srt.chess.game;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the game exceptions into problem details (ADR 0010), so that the service and its exceptions know nothing
 * about HTTP. All rules from an error to an HTTP status live here.
 * <p>
 * Spring MVC's own errors, such as an id that is not a UUID (400), become problem details by themselves, because
 * {@code spring.mvc.problemdetails.enabled} is on.
 */
@RestControllerAdvice
class GameExceptionHandler {

    @ExceptionHandler
    ProblemDetail gameNotFound(GameNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Game not found");
        return problem;
    }
}
