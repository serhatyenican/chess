package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ColorTests {

    @Test
    void oppositeOfWhiteIsBlack() {
        assertThat(Color.WHITE.opposite()).isEqualTo(Color.BLACK);
    }

    @Test
    void oppositeOfBlackIsWhite() {
        assertThat(Color.BLACK.opposite()).isEqualTo(Color.WHITE);
    }

    @ParameterizedTest
    @EnumSource(Color.class)
    void oppositeTwiceGivesTheSameColor(Color color) {
        assertThat(color.opposite().opposite()).isEqualTo(color);
    }
}
