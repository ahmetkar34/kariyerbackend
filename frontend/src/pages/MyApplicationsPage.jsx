import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyApplications } from '../lib/applications'
import { isEmployerRole } from '../lib/roles'
import { useRequireAuth } from '../lib/useRequireAuth'
import './Employer.css'

const statusLabels = {
  PENDING: 'İnceleniyor',
  ACCEPTED: 'Kabul Edildi',
  REJECTED: 'Reddedildi',
}

function MyApplicationsPage() {
  const auth = useRequireAuth({ allowRole: (role) => !isEmployerRole(role), redirectTo: '/isveren' })
  const [applications, setApplications] = useState([])

  useEffect(() => {
    if (!auth) return
    getMyApplications()
      .then(setApplications)
      .catch(() => setApplications([]))
  }, [auth])

  if (!auth) {
    return null
  }

  return (
    <div className="employer-page">
      <div className="container">
        <div className="employer-header">
          <div>
            <h1>Başvurularım</h1>
            <p>Başvurduğunuz ilanları ve durumlarını buradan takip edebilirsiniz.</p>
          </div>
        </div>

        {applications.length === 0 ? (
          <div className="card employer-empty">
            <p className="empty-hint">Henüz hiçbir ilana başvurmadınız.</p>
            <Link to="/" className="btn btn-primary">
              İlanları Keşfet
            </Link>
          </div>
        ) : (
          <div className="employer-job-list">
            {applications.map((app) => (
              <div className="card employer-job-item" key={app.id}>
                <div>
                  <h3>{app.jobTitle}</h3>
                  <p className="job-card-company">{app.jobCompany}</p>
                  <span className={`tag status-${app.status.toLowerCase()}`}>
                    {statusLabels[app.status]}
                  </span>
                </div>
                <div className="employer-job-actions">
                  <span>{new Date(app.appliedAt).toLocaleDateString('tr-TR')}</span>
                  <Link to={`/ilan/${app.jobPostingId}`} className="btn btn-outline btn-sm">
                    İlanı Görüntüle
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MyApplicationsPage
