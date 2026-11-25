package org.srt.chess.engine;

import java.util.List;
import java.util.stream.Stream;

/**
 * A piece that applies each of its offsets once, to an empty square or a capture: the knight's jumps, and the king's
 * steps inside {@link KingMovement}.
 */
record Stepping(List<Offset> offsets) implements Movement {

    @Override
    public List<Move> moves(Position position, Square from) {
        Board board = position.board();
        return reachable(from)
                .filter(to -> !board.isOccupiedBy(to, position.sideToMove()))
                .map(to -> new Move(from, to))
                .toList();
    }

    /**
     * The offsets are symmetric (each one's opposite is in the list too), so an attacker stands one offset away from
     * the target.
     */
    @Override
    public boolean attacks(Board board, Square target, Piece attacker) {
        return reachable(target).anyMatch(from -> board.isOccupiedBy(from, attacker));
    }

    private Stream<Square> reachable(Square from) {
        return offsets.stream().flatMap(offset -> offset.applyTo(from).stream());
    }
}
