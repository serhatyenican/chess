import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { ApiError } from '../api/ApiError'
import type { Color, Game } from '../api/game'
import { getGame } from '../api/games'
import GameBoard from '../components/GameBoard'

/**
 * What the page shows. One type with a case per situation, so impossible mixes such as "loading, but with an error"
 * can't happen. TypeScript only lets the code read `game` after checking that `kind` is 'loaded'.
 */
type PageState =
  | { kind: 'loading' }
  | { kind: 'loaded'; game: Game }
  | { kind: 'notFound' }
  | { kind: 'failed'; message: string }

const COLOR_NAMES: Record<Color, string> = { WHITE: 'White', BLACK: 'Black' }

/** The game page, opened by a game's link: loads the game and shows its position. */
function GamePage() {
  // The route only matches with an id, so it is always there.
  const id = useParams().id!

  // The key gives each game its own GameView: when the id changes, React creates a new one, which starts loading
  // again, instead of keeping the previous game's state.
  return <GameView key={id} id={id} />
}

function GameView({ id }: { id: string }) {
  const [state, setState] = useState<PageState>({ kind: 'loading' })

  // Loading the game is a side effect, so it runs after React has drawn the page.
  useEffect(() => {
    // Set by the cleanup below, so an answer that arrives after the page has gone (or for StrictMode's extra run)
    // is ignored.
    let ignore = false
    getGame(id)
      .then((game) => {
        if (!ignore) setState({ kind: 'loaded', game })
      })
      .catch((e: unknown) => {
        if (!ignore) setState(failedState(e))
      })
    return () => {
      ignore = true
    }
  }, [id])

  return (
    <section>
      <GameContent state={state} />
      <p>
        <Link to="/">Back to the start page</Link>
      </p>
    </section>
  )
}

function GameContent({ state }: { state: PageState }) {
  switch (state.kind) {
    case 'loading':
      return <p>Loading the game…</p>
    case 'notFound':
      return <h2>Game not found</h2>
    case 'failed':
      return <p role="alert">{state.message}</p>
    case 'loaded':
      return (
        <>
          <GameStatus game={state.game} />
          <GameBoard game={state.game} />
        </>
      )
  }
}

/** Whose turn it is, and whether that side is in check. How a game ended is shown from #89. */
function GameStatus({ game }: { game: Game }) {
  if (game.status !== 'IN_PROGRESS') {
    return null
  }
  return (
    <p>
      {COLOR_NAMES[game.sideToMove]} to move{game.check && <strong> · Check</strong>}
    </p>
  )
}

function failedState(e: unknown): PageState {
  if (e instanceof ApiError) {
    return e.status === 404 ? { kind: 'notFound' } : { kind: 'failed', message: e.detail }
  }
  // Not an API error, so a bug in our own code: keep the stack trace in the browser console.
  console.error(e)
  return { kind: 'failed', message: 'Something went wrong. Please try again.' }
}

export default GamePage
