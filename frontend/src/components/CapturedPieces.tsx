import { defaultPieces } from 'react-chessboard'
import type { Color } from '../api/game'
import type { Captures, PieceKind } from '../chess/capturedPieces'
import { colorName, opponent, pieceLetter } from '../chess/colors'

const PIECE_NAMES: Record<PieceKind, string> = { p: 'pawn', n: 'knight', b: 'bishop', r: 'rook', q: 'queen' }

type CapturedPiecesProps = {
  /** The side that took the pieces. The pieces themselves are the opponent's. */
  by: Color
  captures: Captures
  /** This side's points minus the opponent's: positive when it is ahead. */
  lead: number
}

/**
 * One side's captured pieces as small icons, from pawns to queen, followed by its lead in points: "+3" for the side
 * ahead, "-3" for the side behind, "0" while it's even. The row and each icon have a name, so screen readers (and
 * tests) can read them.
 */
function CapturedPieces({ by, captures, lead }: CapturedPiecesProps) {
  const taken = opponent(by)

  return (
    <div
      role="group"
      aria-label={`Captured by ${colorName(by)}`}
      style={{ display: 'flex', alignItems: 'center', gap: 2, minHeight: 28 }}
    >
      {captures.pieces.map((piece, index) => (
        <span
          // The same piece can appear several times, so its position in the list keeps the keys unique.
          key={`${piece}-${index}`}
          role="img"
          aria-label={`${colorName(taken)} ${PIECE_NAMES[piece]}`}
          style={{ width: 24, height: 24 }}
        >
          {defaultPieces[`${pieceLetter(taken)}${piece.toUpperCase()}`]()}
        </span>
      ))}
      <span style={{ marginLeft: 6, fontWeight: 'bold' }}>{formatLead(lead)}</span>
    </div>
  )
}

/** "+3", "-3" or "0". A negative number already carries its sign. */
function formatLead(lead: number): string {
  return lead > 0 ? `+${lead}` : String(lead)
}

export default CapturedPieces
