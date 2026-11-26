import type { Game } from '../api/game'

// Games as the API returns them, for tests. Each test can change what it needs: `{ ...NEW_GAME, status: 'LEFT' }`.

const ID = '41052abf-657d-4047-8741-e41cc9c17a9a'

/** A game that has just started. */
export const NEW_GAME: Game = {
  id: ID,
  fen: 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1',
  sideToMove: 'WHITE',
  check: false,
  status: 'IN_PROGRESS',
  winner: null,
  legalMoves: ['e2e4', 'g1f3'],
}
