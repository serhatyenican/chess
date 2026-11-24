package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnPassantTests {

    @Test
    void whiteEnPassantCaptureRemovesThePawnBesideTheTargetSquare() {
        // the white pawn on e5 captures on f6; the black pawn on f5 is removed
        assertThat(play("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "e5f6"))
                .isEqualTo("rnbqkbnr/ppp1p1pp/5P2/3p4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3");
    }

    @Test
    void blackEnPassantCaptureRemovesThePawnBesideTheTargetSquare() {
        // the black pawn on d4 captures on e3; the white pawn on e4 is removed
        assertThat(play("4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1", "d4e3"))
                .isEqualTo("4k3/8/8/8/8/4p3/8/4K3 w - - 0 2");
    }

    /** Plays the move, given in UCI form like {@code "e5f6"}, from the FEN and returns the resulting FEN. */
    private static String play(String fen, String uci) {
        Move move = new Move(Square.of(uci.substring(0, 2)), Square.of(uci.substring(2, 4)));
        return Fen.toFen(Fen.parse(fen).play(move));
    }
}
