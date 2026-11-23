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

    /**
     * Returns the piece for its letter in chess notation (as used in FEN): {@code K Q R B N P} for white,
     * {@code k q r b n p} for black.
     *
     * @throws IllegalArgumentException for any other character
     */
    public static Piece fromLetter(char letter) {
        Color color = Character.isUpperCase(letter) ? Color.WHITE : Color.BLACK;
        return PieceType.fromLetter(Character.toLowerCase(letter))
                .map(type -> new Piece(color, type))
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown piece letter '" + letter + "', expected one of KQRBNPkqrbnp"));
    }

    /**
     * Returns the piece's letter in chess notation: uppercase for white, lowercase for black, e.g. {@code 'N'} for
     * a white knight and {@code 'p'} for a black pawn.
     */
    public char letter() {
        return color == Color.WHITE ? Character.toUpperCase(type.letter()) : type.letter();
    }
}
