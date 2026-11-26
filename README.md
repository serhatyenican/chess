# Chess

An online chess web app where two players play each other from different devices
(desktop or mobile browser). A learning project focused on modern development
practices and AI-assisted workflows.

> Status: early setup. See the GitHub milestones for the roadmap.

## Tech stack

- Java 25, Spring Boot 4, Gradle
- PostgreSQL (via Docker Compose for local development)
- React, Vite and TypeScript frontend in `frontend/`

## Run locally

Requirements: JDK 25, Docker and Node.js (the LTS version in `frontend/.nvmrc`).

```bash
cp .env.example .env          # then set your own password in .env
docker compose up -d          # starts Postgres
./gradlew bootRun             # starts the app on http://localhost:8080
```

The API documentation is at http://localhost:8080/swagger-ui.html, where you can also try the calls.

Run the tests with `./gradlew test`. Tests start their own throwaway Postgres with
[Testcontainers](https://testcontainers.com), so they only need Docker running, not the `.env` database.

Alternatively, `./gradlew bootTestRun` starts the app against a Testcontainers Postgres,
without docker-compose.

Start the frontend in a second terminal, with Spring running:

```bash
cd frontend
npm ci                        # installs the exact versions from package-lock.json
npm run dev                   # serves the app on http://localhost:5173
```

The dev server forwards `/api` requests to Spring, so open http://localhost:5173 in the browser.
In `frontend/`, `npm test` runs the frontend tests and `npm run lint` the linter.

## License

[MIT](LICENSE)
