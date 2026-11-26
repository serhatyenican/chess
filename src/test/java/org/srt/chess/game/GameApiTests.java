package org.srt.chess.game;

import org.junit.jupiter.api.Test;
import org.srt.chess.TestcontainersConfiguration;
import org.srt.chess.engine.Color;
import org.srt.chess.engine.Fen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Calls the game endpoints over real HTTP on a random port, against a Testcontainers Postgres.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class GameApiTests {

    @Autowired
    RestTestClient client;

    @Test
    void startsAGameInTheStartingPosition() {
        EntityExchangeResult<GameResponse> result = client.post().uri("/api/games")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponse.class)
                .returnResult();

        GameResponse game = result.getResponseBody();
        assertThat(game).isNotNull();
        assertThat(result.getResponseHeaders().getLocation())
                .hasPath("/api/games/" + game.id())
                .hasScheme("http");
        assertThat(game.fen()).isEqualTo(Fen.STARTING_POSITION);
        assertThat(game.sideToMove()).isEqualTo(Color.WHITE);
        assertThat(game.check()).isFalse();
        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.winner()).isNull();
        assertThat(game.legalMoves()).hasSize(20).contains("e2e4", "g1f3");
    }

    @Test
    void writesEveryFieldOfTheGame() {
        // The JSON shape is the contract with the frontend, so the field names and a null winner are checked too.
        client.post().uri("/api/games")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .json("""
                        {
                          "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                          "sideToMove": "WHITE",
                          "check": false,
                          "status": "IN_PROGRESS",
                          "winner": null
                        }
                        """)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.legalMoves.length()").isEqualTo(20)
                .jsonPath("$.moves").doesNotExist();
    }

    @Test
    void opensAStartedGameByItsLink() {
        EntityExchangeResult<GameResponse> started = client.post().uri("/api/games")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult();
        URI location = started.getResponseHeaders().getLocation();

        client.get().uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .isEqualTo(started.getResponseBody());
    }

    @Test
    void answersAnUnknownGameWithNotFound() {
        UUID id = UUID.randomUUID();

        client.get().uri("/api/games/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.title").isEqualTo("Game not found")
                .jsonPath("$.detail").isEqualTo("No game with id " + id);
    }

    @Test
    void answersAnIdThatIsNotAUuidWithBadRequest() {
        client.get().uri("/api/games/not-a-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.title").isEqualTo("Bad Request");
    }
}
