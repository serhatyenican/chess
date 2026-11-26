package org.srt.chess.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.srt.chess.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Leaves games through {@code PATCH /api/games/{id}} over real HTTP, against a Testcontainers Postgres.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class LeaveGameApiTests {

    private static final String LEAVE = "{\"status\": \"LEFT\"}";

    @Autowired
    RestTestClient client;

    @Test
    void leavesAGameInProgress() {
        UUID id = startGame();

        GameResponse game = client.patch().uri("/api/games/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(LEAVE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(game).isNotNull();
        assertThat(game.status()).isEqualTo(GameStatus.LEFT);
        assertThat(game.winner()).isNull();
        assertThat(game.legalMoves()).isEmpty();
        assertThat(get(id)).isEqualTo(game);
    }

    @Test
    void rejectsLeavingTwice() {
        UUID id = startGame();
        leave(id);

        patch(id, LEAVE)
                .expectStatus().isEqualTo(409)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Game over")
                .jsonPath("$.detail").isEqualTo("Game " + id + " has already ended: LEFT");
    }

    @Test
    void rejectsAMoveAfterLeaving() {
        UUID id = startGame();
        leave(id);

        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", "e2e4"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Game " + id + " has already ended: LEFT");
    }

    @Test
    void rejectsLeavingAGameThatEndedInCheckmate() {
        UUID id = startGame();
        List.of("f2f3", "e7e5", "g2g4", "d8h4").forEach(move -> play(id, move));

        patch(id, LEAVE)
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Game " + id + " has already ended: CHECKMATE");
        assertThat(get(id).status()).isEqualTo(GameStatus.CHECKMATE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECKMATE", "STALEMATE", "IN_PROGRESS"})
    void rejectsAnyOtherStatusAndChangesNothing(String status) {
        UUID id = startGame();
        GameResponse before = get(id);

        patch(id, "{\"status\": \"" + status + "\"}")
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Status change not allowed")
                .jsonPath("$.detail").isEqualTo("The status can only be changed to LEFT, not to " + status);
        assertThat(get(id)).isEqualTo(before);
    }

    @Test
    void rejectsAnotherStatusEvenForAGameThatHasEnded() {
        // A status other than LEFT is never allowed, so that error wins over "the game has already ended".
        UUID id = startGame();
        leave(id);

        patch(id, "{\"status\": \"CHECKMATE\"}").expectStatus().isEqualTo(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"status\": \"FOO\"}",
            "{\"status\": null}",
            "{}",
            "{\"fen\": \"8/8/8/8/8/8/8/8 w - - 0 1\"}",
            "{\"status\": \"LEFT\", \"winner\": \"WHITE\"}",
            "not json",
    })
    void rejectsAMalformedBodyAndChangesNothing(String body) {
        UUID id = startGame();
        GameResponse before = get(id);

        patch(id, body)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(get(id)).isEqualTo(before);
    }

    @Test
    void answersLeavingAnUnknownGameWithNotFound() {
        UUID id = UUID.randomUUID();

        patch(id, LEAVE)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("No game with id " + id);
    }

    private UUID startGame() {
        GameResponse game = client.post().uri("/api/games")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(game).isNotNull();
        return game.id();
    }

    private GameResponse get(UUID id) {
        return client.get().uri("/api/games/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void play(UUID id, String move) {
        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", move))
                .exchange()
                .expectStatus().isCreated();
    }

    private void leave(UUID id) {
        patch(id, LEAVE).expectStatus().isOk();
    }

    private RestTestClient.ResponseSpec patch(UUID id, String body) {
        return client.patch().uri("/api/games/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }
}
