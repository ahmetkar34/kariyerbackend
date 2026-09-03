import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { setAuth } from '../lib/auth'
import './Auth.css'

function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '', remember: false })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')
  const navigate = useNavigate()

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  function validate() {
    const newErrors = {}
    if (!form.email.trim()) {
      newErrors.email = 'E-posta adresi gerekli.'
    } else if (!/^\S+@\S+\.\S+$/.test(form.email)) {
      newErrors.email = 'Geçerli bir e-posta adresi girin.'
    }
    if (!form.password) {
      newErrors.password = 'Şifre gerekli.'
    }
    return newErrors
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const newErrors = validate()
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) {
      return
    }

    setApiError('')
    setLoading(true)
    try {
      const response = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: form.email, password: form.password }),
      })

      if (!response.ok) {
        throw new Error('E-posta veya şifre hatalı.')
      }

      const data = await response.json()
      setAuth({
        token: data.token,
        user: {
          id: data.id,
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          role: data.role,
          companyName: data.companyName,
        },
      })
      navigate(data.role?.toLowerCase().includes('employer') ? '/isveren' : '/profile')
    } catch (err) {
      setApiError(err.message || 'Sunucuya bağlanılamadı.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <div className="auth-header">
          <h1>Giriş Yap</h1>
          <p>Hesabınıza giriş yaparak iş aramaya devam edin.</p>
        </div>

        {apiError && (
          <div className="form-error" role="alert">
            {apiError}
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="email">E-posta</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="ornek@eposta.com"
              value={form.email}
              onChange={handleChange}
              className={errors.email ? 'invalid' : ''}
            />
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>

          <div className="form-field">
            <label htmlFor="password">Şifre</label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              className={errors.password ? 'invalid' : ''}
            />
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>

          <div className="form-row">
            <label className="checkbox-field">
              <input
                type="checkbox"
                name="remember"
                checked={form.remember}
                onChange={handleChange}
              />
              Beni hatırla
            </label>
            <a href="#!">Şifremi unuttum</a>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
          </button>
        </form>

        <p className="auth-footer">
          Hesabınız yok mu? <Link to="/register">Kayıt Ol</Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
