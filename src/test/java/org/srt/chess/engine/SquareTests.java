package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SquareTests {

    @ParameterizedTest
    @CsvSource({
            "a1, 0, 0",
            "h8, 7, 7",
            "e4, 4, 3",
            "a8, 0, 7",
            "h1, 7, 0",
    })
    void parsesAlgebraicNotation(String name, int file, int rank) {
        assertThat(Square.of(name)).isEqualTo(new Square(file, rank));
    }

    @ParameterizedTest
    @ValueSource(strings = {"e9", "i1", "", "a0", "e", "e44", "E4", " e4", "4e"})
    void rejectsInvalidNames(String name) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Square.of(name))
                .withMessageContaining("Invalid square \"" + name + "\"");
    }

    @Test
    void rejectsNullName() {
        assertThatNullPointerException()
                .isThrownBy(() -> Square.of(null))
                .withMessage("square name must not be null");
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "8, 0", "0, -1", "0, 8"})
    void rejectsCoordinatesOutsideTheBoard(int file, int rank) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Square(file, rank))
                .withMessageContaining("must be 0-7");
    }

    @ParameterizedTest
    @MethodSource("allSquareNames")
    void toStringGivesBackTheParsedName(String name) {
        assertThat(Square.of(name).toString()).isEqualTo(name);
    }

    static Stream<String> allSquareNames() {
        return "abcdefgh".chars().boxed()
                .flatMap(file -> IntStream.rangeClosed(1, 8).mapToObj(rank -> (char) file.intValue() + "" + rank));
    }

    @Test
    void thereAreSixtyFourSquareNames() {
        assertThat(allSquareNames().distinct()).hasSize(64);
    }
}
