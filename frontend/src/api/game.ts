// The game as every game endpoint returns it: a copy of the backend's GameResponse (ADR 0010).
// Types exist only at compile time, so these must be kept in step with the Java records by hand.

export type Color = 'WHITE' | 'BLACK'

export type GameStatus = 'IN_PROGRESS' | 'CHECKMATE' | 'STALEMATE' | 'LEFT'

export type Game = {
  id: string
  fen: string
  sideToMove: Color
  check: boolean
  status: GameStatus
  /** The winner of a checkmate, otherwise null. */
  winner: Color | null
  /** The moves the side to move can play in UCI notation, e.g. "e7e8q"; empty once the game has ended. */
  legalMoves: string[]
}
