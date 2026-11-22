package org.srt.chess;

import org.springframework.boot.SpringApplication;

/**
 * Runs the app locally against a Testcontainers Postgres instead of docker-compose:
 * {@code ./gradlew bootTestRun}
 */
public class TestChessApplication {

    public static void main(String[] args) {
        SpringApplication.from(ChessApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
