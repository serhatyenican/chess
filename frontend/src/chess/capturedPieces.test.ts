import { describe, expect, it } from 'vitest'
import { NEW_GAME } from '../test/games'
import { capturedPieces } from './capturedPieces'

describe('capturedPieces', () => {
  it('finds nothing captured at the start', () => {
    expect(capturedPieces(NEW_GAME.fen)).toEqual({
      WHITE: { pieces: [], points: 0 },
      BLACK: { pieces: [], points: 0 },
    })
  })

  it('finds the pawn White took after 1. e4 d5 2. exd5', () => {
    expect(capturedPieces('rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2')).toEqual({
      WHITE: { pieces: ['p'], points: 1 },
      BLACK: { pieces: [], points: 0 },
    })
  })

  it('lists captures on both sides from pawns to queen, with their points', () => {
    // White is missing a pawn, a knight and the queen; Black is missing two pawns, a bishop and a rook.
    const fen = 'rnbqk1n1/pppppp2/8/8/8/8/PPPPPPP1/R1B1KBNR w Qq - 0 20'

    expect(capturedPieces(fen)).toEqual({
      WHITE: { pieces: ['p', 'p', 'b', 'r'], points: 10 },
      BLACK: { pieces: ['p', 'n', 'q'], points: 13 },
    })
  })

  it('never counts a negative number of pieces after a promotion', () => {
    // White promoted a pawn to a second queen: the pawn looks captured, the extra queen is not "−1".
    const fen = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPP1/RNBQKBNQ w - - 0 30'

    expect(capturedPieces(fen)).toEqual({
      WHITE: { pieces: [], points: 0 },
      BLACK: { pieces: ['p', 'r'], points: 6 },
    })
  })
})
