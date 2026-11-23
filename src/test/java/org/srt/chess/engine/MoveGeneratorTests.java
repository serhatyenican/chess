package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoveGeneratorTests {

    @Test
    void knightInTheCenterJumpsToEightSquares() {
        assertThat(knightMoves("8/8/8/8/3N4/8/8/8 w - - 0 1", "d4"))
                .containsExactlyInAnyOrder("d4e6", "d4f5", "d4f3", "d4e2", "d4c2", "d4b3", "d4b5", "d4c6");
    }

    @Test
    void knightInTheCornerJumpsToTwoSquares() {
        assertThat(knightMoves("8/8/8/8/8/8/8/N7 w - - 0 1", "a1"))
                .containsExactlyInAnyOrder("a1b3", "a1c2");
    }

    @Test
    void knightOnTheEdgeJumpsToFourSquares() {
        assertThat(knightMoves("8/8/8/8/N7/8/8/8 w - - 0 1", "a4"))
                .containsExactlyInAnyOrder("a4b6", "a4c5", "a4c3", "a4b2");
    }

    @Test
    void knightInTheStartingPositionJumpsOverItsPawns() {
        assertThat(knightMoves(Fen.STARTING_POSITION, "b1"))
                .containsExactlyInAnyOrder("b1a3", "b1c3");
    }

    @Test
    void ownPieceBlocksTheTargetSquare() {
        assertThat(knightMoves("8/8/8/8/8/1P6/8/N7 w - - 0 1", "a1"))
                .containsExactly("a1c2");
    }

    @Test
    void enemyPieceOnTheTargetSquareCanBeCaptured() {
        assertThat(knightMoves("8/8/8/8/8/1p6/8/N7 w - - 0 1", "a1"))
                .containsExactlyInAnyOrder("a1b3", "a1c2");
    }

    @Test
    void blackKnightIsBlockedByBlackPiecesAndCapturesWhitePieces() {
        assertThat(knightMoves("n7/2p5/1P6/8/8/8/8/8 b - - 0 1", "a8"))
                .containsExactly("a8b6");
    }

    /** Returns the knight's moves from {@code from} in UCI form, e.g. {@code "a1b3"}. */
    private static List<String> knightMoves(String fen, String from) {
        return MoveGenerator.knightMoves(Fen.parse(fen), Square.of(from)).stream()
                .map(Move::toString)
                .toList();
    }
}
