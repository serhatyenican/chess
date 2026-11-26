package org.srt.chess;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "Chess API", version = "v1",
        description = "Solo chess games: the server checks every move (ADR 0010, ADR 0012)."))
@SpringBootApplication
public class ChessApplication {

    static void main(String[] args) {
        SpringApplication.run(ChessApplication.class, args);
    }

}
