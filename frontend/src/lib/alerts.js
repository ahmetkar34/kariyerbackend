import { apiFetch } from './api'

export function getMyAlerts() {
  return apiFetch('/api/alerts/me')
}

export function createAlert(data) {
  return apiFetch('/api/alerts', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteAlert(id) {
  return apiFetch(`/api/alerts/${id}`, {
    method: 'DELETE',
  })
}
