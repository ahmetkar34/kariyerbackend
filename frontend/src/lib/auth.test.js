import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('./api', () => ({
  apiFetch: vi.fn(),
}))

import { apiFetch } from './api'
import { clearAuth, getAuth, logout, setAuth } from './auth'

describe('auth storage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('setAuth stores the user and getAuth reads it back', () => {
    setAuth({ id: 1, role: 'USER' })

    expect(getAuth()).toEqual({ user: { id: 1, role: 'USER' } })
  })

  it('clearAuth removes the stored session', () => {
    setAuth({ id: 1 })

    clearAuth()

    expect(getAuth()).toBeNull()
  })

  it('getAuth returns null when storage is empty or corrupted', () => {
    expect(getAuth()).toBeNull()

    localStorage.setItem('kariyer_auth', 'not-json')

    expect(getAuth()).toBeNull()
  })
})

describe('logout', () => {
  beforeEach(() => {
    localStorage.clear()
    apiFetch.mockReset()
  })

  // The JWT lives only in an httpOnly cookie, which JS cannot clear - logout must be
  // a real request to the backend, not just a local storage wipe.
  it('calls the backend logout endpoint and clears the local session', async () => {
    setAuth({ id: 1 })
    apiFetch.mockResolvedValue(null)

    await logout()

    expect(apiFetch).toHaveBeenCalledWith('/api/auth/logout', { method: 'POST' })
    expect(getAuth()).toBeNull()
  })

  it('still clears the local session if the backend call fails', async () => {
    setAuth({ id: 1 })
    apiFetch.mockRejectedValue(new Error('network error'))

    await logout()

    expect(getAuth()).toBeNull()
  })
})
