package org.srt.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    // Parsed once. Safe to initialize here: Fen has no static state that needs Position, so there is no cycle.
    private static final Position STARTING = Fen.parse(Fen.STARTING_POSITION);

    private static final List<Offset> KNIGHT_JUMPS = List.of(
            new Offset(1, 2), new Offset(2, 1), new Offset(2, -1), new Offset(1, -2),
            new Offset(-1, -2), new Offset(-2, -1), new Offset(-2, 1), new Offset(-1, 2));

    private static final List<Offset> KING_STEPS = List.of(
            new Offset(0, 1), new Offset(1, 1), new Offset(1, 0), new Offset(1, -1),
            new Offset(0, -1), new Offset(-1, -1), new Offset(-1, 0), new Offset(-1, 1));

    private static final List<Offset> ROOK_DIRECTIONS = List.of(
            new Offset(0, 1), new Offset(1, 0), new Offset(0, -1), new Offset(-1, 0));

    private static final List<Offset> BISHOP_DIRECTIONS = List.of(
            new Offset(1, 1), new Offset(1, -1), new Offset(-1, -1), new Offset(-1, 1));

    // Declared after the rook and bishop directions, which must be initialized first.
    private static final List<Offset> QUEEN_DIRECTIONS =
            Stream.concat(ROOK_DIRECTIONS.stream(), BISHOP_DIRECTIONS.stream()).toList();

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
        Piece placed = move.isPromotion() ? new Piece(piece.color(), move.promotion()) : piece;
        Board nextBoard = board.without(move.from()).with(move.to(), placed);
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

    /**
     * Returns the pseudo-legal moves of the side to move: moves that follow how each piece moves, without checking
     * whether they leave the mover's own king in check.
     */
    List<Move> pseudoLegalMoves() {
        return Square.all().stream()
                .flatMap(from -> pieceAt(from).stream()
                        .filter(piece -> piece.color() == sideToMove)
                        .flatMap(piece -> movesOf(from, piece.type()).stream()))
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
                .filter(square -> pieceAt(square).filter(king::equals).isPresent())
                .findFirst()
                .map(square -> isAttacked(square, kingColor.opposite()))
                .orElse(false);
    }

    private List<Move> movesOf(Square from, PieceType type) {
        return switch (type) {
            case KING -> kingMoves(from);
            case QUEEN -> queenMoves(from);
            case ROOK -> rookMoves(from);
            case BISHOP -> bishopMoves(from);
            case KNIGHT -> knightMoves(from);
            case PAWN -> pawnMoves(from);
        };
    }

    /**
     * Returns the moves of the king on {@code from}, which belongs to the side to move: one step in each of the eight
     * directions, each to an empty square or a capture, plus castling where it is possible.
     */
    List<Move> kingMoves(Square from) {
        return Stream.concat(stepMoves(from, KING_STEPS).stream(), castlingMoves(from))
                .toList();
    }

    /**
     * Returns the castling moves of the king on {@code from}: for each castling right of the side to move, if its
     * rook is in place, the squares between king and rook are empty and none of the king's squares (start, crossed,
     * target) is attacked.
     */
    private Stream<Move> castlingMoves(Square from) {
        return castlingRights.stream()
                .filter(right -> right.color() == sideToMove)
                .filter(right -> right.kingFrom().equals(from))
                .filter(right -> pieceAt(right.rookFrom()).equals(Optional.of(new Piece(sideToMove, PieceType.ROOK))))
                .filter(right -> right.squaresBetween().stream().allMatch(square -> pieceAt(square).isEmpty()))
                .filter(right -> right.kingPath().stream()
                        .noneMatch(square -> isAttacked(square, sideToMove.opposite())))
                .map(right -> new Move(right.kingFrom(), right.kingTo()));
    }

    /**
     * Returns the moves of the queen on {@code from}, which belongs to the side to move: like a rook and a bishop
     * together, along its rank, file and diagonals up to the edge of the board or the first piece, which it captures
     * if it is an enemy piece.
     */
    List<Move> queenMoves(Square from) {
        return slidingMoves(from, QUEEN_DIRECTIONS);
    }

    /**
     * Returns the moves of the rook on {@code from}, which belongs to the side to move: along its rank and file up to
     * the edge of the board or the first piece, which it captures if it is an enemy piece.
     */
    List<Move> rookMoves(Square from) {
        return slidingMoves(from, ROOK_DIRECTIONS);
    }

    /**
     * Returns the moves of the bishop on {@code from}, which belongs to the side to move: along its diagonals up to
     * the edge of the board or the first piece, which it captures if it is an enemy piece.
     */
    List<Move> bishopMoves(Square from) {
        return slidingMoves(from, BISHOP_DIRECTIONS);
    }

    /**
     * Returns the moves from {@code from} that repeat each direction up to the edge of the board or the first piece,
     * which is included as a capture if it belongs to the opponent.
     */
    private List<Move> slidingMoves(Square from, List<Offset> directions) {
        return directions.stream()
                .flatMap(direction -> slide(from, direction).stream())
                .toList();
    }

    /**
     * Returns the moves from {@code from} in one direction: every empty square up to the first piece, plus that
     * piece if it is an enemy piece.
     */
    private List<Move> slide(Square from, Offset direction) {
        List<Move> moves = new ArrayList<>();
        Optional<Square> next = from.offset(direction.fileDelta(), direction.rankDelta());
        while (next.isPresent()) {
            Square to = next.get();
            if (!isEmpty(to)) {
                if (isEnemy(to)) {
                    moves.add(new Move(from, to));    // capture, then stop
                }
                break;                                // own piece: stop before it
            }
            moves.add(new Move(from, to));            // empty square: keep going
            next = to.offset(direction.fileDelta(), direction.rankDelta());
        }
        return moves;
    }

    /**
     * Returns the moves of the knight on {@code from}, which belongs to the side to move: up to eight jumps, each to
     * an empty square or a capture.
     */
    List<Move> knightMoves(Square from) {
        return stepMoves(from, KNIGHT_JUMPS);
    }

    /**
     * Returns the moves of the pawn on {@code from}, which belongs to the side to move: one step forward onto an empty
     * square, two steps from its starting rank if both squares are empty, and one step diagonally forward onto an
     * enemy piece or, en passant, onto the en passant square. A move onto the last rank becomes four moves, one per
     * piece the pawn can promote to.
     */
    List<Move> pawnMoves(Square from) {
        int forward = forward(sideToMove);
        int maxSteps = from.rank() == startRank(sideToMove) ? 2 : 1;
        Stream<Square> pushes = IntStream.rangeClosed(1, maxSteps)
                .mapToObj(steps -> from.offset(0, forward * steps))
                .flatMap(Optional::stream)
                .takeWhile(this::isEmpty);
        Stream<Square> captures = Stream.of(-1, 1)
                .flatMap(fileDelta -> from.offset(fileDelta, forward).stream())
                .filter(to -> pawnCanCaptureOn(from, to));
        return Stream.concat(pushes, captures)
                .flatMap(to -> to.rank() == lastRank(sideToMove)
                        ? PieceType.promotionTypes().stream().map(type -> new Move(from, to, type))
                        : Stream.of(new Move(from, to)))
                .toList();
    }

    /**
     * Returns whether the pawn on {@code from} can capture diagonally onto {@code to}: an enemy piece, or en passant.
     */
    private boolean pawnCanCaptureOn(Square from, Square to) {
        return isEnemy(to) || isEnPassantCapture(from, to);
    }

    /**
     * Returns whether a pawn on {@code from} capturing diagonally onto {@code to} is an en passant capture: {@code to}
     * is the en passant square and the enemy pawn that just double-stepped stands beside it.
     */
    private boolean isEnPassantCapture(Square from, Square to) {
        Square capturedPawn = new Square(to.file(), from.rank());
        return to.equals(enPassant)
                && pieceAt(capturedPawn).equals(Optional.of(new Piece(oppositeSide(), PieceType.PAWN)));
    }

    /**
     * Returns whether a piece of {@code byColor} attacks {@code square}, i.e. could capture a piece standing there.
     * Looks outward from the square: knight jumps, king steps and sliding lines are symmetric, so an attacker stands
     * one of those offsets or lines away. Pawns attack in one direction only, so their offsets depend on the color.
     */
    boolean isAttacked(Square square, Color byColor) {
        int backward = -forward(byColor);
        List<Offset> pawnAttackers = List.of(new Offset(-1, backward), new Offset(1, backward));
        return isAttackedByStep(square, KNIGHT_JUMPS, new Piece(byColor, PieceType.KNIGHT))
                || isAttackedByStep(square, KING_STEPS, new Piece(byColor, PieceType.KING))
                || isAttackedByStep(square, pawnAttackers, new Piece(byColor, PieceType.PAWN))
                || isAttackedBySlide(square, ROOK_DIRECTIONS,
                        Set.of(new Piece(byColor, PieceType.ROOK), new Piece(byColor, PieceType.QUEEN)))
                || isAttackedBySlide(square, BISHOP_DIRECTIONS,
                        Set.of(new Piece(byColor, PieceType.BISHOP), new Piece(byColor, PieceType.QUEEN)));
    }

    private boolean isAttackedByStep(Square square, List<Offset> offsets, Piece attacker) {
        return offsets.stream()
                .flatMap(offset -> square.offset(offset.fileDelta(), offset.rankDelta()).stream())
                .flatMap(from -> pieceAt(from).stream())
                .anyMatch(attacker::equals);
    }

    private boolean isAttackedBySlide(Square square, List<Offset> directions, Set<Piece> attackers) {
        return directions.stream()
                .flatMap(direction -> firstPiece(square, direction).stream())
                .anyMatch(attackers::contains);
    }

    /**
     * Returns the first piece seen from {@code square} in one direction, or an empty Optional if the line is empty up
     * to the edge of the board.
     */
    private Optional<Piece> firstPiece(Square square, Offset direction) {
        Optional<Square> next = square.offset(direction.fileDelta(), direction.rankDelta());
        while (next.isPresent()) {
            Optional<Piece> piece = pieceAt(next.get());
            if (piece.isPresent()) {
                return piece;
            }
            next = next.get().offset(direction.fileDelta(), direction.rankDelta());
        }
        return Optional.empty();
    }

    /** Returns the rank direction pawns of the given color move in: up for white, down for black. */
    private static int forward(Color color) {
        return color == Color.WHITE ? 1 : -1;
    }

    /** Returns the rank pawns of the given color start on: rank 2 for white, rank 7 for black. */
    private static int startRank(Color color) {
        return color == Color.WHITE ? 1 : 6;
    }

    /** Returns the rank pawns of the given color promote on: rank 8 for white, rank 1 for black. */
    private static int lastRank(Color color) {
        return color == Color.WHITE ? 7 : 0;
    }

    /**
     * Returns the moves from {@code from} that apply each offset once, skipping squares off the board or taken by
     * a piece of the side to move.
     */
    private List<Move> stepMoves(Square from, List<Offset> offsets) {
        return offsets.stream()
                .flatMap(offset -> from.offset(offset.fileDelta(), offset.rankDelta()).stream())
                .filter(to -> isEmpty(to) || isEnemy(to))
                .map(to -> new Move(from, to))
                .toList();
    }

    private boolean isEmpty(Square square) {
        return pieceAt(square).isEmpty();
    }

    private boolean isEnemy(Square square) {
        return pieceAt(square)
                .map(Piece::color)
                .filter(oppositeSide()::equals)
                .isPresent();
    }

    private Color oppositeSide() {
        return sideToMove.opposite();
    }

    private Optional<Piece> pieceAt(Square square) {
        return board.pieceAt(square);
    }

    private record Offset(int fileDelta, int rankDelta) {
    }
}
