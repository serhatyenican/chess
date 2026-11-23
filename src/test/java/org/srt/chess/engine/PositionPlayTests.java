package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PositionPlayTests {

    @Test
    void movesThePieceAndPassesTheTurn() {
        assertThat(play(Fen.STARTING_POSITION, "g1f3"))
                .isEqualTo("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1");
    }

    @Test
    void fullmoveNumberIncreasesAfterBlackMoves() {
        assertThat(play(Fen.STARTING_POSITION, "g1f3", "b8c6"))
                .isEqualTo("r1bqkbnr/pppppppp/2n5/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 2 2");
    }

    @Test
    void pawnMoveResetsTheHalfmoveClock() {
        assertThat(play("4k3/8/8/8/8/8/4P3/4K3 w - - 5 10", "e2e3"))
                .isEqualTo("4k3/8/8/8/8/4P3/8/4K3 b - - 0 10");
    }

    @Test
    void captureReplacesThePieceAndResetsTheHalfmoveClock() {
        assertThat(play("4k3/8/8/3p4/8/2N5/8/4K3 w - - 5 10", "c3d5"))
                .isEqualTo("4k3/8/8/3N4/8/8/8/4K3 b - - 0 10");
    }

    @Test
    void doubleStepSetsTheEnPassantSquareBehindThePawn() {
        assertThat(play(Fen.STARTING_POSITION, "e2e4"))
                .isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
    }

    @Test
    void nextMoveClearsTheEnPassantSquare() {
        assertThat(play(Fen.STARTING_POSITION, "e2e4", "g8f6"))
                .isEqualTo("rnbqkb1r/pppppppp/5n2/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 1 2");
    }

    @Test
    void blackDoubleStepSetsTheEnPassantSquareBehindThePawn() {
        assertThat(play(Fen.STARTING_POSITION, "g1f3", "d7d5"))
                .isEqualTo("rnbqkbnr/ppp1pppp/8/3p4/8/5N2/PPPPPPPP/RNBQKB1R w KQkq d6 0 2");
    }

    @Test
    void leavesTheOriginalPositionUnchanged() {
        Position start = Position.starting();

        start.play(move("g1f3"));

        assertThat(Fen.toFen(start)).isEqualTo(Fen.STARTING_POSITION);
    }

    @Test
    void rejectsAMoveFromAnEmptySquare() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Position.starting().play(move("e4e5")))
                .withMessage("no piece on e4 to play e4e5");
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> Position.starting().play(null))
                .withMessage("move must not be null");
    }

    /** Plays the moves, given in UCI form like {@code "e2e4"}, from the FEN and returns the resulting FEN. */
    private static String play(String fen, String... moves) {
        Position position = Fen.parse(fen);
        for (String uci : moves) {
            position = position.play(move(uci));
        }
        return Fen.toFen(position);
    }

    private static Move move(String uci) {
        return new Move(Square.of(uci.substring(0, 2)), Square.of(uci.substring(2, 4)));
    }
}
