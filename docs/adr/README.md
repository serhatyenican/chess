# Architecture Decision Records

Each file records one decision: the context, the options we considered, what we chose, and what follows from it.
ADRs are never rewritten after they are accepted. If a decision changes, add a new ADR that supersedes the old one,
and mark the old one as superseded.

Copy [`template.md`](template.md) to start a new one, numbered in order.

| ADR | Decision | Status |
| --- | --- | --- |
| [0001](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](0002-spring-mvc-with-virtual-threads.md) | Spring MVC with virtual threads, not WebFlux | Accepted |
| [0003](0003-websockets-with-stomp.md) | WebSockets with STOMP, one topic per game | Accepted |
| [0004](0004-postgresql-with-flyway.md) | PostgreSQL with Flyway migrations | Accepted |
| [0005](0005-testcontainers-for-integration-tests.md) | Testcontainers for integration tests | Accepted |
| [0006](0006-react-vite-typescript-frontend.md) | React with Vite and TypeScript | Accepted |
| [0007](0007-configuration-via-environment.md) | Configuration and secrets via environment variables | Accepted |
| [0008](0008-single-module-pure-engine-package.md) | Single Gradle module, engine package without Spring | Accepted |
