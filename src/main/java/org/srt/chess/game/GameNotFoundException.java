package org.srt.chess.game;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

/**
 * Thrown when no game has the requested id. As an {@link ErrorResponseException}, Spring MVC answers it with a 404
 * problem detail by itself, so it needs no exception handler.
 */
class GameNotFoundException extends ErrorResponseException {

    GameNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, problem(id), null);
    }

    private static ProblemDetail problem(UUID id) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "No game with id " + id);
        problem.setTitle("Game not found");
        return problem;
    }
}
