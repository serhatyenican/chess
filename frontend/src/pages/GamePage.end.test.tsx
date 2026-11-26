import { screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Game } from '../api/game'
import { getGame, playMove, startGame } from '../api/games'
import { CHECK_STYLE, SELECTED_STYLE } from '../components/boardStyles'
import { hasMark, square } from '../test/board'
import { GAME_CHECKMATE, GAME_LEFT, GAME_STALEMATE, NEW_GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

// How a finished game is shown: the result instead of the turn, a locked board, and "New game".
// The result is found by its text, not by role="status": dnd-kit, which react-chessboard drags with, has a hidden
// status region of its own on the page. For the same reason the turn is matched as "White/Black to move": dnd-kit's
// hidden instructions say "use the arrow keys to move the item".

vi.mock('../api/games')

afterEach(() => {
  vi.resetAllMocks()
  vi.restoreAllMocks()
})

async function openGame(game: Game) {
  vi.mocked(getGame).mockResolvedValue(game)
  const rendered = renderAt(`/games/${game.id}`)
  await screen.findByRole('button', { name: 'New game' })
  return rendered
}

describe('a finished game', () => {
  it.each([
    ['a checkmate', GAME_CHECKMATE, 'Checkmate. Black wins.'],
    ['a stalemate', GAME_STALEMATE, "Stalemate. It's a draw."],
    ['a left game', GAME_LEFT, 'This game was left.'],
  ])('shows the result of %s instead of the turn', async (_case, game, result) => {
    await openGame(game)

    expect(screen.getByText(result)).toBeInTheDocument()
    expect(screen.queryByText(/(White|Black) to move/)).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'New game' })).toBeInTheDocument()
  })

  it('names the winner of a checkmate', async () => {
    await openGame({ ...GAME_CHECKMATE, winner: 'WHITE' })

    expect(screen.getByText('Checkmate. White wins.')).toBeInTheDocument()
  })

  it('keeps the mated king highlighted', async () => {
    await openGame(GAME_CHECKMATE)

    expect(hasMark('e1', CHECK_STYLE)).toBe(true)
  })

  it('locks the board', async () => {
    const { user } = await openGame(GAME_STALEMATE)

    await user.click(square('h8'))

    expect(hasMark('h8', SELECTED_STYLE)).toBe(false)
    expect(playMove).not.toHaveBeenCalled()
  })

  it('shows the result right after the last move', async () => {
    const beforeMate: Game = {
      ...NEW_GAME,
      fen: 'rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2',
      sideToMove: 'BLACK',
      legalMoves: ['d8h4', 'g8f6'],
    }
    vi.mocked(getGame).mockResolvedValue(beforeMate)
    vi.mocked(playMove).mockResolvedValue(GAME_CHECKMATE)
    const { user } = renderAt(`/games/${beforeMate.id}`)
    await screen.findByText('Black to move')

    await user.click(square('d8'))
    await user.click(square('h4'))

    expect(await screen.findByText('Checkmate. Black wins.')).toBeInTheDocument()
    expect(playMove).toHaveBeenCalledExactlyOnceWith(beforeMate.id, 'd8h4')
  })

  it('opens a new game from the result', async () => {
    const { user } = await openGame(GAME_CHECKMATE)
    const newGame: Game = { ...NEW_GAME, id: '6b1d3c4e-0f2a-4c8b-9e7d-5a1f2b3c4d5e' }
    vi.mocked(startGame).mockResolvedValue(newGame)
    vi.mocked(getGame).mockResolvedValue(newGame)

    await user.click(screen.getByRole('button', { name: 'New game' }))

    // From /games/{old id} to /games/{new id}: the page loads the new game instead of keeping the old one.
    expect(await screen.findByText('White to move')).toBeInTheDocument()
    await waitFor(() => expect(getGame).toHaveBeenLastCalledWith(newGame.id))
    expect(screen.queryByText(/Checkmate/)).not.toBeInTheDocument()
  })
})
