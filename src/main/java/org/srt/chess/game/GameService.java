package org.srt.chess.game;

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
        return GameResponse.of(gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException(id)));
    }
}
