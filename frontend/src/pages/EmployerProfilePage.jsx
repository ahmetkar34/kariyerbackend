import { useEffect, useState } from 'react'
import { getEmployerProfile, updateEmployerProfile } from '../lib/employerProfile'
import { isEmployerRole } from '../lib/roles'
import { useRequireAuth } from '../lib/useRequireAuth'
import './Employer.css'

function EmployerProfilePage() {
  const auth = useRequireAuth({ allowRole: isEmployerRole })
  const [companyName, setCompanyName] = useState('')
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!auth) return
    getEmployerProfile()
      .then((profile) => setCompanyName(profile.companyName || ''))
      .catch(() => {})
  }, [auth])

  if (!auth) {
    return null
  }

  function handleSubmit(e) {
    e.preventDefault()
    setError('')
    updateEmployerProfile(companyName)
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
            <p>İlanlarınızda görünen şirket bilgilerini güncelleyin.</p>
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
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              placeholder="Şirketinizin adı"
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
