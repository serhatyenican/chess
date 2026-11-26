import { type CSSProperties, useState } from 'react'
import { Chessboard, type PieceDataType } from 'react-chessboard'
import type { Game } from '../api/game'
import { kingSquare } from '../chess/kingSquare'
import { findMove, hasMoves, promotionsFor, targetsFrom } from '../chess/moves'
import { CHECK_STYLE, SELECTED_STYLE, TARGET_STYLE } from './boardStyles'
import PromotionPicker from './PromotionPicker'

type GameBoardProps = {
  game: Game
  /** Called with a legal move in UCI notation, such as "e2e4". The board doesn't move the piece itself. */
  onMove: (move: string) => void
  /** True while a move is being sent: the board then takes no new moves. */
  disabled: boolean
}

/**
 * Draws the game's position, White at the bottom, since one person plays both sides (v1). A move is made by
 * dragging a piece, or by clicking it and then a target square; a pawn reaching the last rank first asks for the
 * piece. The board only reports the move: the page sends it, and the board then shows the position the server
 * answers with (ADR 0006).
 */
function GameBoard({ game, onMove, disabled }: GameBoardProps) {
  // The square of the selected piece. Only the board cares about it, so it is the board's own state.
  const [selected, setSelected] = useState<string | null>(null)
  // A pawn move to the last rank that waits for the piece, chosen in the promotion picker.
  const [promotion, setPromotion] = useState<{ from: string; to: string } | null>(null)

  const playable = !disabled && game.status === 'IN_PROGRESS'
  // react-chessboard names pieces like "wP" or "bK": the first letter is the color.
  const sideToMove = game.sideToMove === 'WHITE' ? 'w' : 'b'
  const isOwnPiece = (piece: PieceDataType | null) => piece?.pieceType.startsWith(sideToMove) ?? false
  // Only a piece of the side to move that has a legal move can be picked up: a blocked or pinned piece can't, and in
  // check only the pieces that can get out of it light up.
  const canMove = (piece: PieceDataType | null, square: string | null) =>
    square !== null && isOwnPiece(piece) && hasMoves(game.legalMoves, square)

  function play(from: string, to: string) {
    setSelected(null)
    const move = findMove(game.legalMoves, from, to)
    if (move) {
      onMove(move)
    } else if (promotionsFor(game.legalMoves, from, to).length > 0) {
      setPromotion({ from, to })
    }
  }

  function handleSquareClick({ piece, square }: { piece: PieceDataType | null; square: string }) {
    if (!playable) {
      return
    }
    if (selected && targetsFrom(game.legalMoves, selected).includes(square)) {
      play(selected, square)
    } else if (canMove(piece, square) && square !== selected) {
      setSelected(square)
    } else {
      setSelected(null)
    }
  }

  return (
    // The board fills the width of its container, so the container limits its size.
    <div style={{ maxWidth: 560 }}>
      <Chessboard
        options={{
          id: 'game',
          position: game.fen,
          boardOrientation: 'white',
          squareStyles: squareStyles(game, selected),
          canDragPiece: ({ piece, square }) => playable && canMove(piece, square),
          onPieceDrop: ({ sourceSquare, targetSquare }) => {
            if (targetSquare) {
              play(sourceSquare, targetSquare)
            }
            // Always false, so the piece goes back: the board shows the server's position once it answers.
            return false
          },
          onSquareClick: handleSquareClick,
        }}
      />
      {promotion && (
        <PromotionPicker
          color={game.sideToMove}
          pieces={promotionsFor(game.legalMoves, promotion.from, promotion.to)}
          onChoose={(piece) => {
            setPromotion(null)
            onMove(promotion.from + promotion.to + piece)
          }}
          onCancel={() => setPromotion(null)}
        />
      )}
    </div>
  )
}

/** The marks on the board: the king in check, the selected piece, and the squares it can move to. */
function squareStyles(game: Game, selected: string | null): Record<string, CSSProperties> {
  const styles: Record<string, CSSProperties> = {}
  if (game.check) {
    styles[kingSquare(game.fen, game.sideToMove)] = CHECK_STYLE
  }
  if (selected) {
    styles[selected] = SELECTED_STYLE
    targetsFrom(game.legalMoves, selected).forEach((target) => {
      styles[target] = TARGET_STYLE
    })
  }
  return styles
}

export default GameBoard
