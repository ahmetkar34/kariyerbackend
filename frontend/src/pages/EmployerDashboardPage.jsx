import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getAuth } from '../lib/auth'
import { deleteJob, getMyJobs } from '../lib/jobsStore'
import { isEmployerRole } from '../lib/roles'
import './Employer.css'

function EmployerDashboardPage() {
  const navigate = useNavigate()
  const [auth, setAuthState] = useState(null)
  const [postings, setPostings] = useState([])

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
    getMyJobs()
      .then(setPostings)
      .catch(() => setPostings([]))
  }, [navigate])

  if (!auth) {
    return null
  }

  function handleDelete(id) {
    if (!window.confirm('Bu ilanı silmek istediğinize emin misiniz?')) {
      return
    }
    deleteJob(id).then(() =>
      getMyJobs()
        .then(setPostings)
        .catch(() => setPostings([]))
    )
  }

  return (
    <div className="employer-page">
      <div className="container">
        <div className="employer-header">
          <div>
            <h1>İlanlarım</h1>
            <p>Şirketiniz adına yayınladığınız iş ilanlarını yönetin.</p>
          </div>
          <Link to="/isveren/ilan-olustur" className="btn btn-primary">
            + Yeni İlan Oluştur
          </Link>
        </div>

        {postings.length === 0 ? (
          <div className="card employer-empty">
            <p className="empty-hint">Henüz bir ilan oluşturmadınız.</p>
            <Link to="/isveren/ilan-olustur" className="btn btn-primary">
              İlk İlanınızı Oluşturun
            </Link>
          </div>
        ) : (
          <div className="employer-job-list">
            {postings.map((job) => (
              <div className="card employer-job-item" key={job.id}>
                <div>
                  <h3>{job.title}</h3>
                  <p className="job-card-company">{job.company}</p>
                  <div className="job-card-tags">
                    {(job.tags || []).map((tag) => (
                      <span key={tag} className="tag">
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="employer-job-actions">
                  <Link to={`/ilan/${job.id}`} className="btn btn-outline btn-sm">
                    Görüntüle
                  </Link>
                  <Link to={`/isveren/ilan/${job.id}/basvuranlar`} className="btn btn-outline btn-sm">
                    Başvuranlar
                  </Link>
                  <Link to={`/isveren/ilan/${job.id}/duzenle`} className="btn btn-outline btn-sm">
                    Düzenle
                  </Link>
                  <button
                    type="button"
                    className="remove-btn"
                    onClick={() => handleDelete(job.id)}
                  >
                    Sil
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default EmployerDashboardPage
