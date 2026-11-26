import { describe, expect, it } from 'vitest'
import { colorName, opponent, pieceLetter } from './colors'

describe('colors', () => {
  it('names a color for the page', () => {
    expect(colorName('WHITE')).toBe('White')
    expect(colorName('BLACK')).toBe('Black')
  })

  it('gives the letter of react-chessboard\'s piece names', () => {
    expect(pieceLetter('WHITE')).toBe('w')
    expect(pieceLetter('BLACK')).toBe('b')
  })

  it('gives the other color', () => {
    expect(opponent('WHITE')).toBe('BLACK')
    expect(opponent('BLACK')).toBe('WHITE')
  })
})
