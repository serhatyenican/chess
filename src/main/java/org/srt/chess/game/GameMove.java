package org.srt.chess.game;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.srt.chess.engine.Move;

import java.time.Instant;

/**
 * One move of a stored game, with its ply: 1 for white's first move, 2 for black's reply, and so on. The move is
 * stored as UCI text, e.g. {@code "e7e8q"} (ADR 0011).
 * <p>
 * Named {@code GameMove} so it doesn't clash with the engine's {@link Move}, which it stores.
 */
@Entity
@Table(name = "moves")
public class GameMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    private int ply;

    private String move;

    @CreationTimestamp
    private Instant playedAt;

    protected GameMove() {
        // for JPA
    }

    GameMove(Game game, int ply, Move move) {
        this.game = game;
        this.ply = ply;
        this.move = move.toString();
    }

    public int ply() {
        return ply;
    }

    public Move move() {
        return Move.of(move);
    }

    public Instant playedAt() {
        return playedAt;
    }
}
