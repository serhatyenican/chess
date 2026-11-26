package org.srt.chess.game;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Turns the game exceptions into problem details (ADR 0010), so that the service and its exceptions know nothing
 * about HTTP. All rules from an error to an HTTP status live here.
 * <p>
 * Spring MVC's own errors, such as an id that is not a UUID or a blank move (400), become problem details by
 * themselves, because {@code spring.mvc.problemdetails.enabled} is on.
 */
@RestControllerAdvice
class GameExceptionHandler {

    /**
     * The unique constraint on {@code (game_id, ply)}. Two moves racing each other fail on it (ADR 0011).
     */
    static final String ONE_MOVE_PER_PLY = "moves_one_per_ply";

    private static final String CHANGED_BY_ANOTHER_REQUEST =
            "The game was changed by another request. Load it again and retry.";

    @ExceptionHandler
    ProblemDetail gameNotFound(GameNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Game not found", exception.getMessage());
    }

    @ExceptionHandler
    ProblemDetail invalidMove(InvalidMoveException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid move", exception.getMessage());
    }

    /**
     * A well-formed request that the rules don't allow: an illegal move, or a status other than {@code LEFT}.
     */
    @ExceptionHandler({IllegalMoveException.class, StatusChangeNotAllowedException.class})
    ProblemDetail unprocessable(TitledException exception) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, exception.title(), exception.getMessage());
    }

    /**
     * The game can't be changed: it has already ended, or another request changed it first. The second is a move
     * racing a change without a new move, such as leaving the game, where the version check fails (ADR 0011). That
     * exception is Spring's, so it has no title of its own.
     */
    @ExceptionHandler({GameOverException.class, ObjectOptimisticLockingFailureException.class})
    ProblemDetail conflict(RuntimeException exception) {
        if (exception instanceof GameOverException gameOver) {
            return problem(HttpStatus.CONFLICT, gameOver.title(), gameOver.getMessage());
        }
        return problem(HttpStatus.CONFLICT, "Conflict", CHANGED_BY_ANOTHER_REQUEST);
    }

    /**
     * Two moves racing each other: the second fails on the unique ply. Any other constraint violation is our own bug,
     * so it is rethrown and stays a 500 instead of looking like a harmless conflict.
     */
    @ExceptionHandler
    ProblemDetail constraintViolated(DataIntegrityViolationException exception) {
        if (!violates(exception, ONE_MOVE_PER_PLY)) {
            throw exception;
        }
        return problem(HttpStatus.CONFLICT, "Conflict", CHANGED_BY_ANOTHER_REQUEST);
    }

    /**
     * Returns whether the exception, or one of its causes, is Hibernate's report of a violation of this constraint.
     */
    static boolean violates(DataIntegrityViolationException exception, String constraint) {
        return Stream.<Throwable>iterate(exception, Objects::nonNull, Throwable::getCause)
                .filter(ConstraintViolationException.class::isInstance)
                .map(ConstraintViolationException.class::cast)
                .anyMatch(violation -> constraint.equals(violation.getConstraintName()));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
