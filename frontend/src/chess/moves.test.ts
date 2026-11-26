import { describe, expect, it } from 'vitest'
import { findMove, hasMoves, promotionsFor, targetsFrom } from './moves'

const START = ['b1a3', 'b1c3', 'g1f3', 'g1h3', 'e2e3', 'e2e4', 'd2d3', 'd2d4']

// A white pawn on e7 that can move to e8 or capture on d8, both promoting.
const PROMOTIONS = ['e7e8q', 'e7e8r', 'e7e8b', 'e7e8n', 'e7d8q', 'e7d8r', 'e7d8b', 'e7d8n', 'e1d1']

describe('hasMoves', () => {
  it('is true for a piece with a legal move', () => {
    expect(hasMoves(START, 'e2')).toBe(true)
    expect(hasMoves(START, 'g1')).toBe(true)
  })

  it('is false for a piece without one, or an empty square', () => {
    expect(hasMoves(START, 'a1')).toBe(false)
    expect(hasMoves(START, 'e5')).toBe(false)
  })

  it('counts promotions', () => {
    expect(hasMoves(['e7e8q', 'e7e8n'], 'e7')).toBe(true)
  })
})

describe('targetsFrom', () => {
  it('lists the squares a piece can move to', () => {
    expect(targetsFrom(START, 'e2')).toEqual(['e3', 'e4'])
    expect(targetsFrom(START, 'g1')).toEqual(['f3', 'h3'])
  })

  it('is empty for a square without legal moves', () => {
    expect(targetsFrom(START, 'e1')).toEqual([])
    expect(targetsFrom(START, 'e5')).toEqual([])
  })

  it('lists a promotion square once', () => {
    expect(targetsFrom(PROMOTIONS, 'e7')).toEqual(['e8', 'd8'])
  })
})

describe('findMove', () => {
  it('finds a legal move', () => {
    expect(findMove(START, 'e2', 'e4')).toBe('e2e4')
  })

  it('finds nothing for an illegal move', () => {
    expect(findMove(START, 'e2', 'e5')).toBeUndefined()
    expect(findMove(START, 'e4', 'e2')).toBeUndefined()
  })

  it('finds no promotion, since it needs the piece', () => {
    expect(findMove(PROMOTIONS, 'e7', 'e8')).toBeUndefined()
  })
})

describe('promotionsFor', () => {
  it('lists the pieces a pawn can become', () => {
    expect(promotionsFor(PROMOTIONS, 'e7', 'e8')).toEqual(['q', 'r', 'b', 'n'])
  })

  it('works for a capture that promotes', () => {
    expect(promotionsFor(PROMOTIONS, 'e7', 'd8')).toEqual(['q', 'r', 'b', 'n'])
  })

  it('is empty for a move that is no promotion', () => {
    expect(promotionsFor(PROMOTIONS, 'e1', 'd1')).toEqual([])
    expect(promotionsFor(START, 'e2', 'e4')).toEqual([])
  })
})
