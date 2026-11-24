package org.srt.chess.engine;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Everything needed to continue a game: the pieces on the board plus the state FEN records alongside them.
 *
 * @param board          the placement of the pieces
 * @param sideToMove     the color that moves next
 * @param castlingRights the castling rights still available; an immutable copy is stored
 * @param enPassant      the square behind a pawn that just made a double step, or {@code null}
 * @param halfmoveClock  half-moves since the last capture or pawn move (for the fifty-move rule)
 * @param fullmoveNumber starts at 1 and increases after each black move
 */
public record Position(
        Board board,
        Color sideToMove,
        Set<CastlingRight> castlingRights,
        Square enPassant,
        int halfmoveClock,
        int fullmoveNumber) {

    // Parsed once. Safe to initialize here: Fen has no static state that needs Position, so there is no cycle.
    private static final Position STARTING = Fen.parse(Fen.STARTING_POSITION);

    public Position {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(sideToMove, "sideToMove must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        castlingRights = Set.copyOf(castlingRights);
        if (enPassant != null) {
            // White to move: black just double-stepped from rank 7 to 5, so the square behind it is on rank 6.
            int expectedRank = sideToMove == Color.WHITE ? 5 : 2;
            if (enPassant.rank() != expectedRank) {
                throw new IllegalArgumentException("en passant square must be on rank " + (expectedRank + 1)
                        + " when " + sideToMove.name().toLowerCase(Locale.ROOT) + " is to move, was " + enPassant);
            }
        }
        if (halfmoveClock < 0) {
            throw new IllegalArgumentException("halfmoveClock must not be negative, was " + halfmoveClock);
        }
        if (fullmoveNumber < 1) {
            throw new IllegalArgumentException("fullmoveNumber must be at least 1, was " + fullmoveNumber);
        }
    }

    /**
     * Returns the position at the start of a game. Always the same instance, which is safe because a position is
     * immutable.
     */
    public static Position starting() {
        return STARTING;
    }

    /**
     * Returns the position after playing the move, leaving this position unchanged. The move is not validated: it is
     * expected to come from the move generator.
     *
     * @throws IllegalArgumentException if there is no piece on the move's start square
     */
    public Position play(Move move) {
        Objects.requireNonNull(move, "move must not be null");
        Piece piece = board.pieceAt(move.from())
                .orElseThrow(() -> new IllegalArgumentException("no piece on " + move.from() + " to play " + move));
        boolean capture = board.pieceAt(move.to()).isPresent();
        Board nextBoard = board.without(move.from()).with(move.to(), piece);
        nextBoard = moveRookIfCastling(nextBoard, piece, move);
        nextBoard = removePawnIfEnPassant(nextBoard, piece, move, enPassant);
        int nextHalfmoveClock = piece.type() == PieceType.PAWN || capture ? 0 : halfmoveClock + 1;
        int nextFullmoveNumber = sideToMove == Color.BLACK ? fullmoveNumber + 1 : fullmoveNumber;
        return new Position(nextBoard, sideToMove.opposite(), castlingRightsAfter(move), enPassantSquare(piece, move),
                nextHalfmoveClock, nextFullmoveNumber);
    }

    /**
     * Returns the castling rights left after the move. A right is lost once a move starts or ends on its king's or
     * rook's starting square: the king or the rook has moved, or the rook has been captured.
     */
    private Set<CastlingRight> castlingRightsAfter(Move move) {
        Set<Square> touched = Set.of(move.from(), move.to());
        return castlingRights.stream()
                .filter(right -> !touched.contains(right.kingFrom()) && !touched.contains(right.rookFrom()))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the board with the rook moved too if the move is a castle: the king moving two squares to castle.
     */
    private static Board moveRookIfCastling(Board board, Piece piece, Move move) {
        if (piece.type() != PieceType.KING) {
            return board;
        }
        return CastlingRight.fromKingMove(move)
                .map(right -> board.without(right.rookFrom())
                        .with(right.rookTo(), new Piece(piece.color(), PieceType.ROOK)))
                .orElse(board);
    }

    /**
     * Returns the board without the captured pawn if the move is an en passant capture: a pawn moving onto the en
     * passant square. The captured pawn stands beside the target square, on the capturing pawn's rank.
     */
    private static Board removePawnIfEnPassant(Board board, Piece piece, Move move, Square enPassant) {
        if (piece.type() != PieceType.PAWN || !move.to().equals(enPassant)) {
            return board;
        }
        return board.without(new Square(move.to().file(), move.from().rank()));
    }

    /**
     * Returns the square a pawn's double step passed over, or {@code null} if the move is not a double step.
     */
    private static Square enPassantSquare(Piece piece, Move move) {
        boolean doubleStep = piece.type() == PieceType.PAWN && Math.abs(move.to().rank() - move.from().rank()) == 2;
        return doubleStep ? new Square(move.from().file(), (move.from().rank() + move.to().rank()) / 2) : null;
    }
}
