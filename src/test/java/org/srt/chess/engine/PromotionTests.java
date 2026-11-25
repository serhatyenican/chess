package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionTests {

    @Test
    void pawnStepOntoTheLastRankBecomesFourPromotions() {
        assertThat(pawnMoves("8/4P3/8/8/8/8/8/k6K w - - 0 1", "e7"))
                .containsExactlyInAnyOrder("e7e8q", "e7e8r", "e7e8b", "e7e8n");
    }

    @Test
    void captureOntoTheLastRankBecomesFourPromotions() {
        // the knight on e8 blocks the step
        assertThat(pawnMoves("3rn3/4P3/8/8/8/8/8/k6K w - - 0 1", "e7"))
                .containsExactlyInAnyOrder("e7d8q", "e7d8r", "e7d8b", "e7d8n");
    }

    @Test
    void blackPawnsPromoteOnRankOne() {
        assertThat(pawnMoves("k6K/8/8/8/8/8/4p3/8 b - - 0 1", "e2"))
                .containsExactlyInAnyOrder("e2e1q", "e2e1r", "e2e1b", "e2e1n");
    }

    @Test
    void pawnBeforeTheSeventhRankDoesNotPromote() {
        assertThat(pawnMoves("8/8/4P3/8/8/8/8/k6K w - - 0 1", "e6")).containsExactly("e6e7");
    }

    @Test
    void legalMovesIncludeAllFourPromotions() {
        assertThat(Fen.parse("8/4P3/8/8/8/8/8/k6K w - - 0 1").legalMoves())
                .map(Move::toString)
                .contains("e7e8q", "e7e8r", "e7e8b", "e7e8n")
                .doesNotContain("e7e8");
    }

    @ParameterizedTest
    @CsvSource({
            // the queen replaces the pawn
            "8/4P3/8/8/8/8/8/k6K w - - 0 1,   e7e8q, 4Q3/8/8/8/8/8/8/k6K b - - 0 1",
            // the chosen piece is placed, not always a queen
            "8/4P3/8/8/8/8/8/k6K w - - 0 1,   e7e8n, 4N3/8/8/8/8/8/8/k6K b - - 0 1",
            // a capturing promotion
            "3r4/4P3/8/8/8/8/8/k6K w - - 0 1, e7d8r, 3R4/8/8/8/8/8/8/k6K b - - 0 1",
            // black promotes on rank 1
            "k6K/8/8/8/8/8/4p3/8 b - - 0 1,   e2e1q, k6K/8/8/8/8/8/8/4q3 w - - 0 2",
            // capturing the rook on a8 also removes black's queenside castling right
            "r3k3/1P6/8/8/8/8/8/4K3 w q - 0 1, b7a8q, Q3k3/8/8/8/8/8/8/4K3 b - - 0 1",
    })
    void promotionPlacesTheChosenPieceInsteadOfThePawn(String fen, String uci, String expectedFen) {
        assertThat(Fen.toFen(Fen.parse(fen).play(move(uci)))).isEqualTo(expectedFen);
    }

    /** Returns the pawn's moves from {@code from} in UCI form, e.g. {@code "e7e8q"}. */
    private static List<String> pawnMoves(String fen, String from) {
        return PieceType.PAWN.movement().moves(Fen.parse(fen), Square.of(from)).stream()
                .map(Move::toString)
                .toList();
    }

    /** Parses a move in UCI form, e.g. {@code "e2e4"} or {@code "e7e8q"}. */
    private static Move move(String uci) {
        Square from = Square.of(uci.substring(0, 2));
        Square to = Square.of(uci.substring(2, 4));
        PieceType promotion = uci.length() == 5 ? PieceType.fromLetter(uci.charAt(4)).orElseThrow() : null;
        return new Move(from, to, promotion);
    }
}
