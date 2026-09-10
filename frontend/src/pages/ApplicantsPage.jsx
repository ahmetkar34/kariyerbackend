import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getApplicants, getJobById, updateApplicationStatus } from '../lib/jobsStore'
import { isEmployerRole } from '../lib/roles'
import { useRequireAuth } from '../lib/useRequireAuth'
import './Employer.css'

const statusLabels = {
  PENDING: 'İnceleniyor',
  ACCEPTED: 'Kabul Edildi',
  REJECTED: 'Reddedildi',
}

function ApplicantsPage() {
  const { id } = useParams()
  const auth = useRequireAuth({ allowRole: isEmployerRole })
  const [job, setJob] = useState(null)
  const [applicants, setApplicants] = useState([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [updatingId, setUpdatingId] = useState(null)

  useEffect(() => {
    if (!auth) return

    setLoading(true)
    getJobById(id)
      .then((jobData) => {
        if (!jobData || jobData.employerId !== auth.user.id) {
          setNotFound(true)
          return
        }
        setJob(jobData)
        return getApplicants(id).then(setApplicants)
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false))
  }, [id, auth])

  function handleStatusChange(applicationId, status) {
    setUpdatingId(applicationId)
    updateApplicationStatus(id, applicationId, status)
      .then((updated) => {
        setApplicants((prev) => prev.map((a) => (a.id === applicationId ? updated : a)))
      })
      .finally(() => setUpdatingId(null))
  }

  if (loading) {
    return null
  }

  if (notFound) {
    return (
      <div className="employer-page">
        <div className="container job-not-found">
          <h1>İlan bulunamadı</h1>
          <p>Başvuranlarını görüntülemek istediğiniz ilana ulaşılamadı.</p>
          <Link to="/isveren" className="btn btn-primary">
            İlanlarıma Dön
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="employer-page">
      <div className="container">
        <div className="employer-header">
          <div>
            <h1>Başvuranlar</h1>
            <p>
              <strong>{job.title}</strong> ilanına {applicants.length} başvuru alındı.
            </p>
          </div>
          <Link to={`/ilan/${job.id}`} className="btn btn-outline">
            İlana Dön
          </Link>
        </div>

        {applicants.length === 0 ? (
          <div className="card employer-empty">
            <p className="empty-hint">Bu ilana henüz başvuru yapılmadı.</p>
          </div>
        ) : (
          <div className="employer-job-list">
            {applicants.map((applicant) => (
              <div className="card employer-job-item" key={applicant.id}>
                <div>
                  <h3>
                    {applicant.firstName} {applicant.lastName}
                  </h3>
                  <p className="job-card-company">{applicant.email}</p>
                  <span className={`tag status-${applicant.status.toLowerCase()}`}>
                    {statusLabels[applicant.status]}
                  </span>
                </div>
                <div className="employer-job-actions">
                  <span>{new Date(applicant.appliedAt).toLocaleDateString('tr-TR')}</span>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    disabled={applicant.status === 'ACCEPTED' || updatingId === applicant.id}
                    onClick={() => handleStatusChange(applicant.id, 'ACCEPTED')}
                  >
                    Kabul Et
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    disabled={applicant.status === 'REJECTED' || updatingId === applicant.id}
                    onClick={() => handleStatusChange(applicant.id, 'REJECTED')}
                  >
                    Reddet
                  </button>
                </div>
                {applicant.coverLetter && <p className="applicant-cover-letter">{applicant.coverLetter}</p>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default ApplicantsPage
