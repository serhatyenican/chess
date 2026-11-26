import { useEffect, useRef } from 'react'
import { defaultPieces } from 'react-chessboard'
import type { Color } from '../api/game'
import { pieceLetter } from '../chess/colors'
import type { PromotionPiece } from '../chess/moves'

type PromotionPickerProps = {
  /** The color of the promoting pawn, so the pieces are drawn in that color. */
  color: Color
  /** The pieces to offer, in this order. */
  pieces: PromotionPiece[]
  onChoose: (piece: PromotionPiece) => void
  /** Escape, or a click outside the four pieces. */
  onCancel: () => void
}

const PIECE_NAMES: Record<PromotionPiece, string> = { q: 'Queen', r: 'Rook', b: 'Bishop', n: 'Knight' }

/**
 * Asks which piece a pawn becomes. It is a modal <dialog>: the browser then dims and blocks the rest of the page,
 * moves the focus into the dialog, and turns Escape into a "cancel" event. It knows nothing about the game: the board
 * decides what a choice means.
 */
function PromotionPicker({ color, pieces, onChoose, onCancel }: PromotionPickerProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)

  // A <dialog> becomes modal only through showModal(), which needs the element on the page: hence the effect.
  useEffect(() => {
    const dialog = dialogRef.current!
    dialog.showModal()
    return () => dialog.close()
  }, [])

  return (
    <dialog
      ref={dialogRef}
      aria-label="Promote the pawn to"
      // Escape: the board closes the picker by no longer rendering it, so the browser mustn't close it itself.
      onCancel={(event) => {
        event.preventDefault()
        onCancel()
      }}
      // The dialog element itself is only hit on its backdrop, outside the buttons.
      onClick={(event) => {
        if (event.target === event.currentTarget) onCancel()
      }}
      style={{ padding: 0, border: 'none', borderRadius: 8 }}
    >
      <div style={{ display: 'flex', gap: 4, padding: 8 }}>
        {pieces.map((piece, index) => (
          <button
            key={piece}
            type="button"
            aria-label={PIECE_NAMES[piece]}
            autoFocus={index === 0}
            onClick={() => onChoose(piece)}
            style={{ width: 64, height: 64, padding: 4, cursor: 'pointer' }}
          >
            {defaultPieces[`${pieceLetter(color)}${piece.toUpperCase()}`]()}
          </button>
        ))}
      </div>
    </dialog>
  )
}

export default PromotionPicker
