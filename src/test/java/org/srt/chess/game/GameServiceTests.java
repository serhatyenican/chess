package org.srt.chess.game;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Checks how {@link GameService} turns the persistence errors of a race into a {@link ConcurrentChangeException}.
 * Plain JUnit with a mocked repository: a real race depends on timing, and {@link GameRepositoryTests} shows that the
 * database really raises these errors.
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTests {

    @Mock
    GameRepository gameRepository;

    GameService gameService;

    Game game;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository);
        game = Game.start();
        given(gameRepository.findById(game.id())).willReturn(Optional.of(game));
    }

    @Test
    void reportsTwoMovesRacingEachOtherAsAConcurrentChange() {
        DataIntegrityViolationException violation = violationOf(GameService.ONE_MOVE_PER_PLY);
        given(gameRepository.saveAndFlush(any())).willThrow(violation);

        assertThatThrownBy(() -> gameService.play(game.id(), "e2e4"))
                .isInstanceOf(ConcurrentChangeException.class)
                .hasCause(violation);
    }

    @Test
    void reportsAMoveRacingALeaveAsAConcurrentChange() {
        var failure = new ObjectOptimisticLockingFailureException(Game.class, game.id());
        given(gameRepository.saveAndFlush(any())).willThrow(failure);

        assertThatThrownBy(() -> gameService.play(game.id(), "e2e4"))
                .isInstanceOf(ConcurrentChangeException.class)
                .hasCause(failure);
    }

    @Test
    void reportsALeaveRacingAMoveAsAConcurrentChange() {
        given(gameRepository.saveAndFlush(any()))
                .willThrow(new ObjectOptimisticLockingFailureException(Game.class, game.id()));

        assertThatThrownBy(() -> gameService.changeStatus(game.id(), GameStatus.LEFT))
                .isInstanceOf(ConcurrentChangeException.class);
    }

    @Test
    void leavesAnyOtherConstraintViolationUnchanged() {
        // Any other violation is our own bug: it must stay a 500, not look like a harmless conflict.
        DataIntegrityViolationException violation = violationOf("games_winner_only_on_checkmate");
        given(gameRepository.saveAndFlush(any())).willThrow(violation);

        assertThatThrownBy(() -> gameService.play(game.id(), "e2e4")).isSameAs(violation);
    }

    private static DataIntegrityViolationException violationOf(String constraint) {
        return new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException("constraint violated", new SQLException(), constraint));
    }
}
