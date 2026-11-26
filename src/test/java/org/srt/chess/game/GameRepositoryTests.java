package org.srt.chess.game;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.srt.chess.TestcontainersConfiguration;
import org.srt.chess.engine.Fen;
import org.srt.chess.engine.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Saves and loads games against a Testcontainers Postgres with the Flyway schema. {@code @DataJpaTest} starts only
 * JPA, Flyway and the database, and rolls back each test's transaction at the end.
 * <p>
 * Most tests flush and clear the persistence context before loading, so the game really comes from the database and
 * not from Hibernate's cache of the objects it already knows.
 */
@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GameRepositoryTests {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void newGameIsInProgressInTheStartingPosition() {
        Game game = Game.start();

        assertThat(game.id()).isNotNull();
        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.winner()).isEmpty();
        assertThat(game.fen()).isEqualTo(Fen.STARTING_POSITION);
        assertThat(game.moves()).isEmpty();
    }

    @Test
    void savesANewGameWithoutCopyingIt() {
        Game game = Game.start();

        // save() returns the same object only if it inserts the game (persist). If it took the game for an existing
        // one, it would merge it and return a copy. This pins the Long version in Game (ADR 0011).
        assertThat(gameRepository.save(game)).isSameAs(game);
    }

    @Test
    void loadsASavedGameWithItsMovesInPlyOrder() {
        Game game = Game.start();
        game.addMove(Move.of("e2e4"));
        game.addMove(Move.of("e7e5"));
        gameRepository.saveAndFlush(game);
        entityManager.clear();

        Game loaded = gameRepository.findById(game.id()).orElseThrow();

        assertThat(loaded).isNotSameAs(game);
        assertThat(loaded.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(loaded.winner()).isEmpty();
        assertThat(loaded.fen()).isEqualTo(Fen.STARTING_POSITION);
        assertThat(loaded.createdAt()).isNotNull();
        assertThat(loaded.updatedAt()).isNotNull();
        assertThat(loaded.moves())
                .extracting(GameMove::ply, GameMove::move)
                .containsExactly(
                        tuple(1, Move.of("e2e4")),
                        tuple(2, Move.of("e7e5")));
        assertThat(loaded.moves()).allSatisfy(move -> assertThat(move.playedAt()).isNotNull());
    }

    @Test
    void numbersTheMovesOfEachGameFromOne() {
        Game first = Game.start();
        first.addMove(Move.of("e2e4"));
        Game second = Game.start();
        second.addMove(Move.of("d2d4"));
        gameRepository.saveAndFlush(first);
        gameRepository.saveAndFlush(second);
        entityManager.clear();

        assertThat(gameRepository.findById(first.id()).orElseThrow().moves()).extracting(GameMove::ply).containsExactly(1);
        assertThat(gameRepository.findById(second.id()).orElseThrow().moves()).extracting(GameMove::ply).containsExactly(1);
    }

    @Test
    void findsNoGameForAnUnknownId() {
        assertThat(gameRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void rejectsSavingAGameThatChangedInTheMeantime() {
        UUID id = gameRepository.saveAndFlush(Game.start()).id();
        entityManager.clear();
        Game stale = gameRepository.findById(id).orElseThrow();
        entityManager.detach(stale);

        // Another request saves the game in the meantime, which increases its version.
        execute("update games set version = version + 1 where id = ?1", id);

        // The stale copy still has the old version, so saving it must fail instead of overwriting that change.
        assertThatThrownBy(() -> gameRepository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void rejectsAMoveThatAnotherRequestStoredFirst() {
        Game game = gameRepository.saveAndFlush(Game.start());
        assertThat(game.moves()).isEmpty();

        // Another request stores its first move before this one: the same race as two tabs playing at once.
        execute("insert into moves (game_id, ply, move, played_at) values (?1, 1, 'd2d4', now())", game.id());

        game.play(Move.of("e2e4"));
        assertThatThrownBy(() -> gameRepository.saveAndFlush(game))
                .isInstanceOfSatisfying(DataIntegrityViolationException.class, exception ->
                        assertThat(GameExceptionHandler.violates(exception, GameExceptionHandler.ONE_MOVE_PER_PLY))
                                .as("recognized as a race by the exception handler")
                                .isTrue());
    }

    // The rules below can't be broken through Game, so these tests write SQL directly to check that the database
    // enforces them as well.

    @Test
    void rejectsASecondMoveWithTheSamePly() {
        Game game = Game.start();
        game.addMove(Move.of("e2e4"));
        gameRepository.saveAndFlush(game);

        assertThatThrownBy(() -> execute(
                "insert into moves (game_id, ply, move, played_at) values (?1, 1, 'd2d4', now())", game.id()))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("moves_one_per_ply");
    }

    @Test
    void rejectsAMoveWithoutAPositivePly() {
        Game game = gameRepository.saveAndFlush(Game.start());

        assertThatThrownBy(() -> execute(
                "insert into moves (game_id, ply, move, played_at) values (?1, 0, 'e2e4', now())", game.id()))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("moves_ply_positive");
    }

    @ParameterizedTest
    @CsvSource(nullValues = "none", value = {
            "DRAW,        none,  games_status_known",
            "CHECKMATE,   GREEN, games_winner_known",
            "CHECKMATE,   none,  games_winner_only_on_checkmate",
            "IN_PROGRESS, WHITE, games_winner_only_on_checkmate",
            "STALEMATE,   BLACK, games_winner_only_on_checkmate",
            "LEFT,        WHITE, games_winner_only_on_checkmate",
    })
    void rejectsAnInvalidStatusAndWinner(String status, String winner, String constraint) {
        Game game = gameRepository.saveAndFlush(Game.start());

        assertThatThrownBy(() -> execute(
                "update games set status = ?2, winner = ?3 where id = ?1", game.id(), status, winner))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining(constraint);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "none", value = {
            "CHECKMATE, WHITE",
            "CHECKMATE, BLACK",
            "STALEMATE, none",
            "LEFT,      none",
    })
    void acceptsAValidStatusAndWinner(String status, String winner) {
        Game game = gameRepository.saveAndFlush(Game.start());

        execute("update games set status = ?2, winner = ?3 where id = ?1", game.id(), status, winner);
        entityManager.clear();

        Game loaded = gameRepository.findById(game.id()).orElseThrow();
        assertThat(loaded.status()).isEqualTo(GameStatus.valueOf(status));
        assertThat(loaded.winner().map(Enum::name)).isEqualTo(Optional.ofNullable(winner));
    }

    private void execute(String sql, Object... parameters) {
        var query = entityManager.getEntityManager().createNativeQuery(sql);
        for (int i = 0; i < parameters.length; i++) {
            query.setParameter(i + 1, parameters[i]);
        }
        query.executeUpdate();
    }
}
