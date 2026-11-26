/**
 * Every way a call to the game API can fail, so a component needs to catch only this one type.
 * `status` is the HTTP status, or null when no response arrived at all.
 */
export class ApiError extends Error {
  readonly status: number | null
  readonly title: string
  readonly detail: string

  constructor(status: number | null, title: string, detail: string, options?: ErrorOptions) {
    super(`${title}: ${detail}`, options)
    this.name = 'ApiError'
    this.status = status
    this.title = title
    this.detail = detail
  }

  /** The server answered with an error status. Spring sends a ProblemDetail, whose title and detail are kept. */
  static async fromResponse(response: Response): Promise<ApiError> {
    const problem = await response.json().catch(() => null) as Partial<ProblemDetail> | null
    // A gateway error without a problem detail comes from a proxy in front of Spring, such as Vite's dev server
    // when Spring isn't running (502). It means the same as no response, but keeps its real status.
    if (!problem && GATEWAY_ERRORS.includes(response.status)) {
      return new ApiError(response.status, UNREACHABLE_TITLE, UNREACHABLE_DETAIL)
    }
    return new ApiError(
      response.status,
      problem?.title ?? (response.statusText || 'Request failed'),
      problem?.detail ?? `The server answered with status ${response.status}.`,
    )
  }

  /** No response arrived: the server is down, or the network is gone. */
  static network(cause: unknown): ApiError {
    return new ApiError(null, UNREACHABLE_TITLE, UNREACHABLE_DETAIL, { cause })
  }

  /** The server answered with success, but the body isn't JSON. */
  static invalidBody(response: Response, cause: unknown): ApiError {
    return new ApiError(response.status, 'Invalid response', 'The server sent a response that is not JSON.', { cause })
  }
}

const UNREACHABLE_TITLE = 'Network error'
const UNREACHABLE_DETAIL = 'Could not reach the server. Check that it is running.'

/** Bad Gateway, Service Unavailable and Gateway Timeout: a proxy couldn't get an answer from the server behind it. */
const GATEWAY_ERRORS = [502, 503, 504]

/** The fields of Spring's ProblemDetail (RFC 9457) that we use. */
type ProblemDetail = {
  title: string
  detail: string
}
