import { Component, type ErrorInfo, type ReactNode } from 'react'
import { Link } from 'react-router'

type ErrorBoundaryProps = {
  children: ReactNode
}

type ErrorBoundaryState = {
  failed: boolean
}

/**
 * Catches errors thrown while its children render, and shows an error page instead. Without it, React unmounts the
 * whole app on such an error and the page goes blank. Such an error is always a bug of ours, e.g. a FEN without the
 * king that is in check (`kingSquare`).
 * <p>
 * A class, because React supports error boundaries only as class components, even in React 19. It doesn't catch
 * errors in event handlers or promises (a failed move, say): those don't happen while rendering, and the pages show
 * them as messages themselves.
 * <p>
 * It doesn't reset itself: App gives it the URL's path as its key, so navigating away creates a fresh one.
 */
class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { failed: false }

  /** Called by React while rendering, after a child has thrown: the state to render with instead. */
  static getDerivedStateFromError(): ErrorBoundaryState {
    return { failed: true }
  }

  /** Called by React afterwards, with the error: keep it in the browser console, with where it happened. */
  componentDidCatch(error: unknown, info: ErrorInfo) {
    console.error(error, info.componentStack)
  }

  render() {
    if (this.state.failed) {
      return (
        <section role="alert">
          <h2>Something went wrong</h2>
          <p>This page could not be shown. The details are in the browser console.</p>
          <Link to="/">Go to the start page</Link>
        </section>
      )
    }
    return this.props.children
  }
}

export default ErrorBoundary
