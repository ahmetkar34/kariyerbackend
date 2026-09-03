import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getAuth } from '../lib/auth'
import { createJob, getJobById, updateJob } from '../lib/jobsStore'
import { isEmployerRole } from '../lib/roles'
import './Employer.css'

const emptyForm = {
  title: '',
  company: '',
  location: '',
  type: 'Tam Zamanlı',
  remote: false,
  salary: '',
  tags: '',
  description: '',
  responsibilities: '',
  requirements: '',
  aboutCompany: '',
}

function JobFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [auth, setAuthState] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    const currentAuth = getAuth()
    if (!currentAuth) {
      navigate('/login')
      return
    }
    if (!isEmployerRole(currentAuth.user.role)) {
      navigate('/profile')
      return
    }
    setAuthState(currentAuth)

    if (isEdit) {
      getJobById(id)
        .then((job) => {
          if (!job || job.employerId !== currentAuth.user.id) {
            setNotFound(true)
            return
          }
          setForm({
            title: job.title || '',
            company: job.company || '',
            location: job.location || '',
            type: job.type || 'Tam Zamanlı',
            remote: Boolean(job.remote),
            salary: job.salary || '',
            tags: (job.tags || []).join(', '),
            description: job.description || '',
            responsibilities: (job.responsibilities || []).join('\n'),
            requirements: (job.requirements || []).join('\n'),
            aboutCompany: job.aboutCompany || '',
          })
        })
        .catch(() => setNotFound(true))
    } else {
      setForm((prev) => ({
        ...prev,
        company: currentAuth.user.companyName || '',
      }))
    }
  }, [id, isEdit, navigate])

  if (!auth) {
    return null
  }

  if (notFound) {
    return (
      <div className="employer-page">
        <div className="container job-not-found">
          <h1>İlan bulunamadı</h1>
          <p>Düzenlemek istediğiniz ilana ulaşılamadı.</p>
          <Link to="/isveren" className="btn btn-primary">
            İlanlarıma Dön
          </Link>
        </div>
      </div>
    )
  }

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  function validate() {
    const newErrors = {}
    if (!form.title.trim()) {
      newErrors.title = 'İlan başlığı gerekli.'
    }
    if (!form.company.trim()) {
      newErrors.company = 'Şirket adı gerekli.'
    }
    if (!form.location.trim()) {
      newErrors.location = 'Konum gerekli.'
    }
    if (!form.description.trim()) {
      newErrors.description = 'İlan açıklaması gerekli.'
    }
    return newErrors
  }

  function handleSubmit(e) {
    e.preventDefault()
    const newErrors = validate()
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) {
      return
    }

    const payload = {
      title: form.title.trim(),
      company: form.company.trim(),
      location: form.location.trim(),
      type: form.type,
      remote: form.remote,
      salary: form.salary.trim() || 'Görüşülür',
      tags: form.tags
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean),
      description: form.description.trim(),
      responsibilities: form.responsibilities
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean),
      requirements: form.requirements
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean),
      aboutCompany: form.aboutCompany.trim(),
    }

    if (isEdit) {
      updateJob(id, payload)
        .then(() => navigate(`/ilan/${id}`))
        .catch((err) => setErrors({ submit: err.message }))
    } else {
      createJob(payload)
        .then((job) => navigate(`/ilan/${job.id}`))
        .catch((err) => setErrors({ submit: err.message }))
    }
  }

  return (
    <div className="employer-page">
      <div className="container">
        <form className="card employer-form" onSubmit={handleSubmit} noValidate>
          <div className="employer-form-header">
            <h1>{isEdit ? 'İlanı Düzenle' : 'Yeni İlan Oluştur'}</h1>
            <p>İlan bilgilerini eksiksiz doldurarak adaylara ulaşın.</p>
          </div>

          {errors.submit && (
            <div className="form-error" role="alert">
              {errors.submit}
            </div>
          )}

          <div className="profile-grid">
            <div className="form-field">
              <label htmlFor="title">İlan Başlığı</label>
              <input
                id="title"
                name="title"
                value={form.title}
                onChange={handleChange}
                placeholder="Örn. Frontend Developer"
                className={errors.title ? 'invalid' : ''}
              />
              {errors.title && <span className="field-error">{errors.title}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="company">Şirket Adı</label>
              <input
                id="company"
                name="company"
                value={form.company}
                onChange={handleChange}
                placeholder="Şirket Adı"
                className={errors.company ? 'invalid' : ''}
              />
              {errors.company && <span className="field-error">{errors.company}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="location">Konum</label>
              <input
                id="location"
                name="location"
                value={form.location}
                onChange={handleChange}
                placeholder="İstanbul"
                className={errors.location ? 'invalid' : ''}
              />
              {errors.location && <span className="field-error">{errors.location}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="type">Çalışma Şekli</label>
              <select id="type" name="type" value={form.type} onChange={handleChange}>
                <option>Tam Zamanlı</option>
                <option>Yarı Zamanlı</option>
                <option>Staj</option>
                <option>Sözleşmeli</option>
              </select>
            </div>

            <div className="form-field">
              <label htmlFor="salary">Maaş Aralığı</label>
              <input
                id="salary"
                name="salary"
                value={form.salary}
                onChange={handleChange}
                placeholder="40.000 - 60.000 ₺"
              />
            </div>

            <div className="form-field">
              <label htmlFor="tags">Etiketler</label>
              <input
                id="tags"
                name="tags"
                value={form.tags}
                onChange={handleChange}
                placeholder="React, TypeScript, CSS"
              />
              <span className="employer-form-hint">Etiketleri virgülle ayırın.</span>
            </div>
          </div>

          <div className="form-field">
            <label className="checkbox-field">
              <input type="checkbox" name="remote" checked={form.remote} onChange={handleChange} />
              Uzaktan çalışmaya uygun
            </label>
          </div>

          <div className="form-field">
            <label htmlFor="description">İlan Açıklaması</label>
            <textarea
              id="description"
              name="description"
              rows={4}
              value={form.description}
              onChange={handleChange}
              placeholder="Pozisyon hakkında kısa bir açıklama yazın."
              className={errors.description ? 'invalid' : ''}
            />
            {errors.description && <span className="field-error">{errors.description}</span>}
          </div>

          <div className="form-field">
            <label htmlFor="responsibilities">Sorumluluklar</label>
            <textarea
              id="responsibilities"
              name="responsibilities"
              rows={4}
              value={form.responsibilities}
              onChange={handleChange}
              placeholder="Her satıra bir madde yazın"
            />
          </div>

          <div className="form-field">
            <label htmlFor="requirements">Aranan Nitelikler</label>
            <textarea
              id="requirements"
              name="requirements"
              rows={4}
              value={form.requirements}
              onChange={handleChange}
              placeholder="Her satıra bir madde yazın"
            />
          </div>

          <div className="form-field">
            <label htmlFor="aboutCompany">Şirket Hakkında</label>
            <textarea
              id="aboutCompany"
              name="aboutCompany"
              rows={3}
              value={form.aboutCompany}
              onChange={handleChange}
              placeholder="Şirketinizi kısaca tanıtın."
            />
          </div>

          <div className="employer-form-actions">
            <Link to="/isveren" className="btn btn-outline">
              Vazgeç
            </Link>
            <button type="submit" className="btn btn-primary">
              {isEdit ? 'Değişiklikleri Kaydet' : 'İlanı Yayınla'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default JobFormPage
