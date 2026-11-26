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
    return new ApiError(
      response.status,
      problem?.title ?? (response.statusText || 'Request failed'),
      problem?.detail ?? `The server answered with status ${response.status}.`,
    )
  }

  /** No response arrived: the server is down, or the network is gone. */
  static network(cause: unknown): ApiError {
    return new ApiError(null, 'Network error', 'Could not reach the server. Check that it is running.', { cause })
  }

  /** The server answered with success, but the body isn't JSON. */
  static invalidBody(response: Response, cause: unknown): ApiError {
    return new ApiError(response.status, 'Invalid response', 'The server sent a response that is not JSON.', { cause })
  }
}

/** The fields of Spring's ProblemDetail (RFC 9457) that we use. */
type ProblemDetail = {
  title: string
  detail: string
}
