import { apiFetch } from './api'

export function getMyApplications() {
  return apiFetch('/api/applications/me')
}
