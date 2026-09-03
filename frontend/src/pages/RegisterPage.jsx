import { useState } from 'react'
import { Link } from 'react-router-dom'
import './Auth.css'

function RegisterPage() {
  const [form, setForm] = useState({
    accountType: 'candidate',
    firstName: '',
    lastName: '',
    companyName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false,
  })
  const [errors, setErrors] = useState({})
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  function validate() {
    const newErrors = {}
    if (!form.firstName.trim()) {
      newErrors.firstName = 'Ad gerekli.'
    }
    if (!form.lastName.trim()) {
      newErrors.lastName = 'Soyad gerekli.'
    }
    if (form.accountType === 'employer' && !form.companyName.trim()) {
      newErrors.companyName = 'Şirket adı gerekli.'
    }
    if (!form.email.trim()) {
      newErrors.email = 'E-posta adresi gerekli.'
    } else if (!/^\S+@\S+\.\S+$/.test(form.email)) {
      newErrors.email = 'Geçerli bir e-posta adresi girin.'
    }
    if (!form.password) {
      newErrors.password = 'Şifre gerekli.'
    } else if (form.password.length < 8) {
      newErrors.password = 'Şifre en az 8 karakter olmalı.'
    }
    if (form.confirmPassword !== form.password) {
      newErrors.confirmPassword = 'Şifreler eşleşmiyor.'
    }
    if (!form.acceptTerms) {
      newErrors.acceptTerms = 'Devam etmek için koşulları kabul edin.'
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
      const response = await fetch('http://localhost:8081/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          role: form.accountType,
          firstName: form.firstName,
          lastName: form.lastName,
          companyName: form.accountType === 'employer' ? form.companyName : undefined,
          email: form.email,
          password: form.password,
          termsAccepted: form.acceptTerms,
        }),
      })

      if (!response.ok) {
        const data = await response.json().catch(() => null)
        throw new Error(data?.message || 'Kayıt işlemi başarısız oldu.')
      }

      setSubmitted(true)
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
          <h1>Hesap Oluştur</h1>
          <p>Ücretsiz kayıt olun, size uygun iş ilanlarını kaçırmayın.</p>
        </div>

        {submitted && (
          <div className="form-success" role="status">
            Kaydınız başarıyla oluşturuldu!
          </div>
        )}

        {apiError && (
          <div className="form-error" role="alert">
            {apiError}
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label>Hesap Türü</label>
            <div className="account-type-toggle">
              <label className={`account-type-option ${form.accountType === 'candidate' ? 'active' : ''}`}>
                <input
                  type="radio"
                  name="accountType"
                  value="candidate"
                  checked={form.accountType === 'candidate'}
                  onChange={handleChange}
                />
                İş Arıyorum
              </label>
              <label className={`account-type-option ${form.accountType === 'employer' ? 'active' : ''}`}>
                <input
                  type="radio"
                  name="accountType"
                  value="employer"
                  checked={form.accountType === 'employer'}
                  onChange={handleChange}
                />
                İlan Vermek İstiyorum
              </label>
            </div>
          </div>

          <div className="form-field">
            <label htmlFor="firstName">Ad</label>
            <input
              id="firstName"
              name="firstName"
              type="text"
              placeholder="Adınız"
              value={form.firstName}
              onChange={handleChange}
              className={errors.firstName ? 'invalid' : ''}
            />
            {errors.firstName && <span className="field-error">{errors.firstName}</span>}
          </div>

          <div className="form-field">
            <label htmlFor="lastName">Soyad</label>
            <input
              id="lastName"
              name="lastName"
              type="text"
              placeholder="Soyadınız"
              value={form.lastName}
              onChange={handleChange}
              className={errors.lastName ? 'invalid' : ''}
            />
            {errors.lastName && <span className="field-error">{errors.lastName}</span>}
          </div>

          {form.accountType === 'employer' && (
            <div className="form-field">
              <label htmlFor="companyName">Şirket Adı</label>
              <input
                id="companyName"
                name="companyName"
                type="text"
                placeholder="Şirketinizin adı"
                value={form.companyName}
                onChange={handleChange}
                className={errors.companyName ? 'invalid' : ''}
              />
              {errors.companyName && <span className="field-error">{errors.companyName}</span>}
            </div>
          )}

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
              placeholder="En az 8 karakter"
              value={form.password}
              onChange={handleChange}
              className={errors.password ? 'invalid' : ''}
            />
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>

          <div className="form-field">
            <label htmlFor="confirmPassword">Şifre Tekrar</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              placeholder="Şifrenizi tekrar girin"
              value={form.confirmPassword}
              onChange={handleChange}
              className={errors.confirmPassword ? 'invalid' : ''}
            />
            {errors.confirmPassword && (
              <span className="field-error">{errors.confirmPassword}</span>
            )}
          </div>

          <div className="form-field">
            <label className="checkbox-field">
              <input
                type="checkbox"
                name="acceptTerms"
                checked={form.acceptTerms}
                onChange={handleChange}
              />
              Kullanım koşullarını ve gizlilik politikasını kabul ediyorum.
            </label>
            {errors.acceptTerms && (
              <span className="field-error">{errors.acceptTerms}</span>
            )}
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Gönderiliyor...' : 'Kayıt Ol'}
          </button>
        </form>

        <p className="auth-footer">
          Zaten hesabınız var mı? <Link to="/login">Giriş Yap</Link>
        </p>
      </div>
    </div>
  )
}

export default RegisterPage
