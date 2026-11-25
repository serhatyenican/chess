package org.srt.chess.engine;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The pawn: one step forward onto an empty square, two steps from its starting rank if both squares are empty, and
 * one step diagonally forward onto an enemy piece or, en passant, onto the en passant square. A move onto the last
 * rank becomes four moves, one per piece the pawn can promote to. Unlike the other pieces it moves in one direction
 * only, which depends on its color, and it captures differently from how it moves.
 */
final class PawnMovement implements Movement {

    @Override
    public List<Move> moves(Position position, Square from) {
        Color mover = position.sideToMove();
        Board board = position.board();
        int forward = forward(mover);
        int maxSteps = from.rank() == startRank(mover) ? 2 : 1;
        Stream<Square> pushes = IntStream.rangeClosed(1, maxSteps)
                .mapToObj(steps -> from.offset(0, forward * steps))
                .flatMap(Optional::stream)
                .takeWhile(board::isEmpty);
        Stream<Square> captures = Stream.of(-1, 1)
                .flatMap(fileDelta -> from.offset(fileDelta, forward).stream())
                .filter(to -> canCaptureOn(position, from, to));
        return Stream.concat(pushes, captures)
                .flatMap(to -> to.rank() == lastRank(mover)
                        ? PieceType.promotionTypes().stream().map(type -> new Move(from, to, type))
                        : Stream.of(new Move(from, to)))
                .toList();
    }

    /**
     * A pawn attacks diagonally forward, so an attacker stands diagonally behind the target, seen from the
     * attacker's side.
     */
    @Override
    public boolean attacks(Board board, Square target, Piece attacker) {
        int backward = -forward(attacker.color());
        return Stream.of(-1, 1)
                .flatMap(fileDelta -> target.offset(fileDelta, backward).stream())
                .anyMatch(from -> board.isOccupiedBy(from, attacker));
    }

    /**
     * Returns whether the pawn on {@code from} can capture diagonally onto {@code to}: an enemy piece, or en passant.
     */
    private static boolean canCaptureOn(Position position, Square from, Square to) {
        return position.board().isOccupiedBy(to, position.sideToMove().opposite())
                || isEnPassantCapture(position, from, to);
    }

    /**
     * Returns whether a pawn on {@code from} capturing diagonally onto {@code to} is an en passant capture: {@code to}
     * is the en passant square and the enemy pawn that just double-stepped stands beside it.
     */
    private static boolean isEnPassantCapture(Position position, Square from, Square to) {
        Square capturedPawn = new Square(to.file(), from.rank());
        Piece enemyPawn = new Piece(position.sideToMove().opposite(), PieceType.PAWN);
        return to.equals(position.enPassant()) && position.board().isOccupiedBy(capturedPawn, enemyPawn);
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
}
