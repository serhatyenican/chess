import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/ApiError'
import { getGame } from '../api/games'
import { GAME_IN_CHECK, NEW_GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

vi.mock('../api/games')

afterEach(() => {
  vi.resetAllMocks()
  vi.restoreAllMocks()
})

/** The square of the board, found by the data-square attribute react-chessboard gives it. */
function square(name: string): HTMLElement {
  const element = document.querySelector<HTMLElement>(`[data-square="${name}"]`)
  if (!element) throw new Error(`No square ${name} on the page`)
  return element
}

/** Whether the square shows the check highlight. react-chessboard puts square styles on an overlay inside it. */
function isHighlighted(name: string): boolean {
  return square(name).querySelector('[style*="radial-gradient"]') !== null
}

describe('GamePage', () => {
  it('loads the game of the link', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    expect(screen.getByText('Loading the game…')).toBeInTheDocument()
    expect(await screen.findByText('White to move')).toBeInTheDocument()
    expect(getGame).toHaveBeenCalledWith(NEW_GAME.id)
  })

  it('shows the position of the FEN, White at the bottom', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    await screen.findByText('White to move')
    expect(square('e1').querySelector('[data-piece="wK"]')).not.toBeNull()
    expect(square('e8').querySelector('[data-piece="bK"]')).not.toBeNull()
    expect(square('e4').querySelector('[data-piece]')).toBeNull()
  })

  it('shows check and highlights the king of the side to move', async () => {
    vi.mocked(getGame).mockResolvedValue(GAME_IN_CHECK)

    renderAt(`/games/${GAME_IN_CHECK.id}`)

    const status = await screen.findByText(/Black to move/)
    expect(within(status).getByText(/Check/)).toBeInTheDocument()
    expect(isHighlighted('e8')).toBe(true)
    expect(isHighlighted('e1')).toBe(false)
  })

  it('shows no check when the side to move is not in check', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    await screen.findByText('White to move')
    expect(screen.queryByText(/Check/)).not.toBeInTheDocument()
    expect(isHighlighted('e1')).toBe(false)
  })

  it('shows "Game not found" for an unknown id', async () => {
    vi.mocked(getGame).mockRejectedValue(new ApiError(404, 'Game not found', 'No game with id 1'))
    const { user } = renderAt('/games/1')

    expect(await screen.findByRole('heading', { name: 'Game not found' })).toBeInTheDocument()

    await user.click(screen.getByRole('link', { name: 'Back to the start page' }))

    expect(screen.getByRole('button', { name: 'New game' })).toBeInTheDocument()
  })

  it('shows the message of any other error', async () => {
    vi.mocked(getGame).mockRejectedValue(ApiError.network(new TypeError('Failed to fetch')))

    renderAt(`/games/${NEW_GAME.id}`)

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not reach the server.')
  })

  it('shows a general message for an unexpected error, and logs it', async () => {
    const bug = new Error('boom')
    vi.mocked(getGame).mockRejectedValue(bug)
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    renderAt(`/games/${NEW_GAME.id}`)

    expect(await screen.findByRole('alert')).toHaveTextContent('Something went wrong. Please try again.')
    expect(consoleError).toHaveBeenCalledWith(bug)
  })

  it('has a link back to the start page', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    expect(await screen.findByRole('link', { name: 'Back to the start page' })).toHaveAttribute('href', '/')
  })
})
