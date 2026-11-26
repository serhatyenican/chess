import type { Color } from '../api/game'

/** A piece type as the FEN writes it, in lower case: pawn, knight, bishop, rook, queen. Kings are never captured. */
export type PieceKind = 'p' | 'n' | 'b' | 'r' | 'q'

/** What one side has taken: the opponent's pieces, from pawns to queen, and their points. */
export type Captures = {
  pieces: PieceKind[]
  points: number
}

const KINDS: PieceKind[] = ['p', 'n', 'b', 'r', 'q']
const STARTING_COUNT: Record<PieceKind, number> = { p: 8, n: 2, b: 2, r: 2, q: 1 }
const POINTS: Record<PieceKind, number> = { p: 1, n: 3, b: 3, r: 5, q: 9 }

/**
 * Returns what each side has captured, read from the piece placement field of a FEN: every piece missing from a
 * side's starting set counts as taken by the other side. Reading the FEN alone keeps it right after reloading a game.
 * <p>
 * Known limit, accepted (#104): a promotion skews the count. The promoted pawn looks captured, and an extra queen
 * would count as "−1" queens taken, which is clamped to 0. Exact captures would need the backend to derive them from
 * the stored moves.
 */
export function capturedPieces(fen: string): Record<Color, Captures> {
  const placement = fen.split(' ')[0]
  return {
    WHITE: captures(missingPieces(placement, 'BLACK')),
    BLACK: captures(missingPieces(placement, 'WHITE')),
  }
}

/** The pieces of this color that are no longer on the board, from pawns to queen. */
function missingPieces(placement: string, color: Color): PieceKind[] {
  return KINDS.flatMap((kind) => {
    // The FEN writes White's pieces in upper case and Black's in lower case.
    const symbol = color === 'WHITE' ? kind.toUpperCase() : kind
    const onBoard = [...placement].filter((square) => square === symbol).length
    return Array<PieceKind>(Math.max(0, STARTING_COUNT[kind] - onBoard)).fill(kind)
  })
}

function captures(pieces: PieceKind[]): Captures {
  return { pieces, points: pieces.reduce((sum, piece) => sum + POINTS[piece], 0) }
}
