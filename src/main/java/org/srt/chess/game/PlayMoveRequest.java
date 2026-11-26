package org.srt.chess.game;

import jakarta.validation.constraints.NotBlank;

/**
 * The body of {@code POST /api/games/{id}/moves}.
 *
 * @param move the move in UCI notation, e.g. {@code "e2e4"} or {@code "e7e8q"}
 */
record PlayMoveRequest(@NotBlank String move) {
}
