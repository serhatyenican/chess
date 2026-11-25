package org.srt.chess.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The six kinds of chess pieces, without color. A colored piece is a {@link Piece}.
 */
public enum PieceType {
    KING('k', new KingMovement()),
    QUEEN('q', new Sliding(Offset.QUEEN_DIRECTIONS)),
    ROOK('r', new Sliding(Offset.ROOK_DIRECTIONS)),
    BISHOP('b', new Sliding(Offset.BISHOP_DIRECTIONS)),
    KNIGHT('n', new Stepping(Offset.KNIGHT_JUMPS)),
    PAWN('p', new PawnMovement());

    private static final PieceType[] VALUES = values();

    private static final List<PieceType> PROMOTION_TYPES = List.of(QUEEN, ROOK, BISHOP, KNIGHT);

    private final char letter;
    private final Movement movement;

    PieceType(char letter, Movement movement) {
        this.letter = letter;
        this.movement = movement;
    }

    /**
     * Returns the lowercase letter of this type in chess notation, e.g. {@code 'n'} for a knight.
     * {@link Piece#letter()} turns it uppercase for white.
     */
    public char letter() {
        return letter;
    }

    /**
     * Returns how pieces of this type move and attack.
     */
    Movement movement() {
        return movement;
    }

    /**
     * Returns whether a pawn can promote to this type: a queen, rook, bishop or knight, not a king or another pawn.
     */
    public boolean canPromoteTo() {
        return PROMOTION_TYPES.contains(this);
    }

    /**
     * Returns the types a pawn can promote to, strongest first: queen, rook, bishop, knight.
     */
    static List<PieceType> promotionTypes() {
        return PROMOTION_TYPES;
    }

    /**
     * Returns the type for its lowercase letter, or an empty Optional for any other character. Unlike the other
     * {@code fromLetter} methods it doesn't throw: its caller {@link Piece#fromLetter} reports the original,
     * possibly uppercase letter itself.
     */
    public static Optional<PieceType> fromLetter(char letter) {
        return Arrays.stream(VALUES)
                .filter(type -> type.letter == letter)
                .findFirst();
    }
}
