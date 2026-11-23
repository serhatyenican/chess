package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
}
