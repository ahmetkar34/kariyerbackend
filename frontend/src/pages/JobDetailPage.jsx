import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getAuth } from '../lib/auth'
import { applyToJob, deleteJob, getApplicationStatus, getJobById } from '../lib/jobsStore'
import { isEmployerRole } from '../lib/roles'
import './JobDetailPage.css'

function JobDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [job, setJob] = useState(null)
  const [loading, setLoading] = useState(true)
  const [applied, setApplied] = useState(false)
  const [applying, setApplying] = useState(false)
  const [applyError, setApplyError] = useState('')
  const auth = getAuth()
  const isOwner = Boolean(auth && job && job.employerId === auth.user.id)
  const isCandidate = Boolean(auth && !isEmployerRole(auth.user.role))

  useEffect(() => {
    setLoading(true)
    getJobById(id)
      .then(setJob)
      .catch(() => setJob(null))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    if (!auth || !isCandidate) return
    getApplicationStatus(id)
      .then((status) => setApplied(status.applied))
      .catch(() => {})
  }, [id, auth, isCandidate])

  function handleDelete() {
    if (!window.confirm('Bu ilanı silmek istediğinize emin misiniz?')) return
    deleteJob(job.id).then(() => navigate('/isveren'))
  }

  function handleApply() {
    setApplying(true)
    setApplyError('')
    applyToJob(job.id)
      .then(() => setApplied(true))
      .catch((err) => setApplyError(err.message || 'Başvuru gönderilemedi.'))
      .finally(() => setApplying(false))
  }

  if (loading) {
    return null
  }

  if (!job) {
    return (
      <div className="job-detail-page">
        <div className="container job-not-found">
          <h1>İlan bulunamadı</h1>
          <p>Aradığınız ilan kaldırılmış veya hiç var olmamış olabilir.</p>
          <Link to="/" className="btn btn-primary">
            Tüm İlanlara Dön
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="job-detail-page">
      <div className="container job-detail-layout">
        <Link to="/" className="back-link">
          ← Tüm İlanlar
        </Link>

        <div className="card job-detail-header">
          <div className="job-detail-heading">
            <div>
              <h1>{job.title}</h1>
              <p className="job-detail-company">{job.company}</p>
            </div>
            <span className="job-card-type">{job.type}</span>
          </div>

          <div className="job-detail-meta">
            <span>
              📍 {job.location}
              {job.remote ? ' · Uzaktan' : ''}
            </span>
            <span>💰 {job.salary}</span>
            <span>{new Date(job.createdAt).toLocaleDateString('tr-TR')}</span>
          </div>

          <div className="job-card-tags">
            {job.tags.map((tag) => (
              <span key={tag} className="tag">
                {tag}
              </span>
            ))}
          </div>

          {isOwner ? (
            <div className="job-detail-owner-actions">
              <Link to={`/isveren/ilan/${job.id}/basvuranlar`} className="btn btn-primary">
                Başvuranları Görüntüle
              </Link>
              <Link to={`/isveren/ilan/${job.id}/duzenle`} className="btn btn-outline">
                İlanı Düzenle
              </Link>
              <button type="button" className="btn btn-outline" onClick={handleDelete}>
                İlanı Sil
              </button>
            </div>
          ) : !auth ? (
            <Link to="/login" className="btn btn-primary btn-block">
              Başvurmak için giriş yapın
            </Link>
          ) : !isCandidate ? null : applied ? (
            <div className="form-success" role="status">
              Başvurunuz alındı! Şirket ekibi en kısa sürede sizinle iletişime geçecek.
            </div>
          ) : (
            <>
              {applyError && (
                <div className="form-error" role="alert">
                  {applyError}
                </div>
              )}
              <button
                type="button"
                className="btn btn-primary btn-block"
                onClick={handleApply}
                disabled={applying}
              >
                {applying ? 'Gönderiliyor...' : 'Hemen Başvur'}
              </button>
            </>
          )}
        </div>

        <div className="card job-detail-section">
          <h2>İlan Açıklaması</h2>
          <p>{job.description}</p>
        </div>

        {job.responsibilities?.length > 0 && (
          <div className="card job-detail-section">
            <h2>Sorumluluklar</h2>
            <ul>
              {job.responsibilities.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </div>
        )}

        {job.requirements?.length > 0 && (
          <div className="card job-detail-section">
            <h2>Aranan Nitelikler</h2>
            <ul>
              {job.requirements.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </div>
        )}

        {job.aboutCompany && (
          <div className="card job-detail-section">
            <h2>Şirket Hakkında</h2>
            <p>{job.aboutCompany}</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default JobDetailPage
