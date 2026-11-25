package org.srt.chess.engine;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The placement of pieces on the 64 squares, and nothing else: side to move, castling rights and so on belong to
 * the position.
 * <p>
 * A board is immutable. {@link #with} and {@link #without} return a new board and leave this one unchanged.
 */
public final class Board {

    private static final Board EMPTY = new Board(new Piece[64]);

    /**
     * One entry per square, {@code null} for an empty square. Index = rank * 8 + file, so a1 is 0, h1 is 7 and
     * h8 is 63. Never exposed and never changed after construction.
     */
    private final Piece[] squares;

    private Board(Piece[] squares) {
        this.squares = squares;
    }

    /**
     * Returns a board without any pieces.
     */
    public static Board empty() {
        return EMPTY;
    }

    /**
     * Returns the piece on the square, or an empty Optional if the square is empty.
     */
    public Optional<Piece> pieceAt(Square square) {
        return Optional.ofNullable(squares[index(square)]);
    }

    /**
     * Returns whether no piece stands on the square.
     */
    boolean isEmpty(Square square) {
        return squares[index(square)] == null;
    }

    /**
     * Returns whether a piece of the given color stands on the square.
     */
    boolean isOccupiedBy(Square square, Color color) {
        Piece piece = squares[index(square)];
        return piece != null && piece.color() == color;
    }

    /**
     * Returns whether exactly this piece, in color and type, stands on the square.
     */
    boolean isOccupiedBy(Square square, Piece piece) {
        return piece.equals(squares[index(square)]);
    }

    /**
     * Returns whether a piece of {@code byColor} attacks the square, i.e. could capture a piece standing there. Own
     * pieces count too, so a defended piece is attacked by its own side. Each piece type's movement looks outward
     * from the square for an attacker of its own type.
     */
    boolean isAttacked(Square square, Color byColor) {
        return Arrays.stream(PieceType.values())
                .anyMatch(type -> type.movement().attacks(this, square, new Piece(byColor, type)));
    }

    /**
     * Returns a new board with the piece on the square, replacing whatever stood there.
     */
    public Board with(Square square, Piece piece) {
        Objects.requireNonNull(piece, "piece must not be null; use without(square) to empty a square");
        Piece[] copy = squares.clone();
        copy[index(square)] = piece;
        return new Board(copy);
    }

    /**
     * Returns a new board with the square empty.
     */
    public Board without(Square square) {
        Piece[] copy = squares.clone();
        copy[index(square)] = null;
        return new Board(copy);
    }

    private static int index(Square square) {
        Objects.requireNonNull(square, "square must not be null");
        return index(square.file().ordinal(), square.rank());
    }

    private static int index(int file, int rank) {
        return rank * 8 + file;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Board board && Arrays.equals(squares, board.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(squares);
    }

    /**
     * Returns a diagram for debugging, rank 8 at the top. Uppercase is white, lowercase is black, {@code .} is empty:
     * <pre>
     * . . . . k . . .
     * . . . . . . . .
     * ...
     * . . . . K . . .
     * </pre>
     */
    @Override
    public String toString() {
        StringBuilder diagram = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Piece piece = squares[index(file, rank)];
                diagram.append(piece == null ? '.' : piece.letter());
                diagram.append(file < 7 ? " " : "\n");
            }
        }
        return diagram.toString();
    }
}
