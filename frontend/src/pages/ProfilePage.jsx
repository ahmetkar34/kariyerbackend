import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAuth } from '../lib/auth'
import { getProfile, saveProfile } from '../lib/profile'
import './Profile.css'

const emptyProfile = {
  photo: null,
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  title: '',
  summary: '',
  education: [],
  certificates: [],
}

function createId() {
  return crypto.randomUUID()
}

function ProfilePage() {
  const navigate = useNavigate()
  const [auth, setAuth] = useState(null)
  const [profile, setProfile] = useState(emptyProfile)
  const [loading, setLoading] = useState(true)
  const [saved, setSaved] = useState(false)
  const [saveError, setSaveError] = useState('')

  useEffect(() => {
    const currentAuth = getAuth()
    if (!currentAuth) {
      navigate('/login')
      return
    }
    setAuth(currentAuth)
    getProfile()
      .then((remoteProfile) => {
        setProfile({
          ...emptyProfile,
          ...remoteProfile,
          firstName: currentAuth.user.firstName || '',
          lastName: currentAuth.user.lastName || '',
          email: currentAuth.user.email || '',
        })
      })
      .catch(() => {
        setProfile({
          ...emptyProfile,
          firstName: currentAuth.user.firstName || '',
          lastName: currentAuth.user.lastName || '',
          email: currentAuth.user.email || '',
        })
      })
      .finally(() => setLoading(false))
  }, [navigate])

  if (!auth || loading) {
    return null
  }

  function handleFieldChange(e) {
    const { name, value } = e.target
    setProfile((prev) => ({ ...prev, [name]: value }))
  }

  function handlePhotoChange(e) {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      setProfile((prev) => ({ ...prev, photo: reader.result }))
    }
    reader.readAsDataURL(file)
  }

  function removePhoto() {
    setProfile((prev) => ({ ...prev, photo: null }))
  }

  function addEducation() {
    setProfile((prev) => ({
      ...prev,
      education: [
        ...prev.education,
        { id: createId(), school: '', degree: '', startYear: '', endYear: '' },
      ],
    }))
  }

  function updateEducation(id, field, value) {
    setProfile((prev) => ({
      ...prev,
      education: prev.education.map((edu) => (edu.id === id ? { ...edu, [field]: value } : edu)),
    }))
  }

  function removeEducation(id) {
    setProfile((prev) => ({
      ...prev,
      education: prev.education.filter((edu) => edu.id !== id),
    }))
  }

  function addCertificate() {
    setProfile((prev) => ({
      ...prev,
      certificates: [...prev.certificates, { id: createId(), name: '', issuer: '', year: '' }],
    }))
  }

  function updateCertificate(id, field, value) {
    setProfile((prev) => ({
      ...prev,
      certificates: prev.certificates.map((cert) =>
        cert.id === id ? { ...cert, [field]: value } : cert,
      ),
    }))
  }

  function removeCertificate(id) {
    setProfile((prev) => ({
      ...prev,
      certificates: prev.certificates.filter((cert) => cert.id !== id),
    }))
  }

  function handleSave(e) {
    e.preventDefault()
    setSaveError('')
    const payload = {
      ...profile,
      education: profile.education.filter(
        (edu) => edu.school.trim() || edu.degree.trim() || edu.startYear.trim() || edu.endYear.trim(),
      ),
      certificates: profile.certificates.filter(
        (cert) => cert.name.trim() || cert.issuer.trim() || cert.year.trim(),
      ),
    }
    saveProfile(payload)
      .then((savedProfile) => {
        setProfile((prev) => ({ ...prev, ...savedProfile }))
        setSaved(true)
        setTimeout(() => setSaved(false), 2000)
      })
      .catch((err) => setSaveError(err.message || 'Profil kaydedilemedi.'))
  }

  function handleDownloadPdf() {
    window.print()
  }

  const initials = `${profile.firstName[0] || ''}${profile.lastName[0] || ''}`.toUpperCase()

  return (
    <div className="profile-page">
      <div className="container">
        <form className="profile-form no-print" onSubmit={handleSave}>
          <div className="profile-header">
            <h1>Profilim</h1>
            <p>Bilgilerini güncelle, dilediğinde CV olarak indir.</p>
          </div>

          <div className="card profile-section avatar-section">
            <div className="avatar-preview">
              {profile.photo ? (
                <img src={profile.photo} alt="Profil fotoğrafı" />
              ) : (
                <span className="avatar-placeholder">{initials || '?'}</span>
              )}
            </div>
            <div className="avatar-actions">
              <label className="btn btn-outline">
                Fotoğraf Yükle
                <input type="file" accept="image/*" onChange={handlePhotoChange} hidden />
              </label>
              {profile.photo && (
                <button type="button" className="link-btn" onClick={removePhoto}>
                  Fotoğrafı kaldır
                </button>
              )}
            </div>
          </div>

          <div className="card profile-section">
            <h2>Kişisel Bilgiler</h2>
            <div className="profile-grid">
              <div className="form-field">
                <label htmlFor="firstName">Ad</label>
                <input id="firstName" value={profile.firstName} disabled />
              </div>
              <div className="form-field">
                <label htmlFor="lastName">Soyad</label>
                <input id="lastName" value={profile.lastName} disabled />
              </div>
              <div className="form-field">
                <label htmlFor="email">E-posta</label>
                <input id="email" value={profile.email} disabled />
              </div>
              <div className="form-field">
                <label htmlFor="phone">Telefon</label>
                <input
                  id="phone"
                  name="phone"
                  value={profile.phone}
                  onChange={handleFieldChange}
                  placeholder="05xx xxx xx xx"
                />
              </div>
              <div className="form-field profile-grid-span">
                <label htmlFor="title">Ünvan</label>
                <input
                  id="title"
                  name="title"
                  value={profile.title}
                  onChange={handleFieldChange}
                  placeholder="Örn. Frontend Developer"
                />
              </div>
            </div>
            <div className="form-field">
              <label htmlFor="summary">Hakkımda</label>
              <textarea
                id="summary"
                name="summary"
                rows={4}
                value={profile.summary}
                onChange={handleFieldChange}
                placeholder="Kendinizden kısaca bahsedin..."
              />
            </div>
          </div>

          <div className="card profile-section">
            <div className="section-header">
              <h2>Eğitim</h2>
              <button type="button" className="btn btn-outline btn-sm" onClick={addEducation}>
                + Ekle
              </button>
            </div>
            {profile.education.length === 0 && (
              <p className="empty-hint">Henüz eğitim bilgisi eklenmedi.</p>
            )}
            {profile.education.map((edu) => (
              <div className="list-item" key={edu.id}>
                <div className="profile-grid">
                  <div className="form-field">
                    <label>Okul</label>
                    <input
                      value={edu.school}
                      onChange={(e) => updateEducation(edu.id, 'school', e.target.value)}
                      placeholder="Üniversite / Okul"
                    />
                  </div>
                  <div className="form-field">
                    <label>Bölüm / Derece</label>
                    <input
                      value={edu.degree}
                      onChange={(e) => updateEducation(edu.id, 'degree', e.target.value)}
                      placeholder="Bilgisayar Mühendisliği"
                    />
                  </div>
                  <div className="form-field">
                    <label>Başlangıç</label>
                    <input
                      value={edu.startYear}
                      onChange={(e) => updateEducation(edu.id, 'startYear', e.target.value)}
                      placeholder="2019"
                    />
                  </div>
                  <div className="form-field">
                    <label>Bitiş</label>
                    <input
                      value={edu.endYear}
                      onChange={(e) => updateEducation(edu.id, 'endYear', e.target.value)}
                      placeholder="2023 / Devam ediyor"
                    />
                  </div>
                </div>
                <button type="button" className="remove-btn" onClick={() => removeEducation(edu.id)}>
                  Kaldır
                </button>
              </div>
            ))}
          </div>

          <div className="card profile-section">
            <div className="section-header">
              <h2>Sertifikalar</h2>
              <button type="button" className="btn btn-outline btn-sm" onClick={addCertificate}>
                + Ekle
              </button>
            </div>
            {profile.certificates.length === 0 && (
              <p className="empty-hint">Henüz sertifika eklenmedi.</p>
            )}
            {profile.certificates.map((cert) => (
              <div className="list-item" key={cert.id}>
                <div className="profile-grid">
                  <div className="form-field">
                    <label>Sertifika Adı</label>
                    <input
                      value={cert.name}
                      onChange={(e) => updateCertificate(cert.id, 'name', e.target.value)}
                      placeholder="AWS Certified Developer"
                    />
                  </div>
                  <div className="form-field">
                    <label>Kurum</label>
                    <input
                      value={cert.issuer}
                      onChange={(e) => updateCertificate(cert.id, 'issuer', e.target.value)}
                      placeholder="Amazon Web Services"
                    />
                  </div>
                  <div className="form-field">
                    <label>Yıl</label>
                    <input
                      value={cert.year}
                      onChange={(e) => updateCertificate(cert.id, 'year', e.target.value)}
                      placeholder="2024"
                    />
                  </div>
                </div>
                <button
                  type="button"
                  className="remove-btn"
                  onClick={() => removeCertificate(cert.id)}
                >
                  Kaldır
                </button>
              </div>
            ))}
          </div>

          <div className="profile-actions">
            {saveError && (
              <span className="form-error" role="alert">
                {saveError}
              </span>
            )}
            {saved && (
              <span className="form-success" role="status">
                Profil kaydedildi.
              </span>
            )}
            <button type="button" className="btn btn-outline" onClick={handleDownloadPdf}>
              CV'yi PDF Olarak İndir
            </button>
            <button type="submit" className="btn btn-primary">
              Kaydet
            </button>
          </div>
        </form>

        <CvPreview profile={profile} initials={initials} />
      </div>
    </div>
  )
}

