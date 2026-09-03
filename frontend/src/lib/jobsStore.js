import { apiFetch } from './api'

export function getAllJobs() {
  return apiFetch('/api/jobs')
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

export function applyToJob(jobId) {
  return apiFetch(`/api/jobs/${jobId}/applications`, {
    method: 'POST',
  })
}

export function getApplicants(jobId) {
  return apiFetch(`/api/jobs/${jobId}/applications`)
}
