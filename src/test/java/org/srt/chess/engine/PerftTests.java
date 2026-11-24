package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/** Positions and expected counts from https://www.chessprogramming.org/Perft_Results. */
class PerftTests {

    private static final String KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    private static final String POSITION_3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
    private static final String POSITION_4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    private static final String POSITION_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";

    @ParameterizedTest(name = "depth {0} -> {1}")
    @CsvSource({
            "0, 1",
            "1, 20",
            "2, 400",
            "3, 8902",
            "4, 197281",
    })
    void startingPosition(int depth, long expected) {
        assertThat(Perft.count(Position.starting(), depth)).isEqualTo(expected);
    }

    /** Castling on both sides, en passant, pins and promotions. */
    @ParameterizedTest(name = "depth {0} -> {1}")
    @CsvSource({
            "1, 48",
            "2, 2039",
            "3, 97862",
    })
    void kiwipete(int depth, long expected) {
        assertThat(Perft.count(Fen.parse(KIWIPETE), depth)).isEqualTo(expected);
    }

    /** An endgame with checks, and an en passant capture that would expose the king along the rank. */
    @ParameterizedTest(name = "depth {0} -> {1}")
    @CsvSource({
            "1, 14",
            "2, 191",
            "3, 2812",
            "4, 43238",
    })
    void position3(int depth, long expected) {
        assertThat(Perft.count(Fen.parse(POSITION_3), depth)).isEqualTo(expected);
    }

    /** White starts in check, with promotions (also by capturing) on both sides and castling for Black only. */
    @ParameterizedTest(name = "depth {0} -> {1}")
    @CsvSource({
            "1, 6",
            "2, 264",
            "3, 9467",
    })
    void position4(int depth, long expected) {
        assertThat(Perft.count(Fen.parse(POSITION_4), depth)).isEqualTo(expected);
    }

    /** A promotion by capture onto the back rank, and castling rights lost when a rook is captured. */
    @ParameterizedTest(name = "depth {0} -> {1}")
    @CsvSource({
            "1, 44",
            "2, 1486",
            "3, 62379",
    })
    void position5(int depth, long expected) {
        assertThat(Perft.count(Fen.parse(POSITION_5), depth)).isEqualTo(expected);
    }

    @Test
    void divideAtDepthOneCountsEachLegalMoveOnce() {
        Map<String, Long> divide = Perft.divide(Position.starting(), 1);

        assertThat(divide).hasSize(20).containsEntry("e2e4", 1L);
        assertThat(divide.values()).containsOnly(1L);
    }

    @Test
    void divideSplitsTheCountByFirstMove() {
        Map<String, Long> divide = Perft.divide(Position.starting(), 3);

        assertThat(divide).containsEntry("e2e4", 600L).containsEntry("g1f3", 440L);
        assertThat(divide.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(8902L);
    }

    @Test
    void divideNeedsAFirstMove() {
        assertThatIllegalArgumentException().isThrownBy(() -> Perft.divide(Position.starting(), 0));
    }
}