function CvPreview({ profile, initials }) {
  return (
    <div className="cv-print">
      <div className="cv-page">
        <div className="cv-header">
          <div className="cv-avatar">
            {profile.photo ? (
              <img src={profile.photo} alt="Profil fotoğrafı" />
            ) : (
              <span>{initials || '?'}</span>
            )}
          </div>
          <div>
            <h1 className="cv-name">
              {profile.firstName} {profile.lastName}
            </h1>
            {profile.title && <p className="cv-title">{profile.title}</p>}
            <p className="cv-contact">
              {profile.email}
              {profile.phone && ` • ${profile.phone}`}
            </p>
          </div>
        </div>

        {profile.summary && (
          <div className="cv-section">
            <h2>Hakkımda</h2>
            <p>{profile.summary}</p>
          </div>
        )}

        {profile.education.length > 0 && (
          <div className="cv-section">
            <h2>Eğitim</h2>
            {profile.education.map((edu) => (
              <div className="cv-item" key={edu.id}>
                <div className="cv-item-header">
                  <strong>{edu.school}</strong>
                  <span>
                    {edu.startYear}
                    {edu.endYear && ` - ${edu.endYear}`}
                  </span>
                </div>
                {edu.degree && <p>{edu.degree}</p>}
              </div>
            ))}
          </div>
        )}

        {profile.certificates.length > 0 && (
          <div className="cv-section">
            <h2>Sertifikalar</h2>
            {profile.certificates.map((cert) => (
              <div className="cv-item" key={cert.id}>
                <div className="cv-item-header">
                  <strong>{cert.name}</strong>
                  <span>{cert.year}</span>
                </div>
                {cert.issuer && <p>{cert.issuer}</p>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default ProfilePage
