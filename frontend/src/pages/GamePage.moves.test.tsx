import { screen, waitFor, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/ApiError'
import type { Game } from '../api/game'
import { getGame, playMove } from '../api/games'
import { SELECTED_STYLE, TARGET_STYLE } from '../components/boardStyles'
import { hasMark, pieceOn, square } from '../test/board'
import { GAME_AFTER_E4, GAME_BEFORE_PROMOTION, GAME_IN_CHECK, NEW_GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

// Playing moves by clicking. Dragging uses real pointer events (dnd-kit), which jsdom can't simulate well, so it is
// checked by hand in the browser (#87).

vi.mock('../api/games')

afterEach(() => {
  vi.resetAllMocks()
  vi.restoreAllMocks()
})

const NEW_GAME_WITH_E2_MOVES: Game = { ...NEW_GAME, legalMoves: ['e2e3', 'e2e4', 'g1f3', 'g1h3'] }

async function openGame(game: Game) {
  vi.mocked(getGame).mockResolvedValue(game)
  const rendered = renderAt(`/games/${game.id}`)
  await screen.findByText(/to move/)
  return rendered
}

describe('playing a move by clicking', () => {
  it('marks the selected piece and the squares it can move to', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)

    await user.click(square('e2'))

    expect(hasMark('e2', SELECTED_STYLE)).toBe(true)
    expect(hasMark('e3', TARGET_STYLE)).toBe(true)
    expect(hasMark('e4', TARGET_STYLE)).toBe(true)
    expect(hasMark('e5', TARGET_STYLE)).toBe(false)
    expect(hasMark('f3', TARGET_STYLE)).toBe(false)
  })

  it('plays the move on a target and shows the position the server answers with', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)
    vi.mocked(playMove).mockResolvedValue(GAME_AFTER_E4)

    await user.click(square('e2'))
    await user.click(square('e4'))

    expect(playMove).toHaveBeenCalledExactlyOnceWith(NEW_GAME.id, 'e2e4')
    expect(await screen.findByText('Black to move')).toBeInTheDocument()
    // react-chessboard animates the move, so the pawn arrives a moment later.
    await waitFor(() => expect(pieceOn('e4')).toBe('wP'))
    expect(pieceOn('e2')).toBeNull()
    expect(hasMark('e2', SELECTED_STYLE)).toBe(false)
  })

  it('clears the selection on a second click on the piece, or on a square that is no target', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)

    await user.click(square('e2'))
    await user.click(square('e2'))
    expect(hasMark('e2', SELECTED_STYLE)).toBe(false)

    await user.click(square('e2'))
    await user.click(square('e6'))
    expect(hasMark('e2', SELECTED_STYLE)).toBe(false)
    expect(hasMark('e4', TARGET_STYLE)).toBe(false)
    expect(playMove).not.toHaveBeenCalled()
  })

  it('selects another piece of the side to move instead', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)

    await user.click(square('e2'))
    await user.click(square('g1'))

    expect(hasMark('e2', SELECTED_STYLE)).toBe(false)
    expect(hasMark('g1', SELECTED_STYLE)).toBe(true)
    expect(hasMark('f3', TARGET_STYLE)).toBe(true)
  })

  it('only selects pieces of the side to move', async () => {
    const { user } = await openGame(GAME_IN_CHECK)

    // A white pawn, while Black is to move.
    expect(pieceOn('d2')).toBe('wP')
    await user.click(square('d2'))
    expect(hasMark('d2', SELECTED_STYLE)).toBe(false)

    await user.click(square('g7'))
    expect(hasMark('g7', SELECTED_STYLE)).toBe(true)
    expect(hasMark('g6', TARGET_STYLE)).toBe(true)
  })

  it('doesn\'t select a piece without a legal move', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)

    // The rook on a1 is blocked at the start.
    await user.click(square('a1'))

    expect(hasMark('a1', SELECTED_STYLE)).toBe(false)
  })

  it('in check, only selects the pieces that can get out of it', async () => {
    const { user } = await openGame(GAME_IN_CHECK)

    await user.click(square('d7'))
    expect(hasMark('d7', SELECTED_STYLE)).toBe(false)

    await user.click(square('g7'))
    expect(hasMark('g7', SELECTED_STYLE)).toBe(true)
  })

  it('takes no new move while a move is being sent', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)
    vi.mocked(playMove).mockReturnValue(new Promise(() => {}))

    await user.click(square('e2'))
    await user.click(square('e4'))
    await user.click(square('g1'))

    expect(hasMark('g1', SELECTED_STYLE)).toBe(false)
    expect(playMove).toHaveBeenCalledOnce()
  })
})

