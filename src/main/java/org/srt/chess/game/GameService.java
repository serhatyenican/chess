package org.srt.chess.game;

import org.hibernate.exception.ConstraintViolationException;
import org.srt.chess.engine.Move;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The game logic between the API and the engine. It knows nothing about HTTP, so the STOMP handlers of v2 can use it
 * too (ADR 0003).
 * <p>
 * Each method maps the game to its response inside its transaction. The response doesn't read the lazily loaded moves
 * today, but with {@code open-in-view} off, anything lazy must be read here and not in the controller.
 */
@Service
@Transactional
class GameService {

    /**
     * The unique constraint on {@code (game_id, ply)}. Two moves racing each other fail on it (ADR 0011).
     */
    static final String ONE_MOVE_PER_PLY = "moves_one_per_ply";

    private final GameRepository gameRepository;

    GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Starts a game in the starting position and saves it.
     */
    GameResponse start() {
        return GameResponse.of(gameRepository.save(Game.start()));
    }

    /**
     * Returns the game with this id.
     *
     * @throws GameNotFoundException if there is none
     */
    @Transactional(readOnly = true)
    GameResponse get(UUID id) {
        return GameResponse.of(find(id));
    }

    /**
     * Plays a move in the game with this id and stores it.
     *
     * @param text the move in UCI notation, e.g. {@code "e7e8q"}
     * @throws GameNotFoundException     if there is no such game
     * @throws InvalidMoveException      if the text is not a move
     * @throws GameOverException         if the game has already ended
     * @throws IllegalMoveException      if the move is not legal in the game's position
     * @throws ConcurrentChangeException if another request changed the game first
     */
    GameResponse play(UUID id, String text) {
        Game game = find(id);
        game.play(readMove(text));
        return GameResponse.of(save(game));
    }

    /**
     * Changes the status of the game with this id. The only change allowed is leaving: {@code IN_PROGRESS} to
     * {@code LEFT}. The other endings come from playing moves.
     * <p>
     * The requested status is checked before the game is loaded: a status other than {@code LEFT} is wrong for any
     * game, so it needs no database query.
     *
     * @throws StatusChangeNotAllowedException if the requested status is not {@code LEFT}
     * @throws GameNotFoundException           if there is no such game
     * @throws GameOverException               if the game has already ended
     * @throws ConcurrentChangeException       if another request changed the game first
     */
    GameResponse changeStatus(UUID id, GameStatus requested) {
        if (requested != GameStatus.LEFT) {
            throw new StatusChangeNotAllowedException(requested);
        }
        Game game = find(id);
        game.leave();
        return GameResponse.of(save(game));
    }

    /**
     * Saves and flushes the game, so a conflict with another request fails here rather than at commit, after the
     * method has returned. The two errors that report such a conflict become a {@link ConcurrentChangeException}:
     * the version check, and the unique ply when two moves race (ADR 0011). Any other constraint violation is our own
     * bug, so it is rethrown unchanged and stays a 500.
     */
    private Game save(Game game) {
        try {
            return gameRepository.saveAndFlush(game);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentChangeException(e);
        } catch (DataIntegrityViolationException e) {
            if (!violates(e, ONE_MOVE_PER_PLY)) {
                throw e;
            }
            throw new ConcurrentChangeException(e);
        }
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

    private Game find(UUID id) {
        return gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
    }

    // Only the parsing is caught: any other IllegalArgumentException is our own bug and must stay a 500.
    private static Move readMove(String text) {
        try {
            return Move.of(text);
        } catch (IllegalArgumentException e) {
            throw new InvalidMoveException(e);
        }
    }
}
