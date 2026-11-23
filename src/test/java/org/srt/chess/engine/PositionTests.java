package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionTests {

    private static final Board BOARD = Board.empty()
            .with(Square.of("e1"), new Piece(Color.WHITE, PieceType.KING))
            .with(Square.of("e8"), new Piece(Color.BLACK, PieceType.KING));

    @Test
    void keepsAllFields() {
        Position position = new Position(BOARD, Color.BLACK, Set.of(CastlingRight.WHITE_KINGSIDE),
                Square.of("e3"), 5, 12);

        assertThat(position.board()).isEqualTo(BOARD);
        assertThat(position.sideToMove()).isEqualTo(Color.BLACK);
        assertThat(position.castlingRights()).containsExactly(CastlingRight.WHITE_KINGSIDE);
        assertThat(position.enPassant()).isEqualTo(Square.of("e3"));
        assertThat(position.halfmoveClock()).isEqualTo(5);
        assertThat(position.fullmoveNumber()).isEqualTo(12);
    }

    @Test
    void startingPositionIsParsedOnce() {
        assertThat(Position.starting()).isSameAs(Position.starting());
    }

    @Test
    void enPassantSquareIsOptional() {
        Position position = new Position(BOARD, Color.WHITE, Set.of(), null, 0, 1);

        assertThat(position.enPassant()).isNull();
    }

    @Test
    void castlingRightsCannotBeChangedFromOutside() {
        Set<CastlingRight> rights = new HashSet<>(Set.of(CastlingRight.WHITE_KINGSIDE));
        Position position = new Position(BOARD, Color.WHITE, rights, null, 0, 1);

        rights.add(CastlingRight.BLACK_QUEENSIDE);

        assertThat(position.castlingRights()).containsExactly(CastlingRight.WHITE_KINGSIDE);
        assertThatThrownBy(() -> position.castlingRights().add(CastlingRight.BLACK_KINGSIDE))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void positionsWithTheSameStateAreEqual() {
        Position first = new Position(BOARD, Color.WHITE, EnumSet.allOf(CastlingRight.class), null, 0, 1);
        Position second = new Position(BOARD, Color.WHITE, Set.of(CastlingRight.values()), null, 0, 1);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @ParameterizedTest
    @CsvSource({"WHITE, e6", "WHITE, a6", "BLACK, e3", "BLACK, h3"})
    void acceptsEnPassantSquareBehindThePawnThatJustMoved(Color sideToMove, String square) {
        Position position = new Position(BOARD, sideToMove, Set.of(), Square.of(square), 0, 1);

        assertThat(position.enPassant()).isEqualTo(Square.of(square));
    }

    @ParameterizedTest
    @CsvSource({
            "WHITE, e3, 'en passant square must be on rank 6 when white is to move, was e3'",
            "BLACK, e6, 'en passant square must be on rank 3 when black is to move, was e6'",
            "WHITE, e4, 'en passant square must be on rank 6 when white is to move, was e4'",
    })
    void rejectsEnPassantSquareOnTheWrongRank(Color sideToMove, String square, String message) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Position(BOARD, sideToMove, Set.of(), Square.of(square), 0, 1))
                .withMessage(message);
    }

    @Test
    void rejectsInvalidCounters() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Position(BOARD, Color.WHITE, Set.of(), null, -1, 1))
                .withMessage("halfmoveClock must not be negative, was -1");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Position(BOARD, Color.WHITE, Set.of(), null, 0, 0))
                .withMessage("fullmoveNumber must be at least 1, was 0");
    }

    @Test
    void rejectsMissingValues() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Position(null, Color.WHITE, Set.of(), null, 0, 1))
                .withMessage("board must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Position(BOARD, null, Set.of(), null, 0, 1))
                .withMessage("sideToMove must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Position(BOARD, Color.WHITE, null, null, 0, 1))
                .withMessage("castlingRights must not be null");
        Set<CastlingRight> withNull = new HashSet<>();
        withNull.add(null);
        assertThatNullPointerException()
                .isThrownBy(() -> new Position(BOARD, Color.WHITE, withNull, null, 0, 1));
    }
}
