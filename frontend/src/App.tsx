import { Route, Routes } from 'react-router'
import GamePage from './pages/GamePage'
import NotFoundPage from './pages/NotFoundPage'
import StartPage from './pages/StartPage'

// The layout and the routes. The router itself (BrowserRouter) is set up in main.tsx, so tests can render the app
// in a MemoryRouter at any URL instead.
function App() {
  return (
    <>
      <header>
        <h1>Chess</h1>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<StartPage />} />
          <Route path="/games/:id" element={<GamePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  )
}

export default App
