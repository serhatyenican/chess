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

/** After 1. e4 f6 2. Qh5+, as the API answered it: Black is in check, and g7g6 is the only way out. */
export const GAME_IN_CHECK: Game = {
  ...NEW_GAME,
  fen: 'rnbqkbnr/ppppp1pp/5p2/7Q/4P3/8/PPPP1PPP/RNB1KBNR b KQkq - 1 2',
  sideToMove: 'BLACK',
  check: true,
  legalMoves: ['g7g6'],
}
