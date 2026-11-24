package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AttackTests {

    @Test
    void knightAttacksItsEightJumpSquares() {
        assertThat(attackedSquares("8/8/8/8/3N4/8/8/8 w - - 0 1", Color.WHITE))
                .containsExactlyInAnyOrder("b3", "b5", "c2", "c6", "e2", "e6", "f3", "f5");
    }

    @Test
    void kingAttacksTheEightSquaresAroundIt() {
        assertThat(attackedSquares("8/8/8/8/3k4/8/8/8 b - - 0 1", Color.BLACK))
                .containsExactlyInAnyOrder("c3", "c4", "c5", "d3", "d5", "e3", "e4", "e5");
    }

    @Test
    void rookAttacksAlongItsRankAndFileUpToTheFirstPiece() {
        // black pawn on d6 and black knight on f4 block; their own squares are attacked
        assertThat(attackedSquares("8/8/3p4/8/3R1n2/8/8/8 w - - 0 1", Color.WHITE))
                .containsExactlyInAnyOrder("d5", "d6", "d3", "d2", "d1", "e4", "f4", "c4", "b4", "a4");
    }

    @Test
    void bishopAttacksAlongItsDiagonals() {
        assertThat(attackedSquares("8/8/8/8/3B4/8/8/8 w - - 0 1", Color.WHITE))
                .containsExactlyInAnyOrder(
                        "e5", "f6", "g7", "h8", "e3", "f2", "g1", "c3", "b2", "a1", "c5", "b6", "a7");
    }

    @Test
    void queenAttacksLikeARookAndABishop() {
        assertThat(attackedSquares("8/8/8/8/3q4/8/8/8 b - - 0 1", Color.BLACK)).hasSize(27);
    }

    @Test
    void pawnsAttackDiagonallyForwardButNotStraightAhead() {
        assertThat(attackedSquares("8/8/8/8/4P3/8/8/8 w - - 0 1", Color.WHITE)).containsExactlyInAnyOrder("d5", "f5");
        assertThat(attackedSquares("8/8/8/4p3/8/8/8/8 b - - 0 1", Color.BLACK)).containsExactlyInAnyOrder("d4", "f4");
        assertThat(attackedSquares("8/8/8/8/8/8/P7/8 w - - 0 1", Color.WHITE)).containsExactly("b3");
    }

    @Test
    void ownPiecesAreAttackedTooSoTheyCountAsDefended() {
        // the rook on a1 defends the knight on a4, but the knight blocks the rook beyond it
        Board board = Fen.parse("8/8/8/8/N7/8/8/R7 w - - 0 1").board();

        assertThat(MoveGenerator.isAttacked(board, Square.of("a4"), Color.WHITE)).isTrue();
        assertThat(MoveGenerator.isAttacked(board, Square.of("a7"), Color.WHITE)).isFalse();
    }

    @Test
    void piecesDoNotAttackForTheOtherColor() {
        assertThat(attackedSquares("8/8/8/8/3N4/8/8/8 w - - 0 1", Color.BLACK)).isEmpty();
    }

    /** Returns every square attacked by {@code byColor} in the FEN position, e.g. {@code "e4"}. */
    private static List<String> attackedSquares(String fen, Color byColor) {
        Board board = Fen.parse(fen).board();
        return Square.all().stream()
                .filter(square -> MoveGenerator.isAttacked(board, square, byColor))
                .map(Square::toString)
                .toList();
    }
}
