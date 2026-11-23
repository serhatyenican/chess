package org.srt.chess.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FenTests {

    private static final String START = Fen.STARTING_POSITION;

    @Test
    void parsesTheStartingPosition() {
        Position position = Fen.parse(START);

        assertThat(position.board()).isEqualTo(startingBoard());
        assertThat(position.sideToMove()).isEqualTo(Color.WHITE);
        assertThat(position.castlingRights()).containsExactlyInAnyOrder(CastlingRight.values());
        assertThat(position.enPassant()).isNull();
        assertThat(position.halfmoveClock()).isZero();
        assertThat(position.fullmoveNumber()).isEqualTo(1);
    }

    @Test
    void startingPositionMatchesTheStartingFen() {
        assertThat(Position.starting()).isEqualTo(Fen.parse(START));
        assertThat(Fen.toFen(Position.starting())).isEqualTo(START);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            START,
            // after 1. e4
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
            // white can capture en passant on f6
            "rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3",
            // no castling rights, non-zero halfmove clock
            "8/8/8/4k3/8/8/8/4K3 w - - 37 80",
            // only some castling rights
            "r3k2r/8/8/8/8/8/8/R3K2R b Kq - 4 20",
            // "Kiwipete", a well-known perft test position
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
    })
    void writingAParsedFenGivesTheSameText(String fen) {
        assertThat(Fen.toFen(Fen.parse(fen))).isEqualTo(fen);
    }

    @ParameterizedTest
    @MethodSource("invalidFens")
    void rejectsInvalidFen(String fen, String reason) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Fen.parse(fen))
                .withMessage("Invalid FEN \"" + fen + "\": " + reason);
    }

    static Stream<Arguments> invalidFens() {
        String pieces = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        return Stream.of(
                arguments("", "expected 6 fields separated by single spaces, found 1"),
                arguments(pieces + " w KQkq -", "expected 6 fields separated by single spaces, found 4"),
                arguments(pieces + " w  KQkq - 0 1", "expected 6 fields separated by single spaces, found 7"),
                arguments("rnbqkbnr/pppppppp/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        "expected 8 ranks separated by '/', found 7"),
                arguments("rnbqkbnr/ppppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        "rank 7 has more than 8 squares"),
                arguments("rnbqkbnr/pppppppp/8/8/44P/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        "rank 4 has more than 8 squares"),
                arguments("rnbqkbn/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        "rank 8 has 7 squares, expected 8"),
                arguments("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBXKBNR w KQkq - 0 1",
                        "unknown piece letter 'X', expected one of KQRBNPkqrbnp"),
                arguments(pieces + " x KQkq - 0 1", "unknown color letter 'x', expected 'w' or 'b'"),
                arguments(pieces + " W KQkq - 0 1", "unknown color letter 'W', expected 'w' or 'b'"),
                arguments(pieces + " wb KQkq - 0 1", "side to move must be a single letter, was \"wb\""),
                arguments(pieces + " w KQxq - 0 1", "unknown castling right letter 'x', expected one of KQkq"),
                arguments(pieces + " w qkQK - 0 1",
                        "castling rights must be in KQkq order without repeats, was \"qkQK\""),
                arguments(pieces + " w KK - 0 1",
                        "castling rights must be in KQkq order without repeats, was \"KK\""),
                arguments(pieces + " w KQkq e9 0 1",
                        "Invalid square \"e9\": expected a file a-h and a rank 1-8, e.g. \"e4\""),
                arguments(pieces + " w KQkq e3 0 1",
                        "en passant square must be on rank 6 when white is to move, was e3"),
                arguments(pieces + " w KQkq - -1 1", "halfmove clock must be a non-negative whole number, was \"-1\""),
                arguments(pieces + " w KQkq - 0 one",
                        "fullmove number must be a non-negative whole number, was \"one\""),
                arguments(pieces + " w KQkq - 0 0", "fullmoveNumber must be at least 1, was 0"));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> Fen.parse(null)).withMessage("fen must not be null");
        assertThatNullPointerException().isThrownBy(() -> Fen.toFen(null)).withMessage("position must not be null");
    }

    private static Board startingBoard() {
        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        Board board = Board.empty();
        for (File file : File.values()) {
            board = board
                    .with(new Square(file, 0), new Piece(Color.WHITE, backRank[file.ordinal()]))
                    .with(new Square(file, 1), new Piece(Color.WHITE, PieceType.PAWN))
                    .with(new Square(file, 6), new Piece(Color.BLACK, PieceType.PAWN))
                    .with(new Square(file, 7), new Piece(Color.BLACK, backRank[file.ordinal()]));
        }
        return board;
    }
}
