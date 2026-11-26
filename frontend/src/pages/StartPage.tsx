import { useState } from 'react'
import { useNavigate } from 'react-router'
import { ApiError } from '../api/ApiError'
import { startGame } from '../api/games'

/** The start page: "New game" starts a game and opens its page, whose URL is the link to come back to it. */
function StartPage() {
  const navigate = useNavigate()
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleNewGame() {
    setStarting(true)
    setError(null)
    try {
      const game = await startGame()
      // No setStarting(false) here: this page is replaced by the game page.
      navigate(`/games/${game.id}`)
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.detail)
      } else {
        // Not an API error, so a bug in our own code: keep the stack trace in the browser console.
        console.error(e)
        setError('Something went wrong. Please try again.')
      }
      setStarting(false)
    }
  }

  return (
    <section>
      <button type="button" onClick={handleNewGame} disabled={starting}>
        New game
      </button>
      {error && <p role="alert">{error}</p>}
    </section>
  )
}

export default StartPage
