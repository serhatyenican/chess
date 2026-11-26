# Architecture Decision Records

Each file records one decision: the context, the options we considered, what we chose, and what follows from it.
ADRs are never rewritten after they are accepted. If a decision changes, add a new ADR that supersedes the old one,
and mark the old one as superseded.

To start a new one, copy [`template.md`](template.md) and give it the next number.

| ADR | Decision | Status |
| --- | --- | --- |
| [0001](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](0002-spring-mvc-with-virtual-threads.md) | Spring MVC with virtual threads, not WebFlux | Accepted |
| [0003](0003-rest-in-v1-websockets-in-v2.md) | REST only in v1, WebSockets (STOMP) in v2 | Accepted |
| [0004](0004-own-chess-engine.md) | Write our own chess engine | Accepted |
| [0005](0005-postgresql-jpa-flyway.md) | PostgreSQL with JPA and Flyway, games and moves tables | Accepted, data model replaced by [0011](0011-store-moves-as-uci-text.md) |
| [0006](0006-react-vite-typescript-frontend.md) | React, Vite, TypeScript and react-chessboard, in the same repo | Accepted |
| [0007](0007-testing-strategy.md) | Testing: JUnit + AssertJ, Testcontainers, Vitest | Accepted |
| [0008](0008-configuration-via-environment.md) | Configuration and secrets via environment variables | Accepted |
| [0009](0009-single-module-pure-engine-package.md) | Single Gradle module, engine package without Spring | Accepted |
| [0010](0010-rest-api-design.md) | REST API around resources: plural names, nested moves, PATCH for state changes | Accepted |
| [0011](0011-store-moves-as-uci-text.md) | Position as FEN, moves as UCI text, optimistic locking on games | Accepted |
