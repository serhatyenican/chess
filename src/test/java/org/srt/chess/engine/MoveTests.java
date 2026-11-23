package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

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
}
