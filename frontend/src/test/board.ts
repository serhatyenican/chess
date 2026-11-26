import type { CSSProperties } from 'react'

// Finding things on react-chessboard's board in tests. Its squares carry data-square="e2", its pieces data-piece="wP".

/** The square element, which is also what a click on the square goes to. */
export function square(name: string): HTMLElement {
  const element = document.querySelector<HTMLElement>(`[data-square="${name}"]`)
  if (!element) throw new Error(`No square ${name} on the page`)
  return element
}

/** The piece on the square, such as "wP", or null if the square is empty. */
export function pieceOn(name: string): string | null {
  return square(name).querySelector('[data-piece]')?.getAttribute('data-piece') ?? null
}

/** Whether the square shows this mark. react-chessboard puts square styles on an overlay inside the square. */
export function hasMark(name: string, style: CSSProperties): boolean {
  return square(name).querySelector(`[style*="${String(style.background)}"]`) !== null
}
