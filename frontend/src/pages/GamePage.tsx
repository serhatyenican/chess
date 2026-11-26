import { useParams } from 'react-router'

/** The game page, opened by a game's link. A placeholder until #86 loads and shows the game. */
function GamePage() {
  const { id } = useParams()

  return (
    <section>
      <h2>Game</h2>
      <p>Game {id}</p>
    </section>
  )
}

export default GamePage
