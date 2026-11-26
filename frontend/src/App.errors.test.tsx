import { screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, type MockInstance, vi } from 'vitest'
import type { Game } from './api/game'
import { getGame, startGame } from './api/games'
import { NEW_GAME } from './test/games'
import { renderAt } from './test/renderAt'

// An error while rendering shows an error page instead of a blank page (#107).

vi.mock('./api/games')

/**
 * A game the server would never send: White is in check, but the FEN has no white king. Drawing the check highlight
 * then throws in kingSquare, while the board renders: a real render error, as a bug of ours would cause one.
 */
const BROKEN_GAME: Game = { ...NEW_GAME, fen: 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQ1BNR w kq - 0 1', check: true }

let consoleError: MockInstance<typeof console.error>

beforeEach(() => {
  // React and the boundary log the expected error: kept out of the test output, and checked below.
  consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
})

afterEach(() => {
  vi.resetAllMocks()
  vi.restoreAllMocks()
})

describe('an error while rendering', () => {
  it('shows an error page below the header, not a blank page', async () => {
    vi.mocked(getGame).mockResolvedValue(BROKEN_GAME)

    renderAt(`/games/${BROKEN_GAME.id}`)

    expect(await screen.findByRole('heading', { name: 'Something went wrong' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'Chess' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Go to the start page' })).toBeInTheDocument()
  })

  it('keeps the error in the browser console', async () => {
    vi.mocked(getGame).mockResolvedValue(BROKEN_GAME)

    renderAt(`/games/${BROKEN_GAME.id}`)
    await screen.findByRole('heading', { name: 'Something went wrong' })

    const loggedErrors = consoleError.mock.calls.map(([first]) => first)
    expect(loggedErrors).toContainEqual(new Error(`No WHITE king in ${BROKEN_GAME.fen}`))
  })

  it('works again after following the link to the start page', async () => {
    vi.mocked(getGame).mockResolvedValue(BROKEN_GAME)
    const { user } = renderAt(`/games/${BROKEN_GAME.id}`)
    await screen.findByRole('heading', { name: 'Something went wrong' })

    await user.click(screen.getByRole('link', { name: 'Go to the start page' }))

    expect(screen.queryByRole('heading', { name: 'Something went wrong' })).not.toBeInTheDocument()
    vi.mocked(startGame).mockResolvedValue(NEW_GAME)
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)
    await user.click(screen.getByRole('button', { name: 'New game' }))
    expect(await screen.findByText('White to move')).toBeInTheDocument()
  })

  it('leaves pages without an error alone', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    expect(await screen.findByText('White to move')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Something went wrong' })).not.toBeInTheDocument()
    expect(consoleError).not.toHaveBeenCalled()
  })
})
