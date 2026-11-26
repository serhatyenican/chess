// What the board needs to know about moves, read from the server's legal moves in UCI notation ("e2e4", "e7e8q").
// The board has no rules of its own: a move is possible exactly when the server listed it.

/**
 * Returns the squares the piece on this square can move to, such as ["e3", "e4"] for "e2" at the start.
 * Promotion moves ("e7e8q") are left out until the board can ask for the piece (#88).
 */
export function targetsFrom(legalMoves: string[], from: string): string[] {
  return legalMoves
    .filter((move) => move.length === 4 && move.startsWith(from))
    .map((move) => move.slice(2, 4))
}

/**
 * Returns the legal move from one square to another, such as "e2e4", or undefined if there is none.
 * A promotion needs a fifth letter for the piece, so it isn't found here yet (#88).
 */
export function findMove(legalMoves: string[], from: string, to: string): string | undefined {
  return legalMoves.find((move) => move === from + to)
}
