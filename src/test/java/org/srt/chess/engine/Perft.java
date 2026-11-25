package org.srt.chess.engine;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Perft ("performance test") counts the positions reached by playing every sequence of legal moves to a fixed depth.
 * The correct counts for well-known positions are published, so comparing with them checks the move generator as a
 * whole: one wrong or missing move anywhere in the tree changes the count.
 */
final class Perft {

    private Perft() {
    }

    /**
     * Returns the number of positions reached from this one after {@code depth} moves (plies). Depth 0 is the
     * position itself, so it counts 1.
     */
    static long count(Position position, int depth) {
        if (depth == 0) {
            return 1;
        }
        return position.legalMoves().stream()
                .mapToLong(move -> count(position.play(move), depth - 1))
                .sum();
    }

    /**
     * Returns the perft count under each legal first move, keyed by its UCI form and sorted, e.g.
     * {@code "e2e4" -> 13160}. When a count is wrong, compare this with a reference engine's divide (Stockfish's
     * {@code go perft}) and follow the move whose count differs down to the bug.
     *
     * @throws IllegalArgumentException if {@code depth} is below 1, since depth 0 has no first moves
     */
    static Map<String, Long> divide(Position position, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("divide needs a depth of at least 1, got " + depth);
        }
        // two legal moves never share a UCI string, so the merge function only selects the TreeMap overload
        return position.legalMoves().stream()
                .collect(Collectors.toMap(
                        Move::toString,
                        move -> count(position.play(move), depth - 1),
                        (first, _) -> first,
                        TreeMap::new));
    }
}
