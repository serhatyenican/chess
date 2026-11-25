package org.srt.chess.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OffsetTests {

    @Test
    void applyToReturnsTheSquareThatFarAway() {
        assertThat(new Offset(1, 2).applyTo(Square.of("d4"))).contains(Square.of("e6"));
        assertThat(new Offset(-1, -1).applyTo(Square.of("d4"))).contains(Square.of("c3"));
    }

    @Test
    void applyToIsEmptyOffTheBoard() {
        assertThat(new Offset(-1, 2).applyTo(Square.of("a1"))).isEmpty();
        assertThat(new Offset(0, 1).applyTo(Square.of("e8"))).isEmpty();
    }

    @Test
    void queenDirectionsAreTheRookAndBishopDirections() {
        assertThat(Offset.QUEEN_DIRECTIONS)
                .hasSize(8)
                .containsAll(Offset.ROOK_DIRECTIONS)
                .containsAll(Offset.BISHOP_DIRECTIONS);
    }
}
