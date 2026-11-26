package org.srt.chess.game;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.srt.chess.engine.Color;
import org.srt.chess.engine.Fen;
import org.srt.chess.engine.GameResult;
import org.srt.chess.engine.Move;
import org.srt.chess.engine.Position;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A stored game: its current position as FEN, its status and winner, and the moves played so far (ADR 0011).
 * <p>
 * The id is a random UUID set when the game is created, so it is known before saving and the game's link can't be
 * guessed (ADR 0010). {@code version} is for optimistic locking: saving a game that another request changed in the
 * meantime fails instead of overwriting that change.
 */
@Entity
@Table(name = "games")
public class Game {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    private Color winner;

    private String fen;

    // A Long, not a long: since the id is set in Java, Spring Data sees from a null version that a game is new
    // and inserts it, instead of first looking it up (ADR 0011).
    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @OrderBy("ply")
    private List<GameMove> moves = new ArrayList<>();

    protected Game() {
        // for JPA
    }

    private Game(UUID id, String fen) {
        this.id = id;
        this.status = GameStatus.IN_PROGRESS;
        this.fen = fen;
    }

    /**
     * Returns a new, not yet saved game in the starting position.
     */
    public static Game start() {
        return new Game(UUID.randomUUID(), Fen.STARTING_POSITION);
    }

    /**
     * Plays a move: checks that the game is in progress and that the move is legal, then updates the position, adds
     * the move to the history, and sets the status and winner from the new position. Either all of this happens or,
     * if an exception is thrown, nothing changes.
     *
     * @throws GameOverException    if the game has already ended
     * @throws IllegalMoveException if the move is not legal in the current position, e.g. a promotion without the
     *                              piece
     */
    void play(Move move) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new GameOverException(id, status);
        }
        Position position = Fen.parse(fen);
        if (!position.legalMoves().contains(move)) {
            throw new IllegalMoveException(move);
        }
        Position next = position.play(move);
        GameResult result = GameResult.of(next);
        fen = Fen.toFen(next);
        addMove(move);
        status = GameStatus.from(result.state());
        winner = result.winner().orElse(null);
    }

    /**
     * Ends the game because a player left it. There is no winner, and the game is kept, so its link still shows it.
     *
     * @throws GameOverException if the game has already ended
     */
    void leave() {
        if (status != GameStatus.IN_PROGRESS) {
            throw new GameOverException(id, status);
        }
        status = GameStatus.LEFT;
    }

    /**
     * Adds a move to the game's history, numbered with the next ply. It only records the move, without checking it
     * or changing the position: {@link #play(Move)} is the way to play one. Tests use it to set up a history.
     * <p>
     * Adding a move alone doesn't increase {@code version}: the link to the game is stored in the {@code moves}
     * table, so Hibernate sees no change to the game's own row. Playing a move also changes the FEN, which does.
     */
    void addMove(Move move) {
        moves.add(new GameMove(this, moves.size() + 1, move));
    }

    public UUID id() {
        return id;
    }

    public GameStatus status() {
        return status;
    }

    /**
     * Returns the winner, which only a checkmate has.
     */
    public Optional<Color> winner() {
        return Optional.ofNullable(winner);
    }

    public String fen() {
        return fen;
    }

    /**
     * Returns the moves in the order they were played, as an unmodifiable list.
     */
    public List<GameMove> moves() {
        return List.copyOf(moves);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
