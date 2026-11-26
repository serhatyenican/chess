import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// Without Vitest globals, React Testing Library can't register its own cleanup,
// so unmount whatever a test rendered before the next one starts.
afterEach(cleanup)

// jsdom has <dialog>, but not its modal behavior. This stands in for what browsers do: showModal() opens the dialog
// and moves the focus to its first focusable element, and Escape inside it fires a cancelable "cancel" event, which
// closes it unless a handler prevents that. Moving the focus matters since jsdom 31: like browsers, it doesn't focus
// an element inside a closed dialog, so React's autoFocus (applied before the effect that opens it) has no effect.
if (!HTMLDialogElement.prototype.showModal) {
  const FOCUSABLE = 'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  function escapeToCancel(this: HTMLDialogElement, event: KeyboardEvent) {
    if (event.key !== 'Escape') return
    const cancel = new Event('cancel', { cancelable: true })
    this.dispatchEvent(cancel)
    if (!cancel.defaultPrevented) this.close()
  }
  HTMLDialogElement.prototype.showModal = function (this: HTMLDialogElement) {
    this.open = true
    this.addEventListener('keydown', escapeToCancel)
    this.querySelector<HTMLElement>(FOCUSABLE)?.focus()
  }
  HTMLDialogElement.prototype.close = function (this: HTMLDialogElement) {
    this.open = false
    this.removeEventListener('keydown', escapeToCancel)
    this.dispatchEvent(new Event('close'))
  }
}
