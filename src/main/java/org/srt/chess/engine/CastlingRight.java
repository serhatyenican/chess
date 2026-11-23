package org.srt.chess.engine;

import java.util.Arrays;

/**
 * One of the four castling rights. A right only says castling is still allowed later (king and rook haven't moved);
 * whether castling is possible right now is decided by the move generator.
 * <p>
 * The constants are declared in FEN order ({@code KQkq}), so iterating {@link #values()} gives the order FEN
 * writes them in.
 */
public enum CastlingRight {
    WHITE_KINGSIDE('K'),
    WHITE_QUEENSIDE('Q'),
    BLACK_KINGSIDE('k'),
    BLACK_QUEENSIDE('q');

    private static final CastlingRight[] VALUES = values();

    private final char letter;

    CastlingRight(char letter) {
        this.letter = letter;
    }

    /**
     * Returns the letter of this right in FEN, e.g. {@code 'K'} for white kingside.
     */
    public char letter() {
        return letter;
    }

    /**
     * Returns the right for its FEN letter.
     *
     * @throws IllegalArgumentException for any other character
     */
    public static CastlingRight fromLetter(char letter) {
        return Arrays.stream(VALUES)
                .filter(right -> right.letter == letter)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown castling right letter '" + letter + "', expected one of KQkq"));
    }
}
