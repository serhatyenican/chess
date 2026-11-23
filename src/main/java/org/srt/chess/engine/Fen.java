package org.srt.chess.engine;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads and writes positions in Forsyth-Edwards Notation, e.g. the starting position
 * {@code rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1}.
 * <p>
 * A FEN has six fields, separated by single spaces:
 * <ol>
 *   <li><b>Pieces</b>: the ranks from 8 down to 1, separated by {@code /}. Each rank lists its squares from the a- to
 *       the h-file: a piece letter ({@code K Q R B N P} white, {@code k q r b n p} black) or a digit 1-8 for that
 *       many empty squares. {@code 4P3} is four empty squares, a white pawn on e, then three empty squares.</li>
 *   <li><b>Side to move</b>: {@code w} or {@code b}.</li>
 *   <li><b>Castling rights</b>: {@code K}/{@code Q} for white king-/queenside, {@code k}/{@code q} for black, in
 *       that order, or {@code -} for none.</li>
 *   <li><b>En passant square</b>: the square behind a pawn that just made a double step, e.g. {@code e3} after
 *       {@code 1. e4}, or {@code -}.</li>
 *   <li><b>Halfmove clock</b>: half-moves since the last capture or pawn move, for the fifty-move rule.</li>
 *   <li><b>Fullmove number</b>: starts at 1 and increases after each black move.</li>
 * </ol>
 * Parsing is strict: exactly six fields separated by single spaces, and castling rights in {@code KQkq} order.
 *
 * @see <a href="https://www.chessprogramming.org/Forsyth-Edwards_Notation">Forsyth-Edwards Notation</a>
 */
public final class Fen {

    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private Fen() {
    }

    /**
     * Parses a FEN string.
     *
     * @throws IllegalArgumentException if the text is not valid FEN; the message says what is wrong
     */
    public static Position parse(String fen) {
        Objects.requireNonNull(fen, "fen must not be null");
        try {
            String[] fields = fen.split(" ", -1);
            if (fields.length != 6) {
                throw new IllegalArgumentException(
                        "expected 6 fields separated by single spaces, found " + fields.length);
            }
            return new Position(
                    parseBoard(fields[0]),
                    parseSideToMove(fields[1]),
                    parseCastlingRights(fields[2]),
                    parseEnPassant(fields[3]),
                    parseCount(fields[4], "halfmove clock"),
                    parseCount(fields[5], "fullmove number"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid FEN \"" + fen + "\": " + e.getMessage(), e);
        }
    }

    /**
     * Writes a position as FEN.
     */
    public static String toFen(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        return writeBoard(position.board())
                + ' ' + position.sideToMove().letter()
                + ' ' + writeCastlingRights(position.castlingRights())
                + ' ' + (position.enPassant() == null ? "-" : position.enPassant())
                + ' ' + position.halfmoveClock()
                + ' ' + position.fullmoveNumber();
    }

    private static Board parseBoard(String placement) {
        String[] ranks = placement.split("/", -1);
        if (ranks.length != 8) {
            throw new IllegalArgumentException("expected 8 ranks separated by '/', found " + ranks.length);
        }
        Board board = Board.empty();
        for (int i = 0; i < 8; i++) {
            int rank = 7 - i; // FEN lists rank 8 first
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                if (c >= '1' && c <= '8') {
                    file += c - '0';
                } else {
                    if (file < 8) {
                        board = board.with(new Square(File.values()[file], rank), Piece.fromLetter(c));
                    }
                    file++;
                }
                if (file > 8) {
                    throw new IllegalArgumentException("rank " + (rank + 1) + " has more than 8 squares");
                }
            }
            if (file != 8) {
                throw new IllegalArgumentException("rank " + (rank + 1) + " has " + file + " squares, expected 8");
            }
        }
        return board;
    }

    private static Color parseSideToMove(String text) {
        if (text.length() != 1) {
            throw new IllegalArgumentException("side to move must be a single letter, was \"" + text + "\"");
        }
        return Color.fromLetter(text.charAt(0));
    }

    private static Set<CastlingRight> parseCastlingRights(String text) {
        if (text.equals("-")) {
            return Set.of();
        }
        Set<CastlingRight> rights = text.chars()
                .mapToObj(c -> CastlingRight.fromLetter((char) c))
                .collect(Collectors.toSet());
        if (!writeCastlingRights(rights).equals(text)) {
            throw new IllegalArgumentException(
                    "castling rights must be in KQkq order without repeats, was \"" + text + "\"");
        }
        return rights;
    }

    private static Square parseEnPassant(String text) {
        return text.equals("-") ? null : Square.of(text);
    }

    private static int parseCount(String text, String name) {
        // \d only matches ASCII digits; Integer.parseInt alone would also accept "+1" and non-ASCII digits.
        if (!text.matches("\\d{1,9}")) {
            throw new IllegalArgumentException(name + " must be a non-negative whole number, was \"" + text + "\"");
        }
        return Integer.parseInt(text);
    }

    private static String writeBoard(Board board) {
        StringBuilder fen = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (File file : File.values()) {
                var piece = board.pieceAt(new Square(file, rank));
                if (piece.isEmpty()) {
                    emptySquares++;
                    continue;
                }
                if (emptySquares > 0) {
                    fen.append(emptySquares);
                    emptySquares = 0;
                }
                fen.append(piece.get().letter());
            }
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            if (rank > 0) {
                fen.append('/');
            }
        }
        return fen.toString();
    }

    private static String writeCastlingRights(Set<CastlingRight> rights) {
        String letters = Arrays.stream(CastlingRight.values()) // declared in KQkq order
                .filter(rights::contains)
                .map(right -> String.valueOf(right.letter()))
                .collect(Collectors.joining());
        return letters.isEmpty() ? "-" : letters;
    }
}
