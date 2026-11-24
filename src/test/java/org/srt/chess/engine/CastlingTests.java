package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CastlingTests {

    @Test
    void whiteCanCastleOnBothSidesInAClearPosition() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1"))
                .containsExactlyInAnyOrder("e1g1", "e1c1");
    }

    @Test
    void blackCanCastleOnBothSidesInAClearPosition() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "e8"))
                .containsExactlyInAnyOrder("e8g8", "e8c8");
    }

    @Test
    void noCastlingWithoutTheRight() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R w Kkq - 0 1", "e1")).containsExactly("e1g1");
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R w kq - 0 1", "e1")).isEmpty();
    }

    @Test
    void noCastlingWithAPieceBetweenKingAndRook() {
        // the knight on b1 blocks queenside: the b-square must be empty too
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/RN2K2R w KQkq - 0 1", "e1")).containsExactly("e1g1");
        assertThat(castlingMoves(Fen.STARTING_POSITION, "e1")).isEmpty();
    }

    @Test
    void noCastlingWhileInCheck() {
        assertThat(castlingMoves("4r1k1/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1")).isEmpty();
    }

    @Test
    void noCastlingThroughAnAttackedSquare() {
        // the rook on f8 attacks f1, the rook on d8 attacks d1
        assertThat(castlingMoves("5rk1/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1")).containsExactly("e1c1");
        assertThat(castlingMoves("3r2k1/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1")).containsExactly("e1g1");
    }

    @Test
    void noCastlingIntoCheck() {
        // the rook on g8 attacks g1, the rook on c8 attacks c1
        assertThat(castlingMoves("6rk/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1")).containsExactly("e1c1");
        assertThat(castlingMoves("2r3k1/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1")).containsExactly("e1g1");
    }

    @Test
    void queensideCastlingIsAllowedWhenOnlyTheBSquareIsAttacked() {
        // the rook on b8 attacks b1, which the king doesn't cross
        assertThat(castlingMoves("1r4k1/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1"))
                .containsExactlyInAnyOrder("e1g1", "e1c1");
    }

    /** Returns the castling moves of the king on {@code from} in UCI form: its moves two files sideways. */
    private static List<String> castlingMoves(String fen, String from) {
        return MoveGenerator.kingMoves(Fen.parse(fen), Square.of(from)).stream()
                .filter(move -> Math.abs(move.to().file().ordinal() - move.from().file().ordinal()) == 2)
                .map(Move::toString)
                .toList();
    }
}
