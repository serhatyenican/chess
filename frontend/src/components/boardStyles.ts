import type { CSSProperties } from 'react'

// The marks the board puts on squares. In their own module, so tests can find them on the page.

/** The king of the side to move, when it is in check. */
export const CHECK_STYLE: CSSProperties = {
  background: 'radial-gradient(circle, rgba(220, 38, 38, 0.85) 25%, transparent 75%)',
}

/** The square of the selected piece. */
export const SELECTED_STYLE: CSSProperties = { background: 'rgba(250, 204, 21, 0.55)' }

/** A square the selected piece can move to. */
export const TARGET_STYLE: CSSProperties = {
  background: 'radial-gradient(circle, rgba(0, 0, 0, 0.25) 18%, transparent 22%)',
}
