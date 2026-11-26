import { ApiError } from './ApiError'
import type { ChangeGameRequest, Game, PlayMoveRequest } from './game'

// The client for the game endpoints (ADR 0010). Components call these functions and never `fetch` itself.
// Every function resolves to the game as the server now has it, or rejects with an ApiError.

/** Starts a new game: `POST /api/games`. */
export function startGame(): Promise<Game> {
  return request('/api/games', { method: 'POST' })
}

/** Loads a game, e.g. when its link is opened again: `GET /api/games/{id}`. */
export function getGame(id: string): Promise<Game> {
  return request(gamePath(id))
}

/** Plays a move in UCI notation, e.g. "e2e4" or "e7e8q": `POST /api/games/{id}/moves`. */
export function playMove(id: string, move: string): Promise<Game> {
  return request(`${gamePath(id)}/moves`, jsonBody('POST', { move } satisfies PlayMoveRequest))
}

/** Leaves a game in progress: `PATCH /api/games/{id}` with status LEFT. */
export function leaveGame(id: string): Promise<Game> {
  return request(gamePath(id), jsonBody('PATCH', { status: 'LEFT' } satisfies ChangeGameRequest))
}

// The id comes from the page's URL, so it is encoded: a stray "/" or "?" can't change which endpoint is called.
function gamePath(id: string): string {
  return `/api/games/${encodeURIComponent(id)}`
}

function jsonBody(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}

async function request(path: string, init?: RequestInit): Promise<Game> {
  let response: Response
  try {
    response = await fetch(path, init)
  } catch (cause) {
    throw ApiError.network(cause)
  }
  if (!response.ok) {
    throw await ApiError.fromResponse(response)
  }
  try {
    // Trusted, not validated: the backend is ours, and its tests pin the shape of GameResponse.
    return (await response.json()) as Game
  } catch (cause) {
    throw ApiError.invalidBody(response, cause)
  }
}
