import ModalDialog from './ModalDialog'

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
 * Asks to confirm an action that can't be undone. Cancel has the focus, so pressing Enter by accident does nothing
 * harmful.
 */
function ConfirmDialog({ title, message, confirmLabel, onConfirm, onCancel }: ConfirmDialogProps) {
  return (
    <ModalDialog label={title} onCancel={onCancel}>
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
    </ModalDialog>
  )
}

export default ConfirmDialog
