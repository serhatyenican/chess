package org.srt.chess.game;

import io.swagger.v3.oas.annotations.media.Schema;
import org.srt.chess.engine.Color;
import org.srt.chess.engine.Fen;
import org.srt.chess.engine.Move;
import org.srt.chess.engine.Position;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * A game as every game endpoint returns it.
 * <p>
 * The moves played so far are stored, but not part of the response: the board draws the position from the FEN, and
 * nothing in v1 shows the history. A feature that needs them, such as highlighting the last move, adds a field.
 * <p>
 * Every field is always in the JSON, also when it is {@code null}, so each is marked as required in the OpenAPI spec
 * (ADR 0012). Without that, the generated TypeScript types would make every field optional.
 * <p>
 * The fields are described in their {@code @Schema} annotations instead of {@code @param} tags, so the text reaches
 * the spec, Swagger UI and the frontend's generated types.
 */
public record GameResponse(
        @Schema(requiredMode = REQUIRED, description = "The game's id, also the last part of its URL")
        UUID id,

        @Schema(requiredMode = REQUIRED, description = "The current position in FEN")
        String fen,

        @Schema(requiredMode = REQUIRED, description = "Whose turn it is, read from the FEN")
        Color sideToMove,

        @Schema(requiredMode = REQUIRED, description = "Whether the side to move is in check")
        boolean check,

        @Schema(requiredMode = REQUIRED, description = "Whether the game goes on, and if not, how it ended")
        GameStatus status,

        @Schema(requiredMode = REQUIRED, nullable = true, description = "The winner of a checkmate, otherwise null")
        Color winner,

        @Schema(requiredMode = REQUIRED, description = "The moves the side to move can play, in UCI notation such as "
                + "\"e7e8q\", so the board needs no copy of the rules; empty once the game has ended")
        List<String> legalMoves) {

    static GameResponse of(Game game) {
        Position position = Fen.parse(game.fen());
        List<String> legalMoves = game.status() == GameStatus.IN_PROGRESS
                ? position.legalMoves().stream().map(Move::toString).toList()
                : List.of();
        return new GameResponse(
                game.id(),
                game.fen(),
                position.sideToMove(),
                position.isInCheck(),
                game.status(),
                game.winner().orElse(null),
                legalMoves);
    }
}
