import { defaultPieces } from 'react-chessboard'
import type { Color } from '../api/game'
import { pieceLetter } from '../chess/colors'
import type { PromotionPiece } from '../chess/moves'
import ModalDialog from './ModalDialog'

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

/** Asks which piece a pawn becomes. It knows nothing about the game: the board decides what a choice means. */
function PromotionPicker({ color, pieces, onChoose, onCancel }: PromotionPickerProps) {
  return (
    <ModalDialog label="Promote the pawn to" onCancel={onCancel}>
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
    </ModalDialog>
  )
}

export default PromotionPicker
