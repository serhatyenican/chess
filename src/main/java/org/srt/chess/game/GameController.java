package org.srt.chess.game;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
 * <p>
 * The OpenAPI annotations (ADR 0012) add only what springdoc can't see in the code: the 201 and {@code Location} of
 * {@link #start()}, and the error answers, which come from the exception handler.
 */
@Tag(name = "Games", description = "Start a game, open it again, play moves, leave it")
@RestController
@RequestMapping("/api/games")
class GameController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final GameService gameService;

    GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Starts a game. Answers 201 with the game, and its URL in the {@code Location} header.
     */
    @Operation(summary = "Start a game")
    @ApiResponse(responseCode = "201", description = "The new game",
            headers = @Header(name = "Location", description = "The game's URL",
                    schema = @Schema(type = "string", format = "uri")),
            content = @Content(schema = @Schema(implementation = GameResponse.class)))
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
    @Operation(summary = "Get a game")
    @ApiResponse(responseCode = "200", description = "The game")
    @ApiResponse(responseCode = "400", description = "The id is not a UUID", content = @Content(
            mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No game with this id", content = @Content(
            mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    GameResponse get(@PathVariable UUID id) {
        return gameService.get(id);
    }

    /**
     * Plays a move, e.g. {@code {"move": "e7e8q"}}, and answers 201 with the updated game. A move gets no URL of its
     * own in v1, so there is no {@code Location} header.
     */
    @Operation(summary = "Play a move")
    @ApiResponse(responseCode = "201", description = "The game after the move")
    @ApiResponse(responseCode = "400", description = "The id is not a UUID, or the move is blank or not a move",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No game with this id", content = @Content(
            mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "The game has ended, or another request changed it first",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The move is not legal in this position", content = @Content(
            mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
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
    @Operation(summary = "Leave a game", description = "Only `{\"status\": \"LEFT\"}` is allowed.")
    @ApiResponse(responseCode = "200", description = "The game, now ended with status LEFT")
    @ApiResponse(responseCode = "400", description = "A missing or unknown status, or another field in the body",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No game with this id", content = @Content(
            mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "The game has already ended, or another request changed it first",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "A status other than LEFT", content = @Content(
            mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{id}")
    GameResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeGameRequest request) {
        return gameService.changeStatus(id, request.status());
    }
}
