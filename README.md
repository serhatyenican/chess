# Chess

An online chess web app where two players play each other from different devices
(desktop or mobile browser). A learning project focused on modern development
practices and AI-assisted workflows.

> Status: v1, a solo game in a desktop browser, running locally. Next: v2, two players via a shared link.
> See the GitHub milestones for the roadmap.

## What v1 can do

One person plays both sides on one board. The server checks every move.

- **New game** starts a game. Its URL is the game's link: opening it again, also after closing the browser, shows
  the game where it was.
- Move by dragging a piece, or by clicking it and then one of the marked squares. A pawn reaching the last rank asks
  for the piece it becomes.
- The page shows whose turn it is, check, the captured pieces with each side's lead in points, and how the game
  ended: checkmate, stalemate, or left.
- **Leave game** ends a game in progress. Its link still shows the final position.

No accounts, no time limit and no mobile layout yet.

## Tech stack

- Backend: Java 25, Spring Boot 4 (Spring MVC on virtual threads), Gradle, our own chess engine in pure Java
- Database: PostgreSQL with Flyway, via Docker Compose for local development
- API: REST, documented with OpenAPI and Swagger UI (springdoc)
- Frontend: React, Vite and TypeScript in `frontend/`, with React Router and react-chessboard; its API types are
  generated from the OpenAPI spec
- Tests: JUnit 5, AssertJ and Testcontainers; Vitest and React Testing Library

The reasons behind each choice are in [`docs/adr/`](docs/adr/).

## Run locally

Requirements: JDK 25, Docker, and Node.js in the LTS version from `frontend/.nvmrc`
(a version manager such as [fnm](https://github.com/Schniz/fnm) or nvm picks it up from that file).

1. Configure the database and start it:

   ```bash
   cp .env.example .env          # then set your own password in .env
   docker compose up -d          # starts Postgres
   ```

2. Start the backend, in one terminal:

   ```bash
   ./gradlew bootRun             # the API on http://localhost:8080
   ```

3. Start the frontend, in a second terminal:

   ```bash
   cd frontend
   npm ci                        # installs the exact versions from package-lock.json
   npm run dev                   # serves the app on http://localhost:5173
   ```

4. Open http://localhost:5173 and click **New game**. The frontend's dev server forwards `/api` requests to Spring,
   so the browser only talks to port 5173.

Without Docker Compose, `./gradlew bootTestRun` starts the backend against a throwaway Testcontainers Postgres
instead of step 1 and 2 (Docker must still be running; games are lost when it stops).

The API documentation is at http://localhost:8080/swagger-ui.html, where you can also try the calls.

## Tests

- Backend: `./gradlew test`. The tests start their own throwaway Postgres with
  [Testcontainers](https://testcontainers.com), so they only need Docker running, not the `.env` database.
  Slow tests, such as deep perft counts, run with `./gradlew slowTest`.
- Frontend, in `frontend/`: `npm test` for the tests, `npm run lint` for the linter, and `npm run build` for the
  type check and the production build.

## License

[MIT](LICENSE)
