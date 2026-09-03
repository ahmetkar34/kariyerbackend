import { apiFetch } from './api'

export function getEmployerProfile() {
  return apiFetch('/api/employer/profile')
}

export function updateEmployerProfile(companyName) {
  return apiFetch('/api/employer/profile', {
    method: 'PUT',
    body: JSON.stringify({ companyName }),
  })
}
