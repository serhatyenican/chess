import { describe, expect, it } from 'vitest'
import { GAME_IN_CHECK, NEW_GAME } from '../test/games'
import { kingSquare } from './kingSquare'

describe('kingSquare', () => {
  it('finds both kings in the starting position', () => {
    expect(kingSquare(NEW_GAME.fen, 'WHITE')).toBe('e1')
    expect(kingSquare(NEW_GAME.fen, 'BLACK')).toBe('e8')
  })

  it('counts the empty squares before a king', () => {
    // After castling on both sides: the white king on g1, the black king on c8.
    const fen = '2kr1bnr/pppqpppp/2n5/3p1b2/3P1B2/2N5/PPPQPPPP/R4RK1 w - - 8 6'

    expect(kingSquare(fen, 'WHITE')).toBe('g1')
    expect(kingSquare(fen, 'BLACK')).toBe('c8')
  })

  it('finds a king on the edge of the board', () => {
    expect(kingSquare('7k/8/8/8/8/8/8/K7 w - - 0 1', 'WHITE')).toBe('a1')
    expect(kingSquare('7k/8/8/8/8/8/8/K7 w - - 0 1', 'BLACK')).toBe('h8')
  })

  it('finds the king of the side in check', () => {
    expect(kingSquare(GAME_IN_CHECK.fen, 'BLACK')).toBe('e8')
  })

  it('fails when there is no such king', () => {
    expect(() => kingSquare('8/8/8/8/8/8/8/K7 w - - 0 1', 'BLACK')).toThrow('No BLACK king')
  })
})
