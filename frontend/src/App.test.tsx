import { screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { getGame } from './api/games'
import { NEW_GAME } from './test/games'
import { renderAt } from './test/renderAt'

vi.mock('./api/games')

describe('App', () => {
  it('shows the heading on every page', () => {
    renderAt('/')

    expect(screen.getByRole('heading', { level: 1, name: 'Chess' })).toBeInTheDocument()
  })

  it('opens the start page at /', () => {
    renderAt('/')

    expect(screen.getByRole('button', { name: 'New game' })).toBeInTheDocument()
  })

  it('opens the game page at a game\'s link', async () => {
    vi.mocked(getGame).mockResolvedValue(NEW_GAME)

    renderAt(`/games/${NEW_GAME.id}`)

    expect(await screen.findByText('White to move')).toBeInTheDocument()
    expect(getGame).toHaveBeenCalledWith(NEW_GAME.id)
  })

  it.each(['/nope', '/games', '/games/1/moves'])('shows "Page not found" at %s', async (url) => {
    const { user } = renderAt(url)

    expect(screen.getByRole('heading', { name: 'Page not found' })).toBeInTheDocument()

    await user.click(screen.getByRole('link', { name: 'Go to the start page' }))

    expect(screen.getByRole('button', { name: 'New game' })).toBeInTheDocument()
  })
})
