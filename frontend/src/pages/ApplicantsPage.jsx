import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getAuth } from '../lib/auth'
import { getApplicants, getJobById } from '../lib/jobsStore'
import './Employer.css'

function isEmployerRole(role) {
  return typeof role === 'string' && role.toLowerCase().includes('employer')
}

function ApplicantsPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [job, setJob] = useState(null)
  const [applicants, setApplicants] = useState([])
  const [loading, setLoading] = useState(true)
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

    setLoading(true)
    getJobById(id)
      .then((jobData) => {
        if (!jobData || jobData.employerId !== currentAuth.user.id) {
          setNotFound(true)
          return
        }
        setJob(jobData)
        return getApplicants(id).then(setApplicants)
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false))
  }, [id, navigate])

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
                </div>
                <span>{new Date(applicant.appliedAt).toLocaleDateString('tr-TR')}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default ApplicantsPage
