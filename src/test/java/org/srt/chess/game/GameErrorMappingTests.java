package org.srt.chess.game;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * Checks how {@link GameExceptionHandler} answers errors that a real race between two requests would cause. A race
 * over HTTP depends on timing, so here the service is a mock that throws the errors directly. That the database
 * really raises them is shown in {@link GameRepositoryTests}.
 * <p>
 * {@code @WebMvcTest} starts only the web layer: the controller and the exception handler, without a database.
 */
@WebMvcTest(GameController.class)
class GameErrorMappingTests {

    private static final UUID ID = UUID.randomUUID();

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    GameService gameService;

    @Test
    void answersTwoMovesRacingEachOtherWithConflict() {
        given(gameService.play(any(), anyString())).willThrow(violationOf(GameExceptionHandler.ONE_MOVE_PER_PLY));

        assertThat(playMove())
                .hasStatus(409)
                .bodyJson().extractingPath("$.detail")
                .isEqualTo("The game was changed by another request. Load it again and retry.");
    }

    @Test
    void answersAMoveRacingAnotherChangeWithConflict() {
        given(gameService.play(any(), anyString()))
                .willThrow(new ObjectOptimisticLockingFailureException(Game.class, ID));

        assertThat(playMove()).hasStatus(409);
    }

    @Test
    void leavesAnyOtherConstraintViolationAnInternalError() {
        DataIntegrityViolationException violation = violationOf("games_winner_only_on_checkmate");
        given(gameService.play(any(), anyString())).willThrow(violation);

        // Without a servlet container, an unhandled exception reaches the test, wrapped by the dispatcher servlet,
        // instead of becoming a 500 page.
        assertThat(playMove()).hasFailed().failure().cause().isSameAs(violation);
    }

    private MockMvcTester.MockMvcRequestBuilder playMove() {
        return mvc.post().uri("/api/games/{id}/moves", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"move\": \"e2e4\"}");
    }

    private static DataIntegrityViolationException violationOf(String constraint) {
        return new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException("duplicate key", new SQLException(), constraint));
    }
}
