package org.srt.chess.game;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * The game resource (ADR 0010). It only maps HTTP to {@link GameService} and back.
 * <p>
 * Errors are problem details. {@link GameExceptionHandler} maps the game's own errors. Spring MVC reports malformed
 * requests as 400 by itself: an id that is not a UUID, a missing body, or a blank {@code move}.
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

    /**
     * Plays a move, e.g. {@code {"move": "e7e8q"}}, and answers 201 with the updated game. A move gets no URL of its
     * own in v1, so there is no {@code Location} header.
     */
    @PostMapping("/{id}/moves")
    @ResponseStatus(HttpStatus.CREATED)
    GameResponse play(@PathVariable UUID id, @Valid @RequestBody PlayMoveRequest request) {
        return gameService.play(id, request.move());
    }

    /**
     * Changes the game's status, and answers 200 with the updated game.
     * <p>
     * Although {@code PATCH} looks like a generic update, only one change is allowed: leaving a game in progress with
     * {@code {"status": "LEFT"}} (ADR 0010). The game then has no winner and no legal moves, and its link still
     * shows it. The errors are checked in this order, first the request and then the game:
     * <ul>
     *   <li>400: a missing or unknown status, or any other field in the body</li>
     *   <li>422: any other status, such as {@code CHECKMATE}, since the other endings come from playing moves</li>
     *   <li>404: no game with this id</li>
     *   <li>409: the game has already ended</li>
     * </ul>
     */
    @PatchMapping("/{id}")
    GameResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeGameRequest request) {
        return gameService.changeStatus(id, request.status());
    }
}
