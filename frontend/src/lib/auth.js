import { apiFetch } from './api'

const STORAGE_KEY = 'kariyer_auth'

// The JWT itself lives only in an httpOnly cookie set by the backend - it is never
// readable from JS, so nothing here holds a token. This storage is just a client-side
// cache of the signed-in user's profile for rendering (Navbar, route guards, etc.).
export function getAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setAuth(user) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ user }))
  window.dispatchEvent(new Event('authchange'))
}

export function clearAuth() {
  localStorage.removeItem(STORAGE_KEY)
  window.dispatchEvent(new Event('authchange'))
}

// Only the backend can clear the httpOnly auth cookie, so logging out requires this
// round-trip - clearing local storage alone would leave the cookie (and the session
// it grants) active.
export async function logout() {
  try {
    await apiFetch('/api/auth/logout', { method: 'POST' })
  } catch {
    // Clear the local session regardless - the user still expects to be logged out
    // even if the backend call to clear the httpOnly cookie couldn't be reached.
  } finally {
    clearAuth()
  }
}
