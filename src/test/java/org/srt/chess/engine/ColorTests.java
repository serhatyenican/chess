package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

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

    @ParameterizedTest
    @CsvSource({"w, WHITE", "b, BLACK"})
    void fromLetterFindsEachColorByItsLetter(char letter, Color color) {
        assertThat(Color.fromLetter(letter)).isEqualTo(color);
        assertThat(color.letter()).isEqualTo(letter);
    }

    @ParameterizedTest
    @ValueSource(chars = {'W', 'B', 'x', '-'})
    void fromLetterRejectsOtherCharacters(char letter) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Color.fromLetter(letter))
                .withMessage("unknown color letter '" + letter + "', expected 'w' or 'b'");
    }
}
