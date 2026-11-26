import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// Without Vitest globals, React Testing Library can't register its own cleanup,
// so unmount whatever a test rendered before the next one starts.
afterEach(cleanup)
