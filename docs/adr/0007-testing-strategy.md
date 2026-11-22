# 0007. Testing: JUnit + AssertJ, Testcontainers, Vitest

- Status: Accepted
- Date: 2026-11-22

## Context
CI must run every test on each pull request. Engine tests should be fast, and database tests should be realistic.

## Options
- Engine: **JUnit 5 + AssertJ**, with no Spring and no database, so the tests run in seconds.
- Database and API tests: **Testcontainers** (a real Postgres in Docker, the same everywhere) vs. H2 (faster, but
  not Postgres, so dialect bugs slip through) vs. a CI service container (real Postgres, but only in CI).
- Frontend: **Vitest + React Testing Library** (the standard pair for Vite apps). Browser end-to-end tests
  (Playwright) were deferred to a later version.

## Decision
JUnit 5 + AssertJ for the engine, Testcontainers with `postgres:17-alpine` for Spring tests, and Vitest + React
Testing Library for a few key frontend tests in v1. GitHub Actions runs all of them on every PR, and `main` requires
a green build.

## Consequences
- Running the Spring tests needs Docker, both locally and in CI (GitHub's Ubuntu runners have it).
- Frontend coverage in v1 is intentionally light.
