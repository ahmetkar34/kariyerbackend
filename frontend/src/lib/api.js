import { getAuth } from './auth'

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

export async function apiFetch(path, options = {}) {
  const auth = getAuth()
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  }
  if (auth?.token) {
    headers.Authorization = `Bearer ${auth.token}`
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })

  if (!response.ok) {
    const data = await response.json().catch(() => null)
    throw new Error(data?.message || 'İşlem başarısız oldu.')
  }

  const text = await response.text()
  return text ? JSON.parse(text) : null
}
