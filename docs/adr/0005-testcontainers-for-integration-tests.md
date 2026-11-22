# 0005. Testcontainers for integration tests

- Status: Accepted
- Date: 2026-11-22

## Context
Tests that start Spring need a database, both locally and in GitHub Actions.

## Options
1. **H2 in-memory**: fast, no Docker, but it isn't Postgres, so SQL dialect and behaviour differ and bugs slip through.
2. **A Postgres service container in the CI workflow**: real Postgres in CI, but local runs need a separate setup.
3. **Testcontainers**: tests start a real, throwaway Postgres in Docker, the same way everywhere. Spring Boot's
   `@ServiceConnection` wires it up without any config.

## Decision
Testcontainers with `postgres:17-alpine`, the same image as docker-compose.

## Consequences
- Running the tests requires Docker, both locally and in CI (GitHub's Ubuntu runners have it).
- The first run downloads the image, so it's slower; after that it's cached.
- Pure engine tests don't use Spring or Docker and stay fast (ADR 0008).
