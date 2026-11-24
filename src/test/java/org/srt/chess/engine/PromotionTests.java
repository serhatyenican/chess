package org.srt.chess.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionTests {

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

    /** Parses a move in UCI form, e.g. {@code "e2e4"} or {@code "e7e8q"}. */
    private static Move move(String uci) {
        Square from = Square.of(uci.substring(0, 2));
        Square to = Square.of(uci.substring(2, 4));
        PieceType promotion = uci.length() == 5 ? PieceType.fromLetter(uci.charAt(4)).orElseThrow() : null;
        return new Move(from, to, promotion);
    }
}
