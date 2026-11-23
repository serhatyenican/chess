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
        return square.rank() * 8 + square.file();
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
                Piece piece = squares[rank * 8 + file];
                diagram.append(piece == null ? '.' : letter(piece));
                diagram.append(file < 7 ? " " : "\n");
            }
        }
        return diagram.toString();
    }

    private static char letter(Piece piece) {
        char letter = switch (piece.type()) {
            case KING -> 'k';
            case QUEEN -> 'q';
            case ROOK -> 'r';
            case BISHOP -> 'b';
            case KNIGHT -> 'n';
            case PAWN -> 'p';
        };
        return piece.color() == Color.WHITE ? Character.toUpperCase(letter) : letter;
    }
}
