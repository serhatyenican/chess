import { Link } from 'react-router'

/** Any URL that no route matches. */
function NotFoundPage() {
  return (
    <section>
      <h2>Page not found</h2>
      <Link to="/">Go to the start page</Link>
    </section>
  )
}

export default NotFoundPage
