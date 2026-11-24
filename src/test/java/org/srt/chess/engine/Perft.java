package org.srt.chess.engine;

/**
 * Perft ("performance test") counts the positions reached by playing every sequence of legal moves to a fixed depth.
 * The correct counts for well-known positions are published, so comparing with them checks the move generator as a
 * whole: one wrong or missing move anywhere in the tree changes the count.
 */
public final class Perft {

    private Perft() {
    }

    /**
     * Returns the number of positions reached from this one after {@code depth} moves (plies). Depth 0 is the
     * position itself, so it counts 1.
     */
    public static long count(Position position, int depth) {
        if (depth == 0) {
            return 1;
        }
        return MoveGenerator.legalMoves(position).stream()
                .mapToLong(move -> count(position.play(move), depth - 1))
                .sum();
    }
}
