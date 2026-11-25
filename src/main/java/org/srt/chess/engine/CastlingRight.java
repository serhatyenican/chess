package org.srt.chess.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * One of the four castling rights. A right only says castling is still allowed later (king and rook haven't moved);
 * whether castling is possible right now is decided when generating the legal moves.
 * <p>
 * Each right knows its squares: when castling, the king moves two squares from {@code kingFrom} to {@code kingTo},
 * and the rook from {@code rookFrom} to {@code rookTo}, the square the king crossed.
 * <p>
 * The constants are declared in FEN order ({@code KQkq}), so iterating {@link #values()} gives the order FEN
 * writes them in.
 */
public enum CastlingRight {
    WHITE_KINGSIDE('K', Color.WHITE, "e1", "g1", "h1", "f1"),
    WHITE_QUEENSIDE('Q', Color.WHITE, "e1", "c1", "a1", "d1"),
    BLACK_KINGSIDE('k', Color.BLACK, "e8", "g8", "h8", "f8"),
    BLACK_QUEENSIDE('q', Color.BLACK, "e8", "c8", "a8", "d8");

    private static final CastlingRight[] VALUES = values();

    private final char letter;
    private final Color color;
    private final Square kingFrom;
    private final Square kingTo;
    private final Square rookFrom;
    private final Square rookTo;
    private final List<Square> squaresBetween;
    private final List<Square> kingPath;

    CastlingRight(char letter, Color color, String kingFrom, String kingTo, String rookFrom, String rookTo) {
        this.letter = letter;
        this.color = color;
        this.kingFrom = Square.of(kingFrom);
        this.kingTo = Square.of(kingTo);
        this.rookFrom = Square.of(rookFrom);
        this.rookTo = Square.of(rookTo);
        List<Square> kingToRook = squaresAlongTheRank(this.kingFrom, this.rookFrom);
        this.squaresBetween = kingToRook.subList(1, kingToRook.size() - 1);
        this.kingPath = squaresAlongTheRank(this.kingFrom, this.kingTo);
    }

    /**
     * Returns the letter of this right in FEN, e.g. {@code 'K'} for white kingside.
     */
    public char letter() {
        return letter;
    }

    public Color color() {
        return color;
    }

    public Square kingFrom() {
        return kingFrom;
    }

    public Square kingTo() {
        return kingTo;
    }

    public Square rookFrom() {
        return rookFrom;
    }

    public Square rookTo() {
        return rookTo;
    }

    /**
     * Returns the squares between king and rook, which must be empty to castle, from the king outwards: f1, g1 for
     * white kingside and d1, c1, b1 for white queenside.
     */
    List<Square> squaresBetween() {
        return squaresBetween;
    }

    /**
     * Returns the squares the king stands on, crosses and lands on, which must not be attacked to castle: e1, f1, g1
     * for white kingside and e1, d1, c1 for white queenside. The queenside b-square is not on it.
     */
    List<Square> kingPath() {
        return kingPath;
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

    /**
     * Returns the right whose castling this king move is, e.g. white kingside for {@code e1g1}, or an empty Optional
     * for any other move. Only the squares are compared: the caller makes sure it is the king that moves.
     */
    static Optional<CastlingRight> fromKingMove(Move move) {
        return Arrays.stream(VALUES)
                .filter(right -> right.kingFrom.equals(move.from()))
                .filter(right -> right.kingTo.equals(move.to()))
                .findFirst();
    }

    /** Returns the squares from {@code from} to {@code to} on their rank, both included. */
    private static List<Square> squaresAlongTheRank(Square from, Square to) {
        int distance = to.file().ordinal() - from.file().ordinal();
        int step = Integer.signum(distance);
        return IntStream.rangeClosed(0, Math.abs(distance))
                .mapToObj(files -> from.offset(files * step, 0).orElseThrow())
                .toList();
    }
}
