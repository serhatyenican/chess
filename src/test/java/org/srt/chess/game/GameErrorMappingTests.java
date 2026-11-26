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
 * Checks how {@link GameExceptionHandler} answers a race between two requests, which can't be caused reliably over
 * real HTTP. The service is a mock that throws directly; {@link GameServiceTests} shows when the real service throws
 * {@link ConcurrentChangeException}.
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
    void answersAConcurrentChangeWithConflict() {
        given(gameService.play(any(), anyString()))
                .willThrow(new ConcurrentChangeException(new ObjectOptimisticLockingFailureException(Game.class, ID)));

        assertThat(playMove())
                .hasStatus(409)
                .bodyJson().satisfies(body -> {
                    assertThat(body).extractingPath("$.title").isEqualTo("Conflict");
                    assertThat(body).extractingPath("$.detail")
                            .isEqualTo("The game was changed by another request. Load it again and retry.");
                });
    }

    @Test
    void leavesAnExceptionThatIsNotAGameExceptionAnInternalError() {
        DataIntegrityViolationException violation = new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException("constraint violated", new SQLException(),
                        "games_winner_only_on_checkmate"));
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
}
