package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CastlingRightTests {

    @ParameterizedTest
    @EnumSource(CastlingRight.class)
    void fromLetterFindsEachRightByItsLetter(CastlingRight right) {
        assertThat(CastlingRight.fromLetter(right.letter())).contains(right);
    }

    @ParameterizedTest
    @ValueSource(chars = {'-', 'x', 'B', '1', 'K'})
    void fromLetterIsEmptyForOtherCharacters(char letter) {
        assertThat(CastlingRight.fromLetter(letter)).isEmpty();
    }

    @Test
    void constantsAreDeclaredInFenOrder() {
        String letters = Arrays.stream(CastlingRight.values())
                .map(right -> String.valueOf(right.letter()))
                .collect(Collectors.joining());

        assertThat(letters).isEqualTo("KQkq");
    }
}
