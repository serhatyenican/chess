package org.srt.chess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Calls the health endpoint over real HTTP on a random port, against a Testcontainers Postgres.
 * <p>
 * In production, health shows only the overall status. Here we also show the components
 * (names and status, no details), so the test can check that the database is one of them.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = "management.endpoint.health.show-components=always")
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class HealthEndpointTests {

    @Autowired
    RestTestClient client;

    @Test
    void healthIsUp() {
        client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.components.db.status").isEqualTo("UP");
    }

    @Test
    void otherActuatorEndpointsAreNotExposed() {
        client.get().uri("/actuator/env")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }
}
