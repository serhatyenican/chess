import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from './ApiError'
import type { Game } from './game'
import { getGame, leaveGame, playMove, startGame } from './games'
import { NEW_GAME as GAME } from '../test/games'

const ID = GAME.id

const fetchMock = vi.fn<typeof fetch>()

beforeEach(() => {
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
  fetchMock.mockReset()
})

function respondWith(status: number, body: unknown, contentType = 'application/json') {
  fetchMock.mockResolvedValue(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': contentType } }),
  )
}

/** An error answer as Spring sends it (checked against the running backend). */
function problem(status: number, title: string, detail: string) {
  respondWith(status, { detail, status, title }, 'application/problem+json')
}

/** The URL and options of the one fetch call made. */
function sentRequest() {
  expect(fetchMock).toHaveBeenCalledOnce()
  const [url, init] = fetchMock.mock.calls[0]
  return { url, method: init?.method ?? 'GET', headers: init?.headers, body: init?.body }
}

describe('the game endpoints', () => {
  it('startGame posts to the games collection', async () => {
    respondWith(201, GAME)

    await expect(startGame()).resolves.toEqual(GAME)
    expect(sentRequest()).toMatchObject({ url: '/api/games', method: 'POST', body: undefined })
  })

  it('getGame loads the game by its id', async () => {
    respondWith(200, GAME)

    await expect(getGame(ID)).resolves.toEqual(GAME)
    expect(sentRequest()).toMatchObject({ url: `/api/games/${ID}`, method: 'GET' })
  })

  it('playMove posts the move as JSON', async () => {
    respondWith(201, GAME)

    await expect(playMove(ID, 'e7e8q')).resolves.toEqual(GAME)
    expect(sentRequest()).toEqual({
      url: `/api/games/${ID}/moves`,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: '{"move":"e7e8q"}',
    })
  })

  it('leaveGame patches the status to LEFT', async () => {
    const left: Game = { ...GAME, status: 'LEFT', legalMoves: [] }
    respondWith(200, left)

    await expect(leaveGame(ID)).resolves.toEqual(left)
    expect(sentRequest()).toEqual({
      url: `/api/games/${ID}`,
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: '{"status":"LEFT"}',
    })
  })

  it('encodes the id, so it stays one path segment', async () => {
    respondWith(200, GAME)

    await getGame('a/b?c')

    expect(sentRequest().url).toBe('/api/games/a%2Fb%3Fc')
  })
})

describe('errors', () => {
  it.each([
    [404, 'Game not found', `No game with id ${ID}`],
    [409, 'Game over', `Game ${ID} has already ended: LEFT`],
    [422, 'Illegal move', 'The move e2e5 is not legal in this position'],
  ])('a %i problem detail becomes an ApiError with its status, title and detail', async (status, title, detail) => {
    problem(status, title, detail)

    const error = await playMove(ID, 'e2e5').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status, title, detail })
  })

  it('an error answer without a problem detail still gives its status', async () => {
    fetchMock.mockResolvedValue(new Response('Oops', { status: 500, statusText: 'Internal Server Error' }))

    await expect(getGame(ID)).rejects.toMatchObject({
      status: 500,
      title: 'Internal Server Error',
      detail: 'The server answered with status 500.',
    })
  })

  // What Vite's dev server answers when Spring isn't running.
  it.each([502, 503, 504])('a %i without a problem detail means the server is unreachable', async (status) => {
    fetchMock.mockResolvedValue(new Response('', { status, headers: { 'Content-Type': 'text/plain' } }))

    await expect(getGame(ID)).rejects.toMatchObject({
      status,
      title: 'Network error',
      detail: 'Could not reach the server. Check that it is running.',
    })
  })

  it('a gateway status with a problem detail keeps the server\'s text', async () => {
    problem(503, 'Service Unavailable', 'Down for maintenance')

    await expect(getGame(ID)).rejects.toMatchObject({ status: 503, title: 'Service Unavailable' })
  })

  it('a network failure becomes an ApiError without a status', async () => {
    const cause = new TypeError('Failed to fetch')
    fetchMock.mockRejectedValue(cause)

    await expect(startGame()).rejects.toMatchObject({ status: null, title: 'Network error', cause })
  })

  it('a successful answer that is not JSON becomes an ApiError', async () => {
    fetchMock.mockResolvedValue(new Response('<html>', { status: 200 }))

    await expect(getGame(ID)).rejects.toMatchObject({ status: 200, title: 'Invalid response' })
  })
})
