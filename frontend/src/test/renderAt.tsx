import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import App from '../App'

/**
 * Renders the whole app as if the browser had opened this URL. A MemoryRouter keeps the URL in memory instead of
 * the address bar, so a test can start anywhere and see which page a navigation leads to.
 */
export function renderAt(url: string) {
  return {
    user: userEvent.setup(),
    ...render(
      <MemoryRouter initialEntries={[url]}>
        <App />
      </MemoryRouter>,
    ),
  }
}
