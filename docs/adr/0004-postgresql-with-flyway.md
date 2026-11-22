# 0004. PostgreSQL with Flyway migrations

- Status: Accepted
- Date: 2026-11-22

## Context
Games and moves must survive a server restart. The schema will change often while we learn.

## Options
- Database: **PostgreSQL** (robust, free, available on every host, already set up locally) vs. H2 or an in-memory
  store (no persistence) vs. a document DB (less useful for learning SQL and JPA).
- Schema changes: **Hibernate `ddl-auto: update`** (automatic, but it can't rename or drop safely, and changes aren't
  reviewed) vs. **Flyway** (versioned SQL files in git, applied in order) vs. Liquibase (similar, but XML/YAML-centric
  and heavier).

## Decision
PostgreSQL 17 with Flyway. Hibernate runs with `ddl-auto: validate`.

## Consequences
- Every schema change is a reviewed `V<n>__description.sql` file. Merged migrations are never edited.
- Production and tests use the same migrations, which ADR 0005 makes possible.
