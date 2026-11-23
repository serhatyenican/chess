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
            "a1, A, 0",
            "h8, H, 7",
            "e4, E, 3",
            "a8, A, 7",
            "h1, H, 0",
    })
    void parsesAlgebraicNotation(String name, File file, int rank) {
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
    @ValueSource(ints = {-1, 8})
    void rejectsRankOutsideTheBoard(int rank) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Square(File.A, rank))
                .withMessage("rank must be 0-7, was " + rank);
    }

    @Test
    void rejectsMissingFile() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Square(null, 0))
                .withMessage("file must not be null");
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
