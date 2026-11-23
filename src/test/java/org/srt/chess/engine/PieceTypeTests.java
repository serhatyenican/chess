package org.srt.chess.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PieceTypeTests {

    @ParameterizedTest
    @EnumSource(PieceType.class)
    void fromLetterFindsEachTypeByItsLetter(PieceType type) {
        assertThat(PieceType.fromLetter(type.letter())).contains(type);
    }

    @ParameterizedTest
    @ValueSource(chars = {'K', 'x', '1', ' '})
    void fromLetterIsEmptyForOtherCharacters(char letter) {
        assertThat(PieceType.fromLetter(letter)).isEmpty();
    }
}
