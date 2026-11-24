# Chess App

## About this project
A learning project by Serhat: an online chess web application where two players
play each other from different devices (desktop or mobile browser).
Not commercial. Priority is learning by doing: modern development practices and
AI-assisted development workflows. Explain key decisions and trade-offs as you work.

## Tech stack
See `docs/adr/` for the reasoning behind each choice.
- Backend: Java 25, Spring Boot 4 (Spring MVC with virtual threads), Gradle (Groovy DSL)
- API: REST in v1; WebSockets with STOMP added in v2 for two-player games
- Chess engine: our own, pure Java, in `org.srt.chess.engine` (no Spring imports)
- Database: PostgreSQL, Spring Data JPA, Flyway migrations (`ddl-auto: validate`); local DB via docker-compose
- Frontend: React + Vite + TypeScript with react-chessboard, in `frontend/`
- Tests: JUnit 5 + AssertJ (engine), Testcontainers (Spring/DB), Vitest + React Testing Library (frontend)
- Planning and CI: GitHub Issues and milestones, GitHub Projects board, GitHub Actions
- Base package: `org.srt.chess`

## Key decisions
- Game types, in build order: solo (v1), two players via a shared link (v2), against a bot (v3).
- No user accounts in the first version: games are joined through a shareable link.
  Accounts and login come in a later phase.
- The server is the authority on chess rules and validates every move.
- REST endpoints handle the game lifecycle and moves; from v2, WebSockets push moves to the opponent.
- Work is tracked as GitHub issues, one per task, grouped into a milestone per phase.
- Configuration and secrets come from environment variables. Locally they live in a
  git-ignored `.env` file (template: `.env.example`), read by both docker-compose and Spring Boot.

## Architecture decisions
Read `docs/adr/` before changing the stack or structure. Record new significant decisions there as a new ADR.

## Roadmap
Version 1: a solo game (one person moves both sides) in a desktop browser, running locally. Every move is stored,
games can be resumed via their link, and "Leave game" ends a game. No accounts, no time limit, no mobile layout yet.
Milestones: v1 · M1 Foundation, v1 · M2 Chess rules, v1 · M3 Game API and storage, v1 · M4 Board in the browser.
Later versions (planned as labelled issues, not detailed yet): v2 two players via link, v3 bot, then timed games,
accounts and friends, and mobile.

## Working conventions
- Small, focused changes with tests.
- Ask before big design decisions; propose options with trade-offs.
- One GitHub issue per task. Branch name `<issue-number>-short-name`, PR description says `Closes #<n>`.
- Commit messages are one short sentence; explanations go in the PR description.
- Feature branches and pull requests into `main`, reviewed before merging. CI must be green.
- Prefer the Java Stream API over for-each loops on collections and arrays (filter, map, find, collect).
  Index-based `for` loops (e.g. over ranks and files) are fine, and so is a for-each loop that carries state
  between iterations.
- Validate untrusted input where it enters the system (the REST API from M3). Inside the engine, only check what
  catches our own mistakes; no hardening against exotic input.
- Tests that need the database use Testcontainers (`@Import(TestcontainersConfiguration.class)`); engine tests are plain JUnit.
- Tests that take more than a few seconds (such as deep perft counts) get `@Tag("slow")`. `./gradlew test` and CI
  leave them out; run them with `./gradlew slowTest`, for example before and after refactoring the engine.
- Schema changes are new Flyway migrations `V<n>__description.sql` in `src/main/resources/db/migration`; never edit a merged one.
- Never commit secrets. Never put passwords in `application.yaml` or `docker-compose.yml`.
- Keep this file up to date with conventions and decisions.
