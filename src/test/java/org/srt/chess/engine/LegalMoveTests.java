package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegalMoveTests {

    @Test
    void startingPositionIsNotCheckAndHasTwentyLegalMoves() {
        assertThat(MoveGenerator.isInCheck(Position.starting())).isFalse();
        assertThat(MoveGenerator.legalMoves(Position.starting())).hasSize(20);
    }

    @Test
    void kingAttackedByTheOpponentIsInCheck() {
        // black king on e8, white rook on e1
        assertThat(MoveGenerator.isInCheck(Fen.parse("4k3/8/8/8/8/8/8/4R3 b - - 0 1"))).isTrue();
    }

    @Test
    void positionWithoutAKingIsNotInCheck() {
        assertThat(MoveGenerator.isInCheck(Fen.parse("8/8/8/8/3N4/8/8/8 w - - 0 1"))).isFalse();
    }

    @Test
    void inCheckOnlyMovesThatResolveTheCheckAreLegal() {
        // the rook on e8 checks the king on e1: capture it, block the e-file, or step off it
        assertThat(legalMoves("R3r1k1/8/8/8/8/3B4/8/4K3 w - - 0 1"))
                .containsExactlyInAnyOrder("a8e8", "d3e2", "d3e4", "e1d1", "e1d2", "e1f1", "e1f2");
    }

    @Test
    void pinnedPieceCannotLeaveTheLineToItsKing() {
        // the black rook on e8 pins the white rook on e2 to its king on e1
        assertThat(legalMoves("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1"))
                .filteredOn(move -> move.startsWith("e2"))
                .containsExactlyInAnyOrder("e2e3", "e2e4", "e2e5", "e2e6", "e2e7", "e2e8");
    }

    @Test
    void kingCannotMoveToAnAttackedSquare() {
        // the black rook on d8 attacks d1 and d2
        assertThat(legalMoves("3r2k1/8/8/8/8/8/8/4K3 w - - 0 1"))
                .containsExactlyInAnyOrder("e1e2", "e1f1", "e1f2");
    }

    @Test
    void kingCannotCaptureADefendedPiece() {
        // the knight on f2 attacks d1; the rook on f8 defends it
        assertThat(legalMoves("5rk1/8/8/8/8/8/5n2/4K3 w - - 0 1"))
                .containsExactlyInAnyOrder("e1d2", "e1e2", "e1f1");
        // without the rook, the king may capture it
        assertThat(legalMoves("6k1/8/8/8/8/8/5n2/4K3 w - - 0 1"))
                .containsExactlyInAnyOrder("e1d2", "e1e2", "e1f1", "e1f2");
    }

    @Test
    void kingsCannotStandNextToEachOther() {
        assertThat(legalMoves("8/8/8/3k4/8/3K4/8/8 w - - 0 1"))
                .containsExactlyInAnyOrder("d3c2", "d3d2", "d3e2", "d3c3", "d3e3");
    }

    @Test
    void kingCannotStepAwayAlongTheLineOfItsAttacker() {
        // before the move the king itself blocks the rook's line to e3, so the check must look at the board after it
        assertThat(legalMoves("4r1k1/8/8/8/4K3/8/8/8 w - - 0 1"))
                .containsExactlyInAnyOrder("e4d3", "e4d4", "e4d5", "e4f3", "e4f4", "e4f5");
    }

    /** Returns the legal moves of the side to move in UCI form, e.g. {@code "e2e4"}. */
    private static List<String> legalMoves(String fen) {
        return MoveGenerator.legalMoves(Fen.parse(fen)).stream()
                .map(Move::toString)
                .toList();
    }
}
