import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/ApiError'
import type { Game } from '../api/game'
import { getGame, leaveGame } from '../api/games'
import { SELECTED_STYLE } from '../components/boardStyles'
import { hasMark, square } from '../test/board'
import { GAME_CHECKMATE, GAME_LEFT, NEW_GAME } from '../test/games'
import { renderAt } from '../test/renderAt'

// "Leave game" (#90): asks first, then ends the game; the page then shows it as left (#89).

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

/** The page's "Leave game" button, not the one in the confirmation dialog, which has the same name. */
function leaveButton() {
  return screen.getAllByRole('button', { name: 'Leave game' }).find((button) => !button.closest('dialog'))!
}

function confirmation() {
  return screen.getByRole('dialog', { name: 'Leave this game?' })
}

describe('leaving a game', () => {
  it('asks first, without leaving yet', async () => {
    const { user } = await openGame(NEW_GAME)

    await user.click(leaveButton())

    expect(within(confirmation()).getByText(/can't be continued/)).toBeInTheDocument()
    expect(within(confirmation()).getByRole('button', { name: 'Cancel' })).toHaveFocus()
    expect(leaveGame).not.toHaveBeenCalled()
  })

  it('leaves the game on confirm, and shows it as left with a locked board', async () => {
    const { user } = await openGame(NEW_GAME)
    vi.mocked(leaveGame).mockResolvedValue(GAME_LEFT)

    await user.click(leaveButton())
    await user.click(within(confirmation()).getByRole('button', { name: 'Leave game' }))

    expect(leaveGame).toHaveBeenCalledExactlyOnceWith(NEW_GAME.id)
    expect(await screen.findByText('This game was left.')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.queryAllByRole('button', { name: 'Leave game' })).toEqual([])

    await user.click(square('e2'))
    expect(hasMark('e2', SELECTED_STYLE)).toBe(false)
  })

  it.each([
    ['Cancel', async (user: ReturnType<typeof renderAt>['user']) => {
      await user.click(within(confirmation()).getByRole('button', { name: 'Cancel' }))
    }],
    ['Escape', async (user: ReturnType<typeof renderAt>['user']) => {
      await user.keyboard('{Escape}')
    }],
    ['a click outside', async (user: ReturnType<typeof renderAt>['user']) => {
      await user.click(confirmation())
    }],
  ])('changes nothing when cancelled with %s', async (_way, cancel) => {
    const { user } = await openGame(NEW_GAME)

    await user.click(leaveButton())
    await cancel(user)

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(leaveGame).not.toHaveBeenCalled()
    expect(screen.getByText('White to move')).toBeInTheDocument()
    expect(leaveButton()).toBeEnabled()
  })

  it('stays open on a click on its text', async () => {
    const { user } = await openGame(NEW_GAME)

    await user.click(leaveButton())
    // Inside the dialog, so no click outside.
    await user.click(within(confirmation()).getByText(/can't be continued/))

    expect(confirmation()).toBeInTheDocument()
    expect(leaveGame).not.toHaveBeenCalled()
  })

  it('shows no Leave button for a finished game', async () => {
    await openGame(GAME_CHECKMATE)

    expect(screen.queryAllByRole('button', { name: 'Leave game' })).toEqual([])
  })

  it('disables the button while leaving', async () => {
    const { user } = await openGame(NEW_GAME)
    vi.mocked(leaveGame).mockReturnValue(new Promise(() => {}))

    await user.click(leaveButton())
    await user.click(within(confirmation()).getByRole('button', { name: 'Leave game' }))

    expect(leaveButton()).toBeDisabled()
  })

  it('shows the message of a 409 and the server\'s game', async () => {
    const { user } = await openGame(NEW_GAME)
    // Meanwhile the game was ended in another tab.
    vi.mocked(leaveGame).mockRejectedValue(
      new ApiError(409, 'Game over', `Game ${NEW_GAME.id} has already ended: LEFT`),
    )
    vi.mocked(getGame).mockResolvedValue(GAME_LEFT)

    await user.click(leaveButton())
    await user.click(within(confirmation()).getByRole('button', { name: 'Leave game' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('has already ended')
    expect(await screen.findByText('This game was left.')).toBeInTheDocument()
    expect(getGame).toHaveBeenCalledTimes(2)
  })
})
