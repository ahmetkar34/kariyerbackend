import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { apiFetch } from './api'

function mockFetchOnce(response) {
  global.fetch = vi.fn().mockResolvedValue(response)
}

describe('apiFetch', () => {
  beforeEach(() => {
    localStorage.clear()
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/'
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // Auth is carried by an httpOnly cookie the browser attaches automatically - the
  // client never sees the JWT, so the only thing apiFetch controls itself is asking
  // the browser to send cookies at all, and echoing back the (JS-readable) CSRF cookie.
  it('sends requests with credentials included so the auth cookie is attached', async () => {
    mockFetchOnce({ ok: true, text: () => Promise.resolve('') })

    await apiFetch('/api/jobs')

    const [, options] = global.fetch.mock.calls[0]
    expect(options.credentials).toBe('include')
  })

  it('echoes the XSRF-TOKEN cookie back as a request header when present', async () => {
    document.cookie = 'XSRF-TOKEN=abc123'
    mockFetchOnce({ ok: true, text: () => Promise.resolve('') })

    await apiFetch('/api/jobs', { method: 'POST' })

    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers['X-XSRF-TOKEN']).toBe('abc123')
  })

  it('omits the CSRF header when no token cookie is set yet', async () => {
    mockFetchOnce({ ok: true, text: () => Promise.resolve('') })

    await apiFetch('/api/jobs')

    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers['X-XSRF-TOKEN']).toBeUndefined()
  })

  // Regression test for a bug fixed in a previous commit: an empty-body 200
  // response (e.g. from a DELETE or POST returning 204/200 with no content)
  // must not throw when JSON-parsed.
  it('returns null for an empty-body success response', async () => {
    mockFetchOnce({ ok: true, text: () => Promise.resolve('') })

    await expect(apiFetch('/api/jobs/1')).resolves.toBeNull()
  })

  it('parses a JSON body on success', async () => {
    mockFetchOnce({ ok: true, text: () => Promise.resolve('{"id":1}') })

    await expect(apiFetch('/api/jobs/1')).resolves.toEqual({ id: 1 })
  })

  it('throws the server-provided message on failure', async () => {
    mockFetchOnce({ ok: false, json: () => Promise.resolve({ message: 'Bu ilana zaten başvurdunuz' }) })

    await expect(apiFetch('/api/jobs/1/applications', { method: 'POST' })).rejects.toThrow(
      'Bu ilana zaten başvurdunuz',
    )
  })

  it('falls back to a generic message when the error body is not JSON', async () => {
    mockFetchOnce({ ok: false, json: () => Promise.reject(new Error('not json')) })

    await expect(apiFetch('/api/jobs')).rejects.toThrow('İşlem başarısız oldu.')
  })
})
