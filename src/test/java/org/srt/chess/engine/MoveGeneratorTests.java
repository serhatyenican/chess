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
    void rookInTheCenterSlidesToFourteenSquares() {
        assertThat(rookMoves("8/8/8/8/3R4/8/8/8 w - - 0 1", "d4"))
                .containsExactlyInAnyOrder(
                        "d4d5", "d4d6", "d4d7", "d4d8", "d4d3", "d4d2", "d4d1",
                        "d4a4", "d4b4", "d4c4", "d4e4", "d4f4", "d4g4", "d4h4");
    }

    @Test
    void rookInTheStartingPositionIsBlockedByItsOwnPieces() {
        assertThat(rookMoves(Fen.STARTING_POSITION, "a1")).isEmpty();
    }

    @Test
    void rookStopsBeforeOwnPiecesAndCapturesOnlyTheFirstEnemyPiece() {
        // white pawn on d6 blocks upwards; black pawn on f4 can be captured, the black knight behind it on g4 not
        assertThat(rookMoves("8/8/3P4/8/3R1pn1/8/8/8 w - - 0 1", "d4"))
                .containsExactlyInAnyOrder(
                        "d4d5", "d4e4", "d4f4", "d4d3", "d4d2", "d4d1", "d4c4", "d4b4", "d4a4");
    }

    @Test
    void blackRookIsBlockedByBlackPiecesAndCapturesWhitePieces() {
        // black pawn on h7 blocks, white knight on c8 can be captured
        assertThat(rookMoves("2N4r/7p/8/8/8/8/8/8 b - - 0 1", "h8"))
                .containsExactlyInAnyOrder("h8g8", "h8f8", "h8e8", "h8d8", "h8c8");
    }

    @Test
    void bishopInTheCenterSlidesToThirteenSquares() {
        assertThat(bishopMoves("8/8/8/8/3B4/8/8/8 w - - 0 1", "d4"))
                .containsExactlyInAnyOrder(
                        "d4e5", "d4f6", "d4g7", "d4h8", "d4e3", "d4f2", "d4g1",
                        "d4c3", "d4b2", "d4a1", "d4c5", "d4b6", "d4a7");
    }

    @Test
    void bishopInTheStartingPositionIsBlockedByItsOwnPieces() {
        assertThat(bishopMoves(Fen.STARTING_POSITION, "c1")).isEmpty();
    }

    @Test
    void bishopStopsBeforeOwnPiecesAndCapturesOnlyTheFirstEnemyPiece() {
        // white pawn on f6 blocks; black pawn on b2 can be captured, the black knight behind it on a1 not
        assertThat(bishopMoves("8/8/5P2/8/3B4/8/1p6/n7 w - - 0 1", "d4"))
                .containsExactlyInAnyOrder(
                        "d4e5", "d4e3", "d4f2", "d4g1", "d4c3", "d4b2", "d4c5", "d4b6", "d4a7");
    }

    @Test
    void blackBishopIsBlockedByBlackPiecesAndCapturesWhitePieces() {
        // black pawn on d7 blocks, white pawn on b7 can be captured
        assertThat(bishopMoves("2b5/1P1p4/8/8/8/8/8/8 b - - 0 1", "c8"))
                .containsExactly("c8b7");
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

    @Test
    void generateIncludesRookMoves() {
        // the rook on a1 slides up the a-file and along rank 1 until the king on e1
        assertThat(generate("4k3/8/8/8/8/8/8/R3K3 w - - 0 1"))
                .containsExactlyInAnyOrder(
                        "a1a2", "a1a3", "a1a4", "a1a5", "a1a6", "a1a7", "a1a8", "a1b1", "a1c1", "a1d1",
                        "e1d1", "e1d2", "e1e2", "e1f2", "e1f1");
    }

    @Test
    void generateIncludesBishopMoves() {
        // the bishop on c1 slides along both diagonals; it shares d2 with the king
        assertThat(generate("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1"))
                .containsExactlyInAnyOrder(
                        "c1d2", "c1e3", "c1f4", "c1g5", "c1h6", "c1b2", "c1a3",
                        "e1d1", "e1d2", "e1e2", "e1f2", "e1f1");
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

    /** Returns the rook's moves from {@code from} in UCI form, e.g. {@code "a1a8"}. */
    private static List<String> rookMoves(String fen, String from) {
        return MoveGenerator.rookMoves(Fen.parse(fen), Square.of(from)).stream()
                .map(Move::toString)
                .toList();
    }

    /** Returns the bishop's moves from {@code from} in UCI form, e.g. {@code "c1h6"}. */
    private static List<String> bishopMoves(String fen, String from) {
        return MoveGenerator.bishopMoves(Fen.parse(fen), Square.of(from)).stream()
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
