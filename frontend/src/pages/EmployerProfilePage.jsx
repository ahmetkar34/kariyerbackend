import { useEffect, useState } from 'react'
import { getEmployerProfile, updateEmployerProfile } from '../lib/employerProfile'
import { isEmployerRole } from '../lib/roles'
import { useRequireAuth } from '../lib/useRequireAuth'
import './Employer.css'

const emptyForm = {
  companyName: '',
  website: '',
  logoUrl: '',
  description: '',
}

function EmployerProfilePage() {
  const auth = useRequireAuth({ allowRole: isEmployerRole })
  const [form, setForm] = useState(emptyForm)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!auth) return
    getEmployerProfile()
      .then((profile) =>
        setForm({
          companyName: profile.companyName || '',
          website: profile.website || '',
          logoUrl: profile.logoUrl || '',
          description: profile.description || '',
        }),
      )
      .catch(() => {})
  }, [auth])

  if (!auth) {
    return null
  }

  function handleChange(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    setError('')
    updateEmployerProfile(form)
      .then(() => {
        setSaved(true)
        setTimeout(() => setSaved(false), 2000)
      })
      .catch((err) => setError(err.message || 'Kaydedilemedi.'))
  }

  return (
    <div className="employer-page">
      <div className="container">
        <form className="card employer-form" onSubmit={handleSubmit} noValidate>
          <div className="employer-form-header">
            <h1>Şirket Profili</h1>
            <p>İlanlarınızda ve şirket sayfanızda görünen bilgileri güncelleyin.</p>
          </div>

          {error && (
            <div className="form-error" role="alert">
              {error}
            </div>
          )}
          {saved && (
            <div className="form-success" role="status">
              Kaydedildi.
            </div>
          )}

          <div className="form-field">
            <label htmlFor="companyName">Şirket Adı</label>
            <input
              id="companyName"
              name="companyName"
              value={form.companyName}
              onChange={handleChange}
              placeholder="Şirketinizin adı"
            />
          </div>

          <div className="form-field">
            <label htmlFor="website">Website</label>
            <input
              id="website"
              name="website"
              value={form.website}
              onChange={handleChange}
              placeholder="https://sirketiniz.com"
            />
          </div>

          <div className="form-field">
            <label htmlFor="logoUrl">Logo Adresi</label>
            <input
              id="logoUrl"
              name="logoUrl"
              value={form.logoUrl}
              onChange={handleChange}
              placeholder="https://sirketiniz.com/logo.png"
            />
            <span className="employer-form-hint">
              Logonuzu bir görsel barındırma servisine yükleyip adresini buraya yapıştırın.
            </span>
          </div>

          <div className="form-field">
            <label htmlFor="description">Şirket Hakkında</label>
            <textarea
              id="description"
              name="description"
              rows={4}
              value={form.description}
              onChange={handleChange}
              placeholder="Şirketinizi adaylara kısaca tanıtın..."
            />
          </div>

          <div className="employer-form-actions">
            <button type="submit" className="btn btn-primary">
              Kaydet
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EmployerProfilePage
