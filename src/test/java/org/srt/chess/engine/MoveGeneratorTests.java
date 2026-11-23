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

    @Test
    void kingInTheCenterStepsToEightSquares() {
        assertThat(kingMoves("8/8/8/8/4K3/8/8/8 w - - 0 1", "e4"))
                .containsExactlyInAnyOrder("e4e5", "e4f5", "e4f4", "e4f3", "e4e3", "e4d3", "e4d4", "e4d5");
    }

    @Test
    void kingInTheCornerStepsToThreeSquares() {
        assertThat(kingMoves("8/8/8/8/8/8/8/K7 w - - 0 1", "a1"))
                .containsExactlyInAnyOrder("a1a2", "a1b2", "a1b1");
    }

    @Test
    void kingInTheStartingPositionIsSurroundedByItsOwnPieces() {
        assertThat(kingMoves(Fen.STARTING_POSITION, "e1")).isEmpty();
    }

    @Test
    void kingIsBlockedByOwnPiecesAndCapturesEnemyPieces() {
        // white pawn on e2 blocks, black pawn on d2 can be captured
        assertThat(kingMoves("8/8/8/8/8/8/3pP3/4K3 w - - 0 1", "e1"))
                .containsExactlyInAnyOrder("e1d1", "e1d2", "e1f2", "e1f1");
    }

    @Test
    void blackKingIsBlockedByBlackPiecesAndCapturesWhitePieces() {
        // black pawn on e7 blocks, white pawn on d7 can be captured
        assertThat(kingMoves("4k3/3Pp3/8/8/8/8/8/8 b - - 0 1", "e8"))
                .containsExactlyInAnyOrder("e8d8", "e8d7", "e8f7", "e8f8");
    }

    @Test
    void generateFindsTheKnightMovesOfWhiteInTheStartingPosition() {
        assertThat(generate(Fen.STARTING_POSITION))
                .containsExactlyInAnyOrder("b1a3", "b1c3", "g1f3", "g1h3");
    }

    @Test
    void generateOnlyMovesThePiecesOfTheSideToMove() {
        // after 1. e4
        assertThat(generate("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"))
                .containsExactlyInAnyOrder("b8a6", "b8c6", "g8f6", "g8h6");
    }

    @Test
    void generateIncludesKnightAndKingMoves() {
        assertThat(generate("4k3/8/8/8/8/8/8/N3K3 w - - 0 1"))
                .containsExactlyInAnyOrder("a1b3", "a1c2", "e1d1", "e1d2", "e1e2", "e1f2", "e1f1");
    }

    /** Returns all moves of the side to move in UCI form, e.g. {@code "b1c3"}. */
    private static List<String> generate(String fen) {
        return MoveGenerator.generate(Fen.parse(fen)).stream()
                .map(Move::toString)
                .toList();
    }

    /** Returns the knight's moves from {@code from} in UCI form, e.g. {@code "a1b3"}. */
    private static List<String> knightMoves(String fen, String from) {
        return MoveGenerator.knightMoves(Fen.parse(fen), Square.of(from)).stream()
                .map(Move::toString)
                .toList();
    }

    /** Returns the king's moves from {@code from} in UCI form, e.g. {@code "e1e2"}. */
    private static List<String> kingMoves(String fen, String from) {
        return MoveGenerator.kingMoves(Fen.parse(fen), Square.of(from)).stream()
                .map(Move::toString)
                .toList();
    }
}
