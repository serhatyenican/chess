package org.srt.chess.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.srt.chess.TestcontainersConfiguration;
import org.srt.chess.engine.Color;
import org.srt.chess.engine.Fen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Plays moves through {@code POST /api/games/{id}/moves} over real HTTP, against a Testcontainers Postgres.
 * <p>
 * The response has no move list, so the stored moves are read with SQL. A game that needs a special position, such
 * as one move before stalemate, gets its FEN written with SQL too: {@link Game} only changes its position by playing.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class PlayMoveApiTests {

    // White plays Qb5-b6 and black's king on a8 has no move left, but is not in check.
    private static final String ONE_MOVE_BEFORE_STALEMATE = "k7/8/8/1Q6/8/8/8/K7 w - - 0 1";

    // White's pawn on a7 can promote on a8.
    private static final String PAWN_BEFORE_PROMOTION = "7k/P7/8/8/8/8/8/K7 w - - 0 1";

    @Autowired
    RestTestClient client;

    @Autowired
    JdbcClient jdbcClient;

    @Test
    void playsAMoveAndStoresIt() {
        UUID id = startGame();

        GameResponse game = play(id, "e2e4");

        assertThat(game.sideToMove()).isEqualTo(Color.BLACK);
        assertThat(game.fen()).isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.legalMoves()).hasSize(20).contains("e7e5");
        assertThat(storedMoves(id)).containsExactly(Map.entry(1, "e2e4"));
    }

    @Test
    void numbersEveryStoredMoveInTheOrderPlayed() {
        UUID id = startGame();

        List.of("e2e4", "e7e5", "g1f3").forEach(move -> play(id, move));

        assertThat(storedMoves(id))
                .containsExactly(Map.entry(1, "e2e4"), Map.entry(2, "e7e5"), Map.entry(3, "g1f3"));
    }

    @Test
    void endsTheGameOnCheckmate() {
        UUID id = startGame();

        List.of("f2f3", "e7e5", "g2g4").forEach(move -> play(id, move));
        GameResponse game = play(id, "d8h4");

        assertThat(game.status()).isEqualTo(GameStatus.CHECKMATE);
        assertThat(game.winner()).isEqualTo(Color.BLACK);
        assertThat(game.check()).isTrue();
        assertThat(game.legalMoves()).isEmpty();
    }

    @Test
    void rejectsAMoveAfterCheckmate() {
        UUID id = startGame();
        List.of("f2f3", "e7e5", "g2g4", "d8h4").forEach(move -> play(id, move));

        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", "a2a3"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Game over")
                .jsonPath("$.detail").isEqualTo("Game " + id + " has already ended: CHECKMATE");
        assertThat(storedMoves(id)).hasSize(4);
    }

    @Test
    void endsTheGameOnStalemate() {
        UUID id = startGameAt(ONE_MOVE_BEFORE_STALEMATE);

        GameResponse game = play(id, "b5b6");

        assertThat(game.status()).isEqualTo(GameStatus.STALEMATE);
        assertThat(game.winner()).isNull();
        assertThat(game.check()).isFalse();
        assertThat(game.legalMoves()).isEmpty();
    }

    @Test
    void promotesAPawnToTheChosenPiece() {
        UUID id = startGameAt(PAWN_BEFORE_PROMOTION);

        GameResponse game = play(id, "a7a8n");

        assertThat(game.fen()).startsWith("N6k/");
        assertThat(storedMoves(id)).containsExactly(Map.entry(1, "a7a8n"));
    }

    @ParameterizedTest
    @CsvSource({
            "'" + Fen.STARTING_POSITION + "', e2e5",  // a pawn can't move three squares
            "'" + Fen.STARTING_POSITION + "', e7e5",  // not white's pawn
            "'" + PAWN_BEFORE_PROMOTION + "', a7a8",  // a promotion without the piece
    })
    void rejectsAnIllegalMoveAndChangesNothing(String fen, String move) {
        UUID id = startGameAt(fen);
        GameResponse before = get(id);

        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", move))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Illegal move")
                .jsonPath("$.detail").isEqualTo("The move " + move + " is not legal in this position");

        assertThat(get(id)).isEqualTo(before);
        assertThat(storedMoves(id)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "xyz,   'Invalid move \"xyz\": expected two squares and an optional promotion letter, e.g. \"e2e4\" or \"e7e8q\"'",
            "e7e8k, 'Invalid move \"e7e8k\": a pawn cannot promote to KING'",
    })
    void rejectsTextThatIsNotAMove(String move, String detail) {
        UUID id = startGame();

        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", move))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Invalid move")
                .jsonPath("$.detail").isEqualTo(detail);
        assertThat(storedMoves(id)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "{\"move\": \"  \"}",
            "{\"move\": null}",
            "{}",
            "not json",
            "''",
    })
    void rejectsAMalformedBody(String body) {
        UUID id = startGame();

        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(storedMoves(id)).isEmpty();
    }

    @Test
    void answersAMoveInAnUnknownGameWithNotFound() {
        UUID id = UUID.randomUUID();

        client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", "e2e4"))
                .exchange()
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

    private UUID startGameAt(String fen) {
        UUID id = startGame();
        jdbcClient.sql("update games set fen = ? where id = ?").params(fen, id).update();
        return id;
    }

    private GameResponse get(UUID id) {
        return client.get().uri("/api/games/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private GameResponse play(UUID id, String move) {
        return client.post().uri("/api/games/{id}/moves", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("move", move))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult()
                .getResponseBody();
    }

    // The stored moves as (ply, move) pairs in ply order.
    private List<Map.Entry<Integer, String>> storedMoves(UUID id) {
        return jdbcClient.sql("select ply, move from moves where game_id = ? order by ply")
                .param(id)
                .query((row, number) -> Map.entry(row.getInt("ply"), row.getString("move")))
                .list();
    }
}
