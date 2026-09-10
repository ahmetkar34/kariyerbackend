import { useEffect, useState } from 'react'
import { createAlert, deleteAlert, getMyAlerts } from '../lib/alerts'
import { isEmployerRole } from '../lib/roles'
import { useRequireAuth } from '../lib/useRequireAuth'
import './Employer.css'

const jobTypes = ['Tam Zamanlı', 'Yarı Zamanlı', 'Staj', 'Sözleşmeli']

const emptyForm = { keyword: '', location: '', type: '', remote: false }

function describeAlert(alert) {
  const parts = []
  if (alert.keyword) parts.push(`"${alert.keyword}"`)
  if (alert.location) parts.push(alert.location)
  if (alert.type) parts.push(alert.type)
  if (alert.remote) parts.push('Uzaktan')
  return parts.length > 0 ? parts.join(' · ') : 'Tüm ilanlar'
}

function MyAlertsPage() {
  const auth = useRequireAuth({ allowRole: (role) => !isEmployerRole(role), redirectTo: '/isveren' })
  const [alerts, setAlerts] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!auth) return
    getMyAlerts()
      .then(setAlerts)
      .catch(() => setAlerts([]))
  }, [auth])

  if (!auth) {
    return null
  }

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    setError('')
    createAlert(form)
      .then((created) => {
        setAlerts((prev) => [created, ...prev])
        setForm(emptyForm)
      })
      .catch((err) => setError(err.message || 'Uyarı oluşturulamadı.'))
  }

  function handleDelete(id) {
    deleteAlert(id).then(() => setAlerts((prev) => prev.filter((a) => a.id !== id)))
  }

  return (
    <div className="employer-page">
      <div className="container">
        <div className="employer-header">
          <div>
            <h1>İlan Uyarılarım</h1>
            <p>Kriterlerinize uyan yeni bir ilan yayınlandığında e-posta ile haberdar olun.</p>
          </div>
        </div>

        <form className="card employer-form" onSubmit={handleSubmit} noValidate>
          {error && (
            <div className="form-error" role="alert">
              {error}
            </div>
          )}
          <div className="form-field">
            <label htmlFor="keyword">Anahtar Kelime</label>
            <input
              id="keyword"
              name="keyword"
              value={form.keyword}
              onChange={handleChange}
              placeholder="Örn. Java, Frontend Developer"
            />
          </div>
          <div className="form-field">
            <label htmlFor="location">Konum</label>
            <input
              id="location"
              name="location"
              value={form.location}
              onChange={handleChange}
              placeholder="Örn. İstanbul"
            />
          </div>
          <div className="form-field">
            <label htmlFor="type">Çalışma Şekli</label>
            <select id="type" name="type" value={form.type} onChange={handleChange}>
              <option value="">Farketmez</option>
              {jobTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>
          <div className="form-field">
            <label className="checkbox-field">
              <input type="checkbox" name="remote" checked={form.remote} onChange={handleChange} />
              Sadece uzaktan ilanlar
            </label>
          </div>
          <div className="employer-form-actions">
            <button type="submit" className="btn btn-primary">
              Uyarı Oluştur
            </button>
          </div>
        </form>

        {alerts.length === 0 ? (
          <div className="card employer-empty">
            <p className="empty-hint">Henüz bir ilan uyarısı oluşturmadınız.</p>
          </div>
        ) : (
          <div className="employer-job-list">
            {alerts.map((alert) => (
              <div className="card employer-job-item" key={alert.id}>
                <div>
                  <h3>{describeAlert(alert)}</h3>
                </div>
                <div className="employer-job-actions">
                  <button type="button" className="remove-btn" onClick={() => handleDelete(alert.id)}>
                    Sil
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MyAlertsPage
