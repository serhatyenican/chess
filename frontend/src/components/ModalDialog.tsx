import { type ReactNode, useEffect, useRef } from 'react'

type ModalDialogProps = {
  /** The dialog's name for screen readers, such as "Leave this game?". */
  label: string
  /** Escape, or a click outside the content. Closing is up to the caller: it stops rendering the dialog. */
  onCancel: () => void
  /** What the dialog shows: whatever is put between <ModalDialog> and </ModalDialog>. */
  children: ReactNode
}

/**
 * A modal <dialog> around its children. The browser then dims and blocks the rest of the page, moves the focus into
 * the dialog, and turns Escape into a "cancel" event. The dialogs (the promotion picker, the confirmation) only
 * render their content inside it: React reuses behavior by wrapping components, where Java would use a base class.
 */
function ModalDialog({ label, onCancel, children }: ModalDialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)

  // A <dialog> becomes modal only through showModal(), which needs the element on the page: hence the effect. The
  // cleanup closes it again, so StrictMode's extra mount in development doesn't call showModal() on an open dialog.
  useEffect(() => {
    const dialog = dialogRef.current!
    dialog.showModal()
    return () => dialog.close()
  }, [])

  return (
    <dialog
      ref={dialogRef}
      aria-label={label}
      // Escape: the caller closes the dialog by no longer rendering it, so the browser mustn't close it itself.
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
      {children}
    </dialog>
  )
}

export default ModalDialog
