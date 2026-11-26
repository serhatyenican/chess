import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { ApiError } from '../api/ApiError'
import type { Color, Game, GameStatus as Status } from '../api/game'
import { getGame, playMove } from '../api/games'
import { capturedPieces } from '../chess/capturedPieces'
import { colorName } from '../chess/colors'
import CapturedPieces from '../components/CapturedPieces'
import GameBoard from '../components/GameBoard'
import NewGameButton from '../components/NewGameButton'

/**
 * What the page shows. One type with a case per situation, so impossible mixes such as "loading, but with an error"
 * can't happen. TypeScript only lets the code read `game` after checking that `kind` is 'loaded'.
 */
type PageState =
  | { kind: 'loading' }
  | { kind: 'loaded'; game: Game }
  | { kind: 'notFound' }
  | { kind: 'failed'; message: string }

/**
 * Both mean that there is no game behind the link: 404 for an unknown id, 400 for one that isn't a UUID at all (a
 * typo, or a link cut off when copied). Loading a game sends nothing but the id, so a 400 can't mean anything else.
 */
const NOT_FOUND_STATUSES: (number | null)[] = [400, 404]

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
      return <LoadedGame initialGame={state.game} />
  }
}

/**
 * A loaded game that is being played. It owns the game from here on: each move goes to the server, and the board
 * then shows the game the server answers with.
 */
function LoadedGame({ initialGame }: { initialGame: Game }) {
  const [game, setGame] = useState(initialGame)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  /**
   * Sends a change of the game to the server and shows the game it answers with. While it runs, the board takes no
   * moves. If the server rejects it because its game differs from ours (409, 422), e.g. because the same game was
   * moved in another tab, the message is shown and the game is loaded again.
   */
  async function sendChange(request: () => Promise<Game>) {
    setSending(true)
    setError(null)
    try {
      setGame(await request())
    } catch (e) {
      setError(messageOf(e))
      if (e instanceof ApiError && (e.status === 409 || e.status === 422)) {
        await getGame(game.id).then(setGame, (reloadError: unknown) => setError(messageOf(reloadError)))
      }
    } finally {
      setSending(false)
    }
  }

  function handleMove(move: string) {
    return sendChange(() => playMove(game.id, move))
  }

  const captured = capturedPieces(game.fen)

  // Each side's captures next to its own side of the board: Black's above, White's below (White is at the bottom).
  return (
    <>
      <GameStatus game={game} />
      {error && <p role="alert">{error}</p>}
      <CapturedPieces by="BLACK" captures={captured.BLACK} lead={captured.BLACK.points - captured.WHITE.points} />
      <GameBoard game={game} onMove={handleMove} disabled={sending} />
      <CapturedPieces by="WHITE" captures={captured.WHITE} lead={captured.WHITE.points - captured.BLACK.points} />
    </>
  )
}

/**
 * While the game goes on: whose turn it is, and whether that side is in check. Once it has ended: the result, and
 * "New game". The board is locked by then, since a finished game has no legal moves.
 */
function GameStatus({ game }: { game: Game }) {
  if (game.status === 'IN_PROGRESS') {
    return (
      <p>
        {colorName(game.sideToMove)} to move{game.check && <strong> · Check</strong>}
      </p>
    )
  }
  return (
    <div>
      <p role="status">
        <strong>{resultText(game.status, game.winner)}</strong>
      </p>
      <NewGameButton />
    </div>
  )
}

/** The ways a game can end: every status except IN_PROGRESS. */
type EndedStatus = Exclude<Status, 'IN_PROGRESS'>

function resultText(status: EndedStatus, winner: Color | null): string {
  switch (status) {
    case 'CHECKMATE':
      // The server stores a winner exactly for a checkmate (ADR 0011), so a missing one is our own mistake.
      if (!winner) throw new Error('A checkmate without a winner')
      return `Checkmate. ${colorName(winner)} wins.`
    case 'STALEMATE':
      return "Stalemate. It's a draw."
    case 'LEFT':
      return 'This game was left.'
    default: {
      // Only compiles while every status above is handled: a new one in the API (via `npm run api:types`) makes the
      // build fail here, like a Java switch over an enum without a default.
      const unhandled: never = status
      throw new Error(`Unknown game status ${String(unhandled)}`)
    }
  }
}

function failedState(e: unknown): PageState {
  if (e instanceof ApiError && NOT_FOUND_STATUSES.includes(e.status)) {
    return { kind: 'notFound' }
  }
  return { kind: 'failed', message: messageOf(e) }
}

/** The text to show for an error: the server's own text for an API error, a general one for anything else. */
function messageOf(e: unknown): string {
  if (e instanceof ApiError) {
    return e.detail
  }
  // Not an API error, so a bug in our own code: keep the stack trace in the browser console.
  console.error(e)
  return 'Something went wrong. Please try again.'
}

export default GamePage
