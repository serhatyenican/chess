# Chess App

## About this project
A learning project by Serhat: an online chess web application where two players
play each other from different devices (desktop or mobile browser).
Not commercial. Priority is learning by doing: modern development practices and
AI-assisted development workflows. Explain key decisions and trade-offs as you work.

## Tech stack
- Backend: Java 25, Spring Boot 4, Gradle (Groovy DSL)
- Database: PostgreSQL (local via docker-compose)
- Frontend: React + Vite + TypeScript (planned, ADR 0006)
- Real-time: WebSockets with STOMP (ADR 0003)
- Version control and planning: GitHub, GitHub Issues, GitHub Projects board, GitHub Actions CI
- Later: Docker and cloud deployment
- Base package: `org.srt.chess`

## Key decisions
- First game type: online player vs player. Other game types and variants come later.
- No user accounts in the first version: games are joined through a shareable link.
  Accounts and login come in a later phase.
- The server is the authority on chess rules and validates every move.
- REST endpoints handle the game lifecycle; WebSockets handle live moves.
- Work is tracked as GitHub issues, one per task, grouped into a milestone per phase.
- Configuration and secrets come from environment variables. Locally they live in a
  git-ignored `.env` file (template: `.env.example`), read by both docker-compose and Spring Boot.

## Architecture decisions
Read `docs/adr/` before changing the stack or structure. Record new significant decisions there as a new ADR.

## Roadmap
Milestones M1 to M7 and their issues are tracked on GitHub:
M1 Foundation, M2 Engine basics, M3 Walking skeleton (first playable game), M4 Full rules,
M5 Game flow and mobile UX, M6 Ship it, M7 Extras.

## Working conventions
- Small, focused changes with tests.
- Ask before big design decisions; propose options with trade-offs.
- One GitHub issue per task. Branch name `<issue-number>-short-name`, PR description says `Closes #<n>`.
- Feature branches and pull requests into `main`, reviewed before merging. CI must be green.
- Tests that need the database use Testcontainers (`@Import(TestcontainersConfiguration.class)`); engine tests are plain JUnit.
- Never commit secrets. Never put passwords in `application.yaml` or `docker-compose.yml`.
- Keep this file up to date with conventions and decisions.
