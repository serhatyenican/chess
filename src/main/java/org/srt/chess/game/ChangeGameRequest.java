package org.srt.chess.game;

import jakarta.validation.constraints.NotNull;

/**
 * The body of {@code PATCH /api/games/{id}}. A status that doesn't exist fails when the body is read (400), while an
 * existing status other than {@code LEFT} is rejected by {@link GameService#changeStatus} (422).
 *
 * @param status the new status; only {@code LEFT} is allowed
 */
record ChangeGameRequest(@NotNull GameStatus status) {
}
