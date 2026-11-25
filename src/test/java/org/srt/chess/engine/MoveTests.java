package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MoveTests {

    private static final Square E2 = Square.of("e2");
    private static final Square E4 = Square.of("e4");
    private static final Square E7 = Square.of("e7");
    private static final Square E8 = Square.of("e8");

    @Test
    void normalMoveHasNoPromotion() {
        Move move = new Move(E2, E4);

        assertThat(move.promotion()).isNull();
        assertThat(move.isPromotion()).isFalse();
        assertThat(move).isEqualTo(new Move(E2, E4, null));
    }

    @Test
    void promotionMoveKeepsThePromotionPiece() {
        Move move = new Move(E7, E8, PieceType.QUEEN);

        assertThat(move.promotion()).isEqualTo(PieceType.QUEEN);
        assertThat(move.isPromotion()).isTrue();
        assertThat(move).isNotEqualTo(new Move(E7, E8, PieceType.KNIGHT));
    }

    @Test
    void rejectsMissingSquares() {
        assertThatNullPointerException().isThrownBy(() -> new Move(null, E4)).withMessage("from must not be null");
        assertThatNullPointerException().isThrownBy(() -> new Move(E2, null)).withMessage("to must not be null");
    }

    @Test
    void rejectsMoveToTheSameSquare() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Move(E4, E4))
                .withMessage("from and to must differ, both were e4");
    }

    @ParameterizedTest
    @EnumSource(value = PieceType.class, names = {"KING", "PAWN"})
    void rejectsPromotionToKingOrPawn(PieceType promotion) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Move(E7, E8, promotion))
                .withMessage("a pawn cannot promote to " + promotion);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "none", value = {
            "e2, e4, none,   e2e4",
            "g1, f3, none,   g1f3",
            "e7, e8, QUEEN,  e7e8q",
            "a2, a1, ROOK,   a2a1r",
            "b7, c8, BISHOP, b7c8b",
            "h2, h1, KNIGHT, h2h1n",
    })
    void toStringUsesUciNotation(String from, String to, PieceType promotion, String expected) {
        assertThat(new Move(Square.of(from), Square.of(to), promotion)).hasToString(expected);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "none", value = {
            "e2e4,  e2, e4, none",
            "g1f3,  g1, f3, none",
            "e7e8q, e7, e8, QUEEN",
            "a2a1r, a2, a1, ROOK",
            "b7c8b, b7, c8, BISHOP",
            "h2h1n, h2, h1, KNIGHT",
    })
    void readsUciNotation(String text, String from, String to, PieceType promotion) {
        assertThat(Move.of(text)).isEqualTo(new Move(Square.of(from), Square.of(to), promotion));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            Fen.STARTING_POSITION,
            "1r5k/P7/8/8/8/8/8/K7 w - - 0 1", // white can promote on a8 and by capturing on b8
            "k7/8/8/8/8/8/p7/1R5K b - - 0 1", // black can promote on a1 and by capturing on b1
    })
    void readingTheTextOfALegalMoveGivesTheSameMove(String fen) {
        assertThat(Fen.parse(fen).legalMoves())
                .isNotEmpty()
                .allSatisfy(move -> assertThat(Move.of(move.toString())).isEqualTo(move));
    }

    @Test
    void readsMovesThatAreNotLegal() {
        assertThat(Move.of("e2e5")).isEqualTo(new Move(E2, Square.of("e5")));
        assertThat(Move.of("e7e8")).isEqualTo(new Move(E7, E8));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "e2", "e2e", "e2e4qq"})
    void rejectsTextOfTheWrongLength(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Move.of(text))
                .withMessage("Invalid move \"" + text
                        + "\": expected two squares and an optional promotion letter, e.g. \"e2e4\" or \"e7e8q\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"    ", "e2e9", "i2e4", "E2E4", "e2-e4"})
    void rejectsTextWithAnInvalidSquare(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Move.of(text))
                .withMessageStartingWith("Invalid move \"" + text + "\": Invalid square \"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"e7e8x", "e7e8Q", "e7e8 "})
    void rejectsTextWithAnUnknownPromotionLetter(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Move.of(text))
                .withMessage("Invalid move \"" + text + "\": unknown promotion letter '" + text.charAt(4)
                        + "', expected q, r, b or n");
    }

    @ParameterizedTest
    @CsvSource({
            "e7e8k, a pawn cannot promote to KING",
            "e7e8p, a pawn cannot promote to PAWN",
            "e2e2,  'from and to must differ, both were e2'",
    })
    void rejectsTextOfAnImpossibleMove(String text, String reason) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Move.of(text))
                .withMessage("Invalid move \"" + text + "\": " + reason);
    }
}
