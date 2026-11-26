package org.srt.chess.game;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * The game resource (ADR 0010). It only maps HTTP to {@link GameService} and back.
 * <p>
 * Errors are problem details: 404 for an unknown game (see {@link GameExceptionHandler}), and 400 for an id that is
 * not a UUID, which Spring MVC reports by itself when it can't convert the path variable.
 */
@RestController
@RequestMapping("/api/games")
class GameController {

    private final GameService gameService;

    GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Starts a game. Answers 201 with the game, and its URL in the {@code Location} header.
     */
    @PostMapping
    ResponseEntity<GameResponse> start() {
        GameResponse game = gameService.start();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(game.id())
                .toUri();
        return ResponseEntity.created(location).body(game);
    }

    /**
     * Returns the game, so its link opens it again, also after the browser was closed.
     */
    @GetMapping("/{id}")
    GameResponse get(@PathVariable UUID id) {
        return gameService.get(id);
    }
}
