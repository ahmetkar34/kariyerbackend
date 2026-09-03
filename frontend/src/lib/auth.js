const STORAGE_KEY = 'kariyer_auth'

export function getAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setAuth(auth) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
  window.dispatchEvent(new Event('authchange'))
}

export function clearAuth() {
  localStorage.removeItem(STORAGE_KEY)
  window.dispatchEvent(new Event('authchange'))
}