describe('a move the server rejects', () => {
  it.each([
    [409, 'Conflict', 'The game was changed by another request. Load it again and retry.'],
    [422, 'Illegal move', 'The move e2e4 is not legal in this position'],
  ])('shows the %i message and then the server\'s position', async (status, title, detail) => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)
    vi.mocked(playMove).mockRejectedValue(new ApiError(status, title, detail))
    // Meanwhile e2e4 was played in another tab, so loading the game again gives that position.
    vi.mocked(getGame).mockResolvedValue(GAME_AFTER_E4)

    await user.click(square('e2'))
    await user.click(square('e4'))

    expect(await screen.findByRole('alert')).toHaveTextContent(detail)
    expect(await screen.findByText('Black to move')).toBeInTheDocument()
    expect(getGame).toHaveBeenCalledTimes(2)
    await waitFor(() => expect(pieceOn('e4')).toBe('wP'))
  })

  it('shows any other error without loading the game again', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)
    vi.mocked(playMove).mockRejectedValue(ApiError.network(new TypeError('Failed to fetch')))

    await user.click(square('e2'))
    await user.click(square('e4'))

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not reach the server.')
    expect(getGame).toHaveBeenCalledOnce()
    expect(screen.getByText('White to move')).toBeInTheDocument()
  })

  it('clears the message with the next move', async () => {
    const { user } = await openGame(NEW_GAME_WITH_E2_MOVES)
    vi.mocked(playMove).mockRejectedValueOnce(ApiError.network(new TypeError('Failed to fetch')))
    vi.mocked(playMove).mockResolvedValueOnce(GAME_AFTER_E4)

    await user.click(square('e2'))
    await user.click(square('e4'))
    await screen.findByRole('alert')
    await user.click(square('e2'))
    await user.click(square('e4'))

    await waitFor(() => expect(screen.queryByRole('alert')).not.toBeInTheDocument())
    expect(await screen.findByText('Black to move')).toBeInTheDocument()
  })
})

describe('promoting a pawn', () => {
  /** The game after the pawn on e7 became this piece on e8 (Q, R, B or N). */
  function promotedTo(piece: string): Game {
    return { ...GAME_BEFORE_PROMOTION, fen: `3r${piece}2k/8/8/8/8/8/8/4K3 b - - 0 1`, sideToMove: 'BLACK' }
  }

  it('asks for the piece instead of sending a move', async () => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)

    await user.click(square('e7'))
    expect(hasMark('e8', TARGET_STYLE)).toBe(true)
    await user.click(square('e8'))

    const picker = screen.getByRole('dialog', { name: 'Promote the pawn to' })
    expect(within(picker).getAllByRole('button').map((button) => button.getAttribute('aria-label')))
      .toEqual(['Queen', 'Rook', 'Bishop', 'Knight'])
    expect(playMove).not.toHaveBeenCalled()
  })

  it.each([
    ['Queen', 'q', 'Q'],
    ['Rook', 'r', 'R'],
    ['Bishop', 'b', 'B'],
    ['Knight', 'n', 'N'],
  ])('sends the move for a %s and shows the new piece', async (name, letter, piece) => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)
    vi.mocked(playMove).mockResolvedValue(promotedTo(piece))

    await user.click(square('e7'))
    await user.click(square('e8'))
    await user.click(screen.getByRole('button', { name }))

    expect(playMove).toHaveBeenCalledExactlyOnceWith(GAME_BEFORE_PROMOTION.id, `e7e8${letter}`)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    await waitFor(() => expect(pieceOn('e8')).toBe(`w${piece}`))
  })

  it('asks for the piece on a capture that promotes too', async () => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)
    vi.mocked(playMove).mockReturnValue(new Promise(() => {}))

    await user.click(square('e7'))
    await user.click(square('d8'))
    await user.click(screen.getByRole('button', { name: 'Knight' }))

    expect(playMove).toHaveBeenCalledExactlyOnceWith(GAME_BEFORE_PROMOTION.id, 'e7d8n')
  })

  it('sends nothing when cancelled with Escape', async () => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)

    await user.click(square('e7'))
    await user.click(square('e8'))
    // The first piece has the focus, as in a browser, so Escape goes to the dialog.
    expect(screen.getByRole('button', { name: 'Queen' })).toHaveFocus()
    await user.keyboard('{Escape}')

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(playMove).not.toHaveBeenCalled()
    expect(pieceOn('e7')).toBe('wP')
  })

  it('sends nothing when cancelled by a click outside the pieces', async () => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)

    await user.click(square('e7'))
    await user.click(square('e8'))
    // A click on the backdrop reaches the dialog element itself, not one of its buttons.
    await user.click(screen.getByRole('dialog'))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(playMove).not.toHaveBeenCalled()
  })

  it('stays open on a click between the pieces', async () => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)

    await user.click(square('e7'))
    await user.click(square('e8'))
    // The box around the four buttons is inside the dialog, so a click there is no click outside.
    await user.click(screen.getByRole('button', { name: 'Queen' }).parentElement!)

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(playMove).not.toHaveBeenCalled()
  })

  it('can promote again after cancelling', async () => {
    const { user } = await openGame(GAME_BEFORE_PROMOTION)
    vi.mocked(playMove).mockReturnValue(new Promise(() => {}))

    await user.click(square('e7'))
    await user.click(square('e8'))
    await user.keyboard('{Escape}')
    await user.click(square('e7'))
    await user.click(square('e8'))
    await user.click(screen.getByRole('button', { name: 'Rook' }))

    expect(playMove).toHaveBeenCalledExactlyOnceWith(GAME_BEFORE_PROMOTION.id, 'e7e8r')
  })
})
