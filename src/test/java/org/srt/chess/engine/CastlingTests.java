package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    void noCastlingWithoutTheRookEvenIfTheRightIsSet() {
        // a hand-written FEN can claim a right whose rook isn't there
        assertThat(castlingMoves("4k3/8/8/8/8/8/8/4K3 w K - 0 1", "e1")).isEmpty();
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

    @ParameterizedTest
    @CsvSource({
            "w, e1g1, r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1",
            "w, e1c1, r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1",
            "b, e8g8, r4rk1/8/8/8/8/8/8/R3K2R w KQ - 1 2",
            "b, e8c8, 2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2",
    })
    void castlingMovesTheRookToTheSquareTheKingCrossed(String sideToMove, String castle, String expectedFen) {
        Position position = Fen.parse("r3k2r/8/8/8/8/8/8/R3K2R " + sideToMove + " KQkq - 0 1");

        assertThat(Fen.toFen(position.play(move(castle)))).isEqualTo(expectedFen);
    }

    @Test
    void otherPiecesMovingBetweenTheCastlingSquaresDoNotMoveTheRook() {
        // the queen slides from e1 to g1, like a kingside castle of the king
        Position position = Fen.parse("4k3/8/8/8/8/8/8/R3Q2R w - - 0 1");

        assertThat(Fen.toFen(position.play(move("e1g1")))).isEqualTo("4k3/8/8/8/8/8/8/R5QR b - - 1 1");
    }

    /** Returns the castling moves of the king on {@code from} in UCI form: its moves two files sideways. */
    private static List<String> castlingMoves(String fen, String from) {
        return PieceType.KING.movement().moves(Fen.parse(fen), Square.of(from)).stream()
                .filter(move -> Math.abs(move.to().file().ordinal() - move.from().file().ordinal()) == 2)
                .map(Move::toString)
                .toList();
    }

    private static Move move(String uci) {
        return new Move(Square.of(uci.substring(0, 2)), Square.of(uci.substring(2, 4)));
    }
}
