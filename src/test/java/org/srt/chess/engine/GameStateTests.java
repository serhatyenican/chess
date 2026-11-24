package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateTests {

    @Test
    void startingPositionIsInProgress() {
        assertThat(GameState.of(Position.starting())).isEqualTo(GameState.IN_PROGRESS);
        assertThat(GameState.winner(Position.starting())).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            // fool's mate: the queen on h4 mates the white king
            "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3, BLACK",
            // back-rank mate by white: the king can't flee to h8 along the rook's line
            "R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1, WHITE",
            // back-rank mate by black, the mirror image
            "6k1/8/8/8/8/8/5PPP/r5K1 w - - 0 1, BLACK",
    })
    void checkmateIsWonByTheSideNotToMove(String fen, Color winner) {
        Position position = Fen.parse(fen);

        assertThat(GameState.of(position)).isEqualTo(GameState.CHECKMATE);
        assertThat(GameState.winner(position)).contains(winner);
    }

    @Test
    void stalemateIsADraw() {
        // the black king on h8 is not in check, but every square it could move to is attacked
        Position position = Fen.parse("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1");

        assertThat(GameState.of(position)).isEqualTo(GameState.DRAW);
        assertThat(GameState.winner(position)).isEmpty();
    }

    @Test
    void checkWithALegalEscapeIsInProgress() {
        // the rook on e1 checks the black king, which can step off the e-file
        assertThat(GameState.of(Fen.parse("4k3/8/8/8/8/8/8/4R2K b - - 0 1"))).isEqualTo(GameState.IN_PROGRESS);
    }
}
