import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderAt } from './test/renderAt'

describe('App', () => {
  it('shows the heading on every page', () => {
    renderAt('/')

    expect(screen.getByRole('heading', { level: 1, name: 'Chess' })).toBeInTheDocument()
  })

  it('opens the start page at /', () => {
    renderAt('/')

    expect(screen.getByRole('button', { name: 'New game' })).toBeInTheDocument()
  })

  it('opens the game page at a game\'s link', () => {
    renderAt('/games/41052abf-657d-4047-8741-e41cc9c17a9a')

    expect(screen.getByRole('heading', { name: 'Game' })).toBeInTheDocument()
    expect(screen.getByText('Game 41052abf-657d-4047-8741-e41cc9c17a9a')).toBeInTheDocument()
  })

  it.each(['/nope', '/games', '/games/1/moves'])('shows "Page not found" at %s', async (url) => {
    const { user } = renderAt(url)

    expect(screen.getByRole('heading', { name: 'Page not found' })).toBeInTheDocument()

    await user.click(screen.getByRole('link', { name: 'Go to the start page' }))

    expect(screen.getByRole('button', { name: 'New game' })).toBeInTheDocument()
  })
})
