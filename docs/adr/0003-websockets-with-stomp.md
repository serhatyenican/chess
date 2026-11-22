# 0003. WebSockets with STOMP, one topic per game

- Status: Accepted
- Date: 2026-11-22

## Context
When a player moves, the opponent must see it within about a second, and both browsers need to learn about draw
offers, resignations and the end of the game.

## Options
1. **Polling** (`GET /games/{id}` every second): trivial, but adds delay and wasted requests.
2. **Server-Sent Events**: simple server-to-client push over HTTP, but one-way, so moves still go over REST.
3. **Raw WebSockets**: two-way and fast, but we'd have to invent our own message format and routing.
4. **WebSockets with STOMP** (Spring's messaging support): two-way, and adds destinations, subscriptions and
   message routing out of the box. A browser client library is available (`@stomp/stompjs`).

## Decision
STOMP over WebSockets. Clients subscribe to `/topic/games/{id}` and send moves to `/app/games/{id}/moves`.
The server validates each move, then broadcasts the new state to the game topic. REST stays for the lifecycle:
create, join, get state.

## Consequences
- Spring's built-in in-memory broker is enough for one server instance. Scaling to several instances later would
  need an external broker (e.g. RabbitMQ) or sticky sessions.
- Each message must be authorized with the player's seat token, not just the game id.
