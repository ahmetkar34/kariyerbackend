import { apiFetch } from './api'

export function getProfile() {
  return apiFetch('/api/profile/me')
}

export function saveProfile(profile) {
  return apiFetch('/api/profile/me', {
    method: 'PUT',
    body: JSON.stringify({
      phone: profile.phone,
      title: profile.title,
      summary: profile.summary,
      education: profile.education,
      certificates: profile.certificates,
    }),
  })
}
