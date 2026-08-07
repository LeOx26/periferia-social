import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { z } from 'zod'
import { ApiError, apiFetch } from './httpClient'

const jsonResponse = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })

describe('apiFetch', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('sends the bearer token when one is provided', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }))

    await apiFetch('http://api', '/thing', { token: 'un.token' })

    const [, init] = vi.mocked(fetch).mock.calls[0]!
    expect((init?.headers as Record<string, string>).Authorization).toBe('Bearer un.token')
  })

  it('omits the Authorization header when there is no token', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }))

    await apiFetch('http://api', '/thing')

    const [, init] = vi.mocked(fetch).mock.calls[0]!
    expect((init?.headers as Record<string, string>).Authorization).toBeUndefined()
  })

  it('always sends a correlation id so a failure can be traced to a server log', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ ok: true }))

    await apiFetch('http://api', '/thing')

    const [, init] = vi.mocked(fetch).mock.calls[0]!
    expect((init?.headers as Record<string, string>)['X-Correlation-Id']).toBeTruthy()
  })

  it('turns an RFC 7807 problem response into a typed ApiError', async () => {
    vi.mocked(fetch).mockResolvedValue(
      jsonResponse(
        {
          type: 'https://periferia.social/errors/self-like-not-allowed',
          title: 'No puedes dar like a tu propia publicación',
          status: 409,
          detail: 'Un autor no puede dar like a su propia publicación.',
          correlationId: 'abc-123',
        },
        409,
      ),
    )

    const failure = await apiFetch('http://api', '/thing').catch((e) => e)

    expect(failure).toBeInstanceOf(ApiError)
    expect(failure.status).toBe(409)
    expect(failure.title).toBe('No puedes dar like a tu propia publicación')
    expect(failure.correlationId).toBe('abc-123')
  })

  it('still produces an ApiError when the server returns a non-JSON body', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response('502 Bad Gateway', { status: 502 }))

    const failure = await apiFetch('http://api', '/thing').catch((e) => e)

    expect(failure).toBeInstanceOf(ApiError)
    expect(failure.status).toBe(502)
  })

  it('wraps a network failure so callers only ever handle ApiError', async () => {
    vi.mocked(fetch).mockRejectedValue(new TypeError('Failed to fetch'))

    const failure = await apiFetch('http://api', '/thing').catch((e) => e)

    expect(failure).toBeInstanceOf(ApiError)
    expect(failure.status).toBe(0)
    expect(failure.title).toMatch(/conexión/i)
  })

  it('validates the response against the schema when one is given', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ unexpected: 'shape' }))

    const failure = await apiFetch('http://api', '/thing', {
      schema: z.object({ id: z.string() }),
    }).catch((e) => e)

    expect(failure).toBeInstanceOf(ApiError)
    expect(failure.title).toMatch(/inesperada/i)
  })

  it('returns the parsed body when the schema matches', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ id: 'ok' }))

    const result = await apiFetch('http://api', '/thing', {
      schema: z.object({ id: z.string() }),
    })

    expect(result).toEqual({ id: 'ok' })
  })
})
