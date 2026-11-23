package org.srt.chess.engine;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // A castling right is lost once a move starts or ends on its king's or rook's starting square: the king or the rook
    // has moved, or the rook has been captured.
    private static final Map<Square, Set<CastlingRight>> CASTLING_RIGHTS_LOST_ON = Map.of(
            Square.of("e1"), Set.of(CastlingRight.WHITE_KINGSIDE, CastlingRight.WHITE_QUEENSIDE),
            Square.of("h1"), Set.of(CastlingRight.WHITE_KINGSIDE),
            Square.of("a1"), Set.of(CastlingRight.WHITE_QUEENSIDE),
            Square.of("e8"), Set.of(CastlingRight.BLACK_KINGSIDE, CastlingRight.BLACK_QUEENSIDE),
            Square.of("h8"), Set.of(CastlingRight.BLACK_KINGSIDE),
            Square.of("a8"), Set.of(CastlingRight.BLACK_QUEENSIDE));

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
        int nextHalfmoveClock = piece.type() == PieceType.PAWN || capture ? 0 : halfmoveClock + 1;
        int nextFullmoveNumber = sideToMove == Color.BLACK ? fullmoveNumber + 1 : fullmoveNumber;
        return new Position(nextBoard, sideToMove.opposite(), castlingRightsAfter(move), enPassantSquare(piece, move),
                nextHalfmoveClock, nextFullmoveNumber);
    }

    private Set<CastlingRight> castlingRightsAfter(Move move) {
        Set<CastlingRight> lost = Stream.of(move.from(), move.to())
                .flatMap(square -> CASTLING_RIGHTS_LOST_ON.getOrDefault(square, Set.of()).stream())
                .collect(Collectors.toSet());
        return castlingRights.stream()
                .filter(right -> !lost.contains(right))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the square a pawn's double step passed over, or {@code null} if the move is not a double step.
     */
    private static Square enPassantSquare(Piece piece, Move move) {
        boolean doubleStep = piece.type() == PieceType.PAWN && Math.abs(move.to().rank() - move.from().rank()) == 2;
        return doubleStep ? new Square(move.from().file(), (move.from().rank() + move.to().rank()) / 2) : null;
    }
}
