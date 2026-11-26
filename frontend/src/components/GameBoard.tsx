import { Chessboard } from 'react-chessboard'
import type { Game } from '../api/game'
import { kingSquare } from '../chess/kingSquare'

const CHECK_STYLE = { background: 'radial-gradient(circle, rgba(220, 38, 38, 0.85) 25%, transparent 75%)' }

/**
 * Draws the game's position, White at the bottom, since one person plays both sides (v1). It only shows the board:
 * playing moves comes in #87.
 */
function GameBoard({ game }: { game: Game }) {
  const squareStyles = game.check ? { [kingSquare(game.fen, game.sideToMove)]: CHECK_STYLE } : {}

  // The board fills the width of its container, so the container limits its size.
  return (
    <div style={{ maxWidth: 560 }}>
      <Chessboard
        options={{
          id: 'game',
          position: game.fen,
          boardOrientation: 'white',
          allowDragging: false,
          squareStyles,
        }}
      />
    </div>
  )
}

export default GameBoard
