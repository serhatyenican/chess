package org.srt.chess.engine;

import java.util.Objects;

/**
 * A chess piece of one color, e.g. a white knight.
 */
public record Piece(Color color, PieceType type) {

    public Piece {
        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }
}
