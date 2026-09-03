import { apiFetch } from './api'

export function getAdminStats() {
  return apiFetch('/api/admin/stats')
}

export function getAdminUsers({ keyword = '', page = 0, size = 20 } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.set('keyword', keyword)
  params.set('page', page)
  params.set('size', size)
  return apiFetch(`/api/admin/users?${params.toString()}`)
}

export function deleteAdminUser(id) {
  return apiFetch(`/api/admin/users/${id}`, { method: 'DELETE' })
}

export function deleteAdminJob(id) {
  return apiFetch(`/api/admin/jobs/${id}`, { method: 'DELETE' })
}
