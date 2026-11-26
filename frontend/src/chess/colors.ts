import type { Color } from '../api/game'

// Small helpers for the two colors, shared by the board, the promotion picker, the captured pieces and the page.

const NAMES: Record<Color, string> = { WHITE: 'White', BLACK: 'Black' }

/** "White" or "Black", for text on the page. */
export function colorName(color: Color): string {
  return NAMES[color]
}

/** "w" or "b": the first letter of react-chessboard's piece names, such as "wP" or "bK". */
export function pieceLetter(color: Color): 'w' | 'b' {
  return color === 'WHITE' ? 'w' : 'b'
}

/** The other color. */
export function opponent(color: Color): Color {
  return color === 'WHITE' ? 'BLACK' : 'WHITE'
}
