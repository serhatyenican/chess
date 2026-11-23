package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PieceTests {

    @Test
    void piecesWithTheSameColorAndTypeAreEqual() {
        assertThat(new Piece(Color.WHITE, PieceType.KNIGHT))
                .isEqualTo(new Piece(Color.WHITE, PieceType.KNIGHT))
                .isNotEqualTo(new Piece(Color.BLACK, PieceType.KNIGHT));
    }

    @Test
    void rejectsMissingColor() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Piece(null, PieceType.KNIGHT))
                .withMessage("color must not be null");
    }

    @Test
    void rejectsMissingType() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Piece(Color.WHITE, null))
                .withMessage("type must not be null");
    }

    @ParameterizedTest
    @CsvSource({
            "K, WHITE, KING", "Q, WHITE, QUEEN", "R, WHITE, ROOK", "B, WHITE, BISHOP", "N, WHITE, KNIGHT", "P, WHITE, PAWN",
            "k, BLACK, KING", "q, BLACK, QUEEN", "r, BLACK, ROOK", "b, BLACK, BISHOP", "n, BLACK, KNIGHT", "p, BLACK, PAWN",
    })
    void lettersMapToPieces(char letter, Color color, PieceType type) {
        Piece piece = new Piece(color, type);

        assertThat(Piece.fromLetter(letter)).isEqualTo(piece);
        assertThat(piece.letter()).isEqualTo(letter);
    }

    @ParameterizedTest
    @ValueSource(chars = {'x', 'X', '1', '.', ' ', 'K'})
    void rejectsUnknownLetters(char letter) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Piece.fromLetter(letter))
                .withMessage("unknown piece letter '" + letter + "'");
    }
}
