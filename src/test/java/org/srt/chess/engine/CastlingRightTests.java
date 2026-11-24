package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CastlingRightTests {

    @ParameterizedTest
    @EnumSource(CastlingRight.class)
    void fromLetterFindsEachRightByItsLetter(CastlingRight right) {
        assertThat(CastlingRight.fromLetter(right.letter())).isEqualTo(right);
    }

    @ParameterizedTest
    @ValueSource(chars = {'-', 'x', 'B', '1'})
    void fromLetterRejectsOtherCharacters(char letter) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CastlingRight.fromLetter(letter))
                .withMessage("unknown castling right letter '" + letter + "', expected one of KQkq");
    }

    @ParameterizedTest
    @CsvSource({
            "WHITE_KINGSIDE,  WHITE, e1, g1, h1, f1",
            "WHITE_QUEENSIDE, WHITE, e1, c1, a1, d1",
            "BLACK_KINGSIDE,  BLACK, e8, g8, h8, f8",
            "BLACK_QUEENSIDE, BLACK, e8, c8, a8, d8",
    })
    void knowsTheSquaresOfKingAndRook(CastlingRight right, Color color, String kingFrom, String kingTo,
                                      String rookFrom, String rookTo) {
        assertThat(right.color()).isEqualTo(color);
        assertThat(right.kingFrom()).isEqualTo(Square.of(kingFrom));
        assertThat(right.kingTo()).isEqualTo(Square.of(kingTo));
        assertThat(right.rookFrom()).isEqualTo(Square.of(rookFrom));
        assertThat(right.rookTo()).isEqualTo(Square.of(rookTo));
    }

    @ParameterizedTest
    @CsvSource({
            "WHITE_KINGSIDE,  f1 g1,    e1 f1 g1",
            "WHITE_QUEENSIDE, d1 c1 b1, e1 d1 c1",
            "BLACK_KINGSIDE,  f8 g8,    e8 f8 g8",
            "BLACK_QUEENSIDE, d8 c8 b8, e8 d8 c8",
    })
    void knowsTheSquaresThatMustBeEmptyAndThoseThatMustNotBeAttacked(
            CastlingRight right, String squaresBetween, String kingPath) {
        assertThat(right.squaresBetween()).isEqualTo(squares(squaresBetween));
        assertThat(right.kingPath()).isEqualTo(squares(kingPath));
    }

    @Test
    void constantsAreDeclaredInFenOrder() {
        String letters = Arrays.stream(CastlingRight.values())
                .map(right -> String.valueOf(right.letter()))
                .collect(Collectors.joining());

        assertThat(letters).isEqualTo("KQkq");
    }

    private static List<Square> squares(String names) {
        return Arrays.stream(names.split(" ")).map(Square::of).toList();
    }
}
