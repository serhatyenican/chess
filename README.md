# Chess

An online chess web app where two players play each other from different devices
(desktop or mobile browser). A learning project focused on modern development
practices and AI-assisted workflows.

> Status: early setup. See the GitHub milestones for the roadmap.

## Tech stack

- Java 25, Spring Boot 4, Gradle
- PostgreSQL (via Docker Compose for local development)
- React frontend (planned)

## Run locally

Requirements: JDK 25 and Docker.

```bash
cp .env.example .env          # then set your own password in .env
docker compose up -d          # starts Postgres
./gradlew bootRun             # starts the app on http://localhost:8080
```

Run the tests with `./gradlew test`. Tests start their own throwaway Postgres with
[Testcontainers](https://testcontainers.com), so they only need Docker running, not the `.env` database.

Alternatively, `./gradlew bootTestRun` starts the app against a Testcontainers Postgres,
without docker-compose.

## License

[MIT](LICENSE)
