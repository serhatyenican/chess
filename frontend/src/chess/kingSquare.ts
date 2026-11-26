import type { Color } from '../api/game'

const FILES = 'abcdefgh'

/**
 * Returns the square of the king of this color, such as "e8", read from the piece placement field of a FEN.
 * The board needs it to highlight a king in check; all other rules stay on the server.
 */
export function kingSquare(fen: string, color: Color): string {
  const king = color === 'WHITE' ? 'K' : 'k'
  // The placement field lists the ranks from 8 down to 1, separated by "/". A digit stands for that many empty squares.
  const ranks = fen.split(' ')[0].split('/')
  for (let rankIndex = 0; rankIndex < ranks.length; rankIndex++) {
    let file = 0
    for (const symbol of ranks[rankIndex]) {
      if (symbol === king) {
        return `${FILES[file]}${8 - rankIndex}`
      }
      file += Number(symbol) || 1
    }
  }
  // The server only sends legal positions, so a missing king is our own mistake.
  throw new Error(`No ${color} king in ${fen}`)
}
