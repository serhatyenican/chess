package org.srt.chess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Reads the OpenAPI spec and Swagger UI over real HTTP (ADR 0012).
 * <p>
 * Besides "it's there", it checks the parts of the spec that are written by hand as annotations, because the
 * frontend's generated types depend on them: a mistake there would only show up as a wrong type in TypeScript.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class OpenApiDocsTests {

    @Autowired
    RestTestClient client;

    @Test
    void servesTheSpecWithTheGameEndpoints() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths['/api/games'].post").exists()
                .jsonPath("$.paths['/api/games/{id}'].get").exists()
                .jsonPath("$.paths['/api/games/{id}'].patch").exists()
                .jsonPath("$.paths['/api/games/{id}/moves'].post").exists();
    }

    @Test
    void documentsTheCreatedGameWithItsLocation() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectBody()
                .jsonPath("$.paths['/api/games'].post.responses['201'].headers.Location").exists()
                .jsonPath("$.paths['/api/games'].post.responses['200']").doesNotExist();
    }

    @Test
    void documentsErrorsAsProblemDetails() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectBody()
                .jsonPath("$.paths['/api/games/{id}/moves'].post.responses['422'].content['application/problem+json']"
                        + ".schema['$ref']").isEqualTo("#/components/schemas/ProblemDetail");
    }

    @Test
    void marksEveryFieldOfTheGameAsRequiredAndTheWinnerAsNullable() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectBody()
                .jsonPath("$.components.schemas.GameResponse.required").value((List<String> required) ->
                        assertThat(required).containsExactlyInAnyOrder(
                                "id", "fen", "sideToMove", "check", "status", "winner", "legalMoves"))
                .jsonPath("$.components.schemas.GameResponse.properties.winner.type").value((List<String> types) ->
                        assertThat(types).containsExactlyInAnyOrder("string", "null"));
    }

    @Test
    void allowsOnlyLeftWhenChangingTheStatus() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectBody()
                .jsonPath("$.components.schemas.ChangeGameRequest.properties.status.enum")
                .value((List<String> values) -> assertThat(values).containsExactly("LEFT"));
    }

    @Test
    void servesSwaggerUi() {
        client.get().uri("/swagger-ui/index.html")
                .exchange()
                .expectStatus().isOk();
    }
}
