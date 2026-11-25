# 0010. Design the REST API around resources

- Status: Accepted
- Date: 2026-11-25

## Context
M3 adds the game API: start a game, open it from its link, play a move, and leave it. The frontend (M4) and, from
v2, a second player use it. We want one consistent style before the first endpoint is written, so that the URLs stay
predictable as the API grows.

## Options
For the names of resources:
1. **Singular** (`/api/game/{id}`): reads naturally for one item, but the collection then has the same name.
2. **Plural** (`/api/games`, `/api/games/{id}`): the common convention (Google, Microsoft, Zalando guidelines). The
   collection and one item of it share a name.

For moves:
1. **Top level** (`/api/moves`, game id in the body): fits a resource that lives on its own or is searched across
   parents. A move doesn't: it only exists inside one game.
2. **Nested** (`/api/games/{id}/moves`): the URL shows that a game owns its moves, and a move is identified by its game
   and its number.

For leaving a game:
1. **`DELETE /api/games/{id}`**: after a delete, a `GET` should return 404, but we keep the game so its link still
   shows it. Rejected.
2. **Action URL** (`POST /api/games/{id}/leave`): a known pragmatic pattern (Google calls it a custom method), but it
   is a verb, not a resource.
3. **`PATCH /api/games/{id}`** with `{"status": "LEFT"}`: a change to the state of the game resource. The server
   accepts only the changes the rules allow.

## Decision
- Resources are nouns, and collections are named in the plural: `/api/games`, `/api/games/{id}`.
- A resource that only exists inside another is nested, at most one level deep: `/api/games/{id}/moves`.
- A state change is a change to the resource, not an action URL. Leaving a game is `PATCH /api/games/{id}` with
  `{"status": "LEFT"}`.
- Game ids are random UUIDs, so a game's link can't be guessed (this matters from v2, when the link is the access).
- Errors use Spring's `ProblemDetail` (RFC 9457): 400 for a malformed request, 404 for an unknown game, 409 when the
  game has already ended, and 422 for a well-formed move that is not legal.

The v1 API:

| Method and path | Meaning |
| --- | --- |
| `POST /api/games` | Start a game. 201 with a `Location` header. |
| `GET /api/games/{id}` | The game: position, turn, check, status, winner, moves and legal moves. |
| `POST /api/games/{id}/moves` | Play a move, e.g. `{"move": "e7e8q"}`. |
| `PATCH /api/games/{id}` | Leave the game: `{"status": "LEFT"}`. |

## Consequences
- `PATCH` looks like a generic update but carries a business rule: only `IN_PROGRESS` to `LEFT` is allowed, and any
  other field or status is rejected. Each such rule must be spelled out in its issue and tested.
- New endpoints follow these rules. A real action that doesn't fit a resource (for example resigning in v2) is
  discussed first, and may need a new ADR.
