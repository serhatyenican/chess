import type { components } from './schema'

// The game as every game endpoint returns it. The types come from the backend's OpenAPI spec (ADR 0012), generated
// into schema.ts by `npm run api:types`, so they follow the Java records instead of being a copy of them.
// The rest of the frontend imports these names, not schema.ts, so it doesn't depend on the generated file's layout.

export type Game = components['schemas']['GameResponse']

export type Color = Game['sideToMove']

export type GameStatus = Game['status']

// The request bodies. games.ts checks its bodies against these with `satisfies`, so a renamed field fails the build.

export type PlayMoveRequest = components['schemas']['PlayMoveRequest']

export type ChangeGameRequest = components['schemas']['ChangeGameRequest']
