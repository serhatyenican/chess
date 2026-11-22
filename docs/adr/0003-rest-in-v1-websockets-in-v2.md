# 0003. REST only in v1, WebSockets (STOMP) in v2

- Status: Accepted
- Date: 2026-11-22

## Context
Version 1 is a solo game: one person moves both sides. The browser sends a move, and the server validates it and
returns the new position. Version 2 adds two-player games, where a move must appear on the opponent's screen within
about a second.

## Options
1. **REST only**: plain request and response, easy to build, test and debug. It can't push updates to another browser.
2. **WebSockets with STOMP from the start**: ready for v2, but adds a messaging layer that v1 doesn't use.
3. **REST in v1, then add STOMP over WebSockets in v2**: v1 stays small, and v2 gets a clear learning topic.

## Decision
Option 3. v1 exposes a REST API for the game lifecycle and moves. In v2, clients subscribe to `/topic/games/{id}`
over STOMP, and the server broadcasts each validated move there. REST stays for create, join and state.

## Consequences
- The game logic must stay in services that both a REST controller and, later, a STOMP handler can call.
- Polling and Server-Sent Events were considered for v2 and rejected: polling adds delay, and SSE is one-way only.
