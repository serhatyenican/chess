# 0005. PostgreSQL with JPA and Flyway, games and moves tables

- Status: Accepted
- Date: 2026-11-22

## Context
Games must survive closing the browser and restarting the server, and every move must be stored.

## Options
- Database: **PostgreSQL** (free, robust, already running locally via docker-compose) vs. H2 or an in-memory store.
- Access: **Spring Data JPA** (industry standard, maps Java classes to tables) vs. JdbcClient or jOOQ (more control,
  more code).
- Schema changes: **Flyway** (versioned SQL files in git) vs. Hibernate `ddl-auto: update` (automatic, but
  unreviewable and unsafe for renames and drops).

## Decision
PostgreSQL 17, Spring Data JPA, and Flyway, with Hibernate in `ddl-auto: validate` mode.

Data model for v1:
- `games`: id, status (in progress, checkmate, stalemate, left), side to move, current position as FEN
- `moves`: game id, move number, from square, to square, promotion piece

## Consequences
- A game loads instantly from its FEN, and the `moves` table holds the full history.
- Every schema change is a reviewed `V<n>__description.sql` file. Merged migrations are never edited.
