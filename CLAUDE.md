# Chess App

## About this project
A learning project by Serhat: an online chess web application where two players
play each other from different devices (desktop or mobile browser).
Not commercial. Priority is learning by doing: modern development practices and
AI-assisted development workflows. Explain key decisions and trade-offs as you work.

## Tech stack
- Backend: Java 25, Spring Boot 4, Gradle (Groovy DSL)
- Database: PostgreSQL (local via docker-compose)
- Frontend: React (planned)
- Real-time: WebSockets, likely Spring with STOMP (to be confirmed)
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

## Roadmap
Being refined: requirements, milestones and tasks are tracked on GitHub.

## Working conventions
- Small, focused changes with tests.
- Ask before big design decisions; propose options with trade-offs.
- Feature branches and pull requests into `main`, reviewed before merging.
- Never commit secrets. Never put passwords in `application.yaml` or `docker-compose.yml`.
- Keep this file up to date with conventions and decisions.
