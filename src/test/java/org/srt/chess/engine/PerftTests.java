package org.srt.chess.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Expected counts from https://www.chessprogramming.org/Perft_Results. */
class PerftTests {

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
}
