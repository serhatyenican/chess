package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class BoardTests {

    private static final Square E1 = Square.of("e1");
    private static final Square E8 = Square.of("e8");
    private static final Piece WHITE_KING = new Piece(Color.WHITE, PieceType.KING);
    private static final Piece BLACK_KING = new Piece(Color.BLACK, PieceType.KING);
    private static final Piece WHITE_QUEEN = new Piece(Color.WHITE, PieceType.QUEEN);

    @Test
    void emptyBoardHasNoPieces() {
        IntStream.range(0, 64).forEach(i -> {
            Square square = new Square(File.fromIndex(i % 8), i / 8);
            assertThat(Board.empty().pieceAt(square)).as("piece at %s", square).isEmpty();
        });
    }

    @Test
    void withPlacesThePiece() {
        Board board = Board.empty().with(E1, WHITE_KING);

        assertThat(board.pieceAt(E1)).contains(WHITE_KING);
        assertThat(board.pieceAt(E8)).isEmpty();
    }

    @Test
    void withLeavesTheOriginalBoardUnchanged() {
        Board original = Board.empty().with(E1, WHITE_KING);

        Board changed = original.with(E8, BLACK_KING);

        assertThat(original.pieceAt(E8)).isEmpty();
        assertThat(original).isEqualTo(Board.empty().with(E1, WHITE_KING));
        assertThat(changed.pieceAt(E8)).contains(BLACK_KING);
    }

    @Test
    void withReplacesThePieceOnTheSquare() {
        Board board = Board.empty().with(E1, WHITE_KING).with(E1, WHITE_QUEEN);

        assertThat(board.pieceAt(E1)).contains(WHITE_QUEEN);
    }

    @Test
    void withoutEmptiesTheSquareAndLeavesTheOriginalUnchanged() {
        Board original = Board.empty().with(E1, WHITE_KING);

        Board changed = original.without(E1);

        assertThat(changed.pieceAt(E1)).isEmpty();
        assertThat(changed).isEqualTo(Board.empty());
        assertThat(original.pieceAt(E1)).contains(WHITE_KING);
    }

    @Test
    void boardsWithTheSamePiecesAreEqual() {
        Board kingsFirstWhite = Board.empty().with(E1, WHITE_KING).with(E8, BLACK_KING);
        Board kingsFirstBlack = Board.empty().with(E8, BLACK_KING).with(E1, WHITE_KING);

        assertThat(kingsFirstWhite)
                .isEqualTo(kingsFirstBlack)
                .hasSameHashCodeAs(kingsFirstBlack)
                .isNotEqualTo(Board.empty().with(E1, WHITE_KING));
    }

    @Test
    void rejectsNullArguments() {
        assertThatNullPointerException()
                .isThrownBy(() -> Board.empty().with(E1, null))
                .withMessageContaining("piece must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> Board.empty().with(null, WHITE_KING))
                .withMessage("square must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> Board.empty().without(null))
                .withMessage("square must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> Board.empty().pieceAt(null))
                .withMessage("square must not be null");
    }

    @Test
    void toStringDrawsTheBoardWithRankEightAtTheTop() {
        Board board = Board.empty()
                .with(E1, WHITE_KING)
                .with(E8, BLACK_KING)
                .with(Square.of("a2"), new Piece(Color.WHITE, PieceType.PAWN));

        assertThat(board).hasToString("""
                . . . . k . . .
                . . . . . . . .
                . . . . . . . .
                . . . . . . . .
                . . . . . . . .
                . . . . . . . .
                P . . . . . . .
                . . . . K . . .
                """);
    }
}
