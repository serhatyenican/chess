package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnPassantTests {

    @Test
    void whiteCanCaptureEnPassantRightAfterTheDoubleStep() {
        Position position = play("4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 1", "d7d5");

        assertThat(pawnMoves(position, "e5")).containsExactlyInAnyOrder("e5e6", "e5d6");
    }

    @Test
    void blackCanCaptureEnPassantRightAfterTheDoubleStep() {
        Position position = play("4k3/8/8/8/4p3/8/3P4/4K3 w - - 0 1", "d2d4");

        assertThat(pawnMoves(position, "e4")).containsExactlyInAnyOrder("e4e3", "e4d3");
    }

    @Test
    void enPassantIsNoLongerPossibleOneMoveLater() {
        Position position = play("4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 1", "d7d5", "e1d1", "e8d8");

        assertThat(pawnMoves(position, "e5")).containsExactly("e5e6");
    }

    @Test
    void onlyAPawnBesideTheDoubleSteppedPawnCanCaptureIt() {
        // the en passant square d6 is not diagonally in front of the pawn on b5
        assertThat(pawnMoves(Fen.parse("4k3/8/8/1P1p4/8/8/8/4K3 w - d6 0 1"), "b5")).containsExactly("b5b6");
    }

    @Test
    void noEnPassantWithoutAPawnToCapture() {
        // a hand-written FEN can set an en passant square without the pawn that double-stepped
        assertThat(pawnMoves(Fen.parse("4k3/8/8/4P3/8/8/8/4K3 w - d6 0 1"), "e5")).containsExactly("e5e6");
    }

    @Test
    void enPassantIsIllegalWhenRemovingBothPawnsExposesTheKing() {
        // both pawns leave rank 5, opening the line from the rook on h5 to the king on a5
        Position position = Fen.parse("8/8/8/KPp4r/8/8/8/7k w - c6 0 1");

        assertThat(pawnMoves(position, "b5")).contains("b5c6");
        assertThat(MoveGenerator.legalMoves(position)).map(Move::toString).doesNotContain("b5c6");
    }

    @Test
    void whiteEnPassantCaptureRemovesThePawnBesideTheTargetSquare() {
        // the white pawn on e5 captures on f6; the black pawn on f5 is removed
        assertThat(Fen.toFen(play("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "e5f6")))
                .isEqualTo("rnbqkbnr/ppp1p1pp/5P2/3p4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3");
    }

    @Test
    void blackEnPassantCaptureRemovesThePawnBesideTheTargetSquare() {
        // the black pawn on d4 captures on e3; the white pawn on e4 is removed
        assertThat(Fen.toFen(play("4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1", "d4e3")))
                .isEqualTo("4k3/8/8/8/8/4p3/8/4K3 w - - 0 2");
    }

    /** Plays the moves, given in UCI form like {@code "e5f6"}, from the FEN. */
    private static Position play(String fen, String... moves) {
        Position position = Fen.parse(fen);
        for (String uci : moves) {
            position = position.play(new Move(Square.of(uci.substring(0, 2)), Square.of(uci.substring(2, 4))));
        }
        return position;
    }

    /** Returns the pawn's moves from {@code from} in UCI form, e.g. {@code "e5d6"}. */
    private static List<String> pawnMoves(Position position, String from) {
        return MoveGenerator.pawnMoves(position, Square.of(from)).stream()
                .map(Move::toString)
                .toList();
    }
}
