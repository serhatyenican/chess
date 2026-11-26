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

/** After 1. e4: Black to move. */
export const GAME_AFTER_E4: Game = {
  ...NEW_GAME,
  fen: 'rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1',
  sideToMove: 'BLACK',
  legalMoves: ['e7e5', 'e7e6', 'g8f6', 'b8c6'],
}

/** A white pawn on e7 that promotes by moving to e8 or by capturing the black rook on d8. */
export const GAME_BEFORE_PROMOTION: Game = {
  ...NEW_GAME,
  fen: '3r3k/4P3/8/8/8/8/8/4K3 w - - 0 1',
  legalMoves: ['e7e8q', 'e7e8r', 'e7e8b', 'e7e8n', 'e7d8q', 'e7d8r', 'e7d8b', 'e7d8n', 'e1d1', 'e1d2', 'e1e2'],
}
