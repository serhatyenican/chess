package org.srt.chess.game;

import org.srt.chess.engine.Color;
import org.srt.chess.engine.Fen;
import org.srt.chess.engine.Move;
import org.srt.chess.engine.Position;

import java.util.List;
import java.util.UUID;

/**
 * A game as every game endpoint returns it.
 * <p>
 * The moves played so far are stored, but not part of the response: the board draws the position from the FEN, and
 * nothing in v1 shows the history. A feature that needs them, such as highlighting the last move, adds a field.
 *
 * @param sideToMove whose turn it is, read from the FEN
 * @param check      whether the side to move is in check
 * @param winner     the winner of a checkmate, otherwise {@code null}
 * @param legalMoves the moves the side to move can play in UCI notation, e.g. {@code "e7e8q"}, so the board needs no
 *                   copy of the rules; empty once the game has ended
 */
public record GameResponse(
        UUID id,
        String fen,
        Color sideToMove,
        boolean check,
        GameStatus status,
        Color winner,
        List<String> legalMoves) {

    static GameResponse of(Game game) {
        Position position = Fen.parse(game.fen());
        List<String> legalMoves = game.status() == GameStatus.IN_PROGRESS
                ? position.legalMoves().stream().map(Move::toString).toList()
                : List.of();
        return new GameResponse(
                game.id(),
                game.fen(),
                position.sideToMove(),
                position.isInCheck(),
                game.status(),
                game.winner().orElse(null),
                legalMoves);
    }
}
