import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/ApiError'
import { getGame } from '../api/games'
import { CHECK_STYLE } from '../components/boardStyles'
import { hasMark, pieceOn } from '../test/board'
import { GAME_IN_CHECK, NEW_GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

vi.mock('../api/games')

afterEach(() => {
  vi.resetAllMocks()
  vi.restoreAllMocks()
})

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
    expect(pieceOn('e1')).toBe('wK')
    expect(pieceOn('e8')).toBe('bK')
    expect(pieceOn('e4')).toBeNull()
  })

  it('shows check and highlights the king of the side to move', async () => {
    vi.mocked(getGame).mockResolvedValue(GAME_IN_CHECK)

    renderAt(`/games/${GAME_IN_CHECK.id}`)

    const status = await screen.findByText(/Black to move/)
    expect(within(status).getByText(/Check/)).toBeInTheDocument()
    expect(hasMark('e8', CHECK_STYLE)).toBe(true)
    expect(hasMark('e1', CHECK_STYLE)).toBe(false)
  })

  it('shows no check when the side to move is not in check', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    await screen.findByText('White to move')
    expect(screen.queryByText(/Check/)).not.toBeInTheDocument()
    expect(hasMark('e1', CHECK_STYLE)).toBe(false)
  })

  // What the backend answers (checked with curl): 400 for an id that isn't a UUID, 404 for an unknown UUID.
  it.each([
    ['an id that is not a UUID', '1', new ApiError(400, 'Bad Request', "Failed to convert 'id' with value: '1'")],
    ['an unknown id', NEW_GAME.id, new ApiError(404, 'Game not found', `No game with id ${NEW_GAME.id}`)],
  ])('shows "Game not found" for %s', async (_case, id, error) => {
    vi.mocked(getGame).mockRejectedValue(error)
    const { user } = renderAt(`/games/${id}`)

    expect(await screen.findByRole('heading', { name: 'Game not found' })).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()

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
