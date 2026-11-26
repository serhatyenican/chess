package org.srt.chess.game;

import org.srt.chess.engine.Move;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
     * Plays a move in the game with this id and stores it. The game is flushed here, so a conflict with another
     * request shows up in this method rather than at commit, and the response has the new version.
     *
     * @param text the move in UCI notation, e.g. {@code "e7e8q"}
     * @throws GameNotFoundException if there is no such game
     * @throws InvalidMoveException  if the text is not a move
     * @throws GameOverException     if the game has already ended
     * @throws IllegalMoveException  if the move is not legal in the game's position
     */
    GameResponse play(UUID id, String text) {
        Game game = find(id);
        game.play(readMove(text));
        return GameResponse.of(gameRepository.saveAndFlush(game));
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
