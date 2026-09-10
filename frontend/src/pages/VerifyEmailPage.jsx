import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { apiFetch } from '../lib/api'
import { setAuth } from '../lib/auth'
import './Auth.css'

function VerifyEmailPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [email, setEmail] = useState(location.state?.email || '')
  const [code, setCode] = useState('')
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')
  const [resent, setResent] = useState(false)

  function validate() {
    const newErrors = {}
    if (!email.trim()) {
      newErrors.email = 'E-posta adresi gerekli.'
    }
    if (!code.trim()) {
      newErrors.code = 'Doğrulama kodu gerekli.'
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
    apiFetch('/api/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify({ email, code: code.trim() }),
    })
      .then((data) => {
        setAuth({
          id: data.id,
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          role: data.role,
          companyName: data.companyName,
        })
        if (data.role === 'ADMIN') {
          navigate('/admin')
        } else if (data.role?.toLowerCase().includes('employer')) {
          navigate('/isveren')
        } else {
          navigate('/profile')
        }
      })
      .catch((err) => setApiError(err.message || 'Doğrulama başarısız oldu.'))
      .finally(() => setLoading(false))
  }

  function handleResend() {
    if (!email.trim()) {
      setErrors({ email: 'Kodu tekrar göndermek için e-posta adresinizi girin.' })
      return
    }
    apiFetch('/api/auth/resend-verification', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }).then(() => setResent(true))
  }

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <div className="auth-header">
          <h1>E-posta Doğrulama</h1>
          <p>E-postanıza gönderdiğimiz 6 haneli kodu girin.</p>
        </div>

        {apiError && (
          <div className="form-error" role="alert">
            {apiError}
          </div>
        )}
        {resent && (
          <div className="form-success" role="status">
            Doğrulama kodu tekrar gönderildi.
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="email">E-posta</label>
            <input
              id="email"
              type="email"
              placeholder="ornek@eposta.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={errors.email ? 'invalid' : ''}
            />
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>

          <div className="form-field">
            <label htmlFor="code">Doğrulama Kodu</label>
            <input
              id="code"
              type="text"
              inputMode="numeric"
              placeholder="123456"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              className={errors.code ? 'invalid' : ''}
            />
            {errors.code && <span className="field-error">{errors.code}</span>}
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Doğrulanıyor...' : 'Doğrula'}
          </button>
        </form>

        <p className="auth-footer">
          Kod gelmedi mi?{' '}
          <button type="button" className="link-btn" onClick={handleResend}>
            Tekrar gönder
          </button>
        </p>
      </div>
    </div>
  )
}

export default VerifyEmailPage
