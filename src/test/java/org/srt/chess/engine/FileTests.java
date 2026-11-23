package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FileTests {

    @ParameterizedTest
    @EnumSource(File.class)
    void fromLetterFindsEachFileByItsLetter(File file) {
        assertThat(File.fromLetter(file.letter())).isEqualTo(file);
    }

    @ParameterizedTest
    @ValueSource(chars = {'A', 'E', 'i', '1', ' '})
    void fromLetterRejectsOtherCharacters(char letter) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> File.fromLetter(letter))
                .withMessage("unknown file letter '" + letter + "', expected one of a-h");
    }

    @Test
    void constantsAreDeclaredFromAToH() {
        String letters = Arrays.stream(File.values())
                .map(file -> String.valueOf(file.letter()))
                .collect(Collectors.joining());

        assertThat(letters).isEqualTo("abcdefgh");
    }
}
