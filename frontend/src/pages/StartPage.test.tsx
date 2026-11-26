import { screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/ApiError'
import type { Game } from '../api/game'
import { getGame, startGame } from '../api/games'
import { NEW_GAME as GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

// Replaces every function of the API module with a mock, like @MockitoBean in a Spring test.
vi.mock('../api/games')

beforeEach(() => {
  // The game page loads the game after the navigation; it stays loading, since these tests are about the start page.
  vi.mocked(getGame).mockReturnValue(new Promise(() => {}))
})

/** Waits until the app has navigated to this game's page, which then asks for that game. */
async function expectGamePageOf(id: string) {
  await waitFor(() => expect(getGame).toHaveBeenCalledWith(id))
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
    await expectGamePageOf(GAME.id)
  })

  it('disables the button while the game is being started', async () => {
    // A promise the test resolves itself, so it can look at the page while the request is still running.
    let resolve!: (game: Game) => void
    vi.mocked(startGame).mockReturnValue(new Promise((r) => (resolve = r)))
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(screen.getByRole('button', { name: 'New game' })).toBeDisabled()

    resolve(GAME)

    await expectGamePageOf(GAME.id)
  })

  it('shows the error and lets you try again when starting fails', async () => {
    vi.mocked(startGame).mockRejectedValueOnce(ApiError.network(new TypeError('Failed to fetch')))
    const { user } = renderAt('/')

    await user.click(screen.getByRole('button', { name: 'New game' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not reach the server. Check that it is running.')
    expect(screen.getByRole('button', { name: 'New game' })).toBeEnabled()

    vi.mocked(startGame).mockResolvedValueOnce(GAME)
    await user.click(screen.getByRole('button', { name: 'New game' }))

    await expectGamePageOf(GAME.id)
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
