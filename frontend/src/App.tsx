import { Route, Routes, useLocation } from 'react-router'
import ErrorBoundary from './components/ErrorBoundary'
import GamePage from './pages/GamePage'
import NotFoundPage from './pages/NotFoundPage'
import StartPage from './pages/StartPage'

// The layout and the routes. The router itself (BrowserRouter) is set up in main.tsx, so tests can render the app
// in a MemoryRouter at any URL instead.
function App() {
  const location = useLocation()

  return (
    <>
      <header>
        <h1>Chess</h1>
      </header>
      <main>
        {/* A render error shows an error page here, below the header. Keyed by the path, so a navigation (the
            error page's link, or the browser's back button) starts with a fresh boundary instead of the error. */}
        <ErrorBoundary key={location.pathname}>
          <Routes>
            <Route path="/" element={<StartPage />} />
            <Route path="/games/:id" element={<GamePage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </ErrorBoundary>
      </main>
    </>
  )
}

export default App
