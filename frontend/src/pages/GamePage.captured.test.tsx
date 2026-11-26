import { screen, waitFor, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Game } from '../api/game'
import { getGame, playMove } from '../api/games'
import { square } from '../test/board'
import { GAME_AFTER_CAPTURE, GAME_CHECKMATE, NEW_GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

// The captured pieces next to the board, read from the FEN (#104).

vi.mock('../api/games')

afterEach(() => {
  vi.resetAllMocks()
})

async function openGame(game: Game) {
  vi.mocked(getGame).mockResolvedValue(game)
  const rendered = renderAt(`/games/${game.id}`)
  await screen.findByRole('group', { name: 'Captured by White' })
  return rendered
}

/** The names of the pieces a side has taken, and its lead in points as shown ("+1", "-1", "0"). */
function capturedBy(side: 'White' | 'Black') {
  const row = screen.getByRole('group', { name: `Captured by ${side}` })
  return {
    pieces: within(row).queryAllByRole('img').map((icon) => icon.getAttribute('aria-label')),
    lead: row.textContent,
  }
}

describe('captured pieces', () => {
  it('shows nothing taken and an even 0 at the start', async () => {
    await openGame(NEW_GAME)

    expect(capturedBy('White')).toEqual({ pieces: [], lead: '0' })
    expect(capturedBy('Black')).toEqual({ pieces: [], lead: '0' })
  })

  it('shows the pawn White took after 1. e4 d5 2. exd5, and the lead on both sides', async () => {
    await openGame(GAME_AFTER_CAPTURE)

    expect(capturedBy('White')).toEqual({ pieces: ['Black pawn'], lead: '+1' })
    expect(capturedBy('Black')).toEqual({ pieces: [], lead: '-1' })
  })

  it('shows the difference, not the totals, when both sides have taken pieces', async () => {
    // White took a pawn and a knight (4 points), Black took a bishop (3 points).
    await openGame({ ...NEW_GAME, fen: 'r1bqkbnr/ppp1pppp/8/8/8/8/PPPPPPPP/RN1QKBNR w KQkq - 0 5' })

    expect(capturedBy('White')).toEqual({ pieces: ['Black pawn', 'Black knight'], lead: '+1' })
    expect(capturedBy('Black')).toEqual({ pieces: ['White bishop'], lead: '-1' })
  })

  it('puts Black\'s captures above the board and White\'s below', async () => {
    await openGame(GAME_AFTER_CAPTURE)

    const board = square('a1').closest('[id="game-board"]')!
    const above = screen.getByRole('group', { name: 'Captured by Black' })
    const below = screen.getByRole('group', { name: 'Captured by White' })
    expect(above.compareDocumentPosition(board) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
    expect(below.compareDocumentPosition(board) & Node.DOCUMENT_POSITION_PRECEDING).toBeTruthy()
  })

  it('updates after a capture is played', async () => {
    // The position before 2. exd5, with the capture as a legal move.
    const beforeCapture: Game = {
      ...NEW_GAME,
      fen: 'rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2',
      legalMoves: ['e4d5', 'e4e5'],
    }
    const { user } = await openGame(beforeCapture)
    vi.mocked(playMove).mockResolvedValue(GAME_AFTER_CAPTURE)
    expect(capturedBy('White').pieces).toEqual([])

    await user.click(square('e4'))
    await user.click(square('d5'))

    await waitFor(() => expect(capturedBy('White')).toEqual({ pieces: ['Black pawn'], lead: '+1' }))
    expect(playMove).toHaveBeenCalledExactlyOnceWith(NEW_GAME.id, 'e4d5')
  })

  it('shows the captures of a finished game too', async () => {
    await openGame(GAME_CHECKMATE)

    // The fool's mate captures nothing.
    expect(capturedBy('White').lead).toBe('0')
    expect(capturedBy('Black').lead).toBe('0')
  })
})

