import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { apiFetch } from '../lib/api'
import './Auth.css'

function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const requestedTokenRef = useRef(null)

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setError('Doğrulama bağlantısı eksik.')
      return
    }
    // React StrictMode runs effects twice in development; without this guard
    // the single-use token would be consumed by the first call and the
    // second (identical) call would fail spuriously.
    if (requestedTokenRef.current === token) {
      return
    }
    requestedTokenRef.current = token

    apiFetch('/api/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify({ token }),
    })
      .then(() => setStatus('success'))
      .catch((err) => {
        setStatus('error')
        setError(err.message || 'Doğrulama başarısız oldu.')
      })
  }, [token])

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <div className="auth-header">
          <h1>E-posta Doğrulama</h1>
        </div>

        {status === 'loading' && <p>Doğrulanıyor...</p>}

        {status === 'success' && (
          <>
            <div className="form-success" role="status">
              E-posta adresiniz doğrulandı! Artık giriş yapabilirsiniz.
            </div>
            <Link to="/login" className="btn btn-primary btn-block">
              Giriş Yap
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <div className="form-error" role="alert">
              {error}
            </div>
            <Link to="/login" className="btn btn-outline btn-block">
              Girişe Dön
            </Link>
          </>
        )}
      </div>
    </div>
  )
}

export default VerifyEmailPage
