import { apiFetch } from './api'

export function getAllJobs({ keyword = '', location = '', page = 0, size = 20 } = {}) {
  const params = new URLSearchParams()
  if (keyword) params.set('keyword', keyword)
  if (location) params.set('location', location)
  params.set('page', page)
  params.set('size', size)
  return apiFetch(`/api/jobs?${params.toString()}`)
}

export function getJobById(id) {
  return apiFetch(`/api/jobs/${id}`)
}

export function getMyJobs() {
  return apiFetch('/api/jobs/me')
}

export function createJob(data) {
  return apiFetch('/api/jobs', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function updateJob(id, data) {
  return apiFetch(`/api/jobs/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

export function deleteJob(id) {
  return apiFetch(`/api/jobs/${id}`, {
    method: 'DELETE',
  })
}

export function getApplicationStatus(jobId) {
  return apiFetch(`/api/jobs/${jobId}/applications/me`)
}

export function applyToJob(jobId, coverLetter) {
  return apiFetch(`/api/jobs/${jobId}/applications`, {
    method: 'POST',
    body: JSON.stringify({ coverLetter }),
  })
}

export function getApplicants(jobId) {
  return apiFetch(`/api/jobs/${jobId}/applications`)
}

export function updateApplicationStatus(jobId, applicationId, status) {
  return apiFetch(`/api/jobs/${jobId}/applications/${applicationId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}

export function getFavoriteStatus(jobId) {
  return apiFetch(`/api/jobs/${jobId}/favorite`)
}

export function saveFavorite(jobId) {
  return apiFetch(`/api/jobs/${jobId}/favorite`, {
    method: 'POST',
  })
}

export function removeFavorite(jobId) {
  return apiFetch(`/api/jobs/${jobId}/favorite`, {
    method: 'DELETE',
  })
}

export function getMyFavorites() {
  return apiFetch('/api/favorites/me')
}
