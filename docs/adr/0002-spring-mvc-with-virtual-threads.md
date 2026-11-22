# 0002. Spring MVC with virtual threads, not WebFlux

- Status: Accepted
- Date: 2026-11-22

## Context
The backend serves a REST API and WebSocket connections for two-player games. Load is small: each game has two
players making a move every few seconds. Spring offers two web stacks.

## Options
1. **Spring MVC (servlet, blocking) with Java 25 virtual threads**: simple, imperative code that is easy to read and
   debug. Works naturally with JPA. Virtual threads make blocking cheap, so one server handles many concurrent connections.
2. **Spring WebFlux (reactive)**: non-blocking from end to end, so it scales for very high connection counts.
   But every line becomes `Mono`/`Flux` chains, stack traces are harder to read, and JPA can't be used (it needs R2DBC).

## Decision
Spring MVC with virtual threads enabled (`spring.threads.virtual.enabled=true`).

## Consequences
- Plain Java code, standard JPA, a gentler learning curve.
- Reactive programming stays out of scope. It's worth learning later, in its own project.
