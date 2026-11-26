import { describe, expect, it } from 'vitest'
import { findMove, targetsFrom } from './moves'

const START = ['b1a3', 'b1c3', 'g1f3', 'g1h3', 'e2e3', 'e2e4', 'd2d3', 'd2d4']

describe('targetsFrom', () => {
  it('lists the squares a piece can move to', () => {
    expect(targetsFrom(START, 'e2')).toEqual(['e3', 'e4'])
    expect(targetsFrom(START, 'g1')).toEqual(['f3', 'h3'])
  })

  it('is empty for a square without legal moves', () => {
    expect(targetsFrom(START, 'e1')).toEqual([])
    expect(targetsFrom(START, 'e5')).toEqual([])
  })

  it('leaves out promotions for now', () => {
    expect(targetsFrom(['e7e8q', 'e7e8r', 'e7e8b', 'e7e8n', 'e7d8q', 'a2a3'], 'e7')).toEqual([])
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

  it('finds no promotion for now', () => {
    expect(findMove(['e7e8q', 'e7e8n'], 'e7', 'e8')).toBeUndefined()
  })
})
