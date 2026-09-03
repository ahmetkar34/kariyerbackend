import { useState } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../lib/api'
import './Auth.css'

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')

  function handleSubmit(e) {
    e.preventDefault()
    setApiError('')
    setLoading(true)
    apiFetch('/api/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email }),
    })
      .then(() => setSubmitted(true))
      .catch((err) => setApiError(err.message || 'Sunucuya bağlanılamadı.'))
      .finally(() => setLoading(false))
  }

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <div className="auth-header">
          <h1>Şifremi Unuttum</h1>
          <p>E-posta adresinizi girin, size bir sıfırlama bağlantısı gönderelim.</p>
        </div>

        {submitted ? (
          <div className="form-success" role="status">
            Eğer bu e-posta adresi kayıtlıysa, bir sıfırlama bağlantısı gönderildi.
          </div>
        ) : (
          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            {apiError && (
              <div className="form-error" role="alert">
                {apiError}
              </div>
            )}
            <div className="form-field">
              <label htmlFor="email">E-posta</label>
              <input
                id="email"
                type="email"
                placeholder="ornek@eposta.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Gönderiliyor...' : 'Sıfırlama Bağlantısı Gönder'}
            </button>
          </form>
        )}

        <p className="auth-footer">
          <Link to="/login">Girişe Dön</Link>
        </p>
      </div>
    </div>
  )
}

export default ForgotPasswordPage
