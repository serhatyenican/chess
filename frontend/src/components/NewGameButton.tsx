import { useState } from 'react'
import { useNavigate } from 'react-router'
import { ApiError } from '../api/ApiError'
import { startGame } from '../api/games'

/**
 * "New game": starts a game and opens its page, whose URL is the link to come back to it. Disabled while the game is
 * being started; if that fails, it shows why and can be clicked again. Used on the start page and after a game ended.
 */
function NewGameButton() {
  const navigate = useNavigate()
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleNewGame() {
    setStarting(true)
    setError(null)
    try {
      const game = await startGame()
      // No setStarting(false) here: the new game's page replaces the one this button is on.
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
    <>
      <button type="button" onClick={handleNewGame} disabled={starting}>
        New game
      </button>
      {error && <p role="alert">{error}</p>}
    </>
  )
}

export default NewGameButton
