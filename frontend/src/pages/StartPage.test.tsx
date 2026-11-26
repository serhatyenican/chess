import { screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/ApiError'
import type { Game } from '../api/game'
import { startGame } from '../api/games'
import { renderAt } from '../test/renderAt'

// Replaces every function of the API module with a mock, like @MockitoBean in a Spring test.
vi.mock('../api/games')

const GAME: Game = {
  id: '41052abf-657d-4047-8741-e41cc9c17a9a',
  fen: 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1',
  sideToMove: 'WHITE',
  check: false,
  status: 'IN_PROGRESS',
  winner: null,
  legalMoves: ['e2e4', 'g1f3'],
}

afterEach(() => {
  vi.resetAllMocks()
  // Puts back what vi.spyOn replaced, such as console.error.
  vi.restoreAllMocks()
})

describe('StartPage', () => {
  it('starts a game and opens its page', async () => {
    vi.mocked(startGame).mockResolvedValue(GAME)
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(startGame).toHaveBeenCalledOnce()
    expect(await screen.findByText(`Game ${GAME.id}`)).toBeInTheDocument()
  })

  it('disables the button while the game is being started', async () => {
    // A promise the test resolves itself, so it can look at the page while the request is still running.
    let resolve!: (game: Game) => void
    vi.mocked(startGame).mockReturnValue(new Promise((r) => (resolve = r)))
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(screen.getByRole('button', { name: 'New game' })).toBeDisabled()

    resolve(GAME)

    expect(await screen.findByText(`Game ${GAME.id}`)).toBeInTheDocument()
  })

  it('shows the error and lets you try again when starting fails', async () => {
    vi.mocked(startGame).mockRejectedValueOnce(ApiError.network(new TypeError('Failed to fetch')))
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not reach the server. Check that it is running.')
    expect(screen.getByRole('button', { name: 'New game' })).toBeEnabled()

    vi.mocked(startGame).mockResolvedValueOnce(GAME)
    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(await screen.findByText(`Game ${GAME.id}`)).toBeInTheDocument()
    expect(startGame).toHaveBeenCalledTimes(2)
  })

  it('shows a general message for an unexpected error, and logs it', async () => {
    const bug = new Error('boom')
    vi.mocked(startGame).mockRejectedValue(bug)
    // Replaced, so the expected error doesn't clutter the test output.
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Something went wrong. Please try again.')
    expect(consoleError).toHaveBeenCalledWith(bug)
  })

  it('doesn\'t log an API error, since it is expected', async () => {
    vi.mocked(startGame).mockRejectedValue(ApiError.network(new TypeError('Failed to fetch')))
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(await screen.findByRole('alert')).toBeInTheDocument()
    expect(consoleError).not.toHaveBeenCalled()
  })
})
