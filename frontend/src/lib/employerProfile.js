import { apiFetch } from './api'

export function getEmployerProfile() {
  return apiFetch('/api/employer/profile')
}

export function updateEmployerProfile({ companyName, website, logoUrl, description }) {
  return apiFetch('/api/employer/profile', {
    method: 'PUT',
    body: JSON.stringify({ companyName, website, logoUrl, description }),
  })
}

export function getCompanyProfile(employerId) {
  return apiFetch(`/api/companies/${employerId}`)
}
