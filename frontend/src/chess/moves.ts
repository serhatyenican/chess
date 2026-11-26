// What the board needs to know about moves, read from the server's legal moves in UCI notation ("e2e4", "e7e8q").
// The board has no rules of its own: a move is possible exactly when the server listed it.

/** The piece a pawn becomes, as the fifth letter of a promotion move: queen, rook, bishop or knight. */
export type PromotionPiece = 'q' | 'r' | 'b' | 'n'

/**
 * Returns whether the piece on this square has any legal move, so it can be selected or dragged. Promotions count:
 * a pawn whose only moves are promotions can still be picked up.
 */
export function hasMoves(legalMoves: string[], from: string): boolean {
  return legalMoves.some((move) => move.startsWith(from))
}

/**
 * Returns the squares the piece on this square can move to, such as ["e3", "e4"] for "e2" at the start. A promotion
 * square appears once, although there is a move for each piece the pawn can become.
 */
export function targetsFrom(legalMoves: string[], from: string): string[] {
  const targets = legalMoves.filter((move) => move.startsWith(from)).map((move) => move.slice(2, 4))
  return [...new Set(targets)]
}

/**
 * Returns the legal move from one square to another, such as "e2e4", or undefined if there is none. A promotion
 * needs the piece as a fifth letter, so it isn't found here: see promotionsFor.
 */
export function findMove(legalMoves: string[], from: string, to: string): string | undefined {
  return legalMoves.find((move) => move === from + to)
}

/** The order the picker shows the pieces in, strongest first. The UI's own choice, not the server's move order. */
const PROMOTION_ORDER: PromotionPiece[] = ['q', 'r', 'b', 'n']

/**
 * Returns the pieces a pawn can become by moving from one square to another, in the order queen, rook, bishop,
 * knight, or an empty list if that move is no promotion.
 */
export function promotionsFor(legalMoves: string[], from: string, to: string): PromotionPiece[] {
  const pieces = legalMoves
    .filter((move) => move.length === 5 && move.startsWith(from + to))
    .map((move) => move[4])
  return PROMOTION_ORDER.filter((piece) => pieces.includes(piece))
}
