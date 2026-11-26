import { useEffect, useRef } from 'react'

type ConfirmDialogProps = {
  /** The question, such as "Leave this game?". Also the dialog's name for screen readers. */
  title: string
  /** More about what happens, such as "It can't be undone." */
  message: string
  /** The label of the button that confirms, such as "Leave game". */
  confirmLabel: string
  onConfirm: () => void
  /** The Cancel button, Escape, or a click outside the dialog. */
  onCancel: () => void
}

/**
 * Asks to confirm an action that can't be undone. A modal <dialog> like the promotion picker: the browser blocks the
 * rest of the page and turns Escape into a "cancel" event. Cancel has the focus, so pressing Enter by accident does
 * nothing harmful.
 */
function ConfirmDialog({ title, message, confirmLabel, onConfirm, onCancel }: ConfirmDialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = dialogRef.current!
    dialog.showModal()
    return () => dialog.close()
  }, [])

  return (
    <dialog
      ref={dialogRef}
      aria-label={title}
      onCancel={(event) => {
        event.preventDefault()
        onCancel()
      }}
      // The dialog element itself is only hit on its backdrop, outside the content.
      onClick={(event) => {
        if (event.target === event.currentTarget) onCancel()
      }}
      style={{ padding: 0, border: 'none', borderRadius: 8 }}
    >
      <div style={{ padding: 16 }}>
        <p>
          <strong>{title}</strong>
        </p>
        <p>{message}</p>
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button type="button" onClick={onCancel} autoFocus>
            Cancel
          </button>
          <button type="button" onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </dialog>
  )
}

export default ConfirmDialog
