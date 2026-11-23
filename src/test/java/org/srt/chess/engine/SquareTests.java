package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void allListsTheSixtyFourSquaresRankByRank() {
        List<Square> all = Square.all();

        assertThat(all).hasSize(64).doesNotHaveDuplicates();
        assertThat(all.getFirst()).isEqualTo(Square.of("a1"));
        assertThat(all.get(7)).isEqualTo(Square.of("h1"));
        assertThat(all.get(8)).isEqualTo(Square.of("a2"));
        assertThat(all.getLast()).isEqualTo(Square.of("h8"));
    }

    @Test
    void allIsBuiltOnceAndCannotBeChanged() {
        assertThat(Square.all()).isSameAs(Square.all());
        assertThatThrownBy(() -> Square.all().add(Square.of("e4")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "d4,  1,  2, e6",   // knight jump up and right
            "d4, -2, -1, b3",   // knight jump down and left
            "e2,  0,  1, e3",   // one rank up
            "e7,  0, -1, e6",   // one rank down
            "a1,  7,  7, h8",   // corner to corner
            "h1, -7,  7, a8",
            "d4,  0,  0, d4",   // no change
    })
    void offsetReturnsTheTargetSquare(String from, int fileDelta, int rankDelta, String expected) {
        assertThat(Square.of(from).offset(fileDelta, rankDelta)).contains(Square.of(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "a1, -1,  0",   // left of the a-file
            "h1,  1,  0",   // right of the h-file
            "a1,  0, -1",   // below rank 1
            "a8,  0,  1",   // above rank 8
            "b1, -2,  1",   // knight jump off the left edge
            "g8,  1,  2",   // knight jump off the top
    })
    void offsetIsEmptyOffTheBoard(String from, int fileDelta, int rankDelta) {
        assertThat(Square.of(from).offset(fileDelta, rankDelta)).isEmpty();
    }
}
