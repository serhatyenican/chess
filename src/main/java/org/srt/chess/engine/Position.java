package org.srt.chess.engine;

import java.util.List;
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
     * expected to come from {@link #legalMoves()}.
     *
     * @throws IllegalArgumentException if there is no piece on the move's start square
     */
    public Position play(Move move) {
        Objects.requireNonNull(move, "move must not be null");
        Piece piece = board.pieceAt(move.from())
                .orElseThrow(() -> new IllegalArgumentException("no piece on " + move.from() + " to play " + move));
        boolean capture = board.pieceAt(move.to()).isPresent();
        Board nextBoard = piece.type().movement().play(this, piece, move);
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
     * Returns the square a pawn's double step passed over, or {@code null} if the move is not a double step.
     */
    private static Square enPassantSquare(Piece piece, Move move) {
        boolean doubleStep = piece.type() == PieceType.PAWN && Math.abs(move.to().rank() - move.from().rank()) == 2;
        return doubleStep ? new Square(move.from().file(), (move.from().rank() + move.to().rank()) / 2) : null;
    }

    /**
     * Returns the pseudo-legal moves of the side to move: moves that follow how each piece moves, without checking
     * whether they leave the mover's own king in check.
     */
    List<Move> pseudoLegalMoves() {
        return Square.all().stream()
                .flatMap(from -> board.pieceAt(from).stream()
                        .filter(piece -> piece.color() == sideToMove)
                        .flatMap(piece -> piece.type().movement().moves(this, from).stream()))
                .toList();
    }

    /**
     * Returns the legal moves of the side to move: the pseudo-legal moves that don't leave its own king attacked.
     * Each move is played and the king checked afterwards, which also covers pins and moving the king into check.
     */
    public List<Move> legalMoves() {
        return pseudoLegalMoves().stream()
                .filter(move -> !play(move).isKingAttacked(sideToMove))
                .toList();
    }

    /**
     * Returns whether the king of the side to move is attacked. A position without that king is never in check.
     */
    public boolean isInCheck() {
        return isKingAttacked(sideToMove);
    }

    // Takes the king's color explicitly: after a move is played, the side to move is already the opponent.
    private boolean isKingAttacked(Color kingColor) {
        Piece king = new Piece(kingColor, PieceType.KING);
        return Square.all().stream()
                .filter(square -> board.isOccupiedBy(square, king))
                .findFirst()
                .map(square -> board.isAttacked(square, kingColor.opposite()))
                .orElse(false);
    }
}
