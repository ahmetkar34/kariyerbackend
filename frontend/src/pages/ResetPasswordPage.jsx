import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { apiFetch } from '../lib/api'
import './Auth.css'

function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')

  function validate() {
    const newErrors = {}
    if (!password) {
      newErrors.password = 'Şifre gerekli.'
    } else if (password.length < 8) {
      newErrors.password = 'Şifre en az 8 karakter olmalı.'
    }
    if (confirmPassword !== password) {
      newErrors.confirmPassword = 'Şifreler eşleşmiyor.'
    }
    return newErrors
  }

  function handleSubmit(e) {
    e.preventDefault()
    const newErrors = validate()
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) return

    setApiError('')
    setLoading(true)
    apiFetch('/api/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ token, newPassword: password }),
    })
      .then(() => navigate('/login'))
      .catch((err) => setApiError(err.message || 'Şifre sıfırlanamadı.'))
      .finally(() => setLoading(false))
  }

  if (!token) {
    return (
      <div className="auth-page">
        <div className="card auth-card">
          <div className="auth-header">
            <h1>Şifre Sıfırlama</h1>
          </div>
          <div className="form-error" role="alert">
            Sıfırlama bağlantısı eksik veya geçersiz.
          </div>
          <Link to="/sifremi-unuttum" className="btn btn-primary btn-block">
            Yeni Bağlantı İste
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <div className="auth-header">
          <h1>Yeni Şifre Belirle</h1>
        </div>

        {apiError && (
          <div className="form-error" role="alert">
            {apiError}
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="password">Yeni Şifre</label>
            <input
              id="password"
              type="password"
              placeholder="En az 8 karakter"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={errors.password ? 'invalid' : ''}
            />
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>
          <div className="form-field">
            <label htmlFor="confirmPassword">Şifre Tekrar</label>
            <input
              id="confirmPassword"
              type="password"
              placeholder="Şifrenizi tekrar girin"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className={errors.confirmPassword ? 'invalid' : ''}
            />
            {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Kaydediliyor...' : 'Şifreyi Güncelle'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default ResetPasswordPage
