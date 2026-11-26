# 0011. Store the position as FEN and each move as UCI text

- Status: Accepted
- Date: 2026-11-26

## Context
ADR 0005 chose PostgreSQL, JPA and Flyway, and sketched a data model: `games` with the side to move and the position
as FEN, and `moves` with the from square, the to square and the promotion piece in separate columns. Before writing
the first migration (#59) we looked at that sketch again. Since then the engine has gained `Move.of(String)` and
`Move.toString()`, which read and write a move as UCI text such as `e2e4` or `e7e8q` (#58).

## Options
For a move:
1. **Separate columns** (`from_square`, `to_square`, `promotion`): the database can answer questions like "all moves
   to e4", but every read and write maps three columns, and no v1 feature needs such a query.
2. **One UCI text column** (`move`): the same text the API receives and the engine writes, converted by
   `Move.of` and `Move.toString()`. Questions about squares would need string functions in SQL.

For the side to move:
1. **Its own column**: easy to see in the table, but it repeats the FEN, which already holds it, so the two can
   disagree.
2. **Only in the FEN**: one source of truth. The engine reads it from the position.

## Decision
- `games` stores the current position as FEN, with no side-to-move column.
- `moves` stores each move as UCI text in a `move` column, with its `ply` (1, 2, 3, … in the order played), unique per
  game. The game numbers the plies when a move is added, so callers can't get them wrong.
- The database checks what it can cheaply: known `status` and `winner` values, a winner exactly when the status is
  `CHECKMATE`, and a positive ply.
- `games.version` is a JPA `@Version` column for optimistic locking, so two requests changing the same game at once
  (for example the same game open in two tabs) can't both win. It is a `Long`, not a `long`: the game id is set in
  Java, so Spring Data uses a `null` version to see that a game is new.

This replaces the data model in ADR 0005. The rest of ADR 0005 stands.

## Consequences
- Reading a game needs no replay: the FEN is the position, and the moves are its history.
- The FEN and the moves must stay consistent. Only the code that plays a move (#61) changes both, in one transaction.
- Adding a move alone doesn't increase the game's version, because the link to the game is stored in `moves`, not in
  `games`. Playing a move also changes the FEN, which does. Two requests adding the same move at once are also stopped
  by the unique `(game_id, ply)`.
- A new status value needs a migration that changes `games_status_known`.
- Queries about squares, for example for statistics later, need SQL string functions or a later migration that adds
  columns.
