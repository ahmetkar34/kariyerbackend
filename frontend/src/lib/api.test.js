import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { apiFetch } from './api'

function mockFetchOnce(response) {
  global.fetch = vi.fn().mockResolvedValue(response)
}

describe('apiFetch', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('attaches the Authorization header when a session is stored', async () => {
    localStorage.setItem('kariyer_auth', JSON.stringify({ token: 'abc123', user: { id: 1 } }))
    mockFetchOnce({ ok: true, text: () => Promise.resolve('') })

    await apiFetch('/api/jobs')

    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers.Authorization).toBe('Bearer abc123')
  })

  it('omits the Authorization header when unauthenticated', async () => {
    mockFetchOnce({ ok: true, text: () => Promise.resolve('') })

    await apiFetch('/api/jobs')

    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers.Authorization).toBeUndefined()
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
